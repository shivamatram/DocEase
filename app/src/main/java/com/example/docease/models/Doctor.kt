package com.example.docease.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

/**
 * Doctor profile model
 * Contains all information needed for doctor search, booking, and profile display
 */
@IgnoreExtraProperties
data class Doctor(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val specialization: String = "",
    val experience: String = "",
    val clinicAddress: String = "",
    val consultationFee: Double = 0.0,
    val phoneNumber: String = "",
    val profileImageUrl: String = "",
    val rating: Double = 0.0,
    val totalPatients: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isAvailable: Boolean = true
) {
    @Exclude
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "specialization" to specialization,
            "experience" to experience,
            "clinicAddress" to clinicAddress,
            "consultationFee" to consultationFee,
            "phoneNumber" to phoneNumber,
            "profileImageUrl" to profileImageUrl,
            "rating" to rating,
            "totalPatients" to totalPatients,
            "createdAt" to createdAt,
            "isAvailable" to isAvailable
        )
    }
}

/**
 * Common specializations for doctors
 */
object Specializations {
    const val CARDIOLOGIST = "Cardiologist"
    const val DERMATOLOGIST = "Dermatologist"
    const val PEDIATRICIAN = "Pediatrician"
    const val NEUROLOGIST = "Neurologist"
    const val ORTHOPEDIC = "Orthopedic"
    const val PSYCHIATRIST = "Psychiatrist"
    const val GENERAL_PHYSICIAN = "General Physician"
    const val ENT_SPECIALIST = "ENT Specialist"
    const val GYNECOLOGIST = "Gynecologist"
    const val OPHTHALMOLOGIST = "Ophthalmologist"
    
    fun getAll(): List<String> = listOf(
        CARDIOLOGIST, DERMATOLOGIST, PEDIATRICIAN, NEUROLOGIST,
        ORTHOPEDIC, PSYCHIATRIST, GENERAL_PHYSICIAN, ENT_SPECIALIST,
        GYNECOLOGIST, OPHTHALMOLOGIST
    )
}
