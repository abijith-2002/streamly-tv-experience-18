package com.android.streamly.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.focus.focusProperties
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
 * Pixel-accurate hero section for Android TV using a 1920x1080 baseline, aligned to
 * assets/home-page-screen_1-2.html and its CSS tokens.
 *
 * Accessibility and TV focus:
 * - Root semantics content description references the hero title for screen readers.
 * - CTA is focusable and will be programmatically focused when the hero receives focus.
 * - CTA exposes Role.Button and descriptive contentDescription.
 * - Up/Down focus traversal can be configured via upDestination/downDestination FocusRequesters.
 *
 * Parameters:
 * - hero: The hero item data (title used for overlay)
 * - modifier: Optional modifier to attach focus and layout behavior
 * - onCtaClick: Callback when the CTA is activated
 * - upDestination: FocusRequester to move to when DPAD_UP is pressed (typically header first tab)
 * - downDestination: FocusRequester to move to when DPAD_DOWN is pressed (typically first rail entry)
 */
@Composable
fun HomeHero(
    hero: HeroItem?,
    modifier: Modifier = Modifier,
    onCtaClick: () -> Unit = {},
    upDestination: FocusRequester? = null,
    downDestination: FocusRequester? = null
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

    // Overlay text/CTA baseline positions (relative to hero viewport).
    // Lateral overlay offset: main slice left + (2*spaceXl + spaceLg) = 88 + 88 = 176 (from theme tokens).
    val overlayLeftPx = mainSliceLeft + (2f * d.spaceXl.value) + d.spaceLg.value
    val titleTop = 40f
    val metaTop = 110f
    val ctaTop = 180f
    val ctaW = 220f
    val ctaH = 56f
    val ctaRadius = 10f

    // Make CTA the focal point when hero receives focus (TV UX expectation).
    val ctaFR = remember { FocusRequester() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            // Forward focus from the hero container into the CTA for immediate action affordance
            .focusable()
            .onFocusChanged { state ->
                if (state.isFocused) {
                    ctaFR.requestFocus()
                }
            }
            .focusProperties {
                up = upDestination ?: FocusRequester.Default
                down = downDestination ?: FocusRequester.Default
            }
            .semantics {
                // Root A11y description for the hero section
                val label = hero?.title ?: "Destacado"
                contentDescription = "Destacado: $label"
            }
    ) {
        // Compute scale from available width using 1920 baseline to preserve pixel accuracy.
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

            // MAIN HERO SLICE: Positioned at left=88, width=1744, height=444 (scaled).
            // Using brand-like gradient in place of image for placeholder fidelity.
            Box(
                modifier = Modifier
                    .offset(x = s(mainSliceLeft), y = 0.dp)
                    .width(s(mainSliceW))
                    .height(s(baseHeroH))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF477F9B),
                                Color(0xFF8D4CA7),
                                Color(0xFF4271D4)
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
                    .offset(x = s(overlayLeftPx), y = s(titleTop)),
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
                    .offset(x = s(overlayLeftPx), y = s(metaTop)),
                style = t.bodyL.merge(
                    TextStyle(
                        color = c.textSecondary,
                        textAlign = TextAlign.Start
                    )
                )
            )

            // Overlay: CTA "Ver ahora" (Focusable TV button) with consistent focus ring
            var ctaFocused by remember { mutableStateOf(false) }

            val ctaShape = RoundedCornerShape(s(ctaRadius))
            val ringColor = c.accent.copy(alpha = 0.85f)
            val ctaBg = if (ctaFocused) c.accent.copy(alpha = 0.95f) else c.accent

            Box(
                modifier = Modifier
                    .offset(x = s(overlayLeftPx), y = s(ctaTop))
                    .size(width = s(ctaW), height = s(ctaH))
                    .background(color = ctaBg, shape = ctaShape)
                    .border(width = if (ctaFocused) d.focusRingThickness else 0.dp, color = if (ctaFocused) ringColor else Color.Transparent, shape = ctaShape)
                    .semantics {
                        role = Role.Button
                        val label = hero?.title ?: "Destacado"
                        contentDescription = "Ver ahora: $label"
                    }
                    .focusRequester(ctaFR)
                    .focusable()
                    .focusProperties {
                        up = upDestination ?: FocusRequester.Default
                        down = downDestination ?: FocusRequester.Default
                    }
                    .onFocusChanged { state -> ctaFocused = state.isFocused }
                    .clickable(
                        enabled = true,
                        onClick = onCtaClick,
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    )
            ) {
                // Text with a baseline offset for visual centering
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
