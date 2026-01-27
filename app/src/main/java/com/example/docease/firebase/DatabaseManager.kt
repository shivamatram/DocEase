package com.example.docease.firebase

import android.content.Context
import com.google.firebase.database.*

/**
 * Firebase Realtime Database Manager
 * Singleton class to manage database instance and common operations
 */
class DatabaseManager private constructor(context: Context) {
    
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    
    companion object {
        @Volatile
        private var instance: DatabaseManager? = null
        
        // Database node names
        const val NODE_USERS = "users"
        const val NODE_DOCTORS = "doctors"
        const val NODE_PATIENTS = "patients"
        const val NODE_APPOINTMENTS = "appointments"
        const val NODE_AVAILABILITY = "availability"
        const val NODE_NOTIFICATIONS = "notifications"
        const val NODE_TOKENS = "tokens"
        
        fun getInstance(context: Context): DatabaseManager {
            return instance ?: synchronized(this) {
                instance ?: DatabaseManager(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
        
        /**
         * Get the initialized instance (no context required).
         * Throws IllegalStateException if not initialized yet. Call the
         * context-taking getInstance(context) in Application.onCreate to
         * initialize the singleton early.
         */
        fun getInstance(): DatabaseManager {
            return instance ?: throw IllegalStateException(
                "DatabaseManager is not initialized. Call DatabaseManager.getInstance(context) in Application.onCreate before using it."
            )
        }
    }
    
    init {
        // Enable offline persistence - data will be cached locally
        database.setPersistenceEnabled(true)
    }
    
    /**
     * Get database reference
     */
    fun getDatabase(): FirebaseDatabase = database
    
    /**
     * Get reference to a specific node
     */
    fun getReference(path: String): DatabaseReference = database.getReference(path)
    
    /**
     * Get reference to users node
     */
    fun getUsersRef(): DatabaseReference = database.getReference(NODE_USERS)
    
    /**
     * Get reference to doctors node
     */
    fun getDoctorsRef(): DatabaseReference = database.getReference(NODE_DOCTORS)
    
    /**
     * Get reference to patients node
     */
    fun getPatientsRef(): DatabaseReference = database.getReference(NODE_PATIENTS)
    
    /**
     * Get reference to appointments node
     */
    fun getAppointmentsRef(): DatabaseReference = database.getReference(NODE_APPOINTMENTS)
    
    /**
     * Get reference to availability node
     */
    fun getAvailabilityRef(): DatabaseReference = database.getReference(NODE_AVAILABILITY)
    
    /**
     * Get reference to notifications node
     */
    fun getNotificationsRef(): DatabaseReference = database.getReference(NODE_NOTIFICATIONS)
    
    /**
     * Get reference to tokens node
     */
    fun getTokensRef(): DatabaseReference = database.getReference(NODE_TOKENS)
    
    /**
     * Keep data synchronized for frequently accessed data
     * Use sparingly as it increases bandwidth usage
     */
    fun keepSynced(path: String, keepSynced: Boolean) {
        database.getReference(path).keepSynced(keepSynced)
    }
    
    /**
     * Go online - resume Firebase Database operations
     */
    fun goOnline() {
        database.goOnline()
    }
    
    /**
     * Go offline - pause Firebase Database operations
     */
    fun goOffline() {
        database.goOffline()
    }

    /**
     * Save a user profile to the users node
     */
    fun saveUser(user: com.example.docease.models.User, callback: (Result<Unit>) -> Unit) {
        android.util.Log.d("DatabaseManager", "saveUser: writing user ${user.uid} to path ${NODE_USERS}/${user.uid}")
        val userRef = getUsersRef().child(user.uid)
        userRef.setValue(user.toMap()).addOnCompleteListener { task ->
            android.util.Log.d("DatabaseManager", "saveUser: onComplete success=${task.isSuccessful} exception=${task.exception}")
            if (task.isSuccessful) {
                callback(Result.success(Unit))
            } else {
                callback(Result.failure(task.exception ?: Exception("Failed to save user")))
            }
        }
    }
}
