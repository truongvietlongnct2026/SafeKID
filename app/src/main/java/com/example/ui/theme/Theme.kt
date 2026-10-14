package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = EyeShieldBlueDark,
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = EyeShieldTealDark,
    onSecondary = Color(0xFF003732),
    secondaryContainer = Color(0xFF005049),
    onSecondaryContainer = Color(0xFF70F7E8),
    tertiary = EyeShieldAmberDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = EyeShieldDangerRed
  )

private val LightColorScheme =
  lightColorScheme(
    primary = EyeShieldBluePrimary,
    onPrimary = EyeShieldOnPrimary,
    primaryContainer = Color(0xFFD0E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = EyeShieldTealSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA7F3D0),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = EyeShieldAmberAlert,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = EyeShieldDangerRed
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep consistent branding colors
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
