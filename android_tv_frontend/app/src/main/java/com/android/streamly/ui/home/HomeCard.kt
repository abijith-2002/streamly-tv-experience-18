package com.android.streamly.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.times
import com.android.streamly.model.home.CardItem
import com.android.streamly.ui.theme.StreamlyTheme

/**
 * PUBLIC_INTERFACE
 * HomeCard
 * Placeholder for a content card entry in a rail.
 *
 * Parameters:
 * - card: data for the card
 */
@Composable
fun HomeCard(
    card: CardItem
) {
    val d = StreamlyTheme.dimens
    val t = StreamlyTheme.typography
    val c = StreamlyTheme.colors

    Box(
        modifier = Modifier
            .height(d.spaceXl * 6f) // placeholder height (uses theme spacing token)
            .aspectRatio(16f / 9f)
            .background(color = c.surface, shape = RoundedCornerShape(d.radiusSm))
            .padding(d.spaceMd)
    ) {
        BasicText(
            text = card.title,
            style = t.labelL.merge(TextStyle(color = c.onSurface))
        )
    }
}
