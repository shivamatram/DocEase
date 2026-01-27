package com.example.docease.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R

/**
 * Adapter for the calendar grid in DoctorAvailabilityActivity
 * Handles day selection and displays availability indicators
 */
class CalendarDayAdapter(
    private val onDaySelected: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder>() {

    private val days = mutableListOf<CalendarDay>()
    private var selectedPosition = -1

    fun submitList(newDays: List<CalendarDay>) {
        days.clear()
        days.addAll(newDays)
        // Find and set selected position
        selectedPosition = days.indexOfFirst { it.isSelected }
        notifyDataSetChanged()
    }

    fun getSelectedDay(): CalendarDay? {
        return if (selectedPosition >= 0 && selectedPosition < days.size) {
            days[selectedPosition]
        } else null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(days[position], position)
    }

    override fun getItemCount(): Int = days.size

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDayNumber: TextView = itemView.findViewById(R.id.tvDayNumber)
        private val indicatorDot: View = itemView.findViewById(R.id.indicatorDot)
        private val dayContainer: View = itemView.findViewById(R.id.dayContainer)

        fun bind(day: CalendarDay, position: Int) {
            val context = itemView.context

            if (day.dayNumber == 0) {
                // Empty cell (padding for first week)
                tvDayNumber.text = ""
                tvDayNumber.isSelected = false
                indicatorDot.visibility = View.GONE
                dayContainer.isClickable = false
                dayContainer.isFocusable = false
                return
            }

            tvDayNumber.text = day.dayNumber.toString()
            dayContainer.isClickable = true
            dayContainer.isFocusable = true

            // Handle selection state
            tvDayNumber.isSelected = day.isSelected
            if (day.isSelected) {
                tvDayNumber.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            } else if (day.isToday) {
                tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.docease_primary))
            } else if (!day.isCurrentMonth) {
                tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.text_hint))
            } else {
                tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            }

            // Handle indicator dot
            when (day.status) {
                DayStatus.AVAILABLE -> {
                    indicatorDot.visibility = View.VISIBLE
                    indicatorDot.setBackgroundResource(R.drawable.bg_day_available_dot)
                }
                DayStatus.BOOKED -> {
                    indicatorDot.visibility = View.VISIBLE
                    indicatorDot.setBackgroundResource(R.drawable.bg_day_booked_dot)
                }
                DayStatus.NONE -> {
                    indicatorDot.visibility = View.GONE
                }
            }

            dayContainer.setOnClickListener {
                if (day.dayNumber > 0) {
                    val previousSelected = selectedPosition
                    selectedPosition = position
                    
                    // Update previous selected item
                    if (previousSelected >= 0 && previousSelected < days.size) {
                        days[previousSelected] = days[previousSelected].copy(isSelected = false)
                        notifyItemChanged(previousSelected)
                    }
                    
                    // Update current selected item
                    days[position] = days[position].copy(isSelected = true)
                    notifyItemChanged(position)
                    
                    onDaySelected(days[position])
                }
            }
        }
    }
}

/**
 * Data class representing a single day in the calendar
 */
data class CalendarDay(
    val dayNumber: Int,
    val date: String, // Format: yyyy-MM-dd
    val isSelected: Boolean = false,
    val isToday: Boolean = false,
    val isCurrentMonth: Boolean = true,
    val status: DayStatus = DayStatus.NONE
)

/**
 * Enum representing the status of a calendar day
 */
enum class DayStatus {
    NONE,       // No availability set
    AVAILABLE,  // Has available slots
    BOOKED      // All slots booked
}
