package com.android.streamly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

/**
 * PUBLIC_INTERFACE
 * MovieCardAdapter - "Seguí viendo" content rail adapter
 * 
 * Displays movie cards with:
 * - Image thumbnail (474x267dp for first card, 412x232dp for others)
 * - Progress bar (#de1717 fill, #2c2c2c track, radius 9.2dp/4.6dp)
 * - Title (Roboto 30sp Medium, #ffffff - typo_23)
 * - Background #323131 for title container
 * 
 * Cards scale on focus (1.05x) and handle DPAD navigation.
 */
class MovieCardAdapter(
    private val movies: List<MovieCard>
) : RecyclerView.Adapter<MovieCardAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardContainer: CardView = view.findViewById(R.id.movie_card_container)
        val posterImage: ImageView = view.findViewById(R.id.movie_poster)
        val progressBar: ProgressBar = view.findViewById(R.id.movie_progress)
        val titleText: TextView = view.findViewById(R.id.movie_title)
        
        init {
            // Focus handling for TV
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            
            view.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    // Scale card on focus (design shows 1.05x scale on hover/focus)
                    view.animate().scaleX(1.05f).scaleY(1.05f).setDuration(200).start()
                } else {
                    view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val layoutId = if (viewType == 0) {
            // First card is larger: 474x329dp vs 412x312dp
            R.layout.item_movie_card_large
        } else {
            R.layout.item_movie_card
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        
        holder.posterImage.setImageResource(movie.imageRes)
        holder.titleText.text = movie.title
        holder.progressBar.progress = movie.progressPercent
        
        // Apply design system colors
        // Progress bar uses #de1717 (color_de1717) for fill
        // Title uses typo_23: Roboto 30sp Medium, #ffffff
        holder.titleText.textSize = 30f
        holder.titleText.setTypeface(null, android.graphics.Typeface.BOLD)
    }

    override fun getItemCount(): Int = movies.size

    override fun getItemViewType(position: Int): Int {
        // First card is type 0 (larger), rest are type 1
        return if (position == 0) 0 else 1
    }
}
