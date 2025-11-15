package com.android.streamly.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.streamly.model.home.HeroItem
import com.android.streamly.ui.theme.StreamlyTheme

/**
 * PUBLIC_INTERFACE
 * HomeHero
 * A TV-ready, pixel-aligned hero section that reproduces the background slice and right-edge mask
 * behavior from assets/home-page-screen_1-2.html using a 1920x1080 baseline, scaled responsively.
 *
 * Behavior and layout:
 * - Uses a 1920x1080 baseline and scales all coordinates by available width to fit 1080p and 4K.
 * - Hero viewport height is kept proportional to baseline 444px height (scale * 444), matching design.
 * - Central main slice placed at baseline (left: 88, width: 1744, height: 444).
 * - Right mask gradient emulates the 48px masked slice (left: 1872, width: 48, height: 444).
 * - Title/metadata/CTA overlay with pixel-exact baseline offsets, scaled to current viewport.
 * - CTA is focusable and provides visual feedback consistent with TV focus rings.
 *
 * Parameters:
 * - hero: The hero item data (title and optional image URL; currently used for title overlay)
 * - modifier: Optional modifier to attach focus and layout behavior
 * - onCtaClick: Callback when "Ver ahora" CTA is activated
 */
@Composable
fun HomeHero(
    hero: HeroItem?,
    modifier: Modifier = Modifier,
    onCtaClick: () -> Unit = {}
) {
    val c = StreamlyTheme.colors
    val t = StreamlyTheme.typography
    val d = StreamlyTheme.dimens

    // Baseline for the home screen (width=1920, heroHeight=444) from the assets.
    val baseW = 1920f
    val baseHeroH = 444f

    // Elements in the hero area from assets (all in pixels relative to screen baseline)
    val mainSliceLeft = 88f
    val mainSliceW = 1744f
    val rightMaskLeft = 1872f
    val rightMaskW = 48f

    // Overlay text and CTA baseline coordinates (relative to hero viewport)
    // These are chosen to align visually with the design spacing and readable areas.
    val overlayLeft = 176f // 88 (slice left) + ~88 spacing
    val titleTop = 40f
    val metaTop = 110f
    val ctaTop = 180f
    val ctaW = 220f
    val ctaH = 56f
    val ctaRadius = 10f

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Scale factor based on current width, preserving 16:9 baseline proportions for coordinates.
        val scale = maxWidth.value / baseW
        fun s(px: Float): Dp = (px * scale).dp

        // Compute scaled height for the hero viewport
        val heroHeight = s(baseHeroH)

        // Root hero viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heroHeight)
        ) {
            // Main hero slice: use a rich gradient placeholder replicating the "image slice"
            // The actual images are design references; we simulate the hero background with a gradient
            // and dark overlay to ensure text readability.
            Box(
                modifier = Modifier
                    .offset(x = s(mainSliceLeft), y = 0.dp)
                    .width(s(mainSliceW))
                    .height(s(baseHeroH))
                    .background(
                        brush = Brush.linearGradient(
                            // Colors chosen from design tokens (common.css): 477f9b, 8d4ca7, 4271d4
                            colors = listOf(
                                Color(0xFF477F9B),
                                Color(0xFF8D4CA7),
                                Color(0xFF4271D4)
                            )
                        )
                    )
            ) {
                // Bottom gradient to enhance text legibility against the hero
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    c.background.copy(alpha = 0.32f)
                                )
                            )
                        )
                )
            }

            // Right edge mask: emulate the 48px masked slice by blending to background.
            Box(
                modifier = Modifier
                    .offset(x = s(rightMaskLeft), y = 0.dp)
                    .width(s(rightMaskW))
                    .height(s(baseHeroH))
                    .background(
                        brush = Brush.horizontalGradient(
                            // Transparent at left edge of mask, to full background at right edge.
                            colors = listOf(
                                Color.Transparent,
                                c.background
                            )
                        )
                    )
            )

            // Overlay: Title
            BasicText(
                text = hero?.title ?: "Destacado",
                modifier = Modifier
                    .offset(x = s(overlayLeft), y = s(titleTop)),
                style = t.titleXL.merge(
                    TextStyle(
                        color = c.textPrimary,
                        textAlign = TextAlign.Start
                    )
                )
            )

            // Overlay: Metadata (placeholder copy, optional)
            BasicText(
                text = "Acción • Ciencia ficción • 2 h 13 min",
                modifier = Modifier
                    .offset(x = s(overlayLeft), y = s(metaTop)),
                style = t.bodyL.merge(
                    TextStyle(
                        color = c.textSecondary,
                        textAlign = TextAlign.Start
                    )
                )
            )

            // Overlay: CTA "Ver ahora" (Focusable TV button)
            val ctaFR = remember { FocusRequester() }
            var ctaFocused by remember { mutableStateOf(false) }

            val ctaShape = RoundedCornerShape(s(ctaRadius))
            val ctaBg = if (ctaFocused) c.accent.copy(alpha = 0.95f) else c.accent
            val ctaBorderColor = if (ctaFocused) c.onAccent.copy(alpha = 0.85f) else Color.Transparent

            Box(
                modifier = Modifier
                    .offset(x = s(overlayLeft), y = s(ctaTop))
                    .size(width = s(ctaW), height = s(ctaH))
                    .background(color = ctaBg, shape = ctaShape)
                    .border(width = d.focusRingThickness, color = ctaBorderColor, shape = ctaShape)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Ver ahora"
                    }
                    .focusRequester(ctaFR)
                    .focusable()
                    .onFocusChanged { state -> ctaFocused = state.isFocused }
                    .clickable(
                        enabled = true,
                        onClick = onCtaClick,
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    )
            ) {
                BasicText(
                    text = "Ver ahora",
                    modifier = Modifier
                        .offset(x = s(24f), y = s(14f)),
                    style = t.navActive.merge(
                        TextStyle(
                            color = c.onAccent,
                            textAlign = TextAlign.Start
                        )
                    )
                )
            }
        }
    }
}
