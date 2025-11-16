package com.android.streamly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * PUBLIC_INTERFACE
 * NavigationAdapter - Horizontal navigation bar adapter
 * 
 * Displays navigation items (Inicio, Películas, etc.) with:
 * - Inactive state: Roboto 29sp Regular, color #7f8282 (typo_28)
 * - Active state: Roboto 29sp Bold, color #ffffff, background #9b0f0f, radius 37dp (typo_29)
 * 
 * Handles DPAD left/right navigation and focus states.
 */
class NavigationAdapter(
    private val items: List<String>,
    private var activeIndex: Int,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<NavigationAdapter.NavViewHolder>() {

    inner class NavViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: CardView = view.findViewById(R.id.nav_item_container)
        val textView: TextView = view.findViewById(R.id.nav_item_text)
        
        init {
            view.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    activeIndex = position
                    onItemClick(position)
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_navigation, parent, false)
        return NavViewHolder(view)
    }

    override fun onBindViewHolder(holder: NavViewHolder, position: Int) {
        val item = items[position]
        val isActive = position == activeIndex
        
        holder.textView.text = item
        
        // Apply design system styles based on active state
        if (isActive) {
            // Active state: typo_29 - Roboto 29sp Bold, #ffffff, background #9b0f0f
            holder.container.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.nav_active_bg)
            )
            holder.container.radius = 37f
            holder.textView.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.tv_primary)
            )
            holder.textView.textSize = 29f
            holder.textView.setTypeface(null, android.graphics.Typeface.BOLD)
        } else {
            // Inactive state: typo_28 - Roboto 29sp Regular, #7f8282, transparent
            holder.container.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.transparent)
            )
            holder.container.radius = 0f
            holder.textView.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.nav_inactive)
            )
            holder.textView.textSize = 29f
            holder.textView.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
        
        // Set focusable for DPAD navigation
        holder.itemView.isFocusable = true
        holder.itemView.isFocusableInTouchMode = true
    }

    override fun getItemCount(): Int = items.size
}
