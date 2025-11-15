package com.android.streamly.ui.placeholder

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.streamly.ui.theme.StreamlyTheme

/**
 * PUBLIC_INTERFACE
 * PlaceholderScreen
 * Minimal placeholder container for non-Home destinations.
 *
 * Behavior:
 * - Fills the screen with the existing app background color
 * - Displays a small, unobtrusive label off-center
 * - Handles DPAD Back or DirectionUp to return to Home via onBackToHome
 *
 * Parameters:
 * - label: Simple label to show
 * - onBackToHome: Callback when Back or DPAD_UP is pressed
 */
@Composable
fun PlaceholderScreen(
    label: String,
    onBackToHome: () -> Unit
) {
    val c = StreamlyTheme.colors
    val t = StreamlyTheme.typography
    val d = StreamlyTheme.dimens

    StreamlyTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(c.background)
                .focusable()
                .onKeyEvent { keyEvent ->
                    when (keyEvent.key) {
                        Key.Back, Key.DirectionUp -> {
                            onBackToHome()
                            true
                        }
                        else -> false
                    }
                }
        ) {
            // Small off-center label, kept decorative to avoid altering Home visuals expectations.
            BasicText(
                text = "$label (placeholder)",
                modifier = Modifier
                    .offset(x = d.safeMargin, y = d.safeMargin)
                    .size(width = 320.dp, height = 56.dp)
                    .clearAndSetSemantics { /* decorative placeholder text only */ },
                style = t.bodyL.merge(
                    TextStyle(
                        color = c.textSecondary,
                        textAlign = TextAlign.Start
                    )
                )
            )
        }
    }
}

/**
 * PUBLIC_INTERFACE
 * MoviesScreenPlaceholder
 * Simple placeholder destination for "Películas".
 */
@Composable
fun MoviesScreenPlaceholder(onBackToHome: () -> Unit) {
    PlaceholderScreen(label = "Películas", onBackToHome = onBackToHome)
}

/**
 * PUBLIC_INTERFACE
 * SeriesScreenPlaceholder
 * Simple placeholder destination for "Series".
 */
@Composable
fun SeriesScreenPlaceholder(onBackToHome: () -> Unit) {
    PlaceholderScreen(label = "Series", onBackToHome = onBackToHome)
}

/**
 * PUBLIC_INTERFACE
 * LiveTvScreenPlaceholder
 * Simple placeholder destination for "TV en vivo".
 */
@Composable
fun LiveTvScreenPlaceholder(onBackToHome: () -> Unit) {
    PlaceholderScreen(label = "TV en vivo", onBackToHome = onBackToHome)
}

/**
 * PUBLIC_INTERFACE
 * KidsScreenPlaceholder
 * Simple placeholder destination for "Kids".
 */
@Composable
fun KidsScreenPlaceholder(onBackToHome: () -> Unit) {
    PlaceholderScreen(label = "Kids", onBackToHome = onBackToHome)
}

/**
 * PUBLIC_INTERFACE
 * MyContentScreenPlaceholder
 * Simple placeholder destination for "Mis Contenidos".
 */
@Composable
fun MyContentScreenPlaceholder(onBackToHome: () -> Unit) {
    PlaceholderScreen(label = "Mis Contenidos", onBackToHome = onBackToHome)
}
