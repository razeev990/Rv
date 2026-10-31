package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = CinemaGold,
  onPrimary = OnCinemaGold,
  primaryContainer = CinemaGoldContainer,
  onPrimaryContainer = CinemaGold,
  secondary = CyanWantToWatch,
  onSecondary = Color(0xFF041E2B),
  secondaryContainer = CyanWantToWatchContainer,
  onSecondaryContainer = CyanWantToWatch,
  tertiary = EmeraldWatched,
  onTertiary = Color(0xFF02261A),
  tertiaryContainer = EmeraldWatchedContainer,
  onTertiaryContainer = EmeraldWatched,
  background = ObsidianBackground,
  onBackground = TextPrimaryLight,
  surface = ObsidianSurface,
  onSurface = TextPrimaryLight,
  surfaceVariant = ObsidianSurfaceVariant,
  onSurfaceVariant = TextSecondaryLight,
  error = VelvetCrimson
)

private val LightColorScheme = lightColorScheme(
  primary = CinemaGoldDark,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFFFF0B3),
  onPrimaryContainer = Color(0xFF2B2100),
  secondary = Color(0xFF0284C7),
  onSecondary = Color.White,
  tertiary = Color(0xFF059669),
  onTertiary = Color.White,
  background = DaylightBackground,
  onBackground = Color(0xFF0F172A),
  surface = DaylightSurface,
  onSurface = Color(0xFF0F172A),
  surfaceVariant = DaylightSurfaceVariant,
  onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
