package com.android.streamly.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.streamly.ui.theme.StreamlyTheme

/**
 * PUBLIC_INTERFACE
 * HomeScaffold
 * A lightweight Compose scaffold-like layout for the TV Home screen.
 *
 * Provides:
 * - Persistent header slot at the top
 * - Main content region that adheres to a 16:9 (1920x1080) baseline sizing
 * - Overscan-safe margins taken from theme tokens
 *
 * Parameters:
 * - modifier: Optional modifier for the root scaffold container
 * - header: Composable slot for the persistent header
 * - content: Composable slot for the main screen content
 */
@Composable
fun HomeScaffold(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val c = StreamlyTheme.colors
    val d = StreamlyTheme.dimens

    // Root viewport: fills the TV screen and applies background color
    Box(
        modifier = modifier
            .background(c.background)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(d.safeMargin)
        ) {
            // Header (persistent)
            header()

            Spacer(modifier = Modifier.height(d.spaceLg))

            // Main 16:9 baseline content box
            BoxWithConstraints {
                // Compute a 16:9 area that fits inside the available space
                val ratio = 16f / 9f
                val maxW = this.maxWidth
                val maxH = this.maxHeight

                // Desired height for 16:9 using the available width
                val desiredH = maxW / ratio
                val contentModifier = if (desiredH <= maxH) {
                    // Width-bound: full width, computed height
                    Modifier
                        .align(Alignment.TopStart)
                        .then(Modifier
                            .height(desiredH)
                        )
                } else {
                    // Height-bound: full height, width computed from height
                    val computedW = maxH * ratio
                    Modifier
                        .align(Alignment.TopStart)
                        .then(Modifier
                            .height(maxH)
                            .padding(end = (maxW - computedW))
                        )
                }

                Box(modifier = contentModifier) {
                    content()
                }
            }
        }
    }
}
