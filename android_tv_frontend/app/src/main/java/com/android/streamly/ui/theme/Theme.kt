package com.android.streamly.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * PUBLIC_INTERFACE
 * StreamlyTheme
 * Apply Streamly TV theme to descendant composables by providing colors, typography and dimens
 * derived from the web tokens (assets/common.css and home-page-screen_1-2.css).
 *
 * Parameters:
 * - colors: TvColors to apply (defaults to DefaultTvColors)
 * - typography: TvTypography token set (defaults to DefaultTvTypography)
 * - dimens: TvDimens token set (defaults to DefaultTvDimens)
 * - content: UI content composable scope
 */
@Composable
fun StreamlyTheme(
    colors: TvColors = DefaultTvColors,
    typography: TvTypography = DefaultTvTypography,
    dimens: TvDimens = DefaultTvDimens,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalTvColors provides colors,
        LocalTvTypography provides typography,
        LocalTvDimens provides dimens
    ) {
        content()
    }
}

/**
 * Convenience object for retrieving token values from CompositionLocal providers.
 */
object StreamlyTheme {
    val colors: TvColors
        @Composable get() = LocalTvColors.current

    val typography: TvTypography
        @Composable get() = LocalTvTypography.current

    val dimens: TvDimens
        @Composable get() = LocalTvDimens.current
}
