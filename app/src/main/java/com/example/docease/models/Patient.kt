package com.example.docease.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

/**
 * Patient profile model
 * Used for tracking patient history and identifying returning customers
 */
@IgnoreExtraProperties
data class Patient(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val age: Int = 0,
    val gender: Gender = Gender.OTHER,
    val phoneNumber: String = "",
    val medicalHistory: String = "",
    val bloodGroup: String = "",
    val profileImageUrl: String = "",
    val totalVisits: Int = 0,
    val lastVisitTimestamp: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    @Exclude
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "age" to age,
            "gender" to gender.name,
            "phoneNumber" to phoneNumber,
            "medicalHistory" to medicalHistory,
            "bloodGroup" to bloodGroup,
            "profileImageUrl" to profileImageUrl,
            "totalVisits" to totalVisits,
            "lastVisitTimestamp" to lastVisitTimestamp,
            "createdAt" to createdAt
        )
    }
    
    @Exclude
    fun isReturningPatient(): Boolean = totalVisits > 0
}

enum class Gender {
    MALE,
    FEMALE,
    OTHER
}

/**
 * Common blood groups
 */
object BloodGroups {
    const val A_POSITIVE = "A+"
    const val A_NEGATIVE = "A-"
    const val B_POSITIVE = "B+"
    const val B_NEGATIVE = "B-"
    const val O_POSITIVE = "O+"
    const val O_NEGATIVE = "O-"
    const val AB_POSITIVE = "AB+"
    const val AB_NEGATIVE = "AB-"
    
    fun getAll(): List<String> = listOf(
        A_POSITIVE, A_NEGATIVE, B_POSITIVE, B_NEGATIVE,
        O_POSITIVE, O_NEGATIVE, AB_POSITIVE, AB_NEGATIVE
    )
}
