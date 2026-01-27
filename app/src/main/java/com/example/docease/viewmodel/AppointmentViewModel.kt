package com.example.docease.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docease.firebase.DatabaseManager
import com.example.docease.models.Appointment
import com.example.docease.models.AppointmentStatus
import com.example.docease.models.Slot
import com.example.docease.repository.AppointmentRepository
import com.example.docease.repository.AvailabilityRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * AppointmentViewModel - Handles appointment booking and management
 * Exposes LiveData for UI to observe
 */
class AppointmentViewModel : ViewModel() {
    
    private lateinit var appointmentRepository: AppointmentRepository
    private lateinit var availabilityRepository: AvailabilityRepository
    
    // LiveData for UI observation
    private val _appointments = MutableLiveData<List<Appointment>>()
    val appointments: LiveData<List<Appointment>> = _appointments
    
    private val _appointment = MutableLiveData<Appointment?>()
    val appointment: LiveData<Appointment?> = _appointment
    
    private val _availableSlots = MutableLiveData<List<Slot>>()
    val availableSlots: LiveData<List<Slot>> = _availableSlots
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _bookingSuccess = MutableLiveData<String?>() // appointmentId
    val bookingSuccess: LiveData<String?> = _bookingSuccess
    
    private val _operationSuccess = MutableLiveData<Boolean>()
    val operationSuccess: LiveData<Boolean> = _operationSuccess
    
    fun initialize(dbManager: DatabaseManager) {
        appointmentRepository = AppointmentRepository(dbManager)
        availabilityRepository = AvailabilityRepository(dbManager)
    }
    
    // ==================== AVAILABILITY OPERATIONS ====================
    
    /**
     * Get available slots for a doctor on a specific date
     */
    fun getAvailableSlots(doctorId: String, date: String) {
        _loading.value = true
        
        viewModelScope.launch {
            val result = availabilityRepository.getAvailableSlots(doctorId, date)
            _loading.value = false
            
            if (result.isSuccess) {
                _availableSlots.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load available slots"
            }
        }
    }
    
    /**
     * Observe slots in real-time
     */
    fun observeSlots(doctorId: String, date: String) {
        viewModelScope.launch {
            availabilityRepository.observeSlots(doctorId, date).collectLatest { result ->
                if (result.isSuccess) {
                    val allSlots = result.getOrNull() ?: emptyList()
                    _availableSlots.value = allSlots.filter { !it.isBooked }
                } else {
                    _error.value = result.exceptionOrNull()?.message
                }
            }
        }
    }
    
    /**
     * Create time slots for doctor (doctor-only operation)
     */
    fun createTimeSlots(doctorId: String, date: String, slots: List<Slot>) {
        _loading.value = true
        
        viewModelScope.launch {
            val result = availabilityRepository.createSlots(doctorId, date, slots)
            _loading.value = false
            
            if (result.isSuccess) {
                _operationSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to create slots"
                _operationSuccess.value = false
            }
        }
    }
    
    // ==================== APPOINTMENT BOOKING ====================
    
    /**
     * Book appointment - CRITICAL OPERATION
     * Uses atomic transaction to prevent double booking
     */
    fun bookAppointment(
        doctorId: String,
        doctorName: String,
        patientId: String,
        patientName: String,
        date: String,
        slot: Slot,
        consultationFee: Double,
        symptoms: String
    ) {
        _loading.value = true
        
        viewModelScope.launch {
            // Generate appointment ID
            val appointmentId = System.currentTimeMillis().toString()
            
            // Step 1: Book slot atomically
            val bookingResult = availabilityRepository.bookSlot(
                doctorId = doctorId,
                date = date,
                slotId = slot.slotId,
                appointmentId = appointmentId
            )
            
            if (bookingResult.isSuccess) {
                // Step 2: Create appointment
                val appointment = Appointment(
                    appointmentId = appointmentId,
                    doctorId = doctorId,
                    doctorName = doctorName,
                    patientId = patientId,
                    patientName = patientName,
                    date = date,
                    startTime = slot.startTime,
                    endTime = slot.endTime,
                    consultationFee = consultationFee,
                    status = AppointmentStatus.PENDING,
                    symptoms = symptoms,
                    createdAt = System.currentTimeMillis()
                )
                
                val appointmentResult = appointmentRepository.createAppointment(appointment)
                _loading.value = false
                
                if (appointmentResult.isSuccess) {
                    _bookingSuccess.value = appointmentId
                    _appointment.value = appointment
                } else {
                    // Rollback: Free the slot
                    availabilityRepository.cancelBooking(doctorId, date, slot.slotId)
                    _error.value = "Failed to create appointment"
                }
            } else {
                _loading.value = false
                _error.value = "Slot already booked. Please select another slot."
            }
        }
    }
    
    // ==================== APPOINTMENT MANAGEMENT ====================
    
    /**
     * Get appointments for doctor
     */
    fun getDoctorAppointments(doctorId: String) {
        _loading.value = true
        
        viewModelScope.launch {
            val result = appointmentRepository.getAppointmentsByDoctor(doctorId)
            _loading.value = false
            
            if (result.isSuccess) {
                _appointments.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load appointments"
            }
        }
    }
    
    /**
     * Get appointments for patient
     */
    fun getPatientAppointments(patientId: String) {
        _loading.value = true
        
        viewModelScope.launch {
            val result = appointmentRepository.getAppointmentsByPatient(patientId)
            _loading.value = false
            
            if (result.isSuccess) {
                _appointments.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load appointments"
            }
        }
    }
    
    /**
     * Observe doctor appointments in real-time
     */
    fun observeDoctorAppointments(doctorId: String) {
        viewModelScope.launch {
            appointmentRepository.observeDoctorAppointments(doctorId).collectLatest { result ->
                if (result.isSuccess) {
                    _appointments.value = result.getOrNull() ?: emptyList()
                } else {
                    _error.value = result.exceptionOrNull()?.message
                }
            }
        }
    }
    
    /**
     * Observe patient appointments in real-time
     */
    fun observePatientAppointments(patientId: String) {
        viewModelScope.launch {
            appointmentRepository.observePatientAppointments(patientId).collectLatest { result ->
                if (result.isSuccess) {
                    _appointments.value = result.getOrNull() ?: emptyList()
                } else {
                    _error.value = result.exceptionOrNull()?.message
                }
            }
        }
    }
    
    /**
     * Get upcoming appointments (CONFIRMED status)
     */
    fun getUpcomingAppointments(userId: String, isDoctor: Boolean) {
        _loading.value = true
        
        viewModelScope.launch {
            val result = if (isDoctor) {
                appointmentRepository.getUpcomingAppointmentsForDoctor(userId)
            } else {
                appointmentRepository.getUpcomingAppointmentsForPatient(userId)
            }
            
            _loading.value = false
            
            if (result.isSuccess) {
                _appointments.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load upcoming appointments"
            }
        }
    }
    
    /**
     * Get appointment history (COMPLETED status)
     */
    fun getAppointmentHistory(patientId: String) {
        _loading.value = true
        
        viewModelScope.launch {
            val result = appointmentRepository.getAppointmentHistory(patientId)
            _loading.value = false
            
            if (result.isSuccess) {
                _appointments.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load history"
            }
        }
    }
    
    /**
     * Confirm appointment (doctor action)
     */
    fun confirmAppointment(appointmentId: String) {
        _loading.value = true
        
        viewModelScope.launch {
            val result = appointmentRepository.confirmAppointment(appointmentId)
            _loading.value = false
            
            if (result.isSuccess) {
                _operationSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to confirm appointment"
                _operationSuccess.value = false
            }
        }
    }
    
    /**
     * Cancel appointment and free slot
     */
    fun cancelAppointment(appointmentId: String, doctorId: String, date: String, slotId: String) {
        _loading.value = true
        
        viewModelScope.launch {
            // Cancel appointment
            val result = appointmentRepository.cancelAppointment(appointmentId)
            
            // Free slot
            if (result.isSuccess) {
                availabilityRepository.cancelBooking(doctorId, date, slotId)
            }
            
            _loading.value = false
            
            if (result.isSuccess) {
                _operationSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to cancel appointment"
                _operationSuccess.value = false
            }
        }
    }
    
    /**
     * Complete appointment
     */
    fun completeAppointment(appointmentId: String) {
        viewModelScope.launch {
            val result = appointmentRepository.completeAppointment(appointmentId)
            
            if (result.isSuccess) {
                _operationSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to complete appointment"
                _operationSuccess.value = false
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Clear booking success
     */
    fun clearBookingSuccess() {
        _bookingSuccess.value = null
    }
    
    /**
     * Clear operation success flag
     */
    fun clearOperationSuccess() {
        _operationSuccess.value = null
    }
}
