package com.android.streamly.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import com.android.streamly.R
import com.android.streamly.model.home.CardItem
import com.android.streamly.model.home.HeroItem
import com.android.streamly.model.home.NavItem
import com.android.streamly.model.home.RailSection
import com.android.streamly.ui.theme.StreamlyTheme

/**
 * PUBLIC_INTERFACE
 * HomeScreen
 * Scaffolding for the Android TV Home screen. Establishes a persistent header slot,
 * a main 16:9 content area aligned with a 1920x1080 baseline, and a predictable D-Pad focus model
 * between Header -> Hero -> Rails using FocusRequester and focusProperties.
 *
 * Focus model:
 * - Initial focus: Header first tab
 * - Header DOWN -> Hero CTA
 * - Hero UP -> Header first tab, DOWN -> First rail (first card)
 * - Rail i UP -> Rail i-1 (or Hero for the first rail)
 * - Rail i DOWN -> Rail i+1; last rail blocks DPAD_DOWN (boundary)
 *
 * Parameters:
 * - nav: List of navigation items for the header
 * - activeIndex: The index of the active navigation entry
 * - hero: Current hero item
 * - rails: Content rail sections below the hero
 * - contentPadding: Optional padding for content insets
 * - onTabSelected: Callback when a header tab is selected, used to trigger navigation
 */
@Composable
fun HomeScreen(
    nav: List<NavItem>,
    activeIndex: Int,
    hero: HeroItem?,
    rails: List<RailSection>,
    contentPadding: PaddingValues = PaddingValues(),
    onTabSelected: (index: Int, item: NavItem) -> Unit = { _, _ -> }
) {
    val spacing = StreamlyTheme.dimens

    // Focus requesters for traversal boundaries
    val headerFirstTabFR = remember { FocusRequester() }
    val heroFR = remember { FocusRequester() }
    val bannerFR = remember { FocusRequester() }
    val railEntryFRs = remember(rails.size) { List(rails.size) { FocusRequester() } }

    StreamlyTheme {
        HomeScaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            header = {
                HomeHeader(
                    items = nav,
                    activeIndex = activeIndex,
                    // Provide a FocusRequester for down traversal (header -> banner)
                    downDestination = bannerFR,
                    // Expose first tab FocusRequester to be targeted by hero/rails
                    firstTabExternalFR = headerFirstTabFR,
                    // Auto focus to first tab when screen loads
                    autoFocusFirstTab = true,
                    // Forward tab selection to caller for navigation
                    onTabSelected = onTabSelected
                )
            },
            content = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(spacing.spaceLg),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = spacing.spaceMd) // keep some breathing room at bottom
                ) {
                    // Inline demo data for banners using existing poster resources
                    val banners = listOf(
                        CardItem(id = "b1", title = "Rogue One", imageResId = R.drawable.poster_rogue_one),
                        CardItem(id = "b2", title = "Ex Machina", imageResId = R.drawable.poster_ex_machina),
                        CardItem(id = "b3", title = "2012", imageResId = R.drawable.poster_2012),
                        CardItem(id = "b4", title = "Ad Astra", imageResId = R.drawable.poster_ad_astra),
                        CardItem(id = "b5", title = "Sing Street", imageResId = R.drawable.poster_sing_street)
                    )

                    // Banner carousel between header and hero.
                    BannerCarousel(
                        items = banners,
                        entryFocusRequester = bannerFR,
                        upDestination = headerFirstTabFR,
                        downDestination = heroFR
                    )

                    // Hero with explicit up/down destinations
                    HomeHero(
                        hero = hero,
                        modifier = Modifier
                            .focusRequester(heroFR)
                            .focusProperties {
                                // Up returns to banner, down goes to the first rail entry
                                up = bannerFR
                                down = railEntryFRs.firstOrNull() ?: FocusRequester.Default
                            },
                        upDestination = bannerFR,
                        downDestination = railEntryFRs.firstOrNull(),
                        onCtaClick = { /* TODO: navigate to playback/info */ }
                    )

                    // Rails - link focus between consecutive rails and bound last rail DOWN
                    rails.forEachIndexed { index, section ->
                        val upDest = if (index == 0) {
                            // From first rail up goes to the hero container
                            heroFR
                        } else {
                            railEntryFRs[index - 1]
                        }
                        val downDest = if (index == rails.lastIndex) {
                            // Last rail: trap DOWN within the same rail to avoid experimental API usage
                            railEntryFRs[index]
                        } else {
                            railEntryFRs[index + 1]
                        }

                        HomeRail(
                            section = section,
                            // Attach entry FocusRequester to the first card in each rail
                            entryFocusRequester = railEntryFRs[index],
                            upDestination = upDest,
                            downDestination = downDest,
                            // Ensure TalkBack reads rails top-to-bottom consistent with D-pad traversal
                            traversalGroupIndex = 2f + index.toFloat()
                        )
                    }
                }

                // Initial focus: ensure header first tab gets focus on first composition
                LaunchedEffect(Unit) {
                    headerFirstTabFR.requestFocus()
                }
            }
        )
    }
}
