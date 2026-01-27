package com.example.docease

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.adapters.AppointmentCardAdapter
import com.example.docease.adapters.AppointmentCardItem
import com.example.docease.adapters.AppointmentCardStatus
import com.example.docease.adapters.AppointmentTab
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * DoctorAppointmentsActivity displays the doctor's appointments
 * with tabs for Today, Upcoming, and Past appointments
 */
class DoctorAppointmentsActivity : AppCompatActivity() {

    // Views
    private lateinit var rootLayout: View
    private lateinit var btnCalendar: ImageView
    private lateinit var profileContainer: MaterialCardView
    private lateinit var tvDate: TextView
    private lateinit var tvGreeting: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var rvAppointments: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var fabAddAppointment: FloatingActionButton
    private lateinit var bottomNavigation: BottomNavigationView

    // Adapter
    private lateinit var appointmentsAdapter: AppointmentCardAdapter

    // Data
    private var allAppointments = mutableListOf<AppointmentCardItem>()
    private var currentTab = AppointmentTab.TODAY
    private var doctorName = "Dr. Smith"

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    // Date formats
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_doctor_appointments)

        initViews()
        setupGreeting()
        setupTabs()
        setupRecyclerView()
        setupClickListeners()
        setupBottomNavigation()
        loadAppointments()
    }

    private fun initViews() {
        rootLayout = findViewById(R.id.rootLayout)
        btnCalendar = findViewById(R.id.btnCalendar)
        profileContainer = findViewById(R.id.profileContainer)
        tvDate = findViewById(R.id.tvDate)
        tvGreeting = findViewById(R.id.tvGreeting)
        tabLayout = findViewById(R.id.tabLayout)
        rvAppointments = findViewById(R.id.rvAppointments)
        emptyState = findViewById(R.id.emptyState)
        fabAddAppointment = findViewById(R.id.fabAddAppointment)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun setupGreeting() {
        // Set current date
        tvDate.text = displayDateFormat.format(Date())

        // Determine greeting based on time of day
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }

        // Load doctor name from preferences or Firebase
        loadDoctorName()

        // Create spannable greeting with highlighted doctor name
        updateGreetingText(greeting)
    }

    private fun loadDoctorName() {
        val userId = auth.currentUser?.uid ?: return
        
        database.child("doctors").child(userId).child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.getValue(String::class.java)
                    if (!name.isNullOrEmpty()) {
                        doctorName = name
                        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                        val greeting = when {
                            hour < 12 -> "Good Morning"
                            hour < 17 -> "Good Afternoon"
                            else -> "Good Evening"
                        }
                        updateGreetingText(greeting)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Use default name
                }
            })
    }

    private fun updateGreetingText(greeting: String) {
        val fullText = "$greeting, $doctorName"
        val spannableString = SpannableString(fullText)
        
        // Find the position of doctor name
        val nameStart = fullText.indexOf(doctorName)
        if (nameStart >= 0) {
            spannableString.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.docease_primary)),
                nameStart,
                nameStart + doctorName.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        
        tvGreeting.text = spannableString
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = when (tab?.position) {
                    0 -> AppointmentTab.TODAY
                    1 -> AppointmentTab.UPCOMING
                    2 -> AppointmentTab.PAST
                    else -> AppointmentTab.TODAY
                }
                filterAppointments()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        appointmentsAdapter = AppointmentCardAdapter(
            onAppointmentClick = { appointment ->
                openAppointmentDetails(appointment)
            },
            onCallClick = { appointment ->
                initiateCall(appointment)
            },
            onVideoClick = { appointment ->
                initiateVideoCall(appointment)
            },
            onNotesClick = { appointment ->
                openNotes(appointment)
            }
        )

        rvAppointments.apply {
            layoutManager = LinearLayoutManager(this@DoctorAppointmentsActivity)
            adapter = appointmentsAdapter
        }
    }

    private fun setupClickListeners() {
        btnCalendar.setOnClickListener {
            // Navigate to calendar/availability screen
            startActivity(Intent(this, DoctorAvailabilityActivity::class.java))
        }

        profileContainer.setOnClickListener {
            // Navigate to profile
            Toast.makeText(this, "Opening Profile", Toast.LENGTH_SHORT).show()
        }

        fabAddAppointment.setOnClickListener {
            // Open add appointment dialog or screen
            showAddAppointmentOptions()
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_calendar

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    navigateToDashboard()
                    true
                }
                R.id.nav_patients -> {
                    Toast.makeText(this, "Opening Patients", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_calendar -> {
                    // Already here
                    true
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Opening Settings", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadAppointments() {
        val doctorId = auth.currentUser?.uid ?: run {
            loadSampleAppointments()
            return
        }

        database.child("appointments")
            .orderByChild("doctorId")
            .equalTo(doctorId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    allAppointments.clear()
                    
                    for (appointmentSnapshot in snapshot.children) {
                        val appointment = parseAppointment(appointmentSnapshot)
                        if (appointment != null) {
                            allAppointments.add(appointment)
                        }
                    }

                    // Sort by date and time
                    allAppointments.sortBy { it.startTime }
                    
                    filterAppointments()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@DoctorAppointmentsActivity,
                        "Failed to load appointments",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadSampleAppointments()
                }
            })
    }

    private fun parseAppointment(snapshot: DataSnapshot): AppointmentCardItem? {
        return try {
            val id = snapshot.key ?: return null
            val patientId = snapshot.child("patientId").getValue(String::class.java) ?: ""
            val patientName = snapshot.child("patientName").getValue(String::class.java) ?: "Unknown"
            val gender = snapshot.child("gender").getValue(String::class.java) ?: "N/A"
            val age = snapshot.child("age").getValue(Int::class.java) ?: 0
            val reason = snapshot.child("reason").getValue(String::class.java) ?: ""
            val timeRange = snapshot.child("timeRange").getValue(String::class.java) ?: ""
            val startTime = snapshot.child("startTime").getValue(Long::class.java) ?: 0L
            val endTime = snapshot.child("endTime").getValue(Long::class.java) ?: 0L
            val date = snapshot.child("date").getValue(String::class.java) ?: ""
            val statusStr = snapshot.child("status").getValue(String::class.java) ?: "PENDING"
            val phoneNumber = snapshot.child("phoneNumber").getValue(String::class.java)
            val isVideo = snapshot.child("isVideoConsultation").getValue(Boolean::class.java) ?: false

            AppointmentCardItem(
                id = id,
                patientId = patientId,
                patientName = patientName,
                gender = gender,
                age = age,
                reason = reason,
                timeRange = timeRange,
                startTime = startTime,
                endTime = endTime,
                date = date,
                status = AppointmentCardStatus.valueOf(statusStr),
                phoneNumber = phoneNumber,
                isVideoConsultation = isVideo
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun loadSampleAppointments() {
        val today = dateFormat.format(Date())
        val calendar = Calendar.getInstance()
        
        // Tomorrow
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val tomorrow = dateFormat.format(calendar.time)
        
        // Day after tomorrow
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val dayAfter = dateFormat.format(calendar.time)
        
        // Yesterday
        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val yesterday = dateFormat.format(calendar.time)

        allAppointments = mutableListOf(
            // Today's appointments
            AppointmentCardItem(
                id = "1",
                patientId = "p1",
                patientName = "Sarah Johnson",
                gender = "Female",
                age = 28,
                reason = "General Checkup",
                timeRange = "09:30 AM – 10:00 AM",
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis() + 1800000,
                date = today,
                status = AppointmentCardStatus.CONFIRMED,
                phoneNumber = "+1234567890",
                isVideoConsultation = true
            ),
            AppointmentCardItem(
                id = "2",
                patientId = "p2",
                patientName = "Michael Chen",
                gender = "Male",
                age = 35,
                reason = "Follow-up Consultation",
                timeRange = "10:30 AM – 11:00 AM",
                startTime = System.currentTimeMillis() + 3600000,
                endTime = System.currentTimeMillis() + 5400000,
                date = today,
                status = AppointmentCardStatus.CONFIRMED,
                phoneNumber = "+1234567891"
            ),
            AppointmentCardItem(
                id = "3",
                patientId = "p3",
                patientName = "Emily Williams",
                gender = "Female",
                age = 42,
                reason = "Blood Test Results",
                timeRange = "11:30 AM – 12:00 PM",
                startTime = System.currentTimeMillis() + 7200000,
                endTime = System.currentTimeMillis() + 9000000,
                date = today,
                status = AppointmentCardStatus.PENDING,
                phoneNumber = "+1234567892"
            ),
            AppointmentCardItem(
                id = "4",
                patientId = "p4",
                patientName = "James Anderson",
                gender = "Male",
                age = 55,
                reason = "Annual Physical",
                timeRange = "02:00 PM – 02:30 PM",
                startTime = System.currentTimeMillis() + 14400000,
                endTime = System.currentTimeMillis() + 16200000,
                date = today,
                status = AppointmentCardStatus.CONFIRMED,
                phoneNumber = "+1234567893",
                isVideoConsultation = true
            ),
            
            // Upcoming appointments
            AppointmentCardItem(
                id = "5",
                patientId = "p5",
                patientName = "Lisa Thompson",
                gender = "Female",
                age = 31,
                reason = "Prescription Renewal",
                timeRange = "09:00 AM – 09:30 AM",
                startTime = System.currentTimeMillis() + 86400000,
                endTime = System.currentTimeMillis() + 88200000,
                date = tomorrow,
                status = AppointmentCardStatus.CONFIRMED,
                phoneNumber = "+1234567894"
            ),
            AppointmentCardItem(
                id = "6",
                patientId = "p6",
                patientName = "Robert Davis",
                gender = "Male",
                age = 48,
                reason = "Diabetes Management",
                timeRange = "10:00 AM – 10:30 AM",
                startTime = System.currentTimeMillis() + 172800000,
                endTime = System.currentTimeMillis() + 174600000,
                date = dayAfter,
                status = AppointmentCardStatus.PENDING,
                phoneNumber = "+1234567895"
            ),
            
            // Past appointments
            AppointmentCardItem(
                id = "7",
                patientId = "p7",
                patientName = "Amanda White",
                gender = "Female",
                age = 26,
                reason = "Flu Symptoms",
                timeRange = "03:00 PM – 03:30 PM",
                startTime = System.currentTimeMillis() - 86400000,
                endTime = System.currentTimeMillis() - 84600000,
                date = yesterday,
                status = AppointmentCardStatus.COMPLETED,
                phoneNumber = "+1234567896"
            ),
            AppointmentCardItem(
                id = "8",
                patientId = "p8",
                patientName = "David Brown",
                gender = "Male",
                age = 62,
                reason = "Heart Checkup",
                timeRange = "11:00 AM – 11:30 AM",
                startTime = System.currentTimeMillis() - 86400000,
                endTime = System.currentTimeMillis() - 84600000,
                date = yesterday,
                status = AppointmentCardStatus.CANCELLED,
                phoneNumber = "+1234567897"
            )
        )

        filterAppointments()
    }

    private fun filterAppointments() {
        val today = dateFormat.format(Date())
        
        val filtered = when (currentTab) {
            AppointmentTab.TODAY -> {
                allAppointments.filter { it.date == today }
            }
            AppointmentTab.UPCOMING -> {
                allAppointments.filter { 
                    it.date > today && 
                    (it.status == AppointmentCardStatus.CONFIRMED || it.status == AppointmentCardStatus.PENDING)
                }
            }
            AppointmentTab.PAST -> {
                allAppointments.filter { 
                    it.date < today || 
                    it.status == AppointmentCardStatus.COMPLETED ||
                    it.status == AppointmentCardStatus.CANCELLED ||
                    it.status == AppointmentCardStatus.NO_SHOW
                }
            }
        }

        appointmentsAdapter.submitList(filtered)
        updateEmptyState(filtered.isEmpty())
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            rvAppointments.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            rvAppointments.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }

    // Action handlers
    private fun openAppointmentDetails(appointment: AppointmentCardItem) {
        Toast.makeText(this, "Opening details for ${appointment.patientName}", Toast.LENGTH_SHORT).show()
        // TODO: Navigate to appointment details screen
        // val intent = Intent(this, AppointmentDetailsActivity::class.java)
        // intent.putExtra("appointment_id", appointment.id)
        // startActivity(intent)
    }

    private fun initiateCall(appointment: AppointmentCardItem) {
        val phoneNumber = appointment.phoneNumber
        if (phoneNumber.isNullOrEmpty()) {
            Toast.makeText(this, "No phone number available", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Call Patient")
            .setMessage("Call ${appointment.patientName}?")
            .setPositiveButton("Call") { _, _ ->
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun initiateVideoCall(appointment: AppointmentCardItem) {
        if (appointment.status != AppointmentCardStatus.CONFIRMED) {
            Toast.makeText(this, "Video call available only for confirmed appointments", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Starting video call with ${appointment.patientName}", Toast.LENGTH_SHORT).show()
        // TODO: Implement video call functionality
        // This would typically integrate with a video SDK like Twilio, Agora, or Jitsi
    }

    private fun openNotes(appointment: AppointmentCardItem) {
        Toast.makeText(this, "Opening notes for ${appointment.patientName}", Toast.LENGTH_SHORT).show()
        // TODO: Navigate to notes screen or show notes dialog
    }

    private fun showAddAppointmentOptions() {
        val options = arrayOf("Create New Appointment", "Manage Availability")
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Add Appointment")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        Toast.makeText(this, "Create appointment", Toast.LENGTH_SHORT).show()
                        // TODO: Navigate to create appointment screen
                    }
                    1 -> {
                        startActivity(Intent(this, DoctorAvailabilityActivity::class.java))
                    }
                }
            }
            .show()
    }

    private fun navigateToDashboard() {
        try {
            val intent = Intent(this, Class.forName("com.example.docease.ui.dashboard.DoctorDashboardActivity"))
            startActivity(intent)
            finish()
        } catch (e: ClassNotFoundException) {
            Toast.makeText(this, "Dashboard not available", Toast.LENGTH_SHORT).show()
        }
    }
}
