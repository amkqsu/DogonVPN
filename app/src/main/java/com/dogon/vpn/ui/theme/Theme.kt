package com.dogon.vpn.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DogonColorScheme = darkColorScheme(
    primary = AccentLive,
    background = BgBase,
    surface = BgCard,
    onPrimary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    secondary = AccentWarn
)

@Composable
fun DogonVPNTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DogonColorScheme,
        typography = DogonTypography,
        content = content
    )
}
