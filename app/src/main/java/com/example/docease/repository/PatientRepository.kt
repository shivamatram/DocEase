package com.example.docease.repository

import com.example.docease.firebase.DatabaseManager
import com.example.docease.models.Patient
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Patient Repository
 * Handles all patient-related database operations
 */
class PatientRepository(private val dbManager: DatabaseManager) {
    
    private val patientsRef = dbManager.getPatientsRef()
    
    /**
     * Create patient profile
     */
    suspend fun createPatient(patient: Patient): Result<Unit> {
        return try {
            patientsRef.child(patient.uid).setValue(patient.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get patient by ID
     */
    suspend fun getPatientById(uid: String): Result<Patient> {
        return try {
            val snapshot = patientsRef.child(uid).get().await()
            val patient = snapshot.getValue(Patient::class.java)
            if (patient != null) {
                Result.success(patient)
            } else {
                Result.failure(Exception("Patient not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update patient profile
     */
    suspend fun updatePatient(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            patientsRef.child(uid).updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Increment total visits for returning patient tracking
     */
    suspend fun incrementTotalVisits(uid: String): Result<Unit> {
        return try {
            val snapshot = patientsRef.child(uid).child("totalVisits").get().await()
            val currentCount = snapshot.getValue(Int::class.java) ?: 0
            
            val updates = mapOf(
                "totalVisits" to currentCount + 1,
                "lastVisitTimestamp" to System.currentTimeMillis()
            )
            
            patientsRef.child(uid).updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update medical history
     */
    suspend fun updateMedicalHistory(uid: String, medicalHistory: String): Result<Unit> {
        return try {
            patientsRef.child(uid).child("medicalHistory").setValue(medicalHistory).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if patient is returning customer
     */
    suspend fun isReturningPatient(uid: String): Boolean {
        return try {
            val snapshot = patientsRef.child(uid).child("totalVisits").get().await()
            val totalVisits = snapshot.getValue(Int::class.java) ?: 0
            totalVisits > 0
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get all patients (for admin/doctor view)
     */
    suspend fun getAllPatients(): Result<List<Patient>> {
        return try {
            val snapshot = patientsRef.get().await()
            val patients = mutableListOf<Patient>()
            snapshot.children.forEach { child ->
                child.getValue(Patient::class.java)?.let { patients.add(it) }
            }
            Result.success(patients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Real-time patient listener
     */
    fun observePatient(uid: String): Flow<Result<Patient>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val patient = snapshot.getValue(Patient::class.java)
                if (patient != null) {
                    trySend(Result.success(patient))
                } else {
                    trySend(Result.failure(Exception("Patient not found")))
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }
        
        patientsRef.child(uid).addValueEventListener(listener)
        
        awaitClose {
            patientsRef.child(uid).removeEventListener(listener)
        }
    }
}
