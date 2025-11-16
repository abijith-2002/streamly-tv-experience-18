package com.android.streamly.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
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
 * Resized and centered header for Android TV with DPAD navigation:
 * - Top nav container is fixed to 579.5dp x 32dp, centered
 * - All nav items (Search, tabs, Avatar) are focusable with explicit left/right chaining
 * - Visual focus state via border ring or highlight
 * - Handles DPAD_LEFT/RIGHT to move focus across Search -> Tabs -> Avatar and back
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
        // Compute dynamic horizontal centering for "Claro video" between left edge and navbar's left edge.
        // We keep the navbar fixed at 579.5.dp x 32.dp and centered; then compute the midpoint to place the label centered on it.
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val availableW = this.maxWidth
            val navLeft = (availableW - navWidth) / 2
            val midBetweenLeftAndNav = navLeft / 2

            var labelTextWidthPx by remember { mutableStateOf(0) }
            val labelTextWidthDp: Dp = with(density) { labelTextWidthPx.toDp() }

            val labelYOffset = 25.9063.dp

            BasicText(
                text = "Claro video",
                modifier = Modifier
                    .offset(
                        x = (midBetweenLeftAndNav - (labelTextWidthDp / 2)).coerceAtLeast(0.dp),
                        y = labelYOffset
                    )
                    .semantics { contentDescription = "Claro video" },
                onTextLayout = { layout -> labelTextWidthPx = layout.size.width },
                style = t.titleL.merge(
                    TextStyle(
                        color = c.textPrimary,
                        textAlign = TextAlign.Start,
                        fontSize = 15.sp
                    )
                )
            )
        }

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

            // Tabs row centered within the capsule
            val labelHPad = 6.dp
            val capsuleHPad = 12.dp

            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
                    // Make the nav row an explicit focus group; children are focus targets
                    .focusGroup()
            ) {
                // Search button
                val searchIS = remember { MutableInteractionSource() }
                val searchFocused by searchIS.collectIsFocusedAsState()
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
                        .focusProperties {
                            right = firstTabFR
                            left = avatarFR
                            down = downDestination ?: FocusRequester.Default
                        }
                        .focusTarget()
                        .focusable(interactionSource = searchIS)
                        .border(
                            width = if (searchFocused) d.focusRingThickness else 0.dp,
                            color = if (searchFocused) ringColor else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable(
                            interactionSource = searchIS,
                            indication = null
                        ) { onSearchClick() }
                        .onKeyEvent { ev ->
                            if (ev.type != KeyEventType.KeyDown) return@onKeyEvent false
                            when (ev.key) {
                                Key.DirectionRight -> {
                                    firstTabFR.requestFocus(); true
                                }
                                Key.DirectionLeft -> {
                                    avatarFR.requestFocus(); true
                                }
                                else -> false
                            }
                        }
                        .background(color = Color.Transparent)
                ) {
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
                    // Focus drives visuals; selected only updates semantics
                    val isActive = (index == activeIndex) || item.active
                    var textWidthPx by remember { mutableStateOf(0) }
                    val textWidthDp: Dp = with(density) { textWidthPx.toDp() }

                    // Pill geometry
                    val capsuleH = 28.dp
                    val capsuleRadius = 14.dp
                    val capsuleW = (textWidthDp + capsuleHPad).coerceAtLeast(44.dp)

                    // Focused pill color (#9B0F0F)
                    val focusedPillColor = Color(0xFF9B0F0F)
                    val tabRingColor = c.accent

                    // InteractionSource drives focus visuals
                    val tabIS = remember { MutableInteractionSource() }
                    val hasFocus by tabIS.collectIsFocusedAsState()

                    // Notify external listener when focus changes for this tab
                    LaunchedEffect(hasFocus) {
                        onTabFocusChanged?.invoke(index, hasFocus)
                    }

                    // Text color: focused primary, otherwise secondary
                    val textColor = if (hasFocus) c.onSurface else c.textSecondary

                    val frForTab = when {
                        index == 0 && firstTabExternalFR != null -> firstTabExternalFR
                        index in internalTabFRs.indices -> internalTabFRs[index]
                        else -> null
                    }

                    Box(
                        modifier = Modifier
                            .height(capsuleH)
                            .wrapContentWidth()
                    ) {
                        // Focus pill driven purely by focus state
                        if (hasFocus) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(width = capsuleW, height = capsuleH)
                                    .background(
                                        color = focusedPillColor,
                                        shape = RoundedCornerShape(capsuleRadius)
                                    )
                                    .border(
                                        width = d.focusRingThickness,
                                        color = tabRingColor,
                                        shape = RoundedCornerShape(capsuleRadius)
                                    )
                                    .clearAndSetSemantics { /* decorative */ }
                            )
                        }

                        // Tab text as focus target
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
                                .focusProperties {
                                    left = if (index == 0) searchFR else {
                                        if (index - 1 in internalTabFRs.indices) internalTabFRs[index - 1] else firstTabFR
                                    }
                                    right = if (index == items.lastIndex) avatarFR else {
                                        if (index + 1 in internalTabFRs.indices) internalTabFRs[index + 1] else lastTabFR
                                    }
                                    down = downDestination ?: FocusRequester.Default
                                }
                                .focusTarget() // explicit focus node
                                .focusable(interactionSource = tabIS) // connect focus updates to the interactionSource
                                .clickable(
                                    interactionSource = tabIS,
                                    indication = null
                                ) { onTabSelected(index, item) }
                                .onKeyEvent { ev ->
                                    if (ev.type != KeyEventType.KeyDown) return@onKeyEvent false
                                    when (ev.key) {
                                        Key.DirectionLeft -> {
                                            if (index == 0) {
                                                searchFR.requestFocus()
                                            } else {
                                                internalTabFRs.getOrNull(index - 1)?.requestFocus()
                                            }
                                            true
                                        }
                                        Key.DirectionRight -> {
                                            if (index == items.lastIndex) {
                                                avatarFR.requestFocus()
                                            } else {
                                                internalTabFRs.getOrNull(index + 1)?.requestFocus()
                                            }
                                            true
                                        }
                                        else -> false
                                    }
                                },
                            onTextLayout = { layout -> textWidthPx = layout.size.width },
                            style = t.nav.merge(
                                TextStyle(
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

        // Avatar halo (decorative)
        val avatarIS = remember { MutableInteractionSource() }
        val avatarFocused by avatarIS.collectIsFocusedAsState()
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

        // Avatar
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
                .focusProperties {
                    left = lastTabFR
                    right = searchFR
                    down = downDestination ?: FocusRequester.Default
                }
                .focusTarget()
                .focusable(interactionSource = avatarIS)
                .border(
                    width = if (avatarFocused) d.focusRingThickness else 0.dp,
                    color = if (avatarFocused) ringColor else Color.Transparent,
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = avatarIS,
                    indication = null
                ) { onAvatarClick() }
                .onKeyEvent { ev ->
                    if (ev.type != KeyEventType.KeyDown) return@onKeyEvent false
                    when (ev.key) {
                        Key.DirectionLeft -> {
                            lastTabFR.requestFocus(); true
                        }
                        Key.DirectionRight -> {
                            searchFR.requestFocus(); true
                        }
                        else -> false
                    }
                }
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
