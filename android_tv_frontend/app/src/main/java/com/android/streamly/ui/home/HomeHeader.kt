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
 * - Top nav container is fixed to 579.5dp x 32dp, horizontally centered (no clipping)
 * - Internal elements scaled to fit the 32dp height (capsule ~28dp with 14dp radius)
 * - Tabs spaced within the fixed width with small paddings so all labels (including "TV en vivo") fit
 * - "Claro video" is NOT part of the tabs; it is rendered as a separate label positioned at
 *   x=44.36.dp and y=25.9063.dp relative to the header container.
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

    // Header sizing
    val headerH = 60.dp

    // Fixed nav container size as requested
    val navWidth = 579.5.dp
    val topNavBgH = 32.dp
    val topNavBgRadius = 16.dp

    // Search and Avatar sizing
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
        // Brand label "Claro video" positioned absolutely within the header container
        // This is not focusable and has a simple label semantics.
        BasicText(
            text = "Claro video",
            modifier = Modifier
                .offset(x = 44.36.dp, y = 25.9063.dp)
                .semantics {
                    // Non-interactive label; keep in a11y tree as static text
                    contentDescription = "Claro video"
                },
            style = t.titleL.merge(
                TextStyle(
                    color = c.textPrimary,
                    textAlign = TextAlign.Start,
                    // Requirement: set this separate label to 15.sp
                    fontSize = 15.sp
                )
            )
        )

        // Centered TopNav background capsule with exact width/height
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .width(navWidth)
                .height(topNavBgH)
                .align(Alignment.Center)
        ) {
            // Background capsule
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(color = c.surface2, shape = RoundedCornerShape(topNavBgRadius))
                    .clearAndSetSemantics { /* decorative */ }
            )

            // Tabs row centered within the capsule, with tight spacing to fit within 579.5dp
            val labelHPad = 6.dp
            val capsuleHPad = 12.dp

            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp) // inner padding so content doesn't touch edges
            ) {
                // Search button INSIDE the capsule as the first item
                var searchFocused by remember { mutableStateOf(false) }
                val ringColor = c.accent
                Box(
                    modifier = Modifier
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

                Spacer(Modifier.width(8.dp))

                // Tabs
                items.forEachIndexed { index, item ->
                    val isActive = (index == activeIndex) || item.active
                    var hasFocus by remember { mutableStateOf(false) }
                    var textWidthPx by remember { mutableStateOf(0) }
                    val textWidthDp: Dp = with(density) { textWidthPx.toDp() }

                    // Capsule geometry relative to 32dp height
                    val capsuleH = 28.dp
                    val capsuleRadius = 14.dp
                    val capsuleW = (textWidthDp + capsuleHPad).coerceAtLeast(44.dp)

                    val tabRingColor = c.accent
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

                    // Each tab container uses intrinsic width; overall row uses tight spacing
                    Box(
                        modifier = Modifier
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
                                    color = if (hasFocus) tabRingColor else Color.Transparent,
                                    shape = RoundedCornerShape(capsuleRadius)
                                )
                                .clearAndSetSemantics { /* decorative */ }
                        )
                        // Active backdrop (under text)
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

                        // Tab text with small size override to ensure fit within 32dp height
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
                                    // Tabs follow search (0f), so start at 1f
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
                                    // Override font size minimally to ensure fit within 32dp height
                                    fontSize = 16.sp,
                                    lineHeight = 20.sp,
                                    color = textColor,
                                    textAlign = TextAlign.Start
                                )
                            )
                        )
                    }

                    if (index != items.lastIndex) {
                        Spacer(Modifier.width(8.dp))
                    }
                }
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
        val ringColor = c.accent
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
