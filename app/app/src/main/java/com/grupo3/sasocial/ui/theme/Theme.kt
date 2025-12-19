package com.grupo3.sasocial.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SASColorScheme = lightColorScheme(
    primary = SASGreen,
    onPrimary = SASWhite,
    primaryContainer = SASGreenLight,
    secondary = SASGreenDark,
    onSecondary = SASWhite,
    background = SASBackground,
    surface = SASWhite,
    onBackground = SASGreenDark,
    onSurface = SASGreenDark,
    error = SASRed,
    onError = SASWhite
)

@Composable
fun SASocialTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = SASColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SASGreen.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
