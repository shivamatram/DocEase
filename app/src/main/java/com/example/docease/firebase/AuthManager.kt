package com.example.docease.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Firebase Authentication Manager
 * Handles all authentication operations
 */
class AuthManager private constructor() {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    companion object {
        @Volatile
        private var instance: AuthManager? = null
        
        fun getInstance(): AuthManager {
            return instance ?: synchronized(this) {
                instance ?: AuthManager().also { instance = it }
            }
        }
    }
    
    /**
     * Get current user
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean = auth.currentUser != null
    
    /**
     * Sign up with email and password
     * @return User ID if successful
     */
    suspend fun signUpWithEmail(email: String, password: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User ID is null")
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sign in with email and password
     * @return User ID if successful
     */
    suspend fun signInWithEmail(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User ID is null")
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update user profile display name
     */
    suspend fun updateDisplayName(displayName: String): Result<Unit> {
        return try {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            auth.currentUser?.updateProfile(profileUpdates)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sign out current user
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Callback-based wrappers for existing code that expects callbacks
     * These internally use suspend functions and call back on the main thread.
     */
    fun signInWithEmailAndPassword(email: String, password: String, callback: (Result<String>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = signInWithEmail(email, password)
            withContext(Dispatchers.Main) { callback(result) }
        }
    }

    fun createUserWithEmailAndPassword(email: String, password: String, callback: (Result<String>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = signUpWithEmail(email, password)
            withContext(Dispatchers.Main) { callback(result) }
        }
    }

    fun sendPasswordResetEmail(email: String, callback: (Result<Unit>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = sendPasswordResetEmail(email)
            withContext(Dispatchers.Main) { callback(result) }
        }
    }
    
    /**
     * Delete current user account
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            auth.currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
