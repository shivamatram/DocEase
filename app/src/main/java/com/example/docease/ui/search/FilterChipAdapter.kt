package com.example.docease.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R

/**
 * Adapter for Category Filter Chips
 */
class FilterChipAdapter(
    private val chips: List<FilterChipItem>,
    private val onChipClick: (FilterChipItem, Int) -> Unit
) : RecyclerView.Adapter<FilterChipAdapter.ChipViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_filter_chip, parent, false)
        return ChipViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        holder.bind(chips[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = chips.size

    fun setSelectedPosition(position: Int) {
        val previousSelected = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousSelected)
        notifyItemChanged(selectedPosition)
    }

    inner class ChipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val chipText: TextView = itemView.findViewById(R.id.chipText)

        fun bind(chip: FilterChipItem, isSelected: Boolean) {
            chipText.text = chip.name

            if (isSelected) {
                chipText.setBackgroundResource(R.drawable.bg_chip_active)
                chipText.setTextColor(ContextCompat.getColor(itemView.context, R.color.docease_background))
            } else {
                chipText.setBackgroundResource(R.drawable.bg_chip_inactive)
                chipText.setTextColor(ContextCompat.getColor(itemView.context, R.color.docease_text_secondary))
            }

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    setSelectedPosition(position)
                    onChipClick(chip, position)
                }
            }
        }
    }
}

/**
 * Data class for Filter Chip items
 */
data class FilterChipItem(
    val id: String,
    val name: String
)
