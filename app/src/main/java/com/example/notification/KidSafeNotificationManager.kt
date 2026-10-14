package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

class KidSafeNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    companion object {
        const val ALERT_CHANNEL_ID = "kidsafe_alerts_channel"
        const val NOTIFICATION_ID_80_PERCENT = 2080
        const val NOTIFICATION_ID_TIME_UP = 2100
        const val NOTIFICATION_ID_TEST = 2099
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Cảnh Báo Thời Gian Sử Dụng KidSafe",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo cho phụ huynh khi con trẻ đạt 80% giới hạn thời gian xem và khi hết giờ."
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setSound(soundUri, audioAttributes)
                setShowBadge(true)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun canSendNotifications(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            return permission == PackageManager.PERMISSION_GRANTED
        }
        return notificationManager?.areNotificationsEnabled() ?: true
    }

    fun send80PercentScreenTimeAlert(usedMinutes: Int, limitMinutes: Int) {
        if (!canSendNotifications()) return

        val remainingMinutes = (limitMinutes - usedMinutes).coerceAtLeast(0)
        val title = "⚠️ Cảnh Báo: Bé Đã Dùng 80% Thời Gian!"
        val content = "Bé đã xem $usedMinutes/$limitMinutes phút hôm nay. Chỉ còn $remainingMinutes phút trước khi thiết bị tự động khóa!"
        val expandedText = "Bé yêu của bạn đã đạt 80% thời lượng sử dụng thiết bị cho phép trong ngày ($usedMinutes trên tổng số $limitMinutes phút).\n\n" +
                "⏳ Còn lại: $remainingMinutes phút.\n" +
                "💡 Lời khuyên: Hãy nhắc bé chuẩn bị dừng xem hoặc hoàn thành nội dung đang theo dõi để bảo vệ thị lực và tránh bị khóa đột ngột."

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_TAB", "SCREEN_TIME")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_80_PERCENT,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 350, 200, 350))
            .setColor(0xFFF59E0B.toInt()) // Amber alert color
            .build()

        notificationManager?.notify(NOTIFICATION_ID_80_PERCENT, notification)
    }

    fun sendTimeUpAlert(limitMinutes: Int) {
        if (!canSendNotifications()) return

        val title = "🔒 Đã Hết Giờ Sử Dụng Thiết Bị!"
        val content = "Bé đã dùng đủ $limitMinutes phút hôm nay. Màn hình bảo vệ đã kích hoạt."

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_TAB", "SCREEN_TIME")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_TIME_UP,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFFEF4444.toInt()) // Red alert color
            .build()

        notificationManager?.notify(NOTIFICATION_ID_TIME_UP, notification)
    }

    fun sendTestAlert(usedMinutes: Int, limitMinutes: Int) {
        send80PercentScreenTimeAlert(usedMinutes, limitMinutes)
    }
}
