package com.android.streamly.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.streamly.model.home.NavItem
import com.android.streamly.ui.theme.StreamlyTheme

/**
 * PUBLIC_INTERFACE
 * HomeHeader
 * Resized and centered header for Android TV:
 * - Smaller nav bar height (≈60dp), smaller background capsule and icons
 * - Tabs are centered horizontally, with relative spacing; no hard absolute X positions
 * - Active/focus capsules sized to text width + padding to avoid clipping
 * - "Claro video" label rendered smaller to prevent crowding
 * - Search/Avatar aligned to start/end and sized to match reduced header height
 *
 * Accessibility:
 * - Decorative shapes are removed from the a11y tree
 * - Traversal group and indexes ensure TalkBack follows DPAD order
 */
@Composable
fun HomeHeader(
    items: List<NavItem>,
    activeIndex: Int,
    modifier: Modifier = Modifier,
    onTabSelected: (index: Int, item: NavItem) -> Unit = { _, _ -> },
    onTabFocusChanged: ((index: Int, hasFocus: Boolean) -> Unit)? = null,
    onSearchClick: () -> Unit = {},
    onAvatarClick: () -> Unit = {},
    downDestination: FocusRequester? = null,
    firstTabExternalFR: FocusRequester? = null,
    lastTabExternalFR: FocusRequester? = null,
    autoFocusFirstTab: Boolean = false
) {
    val t = StreamlyTheme.typography
    val c = StreamlyTheme.colors
    val d = StreamlyTheme.dimens
    val density = LocalDensity.current

    // Header sizing (reduced)
    val headerH = 60.dp

    // TopNav capsule sizing (reduced)
    val topNavBgH = 50.dp
    val topNavBgRadius = 28.dp

    // Search and Avatar sizing (scaled down)
    val searchTouch = 32.dp
    val sidePad = 24.dp

    val avatarHaloSize = 60.dp
    val avatarSize = 48.dp

    // Focus management: search, tabs, avatar
    val searchFR = remember { FocusRequester() }
    val avatarFR = remember { FocusRequester() }
    val internalTabFRs = remember(items.size) { List(items.size) { FocusRequester() } }
    val firstTabFR = firstTabExternalFR ?: (internalTabFRs.firstOrNull() ?: FocusRequester.Default)
    val lastTabFR = lastTabExternalFR ?: (internalTabFRs.lastOrNull() ?: FocusRequester.Default)

    // Traversal order within header (TalkBack): Search (0), Tabs (1..N), Avatar (N+1)
    val avatarTraversalIndex = 1f + items.size.toFloat()

    // Root header: full width, centered content
    Box(
        modifier = modifier
            .focusGroup()
            .fillMaxWidth()
            .height(headerH)
            .semantics {
                isTraversalGroup = true
                traversalIndex = 0f
            }
    ) {
        // Left-aligned logo/text "Claro video" with smaller font to reduce crowding
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 0.dp)
                .height(headerH)
                .clearAndSetSemantics { /* decorative */ }
        ) {
            BasicText(
                text = "Claro video",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 0.dp), // keep at start inside safe margin from scaffold
                style = t.labelL.merge(
                    TextStyle(
                        // Reduce size specifically for this label as requested
                        fontSize = 24.sp,
                        color = c.textPrimary,
                        textAlign = TextAlign.Start
                    )
                )
            )
        }

        // Centered TopNav background capsule limited to a max width
        val maxNavWidth = 1100.dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .widthIn(max = maxNavWidth)
                .height(headerH)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp)
                    .height(topNavBgH)
                    .background(color = c.surface2, shape = RoundedCornerShape(topNavBgRadius))
                    .clearAndSetSemantics { /* decorative */ }
            )

            // Tabs row centered within the capsule, with spacing that adapts to fit longest label
            val labelHPad = 10.dp
            val capsuleHPad = 20.dp
            val rowHPad = 32.dp
            val itemSpacing = 28.dp

            Row(
                horizontalArrangement = Arrangement.spacedBy(itemSpacing, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = rowHPad)
                    .height(topNavBgH)
            ) {
                items.forEachIndexed { index, item ->
                    val isActive = (index == activeIndex) || item.active
                    var hasFocus by remember { mutableStateOf(false) }
                    var textWidthPx by remember { mutableStateOf(0) }
                    val textWidthDp: Dp = with(density) { textWidthPx.toDp() }
                    val capsuleW = (textWidthDp + capsuleHPad).coerceAtLeast(56.dp)
                    val capsuleH = 48.dp
                    val capsuleRadius = 28.dp

                    val ringColor = c.accent
                    val textColor = when {
                        hasFocus -> c.onSurface
                        isActive -> c.onSurface
                        else -> c.textSecondary
                    }
                    val focusBgAlpha = if (hasFocus) 0.12f else 0f

                    val frForTab = when {
                        index == 0 && firstTabExternalFR != null -> firstTabExternalFR
                        index in internalTabFRs.indices -> internalTabFRs[index]
                        else -> null
                    }

                    // Each tab is a box that draws its own background capsule sized to text width
                    Box(
                        modifier = Modifier
                            .padding(vertical = (topNavBgH - capsuleH) / 2f)
                            .height(capsuleH)
                            .wrapContentWidth()
                    ) {
                        // Focus backdrop
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(width = capsuleW, height = capsuleH)
                                .background(
                                    color = c.onSurface.copy(alpha = focusBgAlpha),
                                    shape = RoundedCornerShape(capsuleRadius)
                                )
                                .border(
                                    width = if (hasFocus) d.focusRingThickness else 0.dp,
                                    color = if (hasFocus) ringColor else Color.Transparent,
                                    shape = RoundedCornerShape(capsuleRadius)
                                )
                                .clearAndSetSemantics { /* decorative */ }
                        )
                        // Active backdrop (under text) - lighter accent
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(width = capsuleW, height = capsuleH)
                                    .background(
                                        color = c.accent.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(capsuleRadius)
                                    )
                                    .clearAndSetSemantics { /* decorative */ }
                            )
                        }

                        // Tab text
                        BasicText(
                            text = item.title,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = labelHPad)
                                .semantics {
                                    role = Role.Tab
                                    selected = isActive
                                    contentDescription = item.title
                                    stateDescription = if (isActive) "seleccionada" else "no seleccionada"
                                    traversalIndex = 1f + index.toFloat()
                                }
                                .then(if (frForTab != null) Modifier.focusRequester(frForTab) else Modifier)
                                .focusable()
                                .focusProperties {
                                    left = if (index == 0) searchFR else {
                                        if (index - 1 in internalTabFRs.indices) internalTabFRs[index - 1] else firstTabFR
                                    }
                                    right = if (index == items.lastIndex) avatarFR else {
                                        if (index + 1 in internalTabFRs.indices) internalTabFRs[index + 1] else lastTabFR
                                    }
                                    down = downDestination ?: FocusRequester.Default
                                }
                                .onFocusChanged {
                                    hasFocus = it.isFocused
                                    onTabFocusChanged?.invoke(index, it.isFocused)
                                }
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    onTabSelected(index, item)
                                },
                            onTextLayout = { layout ->
                                textWidthPx = layout.size.width
                            },
                            style = (if (isActive) t.navActive else t.nav).merge(
                                TextStyle(
                                    color = textColor,
                                    textAlign = TextAlign.Start
                                )
                            )
                        )
                    }
                }
            }
        }

        // Search button aligned to start, vertically centered
        var searchFocused by remember { mutableStateOf(false) }
        val ringColor = c.accent
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = sidePad)
                .size(searchTouch)
                .clip(CircleShape)
                .semantics {
                    role = Role.Button
                    contentDescription = "Buscar"
                    traversalIndex = 0f
                }
                .focusRequester(searchFR)
                .focusable()
                .focusProperties {
                    right = firstTabFR
                    left = avatarFR
                    down = downDestination ?: FocusRequester.Default
                }
                .onFocusChanged { searchFocused = it.isFocused }
                .border(
                    width = if (searchFocused) d.focusRingThickness else 0.dp,
                    color = if (searchFocused) ringColor else Color.Transparent,
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onSearchClick() }
                .background(color = Color.Transparent)
        ) {
            // Simple magnifying glass icon (drawn)
            Canvas(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.Center)
                    .clearAndSetSemantics { /* decorative */ }
            ) {
                drawCircle(
                    color = c.textPrimary,
                    radius = size.minDimension * 0.35f,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawLine(
                    color = c.textPrimary,
                    start = androidx.compose.ui.geometry.Offset(
                        x = size.width * 0.65f,
                        y = size.height * 0.65f
                    ),
                    end = androidx.compose.ui.geometry.Offset(
                        x = size.width * 0.9f,
                        y = size.height * 0.9f
                    ),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Avatar halo (decorative) aligned to end
        var avatarFocused by remember { mutableStateOf(false) }
        val haloAlpha = if (avatarFocused) 0.2f else 0.0001f
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = sidePad - ((avatarHaloSize - avatarSize) / 2f))
                .size(avatarHaloSize)
                .clip(CircleShape)
                .background(color = c.accent.copy(alpha = haloAlpha))
                .clearAndSetSemantics { /* decorative */ }
        )

        // Avatar aligned to end
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = sidePad)
                .size(avatarSize)
                .clip(CircleShape)
                .background(color = c.textSecondary.copy(alpha = 0.4f))
                .border(width = 1.dp, color = c.onSurface.copy(alpha = 0.4f), shape = CircleShape)
                .semantics {
                    role = Role.Button
                    contentDescription = "Perfil"
                    traversalIndex = avatarTraversalIndex
                }
                .focusRequester(avatarFR)
                .focusable()
                .focusProperties {
                    left = lastTabFR
                    right = searchFR
                    down = downDestination ?: FocusRequester.Default
                }
                .onFocusChanged { avatarFocused = it.isFocused }
                .border(
                    width = if (avatarFocused) d.focusRingThickness else 0.dp,
                    color = if (avatarFocused) ringColor else Color.Transparent,
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onAvatarClick() }
        )

        // Auto focus first tab if requested
        LaunchedEffect(items.size, autoFocusFirstTab) {
            if (autoFocusFirstTab) {
                firstTabFR.requestFocus()
            }
        }
    }
}

private fun Dp?.orZero(): Dp = this ?: 0.dp
