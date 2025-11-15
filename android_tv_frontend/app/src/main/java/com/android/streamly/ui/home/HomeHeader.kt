package com.android.streamly.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.streamly.model.home.NavItem
import com.android.streamly.ui.theme.StreamlyTheme

/**
 * PUBLIC_INTERFACE
 * HomeHeader
 * Pixel-aligned header layout for Android TV based on assets/home-page-screen_1-2.html:
 * - Places logo (placeholder), search button, navigation tabs, and avatar with exact coordinates
 * - Tabs are focusable and expose selected state (aria-current equivalent via semantics.selected)
 * - D-Pad navigation is wired: Search <-> Tabs <-> Avatar, with proper left/right traversal
 * - Down navigation from header elements moves to the hero (downDestination) when provided
 * - Exposes ability to auto-focus first tab and to attach external FocusRequesters to first/last tabs
 *
 * Accessibility:
 * - Decorative shapes (backgrounds, halos, active capsules) are removed from the a11y tree.
 * - Traversal group and indexes ensure TalkBack follows the same order as DPAD.
 *
 * Parameters:
 * - items: List of NavItem representing the top nav
 * - activeIndex: Index of the active tab (selected)
 * - modifier: Optional modifier for the header root (wrapped in a focusGroup)
 * - onTabSelected: Callback when a tab is clicked/OK-pressed
 * - onTabFocusChanged: Optional callback when a tab gains/loses focus
 * - onSearchClick: Callback when search button is activated
 * - onAvatarClick: Callback when avatar is activated
 * - downDestination: FocusRequester used when pressing DPAD_DOWN on any header element (usually hero)
 * - firstTabExternalFR: External FocusRequester to attach to the first tab for cross-boundary navigation
 * - lastTabExternalFR: External FocusRequester to attach to the last tab for cross-boundary navigation
 * - autoFocusFirstTab: When true, requests focus on the first tab on first composition
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

    // Header frame from assets (relative to screen safe area)
    val headerW = 1466.2799.dp
    val headerH = 74.dp

    // TopNav frame inside header (pixel coordinates from assets)
    val topNavLeft = 307.2799.dp
    val topNavTop = 0.dp
    val topNavW = 1159.dp
    val topNavH = 74.dp

    // TopNav background capsule
    val topNavBgLeft = 0.dp
    val topNavBgTop = 4.dp
    val topNavBgW = topNavW
    val topNavBgH = 64.dp
    val topNavBgRadius = 34.dp

    // Search icon/button in TopNav
    val searchLeft = 26.753.dp
    val searchTop = 20.789.dp
    val searchTouch = 36.dp // focus target; icon drawn inside

    // Avatar focus halo and avatar positions (within TopNav)
    val avatarHaloLeft = 1068.7201.dp
    val avatarHaloTop = 0.dp
    val avatarHaloSize = 74.dp
    val avatarLeft = 1077.7201.dp
    val avatarTop = 8.dp
    val avatarSize = 56.dp

    // Navigation tab label X offsets within TopNav
    val tabLefts: List<Dp> = listOf(110.dp, 230.dp, 400.dp, 532.dp, 717.dp, 824.dp)

    // Active tab indicator (capsule) relative to header
    // For "Inicio" (index 0) the left is 404.2799dp. This equals topNavLeft + tabLeft(0) - 13dp
    val activeCapsuleTop = 10.dp
    val activeCapsuleW = 96.dp
    val activeCapsuleH = 53.dp
    val activeCapsuleRadius = 37.dp
    val activeCapsuleLeft = if (activeIndex in tabLefts.indices) {
        topNavLeft + tabLefts[activeIndex] - 13.dp
    } else {
        topNavLeft + tabLefts.firstOrNull().orZero() - 13.dp
    }

    // Focus management: search, tabs, avatar
    val searchFR = remember { FocusRequester() }
    val avatarFR = remember { FocusRequester() }
    val internalTabFRs = remember(items.size) { List(items.size) { FocusRequester() } }

    val firstTabFR = firstTabExternalFR ?: (internalTabFRs.firstOrNull() ?: FocusRequester.Default)
    val lastTabFR = lastTabExternalFR ?: (internalTabFRs.lastOrNull() ?: FocusRequester.Default)

    // Traversal order within header (TalkBack): Search (0), Tabs (1..N), Avatar (N+1)
    val avatarTraversalIndex = 1f + items.size.toFloat()

    // Root header: focus group container to scope directional nav within the header, and traversal group for TalkBack order
    Box(
        modifier = modifier
            .focusGroup()
            .width(headerW)
            .height(headerH)
            .semantics {
                // Ensure TalkBack starts at header as a group and reads children in DPAD order
                isTraversalGroup = true
                traversalIndex = 0f
            }
    ) {
        // Logo placeholder group (non-interactive heading-like visual)
        Box(
            modifier = Modifier
                .offset(x = 0.dp, y = 15.8129.dp)
                .width(169.637.dp)
                .height(34.3558.dp)
                .clearAndSetSemantics { /* decorative */ }
        ) {
            BasicText(
                text = "Claro video",
                style = t.labelL.merge(
                    TextStyle(
                        color = c.textPrimary,
                        textAlign = TextAlign.Start
                    )
                )
            )
        }

        // Active tab background capsule (behind nav text)
        Box(
            modifier = Modifier
                .offset(x = activeCapsuleLeft, y = activeCapsuleTop)
                .width(activeCapsuleW)
                .height(activeCapsuleH)
                .background(color = c.accent.copy(alpha = 0.35f), shape = RoundedCornerShape(activeCapsuleRadius))
                .clearAndSetSemantics { /* decorative */ }
        )

        // TopNav container
        Box(
            modifier = Modifier
                .offset(x = topNavLeft, y = topNavTop)
                .width(topNavW)
                .height(topNavH)
        ) {
            // Background capsule (decorative)
            Box(
                modifier = Modifier
                    .offset(x = topNavBgLeft, y = topNavBgTop)
                    .width(topNavBgW)
                    .height(topNavBgH)
                    .background(color = c.surface2, shape = RoundedCornerShape(topNavBgRadius))
                    .clearAndSetSemantics { /* decorative */ }
            )

            // Search button with focus ring
            var searchFocused by remember { mutableStateOf(false) }
            // Focus ring uses fully opaque accent for WCAG contrast on dark background
            val ringColor = c.accent
            Box(
                modifier = Modifier
                    .offset(x = searchLeft, y = searchTop)
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
                        // Right goes to first tab when available; left loops to avatar (wrap)
                        right = firstTabFR
                        left = avatarFR
                        // Down exits header to hero (if provided)
                        down = downDestination ?: FocusRequester.Default
                    }
                    .onFocusChanged { searchFocused = it.isFocused }
                    .border(width = if (searchFocused) d.focusRingThickness else 0.dp, color = if (searchFocused) ringColor else Color.Transparent, shape = CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSearchClick() }
                    .background(color = Color.Transparent)
            ) {
                // Draw a simple magnifying glass to avoid external assets (decorative)
                Canvas(
                    modifier = Modifier
                        .size(22.dp)
                        .offset(x = 7.dp, y = 7.dp)
                        .clearAndSetSemantics { /* decorative */ }
                ) {
                    // Lens
                    drawCircle(
                        color = c.textPrimary,
                        radius = size.minDimension * 0.35f,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    // Handle
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

            // Avatar focus halo (subtle when not focused) - decorative
            var avatarFocused by remember { mutableStateOf(false) }
            val haloAlpha = if (avatarFocused) 0.2f else 0.0001f
            Box(
                modifier = Modifier
                    .offset(x = avatarHaloLeft, y = avatarHaloTop)
                    .size(avatarHaloSize)
                    .clip(CircleShape)
                    .background(color = c.accent.copy(alpha = haloAlpha))
                    .clearAndSetSemantics { /* decorative */ }
            )

            // Avatar (round) with focus ring
            Box(
                modifier = Modifier
                    .offset(x = avatarLeft, y = avatarTop)
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
                        // Left goes to last tab (if present); right loops to search
                        left = lastTabFR
                        right = searchFR
                        // Down exits header to hero (if provided)
                        down = downDestination ?: FocusRequester.Default
                    }
                    .onFocusChanged { avatarFocused = it.isFocused }
                    .border(width = if (avatarFocused) d.focusRingThickness else 0.dp, color = if (avatarFocused) ringColor else Color.Transparent, shape = CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onAvatarClick() }
            )

            // Navigation tabs
            // Ensure we have enough positions for the given items.
            val positions: List<Dp> = if (items.size <= tabLefts.size) {
                tabLefts.take(items.size)
            } else {
                // If more items are provided than the design, place extras with a fixed spacing after the last known.
                val extra = items.size - tabLefts.size
                val extraStart = tabLefts.last()
                val spacing = 140.dp // approximate spacing consistent with design
                tabLefts + List(extra) { idx -> extraStart + spacing * (idx + 1) }
            }

            items.forEachIndexed { index, item ->
                val tabX = positions[index]
                val isActive = (index == activeIndex) || item.active
                var hasFocus by remember { mutableStateOf(false) }

                val textColor = when {
                    hasFocus -> c.onSurface
                    isActive -> c.onSurface
                    else -> c.textSecondary
                }
                val bgOnFocusAlpha = if (hasFocus) 0.12f else 0f

                // Focus background and ring capsule behind label (decorative backdrop)
                Box(
                    modifier = Modifier
                        .offset(x = tabX - 16.dp, y = 10.dp) // 16dp padding around label for focus background
                        .width(activeCapsuleW)
                        .height(activeCapsuleH)
                        .background(
                            color = c.onSurface.copy(alpha = bgOnFocusAlpha),
                            shape = RoundedCornerShape(activeCapsuleRadius)
                        )
                        .border(
                            width = if (hasFocus) d.focusRingThickness else 0.dp,
                            color = if (hasFocus) ringColor else Color.Transparent,
                            shape = RoundedCornerShape(activeCapsuleRadius)
                        )
                        .clearAndSetSemantics { /* decorative */ }
                )

                val frForTab = when {
                    index == 0 && firstTabExternalFR != null -> firstTabExternalFR
                    index in internalTabFRs.indices -> internalTabFRs[index]
                    else -> null
                }

                // Tab text itself
                BasicText(
                    text = item.title,
                    modifier = Modifier
                        .offset(x = tabX, y = 19.dp)
                        .semantics {
                            role = Role.Tab
                            selected = isActive
                            contentDescription = item.title
                            stateDescription = if (isActive) "seleccionada" else "no seleccionada"
                            // Ensure TalkBack reads tabs left-to-right in the same order as D-pad
                            traversalIndex = 1f + index.toFloat()
                        }
                        .then(if (frForTab != null) Modifier.focusRequester(frForTab) else Modifier)
                        .focusable()
                        .focusProperties {
                            left = if (index == 0) searchFR else {
                                if (index - 1 in internalTabFRs.indices) internalTabFRs[index - 1] else firstTabFR
                            }
                            right = if (index == items.size - 1) avatarFR else {
                                if (index + 1 in internalTabFRs.indices) internalTabFRs[index + 1] else lastTabFR
                            }
                            // Down exits header to hero (if provided)
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
                    style = (if (isActive) t.navActive else t.nav).merge(
                        TextStyle(
                            color = textColor,
                            textAlign = TextAlign.Start
                        )
                    )
                )
            }
        }

        // Optionally move initial focus to the first tab
        LaunchedEffect(items.size, autoFocusFirstTab) {
            if (autoFocusFirstTab) {
                // Request focus to the first tab in header
                firstTabFR.requestFocus()
            }
        }
    }
}

private fun Dp?.orZero(): Dp = this ?: 0.dp
