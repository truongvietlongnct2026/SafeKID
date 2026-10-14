package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DistanceState
import com.example.data.model.ParentSettings
import com.example.data.model.ViolationLog
import com.example.data.model.ViolationType
import com.example.ui.components.EyeDistanceRadar

@Composable
fun HomeScreen(
    settings: ParentSettings,
    distanceState: DistanceState,
    logs: List<ViolationLog>,
    onToggleGuard: (Boolean) -> Unit,
    onSimulateDistance: (Int) -> Unit,
    onTriggerTestBlackout: () -> Unit,
    onUpdateSettings: (ParentSettings) -> Unit
) {
    val tooCloseCount = logs.count { it.type == ViolationType.TOO_CLOSE }

    val statusBg by animateColorAsState(
        targetValue = if (settings.isGuardActive) Color(0xFF047857) else Color(0xFF64748B),
        label = "statusBg"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Guardian Banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = statusBg),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("guardian_status_banner")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (settings.isGuardActive) Icons.Default.Shield else Icons.Default.Block,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (settings.isGuardActive) "LÁ CHẮN MẮT ĐANG BẬT" else "BẢO VỆ ĐANG TẠM DỪNG",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (settings.isGuardActive) "Đang tự động tắt màn hình khi quá gần & chạy ngầm" else "Chạm công tắc để kích hoạt bảo vệ",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }

                Switch(
                    checked = settings.isGuardActive,
                    onCheckedChange = onToggleGuard,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF10B981),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("guard_master_switch")
                )
            }
        }

        // Live Radar Component
        EyeDistanceRadar(
            distanceState = distanceState,
            safeThresholdCm = settings.safeDistanceCm,
            onSimulateDistance = onSimulateDistance
        )

        // Test Blackout Button
        Button(
            onClick = onTriggerTestBlackout,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0F172A),
                contentColor = Color(0xFFFBBF24)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("test_blackout_button")
        ) {
            Icon(
                imageVector = Icons.Default.VisibilityOff,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = "Thử Nghiệm Màn Hình Tắt Ngay",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        // Quick Stats Today
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                icon = Icons.Default.Timer,
                title = "Thời gian dùng",
                value = "${settings.usedMinutesToday}/${settings.dailyScreenLimitMinutes}p",
                subtitle = "Hôm nay",
                iconTint = Color(0xFF0284C7),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.VisibilityOff,
                title = "Cảnh báo quá gần",
                value = "$tooCloseCount lần",
                subtitle = "Khoảng cách <${settings.safeDistanceCm}cm",
                iconTint = Color(0xFFEF4444),
                modifier = Modifier.weight(1f)
            )
        }

        // Quick Protection Options
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Tùy Chọn Phản Ứng Khi Quá Gần",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                QuickToggleRow(
                    icon = Icons.Default.VisibilityOff,
                    title = "Tự động tắt / đen màn hình",
                    desc = "Màn hình chuyển sang đen lập tức để bé dừng xem",
                    checked = settings.autoBlackoutScreen,
                    onCheckedChange = { onUpdateSettings(settings.copy(autoBlackoutScreen = it)) }
                )

                QuickToggleRow(
                    icon = Icons.Default.VolumeUp,
                    title = "Âm thanh nhắc nhở",
                    desc = "Phát tiếng bíp nhắc bé đưa máy ra xa",
                    checked = settings.warningSoundEnabled,
                    onCheckedChange = { onUpdateSettings(settings.copy(warningSoundEnabled = it)) }
                )

                QuickToggleRow(
                    icon = Icons.Default.Vibration,
                    title = "Rung phản hồi",
                    desc = "Rung nhẹ khi phát hiện khuôn mặt quá gần",
                    checked = settings.vibrationEnabled,
                    onCheckedChange = { onUpdateSettings(settings.copy(vibrationEnabled = it)) }
                )
            }
        }

        // Doctor Eye Health Tip Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Lời khuyên từ Bác Sĩ Nhãn Khoa",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• Khoảng cách tối thiểu từ mắt đến điện thoại nên từ 30cm - 40cm.\n• Cứ sau 20 phút xem, hãy cho mắt nghỉ 20 giây và nhìn xa 6 mét (Quy tắc 20-20-20).\n• Không để trẻ dùng điện thoại trong phòng tối.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun QuickToggleRow(
    icon: ImageVector,
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
    }
}
