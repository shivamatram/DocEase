package com.example.docease.repository

import com.example.docease.firebase.DatabaseManager
import com.example.docease.models.Slot
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Availability Repository
 * Manages doctor time slots and prevents double booking
 * Uses Firebase Transactions for atomic booking operations
 */
class AvailabilityRepository(private val dbManager: DatabaseManager) {
    
    private val availabilityRef = dbManager.getAvailabilityRef()
    
    /**
     * Create time slots for a doctor on a specific date
     * @param doctorId Doctor's UID
     * @param date Date in format yyyy-MM-dd
     * @param slots List of time slots
     */
    suspend fun createSlots(
        doctorId: String,
        date: String,
        slots: List<Slot>
    ): Result<Unit> {
        return try {
            val slotsMap = slots.associate { it.slotId to it.toMap() }
            availabilityRef.child(doctorId).child(date).setValue(slotsMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all slots for a doctor on a specific date
     */
    suspend fun getSlots(doctorId: String, date: String): Result<List<Slot>> {
        return try {
            val snapshot = availabilityRef.child(doctorId).child(date).get().await()
            val slots = mutableListOf<Slot>()
            snapshot.children.forEach { child ->
                child.getValue(Slot::class.java)?.let { slots.add(it) }
            }
            Result.success(slots)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get available (unbooked) slots for a doctor on a specific date
     */
    suspend fun getAvailableSlots(doctorId: String, date: String): Result<List<Slot>> {
        return try {
            val snapshot = availabilityRef.child(doctorId).child(date).get().await()
            val slots = mutableListOf<Slot>()
            snapshot.children.forEach { child ->
                child.getValue(Slot::class.java)?.let { slot ->
                    if (!slot.isBooked) {
                        slots.add(slot)
                    }
                }
            }
            Result.success(slots)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Book a slot using Firebase Transaction (prevents double booking)
     * This is the CRITICAL operation for slot booking
     * @return Result with appointment ID if successful
     */
    suspend fun bookSlot(
        doctorId: String,
        date: String,
        slotId: String,
        appointmentId: String
    ): Result<Unit> = suspendCoroutine { continuation ->
        val slotRef = availabilityRef.child(doctorId).child(date).child(slotId)
        
        slotRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val slot = currentData.getValue(Slot::class.java)
                
                // If slot doesn't exist, abort
                if (slot == null) {
                    return Transaction.abort()
                }
                
                // If slot is already booked, abort
                if (slot.isBooked) {
                    return Transaction.abort()
                }
                
                // Book the slot
                val bookedSlot = slot.copy(
                    isBooked = true,
                    appointmentId = appointmentId
                )
                currentData.value = bookedSlot.toMap()
                
                return Transaction.success(currentData)
            }
            
            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    continuation.resumeWithException(error.toException())
                } else if (!committed) {
                    continuation.resumeWithException(
                        Exception("Slot already booked or unavailable")
                    )
                } else {
                    continuation.resume(Result.success(Unit))
                }
            }
        })
    }
    
    /**
     * Cancel booking and free up the slot
     */
    suspend fun cancelBooking(
        doctorId: String,
        date: String,
        slotId: String
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "isBooked" to false,
                "appointmentId" to null
            )
            availabilityRef.child(doctorId).child(date).child(slotId)
                .updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if a specific slot is available
     */
    suspend fun isSlotAvailable(
        doctorId: String,
        date: String,
        slotId: String
    ): Boolean {
        return try {
            val snapshot = availabilityRef
                .child(doctorId)
                .child(date)
                .child(slotId)
                .get()
                .await()
            
            val slot = snapshot.getValue(Slot::class.java)
            slot != null && !slot.isBooked
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get all dates with slots for a doctor
     */
    suspend fun getDatesWithSlots(doctorId: String): Result<List<String>> {
        return try {
            val snapshot = availabilityRef.child(doctorId).get().await()
            val dates = mutableListOf<String>()
            snapshot.children.forEach { child ->
                child.key?.let { dates.add(it) }
            }
            Result.success(dates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete all slots for a specific date
     */
    suspend fun deleteSlots(doctorId: String, date: String): Result<Unit> {
        return try {
            availabilityRef.child(doctorId).child(date).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update a specific slot
     */
    suspend fun updateSlot(
        doctorId: String,
        date: String,
        slotId: String,
        updates: Map<String, Any?>
    ): Result<Unit> {
        return try {
            availabilityRef.child(doctorId).child(date).child(slotId)
                .updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Real-time slots listener for a specific date
     */
    fun observeSlots(doctorId: String, date: String): Flow<Result<List<Slot>>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val slots = mutableListOf<Slot>()
                snapshot.children.forEach { child ->
                    child.getValue(Slot::class.java)?.let { slots.add(it) }
                }
                trySend(Result.success(slots))
            }
            
            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }
        
        availabilityRef.child(doctorId).child(date).addValueEventListener(listener)
        
        awaitClose {
            availabilityRef.child(doctorId).child(date).removeEventListener(listener)
        }
    }
}
