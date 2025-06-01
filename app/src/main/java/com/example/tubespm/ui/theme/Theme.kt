package com.example.tubespm.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color


private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4FC3F7),
    onPrimary = Color.White,
    secondary = Color(0xFF81C784),
    onSecondary = Color.White,
    background = Color(0xFF222831),
    surface = Color(0xFF393E46),
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF30363D),
    onSurfaceVariant = Color(0xFFB0BEC5),
    outline = Color(0xFF616161),
    error = Color(0xFFEF5350),
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF222831),
    onPrimary = Color.White,
    secondary = Color(0xFF00ACC1),
    onSecondary = Color.White,
    background = Color.White,
    surface = Color(0xFFF5F5F5),
    onBackground = Color(0xFF222831),
    onSurface = Color(0xFF222831),
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF757575),
    outline = Color(0xFFBDBDBD),
    error = Color(0xFFD32F2F),
    onError = Color.White
)
@Composable
fun BlogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
@Composable
fun TubesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}