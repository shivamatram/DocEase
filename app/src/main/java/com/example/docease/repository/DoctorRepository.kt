package com.example.docease.repository

import com.example.docease.firebase.DatabaseManager
import com.example.docease.models.Doctor
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Doctor Repository
 * Handles all doctor-related database operations
 */
class DoctorRepository(private val dbManager: DatabaseManager) {
    
    private val doctorsRef = dbManager.getDoctorsRef()
    
    /**
     * Create doctor profile
     */
    suspend fun createDoctor(doctor: Doctor): Result<Unit> {
        return try {
            doctorsRef.child(doctor.uid).setValue(doctor.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get doctor by ID
     */
    suspend fun getDoctorById(uid: String): Result<Doctor> {
        return try {
            val snapshot = doctorsRef.child(uid).get().await()
            val doctor = snapshot.getValue(Doctor::class.java)
            if (doctor != null) {
                Result.success(doctor)
            } else {
                Result.failure(Exception("Doctor not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all doctors
     */
    suspend fun getAllDoctors(): Result<List<Doctor>> {
        return try {
            val snapshot = doctorsRef.get().await()
            val doctors = mutableListOf<Doctor>()
            snapshot.children.forEach { child ->
                child.getValue(Doctor::class.java)?.let { doctors.add(it) }
            }
            Result.success(doctors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get doctors by specialization
     */
    suspend fun getDoctorsBySpecialization(specialization: String): Result<List<Doctor>> {
        return try {
            val snapshot = doctorsRef
                .orderByChild("specialization")
                .equalTo(specialization)
                .get()
                .await()
            
            val doctors = mutableListOf<Doctor>()
            snapshot.children.forEach { child ->
                child.getValue(Doctor::class.java)?.let { doctors.add(it) }
            }
            Result.success(doctors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update doctor profile
     */
    suspend fun updateDoctor(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            doctorsRef.child(uid).updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update doctor rating
     */
    suspend fun updateRating(uid: String, newRating: Double): Result<Unit> {
        return try {
            doctorsRef.child(uid).child("rating").setValue(newRating).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Increment total patients count
     */
    suspend fun incrementTotalPatients(uid: String): Result<Unit> {
        return try {
            val snapshot = doctorsRef.child(uid).child("totalPatients").get().await()
            val currentCount = snapshot.getValue(Int::class.java) ?: 0
            doctorsRef.child(uid).child("totalPatients").setValue(currentCount + 1).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update availability status
     */
    suspend fun updateAvailability(uid: String, isAvailable: Boolean): Result<Unit> {
        return try {
            doctorsRef.child(uid).child("isAvailable").setValue(isAvailable).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search doctors by name
     */
    suspend fun searchDoctorsByName(query: String): Result<List<Doctor>> {
        return try {
            val snapshot = doctorsRef.get().await()
            val doctors = mutableListOf<Doctor>()
            
            snapshot.children.forEach { child ->
                child.getValue(Doctor::class.java)?.let { doctor ->
                    if (doctor.name.contains(query, ignoreCase = true)) {
                        doctors.add(doctor)
                    }
                }
            }
            
            Result.success(doctors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get top rated doctors
     */
    suspend fun getTopRatedDoctors(limit: Int = 10): Result<List<Doctor>> {
        return try {
            val snapshot = doctorsRef
                .orderByChild("rating")
                .limitToLast(limit)
                .get()
                .await()
            
            val doctors = mutableListOf<Doctor>()
            snapshot.children.forEach { child ->
                child.getValue(Doctor::class.java)?.let { doctors.add(it) }
            }
            
            // Sort in descending order (limitToLast returns ascending)
            Result.success(doctors.sortedByDescending { it.rating })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Real-time all doctors listener
     */
    fun observeAllDoctors(): Flow<Result<List<Doctor>>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val doctors = mutableListOf<Doctor>()
                snapshot.children.forEach { child ->
                    child.getValue(Doctor::class.java)?.let { doctors.add(it) }
                }
                trySend(Result.success(doctors))
            }
            
            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }
        
        doctorsRef.addValueEventListener(listener)
        
        awaitClose {
            doctorsRef.removeEventListener(listener)
        }
    }
    
    /**
     * Real-time single doctor listener
     */
    fun observeDoctor(uid: String): Flow<Result<Doctor>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val doctor = snapshot.getValue(Doctor::class.java)
                if (doctor != null) {
                    trySend(Result.success(doctor))
                } else {
                    trySend(Result.failure(Exception("Doctor not found")))
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }
        
        doctorsRef.child(uid).addValueEventListener(listener)
        
        awaitClose {
            doctorsRef.child(uid).removeEventListener(listener)
        }
    }
}
