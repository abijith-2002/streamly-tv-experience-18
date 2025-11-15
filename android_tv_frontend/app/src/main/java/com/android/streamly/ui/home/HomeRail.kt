package com.android.streamly.ui.home

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.streamly.model.home.RailSection
import com.android.streamly.ui.theme.StreamlyTheme

/**
 * PUBLIC_INTERFACE
 * HomeRail
 * Pixel-aligned content rail for Android TV, modeled from assets/home-page-screen_1-2.html.
 *
 * Behavior:
 * - Renders section title using typography tokens (typo-24 -> t.titleL)
 * - Left offset aligns to 1920x1080 baseline using theme spacings (88px = 2*spaceXl + spaceLg)
 * - LazyRow rendering with exact card sizes and inter-card gaps (special handling for "Seguí viendo")
 * - Focusable rail group to support D-pad navigation across items
 *
 * Parameters:
 * - section: the rail section data
 * - modifier: optional modifier to attach focus and layout behavior
 */
@Composable
fun HomeRail(
    section: RailSection,
    modifier: Modifier = Modifier
) {
    val d = StreamlyTheme.dimens
    val t = StreamlyTheme.typography
    val c = StreamlyTheme.colors

    // Baseline left offset from assets (88px) expressed using theme tokens
    // 88px = 2 * spaceXl (32) + spaceLg (24)
    val leftOffset = d.spaceXl + d.spaceXl + d.spaceLg

    // Vertical spacing between title and the row is ~13px in assets; approximate with 12-16dp
    val titleToRowSpacing = 13.dp

    BoxWithConstraints(modifier = modifier) {
        // Scale factors to maintain pixel-accurate geometry across resolutions, using 1920 baseline
        val baseW = 1920f
        val scale = maxWidth.value / baseW

        fun s(px: Float): Dp = (px * scale).dp

        // Default card geometry for "Seguí viendo" as per assets:
        // First card: 474x329; Others: 412x312; gaps: first->second 9px, then 40px
        val isSeguiViendo = section.title.trim().lowercase().contains("seguí viendo")

        // Constants in px (assets)
        val bigW = 474f
        val bigH = 329f
        val smallW = 412f
        val smallH = 312f

        // Gaps (assets)
        val gapAfterFirst = 9f
        val gapDefault = 40f

        Column(
            modifier = Modifier
                .padding(start = leftOffset)
        ) {
            // Section title
            BasicText(
                text = section.title,
                style = t.titleL.merge(
                    TextStyle(
                        color = c.textPrimary,
                        textAlign = TextAlign.Start
                    )
                )
            )

            Spacer(modifier = Modifier.height(titleToRowSpacing))

            // Focusable row of items
            LazyRow(
                modifier = Modifier.focusGroup()
            ) {
                // Render items with custom pre-spacing to match exact gaps
                itemsIndexed(section.items) { index, card ->
                    // Prepend dynamic spacer BEFORE each item except index 0
                    if (index > 0) {
                        // For the "Seguí viendo" rail, first gap is 9px, then 40px
                        val px = if (isSeguiViendo && index == 1) gapAfterFirst else gapDefault
                        Spacer(modifier = Modifier.width(s(px)))
                    }

                    // Card size: first bigger, others standard, only for "Seguí viendo"
                    val (wPx, hPx) = if (isSeguiViendo && index == 0) {
                        bigW to bigH
                    } else {
                        smallW to smallH
                    }

                    HomeCard(
                        card = card,
                        width = s(wPx),
                        height = s(hPx)
                    )
                }
            }
        }
    }
}
