package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.notification.KidSafeNotificationManager
import com.example.data.model.DistanceState
import com.example.data.model.ParentSettings
import com.example.data.model.ViolationType
import com.example.data.repository.KidSafeRepository
import com.example.sensor.EyeDistanceDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KidSafeGuardService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var detector: EyeDistanceDetector
    private lateinit var repository: KidSafeRepository
    private var windowManager: WindowManager? = null
    private var blackoutOverlayView: View? = null
    private var isOverlayShowing = false

    companion object {
        const val CHANNEL_ID = "kidsafe_guard_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START_GUARD"
        const val ACTION_STOP = "ACTION_STOP_GUARD"

        fun startService(context: Context) {
            val intent = Intent(context, KidSafeGuardService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, KidSafeGuardService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = KidSafeRepository(applicationContext)
        detector = EyeDistanceDetector(applicationContext)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Đang bảo vệ mắt & thời gian sử dụng"))

        detector.startListening(repository.settings.value.safeDistanceCm)

        // Observe distance changes
        serviceScope.launch {
            detector.distanceState.collectLatest { state ->
                val currentSettings = repository.settings.value
                if (!currentSettings.isGuardActive || !currentSettings.isDistanceGuardEnabled) {
                    hideOverlay()
                    return@collectLatest
                }

                when (state) {
                    is DistanceState.TooClose -> {
                        detector.triggerAlerts(
                            currentSettings.warningSoundEnabled,
                            currentSettings.vibrationEnabled
                        )
                        repository.addLog(
                            ViolationType.TOO_CLOSE,
                            "Nhìn quá gần (${state.distanceCm}cm < ${currentSettings.safeDistanceCm}cm)",
                            state.distanceCm
                        )
                        if (currentSettings.autoBlackoutScreen) {
                            showBlackoutOverlay(
                                title = "⚠️ BÉ ƠI, NHÌN XA RA NÀO!",
                                message = "Màn hình tự động tắt để bảo vệ mắt của bé.\nHãy đưa điện thoại ra xa hơn ${currentSettings.safeDistanceCm}cm nhé!",
                                distanceCm = state.distanceCm
                            )
                        }
                    }
                    is DistanceState.Caution -> {
                        // Caution zone, give a slight grace period before blacking out
                    }
                    is DistanceState.Safe -> {
                        // Safe distance restored
                        if (isOverlayShowing) {
                            delay(1200) // Delay to ensure child has steadily pushed phone away
                            hideOverlay()
                        }
                    }
                    DistanceState.Calibrating -> {}
                }
            }
        }

        // Background Screen Time & 80% Alert Loop
        serviceScope.launch {
            val notificationHelper = KidSafeNotificationManager(applicationContext)
            while (true) {
                delay(60_000)
                val s = repository.settings.value
                if (s.isGuardActive) {
                    val newUsed = s.usedMinutesToday + 1
                    val eightyPercent = (s.dailyScreenLimitMinutes * 0.80).toInt()
                    var notified = s.hasNotified80Percent

                    // 80% Screen Time Alert
                    if (s.notifyAt80Percent && !notified && newUsed >= eightyPercent && newUsed < s.dailyScreenLimitMinutes) {
                        notificationHelper.send80PercentScreenTimeAlert(newUsed, s.dailyScreenLimitMinutes)
                        repository.addLog(
                            ViolationType.WARNING_80_PERCENT,
                            "Đạt 80% thời gian màn hình ($newUsed/${s.dailyScreenLimitMinutes} phút - còn ${s.dailyScreenLimitMinutes - newUsed} phút)"
                        )
                        notified = true
                    }

                    // Check Daily Limit Reached
                    if (newUsed >= s.dailyScreenLimitMinutes) {
                        notificationHelper.sendTimeUpAlert(s.dailyScreenLimitMinutes)
                        repository.addLog(ViolationType.TIME_LIMIT, "Đã hết thời gian cho phép hôm nay (${s.dailyScreenLimitMinutes} phút)")
                        if (s.autoBlackoutScreen) {
                            showBlackoutOverlay(
                                title = "🔒 ĐÃ HẾT GIỜ SỬ DỤNG!",
                                message = "Hôm nay bé đã dùng hết ${s.dailyScreenLimitMinutes} phút cho phép.\nĐã đến giờ cho mắt nghỉ ngơi!",
                                distanceCm = 0
                            )
                        }
                    }

                    repository.updateSettings(s.copy(usedMinutesToday = newUsed, hasNotified80Percent = notified))
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun showBlackoutOverlay(title: String, message: String, distanceCm: Int) {
        if (isOverlayShowing) {
            updateOverlayText(distanceCm)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // Cannot draw over other apps yet, user will be prompted in UI
            return
        }

        val wm = windowManager ?: return

        try {
            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
                screenBrightness = 0.05f // Dim brightness to pitch black
            }

            // Create Blackout View programmatically
            val container = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setBackgroundColor(Color.parseColor("#F5000000")) // 96% Blackout
                gravity = Gravity.CENTER
                setPadding(48, 48, 48, 48)

                // Warning Icon / Title
                val tvTitle = TextView(context).apply {
                    text = title
                    setTextColor(Color.parseColor("#FBBF24"))
                    textSize = 22f
                    gravity = Gravity.CENTER
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                addView(tvTitle)

                // Distance readout
                val tvDistance = TextView(context).apply {
                    id = View.generateViewId()
                    text = "Khoảng cách hiện tại: ${distanceCm}cm (Quá gần!)"
                    setTextColor(Color.parseColor("#EF4444"))
                    textSize = 18f
                    gravity = Gravity.CENTER
                    setPadding(0, 24, 0, 16)
                    tag = "distance_text"
                }
                addView(tvDistance)

                // Message
                val tvMsg = TextView(context).apply {
                    text = message
                    setTextColor(Color.WHITE)
                    textSize = 16f
                    gravity = Gravity.CENTER
                    setPadding(0, 0, 0, 32)
                }
                addView(tvMsg)

                // Parent Unlock Button
                val btnUnlock = Button(context).apply {
                    text = "Phụ Huynh Mở Khóa"
                    setBackgroundColor(Color.parseColor("#0284C7"))
                    setTextColor(Color.WHITE)
                    setOnClickListener {
                        val launchIntent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra("REQUEST_PARENT_UNLOCK", true)
                        }
                        startActivity(launchIntent)
                        hideOverlay()
                    }
                }
                addView(btnUnlock)
            }

            wm.addView(container, layoutParams)
            blackoutOverlayView = container
            isOverlayShowing = true
        } catch (_: Exception) {
            isOverlayShowing = false
        }
    }

    private fun updateOverlayText(distanceCm: Int) {
        val tv = blackoutOverlayView?.findViewWithTag<TextView>("distance_text")
        tv?.text = "Khoảng cách hiện tại: ${distanceCm}cm (Quá gần!)"
    }

    private fun hideOverlay() {
        if (isOverlayShowing && blackoutOverlayView != null) {
            try {
                windowManager?.removeView(blackoutOverlayView)
            } catch (_: Exception) {}
            blackoutOverlayView = null
            isOverlayShowing = false
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "KidSafe - Bảo Vệ Mắt & Trẻ Em",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Thông báo dịch vụ bảo vệ mắt khi xem điện thoại và lọc nội dung"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("KidSafe đang bảo vệ bé")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideOverlay()
        detector.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
