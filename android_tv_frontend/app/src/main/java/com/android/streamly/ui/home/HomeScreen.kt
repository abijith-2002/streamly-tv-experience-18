package com.android.streamly.ui.home

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import com.android.streamly.model.home.HeroItem
import com.android.streamly.model.home.NavItem
import com.android.streamly.model.home.RailSection
import com.android.streamly.ui.theme.StreamlyTheme

/**
 * PUBLIC_INTERFACE
 * HomeScreen
 * Scaffolding for the Android TV Home screen. Establishes a persistent header slot,
 * a main 16:9 content area aligned with a 1920x1080 baseline, and basic focus traversal
 * placeholders between header, hero, and the first rail. Uses StreamlyTheme tokens.
 *
 * Parameters:
 * - nav: List of navigation items for the header
 * - activeIndex: The index of the active navigation entry
 * - hero: Current hero item
 * - rails: Content rail sections below the hero
 * - contentPadding: Optional padding for content insets (unused in this placeholder)
 */
@Composable
fun HomeScreen(
    nav: List<NavItem>,
    activeIndex: Int,
    hero: HeroItem?,
    rails: List<RailSection>,
    contentPadding: PaddingValues = PaddingValues()
) {
    val spacing = StreamlyTheme.dimens

    // Focus requesters for basic traversal placeholders
    val headerFR = remember { FocusRequester() }
    val heroFR = remember { FocusRequester() }
    val firstRailFR = remember { FocusRequester() }

    StreamlyTheme {
        HomeScaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            header = {
                HomeHeader(
                    items = nav,
                    activeIndex = activeIndex,
                    modifier = Modifier
                        .focusRequester(headerFR)
                        .focusable()
                        .focusProperties {
                            // Move focus down from header into the hero
                            down = heroFR
                        }
                )
            },
            content = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(spacing.spaceLg),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = spacing.spaceMd) // keep some breathing room at bottom
                ) {
                    HomeHero(
                        hero = hero,
                        modifier = Modifier
                            .focusRequester(heroFR)
                            .focusable()
                            .focusProperties {
                                // Up returns to header, down goes to the first rail
                                up = headerFR
                                down = firstRailFR
                            }
                    )

                    rails.forEachIndexed { index, section ->
                        val railModifier = if (index == 0) {
                            Modifier
                                .focusRequester(firstRailFR)
                                .focusable()
                                .focusProperties {
                                    // Moving up from the first rail returns to hero
                                    up = heroFR
                                }
                        } else {
                            Modifier
                        }

                        HomeRail(
                            section = section,
                            modifier = railModifier
                        )
                    }
                }
            }
        )
    }
}
