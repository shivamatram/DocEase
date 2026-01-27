package com.example.docease.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R
import com.google.android.material.card.MaterialCardView

/**
 * Adapter for displaying patient notes in a RecyclerView
 */
class PatientNoteAdapter(
    private val onNoteClick: (PatientNoteItem) -> Unit
) : ListAdapter<PatientNoteItem, PatientNoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val noteCard: MaterialCardView = itemView.findViewById(R.id.noteCard)
        private val iconContainer: FrameLayout = itemView.findViewById(R.id.iconContainer)
        private val ivNoteIcon: ImageView = itemView.findViewById(R.id.ivNoteIcon)
        private val tvNoteTitle: TextView = itemView.findViewById(R.id.tvNoteTitle)
        private val tvDoctorName: TextView = itemView.findViewById(R.id.tvDoctorName)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvNoteDescription: TextView = itemView.findViewById(R.id.tvNoteDescription)

        fun bind(note: PatientNoteItem) {
            tvNoteTitle.text = note.title
            tvDoctorName.text = note.doctorName
            tvDate.text = note.dateLabel
            tvNoteDescription.text = note.description

            // Set icon and background based on note type
            when (note.type) {
                NoteType.MIGRAINE -> {
                    iconContainer.setBackgroundResource(R.drawable.bg_note_icon_orange)
                    ivNoteIcon.setImageResource(R.drawable.ic_note_migraine)
                }
                NoteType.BLOOD_WORK -> {
                    iconContainer.setBackgroundResource(R.drawable.bg_note_icon_pink)
                    ivNoteIcon.setImageResource(R.drawable.ic_note_blood)
                }
                NoteType.VACCINATION -> {
                    iconContainer.setBackgroundResource(R.drawable.bg_note_icon_green)
                    ivNoteIcon.setImageResource(R.drawable.ic_note_vaccine)
                }
                NoteType.GENERAL -> {
                    iconContainer.setBackgroundResource(R.drawable.bg_note_icon_orange)
                    ivNoteIcon.setImageResource(R.drawable.ic_notes)
                }
                NoteType.PRESCRIPTION -> {
                    iconContainer.setBackgroundResource(R.drawable.bg_note_icon_pink)
                    ivNoteIcon.setImageResource(R.drawable.ic_notes)
                }
            }

            noteCard.setOnClickListener {
                onNoteClick(note)
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<PatientNoteItem>() {
        override fun areItemsTheSame(oldItem: PatientNoteItem, newItem: PatientNoteItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PatientNoteItem, newItem: PatientNoteItem): Boolean {
            return oldItem == newItem
        }
    }
}

/**
 * Data class representing a patient note item
 */
data class PatientNoteItem(
    val id: String,
    val title: String,
    val doctorName: String,
    val description: String,
    val dateLabel: String,
    val timestamp: Long,
    val type: NoteType,
    val patientId: String? = null
)

/**
 * Enum representing different types of medical notes
 */
enum class NoteType {
    MIGRAINE,
    BLOOD_WORK,
    VACCINATION,
    GENERAL,
    PRESCRIPTION
}

/**
 * Enum for patient detail tabs
 */
enum class PatientDetailsTab {
    MEDICAL_NOTES,
    HISTORY,
    PRESCRIPTIONS
}
