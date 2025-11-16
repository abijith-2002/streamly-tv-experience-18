package com.android.streamly.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Dimensional tokens for TV UI derived from assets/common.css and home-page-screen_1-2.css:
 * - Spacing: --spacing-xs/sm/md/lg/xl -> 4/8/16/24/32px
 * - Radii:   --radius-lg/md/sm -> 34/10/8px
 * - Focus ring: thickness 4px, glow ~18px (modeled for usage in Composables)
 * - Safe margin (overscan protection): 48px
 * - Progress heights based on design: track ~18px, fill ~9px
 */
@Immutable
data class TvDimens(
    // Spacing
    val spaceXs: Dp,
    val spaceSm: Dp,
    val spaceMd: Dp,
    val spaceLg: Dp,
    val spaceXl: Dp,

    // Radii
    val radiusLg: Dp,
    val radiusMd: Dp,
    val radiusSm: Dp,

    // Focus ring
    val focusRingThickness: Dp,
    val focusRingGlow: Dp,

    // Safe margins for overscan
    val safeMargin: Dp,

    // Progress bar sizes
    val progressTrackHeight: Dp,
    val progressFillHeight: Dp,

    // Banner carousel geometry
    val bannerWidthFocused: Dp,
    val bannerHeightFocused: Dp,
    val bannerItemSpacing: Dp,
    // Unfocused visual scale factor for banners (0.0..1.0)
    val bannerScaleUnfocused: Float,
)

/**
 * Default dimension set approximated from CSS tokens.
 */
val DefaultTvDimens = TvDimens(
    spaceXs = 4.dp,
    spaceSm = 8.dp,
    spaceMd = 16.dp,
    spaceLg = 24.dp,
    spaceXl = 32.dp,

    radiusLg = 34.dp,
    radiusMd = 10.dp,
    radiusSm = 8.dp,

    focusRingThickness = 4.dp,
    focusRingGlow = 18.dp,

    safeMargin = 48.dp,

    progressTrackHeight = 18.dp,
    progressFillHeight = 9.dp,

    // Banner carousel
    bannerWidthFocused = 872.dp,
    bannerHeightFocused = 222.dp,
    bannerItemSpacing = 20.dp,
    bannerScaleUnfocused = 0.9f,
)

internal val LocalTvDimens = staticCompositionLocalOf { DefaultTvDimens }

/**
 * Utility accessors for dimens.
 */
object StreamlyDimens {
    val default: TvDimens get() = DefaultTvDimens
}
