package com.example.docease.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

/**
 * User model for authentication and role management
 * This is the primary node used to determine user role and navigation flow
 */
@IgnoreExtraProperties
data class User(
    val uid: String = "",
    val email: String = "",
    val role: UserRole = UserRole.PATIENT,
    val createdAt: Long = System.currentTimeMillis()
) {
    @Exclude
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "email" to email,
            "role" to role.name.lowercase(),
            "createdAt" to createdAt
        )
    }
}

enum class UserRole {
    DOCTOR,
    PATIENT
}
