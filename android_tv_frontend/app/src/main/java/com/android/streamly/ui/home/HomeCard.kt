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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.Image
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
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
 * Pixel-accurate content card for TV rails that supports:
 * 1) Default poster variant (e.g., "Seguí viendo") with embedded progress bar and title box.
 * 2) TV Channel variant ("Canales de TV") with left poster, right info column, live badge,
 *    small progress bar under poster, and a circular channel icon.
 *
 * Geometry and colors derive from assets/home-page-screen_1-2.html:
 * - Default poster sizes follow the existing implementation (474x329 | 412x312).
 * - TV channel card: 745x212 (second card can be 221 high in assets; callers provide size).
 * - TV poster inside channel card: 377x212 (scaled).
 * - Info column left offset: 408px; Title/meta/Live badge/time offsets per assets (scaled).
 * - Live badge: 94x32 with radius ~3.8px and typo-27.
 * - Small progress under poster: 207x10.4 track with ~0.8px insets for fill.
 *
 * Parameters:
 * - card: data for the card (title used for labels)
 * - width: target card width (scaled to viewport)
 * - height: target card height (scaled to viewport)
 * - modifier: optional modifier for additional behavior
 * - variant: card presentation variant
 * - showLiveBadge: when variant is TvChannel, show the "EN VIVO" badge
 * - showChannelIcon: when variant is TvChannel, show a circular channel logo placeholder
 */
enum class HomeCardVariant {
    Default,
    TvChannel
}

@Composable
fun HomeCard(
    card: CardItem,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    variant: HomeCardVariant = HomeCardVariant.Default,
    showLiveBadge: Boolean = false,
    showChannelIcon: Boolean = false
) {
    when (variant) {
        HomeCardVariant.Default -> DefaultPosterCard(card, width, height, modifier)
        HomeCardVariant.TvChannel -> TvChannelCard(
            card = card,
            width = width,
            height = height,
            modifier = modifier,
            showLiveBadge = showLiveBadge,
            showChannelIcon = showChannelIcon
        )
    }
}

/**
 * Default poster card variant used for standard rails like "Seguí viendo".
 */
@Composable
private fun DefaultPosterCard(
    card: CardItem,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier
) {
    val d = StreamlyTheme.dimens
    val t = StreamlyTheme.typography
    val c = StreamlyTheme.colors

    // Determine geometry based on width (474 vs 412 baseline)
    val isBig = width.value >= 460f

    // Baseline geometry (px) for 1920x1080 screen
    val baseCardW = if (isBig) 474f else 412f
    val baseCardH = if (isBig) 329f else 312f
    val posterH = if (isBig) 267f else 232f
    val nameBoxLeft = if (isBig) 31f else 0f
    val nameBoxTop = if (isBig) 249f else 232f
    val nameBoxW = 412f
    val nameBoxH = 80f

    // Progress geometry
    val progressLeft = if (isBig) 19.558f else 17f
    val progressTop = if (isBig) 230.172f else 200f
    val progressTrackW = if (isBig) 436.034f else 379f
    val progressTrackH = if (isBig) 18.414f else 16f
    val progressFillLeft = if (isBig) 4.6f else 4f
    val progressFillTop = if (isBig) 4.604f else 4f
    val progressFillH = if (isBig) 9.207f else 8f

    val scale = width.value / baseCardW
    fun s(px: Float): Dp = (px * scale).dp

    val ratio = card.progress?.ratio ?: 0f
    val progressFillW = (progressTrackW - progressFillLeft * 2f) * ratio

    var focused by remember { mutableStateOf(false) }
    val cardShape = RoundedCornerShape(d.radiusSm)
    // Focus ring uses theme accent at high alpha for strong contrast on dark card/background
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
                // Use the content title for TalkBack label
                contentDescription = card.title
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* no-op for now */ }
            )
    ) {
        // Poster area image or placeholder gradient
        if (card.imageResId != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = card.imageResId),
                contentDescription = card.title, // Meaningful image; label for TalkBack
                modifier = Modifier
                    .width(width)
                    .height(s(posterH))
                    .clip(cardShape),
                contentScale = ContentScale.Crop
            )
        } else {
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
        }

        // Progress bar inside poster (decorative; not read by TalkBack)
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

/**
 * TV Channel card variant used for "Canales de TV".
 * Layout per assets:
 * - Card area (callers provide width/height; baseline: 745x212).
 * - Poster left: 377x212.
 * - Info column at left 408, top 12, width 337, height ~186.
 * - Title (typo-25), Channel string (typo-26), "EN VIVO" badge (typo-27), Time (typo-26).
 * - Small progress bar under poster: 207x10.4 (track) with ~0.8 inset for fill.
 * - Circular channel icon over poster area in "danger" color.
 */
@Composable
private fun TvChannelCard(
    card: CardItem,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    showLiveBadge: Boolean,
    showChannelIcon: Boolean
) {
    val d = StreamlyTheme.dimens
    val t = StreamlyTheme.typography
    val c = StreamlyTheme.colors

    // Baseline geometry (px)
    val baseW = 745f
    val baseH = 212f

    // Poster inside this card
    val posterW = 377f
    val posterH = 212f

    // Info column/labels inside card
    val infoLeft = 408f
    val infoTop = 12f
    val infoW = 337f

    val titleTop = 0f
    val channelTop = 54f
    val liveLeft = 52f
    val liveTop = 104f
    val liveW = 94f
    val liveH = 32f
    val liveRadius = 3.8095f
    val timeTop = 151f

    // Small progress (under poster)
    val progLeft = 15f
    val progTop = 192f
    val progW = 207f
    val progH = 10.4f
    val progInset = 0.8f // fill inset

    // Channel icon (circle) over poster (assets vary 268..317 left; choose 268 to match most)
    val iconLeft = 268f
    val iconTop = 103f
    val iconSize = 91.6933f

    // Scale from explicit width (no BoxWithConstraints needed)
    val scale = width.value / baseW
    fun s(px: Float): Dp = (px * scale).dp

    // Focus and ring
    var focused by remember { mutableStateOf(false) }
    // Use theme accent at high alpha for strong contrast on dark background
    val ringColor = c.accent.copy(alpha = 0.85f)
    val borderColor = if (focused) ringColor else Color.Transparent
    val cardShape = RoundedCornerShape(d.radiusMd)

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .border(d.focusRingThickness, borderColor, cardShape)
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
                onClick = { /* no-op */ }
            )
    ) {
        // Left Poster image or placeholder gradient
        if (card.imageResId != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = card.imageResId),
                contentDescription = card.title, // Meaningful image; provide label
                modifier = Modifier
                    .size(width = s(posterW), height = s(posterH))
                    .clip(cardShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(width = s(posterW), height = s(posterH))
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
        }

        // Info column texts
        // Title
        BasicText(
            text = card.title,
            modifier = Modifier
                .offset(x = s(infoLeft + 0f), y = s(infoTop + titleTop)),
            style = t.titleXL.merge(
                TextStyle(
                    color = c.textPrimary,
                    textAlign = TextAlign.Start
                )
            )
        )

        // Channel string placeholder (e.g., "004 | Claro sports")
        BasicText(
            text = "004 | Claro sports",
            modifier = Modifier
                .offset(x = s(infoLeft + 1f), y = s(infoTop + channelTop)),
            style = t.bodyL.merge(
                TextStyle(
                    color = c.textSecondary,
                    textAlign = TextAlign.Start
                )
            )
        )

        // "EN VIVO" badge
        if (showLiveBadge) {
            val badgeShape = RoundedCornerShape(s(liveRadius))
            Box(
                modifier = Modifier
                    .offset(x = s(infoLeft + liveLeft), y = s(infoTop + liveTop))
                    .size(width = s(liveW), height = s(liveH))
                    .background(color = c.live, shape = badgeShape)
            )
            BasicText(
                text = "EN VIVO",
                modifier = Modifier
                    .offset(x = s(infoLeft + liveLeft + 6f), y = s(infoTop + liveTop + 5f)),
                style = t.badge.merge(
                    TextStyle(
                        color = c.onAccent,
                        textAlign = TextAlign.Start
                    )
                )
            )
        }

        // Time window
        BasicText(
            text = "11:30 - 12:30",
            modifier = Modifier
                .offset(x = s(infoLeft + 1f), y = s(infoTop + timeTop)),
            style = t.bodyL.merge(
                TextStyle(
                    color = c.textSecondary,
                    textAlign = TextAlign.Start
                )
            )
        )

        // Small progress track under poster (decorative)
        val ratio = card.progress?.ratio
            ?: (80f / progW).coerceIn(0f, 1f)
        val fillW = (progW - progInset * 2f) * ratio

        Box(
            modifier = Modifier
                .offset(x = s(progLeft), y = s(progTop))
                .size(width = s(progW), height = s(progH))
        ) {
            // Track
            Box(
                modifier = Modifier
                    .size(width = s(progW), height = s(progH))
                    .background(color = c.track, shape = RoundedCornerShape(s(2.4f)))
            )
            // Fill
            Box(
                modifier = Modifier
                    .offset(x = s(progInset), y = s(progInset))
                    .size(width = s(fillW), height = s(progH - progInset * 2f))
                    .background(color = c.progress, shape = RoundedCornerShape(s(1.6f)))
            )
        }

        // Channel icon (simple circle placeholder) - decorative; do not announce
        if (showChannelIcon) {
            Box(
                modifier = Modifier
                    .offset(x = s(iconLeft), y = s(iconTop))
                    .size(s(iconSize))
                    .background(color = c.danger, shape = CircleShape)
                    .clearAndSetSemantics { /* decorative */ }
            )
        }
    }
}
