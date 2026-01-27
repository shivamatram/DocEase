package com.example.docease.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

/**
 * Appointment model - Core feature of the app
 * Stores all booking information and supports bidirectional queries
 */
@IgnoreExtraProperties
data class Appointment(
    val appointmentId: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val date: String = "", // Format: yyyy-MM-dd
    val startTime: String = "", // Format: HH:mm
    val endTime: String = "", // Format: HH:mm
    val consultationFee: Double = 0.0,
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val symptoms: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    @Exclude
    fun toMap(): Map<String, Any> {
        return mapOf(
            "appointmentId" to appointmentId,
            "doctorId" to doctorId,
            "doctorName" to doctorName,
            "patientId" to patientId,
            "patientName" to patientName,
            "date" to date,
            "startTime" to startTime,
            "endTime" to endTime,
            "consultationFee" to consultationFee,
            "status" to status.name,
            "symptoms" to symptoms,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
    
    @Exclude
    fun getFormattedDateTime(): String = "$date at $startTime"
    
    @Exclude
    fun canBeCancelled(): Boolean = status in listOf(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)
    
    @Exclude
    fun canBeCompleted(): Boolean = status == AppointmentStatus.CONFIRMED
}

enum class AppointmentStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED
}
