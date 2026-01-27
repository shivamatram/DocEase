package com.example.docease.examples

import android.content.Context
import com.example.docease.firebase.AuthManager
import com.example.docease.firebase.DatabaseManager
import com.example.docease.models.*
import com.example.docease.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Comprehensive usage examples for DocEase Firebase Backend
 * This file demonstrates all major operations
 * 
 * ⚠️ These are example functions - integrate them into your Activities/ViewModels
 */

class FirebaseUsageExamples(context: Context) {
    
    private val authManager = AuthManager.getInstance()
    private val dbManager = DatabaseManager.getInstance(context)
    
    private val userRepository = UserRepository(dbManager)
    private val doctorRepository = DoctorRepository(dbManager)
    private val patientRepository = PatientRepository(dbManager)
    private val appointmentRepository = AppointmentRepository(dbManager)
    private val availabilityRepository = AvailabilityRepository(dbManager)
    private val notificationRepository = NotificationRepository(dbManager)
    
    // =====================================================
    // 1️⃣ AUTHENTICATION & USER REGISTRATION
    // =====================================================
    
    /**
     * Example 1: Doctor Sign Up Flow
     */
    fun exampleDoctorSignUp(email: String, password: String, doctorData: Doctor) {
        CoroutineScope(Dispatchers.IO).launch {
            // Step 1: Create Firebase Auth account
            val authResult = authManager.signUpWithEmail(email, password)
            
            if (authResult.isSuccess) {
                val uid = authResult.getOrNull() ?: return@launch
                
                // Step 2: Create user role entry
                val user = User(
                    uid = uid,
                    email = email,
                    role = UserRole.DOCTOR
                )
                userRepository.createUser(user)
                
                // Step 3: Create doctor profile
                val doctor = doctorData.copy(uid = uid, email = email)
                val result = doctorRepository.createDoctor(doctor)
                
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        println("✅ Doctor account created successfully!")
                        // Navigate to Doctor Dashboard
                    } else {
                        println("❌ Error: ${result.exceptionOrNull()?.message}")
                    }
                }
            }
        }
    }
    
    /**
     * Example 2: Patient Sign Up Flow
     */
    fun examplePatientSignUp(email: String, password: String, patientData: Patient) {
        CoroutineScope(Dispatchers.IO).launch {
            val authResult = authManager.signUpWithEmail(email, password)
            
            if (authResult.isSuccess) {
                val uid = authResult.getOrNull() ?: return@launch
                
                // Create user role entry
                val user = User(
                    uid = uid,
                    email = email,
                    role = UserRole.PATIENT
                )
                userRepository.createUser(user)
                
                // Create patient profile
                val patient = patientData.copy(uid = uid, email = email)
                val result = patientRepository.createPatient(patient)
                
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        println("✅ Patient account created successfully!")
                        // Navigate to Patient Dashboard
                    }
                }
            }
        }
    }
    
    /**
     * Example 3: Login Flow with Role-Based Navigation
     * ⭐ This is CRITICAL for determining which dashboard to show
     */
    fun exampleLogin(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            // Step 1: Sign in with Firebase Auth
            val authResult = authManager.signInWithEmail(email, password)
            
            if (authResult.isSuccess) {
                val uid = authResult.getOrNull() ?: return@launch
                
                // Step 2: Get user role from database
                val roleResult = userRepository.getUserRole(uid)
                
                withContext(Dispatchers.Main) {
                    if (roleResult.isSuccess) {
                        val role = roleResult.getOrNull()
                        when (role) {
                            UserRole.DOCTOR -> {
                                println("✅ Logged in as Doctor")
                                // Navigate to Doctor Dashboard
                            }
                            UserRole.PATIENT -> {
                                println("✅ Logged in as Patient")
                                // Navigate to Patient Dashboard
                            }
                            else -> {
                                println("❌ Invalid role")
                            }
                        }
                    }
                }
            }
        }
    }
    
    // =====================================================
    // 2️⃣ DOCTOR OPERATIONS
    // =====================================================
    
    /**
     * Example 4: Search Doctors by Specialization
     */
    fun exampleSearchDoctorsBySpecialization(specialization: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = doctorRepository.getDoctorsBySpecialization(specialization)
            
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    val doctors = result.getOrNull() ?: emptyList()
                    println("✅ Found ${doctors.size} ${specialization}s")
                    doctors.forEach { doctor ->
                        println("  - ${doctor.name}, Fee: ₹${doctor.consultationFee}, Rating: ${doctor.rating}⭐")
                    }
                }
            }
        }
    }
    
    /**
     * Example 5: Get Top Rated Doctors
     */
    fun exampleGetTopRatedDoctors() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = doctorRepository.getTopRatedDoctors(limit = 10)
            
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    val doctors = result.getOrNull() ?: emptyList()
                    println("✅ Top Rated Doctors:")
                    doctors.forEachIndexed { index, doctor ->
                        println("${index + 1}. ${doctor.name} - ${doctor.rating}⭐")
                    }
                }
            }
        }
    }
    
    /**
     * Example 6: Real-Time Doctor Profile Listener
     * Use this to show live updates in doctor's profile screen
     */
    fun exampleObserveDoctorProfile(doctorId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            doctorRepository.observeDoctor(doctorId).collect { result ->
                if (result.isSuccess) {
                    val doctor = result.getOrNull()
                    println("🔄 Doctor profile updated: ${doctor?.name}")
                    // Update UI with latest doctor data
                }
            }
        }
    }
    
    // =====================================================
    // 3️⃣ AVAILABILITY & SLOT MANAGEMENT
    // =====================================================
    
    /**
     * Example 7: Doctor Creates Time Slots for a Date
     */
    fun exampleCreateTimeSlots(doctorId: String, date: String) {
        CoroutineScope(Dispatchers.IO).launch {
            // Generate slots from 9 AM to 5 PM (30-minute intervals)
            val slots = SlotGenerator.generateDefaultSlots()
            
            val result = availabilityRepository.createSlots(doctorId, date, slots)
            
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    println("✅ Created ${slots.size} time slots for $date")
                }
            }
        }
    }
    
    /**
     * Example 8: Get Available Slots for Booking
     */
    fun exampleGetAvailableSlots(doctorId: String, date: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = availabilityRepository.getAvailableSlots(doctorId, date)
            
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    val slots = result.getOrNull() ?: emptyList()
                    println("✅ Available slots for $date:")
                    slots.forEach { slot ->
                        println("  - ${slot.getTimeRange()}")
                    }
                    // Show slots in RecyclerView for patient to select
                }
            }
        }
    }
    
    /**
     * Example 9: Real-Time Slots Listener
     * Patients see real-time updates when slots get booked
     */
    fun exampleObserveSlots(doctorId: String, date: String) {
        CoroutineScope(Dispatchers.Main).launch {
            availabilityRepository.observeSlots(doctorId, date).collect { result ->
                if (result.isSuccess) {
                    val slots = result.getOrNull() ?: emptyList()
                    val availableCount = slots.count { !it.isBooked }
                    println("🔄 Slots updated: $availableCount available")
                    // Update RecyclerView in real-time
                }
            }
        }
    }
    
    // =====================================================
    // 4️⃣ APPOINTMENT BOOKING (CORE FEATURE)
    // =====================================================
    
    /**
     * Example 10: Complete Booking Flow (Patient Books Appointment)
     * ⭐ This is the MOST IMPORTANT function - handles atomic booking
     */
    fun exampleBookAppointment(
        doctorId: String,
        doctorName: String,
        patientId: String,
        patientName: String,
        date: String,
        slotId: String,
        slot: Slot,
        consultationFee: Double,
        symptoms: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            // Generate unique appointment ID
            val appointmentId = dbManager.getAppointmentsRef().push().key ?: return@launch
            
            // Step 1: Book slot using Firebase Transaction (prevents double booking)
            val bookingResult = availabilityRepository.bookSlot(
                doctorId = doctorId,
                date = date,
                slotId = slotId,
                appointmentId = appointmentId
            )
            
            if (bookingResult.isSuccess) {
                // Step 2: Create appointment record
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
                    symptoms = symptoms
                )
                
                val appointmentResult = appointmentRepository.createAppointment(appointment)
                
                // Step 3: Increment patient's total visits
                patientRepository.incrementTotalVisits(patientId)
                
                // Step 4: Increment doctor's total patients
                doctorRepository.incrementTotalPatients(doctorId)
                
                // Step 5: Send notifications to both doctor and patient
                sendAppointmentNotifications(
                    doctorId, patientId, doctorName, patientName, date, slot.startTime
                )
                
                withContext(Dispatchers.Main) {
                    if (appointmentResult.isSuccess) {
                        println("✅ Appointment booked successfully!")
                        println("   Appointment ID: $appointmentId")
                        println("   Date: $date at ${slot.startTime}")
                        // Navigate to confirmation screen
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    println("❌ Slot already booked! Please select another slot.")
                    // Show error message to user
                }
            }
        }
    }
    
    /**
     * Example 11: Doctor Confirms Appointment
     */
    fun exampleConfirmAppointment(appointmentId: String, doctorId: String, patientId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = appointmentRepository.confirmAppointment(appointmentId)
            
            if (result.isSuccess) {
                // Send notification to patient
                val notification = Notification(
                    title = "Appointment Confirmed ✅",
                    message = "Your appointment has been confirmed by the doctor",
                    type = NotificationType.APPOINTMENT_CONFIRMED,
                    relatedId = appointmentId
                )
                notificationRepository.createNotification(patientId, notification)
                
                withContext(Dispatchers.Main) {
                    println("✅ Appointment confirmed")
                }
            }
        }
    }
    
    /**
     * Example 12: Cancel Appointment & Free Slot
     */
    fun exampleCancelAppointment(
        appointmentId: String,
        doctorId: String,
        patientId: String,
        date: String,
        slotId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            // Step 1: Update appointment status
            appointmentRepository.cancelAppointment(appointmentId)
            
            // Step 2: Free up the slot
            availabilityRepository.cancelBooking(doctorId, date, slotId)
            
            // Step 3: Notify the other party
            val notification = Notification(
                title = "Appointment Cancelled ❌",
                message = "An appointment has been cancelled",
                type = NotificationType.APPOINTMENT_CANCELLED,
                relatedId = appointmentId
            )
            notificationRepository.createNotification(patientId, notification)
            
            withContext(Dispatchers.Main) {
                println("✅ Appointment cancelled and slot freed")
            }
        }
    }
    
    /**
     * Example 13: Get Doctor's Appointments for Today
     */
    fun exampleGetTodayAppointments(doctorId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = appointmentRepository.getAppointmentsByDoctor(doctorId)
            
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    val appointments = result.getOrNull() ?: emptyList()
                    val confirmedAppointments = appointments.filter { 
                        it.status == AppointmentStatus.CONFIRMED 
                    }
                    println("✅ Today's Appointments: ${confirmedAppointments.size}")
                    confirmedAppointments.forEach { appt ->
                        println("  - ${appt.startTime}: ${appt.patientName}")
                    }
                }
            }
        }
    }
    
    /**
     * Example 14: Real-Time Appointments Listener (Doctor Dashboard)
     */
    fun exampleObserveDoctorAppointments(doctorId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            appointmentRepository.observeDoctorAppointments(doctorId).collect { result ->
                if (result.isSuccess) {
                    val appointments = result.getOrNull() ?: emptyList()
                    val pending = appointments.count { it.status == AppointmentStatus.PENDING }
                    val confirmed = appointments.count { it.status == AppointmentStatus.CONFIRMED }
                    println("🔄 Appointments: $pending pending, $confirmed confirmed")
                    // Update RecyclerView in real-time
                }
            }
        }
    }
    
    // =====================================================
    // 5️⃣ PATIENT OPERATIONS
    // =====================================================
    
    /**
     * Example 15: Check if Patient is Returning Customer
     */
    fun exampleCheckReturningPatient(patientId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val isReturning = patientRepository.isReturningPatient(patientId)
            
            withContext(Dispatchers.Main) {
                if (isReturning) {
                    println("👋 Welcome back! We're glad to see you again.")
                    // Show special message or offer
                } else {
                    println("🎉 Welcome! Thank you for choosing us.")
                }
            }
        }
    }
    
    /**
     * Example 16: Get Patient's Appointment History
     */
    fun exampleGetPatientHistory(patientId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = appointmentRepository.getAppointmentHistory(patientId)
            
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    val history = result.getOrNull() ?: emptyList()
                    println("✅ Your Appointment History (${history.size} visits):")
                    history.forEach { appt ->
                        println("  - ${appt.date}: Dr. ${appt.doctorName}")
                    }
                }
            }
        }
    }
    
    // =====================================================
    // 6️⃣ NOTIFICATIONS
    // =====================================================
    
    /**
     * Example 17: Send Appointment Notifications
     */
    private fun sendAppointmentNotifications(
        doctorId: String,
        patientId: String,
        doctorName: String,
        patientName: String,
        date: String,
        time: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            // Notification for Doctor
            val doctorNotif = Notification(
                title = "New Appointment 📅",
                message = "New appointment with $patientName on $date at $time",
                type = NotificationType.NEW_APPOINTMENT,
                relatedId = null
            )
            notificationRepository.createNotification(doctorId, doctorNotif)
            
            // Notification for Patient
            val patientNotif = Notification(
                title = "Booking Confirmed ✅",
                message = "Your appointment with Dr. $doctorName is booked for $date at $time",
                type = NotificationType.APPOINTMENT_CONFIRMED,
                relatedId = null
            )
            notificationRepository.createNotification(patientId, patientNotif)
        }
    }
    
    /**
     * Example 18: Get Unread Notification Count (for Badge)
     */
    fun exampleGetUnreadCount(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val count = notificationRepository.getUnreadCount(userId)
            
            withContext(Dispatchers.Main) {
                println("🔔 You have $count unread notifications")
                // Update badge on notification icon
            }
        }
    }
    
    /**
     * Example 19: Real-Time Unread Count Listener
     */
    fun exampleObserveUnreadCount(userId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            notificationRepository.observeUnreadCount(userId).collect { count ->
                println("🔄 Unread notifications: $count")
                // Update badge in real-time
            }
        }
    }
    
    /**
     * Example 20: Mark All Notifications as Read
     */
    fun exampleMarkAllNotificationsRead(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            notificationRepository.markAllAsRead(userId)
            
            withContext(Dispatchers.Main) {
                println("✅ All notifications marked as read")
            }
        }
    }
    
    // =====================================================
    // 7️⃣ FCM TOKEN MANAGEMENT
    // =====================================================
    
    /**
     * Example 21: Save FCM Token on App Launch
     * Call this in MainActivity onCreate() or Application class
     */
    fun exampleSaveFCMToken(userId: String, fcmToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            notificationRepository.saveFCMToken(userId, fcmToken)
            println("✅ FCM Token saved for user $userId")
        }
    }
    
    /**
     * Example 22: Get FCM Token to Send Push Notification
     */
    fun exampleSendPushNotification(patientId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val tokenResult = notificationRepository.getFCMToken(patientId)
            
            if (tokenResult.isSuccess) {
                val token = tokenResult.getOrNull() ?: return@launch
                println("📱 Sending push notification to token: $token")
                // Use Firebase Cloud Messaging Admin SDK or Cloud Functions
                // to send actual push notification
            }
        }
    }
}

/**
 * 🎯 Integration Guide:
 * 
 * 1. Copy the functions you need into your ViewModel or Repository
 * 2. Use LiveData/StateFlow instead of println for UI updates
 * 3. Handle loading states (show progress bars)
 * 4. Add proper error handling (show Toast/Snackbar)
 * 5. Remove coroutine scope - use viewModelScope in ViewModel
 * 
 * Example ViewModel Integration:
 * 
 * class DoctorViewModel(private val doctorRepo: DoctorRepository) : ViewModel() {
 *     
 *     private val _doctors = MutableLiveData<List<Doctor>>()
 *     val doctors: LiveData<List<Doctor>> = _doctors
 *     
 *     fun searchDoctors(specialization: String) {
 *         viewModelScope.launch {
 *             val result = doctorRepo.getDoctorsBySpecialization(specialization)
 *             if (result.isSuccess) {
 *                 _doctors.value = result.getOrNull()
 *             }
 *         }
 *     }
 * }
 */
