package com.example.docease.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R

/**
 * Adapter for displaying past appointments in the Patient Profile screen
 */
class PastAppointmentAdapter(
    private val appointments: List<PastAppointment>,
    private val onItemClick: (PastAppointment) -> Unit
) : RecyclerView.Adapter<PastAppointmentAdapter.PastAppointmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PastAppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_past_appointment, parent, false)
        return PastAppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PastAppointmentViewHolder, position: Int) {
        holder.bind(appointments[position])
    }

    override fun getItemCount(): Int = appointments.size

    inner class PastAppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivDoctorAvatar: ImageView = itemView.findViewById(R.id.ivDoctorAvatar)
        private val tvDoctorName: TextView = itemView.findViewById(R.id.tvDoctorName)
        private val tvSpecialtyDate: TextView = itemView.findViewById(R.id.tvSpecialtyDate)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(appointment: PastAppointment) {
            tvDoctorName.text = appointment.doctorName
            tvSpecialtyDate.text = "${appointment.specialty} • ${appointment.date}"
            tvStatus.text = appointment.status.displayName

            // Set status chip appearance
            when (appointment.status) {
                AppointmentStatus.COMPLETED -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_chip_completed)
                    tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_done))
                }
                AppointmentStatus.CANCELLED -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_chip_cancelled)
                    tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_cancelled))
                }
            }

            // Set doctor avatar if available
            appointment.doctorImageRes?.let {
                ivDoctorAvatar.setImageResource(it)
            }

            itemView.setOnClickListener {
                onItemClick(appointment)
            }
        }
    }
}

/**
 * Data class representing a past appointment
 */
data class PastAppointment(
    val id: String,
    val doctorName: String,
    val specialty: String,
    val date: String,
    val status: AppointmentStatus,
    val doctorImageRes: Int? = null
)

/**
 * Enum for appointment status
 */
enum class AppointmentStatus(val displayName: String) {
    COMPLETED("Completed"),
    CANCELLED("Canceled")
}
