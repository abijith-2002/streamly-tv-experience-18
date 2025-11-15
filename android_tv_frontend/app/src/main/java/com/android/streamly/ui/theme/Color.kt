package com.android.streamly.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * TvColors maps design tokens from assets/common.css and assets/home-page-screen_1-2.css
 * into Compose Color values.
 *
 * CSS token mapping reference (common.css -> Compose):
 * - --color-121212 -> background (tv-bg)
 * - --color-ffffff -> textPrimary
 * - --color-7f8282 -> textSecondary (muted)
 * - --color-323131 -> surface
 * - --color-28292f -> surface2
 * - --color-2c2c2c -> track (progress track)
 * - --color-de1717 -> progress (progress fill)
 * - --color-e1251b -> accent (primary accent)
 * - --color-eb0045 -> live (badge)
 * - --color-c60000 -> danger
 *
 * Screen tokens mapping (home-page-screen_1-2.css -> Compose):
 * - --tv-bg        -> background
 * - --tv-text      -> textPrimary
 * - --tv-muted     -> textSecondary
 * - --tv-surface   -> surface
 * - --tv-surface-2 -> surface2
 * - --tv-track     -> track
 * - --tv-progress  -> progress
 * - --tv-accent    -> accent
 * - --tv-live      -> live
 * - --tv-danger    -> danger
 *
 * "On" colors are set for legibility on dark surfaces.
 */
@Immutable
data class TvColors(
    val background: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val surface: Color,
    val surface2: Color,
    val track: Color,
    val progress: Color,
    val accent: Color,
    val live: Color,
    val danger: Color,
    // Convenience "on" colors
    val onAccent: Color,
    val onSurface: Color,
    val onBackground: Color,
)

/**
 * Default color palette approximated from design tokens for the Streamly TV experience.
 */
val DefaultTvColors = TvColors(
    background = Color(0xFF121212),  // --color-121212
    textPrimary = Color(0xFFFFFFFF), // --color-ffffff
    textSecondary = Color(0xFF7F8282), // --color-7f8282
    surface = Color(0xFF323131),    // --color-323131
    surface2 = Color(0xFF28292F),   // --color-28292f
    track = Color(0xFF2C2C2C),      // --color-2c2c2c
    progress = Color(0xFFDE1717),   // --color-de1717
    accent = Color(0xFFE1251B),     // --color-e1251b
    live = Color(0xFFEB0045),       // --color-eb0045
    danger = Color(0xFFC60000),     // --color-c60000
    onAccent = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    onBackground = Color(0xFFFFFFFF),
)

internal val LocalTvColors = staticCompositionLocalOf { DefaultTvColors }

/**
 * Utility accessors for theming.
 */
object StreamlyColors {
    val default: TvColors get() = DefaultTvColors
}
