package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ParentSettings
import com.example.data.model.ViolationLog
import com.example.data.model.ViolationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KidSafeRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("kidsafe_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<ParentSettings> = _settings.asStateFlow()

    private val _logs = MutableStateFlow(loadLogs())
    val logs: StateFlow<List<ViolationLog>> = _logs.asStateFlow()

    private val _isParentUnlocked = MutableStateFlow(false)
    val isParentUnlocked: StateFlow<Boolean> = _isParentUnlocked.asStateFlow()

    private fun loadSettings(): ParentSettings {
        return ParentSettings(
            parentPin = prefs.getString("parent_pin", "1234") ?: "1234",
            isTermsAccepted = prefs.getBoolean("is_terms_accepted", false),
            isGuardActive = prefs.getBoolean("is_guard_active", true),
            isDistanceGuardEnabled = prefs.getBoolean("is_distance_guard_enabled", true),
            safeDistanceCm = prefs.getInt("safe_distance_cm", 30),
            autoBlackoutScreen = prefs.getBoolean("auto_blackout_screen", true),
            warningSoundEnabled = prefs.getBoolean("warning_sound_enabled", true),
            vibrationEnabled = prefs.getBoolean("vibration_enabled", true),
            dailyScreenLimitMinutes = prefs.getInt("daily_screen_limit", 60),
            usedMinutesToday = prefs.getInt("used_minutes_today", 25),
            notifyAt80Percent = prefs.getBoolean("notify_at_80_percent", true),
            hasNotified80Percent = prefs.getBoolean("has_notified_80_percent", false),
            isSessionRestEnabled = prefs.getBoolean("is_session_rest_enabled", true),
            sessionRestMinutes = prefs.getInt("session_rest_minutes", 20),
            isBedtimeLockEnabled = prefs.getBoolean("is_bedtime_lock_enabled", true),
            bedtimeStartHour = prefs.getInt("bedtime_start_hour", 21),
            bedtimeStartMinute = prefs.getInt("bedtime_start_minute", 0),
            bedtimeEndHour = prefs.getInt("bedtime_end_hour", 6),
            bedtimeEndMinute = prefs.getInt("bedtime_end_minute", 0),
            isContentFilterEnabled = prefs.getBoolean("is_content_filter_enabled", true),
            blockAdult = prefs.getBoolean("block_adult", true),
            blockGambling = prefs.getBoolean("block_gambling", true),
            blockViolence = prefs.getBoolean("block_violence", true),
            blockSocialAddiction = prefs.getBoolean("block_social_addiction", false),
            customBlockedUrls = loadStringList("custom_blocked_urls", listOf("tiktok.com", "phimnguoilon.com", "casino888.bet")),
            customBlockedKeywords = loadStringList("custom_blocked_keywords", listOf("cá cược", "đánh bạc", "khiêu dâm", "18+"))
        )
    }

    private fun loadStringList(key: String, default: List<String>): List<String> {
        val raw = prefs.getString(key, null) ?: return default
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
            list
        } catch (_: Exception) {
            default
        }
    }

    private fun saveStringList(key: String, list: List<String>) {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        prefs.edit().putString(key, arr.toString()).apply()
    }

    fun updateSettings(newSettings: ParentSettings) {
        _settings.value = newSettings
        prefs.edit()
            .putString("parent_pin", newSettings.parentPin)
            .putBoolean("is_terms_accepted", newSettings.isTermsAccepted)
            .putBoolean("is_guard_active", newSettings.isGuardActive)
            .putBoolean("is_distance_guard_enabled", newSettings.isDistanceGuardEnabled)
            .putInt("safe_distance_cm", newSettings.safeDistanceCm)
            .putBoolean("auto_blackout_screen", newSettings.autoBlackoutScreen)
            .putBoolean("warning_sound_enabled", newSettings.warningSoundEnabled)
            .putBoolean("vibrationEnabled", newSettings.vibrationEnabled)
            .putInt("daily_screen_limit", newSettings.dailyScreenLimitMinutes)
            .putInt("used_minutes_today", newSettings.usedMinutesToday)
            .putBoolean("notify_at_80_percent", newSettings.notifyAt80Percent)
            .putBoolean("has_notified_80_percent", newSettings.hasNotified80Percent)
            .putBoolean("is_session_rest_enabled", newSettings.isSessionRestEnabled)
            .putInt("session_rest_minutes", newSettings.sessionRestMinutes)
            .putBoolean("is_bedtime_lock_enabled", newSettings.isBedtimeLockEnabled)
            .putInt("bedtime_start_hour", newSettings.bedtimeStartHour)
            .putInt("bedtime_start_minute", newSettings.bedtimeStartMinute)
            .putInt("bedtime_end_hour", newSettings.bedtimeEndHour)
            .putInt("bedtime_end_minute", newSettings.bedtimeEndMinute)
            .putBoolean("is_content_filter_enabled", newSettings.isContentFilterEnabled)
            .putBoolean("block_adult", newSettings.blockAdult)
            .putBoolean("block_gambling", newSettings.blockGambling)
            .putBoolean("block_violence", newSettings.blockViolence)
            .putBoolean("block_social_addiction", newSettings.blockSocialAddiction)
            .apply()

        saveStringList("custom_blocked_urls", newSettings.customBlockedUrls)
        saveStringList("custom_blocked_keywords", newSettings.customBlockedKeywords)
    }

    fun addLog(type: ViolationType, details: String, recordedDistanceCm: Int? = null) {
        val newLog = ViolationLog(
            type = type,
            details = details,
            recordedDistanceCm = recordedDistanceCm
        )
        val updated = listOf(newLog) + _logs.value
        _logs.value = updated.take(50) // Keep last 50
        saveLogs(_logs.value)
    }

    fun clearLogs() {
        _logs.value = emptyList()
        prefs.edit().remove("violation_logs").apply()
    }

    private fun loadLogs(): List<ViolationLog> {
        val raw = prefs.getString("violation_logs", null) ?: return listOf(
            ViolationLog(
                timestamp = System.currentTimeMillis() - 3600000 * 2,
                type = ViolationType.TOO_CLOSE,
                details = "Trẻ xem điện thoại ở khoảng cách 19cm (< 30cm)",
                recordedDistanceCm = 19
            ),
            ViolationLog(
                timestamp = System.currentTimeMillis() - 3600000 * 4,
                type = ViolationType.TOO_CLOSE,
                details = "Trẻ xem quá gần ở khoảng cách 17cm",
                recordedDistanceCm = 17
            ),
            ViolationLog(
                timestamp = System.currentTimeMillis() - 3600000 * 20,
                type = ViolationType.TIME_LIMIT,
                details = "Đã đạt giới hạn xem 60 phút trong ngày"
            )
        )
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<ViolationLog>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ViolationLog(
                        id = obj.getString("id"),
                        timestamp = obj.getLong("timestamp"),
                        type = ViolationType.valueOf(obj.getString("type")),
                        details = obj.getString("details"),
                        recordedDistanceCm = if (obj.has("distance")) obj.getInt("distance") else null
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveLogs(logs: List<ViolationLog>) {
        val arr = JSONArray()
        logs.forEach { log ->
            val obj = JSONObject().apply {
                put("id", log.id)
                put("timestamp", log.timestamp)
                put("type", log.type.name)
                put("details", log.details)
                log.recordedDistanceCm?.let { put("distance", it) }
            }
            arr.put(obj)
        }
        prefs.edit().putString("violation_logs", arr.toString()).apply()
    }

    fun setParentUnlocked(unlocked: Boolean) {
        _isParentUnlocked.value = unlocked
    }

    fun verifyPin(pin: String): Boolean {
        return pin == _settings.value.parentPin
    }
}
