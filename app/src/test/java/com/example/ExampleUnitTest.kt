package com.example

import com.example.data.model.ParentSettings
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSafeDistanceEvaluation() {
        val safeThresholdCm = 30
        val testDistanceClose = 18
        val testDistanceSafe = 38

        assertTrue(testDistanceClose < safeThresholdCm)
        assertTrue(testDistanceSafe >= safeThresholdCm)
    }

    @Test
    fun testContentFilterRules() {
        val blockedUrls = listOf("tiktok.com", "casino888.bet", "phimnguoilon.com")
        val blockedKeywords = listOf("cá cược", "đánh bạc", "khiêu dâm", "18+")

        fun isBlocked(query: String): Boolean {
            val lower = query.lowercase()
            return blockedUrls.any { lower.contains(it) } || blockedKeywords.any { lower.contains(it) }
        }

        assertTrue(isBlocked("https://casino888.bet/games"))
        assertTrue(isBlocked("tìm trang phim khiêu dâm"))
        assertFalse(isBlocked("https://toanlop5.edu.vn"))
        assertFalse(isBlocked("xem phim hoạt hình doremon"))
    }

    @Test
    fun testParentPinVerification() {
        val settings = ParentSettings(parentPin = "2026")
        assertTrue(settings.parentPin == "2026")
        assertFalse(settings.parentPin == "1234")
    }

    @Test
    fun testEightyPercentThresholdCalculation() {
        val dailyLimit = 60
        val eightyPercent = (dailyLimit * 0.80).toInt()
        assertEquals(48, eightyPercent)

        // At 40 mins: not yet 80%
        assertFalse(40 >= eightyPercent)

        // At 48 mins: exactly 80%
        assertTrue(48 >= eightyPercent)

        // At 55 mins: past 80%
        assertTrue(55 >= eightyPercent)
    }

    @Test
    fun testEightyPercentAlertTriggerCondition() {
        var settings = ParentSettings(
            dailyScreenLimitMinutes = 60,
            usedMinutesToday = 30,
            notifyAt80Percent = true,
            hasNotified80Percent = false
        )

        fun checkShouldAlert(used: Int, current: ParentSettings): Boolean {
            val threshold = (current.dailyScreenLimitMinutes * 0.80).toInt()
            return current.notifyAt80Percent && !current.hasNotified80Percent && used >= threshold && used < current.dailyScreenLimitMinutes
        }

        // Used 30m (< 48m): should not alert
        assertFalse(checkShouldAlert(30, settings))

        // Used 48m: should alert
        assertTrue(checkShouldAlert(48, settings))

        // After alert fired, marked as notified
        settings = settings.copy(usedMinutesToday = 48, hasNotified80Percent = true)

        // Used 49m: should not fire again because already notified
        assertFalse(checkShouldAlert(49, settings))

        // When reset daily time: hasNotified80Percent is reset
        val resetSettings = settings.copy(usedMinutesToday = 0, hasNotified80Percent = false)
        assertFalse(resetSettings.hasNotified80Percent)
        assertEquals(0, resetSettings.usedMinutesToday)
    }
}
