package com.example.docease.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

/**
 * In-app notification model
 * Stores notification history for each user
 */
@IgnoreExtraProperties
data class Notification(
    val notificationId: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val relatedId: String? = null, // appointmentId or other entity ID
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "notificationId" to notificationId,
            "title" to title,
            "message" to message,
            "type" to type.name,
            "relatedId" to relatedId,
            "isRead" to isRead,
            "timestamp" to timestamp
        )
    }
}

enum class NotificationType {
    APPOINTMENT_CONFIRMED,
    APPOINTMENT_CANCELLED,
    APPOINTMENT_REMINDER,
    APPOINTMENT_COMPLETED,
    NEW_APPOINTMENT,
    GENERAL
}

/**
 * FCM Token model for push notifications
 */
@IgnoreExtraProperties
data class FCMToken(
    val fcmToken: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    @Exclude
    fun toMap(): Map<String, Any> {
        return mapOf(
            "fcmToken" to fcmToken,
            "updatedAt" to updatedAt
        )
    }
}
