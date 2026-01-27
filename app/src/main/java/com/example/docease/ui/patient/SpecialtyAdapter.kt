package com.example.docease.ui.patient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R

/**
 * Adapter for Doctor Specialty horizontal list
 */
class SpecialtyAdapter(
    private val specialties: List<SpecialtyItem>,
    private val onSpecialtyClick: (SpecialtyItem) -> Unit
) : RecyclerView.Adapter<SpecialtyAdapter.SpecialtyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecialtyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_specialty, parent, false)
        return SpecialtyViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpecialtyViewHolder, position: Int) {
        holder.bind(specialties[position])
    }

    override fun getItemCount(): Int = specialties.size

    inner class SpecialtyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconContainer: View = itemView.findViewById(R.id.specialtyIconContainer)
        private val icon: ImageView = itemView.findViewById(R.id.specialtyIcon)
        private val name: TextView = itemView.findViewById(R.id.specialtyName)

        fun bind(specialty: SpecialtyItem) {
            icon.setImageResource(specialty.iconRes)
            name.text = specialty.name

            itemView.setOnClickListener {
                onSpecialtyClick(specialty)
            }
        }
    }
}

/**
 * Data class for Specialty items
 */
data class SpecialtyItem(
    val id: String,
    val name: String,
    val iconRes: Int
)
