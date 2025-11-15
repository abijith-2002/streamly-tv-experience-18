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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.semantics.semantics
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
 * - LazyRow rendering with exact card sizes and inter-card gaps:
 *   - "Seguí viendo": first card 474x329, others 412x312, first gap 9px then 40px
 *   - "Canales de TV": cards 745x212 (second can be 221px), first gap 0px then 82px
 * - Focusable cards apply up/down focus destinations for predictable TV navigation
 * - Accessibility: Each rail is a traversal group and each card is assigned a traversalIndex matching DPAD order
 *
 * Parameters:
 * - section: the rail section data
 * - modifier: optional modifier to attach focus and layout behavior
 * - entryFocusRequester: optional FocusRequester attached to the first card in the row (used by parent to target this rail)
 * - upDestination: FocusRequester to move to when DPAD_UP is pressed on any card (e.g., hero)
 * - downDestination: FocusRequester to move to when DPAD_DOWN is pressed on any card (e.g., next rail or FocusRequester.Cancel)
 * - traversalGroupIndex: Float index for TalkBack traversal ordering of the rail among sibling groups
 */
@Composable
fun HomeRail(
    section: RailSection,
    modifier: Modifier = Modifier,
    entryFocusRequester: FocusRequester? = null,
    upDestination: FocusRequester? = null,
    downDestination: FocusRequester? = null,
    traversalGroupIndex: Float = 0f
) {
    val d = StreamlyTheme.dimens
    val t = StreamlyTheme.typography
    val c = StreamlyTheme.colors

    // Baseline left offset from assets (88px) expressed using theme tokens
    // 88px = 2 * spaceXl (32) + spaceLg (24)
    val leftOffset = d.spaceXl + d.spaceXl + d.spaceLg

    BoxWithConstraints(modifier = modifier) {
        // Scale using baseline width 1920 to maintain pixel-accurate geometry
        val baseW = 1920f
        val scale = maxWidth.value / baseW
        fun s(px: Float): Dp = (px * scale).dp

        val title = section.title.trim()
        val isSeguiViendo = title.equals("segui viendo", ignoreCase = true) ||
            title.equals("seguí viendo", ignoreCase = true)
        val isTvChannels = title.equals("canales de tv", ignoreCase = true)

        // Title-to-row spacing per assets:
        // - Seguí viendo: ~13px
        // - Canales de TV: 56px (1040 -> 1096)
        val titleToRowSpacingPx = when {
            isTvChannels -> 56f
            isSeguiViendo -> 13f
            else -> 16f // default safe spacing
        }

        // Geometry for rails
        // Seguí viendo (assets)
        val bigW = 474f
        val bigH = 329f
        val smallW = 412f
        val smallH = 312f
        val gapAfterFirstSegui = 9f
        val gapDefaultSegui = 40f

        // Canales de TV (assets)
        val tvW = 745f
        val tvH = 212f
        val tvHSecond = 221f // second card can be taller in assets
        val gapFirstTv = 0f
        val gapNextTv = 82f

        Column(
            modifier = Modifier
                .padding(start = leftOffset)
                .semantics {
                    // Order rails top-to-bottom for TalkBack consistent with D-pad
                    isTraversalGroup = true
                    traversalIndex = traversalGroupIndex
                }
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

            Spacer(modifier = Modifier.height(s(titleToRowSpacingPx)))

            // Focus group row of items; children (cards) are the focus targets
            LazyRow(
                modifier = Modifier.focusGroup()
            ) {
                itemsIndexed(section.items) { index, card ->
                    // Prepend dynamic spacer BEFORE each item except index 0
                    if (index > 0) {
                        val px = when {
                            isSeguiViendo && index == 1 -> gapAfterFirstSegui
                            isSeguiViendo -> gapDefaultSegui
                            isTvChannels && index == 1 -> gapFirstTv
                            isTvChannels -> gapNextTv
                            else -> 40f
                        }
                        Spacer(modifier = Modifier.width(s(px)))
                    }

                    // Build a modifier per-card to attach entry focus and vertical traversal
                    var cardModifier = Modifier
                        .focusProperties {
                            up = upDestination ?: FocusRequester.Default
                            down = downDestination ?: FocusRequester.Default
                        }
                        .semantics {
                            // Ensure each card is announced left-to-right in order
                            traversalIndex = index.toFloat()
                        }

                    if (index == 0 && entryFocusRequester != null) {
                        cardModifier = cardModifier.focusRequester(entryFocusRequester)
                    }

                    if (isSeguiViendo) {
                        // Poster variant with size differences for the first card
                        val (wPx, hPx) = if (index == 0) bigW to bigH else smallW to smallH
                        HomeCard(
                            card = card,
                            width = s(wPx),
                            height = s(hPx),
                            modifier = cardModifier,
                            variant = HomeCardVariant.Default
                        )
                    } else if (isTvChannels) {
                        // TV Channel card variant
                        val hPx = if (index == 1) tvHSecond else tvH
                        HomeCard(
                            card = card,
                            width = s(tvW),
                            height = s(hPx),
                            modifier = cardModifier,
                            variant = HomeCardVariant.TvChannel,
                            showLiveBadge = true,
                            showChannelIcon = true
                        )
                    } else {
                        // Default sizing for other sections (fallback)
                        HomeCard(
                            card = card,
                            width = s(smallW),
                            height = s(smallH),
                            modifier = cardModifier,
                            variant = HomeCardVariant.Default
                        )
                    }
                }
            }
        }
    }
}
