package com.android.streamly

import android.os.Bundle
import android.view.KeyEvent
import androidx.compose.runtime.mutableStateOf
import androidx.fragment.app.FragmentActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.android.streamly.model.home.CardItem
import com.android.streamly.model.home.HeroItem
import com.android.streamly.model.home.NavItem
import com.android.streamly.model.home.Progress
import com.android.streamly.model.home.RailSection
import com.android.streamly.ui.home.HomeScreen
import com.android.streamly.ui.placeholder.KidsScreenPlaceholder
import com.android.streamly.ui.placeholder.LiveTvScreenPlaceholder
import com.android.streamly.ui.placeholder.MoviesScreenPlaceholder
import com.android.streamly.ui.placeholder.MyContentScreenPlaceholder
import com.android.streamly.ui.placeholder.SeriesScreenPlaceholder

/**
 * Main Activity for Android TV
 * Extends FragmentActivity for Leanback compatibility
 */
class MainActivity : FragmentActivity() {

    // Track which top tab destination is currently active.
    // 0 = Home (Inicio), others point to placeholder destinations.
    private val selectedTabIndex = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Compose integration via ComposeView to render Home scaffolding and basic navigation
        val composeView: ComposeView = findViewById(R.id.home_compose)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            // Top navigation model
            val nav = listOf(
                NavItem(id = "inicio", title = "Inicio", active = true),
                NavItem(id = "peliculas", title = "Películas"),
                NavItem(id = "series", title = "Series"),
                NavItem(id = "tv", title = "TV en vivo"),
                NavItem(id = "kids", title = "Kids"),
                NavItem(id = "mis", title = "Mis Contenidos")
            )

            // Minimal placeholder data for Home scaffolding
            val hero = HeroItem(id = "h1", title = "Destacado", imageResId = R.drawable.hero_main)

            val rails = listOf(
                RailSection(
                    id = "r1",
                    title = "Seguí viendo",
                    items = listOf(
                        CardItem(
                            id = "c1",
                            title = "Rogue One",
                            imageResId = R.drawable.poster_rogue_one,
                            progress = Progress(current = 176, total = 436)
                        ),
                        CardItem(
                            id = "c2",
                            title = "Ex Machina",
                            imageResId = R.drawable.poster_ex_machina,
                            progress = Progress(current = 153, total = 379)
                        ),
                        CardItem(
                            id = "c3",
                            title = "Sing Street",
                            imageResId = R.drawable.poster_sing_street,
                            progress = Progress(current = 153, total = 379)
                        )
                    )
                ),
                RailSection(
                    id = "r2",
                    title = "Populares",
                    items = listOf(
                        CardItem(id = "c4", title = "2012", imageResId = R.drawable.poster_2012),
                        CardItem(id = "c5", title = "Ad Astra", imageResId = R.drawable.poster_ad_astra)
                    )
                ),
                // Sample TV Channels rail
                RailSection(
                    id = "r3",
                    title = "Canales de TV",
                    items = listOf(
                        CardItem(
                            id = "tv1",
                            title = "Marca Claro Radio",
                            imageResId = R.drawable.poster_tv1,
                            progress = Progress(current = 80, total = 207)
                        ),
                        CardItem(
                            id = "tv2",
                            title = "E.T.",
                            imageResId = R.drawable.poster_tv2a,
                            progress = Progress(current = 80, total = 207)
                        ),
                        CardItem(
                            id = "tv3",
                            title = "Marca Claro Radio",
                            imageResId = R.drawable.poster_tv3,
                            progress = Progress(current = 80, total = 207)
                        )
                    )
                )
            )

            // Start destination is Home (index 0). Header tab clicks switch selectedTabIndex
            when (selectedTabIndex.value) {
                0 -> {
                    HomeScreen(
                        nav = nav,
                        activeIndex = 0,
                        hero = hero,
                        rails = rails,
                        // Wire header tab navigation to switch to placeholder destinations
                        onTabSelected = { index, _ ->
                            selectedTabIndex.value = index
                        }
                    )
                }
                1 -> MoviesScreenPlaceholder(onBackToHome = { selectedTabIndex.value = 0 })
                2 -> SeriesScreenPlaceholder(onBackToHome = { selectedTabIndex.value = 0 })
                3 -> LiveTvScreenPlaceholder(onBackToHome = { selectedTabIndex.value = 0 })
                4 -> KidsScreenPlaceholder(onBackToHome = { selectedTabIndex.value = 0 })
                5 -> MyContentScreenPlaceholder(onBackToHome = { selectedTabIndex.value = 0 })
                else -> {
                    // Safety fallback to Home if an unexpected index appears
                    selectedTabIndex.value = 0
                    HomeScreen(
                        nav = nav,
                        activeIndex = 0,
                        hero = hero,
                        rails = rails,
                        onTabSelected = { index, _ -> selectedTabIndex.value = index }
                    )
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Handle TV remote control inputs
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER -> {
                // Handle SELECT/OK button
                true
            }
            KeyEvent.KEYCODE_BACK -> {
                // Return to Home when on any placeholder destination
                if (selectedTabIndex.value != 0) {
                    selectedTabIndex.value = 0
                    true
                } else {
                    // Already on Home, exit the app
                    finish()
                    true
                }
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}
