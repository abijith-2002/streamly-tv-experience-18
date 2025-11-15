package com.android.streamly.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.streamly.model.home.HeroItem
import com.android.streamly.model.home.NavItem
import com.android.streamly.model.home.RailSection
import com.android.streamly.ui.theme.StreamlyTheme

/**
 * PUBLIC_INTERFACE
 * HomeScreen
 * Scaffolding for the Android TV Home screen. This is not the final UI; it wires up
 * header, hero and rails with tokens coming from the StreamlyTheme. Intended to match
 * the structure of the Figma "Home Page - screen_1:2" for later pixel-accurate layout.
 *
 * Parameters:
 * - nav: List of navigation items for the header
 * - activeIndex: The index of the active navigation entry
 * - hero: Current hero item
 * - rails: Content rail sections below the hero
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

    StreamlyTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.safeMargin) // Overscan-safe content
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing.spaceLg),
                modifier = Modifier.fillMaxSize()
            ) {
                HomeHeader(
                    items = nav,
                    activeIndex = activeIndex
                )
                HomeHero(hero = hero)

                rails.forEach { section ->
                    HomeRail(section = section)
                }
            }
        }
    }
}
