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
 * TvChannelCardAdapter - "Canales de TV" rail adapter
 * 
 * Displays TV channel cards with:
 * - Thumbnail image (377x212dp)
 * - Play button overlay (circular, 91.69dp diameter, #c60000 background)
 * - Live badge ("EN VIVO", #eb0045 background, radius 3.81dp)
 * - Title (Roboto 36sp Bold - typo_25)
 * - Channel info (Roboto 30sp Regular - typo_26)
 * - Time (Roboto 30sp Regular - typo_26)
 * - Progress bar (#de1717 fill, 80% opacity track)
 * 
 * Card dimensions: 745x212dp, scales to 1.02x on focus.
 */
class TvChannelCardAdapter(
    private val channels: List<TvChannelCard>
) : RecyclerView.Adapter<TvChannelCardAdapter.TvChannelViewHolder>() {

    inner class TvChannelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardContainer: CardView = view.findViewById(R.id.tv_card_container)
        val thumbnailImage: ImageView = view.findViewById(R.id.tv_thumbnail)
        val playButton: ImageView = view.findViewById(R.id.tv_play_button)
        val liveBadge: TextView = view.findViewById(R.id.tv_live_badge)
        val rentalTag: TextView = view.findViewById(R.id.tv_rental_tag)
        val titleText: TextView = view.findViewById(R.id.tv_title)
        val channelText: TextView = view.findViewById(R.id.tv_channel)
        val timeText: TextView = view.findViewById(R.id.tv_time)
        val progressBar: ProgressBar = view.findViewById(R.id.tv_progress)
        
        init {
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            
            view.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    // Scale to 1.02x on focus
                    view.animate().scaleX(1.02f).scaleY(1.02f).setDuration(200).start()
                } else {
                    view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TvChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tv_channel_card, parent, false)
        return TvChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: TvChannelViewHolder, position: Int) {
        val channel = channels[position]
        
        holder.thumbnailImage.setImageResource(channel.imageRes)
        holder.titleText.text = channel.title
        holder.channelText.text = channel.channel
        holder.timeText.text = channel.time
        holder.progressBar.progress = channel.progressPercent
        
        // Show/hide live badge
        holder.liveBadge.visibility = if (channel.isLive) View.VISIBLE else View.GONE
        
        // Show/hide rental tag
        holder.rentalTag.visibility = if (channel.hasRentalTag) View.VISIBLE else View.GONE
        
        // Apply design system typography
        // Title: typo_25 - Roboto 36sp Bold
        holder.titleText.textSize = 36f
        holder.titleText.setTypeface(null, android.graphics.Typeface.BOLD)
        
        // Channel & Time: typo_26 - Roboto 30sp Regular
        holder.channelText.textSize = 30f
        holder.timeText.textSize = 30f
        
        // Live badge: typo_27 - Roboto 21.33sp Medium
        holder.liveBadge.textSize = 21.33f
        holder.liveBadge.setTypeface(null, android.graphics.Typeface.BOLD)
    }

    override fun getItemCount(): Int = channels.size
}
