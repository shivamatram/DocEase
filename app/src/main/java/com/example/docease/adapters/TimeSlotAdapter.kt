package com.example.docease.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R

/**
 * Adapter for time slots in the availability screen
 * Handles different slot states: available, selected, booked
 */
class TimeSlotAdapter(
    private val onSlotClicked: (TimeSlot) -> Unit
) : ListAdapter<TimeSlot, TimeSlotAdapter.SlotViewHolder>(SlotDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val slotContainer: View = itemView.findViewById(R.id.slotContainer)
        private val slotBackground: LinearLayout = (slotContainer as ViewGroup).getChildAt(0) as LinearLayout
        private val tvSlotTime: TextView = itemView.findViewById(R.id.tvSlotTime)
        private val ivLock: ImageView = itemView.findViewById(R.id.ivLock)

        fun bind(slot: TimeSlot) {
            val context = itemView.context
            tvSlotTime.text = slot.time

            when (slot.status) {
                SlotStatus.AVAILABLE -> {
                    slotBackground.setBackgroundResource(R.drawable.bg_slot_available)
                    tvSlotTime.setTextColor(ContextCompat.getColor(context, R.color.docease_primary))
                    ivLock.visibility = View.GONE
                    slotContainer.isEnabled = true
                    slotContainer.alpha = 1f
                }
                SlotStatus.SELECTED -> {
                    slotBackground.setBackgroundResource(R.drawable.bg_slot_selected)
                    tvSlotTime.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    ivLock.visibility = View.GONE
                    slotContainer.isEnabled = true
                    slotContainer.alpha = 1f
                }
                SlotStatus.BOOKED -> {
                    slotBackground.setBackgroundResource(R.drawable.bg_slot_booked)
                    tvSlotTime.setTextColor(ContextCompat.getColor(context, R.color.text_hint))
                    ivLock.visibility = View.VISIBLE
                    slotContainer.isEnabled = false
                    slotContainer.alpha = 0.7f
                }
                SlotStatus.BREAK -> {
                    slotBackground.setBackgroundResource(R.drawable.bg_break_indicator)
                    tvSlotTime.setTextColor(ContextCompat.getColor(context, R.color.break_color))
                    ivLock.visibility = View.GONE
                    slotContainer.isEnabled = false
                    slotContainer.alpha = 1f
                }
            }

            slotContainer.setOnClickListener {
                if (slot.status != SlotStatus.BOOKED && slot.status != SlotStatus.BREAK) {
                    onSlotClicked(slot)
                }
            }
        }
    }

    class SlotDiffCallback : DiffUtil.ItemCallback<TimeSlot>() {
        override fun areItemsTheSame(oldItem: TimeSlot, newItem: TimeSlot): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TimeSlot, newItem: TimeSlot): Boolean {
            return oldItem == newItem
        }
    }
}

/**
 * Data class representing a single time slot
 */
data class TimeSlot(
    val id: String,
    val time: String,           // Format: "HH:mm"
    val startTime: Long,        // Unix timestamp
    val endTime: Long,          // Unix timestamp
    val status: SlotStatus = SlotStatus.AVAILABLE,
    val session: Session = Session.MORNING,
    val bookedBy: String? = null // Patient ID if booked
)

/**
 * Enum representing the status of a time slot
 */
enum class SlotStatus {
    AVAILABLE,  // Slot is open for booking
    SELECTED,   // Doctor has selected this slot as available
    BOOKED,     // Patient has booked this slot
    BREAK       // Break time (e.g., coffee break)
}

/**
 * Enum representing the session type
 */
enum class Session {
    MORNING,    // 08:00 - 12:00
    AFTERNOON,  // 13:00 - 17:00
    EVENING     // 17:00 - 20:00
}
