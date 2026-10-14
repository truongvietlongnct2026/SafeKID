package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BlackoutEyeShield(
    reason: String,
    currentDistanceCm: Int,
    safeThresholdCm: Int,
    onDismissWithPin: (String) -> Boolean,
    onSimulatePushAway: () -> Unit
) {
    var showPinDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)) // Pitch Black for complete screen shutoff simulation
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .testTag("blackout_eye_shield"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing Eye Warning Badge
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1B4B))
                    .border(3.dp, Color(0xFFF59E0B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = "Bảo vệ mắt",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "⚠️ BÉ ƠI, NHÌN XA RA NÀO!",
                color = Color(0xFFFBBF24),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Màn hình tự động tắt để bảo vệ thị lực của bé.",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Con hãy đưa điện thoại ra xa hơn ${safeThresholdCm}cm để màn hình tự bật lại nhé!",
                color = Color(0xFF94A3B8),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Distance Meter Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Khoảng cách hiện tại:",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${currentDistanceCm} cm (Quá gần!)",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Khoảng cách an toàn tối thiểu:",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${safeThresholdCm} cm",
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Simulation button: Push device away
            Button(
                onClick = onSimulatePushAway,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF059669),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(48.dp)
                    .testTag("simulate_push_away_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Đã đưa điện thoại ra xa (>35cm)",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Parent Unlock
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showPinDialog = true }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Parent Unlock",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Phụ huynh mở khóa (Nhập mã PIN)",
                    color = Color(0xFF38BDF8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    if (showPinDialog) {
        PinEntryDialog(
            title = "Mở Khóa Phụ Huynh",
            subtitle = "Nhập mã PIN phụ huynh để tạm thời tắt màn hình đen",
            onDismiss = { showPinDialog = false },
            onConfirm = { pin ->
                val ok = onDismissWithPin(pin)
                if (ok) showPinDialog = false
                ok
            }
        )
    }
}
