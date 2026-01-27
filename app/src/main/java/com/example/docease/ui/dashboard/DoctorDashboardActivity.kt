package com.example.docease.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import coil.load
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Doctor Dashboard Activity
 * Main home screen for doctors showing quick stats and today's appointments
 */
class DoctorDashboardActivity : AppCompatActivity() {

    // UI Components - Header
    private lateinit var rootLayout: View
    private lateinit var doctorAvatar: ImageView
    private lateinit var onlineIndicator: View
    private lateinit var dateText: TextView
    private lateinit var greetingText: TextView
    private lateinit var notificationButton: FrameLayout
    private lateinit var notificationBadge: TextView

    // UI Components - Stats
    private lateinit var statPatientsNumber: TextView
    private lateinit var statAppointmentsNumber: TextView
    private lateinit var statRatingNumber: TextView

    // UI Components - Appointments
    private lateinit var seeAllButton: TextView
    private lateinit var appointmentsRecyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout

    // UI Components - Bottom
    private lateinit var fab: FloatingActionButton
    private lateinit var bottomNavigation: BottomNavigationView

    // Adapter
    private lateinit var appointmentsAdapter: AppointmentsAdapter
    private val appointmentsList = mutableListOf<AppointmentItem>()

    // Doctor data (would come from ViewModel/Repository in production)
    private var doctorName = "Dr. Smith"
    private var totalPatients = 42
    private var todayAppointments = 12
    private var rating = 4.8f
    private var notificationCount = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_doctor_dashboard)

        // Apply window insets
        setupWindowInsets()

        // Initialize UI components
        initViews()

        // Setup data
        setupDoctorData()

        // Setup RecyclerView
        setupAppointmentsList()

        // Setup click listeners
        setupClickListeners()

        // Setup bottom navigation
        setupBottomNavigation()

        // Play entrance animations
        playEntranceAnimations()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            
            // Adjust bottom navigation for system navigation bar
            bottomNavigation.setPadding(0, 0, 0, systemBars.bottom)
            
            insets
        }
    }

    private fun initViews() {
        rootLayout = findViewById(R.id.rootLayout)
        
        // Header
        doctorAvatar = findViewById(R.id.doctorAvatar)
        onlineIndicator = findViewById(R.id.onlineIndicator)
        dateText = findViewById(R.id.dateText)
        greetingText = findViewById(R.id.greetingText)
        notificationButton = findViewById(R.id.notificationButton)
        notificationBadge = findViewById(R.id.notificationBadge)

        // Stats
        statPatientsNumber = findViewById(R.id.statPatientsNumber)
        statAppointmentsNumber = findViewById(R.id.statAppointmentsNumber)
        statRatingNumber = findViewById(R.id.statRatingNumber)

        // Appointments
        seeAllButton = findViewById(R.id.seeAllButton)
        appointmentsRecyclerView = findViewById(R.id.appointmentsRecyclerView)
        emptyState = findViewById(R.id.emptyState)

        // Bottom
        fab = findViewById(R.id.fab)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun setupDoctorData() {
        // Set current date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        dateText.text = dateFormat.format(Date())

        // Fetch doctor profile from Realtime Database
        val userId = com.example.docease.firebase.AuthManager.getInstance().getCurrentUserId()
        if (userId == null) {
            showSnackbar("Please login again")
            return
        }

        val doctorsRef = com.example.docease.firebase.DatabaseManager.getInstance().getDoctorsRef().child(userId)
        doctorsRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val name = snapshot.child("name").getValue(String::class.java) ?: doctorName
                val ratingVal = snapshot.child("rating").getValue(Double::class.java)?.toFloat() ?: rating
                val patientsVal = snapshot.child("totalPatients").getValue(Int::class.java) ?: totalPatients
                val appointmentsVal = snapshot.child("totalAppointments").getValue(Int::class.java) ?: todayAppointments
                val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java) ?: ""

                doctorName = name
                rating = ratingVal
                totalPatients = patientsVal
                todayAppointments = appointmentsVal

                // Update UI (prefix name with 'Dr.')
                greetingText.text = "${getGreeting()}, Dr. $doctorName"
                statPatientsNumber.text = totalPatients.toString()
                statAppointmentsNumber.text = todayAppointments.toString()
                statRatingNumber.text = rating.toString()

                // Load profile image using Coil if available
                if (!profileImageUrl.isNullOrEmpty()) {
                    try {
                        // Use Coil's ImageView extension
                        doctorAvatar.post {
                            doctorAvatar.load(profileImageUrl) {
                                placeholder(R.drawable.ic_doctor_avatar)
                                error(R.drawable.ic_doctor_avatar)
                            }
                        }
                    } catch (e: Exception) {
                        doctorAvatar.setImageResource(R.drawable.ic_doctor_avatar)
                        android.util.Log.e("DoctorDashboard", "Failed to load avatar", e)
                    }
                } else {
                    doctorAvatar.setImageResource(R.drawable.ic_doctor_avatar)
                }
            } else {
                // No profile yet - redirect to profile setup
                try {
                    val intent = Intent(this, Class.forName("com.example.docease.ui.profile.DoctorProfileSetupActivity"))
                    startActivity(intent)
                    finish()
                } catch (e: ClassNotFoundException) {
                    // ignore
                }
            }
        }.addOnFailureListener { e ->
            // If read fails, keep sample data and show a snackbar
            showSnackbar("Failed to load profile: ${e.message}")
        }

        // Set notification badge
        if (notificationCount > 0) {
            notificationBadge.visibility = View.VISIBLE
            notificationBadge.text = if (notificationCount > 9) "9+" else notificationCount.toString()
        } else {
            notificationBadge.visibility = View.GONE
        }
    }

    private fun getGreeting(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    private fun setupAppointmentsList() {
        // Create sample appointments data
        appointmentsList.addAll(getSampleAppointments())

        // Setup adapter
        appointmentsAdapter = AppointmentsAdapter(appointmentsList) { appointment ->
            onAppointmentClicked(appointment)
        }

        // Setup RecyclerView
        appointmentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DoctorDashboardActivity)
            adapter = appointmentsAdapter
            setHasFixedSize(false)
        }

        // Show/hide empty state
        updateEmptyState()
    }

    private fun getSampleAppointments(): List<AppointmentItem> {
        return listOf(
            AppointmentItem(
                id = "1",
                patientName = "Sarah Johnson",
                reason = "General Checkup",
                time = "09:00 AM",
                status = AppointmentStatus.DONE,
                isHighlighted = false
            ),
            AppointmentItem(
                id = "2",
                patientName = "Michael Chen",
                reason = "Follow-up Consultation",
                time = "10:30 AM",
                status = AppointmentStatus.UPCOMING,
                isHighlighted = true
            ),
            AppointmentItem(
                id = "3",
                patientName = "Emily Williams",
                reason = "Blood Test Results",
                time = "11:45 AM",
                status = AppointmentStatus.CONFIRMED,
                isHighlighted = false
            ),
            AppointmentItem(
                id = "4",
                patientName = "James Anderson",
                reason = "Annual Physical",
                time = "02:00 PM",
                status = AppointmentStatus.PENDING,
                isHighlighted = false
            ),
            AppointmentItem(
                id = "5",
                patientName = "Lisa Thompson",
                reason = "Prescription Renewal",
                time = "03:30 PM",
                status = AppointmentStatus.UPCOMING,
                isHighlighted = false
            )
        )
    }

    private fun updateEmptyState() {
        if (appointmentsList.isEmpty()) {
            appointmentsRecyclerView.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            appointmentsRecyclerView.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        // Profile avatar click
        findViewById<View>(R.id.profileContainer).setOnClickListener {
            navigateToProfile()
        }

        // Notification button click
        notificationButton.setOnClickListener {
            navigateToNotifications()
        }

        // See All button click
        seeAllButton.setOnClickListener {
            navigateToAllAppointments()
        }

        // FAB click
        fab.setOnClickListener {
            navigateToCreateAppointment()
        }

        // Stats cards click
        findViewById<View>(R.id.statCardPatients).setOnClickListener {
            navigateToPatientsList()
        }

        findViewById<View>(R.id.statCardAppointments).setOnClickListener {
            navigateToAllAppointments()
        }

        findViewById<View>(R.id.statCardRating).setOnClickListener {
            showSnackbar("View your ratings and reviews")
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home
                    true
                }
                R.id.nav_schedule -> {
                    navigateToSchedule()
                    true
                }
                R.id.nav_patients -> {
                    navigateToPatientsList()
                    true
                }
                R.id.nav_profile -> {
                    navigateToProfile()
                    true
                }
                else -> false
            }
        }
    }

    private fun onAppointmentClicked(appointment: AppointmentItem) {
        showSnackbar("Opening appointment with ${appointment.patientName}")
        // TODO: Navigate to appointment details
        // val intent = Intent(this, AppointmentDetailsActivity::class.java)
        // intent.putExtra("appointment_id", appointment.id)
        // startActivity(intent)
    }

    // Navigation methods
    private fun navigateToProfile() {
        showSnackbar("Opening Profile")
        // TODO: Navigate to profile screen
    }

    private fun navigateToNotifications() {
        showSnackbar("Opening Notifications")
        // TODO: Navigate to notifications screen
    }

    private fun navigateToAllAppointments() {
        showSnackbar("Opening All Appointments")
        // TODO: Navigate to all appointments list
    }

    private fun navigateToCreateAppointment() {
        showSnackbar("Create New Appointment")
        // TODO: Navigate to create appointment screen
    }

    private fun navigateToPatientsList() {
        showSnackbar("Opening Patients List")
        // TODO: Navigate to patients list screen
    }

    private fun navigateToSchedule() {
        val intent = Intent(this, com.example.docease.DoctorAvailabilityActivity::class.java)
        startActivity(intent)
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun playEntranceAnimations() {
        // Fade in animation for profile section
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in).apply {
            duration = 500
        }

        // Slide up animation for stats
        val slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left).apply {
            duration = 400
            startOffset = 200
        }

        // Apply animations
        findViewById<View>(R.id.profileContainer).startAnimation(fadeIn)
        findViewById<View>(R.id.notificationButton).startAnimation(fadeIn)
        findViewById<View>(R.id.statsContainer).startAnimation(slideUp)
        appointmentsRecyclerView.startAnimation(slideUp)
    }

    /**
     * Update dashboard data (called from ViewModel observer in production)
     */
    fun updateDashboardData(
        name: String,
        patients: Int,
        appointments: Int,
        doctorRating: Float,
        notifications: Int
    ) {
        doctorName = name
        totalPatients = patients
        todayAppointments = appointments
        rating = doctorRating
        notificationCount = notifications

        setupDoctorData()
    }

    /**
     * Update appointments list
     */
    fun updateAppointments(appointments: List<AppointmentItem>) {
        appointmentsList.clear()
        appointmentsList.addAll(appointments)
        appointmentsAdapter.notifyDataSetChanged()
        updateEmptyState()
    }
}

/**
 * Appointment data class
 */
data class AppointmentItem(
    val id: String,
    val patientName: String,
    val reason: String,
    val time: String,
    val status: AppointmentStatus,
    val isHighlighted: Boolean = false,
    val avatarUrl: String? = null
)

/**
 * Appointment status enum
 */
enum class AppointmentStatus {
    DONE,
    UPCOMING,
    PENDING,
    CONFIRMED
}
