package com.android.streamly

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import android.view.KeyEvent
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.android.streamly.model.home.CardItem
import com.android.streamly.model.home.HeroItem
import com.android.streamly.model.home.NavItem
import com.android.streamly.model.home.Progress
import com.android.streamly.model.home.RailSection
import com.android.streamly.ui.home.HomeScreen

/**
 * Main Activity for Android TV
 * Extends FragmentActivity for Leanback compatibility
 */
class MainActivity : FragmentActivity() {

    private lateinit var titleText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        titleText = findViewById(R.id.title_text)
        // Use the app_name string resource to present the title
        titleText.text = getString(R.string.app_name)

        // Compose integration via ComposeView to render Home scaffolding
        val composeView: ComposeView = findViewById(R.id.home_compose)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            // Minimal placeholder data for scaffolding
            val nav = listOf(
                NavItem(id = "inicio", title = "Inicio", active = true),
                NavItem(id = "peliculas", title = "Películas"),
                NavItem(id = "series", title = "Series"),
                NavItem(id = "tv", title = "TV en vivo"),
                NavItem(id = "kids", title = "Kids"),
                NavItem(id = "mis", title = "Mis Contenidos")
            )

            val hero = HeroItem(id = "h1", title = "Destacado")

            val rails = listOf(
                RailSection(
                    id = "r1",
                    title = "Seguí viendo",
                    items = listOf(
                        CardItem(id = "c1", title = "Rogue One", progress = Progress(current = 176, total = 436)),
                        CardItem(id = "c2", title = "Ex Machina", progress = Progress(current = 153, total = 379)),
                        CardItem(id = "c3", title = "Sing Street", progress = Progress(current = 153, total = 379))
                    )
                ),
                RailSection(
                    id = "r2",
                    title = "Populares",
                    items = listOf(
                        CardItem(id = "c4", title = "2012"),
                        CardItem(id = "c5", title = "Ad Astra")
                    )
                )
            )

            HomeScreen(
                nav = nav,
                activeIndex = 0,
                hero = hero,
                rails = rails
            )
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
                // Handle BACK button
                finish()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}
