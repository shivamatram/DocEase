package com.example.docease.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docease.firebase.AuthManager
import com.example.docease.firebase.DatabaseManager
import com.example.docease.models.User
import com.example.docease.models.UserRole
import com.example.docease.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * AuthViewModel - Handles authentication logic
 * Exposes LiveData for UI to observe
 */
class AuthViewModel : ViewModel() {
    
    private val authManager = AuthManager.getInstance()
    private lateinit var userRepository: UserRepository
    
    // LiveData for UI observation
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    
    private val _userRole = MutableLiveData<UserRole?>()
    val userRole: LiveData<UserRole?> = _userRole
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    fun initialize(dbManager: DatabaseManager) {
        userRepository = UserRepository(dbManager)
    }
    
    /**
     * Sign up with email and password
     */
    fun signUp(email: String, password: String, role: UserRole, name: String) {
        if (!validateInput(email, password, name)) return
        
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            val result = authManager.signUpWithEmail(email, password)
            
            if (result.isSuccess) {
                val uid = result.getOrNull()!!
                
                // Create user in database
                val user = User(
                    uid = uid,
                    email = email,
                    role = role,
                    createdAt = System.currentTimeMillis()
                )
                
                val userResult = userRepository.createUser(user)
                
                if (userResult.isSuccess) {
                    _authState.value = AuthState.SignUpSuccess(uid, role)
                    _userRole.value = role
                } else {
                    _authState.value = AuthState.Error("Failed to create user profile")
                    _errorMessage.value = userResult.exceptionOrNull()?.message
                }
            } else {
                _authState.value = AuthState.Error("Sign up failed")
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }
    
    /**
     * Sign in with email and password
     */
    fun signIn(email: String, password: String) {
        if (!validateLoginInput(email, password)) return
        
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            val result = authManager.signInWithEmail(email, password)
            
            if (result.isSuccess) {
                val uid = result.getOrNull()!!
                
                // Get user role from database
                val roleResult = userRepository.getUserRole(uid)
                
                if (roleResult.isSuccess) {
                    val role = roleResult.getOrNull()!!
                    _authState.value = AuthState.SignInSuccess(uid, role)
                    _userRole.value = role
                } else {
                    _authState.value = AuthState.Error("Failed to get user role")
                    _errorMessage.value = roleResult.exceptionOrNull()?.message
                }
            } else {
                _authState.value = AuthState.Error("Sign in failed")
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }
    
    /**
     * Check if user is already logged in
     */
    fun checkAuthStatus() {
        viewModelScope.launch {
            val currentUser = authManager.getCurrentUser()
            
            if (currentUser != null) {
                val uid = currentUser.uid
                val roleResult = userRepository.getUserRole(uid)
                
                if (roleResult.isSuccess) {
                    val role = roleResult.getOrNull()!!
                    _authState.value = AuthState.AlreadyLoggedIn(uid, role)
                    _userRole.value = role
                } else {
                    _authState.value = AuthState.NotLoggedIn
                }
            } else {
                _authState.value = AuthState.NotLoggedIn
            }
        }
    }
    
    /**
     * Sign out current user
     */
    fun signOut() {
        authManager.signOut()
        _authState.value = AuthState.SignedOut
        _userRole.value = null
    }
    
    /**
     * Send password reset email
     */
    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _errorMessage.value = "Email cannot be empty"
            return
        }
        
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            val result = authManager.sendPasswordResetEmail(email)
            
            if (result.isSuccess) {
                _authState.value = AuthState.PasswordResetSent
            } else {
                _authState.value = AuthState.Error("Failed to send reset email")
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? = authManager.getCurrentUserId()
    
    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean = authManager.isUserLoggedIn()
    
    // Validation methods
    private fun validateInput(email: String, password: String, name: String): Boolean {
        return when {
            email.isBlank() -> {
                _errorMessage.value = "Email cannot be empty"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _errorMessage.value = "Invalid email format"
                false
            }
            password.isBlank() -> {
                _errorMessage.value = "Password cannot be empty"
                false
            }
            password.length < 6 -> {
                _errorMessage.value = "Password must be at least 6 characters"
                false
            }
            name.isBlank() -> {
                _errorMessage.value = "Name cannot be empty"
                false
            }
            else -> true
        }
    }
    
    private fun validateLoginInput(email: String, password: String): Boolean {
        return when {
            email.isBlank() -> {
                _errorMessage.value = "Email cannot be empty"
                false
            }
            password.isBlank() -> {
                _errorMessage.value = "Password cannot be empty"
                false
            }
            else -> true
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}

/**
 * Authentication states
 */
sealed class AuthState {
    object Loading : AuthState()
    object NotLoggedIn : AuthState()
    object SignedOut : AuthState()
    object PasswordResetSent : AuthState()
    data class SignUpSuccess(val uid: String, val role: UserRole) : AuthState()
    data class SignInSuccess(val uid: String, val role: UserRole) : AuthState()
    data class AlreadyLoggedIn(val uid: String, val role: UserRole) : AuthState()
    data class Error(val message: String) : AuthState()
}
