package com.example.docease.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R
import com.google.android.material.card.MaterialCardView

/**
 * Adapter for displaying appointment cards in the Doctor Appointments screen
 */
class AppointmentCardAdapter(
    private val onAppointmentClick: (AppointmentCardItem) -> Unit,
    private val onCallClick: (AppointmentCardItem) -> Unit,
    private val onVideoClick: (AppointmentCardItem) -> Unit,
    private val onNotesClick: (AppointmentCardItem) -> Unit
) : ListAdapter<AppointmentCardItem, AppointmentCardAdapter.AppointmentViewHolder>(AppointmentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment_card, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appointmentCard: MaterialCardView = itemView.findViewById(R.id.appointmentCard)
        private val ivPatientAvatar: ImageView = itemView.findViewById(R.id.ivPatientAvatar)
        private val tvPatientName: TextView = itemView.findViewById(R.id.tvPatientName)
        private val tvPatientDetails: TextView = itemView.findViewById(R.id.tvPatientDetails)
        private val statusChip: TextView = itemView.findViewById(R.id.statusChip)
        private val tvReason: TextView = itemView.findViewById(R.id.tvReason)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val btnCall: FrameLayout = itemView.findViewById(R.id.btnCall)
        private val btnVideo: FrameLayout = itemView.findViewById(R.id.btnVideo)
        private val btnNotes: FrameLayout = itemView.findViewById(R.id.btnNotes)
        private val actionsContainer: LinearLayout = itemView.findViewById(R.id.actionsContainer)

        fun bind(item: AppointmentCardItem) {
            val context = itemView.context

            // Set patient info
            tvPatientName.text = item.patientName
            tvPatientDetails.text = "${item.gender}, ${item.age}"
            tvReason.text = item.reason
            tvTime.text = item.timeRange

            // Set avatar (placeholder for now)
            // In production, use Glide/Coil to load actual image
            // Glide.with(context).load(item.avatarUrl).into(ivPatientAvatar)

            // Configure status chip
            configureStatusChip(item.status)

            // Configure action buttons based on status
            configureActionButtons(item.status)

            // Click listeners
            appointmentCard.setOnClickListener { onAppointmentClick(item) }
            btnCall.setOnClickListener { onCallClick(item) }
            btnVideo.setOnClickListener { onVideoClick(item) }
            btnNotes.setOnClickListener { onNotesClick(item) }
        }

        private fun configureStatusChip(status: AppointmentCardStatus) {
            val context = itemView.context
            
            when (status) {
                AppointmentCardStatus.CONFIRMED -> {
                    statusChip.text = "Confirmed"
                    statusChip.setBackgroundResource(R.drawable.bg_status_confirmed)
                    statusChip.setTextColor(ContextCompat.getColor(context, R.color.docease_primary))
                }
                AppointmentCardStatus.PENDING -> {
                    statusChip.text = "Pending"
                    statusChip.setBackgroundResource(R.drawable.bg_status_pending)
                    statusChip.setTextColor(ContextCompat.getColor(context, R.color.status_pending))
                }
                AppointmentCardStatus.COMPLETED -> {
                    statusChip.text = "Completed"
                    statusChip.setBackgroundResource(R.drawable.bg_status_completed)
                    statusChip.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                }
                AppointmentCardStatus.CANCELLED -> {
                    statusChip.text = "Cancelled"
                    statusChip.setBackgroundResource(R.drawable.bg_status_cancelled)
                    statusChip.setTextColor(ContextCompat.getColor(context, R.color.status_cancelled))
                }
                AppointmentCardStatus.NO_SHOW -> {
                    statusChip.text = "No Show"
                    statusChip.setBackgroundResource(R.drawable.bg_status_cancelled)
                    statusChip.setTextColor(ContextCompat.getColor(context, R.color.status_cancelled))
                }
            }
        }

        private fun configureActionButtons(status: AppointmentCardStatus) {
            when (status) {
                AppointmentCardStatus.CONFIRMED -> {
                    // Show all action buttons with teal styling
                    btnCall.visibility = View.VISIBLE
                    btnVideo.visibility = View.VISIBLE
                    btnNotes.visibility = View.VISIBLE
                    btnCall.setBackgroundResource(R.drawable.bg_action_button_teal)
                    btnVideo.setBackgroundResource(R.drawable.bg_action_button_teal)
                }
                AppointmentCardStatus.PENDING -> {
                    // Show call and notes, hide video
                    btnCall.visibility = View.VISIBLE
                    btnVideo.visibility = View.GONE
                    btnNotes.visibility = View.VISIBLE
                    btnCall.setBackgroundResource(R.drawable.bg_action_button_teal)
                }
                AppointmentCardStatus.COMPLETED -> {
                    // Show only notes
                    btnCall.visibility = View.GONE
                    btnVideo.visibility = View.GONE
                    btnNotes.visibility = View.VISIBLE
                }
                AppointmentCardStatus.CANCELLED, AppointmentCardStatus.NO_SHOW -> {
                    // Show only notes for reference
                    btnCall.visibility = View.GONE
                    btnVideo.visibility = View.GONE
                    btnNotes.visibility = View.VISIBLE
                }
            }
        }
    }

    class AppointmentDiffCallback : DiffUtil.ItemCallback<AppointmentCardItem>() {
        override fun areItemsTheSame(oldItem: AppointmentCardItem, newItem: AppointmentCardItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AppointmentCardItem, newItem: AppointmentCardItem): Boolean {
            return oldItem == newItem
        }
    }
}

/**
 * Data class representing an appointment card item
 */
data class AppointmentCardItem(
    val id: String,
    val patientId: String,
    val patientName: String,
    val gender: String,
    val age: Int,
    val reason: String,
    val timeRange: String,
    val startTime: Long,
    val endTime: Long,
    val date: String, // yyyy-MM-dd format
    val status: AppointmentCardStatus,
    val avatarUrl: String? = null,
    val phoneNumber: String? = null,
    val notes: String? = null,
    val isVideoConsultation: Boolean = false
)

/**
 * Enum representing appointment statuses
 */
enum class AppointmentCardStatus {
    CONFIRMED,
    PENDING,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}

/**
 * Enum for filtering appointments by tab
 */
enum class AppointmentTab {
    TODAY,
    UPCOMING,
    PAST
}
