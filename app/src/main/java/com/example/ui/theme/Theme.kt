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

private val DarkColorScheme = darkColorScheme(
    primary = HighDensityPrimaryDark,
    onPrimary = Color.Black,
    secondary = HighDensitySecondaryDark,
    onSecondary = Color.Black,
    tertiary = HighDensityPrimaryDark,
    onTertiary = Color.Black,
    background = Slate900Dark,
    onBackground = Slate100Dark,
    surface = Slate800Dark,
    onSurface = Slate100Dark,
    outline = Slate700Dark,
    error = CoralRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = HighDensityPrimaryLight,
    onPrimary = Color.White,
    secondary = HighDensitySecondaryLight,
    onSecondary = Color.White,
    tertiary = HighDensitySecondaryLight,
    onTertiary = Color.White,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate800,
    outline = Slate200,
    error = CoralRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Allow turning dynamic color off/on as needed
    dynamicColor: Boolean = false, // Let's default to false to strictly enforce our premium dark/light branding colors!
    content: @Composable () -> Unit,
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
