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
 * Adapter for time slot selection grid in Doctor Details booking screen
 */
class TimeSlotBookingAdapter(
    private val timeSlotList: List<TimeSlotItem>,
    private val onItemClick: (TimeSlotItem, Int) -> Unit
) : RecyclerView.Adapter<TimeSlotBookingAdapter.TimeSlotViewHolder>() {

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeSlotContainer: View = itemView.findViewById(R.id.timeSlotContainer)
        val timeSlotContent: LinearLayout = itemView.findViewById(R.id.timeSlotContent)
        val tvTimeSlot: TextView = itemView.findViewById(R.id.tvTimeSlot)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(timeSlotList[position], position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot_booking, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val item = timeSlotList[position]
        val context = holder.itemView.context

        // Set time text
        holder.tvTimeSlot.text = item.time

        // Apply styling based on state
        when {
            item.isSelected -> {
                // Selected state - teal background, white text
                holder.timeSlotContent.setBackgroundResource(R.drawable.bg_time_slot_selected)
                holder.tvTimeSlot.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.itemView.alpha = 1f
            }
            !item.isAvailable -> {
                // Unavailable/booked state - gray background, muted text
                holder.timeSlotContent.setBackgroundResource(R.drawable.bg_time_slot_disabled)
                holder.tvTimeSlot.setTextColor(ContextCompat.getColor(context, R.color.docease_text_hint))
                holder.itemView.alpha = 0.6f
            }
            else -> {
                // Available state - white background with border, teal text
                holder.timeSlotContent.setBackgroundResource(R.drawable.bg_time_slot_available)
                holder.tvTimeSlot.setTextColor(ContextCompat.getColor(context, R.color.docease_primary))
                holder.itemView.alpha = 1f
            }
        }

        // Enable/disable click based on availability
        holder.itemView.isClickable = item.isAvailable
        holder.itemView.isFocusable = item.isAvailable
    }

    override fun getItemCount(): Int = timeSlotList.size
}
