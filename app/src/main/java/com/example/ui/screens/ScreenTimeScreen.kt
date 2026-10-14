package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ParentSettings
import com.example.ui.components.PinEntryDialog

import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Warning

@Composable
fun ScreenTimeScreen(
    settings: ParentSettings,
    eyeRestSeconds: Int,
    onUpdateSettings: (ParentSettings) -> Unit,
    onResetDailyTime: () -> Unit,
    onVerifyPin: (String) -> Boolean,
    onTest80PercentAlert: () -> Unit,
    onSimulateScreenTime: (Int) -> Unit
) {
    var showPinDialogForReset by remember { mutableStateOf(false) }
    var showEyeRestModal by remember { mutableStateOf(false) }

    val usedMinutes = settings.usedMinutesToday
    val totalLimit = settings.dailyScreenLimitMinutes
    val eightyPercentMinutes = (totalLimit * 0.8).toInt()
    val isPast80Percent = usedMinutes >= eightyPercentMinutes
    val remainingMinutes = (totalLimit - usedMinutes).coerceAtLeast(0)
    val progress = (usedMinutes.toFloat() / totalLimit.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Giám Sát Thời Gian Sử Dụng",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        // Today's Progress Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Đã dùng hôm nay",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$usedMinutes",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (progress >= 1f) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " / $totalLimit phút",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (remainingMinutes > 0) Color(0xFF10B981).copy(alpha = 0.15f)
                                else Color(0xFFEF4444).copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (remainingMinutes > 0) "Còn $remainingMinutes phút" else "Đã hết giờ!",
                            color = if (remainingMinutes > 0) Color(0xFF047857) else Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = when {
                        progress >= 1f -> Color(0xFFEF4444)
                        progress >= 0.8f -> Color(0xFFF59E0B)
                        else -> MaterialTheme.colorScheme.primary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                // 80% Marker Labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "0p", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = "▲ Cảnh báo 80% ($eightyPercentMinutes p)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPast80Percent) Color(0xFFD97706) else MaterialTheme.colorScheme.outline
                    )
                    Text(text = "100% ($totalLimit p)", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                }

                if (isPast80Percent && remainingMinutes > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Bé đã dùng quá 80% thời gian! Thông báo cảnh báo đã được gửi đến thiết bị.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val updated = settings.copy(dailyScreenLimitMinutes = settings.dailyScreenLimitMinutes + 15)
                            onUpdateSettings(updated)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "+15 phút", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showPinDialogForReset = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Đặt lại hôm nay", fontSize = 12.sp)
                    }
                }
            }
        }

        // 80% Screen Time Notification System Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("notification_80_percent_card")
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Thông Báo Cảnh Báo 80% Thời Gian",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Gửi thông báo âm thanh & rung khi bé đạt 80%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = settings.notifyAt80Percent,
                        onCheckedChange = { onUpdateSettings(settings.copy(notifyAt80Percent = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFF59E0B)
                        ),
                        modifier = Modifier.testTag("toggle_80_percent_notification")
                    )
                }

                Text(
                    text = "Khi bé đạt 80% thời gian ($eightyPercentMinutes/$totalLimit phút), hệ thống sẽ gửi một thông báo trên màn hình cảnh báo phụ huynh và bé còn $remainingMinutes phút trước khi máy tự động khóa.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Test Notification Button
                Button(
                    onClick = onTest80PercentAlert,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("test_80_percent_notification_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationImportant,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Thử Nghiệm Gửi Thông Báo 80% Ngay",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Simulation Jumps for Testing
                Text(
                    text = "Mô phỏng thời gian sử dụng nhanh:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSimulateScreenTime(totalLimit / 2) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "50% (${totalLimit / 2}p)", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { onSimulateScreenTime(eightyPercentMinutes) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "80% (${eightyPercentMinutes}p)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onSimulateScreenTime(totalLimit) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "100% (${totalLimit}p)", fontSize = 11.sp)
                    }
                }
            }
        }

        // Daily Limit Slider
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Giới Hạn Mỗi Ngày (Phụ huynh cài đặt)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Khi hết giờ, màn hình sẽ tự động khóa và cần mã PIN của phụ huynh để tiếp tục.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${settings.dailyScreenLimitMinutes} phút",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (settings.dailyScreenLimitMinutes) {
                            in 0..30 -> "Nghiêm ngặt"
                            in 31..60 -> "Chuẩn y khoa (Khuyên dùng)"
                            else -> "Nhiều"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Slider(
                    value = settings.dailyScreenLimitMinutes.toFloat(),
                    onValueChange = {
                        onUpdateSettings(settings.copy(dailyScreenLimitMinutes = it.toInt()))
                    },
                    valueRange = 15f..180f,
                    steps = 10,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(30, 45, 60, 90, 120).forEach { mins ->
                        Button(
                            onClick = { onUpdateSettings(settings.copy(dailyScreenLimitMinutes = mins)) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (settings.dailyScreenLimitMinutes == mins) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (settings.dailyScreenLimitMinutes == mins) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(text = "${mins}p", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 20-20-20 Rule Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0D9488).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SelfImprovement,
                                contentDescription = null,
                                tint = Color(0xFF0D9488),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Quy Tắc 20-20-20 Nghỉ Mắt",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Cứ xem 20 phút, nhắc bé nhìn xa 20 giây",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = settings.isSessionRestEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(isSessionRestEnabled = it)) }
                    )
                }

                if (settings.isSessionRestEnabled) {
                    val minutesPassed = eyeRestSeconds / 60
                    val secondsPassed = eyeRestSeconds % 60
                    Text(
                        text = "Thời gian xem liên tục hiện tại: ${String.format("%02d:%02d", minutesPassed, secondsPassed)} / 20:00",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0D9488)
                    )

                    Button(
                        onClick = { showEyeRestModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Bé tập bài tập thư giãn mắt ngay (20s)")
                    }
                }
            }
        }

        // Bedtime Lock Schedule
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Khóa Màn Hình Giờ Đi Ngủ",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Khóa từ ${settings.bedtimeStartHour}:00 tối đến ${settings.bedtimeEndHour}:00 sáng",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = settings.isBedtimeLockEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(isBedtimeLockEnabled = it)) }
                    )
                }

                Text(
                    text = "Ngăn chặn trẻ trùm chăn xem điện thoại ban đêm - nguyên nhân số 1 gây suy giảm thị lực và mất ngủ.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    if (showPinDialogForReset) {
        PinEntryDialog(
            title = "Xác nhận đặt lại",
            subtitle = "Nhập mã PIN để đặt lại thời gian xem về 0 phút",
            onDismiss = { showPinDialogForReset = false },
            onConfirm = { pin ->
                val ok = onVerifyPin(pin)
                if (ok) {
                    onResetDailyTime()
                    showPinDialogForReset = false
                }
                ok
            }
        )
    }

    if (showEyeRestModal) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEyeRestModal = false },
            title = {
                Text(
                    text = "🌸 Bé Ơi, Cùng Thư Giãn Mắt Nào!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "1. Bé hãy chớp mắt nhẹ nhàng 5 lần.\n2. Hướng mắt nhìn ra cửa sổ hoặc nhìn một vật ở xa (khoảng 6 mét).\n3. Giữ mắt nhìn xa trong 20 giây để cơ mắt được nghỉ ngơi nhé!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showEyeRestModal = false }) {
                    Text("Đã nghỉ mắt xong!")
                }
            }
        )
    }
}
