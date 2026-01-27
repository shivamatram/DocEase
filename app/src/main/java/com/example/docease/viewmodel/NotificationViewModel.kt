package com.example.docease.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docease.firebase.DatabaseManager
import com.example.docease.models.Notification
import com.example.docease.models.NotificationType
import com.example.docease.repository.NotificationRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * NotificationViewModel - Handles notifications and FCM tokens
 * Exposes LiveData for UI to observe
 */
class NotificationViewModel : ViewModel() {
    
    private lateinit var notificationRepository: NotificationRepository
    
    // LiveData for UI observation
    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> = _notifications
    
    private val _unreadCount = MutableLiveData<Int>()
    val unreadCount: LiveData<Int> = _unreadCount
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _operationSuccess = MutableLiveData<Boolean>()
    val operationSuccess: LiveData<Boolean> = _operationSuccess
    
    fun initialize(dbManager: DatabaseManager) {
        notificationRepository = NotificationRepository(dbManager)
    }
    
    /**
     * Get all notifications for user
     */
    fun getNotifications(userId: String) {
        _loading.value = true
        
        viewModelScope.launch {
            val result = notificationRepository.getNotifications(userId)
            _loading.value = false
            
            if (result.isSuccess) {
                _notifications.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load notifications"
            }
        }
    }
    
    /**
     * Observe notifications in real-time
     */
    fun observeNotifications(userId: String) {
        viewModelScope.launch {
            notificationRepository.observeNotifications(userId).collectLatest { result ->
                if (result.isSuccess) {
                    _notifications.value = result.getOrNull() ?: emptyList()
                } else {
                    _error.value = result.exceptionOrNull()?.message
                }
            }
        }
    }
    
    /**
     * Get unread notifications
     */
    fun getUnreadNotifications(userId: String) {
        viewModelScope.launch {
            val result = notificationRepository.getUnreadNotifications(userId)
            
            if (result.isSuccess) {
                val unreadNotifications = result.getOrNull() ?: emptyList()
                _unreadCount.value = unreadNotifications.size
            }
        }
    }
    
    /**
     * Observe unread count in real-time
     */
    fun observeUnreadCount(userId: String) {
        viewModelScope.launch {
            notificationRepository.observeUnreadCount(userId).collectLatest { count ->
                _unreadCount.value = count
            }
        }
    }
    
    /**
     * Create notification
     */
    fun createNotification(
        userId: String,
        title: String,
        message: String,
        type: NotificationType,
        relatedId: String? = null
    ) {
        viewModelScope.launch {
            val notification = Notification(
                title = title,
                message = message,
                type = type,
                relatedId = relatedId,
                timestamp = System.currentTimeMillis()
            )
            
            val result = notificationRepository.createNotification(userId, notification)
            
            if (result.isSuccess) {
                _operationSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to create notification"
                _operationSuccess.value = false
            }
        }
    }
    
    /**
     * Mark notification as read
     */
    fun markAsRead(userId: String, notificationId: String) {
        viewModelScope.launch {
            val result = notificationRepository.markAsRead(userId, notificationId)
            
            if (result.isSuccess) {
                _operationSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to mark as read"
            }
        }
    }
    
    /**
     * Mark all notifications as read
     */
    fun markAllAsRead(userId: String) {
        viewModelScope.launch {
            val result = notificationRepository.markAllAsRead(userId)
            
            if (result.isSuccess) {
                _operationSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to mark all as read"
            }
        }
    }
    
    /**
     * Delete notification
     */
    fun deleteNotification(userId: String, notificationId: String) {
        viewModelScope.launch {
            val result = notificationRepository.deleteNotification(userId, notificationId)
            
            if (result.isSuccess) {
                _operationSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to delete notification"
            }
        }
    }
    
    /**
     * Delete all notifications
     */
    fun deleteAllNotifications(userId: String) {
        viewModelScope.launch {
            val result = notificationRepository.deleteAllNotifications(userId)
            
            if (result.isSuccess) {
                _operationSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to delete notifications"
            }
        }
    }
    
    /**
     * Save FCM token
     */
    fun saveFCMToken(userId: String, token: String) {
        viewModelScope.launch {
            notificationRepository.saveFCMToken(userId, token)
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Clear operation success flag
     */
    fun clearOperationSuccess() {
        _operationSuccess.value = null
    }
}
