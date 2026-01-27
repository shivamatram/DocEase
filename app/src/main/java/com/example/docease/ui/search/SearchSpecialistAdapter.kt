package com.example.docease.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

/**
 * Adapter for Search Specialist Cards
 */
class SearchSpecialistAdapter(
    private val specialists: MutableList<SearchSpecialistItem>,
    private val onSpecialistClick: (SearchSpecialistItem) -> Unit,
    private val onBookClick: (SearchSpecialistItem) -> Unit,
    private val onFavoriteClick: (SearchSpecialistItem, Int) -> Unit
) : RecyclerView.Adapter<SearchSpecialistAdapter.SpecialistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecialistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_specialist, parent, false)
        return SpecialistViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpecialistViewHolder, position: Int) {
        holder.bind(specialists[position])
    }

    override fun getItemCount(): Int = specialists.size

    fun updateList(newList: List<SearchSpecialistItem>) {
        specialists.clear()
        specialists.addAll(newList)
        notifyDataSetChanged()
    }

    fun updateFavorite(position: Int, isFavorite: Boolean) {
        if (position in specialists.indices) {
            specialists[position] = specialists[position].copy(isFavorite = isFavorite)
            notifyItemChanged(position)
        }
    }

    inner class SpecialistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.specialistCard)
        private val avatar: ImageView = itemView.findViewById(R.id.doctorAvatar)
        private val ratingText: TextView = itemView.findViewById(R.id.ratingText)
        private val favoriteButton: ImageView = itemView.findViewById(R.id.favoriteButton)
        private val doctorName: TextView = itemView.findViewById(R.id.doctorName)
        private val specialtyHospital: TextView = itemView.findViewById(R.id.specialtyHospital)
        private val availabilityDot: View = itemView.findViewById(R.id.availabilityDot)
        private val availabilityText: TextView = itemView.findViewById(R.id.availabilityText)
        private val nextSlotText: TextView = itemView.findViewById(R.id.nextSlotText)
        private val bookNowButton: MaterialButton = itemView.findViewById(R.id.bookNowButton)

        fun bind(specialist: SearchSpecialistItem) {
            // Set avatar
            specialist.avatarRes?.let { avatar.setImageResource(it) }
                ?: avatar.setImageResource(R.drawable.ic_avatar_placeholder)

            // Set rating
            ratingText.text = specialist.rating.toString()

            // Set favorite state
            updateFavoriteIcon(specialist.isFavorite)

            // Set doctor info
            doctorName.text = specialist.name
            specialtyHospital.text = "${specialist.specialty} • ${specialist.hospital}"

            // Set availability
            when (specialist.availability) {
                AvailabilityStatus.TODAY -> {
                    availabilityDot.setBackgroundResource(R.drawable.bg_availability_available)
                    availabilityText.text = "Available Today"
                    availabilityText.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_done))
                }
                AvailabilityStatus.TOMORROW -> {
                    availabilityDot.setBackgroundResource(R.drawable.bg_availability_tomorrow)
                    availabilityText.text = "Tomorrow"
                    availabilityText.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_confirmed))
                }
                AvailabilityStatus.NEXT_WEEK -> {
                    availabilityDot.setBackgroundResource(R.drawable.bg_availability_later)
                    availabilityText.text = "Next Week"
                    availabilityText.setTextColor(ContextCompat.getColor(itemView.context, R.color.docease_text_secondary))
                }
            }

            // Set next slot
            nextSlotText.text = "Next slot: ${specialist.nextSlot}"

            // Click listeners
            card.setOnClickListener {
                onSpecialistClick(specialist)
            }

            bookNowButton.setOnClickListener {
                onBookClick(specialist)
            }

            favoriteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onFavoriteClick(specialist, position)
                }
            }
        }

        private fun updateFavoriteIcon(isFavorite: Boolean) {
            if (isFavorite) {
                favoriteButton.setImageResource(R.drawable.ic_heart_filled)
            } else {
                favoriteButton.setImageResource(R.drawable.ic_heart_outline)
            }
        }
    }
}

/**
 * Availability status enum
 */
enum class AvailabilityStatus {
    TODAY,
    TOMORROW,
    NEXT_WEEK
}

/**
 * Data class for Search Specialist items
 */
data class SearchSpecialistItem(
    val id: String,
    val name: String,
    val specialty: String,
    val hospital: String,
    val rating: Float,
    val availability: AvailabilityStatus,
    val nextSlot: String,
    val isFavorite: Boolean = false,
    val avatarRes: Int? = null,
    val avatarUrl: String? = null
)
