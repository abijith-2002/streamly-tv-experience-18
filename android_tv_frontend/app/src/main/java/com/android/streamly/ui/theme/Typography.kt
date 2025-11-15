package com.android.streamly.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Typography token mapping (approx):
 * - typo-23: 30px, 500, lh 32px, letterSpacing -1.8967px
 * - typo-24: 32px, 400, lh 32px
 * - typo-25: 36px, 700, lh 42.18px
 * - typo-26: 30px, 400, lh 35.16px
 * - typo-27: 21.33px, 500, lh 21px
 * - typo-28: 29px, 400, lh 33.98px, letterSpacing 0.3816px
 * - typo-29: 29px, 700, lh 32px, centered (handled in Composables)
 *
 * Note: LetterSpacing in Compose is best in em; convert px/size ≈ em.
 */
@Immutable
data class TvTypography(
    val titleXL: TextStyle,    // typo-25
    val titleL: TextStyle,     // typo-24
    val labelL: TextStyle,     // typo-23
    val bodyL: TextStyle,      // typo-26
    val badge: TextStyle,      // typo-27
    val nav: TextStyle,        // typo-28
    val navActive: TextStyle,  // typo-29
)

private fun pxToEm(letterPx: Float, fontSizePx: Float): TextUnit {
    if (fontSizePx == 0f) return 0.em
    return (letterPx / fontSizePx).em
}

val DefaultTvTypography = TvTypography(
    titleXL = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold, // 700
        fontSize = 36.sp,
        lineHeight = 42.2.sp,
        letterSpacing = 0.sp
    ),
    titleL = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal, // 400
        fontSize = 32.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    labelL = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium, // 500
        fontSize = 30.sp,
        lineHeight = 32.sp,
        // -1.8967 px on 30px font ≈ -0.0632 em
        letterSpacing = pxToEm(letterPx = -1.8967f, fontSizePx = 30f)
    ),
    bodyL = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal, // 400
        fontSize = 30.sp,
        lineHeight = 35.16.sp,
        letterSpacing = 0.sp
    ),
    badge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium, // 500
        fontSize = 21.33.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp
    ),
    nav = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal, // 400
        fontSize = 26.sp, // reduced ~3sp to fit within centered bar
        lineHeight = 30.sp,
        // Recompute letterSpacing proportionally for 26px base: 0.3816px on 26px ≈ 0.01468em
        letterSpacing = pxToEm(letterPx = 0.3816f, fontSizePx = 26f)
    ),
    navActive = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold, // 700
        fontSize = 26.sp, // reduced ~3sp
        lineHeight = 30.sp,
        // Proportional letterSpacing
        letterSpacing = pxToEm(letterPx = 0.3816f, fontSizePx = 26f)
    )
)

internal val LocalTvTypography = staticCompositionLocalOf { DefaultTvTypography }

/**
 * Utility accessors for typography.
 */
object StreamlyTypography {
    val default: TvTypography get() = DefaultTvTypography
}
