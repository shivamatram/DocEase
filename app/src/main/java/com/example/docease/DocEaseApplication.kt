package com.example.docease

import android.app.Application
import com.example.docease.firebase.DatabaseManager
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

/**
 * Custom Application class for DocEase
 * Initializes Firebase with offline persistence
 */
class DocEaseApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Enable offline persistence - CRITICAL for offline support
        // This allows the app to work without internet and sync when online
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            // Persistence already enabled or error
            e.printStackTrace()
        }
        
        // Initialize DatabaseManager singleton
        DatabaseManager.getInstance(this)
        
        // Optional: Enable Firebase Analytics (if needed)
        // FirebaseAnalytics.getInstance(this)
    }
}
