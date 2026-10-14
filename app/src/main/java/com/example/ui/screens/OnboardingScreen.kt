package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(
    onComplete: (pin: String) -> Unit
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("1234") }
    var agreeBackgroundTerms by remember { mutableStateOf(false) }
    var agreeDistanceTerms by remember { mutableStateOf(false) }
    var agreeContentFilterTerms by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission callback */ }

    val allAgreed = agreeBackgroundTerms && agreeDistanceTerms && agreeContentFilterTerms && pin.length >= 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Hero Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF0284C7), Color(0xFF0F172A))
                    )
                )
                .border(2.dp, Color(0xFF38BDF8), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Shield",
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Thiết Lập Bảo Vệ Cho Trẻ",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Bảo vệ thị lực mắt bé, tự động tắt màn hình khi nhìn quá gần & quản lý thời gian sử dụng lành mạnh.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )

        // Key Features Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                FeatureRow(
                    icon = Icons.Default.Visibility,
                    title = "Tự Động Tắt Màn Hình Khi Quá Gần",
                    desc = "Màn hình chuyển màu đen hoàn toàn khi trẻ nhìn sát < 30cm, ngăn ngừa cận thị và mỏi mắt."
                )
                FeatureRow(
                    icon = Icons.Default.Layers,
                    title = "Chạy Dưới Nền 24/7 (Background Service)",
                    desc = "Duy trì bảo vệ liên tục ngay cả khi trẻ đang xem YouTube, TikTok hoặc chơi game."
                )
                FeatureRow(
                    icon = Icons.Default.Timer,
                    title = "Giới Hạn Thời Gian & Giờ Đi Ngủ",
                    desc = "Nhắc nhở nghỉ mắt theo quy tắc 20-20-20 và tự động khóa màn hình khi đến giờ ngủ."
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // System Permissions Section
        Text(
            text = "Cấp Quyền Cần Thiết Để Chạy Dưới Nền",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        PermissionItemCard(
            icon = Icons.Default.Layers,
            title = "1. Quyền hiển thị trên ứng dụng khác",
            desc = "Cần thiết để phủ màn hình đen bảo vệ mắt khi trẻ mở ứng dụng khác.",
            btnText = "Mở cài đặt",
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        PermissionItemCard(
            icon = Icons.Default.Videocam,
            title = "2. Quyền Camera / Cảm Biến Khoảng Cách",
            desc = "Nhận diện khoảng cách từ mắt trẻ đến màn hình điện thoại.",
            btnText = "Cho phép",
            onClick = {
                cameraLauncher.launch(android.Manifest.permission.CAMERA)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        PermissionItemCard(
            icon = Icons.Default.BatteryAlert,
            title = "3. Cho phép chạy ngầm không bị tắt",
            desc = "Bỏ qua tối ưu pin để hệ thống Android không tự đóng dịch vụ bảo vệ.",
            btnText = "Cài đặt pin",
            onClick = {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                try {
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Parent PIN Setup Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Thiết Lập Mã PIN Phụ Huynh",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Text(
                    text = "Mã PIN dùng để mở khóa màn hình hoặc thay đổi cài đặt bảo vệ (tránh trẻ tự tắt).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                            pin = it
                        }
                    },
                    label = { Text("Mã PIN (4 chữ số)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_pin_input")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Terms Checkboxes
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                TermCheckboxRow(
                    checked = agreeBackgroundTerms,
                    onCheckedChange = { agreeBackgroundTerms = it },
                    text = "Tôi đồng ý cho ứng dụng chạy ngầm liên tục dưới nền để bảo vệ mắt trẻ."
                )
                TermCheckboxRow(
                    checked = agreeDistanceTerms,
                    onCheckedChange = { agreeDistanceTerms = it },
                    text = "Đồng ý tự động tắt / đen màn hình khi khoảng cách mắt < 30cm."
                )
                TermCheckboxRow(
                    checked = agreeContentFilterTerms,
                    onCheckedChange = { agreeContentFilterTerms = it },
                    text = "Kích hoạt bộ lọc nội dung độc hại (18+, bạo lực, cờ bạc) cho trẻ."
                )
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Complete Button
        Button(
            onClick = {
                if (pin.length < 4) {
                    errorMessage = "Vui lòng nhập mã PIN đủ 4 số"
                    return@Button
                }
                if (!agreeBackgroundTerms || !agreeDistanceTerms || !agreeContentFilterTerms) {
                    errorMessage = "Vui lòng tích đồng ý tất cả điều khoản bảo vệ ở trên"
                    return@Button
                }
                onComplete(pin)
            },
            enabled = allAgreed,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("accept_terms_and_start_button")
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Đồng Ý & Kích Hoạt Bảo Vệ Ngay",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PermissionItemCard(
    icon: ImageVector,
    title: String,
    desc: String,
    btnText: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            OutlinedButton(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(text = btnText, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TermCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
