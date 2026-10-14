package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ParentSettings
import com.example.data.model.ViolationLog
import com.example.data.model.ViolationType
import com.example.ui.components.PinEntryDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ParentSettingsScreen(
    settings: ParentSettings,
    logs: List<ViolationLog>,
    isParentUnlocked: Boolean,
    onUnlockParent: (String) -> Boolean,
    onLockParent: () -> Unit,
    onUpdateSettings: (ParentSettings) -> Unit,
    onClearLogs: () -> Unit
) {
    val context = LocalContext.current
    var showPinDialog by remember { mutableStateOf(!isParentUnlocked) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    if (!isParentUnlocked) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Khu Vực Dành Riêng Cho Phụ Huynh",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Vui lòng nhập mã PIN để cấu hình khoảng cách an toàn, quản lý quyền chạy ngầm hoặc xem lịch sử vi phạm.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Button(
                    onClick = { showPinDialog = true },
                    modifier = Modifier.testTag("unlock_parent_button")
                ) {
                    Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Nhập Mã PIN Phụ Huynh")
                }
            }
        }

        if (showPinDialog) {
            PinEntryDialog(
                title = "Xác Thực Phụ Huynh",
                subtitle = "Nhập mã PIN phụ huynh (Mặc định: 1234)",
                onDismiss = { showPinDialog = false },
                onConfirm = { pin ->
                    val ok = onUnlockParent(pin)
                    if (ok) showPinDialog = false
                    ok
                }
            )
        }
        return
    }

    // Unlocked parent interface
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Cài Đặt Phụ Huynh",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Đang ở chế độ Phụ Huynh (Đã mở khóa)",
                    fontSize = 12.sp,
                    color = Color(0xFF10B981)
                )
            }

            OutlinedButton(
                onClick = onLockParent,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text("Khóa lại", fontSize = 12.sp)
            }
        }

        // Distance Threshold Settings
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Ngưỡng Khoảng Cách An Toàn",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Khi trẻ đưa điện thoại gần hơn khoảng cách này, màn hình sẽ tự động tắt ngay lập tức.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(25, 30, 35, 40).forEach { dist ->
                        Button(
                            onClick = { onUpdateSettings(settings.copy(safeDistanceCm = dist)) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (settings.safeDistanceCm == dist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (settings.safeDistanceCm == dist) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text(
                                text = "${dist}cm" + if (dist == 30) " ★" else "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Security & PIN
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Bảo Mật & Mã Khóa",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showChangePinDialog = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(imageVector = Icons.Default.Password, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(text = "Đổi Mã PIN Phụ Huynh", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text(text = "Mã PIN hiện tại: ••••", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showTermsDialog = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(text = "Điều Khoản Chạy Dưới Nền & Bảo Vệ Mắt", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text(text = "Đã đồng ý ngày kích hoạt", fontSize = 12.sp, color = Color(0xFF10B981))
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                }
            }
        }

        // Background & Device Permissions Status
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Trạng Thái Quyền Hoạt Động Ngầm",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                PermissionStatusRow(
                    title = "Hiển thị đè lên ứng dụng khác",
                    isGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true,
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                            context.startActivity(intent)
                        }
                    }
                )

                PermissionStatusRow(
                    title = "Chạy ngầm không bị đóng (Pin)",
                    isGranted = true,
                    onClick = {
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                )
            }
        }

        // Violation Logs
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Lịch Sử Cảnh Báo (${logs.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    if (logs.isNotEmpty()) {
                        IconButton(onClick = onClearLogs, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear logs", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (logs.isEmpty()) {
                    Text(
                        text = "Chưa có vi phạm nào được ghi nhận. Bé xem điện thoại ở khoảng cách an toàn rất tốt! 🎉",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    val sdf = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault())
                    logs.take(15).forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = when (log.type) {
                                    ViolationType.TOO_CLOSE -> Icons.Default.VisibilityOff
                                    ViolationType.WARNING_80_PERCENT -> Icons.Default.Warning
                                    ViolationType.TIME_LIMIT -> Icons.Default.Warning
                                    ViolationType.BEDTIME -> Icons.Default.Security
                                    ViolationType.BLOCKED_CONTENT -> Icons.Default.Security
                                },
                                contentDescription = null,
                                tint = when (log.type) {
                                    ViolationType.TOO_CLOSE -> Color(0xFFEF4444)
                                    ViolationType.WARNING_80_PERCENT -> Color(0xFFF59E0B)
                                    ViolationType.TIME_LIMIT -> Color(0xFFDC2626)
                                    else -> Color(0xFF6366F1)
                                },
                                modifier = Modifier.size(18.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = log.details,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = sdf.format(Date(log.timestamp)),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    if (showChangePinDialog) {
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var err by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Đổi Mã PIN Phụ Huynh") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPin = it },
                        label = { Text("Mã PIN mới (4 số)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmPin = it },
                        label = { Text("Xác nhận lại PIN mới") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    err?.let { Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length < 4) {
                            err = "PIN phải có đủ 4 chữ số"
                            return@Button
                        }
                        if (newPin != confirmPin) {
                            err = "Mã xác nhận không khớp"
                            return@Button
                        }
                        onUpdateSettings(settings.copy(parentPin = newPin))
                        showChangePinDialog = false
                    }
                ) {
                    Text("Lưu PIN mới")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showChangePinDialog = false }) { Text("Hủy") }
            }
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Điều Khoản & Cam Kết Bảo Vệ") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "1. Mục đích ứng dụng:\nKidSafe được phát triển nhằm hỗ trợ cha mẹ bảo vệ thị lực của con trẻ, hạn chế nguy cơ cận thị tiến triển do thói quen dí sát màn hình điện thoại (<30cm).\n\n2. Quyền chạy ngầm (Background Service):\nỨng dụng chạy liên tục dịch vụ giám sát dưới nền để luôn kịp thời cảnh báo và làm đen/tắt màn hình khi trẻ xem nội dung ở các ứng dụng khác.\n\n3. Quyền riêng tư:\nMọi dữ liệu khoảng cách và thời gian đều được xử lý và lưu trữ hoàn toàn cục bộ trên thiết bị của bạn.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showTermsDialog = false }) { Text("Đóng") }
            }
        )
    }
}

@Composable
private fun PermissionStatusRow(title: String, isGranted: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(
                text = if (isGranted) "Đã cấp quyền hợp lệ" else "Chưa cấp - Chạm để cài đặt",
                fontSize = 11.sp,
                color = if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444)
            )
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
    }
}
