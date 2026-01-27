package com.example.docease.ui.booking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R

/**
 * Adapter for horizontal date chip selection in Book Appointment screen
 */
class BookingDateChipAdapter(
    private val dateList: List<BookingDateItem>,
    private val onItemClick: (BookingDateItem, Int) -> Unit
) : RecyclerView.Adapter<BookingDateChipAdapter.DateChipViewHolder>() {

    inner class DateChipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateChipContent: LinearLayout = itemView.findViewById(R.id.dateChipContent)
        val tvDayLabel: TextView = itemView.findViewById(R.id.tvDayLabel)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateChipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking_date_chip, parent, false)
        return DateChipViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateChipViewHolder, position: Int) {
        val item = dateList[position]
        val context = holder.itemView.context

        holder.tvDayLabel.text = item.dayLabel
        holder.tvDayNumber.text = item.dayNumber

        when {
            item.isSelected -> {
                // Selected state - teal background, white text
                holder.dateChipContent.setBackgroundResource(R.drawable.bg_date_chip_selected)
                holder.tvDayLabel.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.itemView.alpha = 1f
            }
            !item.isAvailable -> {
                // Unavailable state
                holder.dateChipContent.setBackgroundResource(R.drawable.bg_date_chip_unselected)
                holder.tvDayLabel.setTextColor(ContextCompat.getColor(context, R.color.docease_text_hint))
                holder.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.docease_text_hint))
                holder.itemView.alpha = 0.5f
            }
            else -> {
                // Default available state
                holder.dateChipContent.setBackgroundResource(R.drawable.bg_date_chip_unselected)
                holder.tvDayLabel.setTextColor(ContextCompat.getColor(context, R.color.docease_text_secondary))
                holder.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.docease_text_primary))
                holder.itemView.alpha = 1f
            }
        }

        holder.itemView.isClickable = item.isAvailable
        holder.itemView.isFocusable = item.isAvailable
    }

    override fun getItemCount(): Int = dateList.size
}
