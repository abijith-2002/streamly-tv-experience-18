package com.android.streamly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * PUBLIC_INTERFACE
 * HomeFragment - Native Android TV Home Page
 * 
 * Implements the Figma design pixel-for-pixel:
 * - Header with logo, navigation bar (Inicio, Películas, Series, TV en vivo, Kids, Mis Contenidos)
 * - Hero banner section with large highlight image
 * - "Seguí viendo" content rail with movie cards and progress indicators
 * - "Canales de TV" rail with TV channel cards, play buttons, and live badges
 * 
 * Full DPAD navigation support for TV remote control.
 * Colors, typography, and spacing match CSS design system from common.css.
 */
class HomeFragment : Fragment() {

    private lateinit var navRecyclerView: RecyclerView
    private lateinit var continueWatchingRecyclerView: RecyclerView
    private lateinit var tvChannelsRecyclerView: RecyclerView
    
    private var currentNavIndex = 0
    private val navItems = listOf("Inicio", "Películas", "Series", "TV en vivo", "Kids", "Mis Contenidos")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupNavigationBar(view)
        setupContinueWatchingRail(view)
        setupTvChannelsRail(view)
        
        // Set initial focus to navigation bar
        view.post {
            navRecyclerView.requestFocus()
        }
    }

    /**
     * Setup horizontal navigation bar with nav items
     * Position: x:356, y:49 from design (mapped to dp)
     * Background: #28292f, radius: 34dp
     * Typography: Roboto 29sp for nav items
     */
    private fun setupNavigationBar(view: View) {
        navRecyclerView = view.findViewById(R.id.nav_recycler_view)
        navRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        
        val adapter = NavigationAdapter(navItems, currentNavIndex) { position ->
            currentNavIndex = position
            // Navigation item clicked - update active state
            navRecyclerView.adapter?.notifyDataSetChanged()
        }
        navRecyclerView.adapter = adapter
        
        // Enable DPAD navigation
        navRecyclerView.isFocusable = true
        navRecyclerView.isFocusableInTouchMode = true
    }

    /**
     * Setup "Seguí viendo" horizontal content rail
     * Position: x:48, y:645 (title), content rail starts at y:684
     * Card dimensions: First card 474x329dp, others 412x312dp
     * Gap between cards: 40dp
     */
    private fun setupContinueWatchingRail(view: View) {
        continueWatchingRecyclerView = view.findViewById(R.id.continue_watching_recycler)
        continueWatchingRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        
        val movies = listOf(
            MovieCard("Rogue One", R.drawable.figma_image_1_41, 40),
            MovieCard("Ex Machina", R.drawable.figma_image_1_68, 40),
            MovieCard("Sing Street", R.drawable.figma_image_1_85, 40),
            MovieCard("2012", R.drawable.figma_image_1_102, 40),
            MovieCard("Ad Astra", R.drawable.figma_image_1_119, 40)
        )
        
        val adapter = MovieCardAdapter(movies)
        continueWatchingRecyclerView.adapter = adapter
        
        // Enable DPAD navigation
        continueWatchingRecyclerView.isFocusable = true
        continueWatchingRecyclerView.isFocusableInTouchMode = true
    }

    /**
     * Setup "Canales de TV" horizontal rail
     * Position: x:48, y:1053 (title), cards start at y:1109
     * Card dimensions: 745x212dp
     * Live badge: #eb0045, radius: 3.81dp
     */
    private fun setupTvChannelsRail(view: View) {
        tvChannelsRecyclerView = view.findViewById(R.id.tv_channels_recycler)
        tvChannelsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        
        val channels = listOf(
            TvChannelCard("Marca Claro Radio", "004 | Claro sports", "11:30 - 12:30", R.drawable.figma_image_1_154, true, 38),
            TvChannelCard("E.T.", "005 | HBO Channel", "11:30 - 12:30", R.drawable.figma_image_1_180, true, 38, hasRentalTag = true),
            TvChannelCard("Marca Claro Radio", "004 | Claro sports", "11:30 - 12:30", R.drawable.figma_image_1_218, true, 38)
        )
        
        val adapter = TvChannelCardAdapter(channels)
        tvChannelsRecyclerView.adapter = adapter
        
        // Enable DPAD navigation
        tvChannelsRecyclerView.isFocusable = true
        tvChannelsRecyclerView.isFocusableInTouchMode = true
    }
}

/**
 * Data class for movie card in "Seguí viendo" rail
 */
data class MovieCard(
    val title: String,
    val imageRes: Int,
    val progressPercent: Int
)

/**
 * Data class for TV channel card in "Canales de TV" rail
 */
data class TvChannelCard(
    val title: String,
    val channel: String,
    val time: String,
    val imageRes: Int,
    val isLive: Boolean,
    val progressPercent: Int,
    val hasRentalTag: Boolean = false
)
