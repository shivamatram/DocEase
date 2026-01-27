package com.example.docease.ui.appointments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R
import com.example.docease.ui.finddoctor.FindDoctorActivity
import com.example.docease.ui.patient.PatientDashboardActivity
import com.example.docease.ui.profile.PatientProfileActivity
import com.example.docease.ui.search.SearchDoctorActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

/**
 * Patient Appointments Activity
 * Shows list of upcoming appointments or empty state when no appointments exist
 */
class PatientAppointmentsActivity : AppCompatActivity() {

    // Views - App Bar
    private lateinit var btnBack: ImageButton
    private lateinit var tvTitle: TextView

    // Views - Empty State
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var btnFindDoctor: MaterialButton
    private lateinit var btnGoHome: MaterialButton

    // Views - Appointments List
    private lateinit var layoutAppointmentsList: NestedScrollView
    private lateinit var rvAppointments: RecyclerView

    // Views - Loading
    private lateinit var layoutLoading: FrameLayout

    // Views - Bottom Navigation
    private lateinit var bottomNavigation: BottomNavigationView

    // Data
    private val appointments = mutableListOf<PatientAppointmentItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_appointments)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        initViews()
        setupWindowInsets()
        setupClickListeners()
        setupBottomNavigation()
        loadAppointments()
    }

    private fun initViews() {
        // App Bar
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)

        // Empty State
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        btnFindDoctor = findViewById(R.id.btnFindDoctor)
        btnGoHome = findViewById(R.id.btnGoHome)

        // Appointments List
        layoutAppointmentsList = findViewById(R.id.layoutAppointmentsList)
        rvAppointments = findViewById(R.id.rvAppointments)

        // Loading
        layoutLoading = findViewById(R.id.layoutLoading)

        // Bottom Navigation
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigation) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnFindDoctor.setOnClickListener {
            navigateToFindDoctor()
        }

        btnGoHome.setOnClickListener {
            navigateToHome()
        }
    }

    private fun setupBottomNavigation() {
        // Set Visits as selected
        bottomNavigation.selectedItemId = R.id.nav_visits

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    navigateToHome()
                    true
                }
                R.id.nav_search -> {
                    navigateToSearch()
                    true
                }
                R.id.nav_visits -> {
                    // Already on this screen
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

    private fun loadAppointments() {
        showLoading(true)

        // Simulate loading from Firebase
        // In production, fetch from Firebase Realtime Database
        android.os.Handler(mainLooper).postDelayed({
            showLoading(false)

            // For demo, show empty state
            // Replace with actual data fetch
            appointments.clear()

            if (appointments.isEmpty()) {
                showEmptyState()
            } else {
                showAppointmentsList()
            }
        }, 500)
    }

    private fun showEmptyState() {
        layoutEmptyState.visibility = View.VISIBLE
        layoutAppointmentsList.visibility = View.GONE
    }

    private fun showAppointmentsList() {
        layoutEmptyState.visibility = View.GONE
        layoutAppointmentsList.visibility = View.VISIBLE

        // Setup RecyclerView adapter
        rvAppointments.layoutManager = LinearLayoutManager(this)
        // rvAppointments.adapter = PatientAppointmentsAdapter(appointments) { appointment ->
        //     navigateToAppointmentDetails(appointment)
        // }
    }

    private fun showLoading(show: Boolean) {
        layoutLoading.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun navigateToFindDoctor() {
        val intent = Intent(this, FindDoctorActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToHome() {
        val intent = Intent(this, PatientDashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun navigateToSearch() {
        val intent = Intent(this, SearchDoctorActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToProfile() {
        val intent = Intent(this, PatientProfileActivity::class.java)
        startActivity(intent)
    }
}

/**
 * Data class for Patient Appointment item
 */
data class PatientAppointmentItem(
    val id: String,
    val doctorId: String,
    val doctorName: String,
    val doctorSpecialty: String,
    val date: String,
    val time: String,
    val status: String,
    val avatarUrl: String
)
