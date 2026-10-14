package com.example.data.model

data class ParentSettings(
    val parentPin: String = "1234",
    val isTermsAccepted: Boolean = false,
    val isGuardActive: Boolean = true,
    val isDistanceGuardEnabled: Boolean = true,
    val safeDistanceCm: Int = 30, // 25, 30, 35, 40 cm
    val autoBlackoutScreen: Boolean = true,
    val warningSoundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val dailyScreenLimitMinutes: Int = 60,
    val usedMinutesToday: Int = 24,
    val notifyAt80Percent: Boolean = true,
    val hasNotified80Percent: Boolean = false,
    val isSessionRestEnabled: Boolean = true,
    val sessionRestMinutes: Int = 20, // 20-20-20 rule
    val isBedtimeLockEnabled: Boolean = true,
    val bedtimeStartHour: Int = 21, // 9:00 PM
    val bedtimeStartMinute: Int = 0,
    val bedtimeEndHour: Int = 6, // 6:00 AM
    val bedtimeEndMinute: Int = 0,
    val isContentFilterEnabled: Boolean = true,
    val blockAdult: Boolean = true,
    val blockGambling: Boolean = true,
    val blockViolence: Boolean = true,
    val blockSocialAddiction: Boolean = false,
    val customBlockedUrls: List<String> = listOf(
        "tiktok.com",
        "xxxtentacion.xxx",
        "casino888.bet",
        "phimnguoilon.com",
        "gamebai777.com"
    ),
    val customBlockedKeywords: List<String> = listOf(
        "cá cược",
        "đánh bạc",
        "khiêu dâm",
        "18+",
        "bạo lực",
        "tự tử"
    )
)

enum class ViolationType(val title: String) {
    TOO_CLOSE("Xem quá gần màn hình"),
    WARNING_80_PERCENT("Đạt 80% thời gian màn hình"),
    TIME_LIMIT("Hết thời gian sử dụng"),
    BEDTIME("Vi phạm giờ đi ngủ"),
    BLOCKED_CONTENT("Truy cập nội dung độc hại")
}

data class ViolationLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val type: ViolationType,
    val details: String,
    val recordedDistanceCm: Int? = null
)

sealed class DistanceState {
    data class Safe(val distanceCm: Int) : DistanceState()
    data class Caution(val distanceCm: Int) : DistanceState()
    data class TooClose(val distanceCm: Int) : DistanceState()
    object Calibrating : DistanceState()
}
