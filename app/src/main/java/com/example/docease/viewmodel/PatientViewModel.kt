package com.example.docease.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docease.firebase.DatabaseManager
import com.example.docease.models.Patient
import com.example.docease.repository.PatientRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * PatientViewModel - Handles patient-related operations
 * Exposes LiveData for UI to observe
 */
class PatientViewModel : ViewModel() {
    
    private lateinit var patientRepository: PatientRepository
    
    // LiveData for UI observation
    private val _patient = MutableLiveData<Patient?>()
    val patient: LiveData<Patient?> = _patient
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _operationSuccess = MutableLiveData<Boolean>()
    val operationSuccess: LiveData<Boolean> = _operationSuccess
    
    private val _isReturningPatient = MutableLiveData<Boolean>()
    val isReturningPatient: LiveData<Boolean> = _isReturningPatient
    
    fun initialize(dbManager: DatabaseManager) {
        patientRepository = PatientRepository(dbManager)
    }
    
    /**
     * Create patient profile
     */
    fun createPatientProfile(patient: Patient) {
        _loading.value = true
        
        viewModelScope.launch {
            val result = patientRepository.createPatient(patient)
            _loading.value = false
            
            if (result.isSuccess) {
                _operationSuccess.value = true
                _patient.value = patient
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to create patient profile"
                _operationSuccess.value = false
            }
        }
    }
    
    /**
     * Get patient profile by ID
     */
    fun getPatientProfile(uid: String) {
        _loading.value = true
        
        viewModelScope.launch {
            val result = patientRepository.getPatientById(uid)
            _loading.value = false
            
            if (result.isSuccess) {
                _patient.value = result.getOrNull()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load patient profile"
            }
        }
    }
    
    /**
     * Observe patient profile in real-time
     */
    fun observePatientProfile(uid: String) {
        viewModelScope.launch {
            patientRepository.observePatient(uid).collectLatest { result ->
                if (result.isSuccess) {
                    _patient.value = result.getOrNull()
                } else {
                    _error.value = result.exceptionOrNull()?.message
                }
            }
        }
    }
    
    /**
     * Update patient profile
     */
    fun updatePatientProfile(uid: String, updates: Map<String, Any>) {
        _loading.value = true
        
        viewModelScope.launch {
            val result = patientRepository.updatePatient(uid, updates)
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
     * Update medical history
     */
    fun updateMedicalHistory(uid: String, medicalHistory: String) {
        _loading.value = true
        
        viewModelScope.launch {
            val result = patientRepository.updateMedicalHistory(uid, medicalHistory)
            _loading.value = false
            
            if (result.isSuccess) {
                _operationSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to update medical history"
                _operationSuccess.value = false
            }
        }
    }
    
    /**
     * Check if patient is returning customer
     */
    fun checkReturningPatient(uid: String) {
        viewModelScope.launch {
            val isReturning = patientRepository.isReturningPatient(uid)
            _isReturningPatient.value = isReturning
        }
    }
    
    /**
     * Increment total visits (called after appointment completion)
     */
    fun incrementTotalVisits(uid: String) {
        viewModelScope.launch {
            val result = patientRepository.incrementTotalVisits(uid)
            
            if (result.isSuccess) {
                _operationSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to update visit count"
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
     * Clear operation success flag
     */
    fun clearOperationSuccess() {
        _operationSuccess.value = null
    }
}
