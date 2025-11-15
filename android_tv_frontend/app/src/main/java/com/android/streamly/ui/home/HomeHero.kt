package com.android.streamly.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.times
import com.android.streamly.model.home.HeroItem
import com.android.streamly.ui.theme.StreamlyTheme

/**
 * PUBLIC_INTERFACE
 * HomeHero
 * Placeholder for the large hero banner under the header.
 *
 * Parameters:
 * - hero: The hero item data (image/title not yet rendered; just a themed block)
 */
@Composable
fun HomeHero(
    hero: HeroItem?
) {
    val d = StreamlyTheme.dimens
    val c = StreamlyTheme.colors

    // For now, render a simple accent-colored wide box to mark hero area.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = d.spaceXl * 10f) // approx min height using spacing token
            .aspectRatio(1744f / 444f) // matches figma slice proportion
            .background(color = c.surface2)
            .padding(d.spaceMd)
            .background(color = c.accent.copy(alpha = 0.12f))
    )
}
