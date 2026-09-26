package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AtlasDarkColorScheme = darkColorScheme(
    primary = AtlasCyan,
    onPrimary = AtlasBgDark,
    primaryContainer = AtlasCardDark,
    onPrimaryContainer = AtlasCyan,
    secondary = AtlasBlue,
    onSecondary = AtlasBgDark,
    secondaryContainer = AtlasCardElevated,
    onSecondaryContainer = AtlasTextPrimary,
    tertiary = AtlasPurple,
    onTertiary = AtlasTextPrimary,
    background = AtlasBgDark,
    onBackground = AtlasTextPrimary,
    surface = AtlasSurfaceDark,
    onSurface = AtlasTextPrimary,
    surfaceVariant = AtlasCardDark,
    onSurfaceVariant = AtlasTextSecondary,
    outline = AtlasBorder,
    error = AtlasError
)

private val AtlasLightColorScheme = AtlasDarkColorScheme // Atlas defaults to its signature cyber-dark theme

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Signature dark sci-fi mode for Atlas AI
    dynamicColor: Boolean = false, // Keep signature cyan/blue sci-fi identity
    content: @Composable () -> Unit
) {
    val colorScheme = AtlasDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = AtlasBgDark.toArgb()
                window.navigationBarColor = AtlasBgDark.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
