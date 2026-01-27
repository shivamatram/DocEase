package com.example.docease.ui.doctor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R

/**
 * Adapter for horizontal date card selection in Doctor Details screen
 */
class DateCardAdapter(
    private val dateList: List<DateCardItem>,
    private val onItemClick: (DateCardItem, Int) -> Unit
) : RecyclerView.Adapter<DateCardAdapter.DateCardViewHolder>() {

    inner class DateCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateCardContent: LinearLayout = itemView.findViewById(R.id.dateCardContent)
        val tvDayOfWeek: TextView = itemView.findViewById(R.id.tvDayOfWeek)
        val tvDayNumber: TextView = itemView.findViewById(R.id.tvDayNumber)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(dateList[position], position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_date_card, parent, false)
        return DateCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateCardViewHolder, position: Int) {
        val item = dateList[position]
        val context = holder.itemView.context

        // Set text values
        holder.tvDayOfWeek.text = item.dayOfWeek
        holder.tvDayNumber.text = item.dayNumber

        // Apply styling based on state
        when {
            item.isSelected -> {
                // Selected state - teal background, white text
                holder.dateCardContent.setBackgroundResource(R.drawable.bg_date_card_selected)
                holder.tvDayOfWeek.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.itemView.alpha = 1f
            }
            !item.isAvailable -> {
                // Unavailable state - disabled appearance
                holder.dateCardContent.setBackgroundResource(R.drawable.bg_date_card_unselected)
                holder.tvDayOfWeek.setTextColor(ContextCompat.getColor(context, R.color.docease_text_hint))
                holder.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.docease_text_hint))
                holder.itemView.alpha = 0.5f
            }
            else -> {
                // Default state - white background, normal text
                holder.dateCardContent.setBackgroundResource(R.drawable.bg_date_card_unselected)
                holder.tvDayOfWeek.setTextColor(ContextCompat.getColor(context, R.color.docease_text_secondary))
                holder.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.docease_text_primary))
                holder.itemView.alpha = 1f
            }
        }

        // Enable/disable click based on availability
        holder.itemView.isClickable = item.isAvailable
        holder.itemView.isFocusable = item.isAvailable
    }

    override fun getItemCount(): Int = dateList.size
}
