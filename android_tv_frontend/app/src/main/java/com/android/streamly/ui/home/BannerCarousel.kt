package com.android.streamly.ui.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.android.streamly.model.home.CardItem
import com.android.streamly.ui.theme.StreamlyTheme
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

/**
 * PUBLIC_INTERFACE
 * BannerCarousel
 * A TV-optimized banner carousel with:
 * - Focused item size 872.dp x 222.dp, centered horizontally on focus
 * - Unfocused items are visible as 34.dp-wide slivers, height 222.dp
 * - Spacing between items is exactly 20.dp
 * - Smooth focus transitions and D-Pad navigation (left/right), maintaining center alignment
 * - Uses LazyRow + rememberLazyListState + animateScrollToItem with dynamic side padding
 *
 * Parameters:
 * - items: List of CardItem representing each banner (title used as contentDescription)
 * - modifier: Optional modifier for the row container
 * - entryFocusRequester: FocusRequester applied to the focused item when the carousel first receives focus
 * - upDestination: FocusRequester for DPAD_UP
 * - downDestination: FocusRequester for DPAD_DOWN
 */
@Composable
fun BannerCarousel(
    items: List<CardItem>,
    modifier: Modifier = Modifier,
    entryFocusRequester: FocusRequester? = null,
    upDestination: FocusRequester? = null,
    downDestination: FocusRequester? = null
) {
    val d = StreamlyTheme.dimens
    val c = StreamlyTheme.colors

    // Fixed geometry as requested
    val rowHeight = 222.dp
    val focusedW = 872.dp
    val unfocusedW = 34.dp
    val itemSpacing = 20.dp
    val shape = RoundedCornerShape(StreamlyTheme.dimens.radiusMd)

    BoxWithConstraints(
        modifier = modifier
            .height(rowHeight)
            .semantics { /* traversal group only; no special role */ }
    ) {
        val containerW: Dp = this.maxWidth
        // Content padding so that when we scroll an item to index it lands centered
        val sidePad = ((containerW - focusedW) / 2).coerceAtLeast(0.dp)

        val listState = rememberLazyListState()
        var focusedIndex by remember { mutableIntStateOf(0) }
        val scope = rememberCoroutineScope()

        // When focus changes to a particular index, scroll to center that item.
        LaunchedEffect(focusedIndex, containerW) {
            scope.launch {
                // With symmetric side padding, animateScrollToItem centers the item start into the padded viewport.
                listState.animateScrollToItem(focusedIndex)
            }
        }

        LazyRow(
            state = listState,
            modifier = Modifier
                .focusGroup()
                .height(rowHeight),
            contentPadding = PaddingValues(horizontal = sidePad),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            userScrollEnabled = false // DPAD handles navigation; avoid accidental touch scroll
        ) {
            itemsIndexed(items) { index, card ->
                BannerItem(
                    card = card,
                    isFocusedItem = (index == focusedIndex),
                    rowHeight = rowHeight,
                    focusedWidth = focusedW,
                    unfocusedWidth = unfocusedW,
                    onFocused = {
                        focusedIndex = index
                    },
                    modifier = Modifier
                        .focusProperties {
                            up = upDestination ?: FocusRequester.Default
                            down = downDestination ?: FocusRequester.Default
                            left = FocusRequester.Default // handled by DPAD within row
                            right = FocusRequester.Default
                        }
                        .then(
                            if (index == 0 && entryFocusRequester != null) {
                                Modifier.focusRequester(entryFocusRequester)
                            } else {
                                Modifier
                            }
                        ),
                    shape = shape,
                    borderColor = c.accent,
                    focusRingThickness = d.focusRingThickness,
                    spacing = itemSpacing
                )
            }
        }
    }
}

/**
 * A single banner item that animates its width between focused and unfocused sizes.
 */
@Composable
private fun BannerItem(
    card: CardItem,
    isFocusedItem: Boolean,
    rowHeight: Dp,
    focusedWidth: Dp,
    unfocusedWidth: Dp,
    onFocused: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape,
    borderColor: Color,
    focusRingThickness: Dp,
    spacing: Dp
) {
    val density = LocalDensity.current
    val context = LocalContext.current

    var hasFocus by remember { mutableStateOf(false) }
    val targetWidth = if (hasFocus || isFocusedItem) focusedWidth else unfocusedWidth
    val animatedWidth by animateDpAsState(targetValue = targetWidth, label = "bannerWidthAnim")

    // Calculate pixel size for coil request
    val widthPx = with(density) { animatedWidth.toPx() }.toInt().coerceAtLeast(1)
    val heightPx = with(density) { rowHeight.toPx() }.toInt().coerceAtLeast(1)

    // Visual hint for non-focused items; optional subtle alpha
    val alpha = if (hasFocus) 1f else 0.9f

    Box(
        modifier = modifier
            .width(animatedWidth)
            .height(rowHeight)
            .onFocusChanged { f ->
                hasFocus = f.isFocused
                if (f.isFocused) onFocused()
            }
            .focusable()
            .border(
                width = if (hasFocus) focusRingThickness else 0.dp,
                color = if (hasFocus) borderColor else Color.Transparent,
                shape = shape
            )
            .background(color = Color.Transparent, shape = shape)
            .semantics(mergeDescendants = true) {
                role = Role.Button
            }
            // Keep spacing to the right of each item except the last; use a trailing spacer effect
        ,
    ) {
        val modelData = card.imageUrl ?: card.imageResId
        if (modelData != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(modelData)
                    .crossfade(false)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .allowHardware(true)
                    .size(coil.size.Size(widthPx, heightPx))
                    .build(),
                contentDescription = card.title,
                modifier = Modifier
                    .size(width = animatedWidth, height = rowHeight)
                    .background(color = Color.Transparent, shape = shape)
                    .clearAndSetSemantics { /* image is labeled by parent */ }
                    .graphicsLayer(alpha = alpha),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback gradient box if no image; decorative
            Box(
                modifier = Modifier
                    .size(width = animatedWidth, height = rowHeight)
                    .background(color = Color(0xFF323131), shape = shape)
                    .clearAndSetSemantics { /* decorative */ }
            )
        }
    }
}
