package com.example.docease.ui.appointment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.docease.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar

/**
 * Activity displaying the status and details of a booked appointment
 * Features a timeline view, doctor info, visit details, and action buttons
 */
class AppointmentStatusActivity : AppCompatActivity() {

    // Views
    private lateinit var toolbar: MaterialToolbar
    private lateinit var cardStatusHeader: MaterialCardView
    private lateinit var ivStatusIcon: ImageView
    private lateinit var tvStatusTitle: TextView
    private lateinit var tvStatusSubtitle: TextView
    private lateinit var tvBookingId: TextView
    
    // Timeline views
    private lateinit var timelineCircle1: View
    private lateinit var timelineCircle2: View
    private lateinit var timelineCircle3: View
    private lateinit var timelineLine1: View
    private lateinit var timelineLine2: View
    private lateinit var tvStep1Title: TextView
    private lateinit var tvStep1Time: TextView
    private lateinit var tvStep2Title: TextView
    private lateinit var tvStep2Time: TextView
    private lateinit var tvStep3Title: TextView
    
    // Doctor card views
    private lateinit var ivDoctorImage: ShapeableImageView
    private lateinit var tvDoctorName: TextView
    private lateinit var tvDoctorSpecialty: TextView
    private lateinit var tvDoctorRating: TextView
    private lateinit var tvReviewCount: TextView
    private lateinit var btnMessage: MaterialButton
    
    // Visit details views
    private lateinit var tvVisitDate: TextView
    private lateinit var tvVisitTime: TextView
    private lateinit var tvVisitLocation: TextView
    private lateinit var tvVisitAddress: TextView
    private lateinit var tvVisitType: TextView
    
    // Map and actions
    private lateinit var mapContainer: FrameLayout
    private lateinit var btnGetDirections: MaterialButton
    private lateinit var btnReschedule: MaterialButton
    private lateinit var btnCancelAppointment: MaterialButton

    // Appointment data
    private var appointmentId: String = ""
    private var doctorName: String = ""
    private var doctorSpecialty: String = ""
    private var appointmentDate: String = ""
    private var appointmentTime: String = ""
    private var locationName: String = ""
    private var locationAddress: String = ""
    private var appointmentStatus: AppointmentStatusType = AppointmentStatusType.CONFIRMED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_appointment_status)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        initViews()
        setupWindowInsets()
        setupToolbar()
        loadAppointmentData()
        setupClickListeners()
        updateUI()
    }

    private fun initViews() {
        // Toolbar
        toolbar = findViewById(R.id.toolbar)
        
        // Status header
        cardStatusHeader = findViewById(R.id.cardStatusHeader)
        ivStatusIcon = findViewById(R.id.ivStatusIcon)
        tvStatusTitle = findViewById(R.id.tvStatusTitle)
        tvStatusSubtitle = findViewById(R.id.tvStatusSubtitle)
        tvBookingId = findViewById(R.id.tvBookingId)
        
        // Timeline
        timelineCircle1 = findViewById(R.id.timelineCircle1)
        timelineCircle2 = findViewById(R.id.timelineCircle2)
        timelineCircle3 = findViewById(R.id.timelineCircle3)
        timelineLine1 = findViewById(R.id.timelineLine1)
        timelineLine2 = findViewById(R.id.timelineLine2)
        tvStep1Title = findViewById(R.id.tvStep1Title)
        tvStep1Time = findViewById(R.id.tvStep1Time)
        tvStep2Title = findViewById(R.id.tvStep2Title)
        tvStep2Time = findViewById(R.id.tvStep2Time)
        tvStep3Title = findViewById(R.id.tvStep3Title)
        
        // Doctor card
        ivDoctorImage = findViewById(R.id.ivDoctorImage)
        tvDoctorName = findViewById(R.id.tvDoctorName)
        tvDoctorSpecialty = findViewById(R.id.tvDoctorSpecialty)
        tvDoctorRating = findViewById(R.id.tvDoctorRating)
        tvReviewCount = findViewById(R.id.tvReviewCount)
        btnMessage = findViewById(R.id.btnMessage)
        
        // Visit details
        tvVisitDate = findViewById(R.id.tvVisitDate)
        tvVisitTime = findViewById(R.id.tvVisitTime)
        tvVisitLocation = findViewById(R.id.tvVisitLocation)
        tvVisitAddress = findViewById(R.id.tvVisitAddress)
        tvVisitType = findViewById(R.id.tvVisitType)
        
        // Map and actions
        mapContainer = findViewById(R.id.mapContainer)
        btnGetDirections = findViewById(R.id.btnGetDirections)
        btnReschedule = findViewById(R.id.btnReschedule)
        btnCancelAppointment = findViewById(R.id.btnCancelAppointment)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadAppointmentData() {
        // Load data from intent or use defaults
        intent?.let {
            appointmentId = it.getStringExtra(EXTRA_APPOINTMENT_ID) ?: "#APT-2024-0892"
            doctorName = it.getStringExtra(EXTRA_DOCTOR_NAME) ?: "Dr. Sarah Johnson"
            doctorSpecialty = it.getStringExtra(EXTRA_DOCTOR_SPECIALTY) ?: "Cardiologist"
            appointmentDate = it.getStringExtra(EXTRA_APPOINTMENT_DATE) ?: "Monday, Dec 20, 2024"
            appointmentTime = it.getStringExtra(EXTRA_APPOINTMENT_TIME) ?: "10:00 AM - 10:30 AM"
            locationName = it.getStringExtra(EXTRA_LOCATION_NAME) ?: "City Medical Center"
            locationAddress = it.getStringExtra(EXTRA_LOCATION_ADDRESS) ?: "123 Healthcare Ave, Suite 456"
            appointmentStatus = AppointmentStatusType.fromString(
                it.getStringExtra(EXTRA_STATUS) ?: "CONFIRMED"
            )
        }
    }

    private fun setupClickListeners() {
        // Message doctor
        btnMessage.setOnClickListener {
            openChatWithDoctor()
        }
        
        // Get directions
        btnGetDirections.setOnClickListener {
            openMapsNavigation()
        }
        
        // Map container click
        mapContainer.setOnClickListener {
            openMapsNavigation()
        }
        
        // Reschedule appointment
        btnReschedule.setOnClickListener {
            showRescheduleOptions()
        }
        
        // Cancel appointment
        btnCancelAppointment.setOnClickListener {
            showCancelConfirmationDialog()
        }
    }

    private fun updateUI() {
        // Update booking ID
        tvBookingId.text = "Booking ID: $appointmentId"
        
        // Update status header based on appointment status
        updateStatusHeader()
        
        // Update timeline based on status
        updateTimeline()
        
        // Update doctor info
        tvDoctorName.text = doctorName
        tvDoctorSpecialty.text = doctorSpecialty
        
        // Update visit details
        tvVisitDate.text = appointmentDate
        tvVisitTime.text = appointmentTime
        tvVisitLocation.text = locationName
        tvVisitAddress.text = locationAddress
        
        // Update action buttons visibility based on status
        updateActionButtons()
    }

    private fun updateStatusHeader() {
        when (appointmentStatus) {
            AppointmentStatusType.BOOKED -> {
                tvStatusTitle.text = "Appointment Booked"
                tvStatusSubtitle.text = "Waiting for confirmation from the clinic"
                cardStatusHeader.setCardBackgroundColor(getColor(R.color.status_pending))
            }
            AppointmentStatusType.CONFIRMED -> {
                tvStatusTitle.text = "Appointment Confirmed"
                tvStatusSubtitle.text = "Your appointment has been confirmed"
                cardStatusHeader.setCardBackgroundColor(getColor(R.color.docease_primary))
            }
            AppointmentStatusType.COMPLETED -> {
                tvStatusTitle.text = "Visit Completed"
                tvStatusSubtitle.text = "Thank you for your visit"
                cardStatusHeader.setCardBackgroundColor(getColor(R.color.status_done))
            }
            AppointmentStatusType.CANCELLED -> {
                tvStatusTitle.text = "Appointment Cancelled"
                tvStatusSubtitle.text = "This appointment has been cancelled"
                cardStatusHeader.setCardBackgroundColor(getColor(R.color.status_cancelled))
            }
        }
    }

    private fun updateTimeline() {
        when (appointmentStatus) {
            AppointmentStatusType.BOOKED -> {
                // Step 1 is current
                timelineCircle1.setBackgroundResource(R.drawable.bg_timeline_circle_current)
                timelineCircle2.setBackgroundResource(R.drawable.bg_timeline_circle_pending)
                timelineCircle3.setBackgroundResource(R.drawable.bg_timeline_circle_pending)
                timelineLine1.setBackgroundResource(R.drawable.bg_timeline_line_pending)
                timelineLine2.setBackgroundResource(R.drawable.bg_timeline_line_pending)
                tvStep1Title.setTextColor(getColor(R.color.docease_primary))
                tvStep2Title.setTextColor(getColor(R.color.docease_text_secondary))
                tvStep3Title.setTextColor(getColor(R.color.docease_text_secondary))
            }
            AppointmentStatusType.CONFIRMED -> {
                // Step 1 completed, Step 2 current
                timelineCircle1.setBackgroundResource(R.drawable.bg_timeline_circle_completed)
                timelineCircle2.setBackgroundResource(R.drawable.bg_timeline_circle_current)
                timelineCircle3.setBackgroundResource(R.drawable.bg_timeline_circle_pending)
                timelineLine1.setBackgroundResource(R.drawable.bg_timeline_line_completed)
                timelineLine2.setBackgroundResource(R.drawable.bg_timeline_line_pending)
                tvStep1Title.setTextColor(getColor(R.color.docease_text_primary))
                tvStep2Title.setTextColor(getColor(R.color.docease_primary))
                tvStep3Title.setTextColor(getColor(R.color.docease_text_secondary))
            }
            AppointmentStatusType.COMPLETED -> {
                // All steps completed
                timelineCircle1.setBackgroundResource(R.drawable.bg_timeline_circle_completed)
                timelineCircle2.setBackgroundResource(R.drawable.bg_timeline_circle_completed)
                timelineCircle3.setBackgroundResource(R.drawable.bg_timeline_circle_completed)
                timelineLine1.setBackgroundResource(R.drawable.bg_timeline_line_completed)
                timelineLine2.setBackgroundResource(R.drawable.bg_timeline_line_completed)
                tvStep1Title.setTextColor(getColor(R.color.docease_text_primary))
                tvStep2Title.setTextColor(getColor(R.color.docease_text_primary))
                tvStep3Title.setTextColor(getColor(R.color.docease_text_primary))
            }
            AppointmentStatusType.CANCELLED -> {
                // Show cancelled state
                timelineCircle1.setBackgroundResource(R.drawable.bg_timeline_circle_completed)
                timelineCircle2.setBackgroundResource(R.drawable.bg_timeline_circle_pending)
                timelineCircle3.setBackgroundResource(R.drawable.bg_timeline_circle_pending)
                timelineLine1.setBackgroundResource(R.drawable.bg_timeline_line_pending)
                timelineLine2.setBackgroundResource(R.drawable.bg_timeline_line_pending)
            }
        }
    }

    private fun updateActionButtons() {
        when (appointmentStatus) {
            AppointmentStatusType.BOOKED, AppointmentStatusType.CONFIRMED -> {
                btnReschedule.visibility = View.VISIBLE
                btnCancelAppointment.visibility = View.VISIBLE
            }
            AppointmentStatusType.COMPLETED, AppointmentStatusType.CANCELLED -> {
                btnReschedule.visibility = View.GONE
                btnCancelAppointment.visibility = View.GONE
            }
        }
    }

    private fun openChatWithDoctor() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Opening chat with $doctorName",
            Snackbar.LENGTH_SHORT
        ).show()
        
        // In a real app, this would navigate to a chat screen
        // val intent = Intent(this, ChatActivity::class.java)
        // intent.putExtra("doctor_id", doctorId)
        // startActivity(intent)
    }

    private fun openMapsNavigation() {
        // Create a geo intent for navigation
        val geoUri = "geo:0,0?q=${Uri.encode(locationAddress)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
        
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // Fallback to Google Maps web
            val mapsUrl = "https://www.google.com/maps/search/?api=1&query=${Uri.encode(locationAddress)}"
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl))
            startActivity(webIntent)
        }
    }

    private fun showRescheduleOptions() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Navigating to reschedule screen",
            Snackbar.LENGTH_SHORT
        ).show()
        
        // In a real app, this would navigate to a reschedule screen
        // val intent = Intent(this, RescheduleAppointmentActivity::class.java)
        // intent.putExtra(EXTRA_APPOINTMENT_ID, appointmentId)
        // startActivity(intent)
    }

    private fun showCancelConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cancel Appointment")
            .setMessage("Are you sure you want to cancel this appointment with $doctorName on $appointmentDate?")
            .setPositiveButton("Yes, Cancel") { dialog, _ ->
                cancelAppointment()
                dialog.dismiss()
            }
            .setNegativeButton("No, Keep It") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun cancelAppointment() {
        // In a real app, this would make an API call to cancel the appointment
        appointmentStatus = AppointmentStatusType.CANCELLED
        updateUI()
        
        Snackbar.make(
            findViewById(android.R.id.content),
            "Appointment cancelled successfully",
            Snackbar.LENGTH_LONG
        ).show()
    }

    /**
     * Enum representing possible appointment statuses
     */
    enum class AppointmentStatusType {
        BOOKED,
        CONFIRMED,
        COMPLETED,
        CANCELLED;

        companion object {
            fun fromString(value: String): AppointmentStatusType {
                return try {
                    valueOf(value.uppercase())
                } catch (e: IllegalArgumentException) {
                    CONFIRMED
                }
            }
        }
    }

    companion object {
        const val EXTRA_APPOINTMENT_ID = "extra_appointment_id"
        const val EXTRA_DOCTOR_NAME = "extra_doctor_name"
        const val EXTRA_DOCTOR_SPECIALTY = "extra_doctor_specialty"
        const val EXTRA_APPOINTMENT_DATE = "extra_appointment_date"
        const val EXTRA_APPOINTMENT_TIME = "extra_appointment_time"
        const val EXTRA_LOCATION_NAME = "extra_location_name"
        const val EXTRA_LOCATION_ADDRESS = "extra_location_address"
        const val EXTRA_STATUS = "extra_status"
    }
}
