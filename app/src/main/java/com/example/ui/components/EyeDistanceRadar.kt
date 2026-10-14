package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DistanceState

@Composable
fun EyeDistanceRadar(
    distanceState: DistanceState,
    safeThresholdCm: Int,
    onSimulateDistance: (Int) -> Unit
) {
    val currentDistance = when (distanceState) {
        is DistanceState.Safe -> distanceState.distanceCm
        is DistanceState.Caution -> distanceState.distanceCm
        is DistanceState.TooClose -> distanceState.distanceCm
        DistanceState.Calibrating -> 35
    }

    val isDanger = currentDistance < safeThresholdCm - 5
    val isCaution = currentDistance in (safeThresholdCm - 5)..safeThresholdCm
    val isSafe = currentDistance > safeThresholdCm

    val statusColor by animateColorAsState(
        targetValue = when {
            isDanger -> Color(0xFFEF4444)
            isCaution -> Color(0xFFF59E0B)
            else -> Color(0xFF10B981)
        },
        label = "statusColor"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = (currentDistance.toFloat() / 50f).coerceIn(0.1f, 1f),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("eye_distance_radar_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Giám Sát Khoảng Cách Mắt Trực Tiếp",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Radar Ring Canvas
            Box(
                modifier = Modifier.size(170.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(160.dp)) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val maxRadius = size.width / 2 - 10f

                    // Outer Safe Ring
                    drawCircle(
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        radius = maxRadius,
                        center = center,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Middle Caution Ring
                    drawCircle(
                        color = Color(0xFFF59E0B).copy(alpha = 0.25f),
                        radius = maxRadius * 0.7f,
                        center = center,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Inner Danger Ring (Auto-Blackout zone)
                    drawCircle(
                        color = Color(0xFFEF4444).copy(alpha = 0.35f),
                        radius = maxRadius * 0.45f,
                        center = center,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Dynamic distance wave
                    drawCircle(
                        color = statusColor.copy(alpha = 0.2f),
                        radius = maxRadius * animatedProgress,
                        center = center
                    )
                    drawCircle(
                        color = statusColor,
                        radius = maxRadius * animatedProgress,
                        center = center,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }

                // Center Eye Icon & Status
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            isDanger -> Icons.Default.VisibilityOff
                            isCaution -> Icons.Default.SentimentDissatisfied
                            else -> Icons.Default.Visibility
                        },
                        contentDescription = "Khoảng cách mắt",
                        tint = statusColor,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Large Distance Number
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$currentDistance",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = statusColor
                )
                Text(
                    text = " cm",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Text(
                text = when {
                    isDanger -> "⚠️ QUÁ GẦN (<${safeThresholdCm}cm) - MÀN HÌNH TỰ TẮT!"
                    isCaution -> "⚡ Cảnh báo: Đang đến gần ngưỡng an toàn"
                    else -> "✅ Khoảng cách an toàn cho mắt của bé"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = statusColor,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Distance Slider (For parent testing & calibration)
            Text(
                text = "Kéo để thử nghiệm mô phỏng khoảng cách:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Slider(
                value = currentDistance.toFloat(),
                onValueChange = { onSimulateDistance(it.toInt()) },
                valueRange = 10f..50f,
                steps = 39,
                colors = SliderDefaults.colors(
                    thumbColor = statusColor,
                    activeTrackColor = statusColor,
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("distance_simulation_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "10 cm (Quá gần)", fontSize = 11.sp, color = Color(0xFFEF4444))
                Text(text = "30 cm (Chuẩn)", fontSize = 11.sp, color = Color(0xFF10B981))
                Text(text = "50 cm (Xa)", fontSize = 11.sp, color = Color(0xFF0284C7))
            }
        }
    }
}
