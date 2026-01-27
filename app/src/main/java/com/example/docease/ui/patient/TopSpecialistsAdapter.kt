package com.example.docease.ui.patient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

/**
 * Adapter for Top Specialists / Doctors vertical list
 */
class TopSpecialistsAdapter(
    private val specialists: List<SpecialistItem>,
    private val onSpecialistClick: (SpecialistItem) -> Unit,
    private val onBookClick: (SpecialistItem) -> Unit
) : RecyclerView.Adapter<TopSpecialistsAdapter.SpecialistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecialistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_top_specialist, parent, false)
        return SpecialistViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpecialistViewHolder, position: Int) {
        holder.bind(specialists[position])
    }

    override fun getItemCount(): Int = specialists.size

    inner class SpecialistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.specialistCard)
        private val avatar: ImageView = itemView.findViewById(R.id.doctorAvatar)
        private val name: TextView = itemView.findViewById(R.id.doctorName)
        private val specialtyDistance: TextView = itemView.findViewById(R.id.specialtyDistance)
        private val ratingBadge: LinearLayout = itemView.findViewById(R.id.ratingBadge)
        private val ratingText: TextView = itemView.findViewById(R.id.ratingText)
        private val reviewsBadge: LinearLayout = itemView.findViewById(R.id.reviewsBadge)
        private val reviewsText: TextView = itemView.findViewById(R.id.reviewsText)
        private val bookButton: MaterialButton = itemView.findViewById(R.id.bookButton)

        fun bind(specialist: SpecialistItem) {
            // Set avatar (if available, otherwise use placeholder)
            specialist.avatarRes?.let { avatar.setImageResource(it) }
                ?: avatar.setImageResource(R.drawable.ic_avatar_placeholder)

            // Set doctor info
            name.text = specialist.name
            specialtyDistance.text = "${specialist.specialty} • ${specialist.distance}"

            // Set rating
            ratingText.text = specialist.rating.toString()

            // Set reviews count
            reviewsText.text = specialist.reviewCount.toString()

            // Click listeners
            card.setOnClickListener {
                onSpecialistClick(specialist)
            }

            bookButton.setOnClickListener {
                onBookClick(specialist)
            }
        }
    }
}

/**
 * Data class for Specialist/Doctor items
 */
data class SpecialistItem(
    val id: String,
    val name: String,
    val specialty: String,
    val distance: String,
    val rating: Float,
    val reviewCount: Int,
    val avatarRes: Int? = null,
    val avatarUrl: String? = null
)
