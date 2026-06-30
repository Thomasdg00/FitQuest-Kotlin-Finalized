package com.univpm.fitquest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.univpm.fitquest.domain.model.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = TrailGreen,
    secondary = SummitBlue,
    background = WarmSand,
    surface = WarmSand,
    onBackground = Charcoal,
    onSurface = Charcoal,
)

private val DarkColorScheme = darkColorScheme(
    primary = TrailGreen,
    secondary = SummitBlue,
)

@Composable
fun FitQuestTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
