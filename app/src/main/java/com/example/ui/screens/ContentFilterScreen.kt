package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Explicit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ParentSettings
import com.example.ui.viewmodel.ContentSafetyResult

@Composable
fun ContentFilterScreen(
    settings: ParentSettings,
    onUpdateSettings: (ParentSettings) -> Unit,
    onCheckSafety: (String) -> ContentSafetyResult,
    onAddBlockedUrl: (String) -> Unit,
    onRemoveBlockedUrl: (String) -> Unit,
    onAddBlockedKeyword: (String) -> Unit,
    onRemoveBlockedKeyword: (String) -> Unit
) {
    var testQuery by remember { mutableStateOf("casino888.bet") }
    var safetyResult by remember { mutableStateOf<ContentSafetyResult?>(null) }

    var newUrlInput by remember { mutableStateOf("") }
    var newKeywordInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Lọc Nội Dung Độc Hại Cho Bé",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        // Master Filter Switch Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (settings.isContentFilterEnabled) Color(0xFF0F766E) else Color(0xFF64748B)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (settings.isContentFilterEnabled) "BỘ LỌC NỘI DUNG ĐANG BẬT" else "BỘ LỌC ĐANG TẮT",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (settings.isContentFilterEnabled) "Đang chủ động chặn web đen, cờ bạc & từ khóa xấu" else "Gạt công tắc để kích hoạt bảo vệ",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = settings.isContentFilterEnabled,
                    onCheckedChange = { onUpdateSettings(settings.copy(isContentFilterEnabled = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF14B8A6))
                )
            }
        }

        // Category Shield List
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Danh Mục Nội Dung Tự Động Chặn",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                CategoryToggleRow(
                    icon = Icons.Default.Explicit,
                    iconColor = Color(0xFFEF4444),
                    title = "Nội dung người lớn (18+ / Khiêu dâm)",
                    desc = "Tự động chặn các trang web đen, ảnh nhạy cảm",
                    checked = settings.blockAdult,
                    onCheckedChange = { onUpdateSettings(settings.copy(blockAdult = it)) }
                )

                CategoryToggleRow(
                    icon = Icons.Default.Casino,
                    iconColor = Color(0xFFF59E0B),
                    title = "Cá cược, cờ bạc trực tuyến",
                    desc = "Chặn web game đổi thưởng, lô đề, cá độ",
                    checked = settings.blockGambling,
                    onCheckedChange = { onUpdateSettings(settings.copy(blockGambling = it)) }
                )

                CategoryToggleRow(
                    icon = Icons.Default.Warning,
                    iconColor = Color(0xFFDC2626),
                    title = "Bạo lực & Nội dung nguy hiểm",
                    desc = "Chặn video kinh dị, hành vi gây hại tự sát",
                    checked = settings.blockViolence,
                    onCheckedChange = { onUpdateSettings(settings.copy(blockViolence = it)) }
                )

                CategoryToggleRow(
                    icon = Icons.Default.Block,
                    iconColor = Color(0xFF8B5CF6),
                    title = "Mạng xã hội video ngắn (TikTok, v.v.)",
                    desc = "Hạn chế hội chứng nghiện lướt màn hình ngắn",
                    checked = settings.blockSocialAddiction,
                    onCheckedChange = { onUpdateSettings(settings.copy(blockSocialAddiction = it)) }
                )
            }
        }

        // Live Safe Browser Simulator
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Trình Kiểm Tra & Mô Phỏng Lọc Web",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Nhập thử một địa chỉ website hoặc từ khóa tìm kiếm để kiểm tra phản ứng của bộ lọc:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = testQuery,
                        onValueChange = { testQuery = it },
                        placeholder = { Text("Nhập domain hoặc từ khóa...") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("browser_test_input"),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            safetyResult = onCheckSafety(testQuery)
                        })
                    )

                    Button(
                        onClick = { safetyResult = onCheckSafety(testQuery) },
                        modifier = Modifier.testTag("browser_test_check_button")
                    ) {
                        Text("Kiểm tra")
                    }
                }

                // Preset Test Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("phimnguoilon.com", "casino888.bet", "toanlop4.edu.vn").forEach { sample ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    testQuery = sample
                                    safetyResult = onCheckSafety(sample)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = sample, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Result Display
                safetyResult?.let { res ->
                    Spacer(modifier = Modifier.height(14.dp))
                    if (!res.isSafe) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            modifier = Modifier.border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "🚫 ĐÃ CHẶN TRANG WEB NÀY!",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFDC2626),
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = res.reason,
                                        fontSize = 12.sp,
                                        color = Color(0xFF991B1B)
                                    )
                                    if (res.matchedTerm.isNotBlank()) {
                                        Text(
                                            text = "Từ khóa vi phạm: \"${res.matchedTerm}\"",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFB91C1C)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            modifier = Modifier.border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "✅ TRANG WEB AN TOÀN CHO BÉ",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF16A34A),
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Nội dung phù hợp, không chứa từ khóa độc hại.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF166534)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Custom Blocked Domains Management
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Danh Sách Website Chặn Tùy Chỉnh",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newUrlInput,
                        onValueChange = { newUrlInput = it },
                        placeholder = { Text("vd: gameonline.vn") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newUrlInput.isNotBlank()) {
                                onAddBlockedUrl(newUrlInput)
                                newUrlInput = ""
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                settings.customBlockedUrls.forEach { url ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text(text = url, fontSize = 13.sp)
                        }
                        IconButton(onClick = { onRemoveBlockedUrl(url) }, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Custom Blocked Keywords Management
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Danh Sách Từ Khóa Nhạy Cảm",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newKeywordInput,
                        onValueChange = { newKeywordInput = it },
                        placeholder = { Text("vd: nói tục, đánh nhau") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newKeywordInput.isNotBlank()) {
                                onAddBlockedKeyword(newKeywordInput)
                                newKeywordInput = ""
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                settings.customBlockedKeywords.forEach { kw ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "• $kw", fontSize = 13.sp)
                        IconButton(onClick = { onRemoveBlockedKeyword(kw) }, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
private fun CategoryToggleRow(
    icon: ImageVector,
    iconColor: Color,
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
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
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
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = iconColor)
        )
    }
}
