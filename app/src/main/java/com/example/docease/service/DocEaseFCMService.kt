package com.example.docease.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.docease.MainActivity
import com.example.docease.firebase.DatabaseManager
import com.example.docease.models.Notification
import com.example.docease.models.NotificationType
import com.example.docease.repository.NotificationRepository
import com.example.docease.utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * DocEaseFCMService - Firebase Cloud Messaging Service
 * Handles incoming FCM push notifications
 */
class DocEaseFCMService : FirebaseMessagingService() {
    
    private val notificationRepository by lazy {
        NotificationRepository(DatabaseManager.getInstance())
    }
    
    /**
     * Called when a new FCM token is generated
     * Save this token to Firebase Database
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        // Save token to SharedPreferences
        saveTokenToPreferences(token)
        
        // Get current user ID and save token to Firebase
        val userId = getUserIdFromPreferences()
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                notificationRepository.saveFCMToken(userId, token)
            }
        }
    }
    
    /**
     * Called when a message is received
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        // Handle data payload
        message.data.isNotEmpty().let {
            handleDataPayload(message.data)
        }
        
        // Handle notification payload
        message.notification?.let {
            handleNotificationPayload(it)
        }
    }
    
    /**
     * Handle data payload from FCM
     */
    private fun handleDataPayload(data: Map<String, String>) {
        val title = data["title"] ?: "DocEase"
        val body = data["body"] ?: ""
        val type = data["type"] ?: Constants.NotificationTypes.GENERAL
        val relatedId = data["relatedId"]
        val userId = data["userId"]
        
        // Save to Firebase Database
        if (userId != null) {
            val notification = Notification(
                title = title,
                message = body,
                type = NotificationType.valueOf(type),
                relatedId = relatedId,
                timestamp = System.currentTimeMillis()
            )
            
            CoroutineScope(Dispatchers.IO).launch {
                notificationRepository.createNotification(userId, notification)
            }
        }
        
        // Show system notification
        showNotification(title, body, type, relatedId)
    }
    
    /**
     * Handle notification payload from FCM
     */
    private fun handleNotificationPayload(notification: RemoteMessage.Notification) {
        val title = notification.title ?: "DocEase"
        val body = notification.body ?: ""
        
        // Show system notification
        showNotification(title, body)
    }
    
    /**
     * Show system notification
     */
    private fun showNotification(
        title: String,
        message: String,
        type: String = Constants.NotificationTypes.GENERAL,
        relatedId: String? = null
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels(notificationManager)
        }
        
        // Determine channel based on notification type
        val channelId = getChannelForType(type)
        
        // Create intent to open app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Constants.Extras.EXTRA_NOTIFICATION_ID, relatedId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app icon
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
        
        // Show notification
        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notificationBuilder.build()
        )
    }
    
    /**
     * Create notification channels for Android O+
     */
    private fun createNotificationChannels(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Appointments channel
            val appointmentsChannel = NotificationChannel(
                Constants.NotificationChannels.CHANNEL_ID_APPOINTMENTS,
                Constants.NotificationChannels.CHANNEL_NAME_APPOINTMENTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = Constants.NotificationChannels.CHANNEL_DESC_APPOINTMENTS
                enableVibration(true)
            }
            
            // Reminders channel
            val remindersChannel = NotificationChannel(
                Constants.NotificationChannels.CHANNEL_ID_REMINDERS,
                Constants.NotificationChannels.CHANNEL_NAME_REMINDERS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = Constants.NotificationChannels.CHANNEL_DESC_REMINDERS
                enableVibration(true)
            }
            
            // General channel
            val generalChannel = NotificationChannel(
                Constants.NotificationChannels.CHANNEL_ID_GENERAL,
                Constants.NotificationChannels.CHANNEL_NAME_GENERAL,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = Constants.NotificationChannels.CHANNEL_DESC_GENERAL
            }
            
            notificationManager.createNotificationChannel(appointmentsChannel)
            notificationManager.createNotificationChannel(remindersChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }
    
    /**
     * Get notification channel ID based on notification type
     */
    private fun getChannelForType(type: String): String {
        return when (type) {
            Constants.NotificationTypes.APPOINTMENT_BOOKED,
            Constants.NotificationTypes.APPOINTMENT_CONFIRMED,
            Constants.NotificationTypes.APPOINTMENT_CANCELLED,
            Constants.NotificationTypes.APPOINTMENT_COMPLETED ->
                Constants.NotificationChannels.CHANNEL_ID_APPOINTMENTS
            
            Constants.NotificationTypes.APPOINTMENT_REMINDER ->
                Constants.NotificationChannels.CHANNEL_ID_REMINDERS
            
            else ->
                Constants.NotificationChannels.CHANNEL_ID_GENERAL
        }
    }
    
    /**
     * Save FCM token to SharedPreferences
     */
    private fun saveTokenToPreferences(token: String) {
        val prefs = getSharedPreferences(Constants.Preferences.PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(Constants.Preferences.KEY_FCM_TOKEN, token).apply()
    }
    
    /**
     * Get user ID from SharedPreferences
     */
    private fun getUserIdFromPreferences(): String? {
        val prefs = getSharedPreferences(Constants.Preferences.PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(Constants.Preferences.KEY_USER_ID, null)
    }
}
