package com.example.myapplication.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NirbhayaColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Background,
    primaryContainer = SecondaryCard,
    onPrimaryContainer = PrimaryText,
    secondary = Cyan,
    onSecondary = Background,
    background = Background,
    onBackground = PrimaryText,
    surface = MainCard,
    onSurface = PrimaryText,
    surfaceVariant = SecondaryCard,
    onSurfaceVariant = SecondaryText,
    outline = Border,
    error = EmergencyRed,
    onError = PrimaryText
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Background.toArgb()
            window.navigationBarColor = Background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = NirbhayaColorScheme,
        typography = Typography,
        content = content
    )
}
