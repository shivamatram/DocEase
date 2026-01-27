package com.example.docease.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.docease.models.UserRole

/**
 * PreferenceManager - Helper class for managing SharedPreferences
 */
class PreferenceManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.Preferences.PREF_NAME,
        Context.MODE_PRIVATE
    )
    
    // ==================== USER DATA ====================
    
    /**
     * Save user ID
     */
    fun saveUserId(userId: String) {
        prefs.edit().putString(Constants.Preferences.KEY_USER_ID, userId).apply()
    }
    
    /**
     * Get user ID
     */
    fun getUserId(): String? {
        return prefs.getString(Constants.Preferences.KEY_USER_ID, null)
    }
    
    /**
     * Save user role
     */
    fun saveUserRole(role: UserRole) {
        prefs.edit().putString(Constants.Preferences.KEY_USER_ROLE, role.name).apply()
    }
    
    /**
     * Get user role
     */
    fun getUserRole(): UserRole? {
        val roleName = prefs.getString(Constants.Preferences.KEY_USER_ROLE, null)
        return roleName?.let { UserRole.valueOf(it) }
    }
    
    /**
     * Set logged in status
     */
    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(Constants.Preferences.KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(Constants.Preferences.KEY_IS_LOGGED_IN, false)
    }
    
    // ==================== FCM TOKEN ====================
    
    /**
     * Save FCM token
     */
    fun saveFCMToken(token: String) {
        prefs.edit().putString(Constants.Preferences.KEY_FCM_TOKEN, token).apply()
    }
    
    /**
     * Get FCM token
     */
    fun getFCMToken(): String? {
        return prefs.getString(Constants.Preferences.KEY_FCM_TOKEN, null)
    }
    
    // ==================== FIRST TIME USER ====================
    
    /**
     * Set first time user flag
     */
    fun setFirstTimeUser(isFirstTime: Boolean) {
        prefs.edit().putBoolean(Constants.Preferences.KEY_FIRST_TIME_USER, isFirstTime).apply()
    }
    
    /**
     * Check if first time user
     */
    fun isFirstTimeUser(): Boolean {
        return prefs.getBoolean(Constants.Preferences.KEY_FIRST_TIME_USER, true)
    }
    
    // ==================== CLEAR DATA ====================
    
    /**
     * Clear all preferences (on logout)
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
    
    /**
     * Clear user session data (keep app settings)
     */
    fun clearUserData() {
        prefs.edit()
            .remove(Constants.Preferences.KEY_USER_ID)
            .remove(Constants.Preferences.KEY_USER_ROLE)
            .remove(Constants.Preferences.KEY_IS_LOGGED_IN)
            .apply()
    }
    
    // ==================== DOCTOR PROFILE ====================
    
    /**
     * Save doctor name
     */
    fun saveDoctorName(name: String) {
        prefs.edit().putString("doctor_name", name).apply()
    }
    
    /**
     * Get doctor name
     */
    fun getDoctorName(): String? {
        return prefs.getString("doctor_name", null)
    }

    /**
     * Save user name (generic for patient/doctor)
     */
    fun saveUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
    }

    /**
     * Get user name
     */
    fun getUserName(): String? {
        return prefs.getString("user_name", null)
    }
    
    /**
     * Set profile complete flag
     */
    fun setProfileComplete(isComplete: Boolean) {
        prefs.edit().putBoolean("profile_complete", isComplete).apply()
    }
    
    /**
     * Check if profile is complete
     */
    fun isProfileComplete(): Boolean {
        return prefs.getBoolean("profile_complete", false)
    }
}
