package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DistanceState
import com.example.data.model.ParentSettings
import com.example.data.model.ViolationLog
import com.example.data.model.ViolationType
import com.example.data.repository.KidSafeRepository
import com.example.notification.KidSafeNotificationManager
import com.example.sensor.EyeDistanceDetector
import com.example.service.KidSafeGuardService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

data class ContentSafetyResult(
    val isSafe: Boolean,
    val reason: String = "",
    val matchedTerm: String = ""
)

class KidSafeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = KidSafeRepository(application)
    private val detector = EyeDistanceDetector(application)
    private val notificationManager = KidSafeNotificationManager(application)

    val settings: StateFlow<ParentSettings> = repository.settings
    val logs: StateFlow<List<ViolationLog>> = repository.logs
    val isParentUnlocked: StateFlow<Boolean> = repository.isParentUnlocked

    private val _distanceState = MutableStateFlow<DistanceState>(DistanceState.Safe(38))
    val distanceState: StateFlow<DistanceState> = _distanceState.asStateFlow()

    private val _isInAppBlackout = MutableStateFlow(false)
    val isInAppBlackout: StateFlow<Boolean> = _isInAppBlackout.asStateFlow()

    private val _blackoutReason = MutableStateFlow("")
    val blackoutReason: StateFlow<String> = _blackoutReason.asStateFlow()

    private val _currentDistanceForBlackout = MutableStateFlow(18)
    val currentDistanceForBlackout: StateFlow<Int> = _currentDistanceForBlackout.asStateFlow()

    // 20-20-20 Rule Timer
    private val _eyeRestTimerSeconds = MutableStateFlow(0)
    val eyeRestTimerSeconds: StateFlow<Int> = _eyeRestTimerSeconds.asStateFlow()

    private val _isRestBreakActive = MutableStateFlow(false)
    val isRestBreakActive: StateFlow<Boolean> = _isRestBreakActive.asStateFlow()

    private var restJob: Job? = null
    private var screenTimeJob: Job? = null

    init {
        // Collect sensor state
        viewModelScope.launch {
            detector.distanceState.collectLatest { state ->
                _distanceState.value = state
                val s = settings.value
                if (s.isGuardActive && s.isDistanceGuardEnabled) {
                    when (state) {
                        is DistanceState.TooClose -> {
                            _currentDistanceForBlackout.value = state.distanceCm
                            _blackoutReason.value = "Bé nhìn quá gần (${state.distanceCm}cm < ${s.safeDistanceCm}cm)"
                            if (s.autoBlackoutScreen) {
                                _isInAppBlackout.value = true
                            }
                            repository.addLog(
                                ViolationType.TOO_CLOSE,
                                "Nhìn quá gần ${state.distanceCm}cm",
                                state.distanceCm
                            )
                        }
                        is DistanceState.Caution -> {
                            _currentDistanceForBlackout.value = state.distanceCm
                        }
                        is DistanceState.Safe -> {
                            _currentDistanceForBlackout.value = state.distanceCm
                            if (_isInAppBlackout.value && _blackoutReason.value.contains("quá gần")) {
                                delay(1000)
                                _isInAppBlackout.value = false
                            }
                        }
                        DistanceState.Calibrating -> {}
                    }
                }
            }
        }

        // Start detector if active
        if (settings.value.isGuardActive) {
            detector.startListening(settings.value.safeDistanceCm)
            startTimers()
        }
    }

    private fun startTimers() {
        screenTimeJob?.cancel()
        screenTimeJob = viewModelScope.launch {
            while (true) {
                delay(60_000) // 1 minute tick
                val s = settings.value
                if (s.isGuardActive) {
                    val newUsed = s.usedMinutesToday + 1
                    val eightyPercent = (s.dailyScreenLimitMinutes * 0.80).toInt()
                    var notified = s.hasNotified80Percent

                    // 80% Screen Time Alert
                    if (s.notifyAt80Percent && !notified && newUsed >= eightyPercent && newUsed < s.dailyScreenLimitMinutes) {
                        notificationManager.send80PercentScreenTimeAlert(newUsed, s.dailyScreenLimitMinutes)
                        repository.addLog(
                            ViolationType.WARNING_80_PERCENT,
                            "Đạt 80% giới hạn xem ($newUsed/${s.dailyScreenLimitMinutes} phút - còn ${s.dailyScreenLimitMinutes - newUsed} phút)"
                        )
                        notified = true
                    }

                    // Check Bedtime
                    checkBedtime()

                    // Check Daily Limit
                    if (newUsed >= s.dailyScreenLimitMinutes) {
                        _blackoutReason.value = "Đã hết thời gian sử dụng điện thoại hôm nay (${s.dailyScreenLimitMinutes} phút)!"
                        _isInAppBlackout.value = true
                        notificationManager.sendTimeUpAlert(s.dailyScreenLimitMinutes)
                        repository.addLog(ViolationType.TIME_LIMIT, "Đã dùng hết ${s.dailyScreenLimitMinutes} phút cho phép")
                    }

                    repository.updateSettings(s.copy(usedMinutesToday = newUsed, hasNotified80Percent = notified))
                }
            }
        }

        // 20-20-20 Rule Counter
        restJob?.cancel()
        restJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val s = settings.value
                if (s.isGuardActive && s.isSessionRestEnabled) {
                    _eyeRestTimerSeconds.value += 1
                    val targetSeconds = s.sessionRestMinutes * 60
                    if (_eyeRestTimerSeconds.value >= targetSeconds) {
                        _isRestBreakActive.value = true
                    }
                }
            }
        }
    }

    private fun checkBedtime() {
        val s = settings.value
        if (!s.isBedtimeLockEnabled) return
        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMinute = cal.get(Calendar.MINUTE)
        val currentTimeInMinutes = currentHour * 60 + currentMinute
        val bedStartInMinutes = s.bedtimeStartHour * 60 + s.bedtimeStartMinute
        val bedEndInMinutes = s.bedtimeEndHour * 60 + s.bedtimeEndMinute

        val isBedtime = if (bedStartInMinutes > bedEndInMinutes) {
            // Over midnight (e.g. 21:00 to 06:00)
            currentTimeInMinutes >= bedStartInMinutes || currentTimeInMinutes < bedEndInMinutes
        } else {
            currentTimeInMinutes in bedStartInMinutes until bedEndInMinutes
        }

        if (isBedtime) {
            _blackoutReason.value = "Đã đến giờ đi ngủ (${s.bedtimeStartHour}:00 - ${s.bedtimeEndHour}:00)! Bé hãy đi ngủ sớm để mắt luôn sáng khỏe nhé."
            _isInAppBlackout.value = true
            repository.addLog(ViolationType.BEDTIME, "Đã kích hoạt khóa giờ đi ngủ")
        }
    }

    fun dismissEyeRestBreak() {
        _isRestBreakActive.value = false
        _eyeRestTimerSeconds.value = 0
    }

    fun acceptTermsAndStartGuard(pin: String) {
        val current = settings.value
        val updated = current.copy(
            isTermsAccepted = true,
            isGuardActive = true,
            parentPin = pin.ifBlank { "1234" }
        )
        repository.updateSettings(updated)
        KidSafeGuardService.startService(getApplication())
        detector.startListening(updated.safeDistanceCm)
        startTimers()
    }

    fun toggleGuard(active: Boolean) {
        val current = settings.value
        val updated = current.copy(isGuardActive = active)
        repository.updateSettings(updated)
        if (active) {
            KidSafeGuardService.startService(getApplication())
            detector.startListening(updated.safeDistanceCm)
            startTimers()
        } else {
            KidSafeGuardService.stopService(getApplication())
            detector.stopListening()
            screenTimeJob?.cancel()
            restJob?.cancel()
            _isInAppBlackout.value = false
        }
    }

    fun updateSettings(newSettings: ParentSettings) {
        repository.updateSettings(newSettings)
        detector.setSafeThreshold(newSettings.safeDistanceCm)
    }

    fun simulateDistance(distanceCm: Int) {
        val s = settings.value
        detector.updateSimulatedDistance(distanceCm, s.warningSoundEnabled, s.vibrationEnabled)
        _currentDistanceForBlackout.value = distanceCm
        if (distanceCm < s.safeDistanceCm) {
            _blackoutReason.value = "Bé nhìn quá gần (${distanceCm}cm < ${s.safeDistanceCm}cm)"
            if (s.autoBlackoutScreen && s.isGuardActive) {
                _isInAppBlackout.value = true
            }
        } else {
            if (_isInAppBlackout.value && _blackoutReason.value.contains("quá gần")) {
                _isInAppBlackout.value = false
            }
        }
    }

    fun triggerTestBlackout() {
        _currentDistanceForBlackout.value = 16
        _blackoutReason.value = "Mô phỏng: Bé đang nhìn ở khoảng cách 16cm (Quá gần! Màn hình tự động tắt để bảo vệ mắt)"
        _isInAppBlackout.value = true
        detector.triggerAlerts(settings.value.warningSoundEnabled, settings.value.vibrationEnabled)
    }

    fun dismissBlackoutWithPin(pin: String): Boolean {
        if (repository.verifyPin(pin)) {
            _isInAppBlackout.value = false
            return true
        }
        return false
    }

    fun verifyPin(pin: String): Boolean {
        return repository.verifyPin(pin)
    }

    fun unlockParent(pin: String): Boolean {
        val isValid = repository.verifyPin(pin)
        if (isValid) {
            repository.setParentUnlocked(true)
        }
        return isValid
    }

    fun lockParent() {
        repository.setParentUnlocked(false)
    }

    fun clearAllLogs() {
        repository.clearLogs()
    }

    fun resetDailyTime() {
        val updated = settings.value.copy(
            usedMinutesToday = 0,
            hasNotified80Percent = false
        )
        repository.updateSettings(updated)
        if (_blackoutReason.value.contains("thời gian sử dụng")) {
            _isInAppBlackout.value = false
        }
    }

    fun testSend80PercentAlert() {
        val s = settings.value
        val eightyPercent = (s.dailyScreenLimitMinutes * 0.80).toInt()
        notificationManager.send80PercentScreenTimeAlert(eightyPercent, s.dailyScreenLimitMinutes)
        repository.addLog(
            ViolationType.WARNING_80_PERCENT,
            "Thử nghiệm gửi cảnh báo 80% thời gian ($eightyPercent/${s.dailyScreenLimitMinutes} phút)"
        )
    }

    fun simulateScreenTime(minutes: Int) {
        val s = settings.value
        val newUsed = minutes.coerceAtLeast(0)
        val eightyPercent = (s.dailyScreenLimitMinutes * 0.80).toInt()
        var notified = s.hasNotified80Percent

        if (s.notifyAt80Percent && !notified && newUsed >= eightyPercent && newUsed < s.dailyScreenLimitMinutes) {
            notificationManager.send80PercentScreenTimeAlert(newUsed, s.dailyScreenLimitMinutes)
            repository.addLog(
                ViolationType.WARNING_80_PERCENT,
                "Đạt 80% giới hạn xem ($newUsed/${s.dailyScreenLimitMinutes} phút - còn ${s.dailyScreenLimitMinutes - newUsed} phút)"
            )
            notified = true
        }

        if (newUsed >= s.dailyScreenLimitMinutes) {
            _blackoutReason.value = "Đã hết thời gian sử dụng điện thoại hôm nay (${s.dailyScreenLimitMinutes} phút)!"
            _isInAppBlackout.value = true
            notificationManager.sendTimeUpAlert(s.dailyScreenLimitMinutes)
        } else if (_blackoutReason.value.contains("thời gian sử dụng")) {
            _isInAppBlackout.value = false
        }

        repository.updateSettings(s.copy(usedMinutesToday = newUsed, hasNotified80Percent = notified))
    }

    fun checkUrlOrContent(query: String): ContentSafetyResult {
        val s = settings.value
        if (!s.isContentFilterEnabled) return ContentSafetyResult(isSafe = true)

        val lower = query.lowercase().trim()

        // Check custom blocked URLs
        for (url in s.customBlockedUrls) {
            if (lower.contains(url.lowercase())) {
                repository.addLog(ViolationType.BLOCKED_CONTENT, "Truy cập URL bị chặn: $url")
                return ContentSafetyResult(
                    isSafe = false,
                    reason = "Trang web này nằm trong danh sách chặn của cha mẹ",
                    matchedTerm = url
                )
            }
        }

        // Check category adult
        if (s.blockAdult) {
            val adultKeywords = listOf("sex", "xxx", "porn", "18+", "người lớn", "khiêu dâm", "gái gọi", "jav")
            for (kw in adultKeywords) {
                if (lower.contains(kw)) {
                    repository.addLog(ViolationType.BLOCKED_CONTENT, "Phát hiện nội dung người lớn: $kw")
                    return ContentSafetyResult(
                        isSafe = false,
                        reason = "Nội dung người lớn (18+) không phù hợp với lứa tuổi của trẻ",
                        matchedTerm = kw
                    )
                }
            }
        }

        // Check category gambling
        if (s.blockGambling) {
            val gambleKeywords = listOf("bet", "casino", "đánh bạc", "cá cược", "tài xỉu", "nổ hũ", "lô đề", "tiến lên")
            for (kw in gambleKeywords) {
                if (lower.contains(kw)) {
                    repository.addLog(ViolationType.BLOCKED_CONTENT, "Phát hiện nội dung cá cược, cờ bạc: $kw")
                    return ContentSafetyResult(
                        isSafe = false,
                        reason = "Trang web cờ bạc, cá cược bị cấm đối với trẻ em",
                        matchedTerm = kw
                    )
                }
            }
        }

        // Check custom keywords
        for (kw in s.customBlockedKeywords) {
            if (lower.contains(kw.lowercase())) {
                repository.addLog(ViolationType.BLOCKED_CONTENT, "Từ khóa bị chặn: $kw")
                return ContentSafetyResult(
                    isSafe = false,
                    reason = "Từ khóa tìm kiếm nằm trong danh mục cấm của gia đình",
                    matchedTerm = kw
                )
            }
        }

        return ContentSafetyResult(isSafe = true)
    }

    fun addBlockedUrl(url: String) {
        val clean = url.trim().lowercase()
        if (clean.isNotBlank() && !settings.value.customBlockedUrls.contains(clean)) {
            val list = settings.value.customBlockedUrls + clean
            repository.updateSettings(settings.value.copy(customBlockedUrls = list))
        }
    }

    fun removeBlockedUrl(url: String) {
        val list = settings.value.customBlockedUrls.filter { it != url }
        repository.updateSettings(settings.value.copy(customBlockedUrls = list))
    }

    fun addBlockedKeyword(keyword: String) {
        val clean = keyword.trim().lowercase()
        if (clean.isNotBlank() && !settings.value.customBlockedKeywords.contains(clean)) {
            val list = settings.value.customBlockedKeywords + clean
            repository.updateSettings(settings.value.copy(customBlockedKeywords = list))
        }
    }

    fun removeBlockedKeyword(keyword: String) {
        val list = settings.value.customBlockedKeywords.filter { it != keyword }
        repository.updateSettings(settings.value.copy(customBlockedKeywords = list))
    }

    override fun onCleared() {
        super.onCleared()
        detector.release()
    }
}
