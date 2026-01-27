package com.example.docease.repository

import com.example.docease.firebase.DatabaseManager
import com.example.docease.models.Appointment
import com.example.docease.models.AppointmentStatus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Appointment Repository
 * Handles all appointment-related database operations
 * Core feature of the app
 */
class AppointmentRepository(private val dbManager: DatabaseManager) {
    
    private val appointmentsRef = dbManager.getAppointmentsRef()
    
    /**
     * Create new appointment
     */
    suspend fun createAppointment(appointment: Appointment): Result<String> {
        return try {
            val appointmentId = appointment.appointmentId.ifEmpty { 
                appointmentsRef.push().key ?: throw Exception("Failed to generate appointment ID")
            }
            
            val appointmentWithId = appointment.copy(appointmentId = appointmentId)
            appointmentsRef.child(appointmentId).setValue(appointmentWithId.toMap()).await()
            Result.success(appointmentId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get appointment by ID
     */
    suspend fun getAppointmentById(appointmentId: String): Result<Appointment> {
        return try {
            val snapshot = appointmentsRef.child(appointmentId).get().await()
            val appointment = snapshot.getValue(Appointment::class.java)
            if (appointment != null) {
                Result.success(appointment)
            } else {
                Result.failure(Exception("Appointment not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all appointments for a doctor
     */
    suspend fun getAppointmentsByDoctor(doctorId: String): Result<List<Appointment>> {
        return try {
            val snapshot = appointmentsRef
                .orderByChild("doctorId")
                .equalTo(doctorId)
                .get()
                .await()
            
            val appointments = mutableListOf<Appointment>()
            snapshot.children.forEach { child ->
                child.getValue(Appointment::class.java)?.let { appointments.add(it) }
            }
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all appointments for a patient
     */
    suspend fun getAppointmentsByPatient(patientId: String): Result<List<Appointment>> {
        return try {
            val snapshot = appointmentsRef
                .orderByChild("patientId")
                .equalTo(patientId)
                .get()
                .await()
            
            val appointments = mutableListOf<Appointment>()
            snapshot.children.forEach { child ->
                child.getValue(Appointment::class.java)?.let { appointments.add(it) }
            }
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get appointments by status for a doctor
     */
    suspend fun getAppointmentsByDoctorAndStatus(
        doctorId: String,
        status: AppointmentStatus
    ): Result<List<Appointment>> {
        return try {
            val snapshot = appointmentsRef
                .orderByChild("doctorId")
                .equalTo(doctorId)
                .get()
                .await()
            
            val appointments = mutableListOf<Appointment>()
            snapshot.children.forEach { child ->
                child.getValue(Appointment::class.java)?.let { appointment ->
                    if (appointment.status == status) {
                        appointments.add(appointment)
                    }
                }
            }
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get appointments by status for a patient
     */
    suspend fun getAppointmentsByPatientAndStatus(
        patientId: String,
        status: AppointmentStatus
    ): Result<List<Appointment>> {
        return try {
            val snapshot = appointmentsRef
                .orderByChild("patientId")
                .equalTo(patientId)
                .get()
                .await()
            
            val appointments = mutableListOf<Appointment>()
            snapshot.children.forEach { child ->
                child.getValue(Appointment::class.java)?.let { appointment ->
                    if (appointment.status == status) {
                        appointments.add(appointment)
                    }
                }
            }
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update appointment status
     */
    suspend fun updateAppointmentStatus(
        appointmentId: String,
        status: AppointmentStatus
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "status" to status.name,
                "updatedAt" to System.currentTimeMillis()
            )
            appointmentsRef.child(appointmentId).updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cancel appointment
     */
    suspend fun cancelAppointment(appointmentId: String): Result<Unit> {
        return updateAppointmentStatus(appointmentId, AppointmentStatus.CANCELLED)
    }
    
    /**
     * Confirm appointment
     */
    suspend fun confirmAppointment(appointmentId: String): Result<Unit> {
        return updateAppointmentStatus(appointmentId, AppointmentStatus.CONFIRMED)
    }
    
    /**
     * Complete appointment
     */
    suspend fun completeAppointment(appointmentId: String): Result<Unit> {
        return updateAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED)
    }
    
    /**
     * Get upcoming appointments for doctor (CONFIRMED status)
     */
    suspend fun getUpcomingAppointmentsForDoctor(doctorId: String): Result<List<Appointment>> {
        return getAppointmentsByDoctorAndStatus(doctorId, AppointmentStatus.CONFIRMED)
    }
    
    /**
     * Get upcoming appointments for patient (CONFIRMED status)
     */
    suspend fun getUpcomingAppointmentsForPatient(patientId: String): Result<List<Appointment>> {
        return getAppointmentsByPatientAndStatus(patientId, AppointmentStatus.CONFIRMED)
    }
    
    /**
     * Get appointment history for patient (COMPLETED status)
     */
    suspend fun getAppointmentHistory(patientId: String): Result<List<Appointment>> {
        return getAppointmentsByPatientAndStatus(patientId, AppointmentStatus.COMPLETED)
    }
    
    /**
     * Delete appointment
     */
    suspend fun deleteAppointment(appointmentId: String): Result<Unit> {
        return try {
            appointmentsRef.child(appointmentId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Real-time appointments listener for doctor
     */
    fun observeDoctorAppointments(doctorId: String): Flow<Result<List<Appointment>>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val appointments = mutableListOf<Appointment>()
                snapshot.children.forEach { child ->
                    child.getValue(Appointment::class.java)?.let { appointment ->
                        if (appointment.doctorId == doctorId) {
                            appointments.add(appointment)
                        }
                    }
                }
                trySend(Result.success(appointments))
            }
            
            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }
        
        appointmentsRef.addValueEventListener(listener)
        
        awaitClose {
            appointmentsRef.removeEventListener(listener)
        }
    }
    
    /**
     * Real-time appointments listener for patient
     */
    fun observePatientAppointments(patientId: String): Flow<Result<List<Appointment>>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val appointments = mutableListOf<Appointment>()
                snapshot.children.forEach { child ->
                    child.getValue(Appointment::class.java)?.let { appointment ->
                        if (appointment.patientId == patientId) {
                            appointments.add(appointment)
                        }
                    }
                }
                trySend(Result.success(appointments))
            }
            
            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }
        
        appointmentsRef.addValueEventListener(listener)
        
        awaitClose {
            appointmentsRef.removeEventListener(listener)
        }
    }
    
    /**
     * Real-time single appointment listener
     */
    fun observeAppointment(appointmentId: String): Flow<Result<Appointment>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val appointment = snapshot.getValue(Appointment::class.java)
                if (appointment != null) {
                    trySend(Result.success(appointment))
                } else {
                    trySend(Result.failure(Exception("Appointment not found")))
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }
        
        appointmentsRef.child(appointmentId).addValueEventListener(listener)
        
        awaitClose {
            appointmentsRef.child(appointmentId).removeEventListener(listener)
        }
    }
}
