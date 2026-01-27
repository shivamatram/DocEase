package com.example.docease.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R
import com.google.android.material.card.MaterialCardView

/**
 * RecyclerView Adapter for displaying appointments list
 */
class AppointmentsAdapter(
    private val appointments: List<AppointmentItem>,
    private val onItemClick: (AppointmentItem) -> Unit
) : RecyclerView.Adapter<AppointmentsAdapter.AppointmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(appointments[position])
    }

    override fun getItemCount(): Int = appointments.size

    inner class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val appointmentCard: MaterialCardView = itemView.findViewById(R.id.appointmentCard)
        private val accentLine: View = itemView.findViewById(R.id.accentLine)
        private val patientAvatar: ImageView = itemView.findViewById(R.id.patientAvatar)
        private val patientName: TextView = itemView.findViewById(R.id.patientName)
        private val appointmentReason: TextView = itemView.findViewById(R.id.appointmentReason)
        private val appointmentTime: TextView = itemView.findViewById(R.id.appointmentTime)
        private val statusChip: TextView = itemView.findViewById(R.id.statusChip)

        fun bind(appointment: AppointmentItem) {
            // Set patient info
            patientName.text = appointment.patientName
            appointmentReason.text = appointment.reason
            appointmentTime.text = appointment.time

            // Show/hide accent line for highlighted appointment
            accentLine.visibility = if (appointment.isHighlighted) View.VISIBLE else View.GONE

            // Setup status chip
            setupStatusChip(appointment.status)

            // Load avatar (using placeholder for now)
            // In production, use Glide/Coil to load from URL
            patientAvatar.setImageResource(R.drawable.ic_avatar_placeholder)

            // Set click listener
            appointmentCard.setOnClickListener {
                onItemClick(appointment)
            }

            // Add ripple effect
            appointmentCard.isClickable = true
            appointmentCard.isFocusable = true
        }

        private fun setupStatusChip(status: AppointmentStatus) {
            val context = itemView.context

            when (status) {
                AppointmentStatus.DONE -> {
                    statusChip.text = "Done"
                    statusChip.setBackgroundResource(R.drawable.bg_chip_done)
                    statusChip.setTextColor(ContextCompat.getColor(context, R.color.status_done))
                }
                AppointmentStatus.UPCOMING -> {
                    statusChip.text = "Upcoming"
                    statusChip.setBackgroundResource(R.drawable.bg_chip_upcoming)
                    statusChip.setTextColor(ContextCompat.getColor(context, R.color.docease_primary))
                }
                AppointmentStatus.PENDING -> {
                    statusChip.text = "Pending"
                    statusChip.setBackgroundResource(R.drawable.bg_chip_pending)
                    statusChip.setTextColor(ContextCompat.getColor(context, R.color.status_pending))
                }
                AppointmentStatus.CONFIRMED -> {
                    statusChip.text = "Confirmed"
                    statusChip.setBackgroundResource(R.drawable.bg_chip_confirmed)
                    statusChip.setTextColor(ContextCompat.getColor(context, R.color.status_confirmed))
                }
            }
        }
    }
}
