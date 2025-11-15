package com.android.streamly.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
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
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
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
 * - Exposes callbacks for tab selection, search click, and avatar click
 *
 * Parameters:
 * - items: List of NavItem representing the top nav
 * - activeIndex: Index of the active tab (selected)
 * - modifier: Optional modifier for the header root (will be wrapped in a focusGroup)
 * - onTabSelected: Callback when a tab is clicked/OK-pressed
 * - onTabFocusChanged: Optional callback when a tab gains/loses focus
 * - onSearchClick: Callback when search button is activated
 * - onAvatarClick: Callback when avatar is activated
 */
@Composable
fun HomeHeader(
    items: List<NavItem>,
    activeIndex: Int,
    modifier: Modifier = Modifier,
    onTabSelected: (index: Int, item: NavItem) -> Unit = { _, _ -> },
    onTabFocusChanged: ((index: Int, hasFocus: Boolean) -> Unit)? = null,
    onSearchClick: () -> Unit = {},
    onAvatarClick: () -> Unit = {}
) {
    val t = StreamlyTheme.typography
    val c = StreamlyTheme.colors

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
    val searchTouch = 36.dp // touch target; icon drawn inside

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
    // For "Inicio" (index 0) the left is 404.2799dp. This equals topNavLeft + tabLeft(0) - 13dp: 307.2799 + 110 - 13 = 404.2799
    // We'll generalize as: topNavLeft + tabLeft - 13dp
    val activeCapsuleTop = 10.dp
    val activeCapsuleW = 96.dp
    val activeCapsuleH = 53.dp
    val activeCapsuleRadius = 37.dp
    val activeCapsuleLeft = if (activeIndex in tabLefts.indices) {
        topNavLeft + tabLefts[activeIndex] - 13.dp
    } else {
        // Fallback: if active index outside known positions, clamp to first
        topNavLeft + tabLefts.firstOrNull().orZero() - 13.dp
    }

    // Focus management: search, tabs, avatar
    val searchFR = remember { FocusRequester() }
    val avatarFR = remember { FocusRequester() }
    val tabFRs = remember(items.size) { List(items.size) { FocusRequester() } }

    val firstTabFR = if (tabFRs.isNotEmpty()) tabFRs.first() else FocusRequester.Default
    val lastTabFR = if (tabFRs.isNotEmpty()) tabFRs.last() else FocusRequester.Default

    // Root header: focus group container so directional nav enters this group and lands on children
    Box(
        modifier = modifier
            .focusGroup()
            .width(headerW)
            .height(headerH)
    ) {
        // Logo placeholder group: assets say (left:0, top:15.8129, width:169.637, height:34.3558)
        // We render a simple text placeholder with the correct position/size.
        Box(
            modifier = Modifier
                .offset(x = 0.dp, y = 15.8129.dp)
                .width(169.637.dp)
                .height(34.3558.dp)
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

        // Active tab capsule drawn at header level (sits visually behind nav text)
        Box(
            modifier = Modifier
                .offset(x = activeCapsuleLeft, y = activeCapsuleTop)
                .width(activeCapsuleW)
                .height(activeCapsuleH)
                .background(color = c.accent.copy(alpha = 0.35f), shape = RoundedCornerShape(activeCapsuleRadius))
        )

        // TopNav container
        Box(
            modifier = Modifier
                .offset(x = topNavLeft, y = topNavTop)
                .width(topNavW)
                .height(topNavH)
        ) {
            // Background capsule
            Box(
                modifier = Modifier
                    .offset(x = topNavBgLeft, y = topNavBgTop)
                    .width(topNavBgW)
                    .height(topNavBgH)
                    .background(color = c.surface2, shape = RoundedCornerShape(topNavBgRadius))
            )

            // Search button
            Box(
                modifier = Modifier
                    .offset(x = searchLeft, y = searchTop)
                    .size(searchTouch)
                    .clip(CircleShape)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Buscar"
                    }
                    .focusRequester(searchFR)
                    .focusProperties {
                        // Right goes to first tab when available; left loops to avatar (wrap)
                        right = firstTabFR
                        left = avatarFR
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSearchClick() }
                    .background(color = Color.Transparent)
            ) {
                // Draw a simple magnifying glass to avoid external assets
                Canvas(
                    modifier = Modifier
                        .size(22.dp)
                        .offset(x = 7.dp, y = 7.dp)
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

            // Avatar focus halo (appears stronger on focus; otherwise subtle)
            var avatarFocused by remember { mutableStateOf(false) }
            val haloAlpha = if (avatarFocused) 0.2f else 0.0001f
            Box(
                modifier = Modifier
                    .offset(x = avatarHaloLeft, y = avatarHaloTop)
                    .size(avatarHaloSize)
                    .clip(CircleShape)
                    .background(color = c.accent.copy(alpha = haloAlpha))
            )

            // Avatar (round) - using a placeholder colored circle
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
                    }
                    .focusRequester(avatarFR)
                    .focusProperties {
                        // Left goes to last tab (if present); right loops to search
                        left = lastTabFR
                        right = searchFR
                    }
                    .onFocusChanged { avatarFocused = it.isFocused }
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

                // Background on focus for better TV feedback (soft capsule)
                Box(
                    modifier = Modifier
                        .offset(x = tabX - 16.dp, y = 10.dp) // 16dp padding around label for focus background
                        .width(activeCapsuleW)
                        .height(activeCapsuleH)
                        .background(
                            color = c.onSurface.copy(alpha = bgOnFocusAlpha),
                            shape = RoundedCornerShape(activeCapsuleRadius)
                        )
                )

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
                        }
                        .focusRequester(tabFRs[index])
                        .focusProperties {
                            left = if (index == 0) searchFR else tabFRs[index - 1]
                            right = if (index == items.size - 1) avatarFR else tabFRs[index + 1]
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

        // Move initial focus into first tab if header gets initial focus from parent
        LaunchedEffect(items.size) {
            // No-op: focus is controlled by parent via FocusRequester; tabs have defined order.
            // This hook is left for future usage if needed for programmatic focus.
        }
    }
}

private fun Dp?.orZero(): Dp = this ?: 0.dp
