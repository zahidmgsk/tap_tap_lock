package com.taplock.myapplication.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun MyApplicationTheme(
    seedColor: Color = Color(0xFF6750A4),
    themeMode: String = "System",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "Light" -> false
        "Dark" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = seedColor,
            onPrimary = Color.White,
            secondary = seedColor.copy(alpha = 0.8f),
            background = Color(0xFF1C1B1F),
            surface = Color(0xFF1C1B1F)
        )
        else -> lightColorScheme(
            primary = seedColor,
            onPrimary = Color.White,
            secondary = seedColor.copy(alpha = 0.8f),
            background = Color.White,
            surface = Color.White,
            surfaceVariant = seedColor.copy(alpha = 0.1f)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
