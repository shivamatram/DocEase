package com.example.docease.ui.finddoctor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView

/**
 * Adapter for Find a Doctor list with multiple view types
 * Supports Doctor items and Native Ad placeholders
 */
class FindDoctorAdapter(
    private val items: List<FindDoctorListItem>,
    private val onDoctorClick: (FindDoctorItem) -> Unit,
    private val onAdClick: (FindDoctorAd) -> Unit,
    private val onAdCtaClick: (FindDoctorAd) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_DOCTOR = 0
        private const val VIEW_TYPE_AD = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is FindDoctorListItem.DoctorItem -> VIEW_TYPE_DOCTOR
            is FindDoctorListItem.AdItem -> VIEW_TYPE_AD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_DOCTOR -> {
                val view = inflater.inflate(R.layout.item_find_doctor, parent, false)
                DoctorViewHolder(view)
            }
            VIEW_TYPE_AD -> {
                val view = inflater.inflate(R.layout.item_find_doctor_ad, parent, false)
                AdViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is FindDoctorListItem.DoctorItem -> {
                (holder as DoctorViewHolder).bind(item.doctor)
            }
            is FindDoctorListItem.AdItem -> {
                (holder as AdViewHolder).bind(item.ad)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    /**
     * ViewHolder for Doctor items
     */
    inner class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardDoctor: MaterialCardView = itemView.findViewById(R.id.cardDoctor)
        private val ivDoctorAvatar: ShapeableImageView = itemView.findViewById(R.id.ivDoctorAvatar)
        private val viewOnlineIndicator: View = itemView.findViewById(R.id.viewOnlineIndicator)
        private val tvDoctorName: TextView = itemView.findViewById(R.id.tvDoctorName)
        private val tvSpecialty: TextView = itemView.findViewById(R.id.tvSpecialty)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)

        fun bind(doctor: FindDoctorItem) {
            tvDoctorName.text = doctor.name
            tvSpecialty.text = doctor.specialty
            tvRating.text = "${doctor.rating} (${doctor.reviewCount})"

            // Show/hide online indicator
            viewOnlineIndicator.visibility = if (doctor.isOnline) View.VISIBLE else View.GONE

            // Load avatar image - use placeholder for now
            ivDoctorAvatar.setImageResource(R.drawable.bg_avatar_placeholder)

            // In production, load with Glide/Coil:
            // Glide.with(itemView.context)
            //     .load(doctor.avatarUrl)
            //     .placeholder(R.drawable.bg_avatar_placeholder)
            //     .circleCrop()
            //     .into(ivDoctorAvatar)

            cardDoctor.setOnClickListener {
                onDoctorClick(doctor)
            }
        }
    }

    /**
     * ViewHolder for Ad items (Native Ad placeholder)
     */
    inner class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardAd: MaterialCardView = itemView.findViewById(R.id.cardAd)
        private val tvAdLabel: TextView = itemView.findViewById(R.id.tvAdLabel)
        private val tvSponsor: TextView = itemView.findViewById(R.id.tvSponsor)
        private val btnAdOptions: ImageButton = itemView.findViewById(R.id.btnAdOptions)
        private val tvAdTitle: TextView = itemView.findViewById(R.id.tvAdTitle)
        private val tvAdDescription: TextView = itemView.findViewById(R.id.tvAdDescription)
        private val btnAdCta: MaterialButton = itemView.findViewById(R.id.btnAdCta)

        fun bind(ad: FindDoctorAd) {
            tvSponsor.text = "Sponsored by ${ad.sponsor}"
            tvAdTitle.text = ad.title
            tvAdDescription.text = ad.description
            btnAdCta.text = ad.ctaText

            // Card click
            cardAd.setOnClickListener {
                onAdClick(ad)
            }

            // CTA button click
            btnAdCta.setOnClickListener {
                onAdCtaClick(ad)
            }

            // Options menu click
            btnAdOptions.setOnClickListener {
                // Show ad options (hide ad, report, etc.)
                // Implementation depends on ad provider
            }

            // In production with AdMob Native Ads:
            // - Replace tvAdTitle with NativeAdView headline
            // - Replace tvAdDescription with NativeAdView body
            // - Replace btnAdCta with NativeAdView call to action
            // - Replace image placeholder with MediaView
        }
    }
}
