package com.android.streamly.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.android.streamly.model.home.CardItem
import com.android.streamly.ui.theme.StreamlyTheme

/**
 * PUBLIC_INTERFACE
 * HomeCard
 * Pixel-accurate content card for TV rails with poster, title box, and progress bar.
 *
 * Geometry and colors are derived from assets/home-page-screen_1-2.html:
 * - Card sizes: 474x329 (first card) or 412x312 (others) - supplied by caller
 * - Poster heights: 267px (big) or 232px (small) - scaled from baseline
 * - Progress track/fill use tv track/progress colors with exact heights and radii
 * - Name box uses surface color with label typography
 *
 * Parameters:
 * - card: data for the card
 * - width: target card width (scaled to viewport)
 * - height: target card height (scaled to viewport)
 * - modifier: optional modifier for additional behavior
 */
@Composable
fun HomeCard(
    card: CardItem,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier
) {
    val d = StreamlyTheme.dimens
    val t = StreamlyTheme.typography
    val c = StreamlyTheme.colors

    // Determine which geometry set to use based on width (approx threshold between 474 and 412)
    val isBig = width.value >= 460f

    // Assets geometry in px for baseline (1920)
    val baseCardW = if (isBig) 474f else 412f
    val baseCardH = if (isBig) 329f else 312f
    val posterH = if (isBig) 267f else 232f
    val nameBoxLeft = if (isBig) 31f else 0f
    val nameBoxTop = if (isBig) 249f else 232f
    val nameBoxW = if (isBig) 412f else 412f
    val nameBoxH = 80f

    // Progress geometry per assets
    val progressLeft = if (isBig) 19.558f else 17f
    val progressTop = if (isBig) 230.172f else 200f
    val progressTrackW = if (isBig) 436.034f else 379f
    val progressTrackH = if (isBig) 18.414f else 16f
    val progressFillLeft = if (isBig) 4.6f else 4f
    val progressFillTop = if (isBig) 4.604f else 4f
    val progressFillH = if (isBig) 9.207f else 8f

    // Scale geometry from baseline to the requested size
    val scale = width.value / baseCardW
    fun s(px: Float): Dp = (px * scale).dp

    // Progress ratio (0..1)
    val ratio = card.progress?.ratio ?: 0f
    val progressFillW = (progressTrackW - progressFillLeft * 2f) * ratio

    // Card container focus state and focus ring style
    var focused by remember { mutableStateOf(false) }
    val cardShape = RoundedCornerShape(d.radiusSm)
    val focusRingColor = c.accent.copy(alpha = 0.85f)
    val cardBorderColor = if (focused) focusRingColor else Color.Transparent

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .border(width = d.focusRingThickness, color = cardBorderColor, shape = cardShape)
            .background(color = Color.Transparent, shape = cardShape)
            .focusable()
            .onFocusChanged { focused = it.isFocused }
            .semantics {
                role = Role.Button
                contentDescription = card.title
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* no-op for now */ }
            )
    ) {
        // Poster area (placeholder gradient instead of external image loader)
        Box(
            modifier = Modifier
                .width(width)
                .height(s(posterH))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF477F9B),
                            Color(0xFF8D4CA7),
                            Color(0xFF4271D4)
                        )
                    ),
                    shape = cardShape
                )
        )

        // Progress bar inside poster
        Box(
            modifier = Modifier
                .offset(x = s(progressLeft), y = s(progressTop))
                .size(width = s(progressTrackW), height = s(progressTrackH))
        ) {
            // Track
            Box(
                modifier = Modifier
                    .size(width = s(progressTrackW), height = s(progressTrackH))
                    .background(color = c.track, shape = RoundedCornerShape(s(progressTrackH / 2f)))
            )
            // Fill
            Box(
                modifier = Modifier
                    .offset(x = s(progressFillLeft), y = s(progressFillTop))
                    .size(width = s(progressFillW), height = s(progressFillH))
                    .background(color = c.progress, shape = RoundedCornerShape(s(progressFillH / 2f)))
            )
        }

        // Name box and title
        Box(
            modifier = Modifier
                .offset(x = s(nameBoxLeft), y = s(nameBoxTop))
                .size(width = s(nameBoxW), height = s(nameBoxH))
                .background(color = c.surface, shape = RoundedCornerShape(0.dp))
        )

        BasicText(
            text = card.title,
            modifier = Modifier
                .offset(x = s(nameBoxLeft + 16f), y = s(nameBoxTop + 24f)),
            style = t.labelL.merge(
                TextStyle(
                    color = c.onSurface,
                    textAlign = TextAlign.Start
                )
            )
        )
    }
}
