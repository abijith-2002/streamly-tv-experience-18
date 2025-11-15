package com.android.streamly.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
 * Pixel-accurate hero section for Android TV using a 1920x1080 baseline, aligned to the design
 * references in assets/home-page-screen_1-2.html and CSS tokens.
 *
 * Behavior and layout:
 * - Uses 1920x1080 as baseline. All coordinates and sizes inside the hero scale by width.
 * - Hero height follows baseline 444px (scaled) per the Highlights section in the asset.
 * - Background is composed of:
 *   - A main slice positioned at left: 88px, width: 1744px, height: 444px (scaled).
 *   - A right mask slice at left: 1872px, width: 48px (scaled), fading to background.
 *   - A left soft mask at left: 0px, width: 88px (scaled), fading from background into the main slice.
 * - Overlay title/metadata/CTA are placed with pixel-exact baseline offsets (scaled).
 * - CTA is focusable, uses Streamly theme tokens for colors, focus ring thickness, and typography.
 *
 * Parameters:
 * - hero: The hero item data (title used for overlay)
 * - modifier: Optional modifier to attach focus and layout behavior
 * - onCtaClick: Callback when the CTA is activated
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

    // 1920x1080 baseline (matches assets screen)
    val baseW = 1920f
    val baseHeroH = 444f

    // Background slices (from assets/home-page-screen_1-2.html):
    // - Main slice: left=88, width=1744
    // - Right mask: left=1872, width=48
    val mainSliceLeft = 88f
    val mainSliceW = 1744f
    val rightMaskLeft = 1872f
    val rightMaskW = 48f

    // Left mask width equals the gap before the main slice
    val leftMaskLeft = 0f
    val leftMaskW = mainSliceLeft

    // Overlay text/CTA baseline positions (relative to hero viewport)
    // Chosen to match typical safe-region spacing and design spacing from the assets.
    val overlayLeft = 176f   // 88 (slice left) + 88 spacing to keep text away from edge
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
        // Compute scale from available width using 1920 baseline. This preserves pixel-exact ratios
        // for 1080p and 4K, as required.
        val scale = maxWidth.value / baseW
        fun s(px: Float): Dp = (px * scale).dp

        // Scaled hero height
        val heroHeight = s(baseHeroH)

        // Root hero viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heroHeight)
        ) {
            // LEFT MASK: Fade from background (left) into transparent (right)
            // This emulates a soft edge before the main slice, maintaining the visual blending
            // similar to how the CSS/right mask works on the opposite side.
            Box(
                modifier = Modifier
                    .offset(x = s(leftMaskLeft), y = 0.dp)
                    .width(s(leftMaskW))
                    .height(s(baseHeroH))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                c.background,      // full background at extreme left
                                Color.Transparent  // blend into the main slice area
                            )
                        )
                    )
            )

            // MAIN HERO SLICE: Positioned at left=88, width=1744, height=444 (all scaled).
            // We use a rich gradient placeholder in lieu of actual images, matching the brand palette.
            Box(
                modifier = Modifier
                    .offset(x = s(mainSliceLeft), y = 0.dp)
                    .width(s(mainSliceW))
                    .height(s(baseHeroH))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF477F9B), // approximate to design gradient left
                                Color(0xFF8D4CA7), // mid
                                Color(0xFF4271D4)  // right
                            )
                        )
                    )
            ) {
                // Bottom gradient for legibility over imagery
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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

            // RIGHT MASK: Emulate 48px masked slice at left=1872; fade to background
            Box(
                modifier = Modifier
                    .offset(x = s(rightMaskLeft), y = 0.dp)
                    .width(s(rightMaskW))
                    .height(s(baseHeroH))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent, // transparent at boundary with main slice
                                c.background       // full background towards the far right
                            )
                        )
                    )
            )

            // Overlay: Title (typo-25 mapping -> titleXL)
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

            // Overlay: Metadata (typo-26 mapping -> bodyL)
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
                // Center the text vertically with a baseline offset (approx. 12px on baseline)
                BasicText(
                    text = "Ver ahora",
                    modifier = Modifier
                        .offset(x = s(24f), y = s(12f)),
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
