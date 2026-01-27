package com.example.docease.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docease.firebase.DatabaseManager
import com.example.docease.models.Doctor
import com.example.docease.repository.DoctorRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * DoctorViewModel - Handles doctor-related operations
 * Exposes LiveData for UI to observe
 */
class DoctorViewModel : ViewModel() {
    
    private lateinit var doctorRepository: DoctorRepository
    
    // LiveData for UI observation
    private val _doctors = MutableLiveData<List<Doctor>>()
    val doctors: LiveData<List<Doctor>> = _doctors
    
    private val _doctor = MutableLiveData<Doctor?>()
    val doctor: LiveData<Doctor?> = _doctor
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _operationSuccess = MutableLiveData<Boolean>()
    val operationSuccess: LiveData<Boolean> = _operationSuccess
    
    fun initialize(dbManager: DatabaseManager) {
        doctorRepository = DoctorRepository(dbManager)
    }
    
    /**
     * Create doctor profile
     */
    fun createDoctorProfile(doctor: Doctor) {
        _loading.value = true
        
        viewModelScope.launch {
            val result = doctorRepository.createDoctor(doctor)
            _loading.value = false
            
            if (result.isSuccess) {
                _operationSuccess.value = true
                _doctor.value = doctor
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to create doctor profile"
                _operationSuccess.value = false
            }
        }
    }
    
    /**
     * Get doctor profile by ID
     */
    fun getDoctorProfile(uid: String) {
        _loading.value = true
        
        viewModelScope.launch {
            val result = doctorRepository.getDoctorById(uid)
            _loading.value = false
            
            if (result.isSuccess) {
                _doctor.value = result.getOrNull()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load doctor profile"
            }
        }
    }
    
    /**
     * Observe doctor profile in real-time
     */
    fun observeDoctorProfile(uid: String) {
        viewModelScope.launch {
            doctorRepository.observeDoctor(uid).collectLatest { result ->
                if (result.isSuccess) {
                    _doctor.value = result.getOrNull()
                } else {
                    _error.value = result.exceptionOrNull()?.message
                }
            }
        }
    }
    
    /**
     * Get all doctors
     */
    fun getAllDoctors() {
        _loading.value = true
        
        viewModelScope.launch {
            val result = doctorRepository.getAllDoctors()
            _loading.value = false
            
            if (result.isSuccess) {
                _doctors.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load doctors"
            }
        }
    }
    
    /**
     * Observe all doctors in real-time
     */
    fun observeAllDoctors() {
        viewModelScope.launch {
            doctorRepository.observeAllDoctors().collectLatest { result ->
                if (result.isSuccess) {
                    _doctors.value = result.getOrNull() ?: emptyList()
                } else {
                    _error.value = result.exceptionOrNull()?.message
                }
            }
        }
    }
    
    /**
     * Search doctors by specialization
     */
    fun searchBySpecialization(specialization: String) {
        _loading.value = true
        
        viewModelScope.launch {
            val result = doctorRepository.getDoctorsBySpecialization(specialization)
            _loading.value = false
            
            if (result.isSuccess) {
                _doctors.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to search doctors"
            }
        }
    }
    
    /**
     * Search doctors by name
     */
    fun searchByName(query: String) {
        _loading.value = true
        
        viewModelScope.launch {
            val result = doctorRepository.searchDoctorsByName(query)
            _loading.value = false
            
            if (result.isSuccess) {
                _doctors.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to search doctors"
            }
        }
    }
    
    /**
     * Get top rated doctors
     */
    fun getTopRatedDoctors(limit: Int = 10) {
        _loading.value = true
        
        viewModelScope.launch {
            val result = doctorRepository.getTopRatedDoctors(limit)
            _loading.value = false
            
            if (result.isSuccess) {
                _doctors.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load top doctors"
            }
        }
    }
    
    /**
     * Update doctor profile
     */
    fun updateDoctorProfile(uid: String, updates: Map<String, Any>) {
        _loading.value = true
        
        viewModelScope.launch {
            val result = doctorRepository.updateDoctor(uid, updates)
            _loading.value = false
            
            if (result.isSuccess) {
                _operationSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to update profile"
                _operationSuccess.value = false
            }
        }
    }
    
    /**
     * Update doctor availability status
     */
    fun updateAvailability(uid: String, isAvailable: Boolean) {
        viewModelScope.launch {
            val result = doctorRepository.updateAvailability(uid, isAvailable)
            
            if (result.isSuccess) {
                _operationSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to update availability"
                _operationSuccess.value = false
            }
        }
    }
    
    /**
     * Update doctor rating
     */
    fun updateRating(uid: String, newRating: Double) {
        viewModelScope.launch {
            val result = doctorRepository.updateRating(uid, newRating)
            
            if (result.isSuccess) {
                _operationSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to update rating"
                _operationSuccess.value = false
            }
        }
    }
    
    /**
     * Increment total patients count
     */
    fun incrementTotalPatients(uid: String) {
        viewModelScope.launch {
            doctorRepository.incrementTotalPatients(uid)
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Clear operation success flag
     */
    fun clearOperationSuccess() {
        _operationSuccess.value = null
    }
}
