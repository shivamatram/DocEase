package com.example.docease.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R
import com.example.docease.ui.auth.LoginActivity
import com.example.docease.ui.patient.PatientDashboardActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar

/**
 * Patient Profile Activity
 * Displays user profile information, health data, past appointments, and settings
 */
class PatientProfileActivity : AppCompatActivity() {

    // Views - Header
    private lateinit var tvEdit: TextView
    private lateinit var ivProfileAvatar: ShapeableImageView
    private lateinit var fabCamera: FloatingActionButton
    private lateinit var tvPatientName: TextView
    private lateinit var tvPatientId: TextView
    private lateinit var tvPhoneNumber: TextView

    // Views - Health Info
    private lateinit var tvAge: TextView
    private lateinit var tvWeight: TextView
    private lateinit var tvHeight: TextView
    private lateinit var tvBloodType: TextView
    private lateinit var tvGender: TextView

    // Views - Past Appointments
    private lateinit var tvSeeAllAppointments: TextView
    private lateinit var rvPastAppointments: RecyclerView
    private lateinit var pastAppointmentAdapter: PastAppointmentAdapter

    // Views - Settings
    private lateinit var layoutNotificationSettings: ConstraintLayout
    private lateinit var layoutPrivacyPolicy: ConstraintLayout
    private lateinit var btnLogout: MaterialButton

    // Views - Navigation
    private lateinit var bottomNavigation: BottomNavigationView

    // Photo Picker
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            ivProfileAvatar.setImageURI(it)
            Snackbar.make(
                findViewById(android.R.id.content),
                "Profile photo updated",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_profile)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        initViews()
        setupWindowInsets()
        setupClickListeners()
        setupPastAppointments()
        setupBottomNavigation()
        loadUserData()
    }

    private fun initViews() {
        // Header
        tvEdit = findViewById(R.id.tvEdit)
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar)
        fabCamera = findViewById(R.id.fabCamera)
        tvPatientName = findViewById(R.id.tvPatientName)
        tvPatientId = findViewById(R.id.tvPatientId)
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber)

        // Health Info
        tvAge = findViewById(R.id.tvAge)
        tvWeight = findViewById(R.id.tvWeight)
        tvHeight = findViewById(R.id.tvHeight)
        tvBloodType = findViewById(R.id.tvBloodType)
        tvGender = findViewById(R.id.tvGender)

        // Past Appointments
        tvSeeAllAppointments = findViewById(R.id.tvSeeAllAppointments)
        rvPastAppointments = findViewById(R.id.rvPastAppointments)

        // Settings
        layoutNotificationSettings = findViewById(R.id.layoutNotificationSettings)
        layoutPrivacyPolicy = findViewById(R.id.layoutPrivacyPolicy)
        btnLogout = findViewById(R.id.btnLogout)

        // Navigation
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
        // Edit Profile
        tvEdit.setOnClickListener {
            openEditProfile()
        }

        // Camera / Change Photo
        fabCamera.setOnClickListener {
            openImagePicker()
        }

        ivProfileAvatar.setOnClickListener {
            openImagePicker()
        }

        // See All Past Appointments
        tvSeeAllAppointments.setOnClickListener {
            openAllAppointments()
        }

        // Notification Settings
        layoutNotificationSettings.setOnClickListener {
            openNotificationSettings()
        }

        // Privacy Policy
        layoutPrivacyPolicy.setOnClickListener {
            openPrivacyPolicy()
        }

        // Logout
        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun setupPastAppointments() {
        // Sample data for past appointments
        val pastAppointments = listOf(
            PastAppointment(
                id = "1",
                doctorName = "Dr. Sarah Smith",
                specialty = "Cardiologist",
                date = "Dec 15, 2024",
                status = AppointmentStatus.COMPLETED
            ),
            PastAppointment(
                id = "2",
                doctorName = "Dr. John Doe",
                specialty = "Dermatologist",
                date = "Dec 10, 2024",
                status = AppointmentStatus.CANCELLED
            ),
            PastAppointment(
                id = "3",
                doctorName = "Dr. Emily White",
                specialty = "Neurologist",
                date = "Dec 5, 2024",
                status = AppointmentStatus.COMPLETED
            )
        )

        pastAppointmentAdapter = PastAppointmentAdapter(pastAppointments) { appointment ->
            openAppointmentDetails(appointment)
        }

        rvPastAppointments.apply {
            layoutManager = LinearLayoutManager(this@PatientProfileActivity)
            adapter = pastAppointmentAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_profile

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    navigateToHome()
                    true
                }
                R.id.nav_bookings -> {
                    navigateToBookings()
                    true
                }
                R.id.nav_chat -> {
                    navigateToChat()
                    true
                }
                R.id.nav_profile -> {
                    // Already on profile
                    true
                }
                else -> false
            }
        }
    }

    private fun loadUserData() {
        // In a real app, this would load data from a repository/ViewModel
        // For now, we're using the default values set in the layout
        tvPatientName.text = "Alex Johnson"
        tvPatientId.text = "Patient ID: #882190"
        tvPhoneNumber.text = "+1 (555) 123-4567"
        
        tvAge.text = "28"
        tvWeight.text = "75"
        tvHeight.text = "180"
        tvBloodType.text = "O+"
        tvGender.text = "Male"
    }

    private fun openEditProfile() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Opening profile editor...",
            Snackbar.LENGTH_SHORT
        ).show()
        
        // In a real app:
        // val intent = Intent(this, EditProfileActivity::class.java)
        // startActivity(intent)
    }

    private fun openImagePicker() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun openAllAppointments() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Opening all appointments...",
            Snackbar.LENGTH_SHORT
        ).show()
        
        // In a real app:
        // val intent = Intent(this, AppointmentHistoryActivity::class.java)
        // startActivity(intent)
    }

    private fun openAppointmentDetails(appointment: PastAppointment) {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Opening details for ${appointment.doctorName}",
            Snackbar.LENGTH_SHORT
        ).show()
        
        // In a real app:
        // val intent = Intent(this, AppointmentStatusActivity::class.java)
        // intent.putExtra("appointment_id", appointment.id)
        // startActivity(intent)
    }

    private fun openNotificationSettings() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Opening notification settings...",
            Snackbar.LENGTH_SHORT
        ).show()
        
        // In a real app:
        // val intent = Intent(this, NotificationSettingsActivity::class.java)
        // startActivity(intent)
    }

    private fun openPrivacyPolicy() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Opening privacy policy...",
            Snackbar.LENGTH_SHORT
        ).show()
        
        // In a real app:
        // val intent = Intent(this, PrivacyPolicyActivity::class.java)
        // startActivity(intent)
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out of your account?")
            .setPositiveButton("Log Out") { dialog, _ ->
                performLogout()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performLogout() {
        // In a real app, clear session/tokens from SharedPreferences or DataStore
        // FirebaseAuth.getInstance().signOut()
        
        Snackbar.make(
            findViewById(android.R.id.content),
            "Logging out...",
            Snackbar.LENGTH_SHORT
        ).show()

        // Navigate to Login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToHome() {
        val intent = Intent(this, PatientDashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun navigateToBookings() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Navigating to Bookings...",
            Snackbar.LENGTH_SHORT
        ).show()
        
        // In a real app:
        // val intent = Intent(this, BookingsActivity::class.java)
        // startActivity(intent)
        // overridePendingTransition(0, 0)
    }

    private fun navigateToChat() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Navigating to Chat...",
            Snackbar.LENGTH_SHORT
        ).show()
        
        // In a real app:
        // val intent = Intent(this, ChatActivity::class.java)
        // startActivity(intent)
        // overridePendingTransition(0, 0)
    }
}
