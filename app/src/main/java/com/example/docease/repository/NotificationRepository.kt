package com.example.docease.repository

import com.example.docease.firebase.DatabaseManager
import com.example.docease.models.FCMToken
import com.example.docease.models.Notification
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Notification Repository
 * Handles in-app notifications and FCM tokens
 */
class NotificationRepository(private val dbManager: DatabaseManager) {
    
    private val notificationsRef = dbManager.getNotificationsRef()
    private val tokensRef = dbManager.getTokensRef()
    
    /**
     * Create notification for a user
     */
    suspend fun createNotification(userId: String, notification: Notification): Result<String> {
        return try {
            val notificationId = notification.notificationId.ifEmpty {
                notificationsRef.child(userId).push().key 
                    ?: throw Exception("Failed to generate notification ID")
            }
            
            val notificationWithId = notification.copy(notificationId = notificationId)
            notificationsRef.child(userId).child(notificationId)
                .setValue(notificationWithId.toMap()).await()
            Result.success(notificationId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all notifications for a user
     */
    suspend fun getNotifications(userId: String): Result<List<Notification>> {
        return try {
            val snapshot = notificationsRef.child(userId).get().await()
            val notifications = mutableListOf<Notification>()
            snapshot.children.forEach { child ->
                child.getValue(Notification::class.java)?.let { notifications.add(it) }
            }
            // Sort by timestamp (newest first)
            Result.success(notifications.sortedByDescending { it.timestamp })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get unread notifications for a user
     */
    suspend fun getUnreadNotifications(userId: String): Result<List<Notification>> {
        return try {
            val snapshot = notificationsRef.child(userId).get().await()
            val notifications = mutableListOf<Notification>()
            snapshot.children.forEach { child ->
                child.getValue(Notification::class.java)?.let { notification ->
                    if (!notification.isRead) {
                        notifications.add(notification)
                    }
                }
            }
            Result.success(notifications.sortedByDescending { it.timestamp })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get unread notification count
     */
    suspend fun getUnreadCount(userId: String): Int {
        return try {
            val snapshot = notificationsRef.child(userId).get().await()
            var count = 0
            snapshot.children.forEach { child ->
                child.getValue(Notification::class.java)?.let { notification ->
                    if (!notification.isRead) count++
                }
            }
            count
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Mark notification as read
     */
    suspend fun markAsRead(userId: String, notificationId: String): Result<Unit> {
        return try {
            notificationsRef.child(userId).child(notificationId).child("isRead")
                .setValue(true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Mark all notifications as read
     */
    suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            val snapshot = notificationsRef.child(userId).get().await()
            snapshot.children.forEach { child ->
                child.ref.child("isRead").setValue(true)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete notification
     */
    suspend fun deleteNotification(userId: String, notificationId: String): Result<Unit> {
        return try {
            notificationsRef.child(userId).child(notificationId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete all notifications for a user
     */
    suspend fun deleteAllNotifications(userId: String): Result<Unit> {
        return try {
            notificationsRef.child(userId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Save or update FCM token for push notifications
     */
    suspend fun saveFCMToken(userId: String, token: String): Result<Unit> {
        return try {
            val fcmToken = FCMToken(token, System.currentTimeMillis())
            tokensRef.child(userId).setValue(fcmToken.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get FCM token for a user
     */
    suspend fun getFCMToken(userId: String): Result<String> {
        return try {
            val snapshot = tokensRef.child(userId).child("fcmToken").get().await()
            val token = snapshot.getValue(String::class.java)
            if (token != null) {
                Result.success(token)
            } else {
                Result.failure(Exception("Token not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete FCM token
     */
    suspend fun deleteFCMToken(userId: String): Result<Unit> {
        return try {
            tokensRef.child(userId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Real-time notifications listener
     */
    fun observeNotifications(userId: String): Flow<Result<List<Notification>>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notifications = mutableListOf<Notification>()
                snapshot.children.forEach { child ->
                    child.getValue(Notification::class.java)?.let { notifications.add(it) }
                }
                trySend(Result.success(notifications.sortedByDescending { it.timestamp }))
            }
            
            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }
        
        notificationsRef.child(userId).addValueEventListener(listener)
        
        awaitClose {
            notificationsRef.child(userId).removeEventListener(listener)
        }
    }
    
    /**
     * Real-time unread count listener
     */
    fun observeUnreadCount(userId: String): Flow<Int> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0
                snapshot.children.forEach { child ->
                    child.getValue(Notification::class.java)?.let { notification ->
                        if (!notification.isRead) count++
                    }
                }
                trySend(count)
            }
            
            override fun onCancelled(error: DatabaseError) {
                trySend(0)
            }
        }
        
        notificationsRef.child(userId).addValueEventListener(listener)
        
        awaitClose {
            notificationsRef.child(userId).removeEventListener(listener)
        }
    }
}
