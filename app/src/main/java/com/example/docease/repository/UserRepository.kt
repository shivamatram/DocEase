package com.example.docease.repository

import com.example.docease.firebase.DatabaseManager
import com.example.docease.models.User
import com.example.docease.models.UserRole
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * User Repository
 * Handles all user-related database operations
 */
class UserRepository(private val dbManager: DatabaseManager) {
    
    private val usersRef = dbManager.getUsersRef()
    
    /**
     * Create new user after signup
     * This is the first operation after Firebase Authentication signup
     */
    suspend fun createUser(user: User): Result<Unit> {
        return try {
            usersRef.child(user.uid).setValue(user.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user by ID (single read)
     * Use this to check user role after login
     */
    suspend fun getUserById(uid: String): Result<User> {
        return try {
            val snapshot = usersRef.child(uid).get().await()
            val user = snapshot.getValue(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user role (for navigation decision)
     * Returns doctor or patient
     */
    suspend fun getUserRole(uid: String): Result<UserRole> {
        return try {
            val snapshot = usersRef.child(uid).child("role").get().await()
            val roleString = snapshot.getValue(String::class.java)
            val role = when (roleString?.lowercase()) {
                "doctor" -> UserRole.DOCTOR
                "patient" -> UserRole.PATIENT
                else -> throw Exception("Invalid role")
            }
            Result.success(role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if user exists
     */
    suspend fun userExists(uid: String): Boolean {
        return try {
            val snapshot = usersRef.child(uid).get().await()
            snapshot.exists()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Update user email
     */
    suspend fun updateEmail(uid: String, newEmail: String): Result<Unit> {
        return try {
            usersRef.child(uid).child("email").setValue(newEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete user
     */
    suspend fun deleteUser(uid: String): Result<Unit> {
        return try {
            usersRef.child(uid).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Real-time user listener (Flow-based)
     */
    fun observeUser(uid: String): Flow<Result<User>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    trySend(Result.success(user))
                } else {
                    trySend(Result.failure(Exception("User not found")))
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }
        
        usersRef.child(uid).addValueEventListener(listener)
        
        awaitClose {
            usersRef.child(uid).removeEventListener(listener)
        }
    }
}
