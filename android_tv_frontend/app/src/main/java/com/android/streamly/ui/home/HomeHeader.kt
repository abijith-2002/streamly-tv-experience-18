package com.android.streamly.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.streamly.model.home.NavItem
import com.android.streamly.ui.theme.StreamlyTheme

/**
 * PUBLIC_INTERFACE
 * HomeHeader
 * Placeholder for the top navigation header. Uses typography tokens for "nav" and "navActive".
 *
 * Parameters:
 * - items: list of navigation entries
 * - activeIndex: index of the active entry
 */
@Composable
fun HomeHeader(
    items: List<NavItem>,
    activeIndex: Int
) {
    val t = StreamlyTheme.typography
    val c = StreamlyTheme.colors
    val d = StreamlyTheme.dimens

    Row(
        horizontalArrangement = Arrangement.spacedBy(d.spaceLg)
    ) {
        items.forEachIndexed { index, item ->
            val active = index == activeIndex || item.active
            BasicText(
                text = item.title,
                style = (if (active) t.navActive else t.nav).merge(
                    TextStyle(
                        color = if (active) c.textPrimary else c.textSecondary,
                        textAlign = TextAlign.Start
                    )
                )
            )
        }
    }

    // Spacer that can represent the header background capsule height in future
    Spacer(modifier = Modifier.height(8.dp))
    Spacer(modifier = Modifier.width(1.dp))
}
