package com.example.docease.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

/**
 * Time slot model for doctor availability
 * Used to prevent double booking and manage doctor schedules
 * Structure: availability → doctorId → date → slotId
 */
@IgnoreExtraProperties
data class Slot(
    val slotId: String = "",
    val startTime: String = "", // Format: HH:mm
    val endTime: String = "", // Format: HH:mm
    val isBooked: Boolean = false,
    val appointmentId: String? = null
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "slotId" to slotId,
            "startTime" to startTime,
            "endTime" to endTime,
            "isBooked" to isBooked,
            "appointmentId" to appointmentId
        )
    }
    
    @Exclude
    fun getTimeRange(): String = "$startTime - $endTime"
}

/**
 * Helper class to generate time slots
 */
object SlotGenerator {
    /**
     * Generates time slots for a given time range
     * @param startHour Starting hour (24-hour format)
     * @param endHour Ending hour (24-hour format)
     * @param intervalMinutes Interval between slots in minutes (default: 30)
     * @return List of generated slots
     */
    fun generateSlots(
        startHour: Int,
        endHour: Int,
        intervalMinutes: Int = 30
    ): List<Slot> {
        val slots = mutableListOf<Slot>()
        var currentMinutes = startHour * 60
        val endMinutes = endHour * 60
        var slotCounter = 1
        
        while (currentMinutes < endMinutes) {
            val startHourVal = currentMinutes / 60
            val startMinVal = currentMinutes % 60
            val endSlotMinutes = currentMinutes + intervalMinutes
            val endHourVal = endSlotMinutes / 60
            val endMinVal = endSlotMinutes % 60
            
            val slot = Slot(
                slotId = "slot$slotCounter",
                startTime = String.format("%02d:%02d", startHourVal, startMinVal),
                endTime = String.format("%02d:%02d", endHourVal, endMinVal),
                isBooked = false,
                appointmentId = null
            )
            
            slots.add(slot)
            currentMinutes += intervalMinutes
            slotCounter++
        }
        
        return slots
    }
    
    /**
     * Default clinic hours: 9 AM to 5 PM
     */
    fun generateDefaultSlots(): List<Slot> = generateSlots(9, 17)
}
