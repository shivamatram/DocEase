package com.example.docease.utils

/**
 * Constants - Application-wide constants
 */
object Constants {
    
    // ==================== FIREBASE DATABASE NODES ====================
    object DatabaseNodes {
        const val USERS = "users"
        const val DOCTORS = "doctors"
        const val PATIENTS = "patients"
        const val APPOINTMENTS = "appointments"
        const val AVAILABILITY = "availability"
        const val NOTIFICATIONS = "notifications"
        const val FCM_TOKENS = "fcm_tokens"
    }
    
    // ==================== SHARED PREFERENCES ====================
    object Preferences {
        const val PREF_NAME = "DocEasePrefs"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_ROLE = "user_role"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_FCM_TOKEN = "fcm_token"
        const val KEY_FIRST_TIME_USER = "first_time_user"
    }
    
    // ==================== INTENT EXTRAS ====================
    object Extras {
        const val EXTRA_USER_ID = "extra_user_id"
        const val EXTRA_USER_ROLE = "extra_user_role"
        const val EXTRA_DOCTOR_ID = "extra_doctor_id"
        const val EXTRA_PATIENT_ID = "extra_patient_id"
        const val EXTRA_APPOINTMENT_ID = "extra_appointment_id"
        const val EXTRA_DATE = "extra_date"
        const val EXTRA_SLOT_ID = "extra_slot_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        // Flag used when redirecting from social (Google) sign-in to SignUp to indicate profile-only flow
        const val EXTRA_FROM_GOOGLE = "extra_from_google"
    }
    
    // ==================== REQUEST CODES ====================
    object RequestCodes {
        const val REQUEST_LOGIN = 100
        const val REQUEST_SIGNUP = 101
        const val REQUEST_SELECT_DOCTOR = 102
        const val REQUEST_BOOK_APPOINTMENT = 103
        const val REQUEST_EDIT_PROFILE = 104
        // Google Sign-In request code
        const val REQUEST_GOOGLE_SIGN_IN = 110
    }
    
    // ==================== RESULT CODES ====================
    object ResultCodes {
        const val RESULT_SUCCESS = 200
        const val RESULT_CANCELLED = 201
    }
    
    // ==================== NOTIFICATION CHANNELS ====================
    object NotificationChannels {
        const val CHANNEL_ID_APPOINTMENTS = "appointments_channel"
        const val CHANNEL_NAME_APPOINTMENTS = "Appointments"
        const val CHANNEL_DESC_APPOINTMENTS = "Notifications for appointment updates"
        
        const val CHANNEL_ID_REMINDERS = "reminders_channel"
        const val CHANNEL_NAME_REMINDERS = "Reminders"
        const val CHANNEL_DESC_REMINDERS = "Appointment reminders"
        
        const val CHANNEL_ID_GENERAL = "general_channel"
        const val CHANNEL_NAME_GENERAL = "General"
        const val CHANNEL_DESC_GENERAL = "General notifications"
    }
    
    // ==================== TIME CONSTANTS ====================
    object Time {
        const val APPOINTMENT_DURATION_MINUTES = 30
        const val CLINIC_START_HOUR = 9 // 9:00 AM
        const val CLINIC_END_HOUR = 18 // 6:00 PM
        const val MILLISECONDS_IN_DAY = 86400000L
        const val MILLISECONDS_IN_HOUR = 3600000L
        const val MILLISECONDS_IN_MINUTE = 60000L
    }
    
    // ==================== VALIDATION ====================
    object Validation {
        const val MIN_PASSWORD_LENGTH = 6
        const val MAX_NAME_LENGTH = 50
        const val MIN_AGE = 1
        const val MAX_AGE = 120
        const val MIN_PHONE_LENGTH = 10
        const val MAX_PHONE_LENGTH = 15
    }
    
    // ==================== QUERY LIMITS ====================
    object Limits {
        const val DEFAULT_QUERY_LIMIT = 20
        const val MAX_QUERY_LIMIT = 100
        const val TOP_DOCTORS_LIMIT = 10
        const val RECENT_APPOINTMENTS_LIMIT = 5
    }
    
    // ==================== APPOINTMENT STATUS VALUES ====================
    object AppointmentStatusValues {
        const val PENDING = "PENDING"
        const val CONFIRMED = "CONFIRMED"
        const val COMPLETED = "COMPLETED"
        const val CANCELLED = "CANCELLED"
        const val NO_SHOW = "NO_SHOW"
    }
    
    // ==================== USER ROLES ====================
    object UserRoles {
        const val DOCTOR = "DOCTOR"
        const val PATIENT = "PATIENT"
    }
    
    // ==================== NOTIFICATION TYPES ====================
    object NotificationTypes {
        const val APPOINTMENT_BOOKED = "APPOINTMENT_BOOKED"
        const val APPOINTMENT_CONFIRMED = "APPOINTMENT_CONFIRMED"
        const val APPOINTMENT_CANCELLED = "APPOINTMENT_CANCELLED"
        const val APPOINTMENT_REMINDER = "APPOINTMENT_REMINDER"
        const val APPOINTMENT_COMPLETED = "APPOINTMENT_COMPLETED"
        const val NEW_MESSAGE = "NEW_MESSAGE"
        const val GENERAL = "GENERAL"
    }
    
    // ==================== DATE FORMATS ====================
    object DateFormats {
        const val DATE_FORMAT = "yyyy-MM-dd"
        const val TIME_FORMAT = "HH:mm"
        const val DATETIME_FORMAT = "yyyy-MM-dd HH:mm"
        const val DISPLAY_DATE_FORMAT = "dd MMM yyyy"
        const val DISPLAY_TIME_FORMAT = "hh:mm a"
        const val DISPLAY_DATETIME_FORMAT = "dd MMM yyyy, hh:mm a"
    }
    
    // ==================== ERROR MESSAGES ====================
    object ErrorMessages {
        const val NETWORK_ERROR = "Network error. Please check your connection."
        const val AUTH_ERROR = "Authentication failed. Please try again."
        const val PERMISSION_DENIED = "Permission denied. Please check your credentials."
        const val DATA_NOT_FOUND = "Data not found."
        const val OPERATION_FAILED = "Operation failed. Please try again."
        const val SLOT_ALREADY_BOOKED = "This slot is already booked. Please select another time."
        const val INVALID_INPUT = "Invalid input. Please check your data."
    }
    
    // ==================== SUCCESS MESSAGES ====================
    object SuccessMessages {
        const val SIGNUP_SUCCESS = "Account created successfully!"
        const val LOGIN_SUCCESS = "Login successful!"
        const val PROFILE_UPDATED = "Profile updated successfully!"
        const val APPOINTMENT_BOOKED = "Appointment booked successfully!"
        const val APPOINTMENT_CANCELLED = "Appointment cancelled successfully!"
        const val PASSWORD_RESET_SENT = "Password reset email sent!"
    }
    
    // ==================== DOCTOR SPECIALIZATIONS ====================
    object Specializations {
        const val GENERAL_PHYSICIAN = "General Physician"
        const val CARDIOLOGIST = "Cardiologist"
        const val DERMATOLOGIST = "Dermatologist"
        const val PEDIATRICIAN = "Pediatrician"
        const val ORTHOPEDIC = "Orthopedic"
        const val GYNECOLOGIST = "Gynecologist"
        const val NEUROLOGIST = "Neurologist"
        const val PSYCHIATRIST = "Psychiatrist"
        const val DENTIST = "Dentist"
        const val OPHTHALMOLOGIST = "Ophthalmologist"
        const val ENT_SPECIALIST = "ENT Specialist"
        const val UROLOGIST = "Urologist"
    }
    
    // ==================== BLOOD GROUPS ====================
    object BloodGroups {
        const val A_POSITIVE = "A+"
        const val A_NEGATIVE = "A-"
        const val B_POSITIVE = "B+"
        const val B_NEGATIVE = "B-"
        const val O_POSITIVE = "O+"
        const val O_NEGATIVE = "O-"
        const val AB_POSITIVE = "AB+"
        const val AB_NEGATIVE = "AB-"
    }
    
    // ==================== GENDERS ====================
    object Genders {
        const val MALE = "MALE"
        const val FEMALE = "FEMALE"
        const val OTHER = "OTHER"
    }
    
    // ==================== FIREBASE STORAGE ====================
    object Storage {
        const val PROFILE_IMAGES = "profile_images"
        const val DOCTOR_DOCUMENTS = "doctor_documents"
        const val PRESCRIPTION_IMAGES = "prescription_images"
        const val MAX_IMAGE_SIZE_MB = 5
    }
    
    // ==================== RATING CONSTANTS ====================
    object Rating {
        const val MIN_RATING = 0.0
        const val MAX_RATING = 5.0
        const val DEFAULT_RATING = 0.0
    }
}
