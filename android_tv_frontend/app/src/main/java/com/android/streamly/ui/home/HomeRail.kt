package com.android.streamly.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.android.streamly.model.home.RailSection
import com.android.streamly.ui.theme.StreamlyTheme

/**
 * PUBLIC_INTERFACE
 * HomeRail
 * Placeholder for a content rail. Displays the section title and a row of cards.
 *
 * Parameters:
 * - section: the rail section data
 */
@Composable
fun HomeRail(
    section: RailSection
) {
    val d = StreamlyTheme.dimens
    val t = StreamlyTheme.typography
    val c = StreamlyTheme.colors

    Column(
        verticalArrangement = Arrangement.spacedBy(d.spaceMd)
    ) {
        BasicText(
            text = section.title,
            style = t.titleL.merge(
                TextStyle(
                    color = c.textPrimary,
                    textAlign = TextAlign.Start
                )
            )
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(d.spaceLg)
        ) {
            section.items.forEach { card ->
                HomeCard(card = card)
            }
        }
    }
}
