package com.example.docease.ui.patient

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.example.docease.firebase.AuthManager
import com.example.docease.firebase.DatabaseManager
import com.example.docease.ui.doctor.DoctorDetailsActivity
import com.example.docease.ui.search.SearchDoctorActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import coil.load
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Patient Dashboard Activity
 * Main home screen for patients showing upcoming appointments and doctor search
 */
class PatientDashboardActivity : AppCompatActivity() {

    // UI Components - Header
    private lateinit var rootLayout: View
    private lateinit var patientAvatar: ImageView
    private lateinit var onlineIndicator: View
    private lateinit var greetingSmallText: TextView
    private lateinit var patientNameText: TextView
    private lateinit var notificationButton: FrameLayout
    private lateinit var notificationBadge: TextView

    // UI Components - Search Bar
    private lateinit var searchBar: LinearLayout

    // UI Components - Upcoming Schedule
    private lateinit var seeAllUpcoming: TextView
    private lateinit var upcomingAppointmentCard: MaterialCardView
    private lateinit var upcomingDoctorAvatar: ImageView
    private lateinit var upcomingDoctorName: TextView
    private lateinit var upcomingDoctorSpecialty: TextView
    private lateinit var videoCallButton: FrameLayout
    private lateinit var upcomingDate: TextView
    private lateinit var upcomingTime: TextView

    // UI Components - Specialty
    private lateinit var specialtyRecyclerView: RecyclerView

    // UI Components - Top Specialists
    private lateinit var filterButton: FrameLayout
    private lateinit var topSpecialistsRecyclerView: RecyclerView

    // UI Components - Bottom Navigation
    private lateinit var bottomNavigation: BottomNavigationView

    // Adapters
    private lateinit var specialtyAdapter: SpecialtyAdapter
    private lateinit var topSpecialistsAdapter: TopSpecialistsAdapter

    // Data
    private val specialtiesList = mutableListOf<SpecialtyItem>()
    private val specialistsList = mutableListOf<SpecialistItem>()

    // Managers
    private lateinit var authManager: AuthManager
    private lateinit var databaseManager: DatabaseManager

    // Patient data
    private var patientName = "Patient"
    private var profileImageUrl = ""
    private var notificationCount = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_patient_dashboard)

        // Initialize managers
        authManager = AuthManager.getInstance()
        databaseManager = DatabaseManager.getInstance()

        // Apply window insets
        setupWindowInsets()

        // Initialize UI components
        initViews()

        // Setup data
        setupPatientData()

        // Setup RecyclerViews
        setupSpecialtyList()
        setupSpecialistsList()

        // Setup click listeners
        setupClickListeners()

        // Setup bottom navigation
        setupBottomNavigation()
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
        patientAvatar = findViewById(R.id.patientAvatar)
        onlineIndicator = findViewById(R.id.onlineIndicator)
        greetingSmallText = findViewById(R.id.greetingSmallText)
        patientNameText = findViewById(R.id.patientNameText)
        notificationButton = findViewById(R.id.notificationButton)
        notificationBadge = findViewById(R.id.notificationBadge)

        // Search Bar
        searchBar = findViewById(R.id.searchBar)

        // Upcoming Schedule
        seeAllUpcoming = findViewById(R.id.seeAllUpcoming)
        upcomingAppointmentCard = findViewById(R.id.upcomingAppointmentCard)
        upcomingDoctorAvatar = findViewById(R.id.upcomingDoctorAvatar)
        upcomingDoctorName = findViewById(R.id.upcomingDoctorName)
        upcomingDoctorSpecialty = findViewById(R.id.upcomingDoctorSpecialty)
        videoCallButton = findViewById(R.id.videoCallButton)
        upcomingDate = findViewById(R.id.upcomingDate)
        upcomingTime = findViewById(R.id.upcomingTime)

        // Specialty
        specialtyRecyclerView = findViewById(R.id.specialtyRecyclerView)

        // Top Specialists
        filterButton = findViewById(R.id.filterButton)
        topSpecialistsRecyclerView = findViewById(R.id.topSpecialistsRecyclerView)

        // Bottom Navigation
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun setupPatientData() {
        // Set greeting based on time of day
        greetingSmallText.text = "${getGreeting()},"

        // Fetch patient data from RTDB
        val currentUserId = authManager.getCurrentUserId()
        if (currentUserId != null) {
            android.util.Log.d("PatientDashboard", "setupPatientData: fetching for uid=$currentUserId")
            databaseManager.getDatabase().reference.child("patients").child(currentUserId).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val snapshot = task.result
                        if (snapshot != null && snapshot.exists()) {
                            android.util.Log.d("PatientDashboard", "setupPatientData: patient found")
                            try {
                                patientName = snapshot.child("name").getValue(String::class.java) ?: "Patient"
                                profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java) ?: ""
                                
                                // Update UI
                                patientNameText.text = patientName
                                if (profileImageUrl.isNotEmpty()) {
                                    patientAvatar.load(profileImageUrl) {
                                        crossfade(true)
                                    }
                                } else {
                                    patientAvatar.setImageResource(R.drawable.ic_doctor_avatar)
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("PatientDashboard", "Failed to parse patient data", e)
                                patientNameText.text = patientName
                            }
                        } else {
                            android.util.Log.d("PatientDashboard", "setupPatientData: patient profile not found")
                            patientNameText.text = patientName
                        }
                    } else {
                        android.util.Log.e("PatientDashboard", "setupPatientData: failed to fetch", task.exception)
                        patientNameText.text = patientName
                    }
                }
        } else {
            patientNameText.text = patientName
        }

        // Set notification badge
        if (notificationCount > 0) {
            notificationBadge.visibility = View.VISIBLE
            notificationBadge.text = if (notificationCount > 9) "9+" else notificationCount.toString()
        } else {
            notificationBadge.visibility = View.GONE
        }

        // Set upcoming appointment data
        setupUpcomingAppointment()
    }

    private fun setupUpcomingAppointment() {
        // Sample upcoming appointment data
        upcomingDoctorName.text = "Dr. Albert Flores"
        upcomingDoctorSpecialty.text = "Surgeon"

        // Set date (Today's date)
        val dateFormat = SimpleDateFormat("d'th' MMMM", Locale.getDefault())
        val today = Date()
        upcomingDate.text = "Today, ${dateFormat.format(today)}"

        // Set time
        upcomingTime.text = "11:00 AM – 12:00 PM"
    }

    private fun getGreeting(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }

    private fun setupSpecialtyList() {
        // Populate specialties data
        specialtiesList.clear()
        specialtiesList.addAll(
            listOf(
                SpecialtyItem("dentist", "Dentist", R.drawable.ic_dentist),
                SpecialtyItem("cardiology", "Cardiology", R.drawable.ic_cardiology),
                SpecialtyItem("eye", "Eye", R.drawable.ic_eye),
                SpecialtyItem("nutrition", "Nutrition", R.drawable.ic_nutrition)
            )
        )

        // Setup adapter
        specialtyAdapter = SpecialtyAdapter(specialtiesList) { specialty ->
            onSpecialtyClick(specialty)
        }

        specialtyRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                this@PatientDashboardActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = specialtyAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSpecialistsList() {
        // Populate specialists data
        specialistsList.clear()
        specialistsList.addAll(
            listOf(
                SpecialistItem(
                    id = "1",
                    name = "Dr. Dianne Russell",
                    specialty = "Dermatologist",
                    distance = "2.5km away",
                    rating = 4.8f,
                    reviewCount = 124
                ),
                SpecialistItem(
                    id = "2",
                    name = "Dr. Bessie Cooper",
                    specialty = "Cardiologist",
                    distance = "3.2km away",
                    rating = 4.9f,
                    reviewCount = 98
                ),
                SpecialistItem(
                    id = "3",
                    name = "Dr. Esther Howard",
                    specialty = "Neurologist",
                    distance = "1.8km away",
                    rating = 4.7f,
                    reviewCount = 156
                )
            )
        )

        // Setup adapter
        topSpecialistsAdapter = TopSpecialistsAdapter(
            specialists = specialistsList,
            onSpecialistClick = { specialist -> onSpecialistClick(specialist) },
            onBookClick = { specialist -> onBookClick(specialist) }
        )

        topSpecialistsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PatientDashboardActivity)
            adapter = topSpecialistsAdapter
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners() {
        // Search bar click - navigate to doctor search
        searchBar.setOnClickListener {
            startActivity(Intent(this, SearchDoctorActivity::class.java))
        }

        // Notification button click
        notificationButton.setOnClickListener {
            showSnackbar("Opening notifications...")
            // TODO: Navigate to notifications activity
            // startActivity(Intent(this, NotificationsActivity::class.java))
        }

        // See all upcoming appointments
        seeAllUpcoming.setOnClickListener {
            showSnackbar("Opening appointments list...")
            // TODO: Navigate to appointments list activity
            // startActivity(Intent(this, AppointmentsListActivity::class.java))
        }

        // Upcoming appointment card click
        upcomingAppointmentCard.setOnClickListener {
            showSnackbar("Opening appointment details...")
            // TODO: Navigate to appointment details
        }

        // Video call button click
        videoCallButton.setOnClickListener {
            showSnackbar("Starting video call...")
            // TODO: Start video call
        }

        // Filter button click
        filterButton.setOnClickListener {
            showSnackbar("Opening filters...")
            // TODO: Show filter dialog or navigate to filter screen
        }

        // Profile avatar click
        findViewById<FrameLayout>(R.id.profileContainer).setOnClickListener {
            showSnackbar("Opening profile...")
            // TODO: Navigate to profile activity
        }
    }

    private fun setupBottomNavigation() {
        // Set Home as selected
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home, do nothing
                    true
                }
                R.id.nav_appointments -> {
                    showSnackbar("Navigating to Appointments...")
                    // TODO: Navigate to appointments activity
                    // startActivity(Intent(this, PatientAppointmentsActivity::class.java))
                    true
                }
                R.id.nav_doctors -> {
                    showSnackbar("Navigating to Doctors...")
                    // TODO: Navigate to doctors list activity
                    // startActivity(Intent(this, DoctorsListActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    showSnackbar("Navigating to Profile...")
                    // TODO: Navigate to patient profile activity
                    // startActivity(Intent(this, PatientProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun onSpecialtyClick(specialty: SpecialtyItem) {
        showSnackbar("Selected specialty: ${specialty.name}")
        // TODO: Navigate to doctors list filtered by specialty
        // val intent = Intent(this, DoctorsListActivity::class.java)
        // intent.putExtra("specialty", specialty.id)
        // startActivity(intent)
    }

    private fun onSpecialistClick(specialist: SpecialistItem) {
        // Navigate to Doctor Details screen
        val intent = Intent(this, DoctorDetailsActivity::class.java).apply {
            putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_ID, specialist.id)
            putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_NAME, specialist.name)
            putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_SPECIALTY, specialist.specialty)
            putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_LOCATION, specialist.distance)
            putExtra(DoctorDetailsActivity.EXTRA_CONSULTATION_FEE, 40.00)
        }
        startActivity(intent)
    }

    private fun onBookClick(specialist: SpecialistItem) {
        // Navigate directly to Doctor Details for booking
        val intent = Intent(this, DoctorDetailsActivity::class.java).apply {
            putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_ID, specialist.id)
            putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_NAME, specialist.name)
            putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_SPECIALTY, specialist.specialty)
            putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_LOCATION, specialist.distance)
            putExtra(DoctorDetailsActivity.EXTRA_CONSULTATION_FEE, 40.00)
        }
        startActivity(intent)
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(getColor(R.color.docease_primary))
            .setTextColor(getColor(R.color.docease_background))
            .show()
    }
}
