package com.example.docease.ui.finddoctor

/**
 * Data class for Doctor item in Find a Doctor screen
 */
data class FindDoctorItem(
    val id: String,
    val name: String,
    val specialty: String,
    val category: String,
    val rating: Float,
    val reviewCount: Int,
    val avatarUrl: String,
    val isOnline: Boolean
)

/**
 * Data class for Ad item in Find a Doctor screen
 */
data class FindDoctorAd(
    val id: String,
    val sponsor: String,
    val title: String,
    val description: String,
    val ctaText: String,
    val imageUrl: String,
    val targetUrl: String
)

/**
 * Sealed class for RecyclerView items (Doctor + Ad)
 */
sealed class FindDoctorListItem {
    data class DoctorItem(val doctor: FindDoctorItem) : FindDoctorListItem()
    data class AdItem(val ad: FindDoctorAd) : FindDoctorListItem()
}
