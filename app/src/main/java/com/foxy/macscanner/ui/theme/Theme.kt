package com.foxy.macscanner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = TerminalGreen,
    background = DarkBackground,
    surface = DarkSurface,
    error = ErrorRed,
    onPrimary = DarkBackground,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun MacScannerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
