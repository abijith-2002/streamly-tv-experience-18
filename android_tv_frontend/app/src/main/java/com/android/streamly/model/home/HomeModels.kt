package com.android.streamly.model.home

/**
 * Data models for the Home screen scaffolding. These map one-to-one with the composable placeholders.
 */

/**
 * Represents a top navigation item (e.g., "Inicio", "Películas").
 */
data class NavItem(
    val id: String,
    val title: String,
    val active: Boolean = false
)

/**
 * Represents a progress indicator for a card (e.g., continue watching).
 */
data class Progress(
    val current: Int,
    val total: Int
) {
    val ratio: Float = if (total > 0) current.toFloat() / total else 0f
}

/**
 * Represents a single content card in a rail.
 */
data class CardItem(
    val id: String,
    val title: String,
    val imageUrl: String? = null,
    val progress: Progress? = null
)

/**
 * Represents the large hero item on top of Home.
 */
data class HeroItem(
    val id: String,
    val title: String,
    val imageUrl: String? = null
)

/**
 * Represents a horizontally scrollable rail section with a title.
 */
data class RailSection(
    val id: String,
    val title: String,
    val items: List<CardItem>
)
