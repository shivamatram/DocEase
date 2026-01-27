package com.example.docease.ui.booking

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R

/**
 * Adapter for time slot chip grid in Book Appointment screen
 */
class BookingTimeChipAdapter(
    private val timeList: List<BookingTimeItem>,
    private val onItemClick: (BookingTimeItem, Int) -> Unit
) : RecyclerView.Adapter<BookingTimeChipAdapter.TimeChipViewHolder>() {

    inner class TimeChipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTimeSlot: TextView = itemView.findViewById(R.id.tvTimeSlot)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(timeList[position], position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeChipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking_time_chip, parent, false)
        return TimeChipViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeChipViewHolder, position: Int) {
        val item = timeList[position]
        val context = holder.itemView.context

        holder.tvTimeSlot.text = item.time

        when {
            item.isSelected -> {
                // Selected state - teal outline with light background
                holder.tvTimeSlot.setBackgroundResource(R.drawable.bg_time_chip_selected)
                holder.tvTimeSlot.setTextColor(ContextCompat.getColor(context, R.color.docease_primary))
                holder.tvTimeSlot.paintFlags = holder.tvTimeSlot.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                holder.itemView.alpha = 1f
            }
            !item.isAvailable -> {
                // Disabled/booked state - gray with strikethrough
                holder.tvTimeSlot.setBackgroundResource(R.drawable.bg_time_chip_disabled)
                holder.tvTimeSlot.setTextColor(ContextCompat.getColor(context, R.color.docease_text_hint))
                holder.tvTimeSlot.paintFlags = holder.tvTimeSlot.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                holder.itemView.alpha = 0.7f
            }
            else -> {
                // Available state - white background with border
                holder.tvTimeSlot.setBackgroundResource(R.drawable.bg_time_chip_available)
                holder.tvTimeSlot.setTextColor(ContextCompat.getColor(context, R.color.docease_text_primary))
                holder.tvTimeSlot.paintFlags = holder.tvTimeSlot.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                holder.itemView.alpha = 1f
            }
        }

        holder.itemView.isClickable = item.isAvailable
        holder.itemView.isFocusable = item.isAvailable
    }

    override fun getItemCount(): Int = timeList.size
}
