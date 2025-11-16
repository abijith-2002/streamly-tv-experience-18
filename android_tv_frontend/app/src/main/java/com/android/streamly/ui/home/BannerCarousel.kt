package com.android.streamly.ui.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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
 * TV-optimized banner carousel that:
 * - Uses fixed focused size 872.dp x 222.dp (from dimens) and centers the focused item horizontally.
 * - Applies a slight scale down (e.g., 0.9f from dimens) and lower elevation to unfocused items.
 * - Maintains exact inter-item spacing from dimens.
 * - Scrolls the focused item into the exact center using symmetric side padding.
 *
 * Parameters:
 * - items: List of CardItem representing each banner (title used as contentDescription)
 * - modifier: Optional modifier for the row container
 * - entryFocusRequester: FocusRequester applied to the initially focusable item for DPAD entry into the row
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

    val rowHeight = d.bannerHeightFocused
    val itemWidth = d.bannerWidthFocused
    val itemSpacing = d.bannerItemSpacing
    val shape = RoundedCornerShape(StreamlyTheme.dimens.radiusMd)

    BoxWithConstraints(
        modifier = modifier
            .height(rowHeight)
            .semantics { /* traversal group only; no special role */ }
    ) {
        val containerW: Dp = this.maxWidth
        // Symmetric side padding so that the selected item aligns exactly in the horizontal center.
        val sidePad = ((containerW - itemWidth) / 2).coerceAtLeast(0.dp)

        val listState = rememberLazyListState()
        var focusedIndex by remember { mutableIntStateOf(0) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(focusedIndex, containerW) {
            scope.launch {
                // With constant item width and symmetric padding, index scrolling centers the item.
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
                    itemWidth = itemWidth,
                    onFocused = { focusedIndex = index },
                    modifier = Modifier
                        .focusProperties {
                            up = upDestination ?: FocusRequester.Default
                            down = downDestination ?: FocusRequester.Default
                            left = FocusRequester.Default
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
                    unfocusedScale = d.bannerScaleUnfocused
                )
            }
        }
    }
}

/**
 * A single banner item with fixed layout size that scales and elevates on focus.
 */
@Composable
private fun BannerItem(
    card: CardItem,
    isFocusedItem: Boolean,
    rowHeight: Dp,
    itemWidth: Dp,
    onFocused: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape,
    borderColor: Color,
    focusRingThickness: Dp,
    unfocusedScale: Float
) {
    val density = LocalDensity.current
    val context = LocalContext.current

    var hasFocus by remember { mutableStateOf(false) }
    val targetScale = if (hasFocus || isFocusedItem) 1f else unfocusedScale
    val animatedScale by animateFloatAsState(targetValue = targetScale, label = "bannerScaleAnim")

    // Elevation: focused higher, unfocused lower
    val targetElevation = if (hasFocus || isFocusedItem) 8.dp else 2.dp
    val animatedElevation by animateDpAsState(targetValue = targetElevation, label = "bannerElevationAnim")

    // Calculate pixel size for coil request based on the final drawn size (after scale).
    val widthPx = with(density) { itemWidth.toPx() }.toInt().coerceAtLeast(1)
    val heightPx = with(density) { rowHeight.toPx() }.toInt().coerceAtLeast(1)

    // Subtle alpha for unfocused state
    val alpha = if (hasFocus) 1f else 0.95f

    Box(
        modifier = modifier
            .width(itemWidth)
            .height(rowHeight)
            .onFocusChanged { f ->
                hasFocus = f.isFocused
                if (f.isFocused) onFocused()
            }
            .focusable()
            .shadow(animatedElevation, shape = shape, clip = false)
            .border(
                width = if (hasFocus) focusRingThickness else 0.dp,
                color = if (hasFocus) borderColor else Color.Transparent,
                shape = shape
            )
            .background(color = Color.Transparent, shape = shape)
            .semantics(mergeDescendants = true) {
                role = Role.Button
            }
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
                    .size(width = itemWidth, height = rowHeight)
                    .background(color = Color.Transparent, shape = shape)
                    .clearAndSetSemantics { /* image is labeled by parent */ }
                    .graphicsLayer(
                        alpha = alpha,
                        scaleX = animatedScale,
                        scaleY = animatedScale
                    ),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback gradient box if no image; decorative
            Box(
                modifier = Modifier
                    .size(width = itemWidth, height = rowHeight)
                    .background(color = Color(0xFF323131), shape = shape)
                    .graphicsLayer(
                        alpha = alpha,
                        scaleX = animatedScale,
                        scaleY = animatedScale
                    )
                    .clearAndSetSemantics { /* decorative */ }
            )
        }
    }
}
