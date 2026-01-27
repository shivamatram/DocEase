package com.example.docease.ui.doctor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R
import com.example.docease.ui.booking.BookAppointmentActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Doctor Details Activity
 * Displays comprehensive doctor profile with booking functionality
 * including date selection and time slot picking.
 */
class DoctorDetailsActivity : AppCompatActivity() {

    // UI Components - Top Bar
    private lateinit var rootLayout: View
    private lateinit var btnBack: ImageView
    private lateinit var btnFavorite: ImageView

    // UI Components - Doctor Profile
    private lateinit var ivDoctorPhoto: ShapeableImageView
    private lateinit var viewOnlineStatus: View
    private lateinit var tvDoctorName: TextView
    private lateinit var ivVerifiedBadge: ImageView
    private lateinit var tvSpecialty: TextView
    private lateinit var tvLocation: TextView

    // UI Components - Action Buttons
    private lateinit var btnCall: LinearLayout
    private lateinit var btnMessage: LinearLayout
    private lateinit var btnVideo: LinearLayout

    // UI Components - Stats
    private lateinit var tvPatientsCount: TextView
    private lateinit var tvExperience: TextView
    private lateinit var tvRating: TextView

    // UI Components - About
    private lateinit var tvAboutDoctor: TextView
    private lateinit var tvReadMore: TextView

    // UI Components - Schedule
    private lateinit var tvSelectedMonth: TextView
    private lateinit var monthSelector: LinearLayout
    private lateinit var rvDateCards: RecyclerView

    // UI Components - Time Slots
    private lateinit var rvTimeSlots: RecyclerView

    // UI Components - Booking Footer
    private lateinit var tvPrice: TextView
    private lateinit var btnBookAppointment: MaterialButton

    // Adapters
    private lateinit var dateAdapter: DateCardAdapter
    private lateinit var timeSlotAdapter: TimeSlotBookingAdapter

    // Data
    private val dateList = mutableListOf<DateCardItem>()
    private val timeSlotList = mutableListOf<TimeSlotItem>()

    // State
    private var isFavorite = false
    private var isAboutExpanded = false
    private var selectedDatePosition = 0
    private var selectedTimePosition = -1

    // Doctor data (would normally come from intent/ViewModel)
    private var doctorId = ""
    private var doctorName = "Dr. Marcus Williams"
    private var specialty = "Cardiologist"
    private var location = "New York, USA"
    private var consultationFee = 40.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_doctor_details)

        // Get data from intent
        extractIntentData()

        // Initialize UI components
        initViews()

        // Apply window insets
        setupWindowInsets()

        // Populate data
        populateDoctorInfo()
        setupDateCards()
        setupTimeSlots()

        // Setup click listeners
        setupClickListeners()
    }

    private fun extractIntentData() {
        intent?.let {
            doctorId = it.getStringExtra("DOCTOR_ID") ?: ""
            doctorName = it.getStringExtra("DOCTOR_NAME") ?: doctorName
            specialty = it.getStringExtra("DOCTOR_SPECIALTY") ?: specialty
            location = it.getStringExtra("DOCTOR_LOCATION") ?: location
            consultationFee = it.getDoubleExtra("CONSULTATION_FEE", consultationFee)
            isFavorite = it.getBooleanExtra("IS_FAVORITE", false)
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Only apply top padding since we handle bottom with the fixed footer
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun initViews() {
        rootLayout = findViewById(R.id.rootLayout)

        // Top Bar
        btnBack = findViewById(R.id.btnBack)
        btnFavorite = findViewById(R.id.btnFavorite)

        // Doctor Profile
        ivDoctorPhoto = findViewById(R.id.ivDoctorPhoto)
        viewOnlineStatus = findViewById(R.id.viewOnlineStatus)
        tvDoctorName = findViewById(R.id.tvDoctorName)
        ivVerifiedBadge = findViewById(R.id.ivVerifiedBadge)
        tvSpecialty = findViewById(R.id.tvSpecialty)
        tvLocation = findViewById(R.id.tvLocation)

        // Action Buttons
        btnCall = findViewById(R.id.btnCall)
        btnMessage = findViewById(R.id.btnMessage)
        btnVideo = findViewById(R.id.btnVideo)

        // Stats
        tvPatientsCount = findViewById(R.id.tvPatientsCount)
        tvExperience = findViewById(R.id.tvExperience)
        tvRating = findViewById(R.id.tvRating)

        // About
        tvAboutDoctor = findViewById(R.id.tvAboutDoctor)
        tvReadMore = findViewById(R.id.tvReadMore)

        // Schedule
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth)
        monthSelector = findViewById(R.id.monthSelector)
        rvDateCards = findViewById(R.id.rvDateCards)

        // Time Slots
        rvTimeSlots = findViewById(R.id.rvTimeSlots)

        // Booking Footer
        tvPrice = findViewById(R.id.tvPrice)
        btnBookAppointment = findViewById(R.id.btnBookAppointment)

        // Set coordinator layout as root for Snackbars
        rootLayout = findViewById(android.R.id.content)
    }

    private fun populateDoctorInfo() {
        // Set doctor details
        tvDoctorName.text = doctorName
        tvSpecialty.text = specialty
        tvLocation.text = location
        tvPrice.text = String.format("$%.2f", consultationFee)

        // Update favorite button state
        updateFavoriteButton()

        // Set stats (sample data - would come from backend)
        tvPatientsCount.text = "500+"
        tvExperience.text = "10+"
        tvRating.text = "4.8"

        // Set about text (sample data)
        tvAboutDoctor.text = "Dr. $doctorName is a highly experienced $specialty with over 10 years of medical practice. Specializing in cardiovascular diseases, heart surgery, and preventive cardiology. Known for patient-centered care and innovative treatment approaches."

        // Set current month
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        tvSelectedMonth.text = monthFormat.format(calendar.time)
    }

    private fun setupDateCards() {
        // Generate dates for the next 14 days
        dateList.clear()
        val calendar = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("d", Locale.getDefault())

        for (i in 0 until 14) {
            val dayOfWeek = dayFormat.format(calendar.time)
            val dayNumber = dateFormat.format(calendar.time)

            dateList.add(
                DateCardItem(
                    id = i,
                    dayOfWeek = dayOfWeek,
                    dayNumber = dayNumber,
                    fullDate = calendar.time,
                    isSelected = i == 0, // First date is selected by default
                    isAvailable = i != 5 && i != 6 // Mark some days as unavailable (weekends example)
                )
            )

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Setup adapter
        dateAdapter = DateCardAdapter(dateList) { item, position ->
            onDateCardClick(item, position)
        }

        rvDateCards.apply {
            layoutManager = LinearLayoutManager(
                this@DoctorDetailsActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = dateAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupTimeSlots() {
        // Generate time slots (9 AM to 6 PM)
        timeSlotList.clear()
        val times = listOf(
            "09:00 AM", "09:30 AM", "10:00 AM",
            "10:30 AM", "11:00 AM", "11:30 AM",
            "02:00 PM", "02:30 PM", "03:00 PM",
            "03:30 PM", "04:00 PM", "04:30 PM",
            "05:00 PM", "05:30 PM", "06:00 PM"
        )

        // Mark some slots as unavailable (booked)
        val unavailableSlots = setOf(2, 5, 8, 11) // Sample booked slots

        times.forEachIndexed { index, time ->
            timeSlotList.add(
                TimeSlotItem(
                    id = index,
                    time = time,
                    isSelected = false,
                    isAvailable = !unavailableSlots.contains(index)
                )
            )
        }

        // Setup adapter
        timeSlotAdapter = TimeSlotBookingAdapter(timeSlotList) { item, position ->
            onTimeSlotClick(item, position)
        }

        rvTimeSlots.apply {
            layoutManager = GridLayoutManager(this@DoctorDetailsActivity, 3)
            adapter = timeSlotAdapter
            setHasFixedSize(false)
        }
    }

    private fun setupClickListeners() {
        // Back button
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Favorite button
        btnFavorite.setOnClickListener {
            toggleFavorite()
        }

        // Action buttons
        btnCall.setOnClickListener {
            showSnackbar("Calling Dr. $doctorName...")
            // TODO: Implement phone call functionality
        }

        btnMessage.setOnClickListener {
            showSnackbar("Opening chat with Dr. $doctorName...")
            // TODO: Navigate to chat screen
        }

        btnVideo.setOnClickListener {
            showSnackbar("Starting video call with Dr. $doctorName...")
            // TODO: Implement video call functionality
        }

        // Read more
        tvReadMore.setOnClickListener {
            toggleAboutSection()
        }

        // Month selector
        monthSelector.setOnClickListener {
            showMonthPicker()
        }

        // Book appointment button
        btnBookAppointment.setOnClickListener {
            onBookAppointmentClick()
        }
    }

    private fun onDateCardClick(item: DateCardItem, position: Int) {
        if (!item.isAvailable) {
            showSnackbar("This date is not available")
            return
        }

        // Update selection
        val previousPosition = selectedDatePosition
        selectedDatePosition = position

        // Update the list
        dateList.forEachIndexed { index, dateItem ->
            dateItem.isSelected = index == position
        }

        // Notify adapter
        dateAdapter.notifyItemChanged(previousPosition)
        dateAdapter.notifyItemChanged(position)

        // Reset time selection and refresh time slots for new date
        selectedTimePosition = -1
        refreshTimeSlots()
    }

    private fun onTimeSlotClick(item: TimeSlotItem, position: Int) {
        if (!item.isAvailable) {
            showSnackbar("This time slot is already booked")
            return
        }

        // Update selection
        val previousPosition = selectedTimePosition
        selectedTimePosition = position

        // Update the list
        timeSlotList.forEachIndexed { index, slotItem ->
            slotItem.isSelected = index == position
        }

        // Notify adapter
        if (previousPosition >= 0) {
            timeSlotAdapter.notifyItemChanged(previousPosition)
        }
        timeSlotAdapter.notifyItemChanged(position)
    }

    private fun refreshTimeSlots() {
        // In real app, fetch available slots for selected date from backend
        // For now, just reset selections
        timeSlotList.forEach { it.isSelected = false }
        timeSlotAdapter.notifyDataSetChanged()
    }

    private fun toggleFavorite() {
        isFavorite = !isFavorite
        updateFavoriteButton()

        val message = if (isFavorite) {
            "Added to favorites"
        } else {
            "Removed from favorites"
        }
        showSnackbar(message)
    }

    private fun updateFavoriteButton() {
        val icon = if (isFavorite) {
            R.drawable.ic_heart_filled
        } else {
            R.drawable.ic_heart_outline
        }
        btnFavorite.setImageResource(icon)

        val tint = if (isFavorite) {
            ContextCompat.getColor(this, R.color.docease_favorite_red)
        } else {
            ContextCompat.getColor(this, R.color.docease_text_primary)
        }
        btnFavorite.setColorFilter(tint)
    }

    private fun toggleAboutSection() {
        isAboutExpanded = !isAboutExpanded

        if (isAboutExpanded) {
            tvAboutDoctor.maxLines = Integer.MAX_VALUE
            tvReadMore.text = "Read less"
        } else {
            tvAboutDoctor.maxLines = 3
            tvReadMore.text = "Read more"
        }
    }

    private fun showMonthPicker() {
        // Simple month picker - in production use a proper DatePicker
        val months = arrayOf(
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"
        )

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Month")
            .setItems(months) { _, which ->
                tvSelectedMonth.text = months[which]
                // Regenerate dates for selected month
                regenerateDatesForMonth(which)
            }
            .show()
    }

    private fun regenerateDatesForMonth(month: Int) {
        dateList.clear()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("d", Locale.getDefault())
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in 0 until daysInMonth) {
            val dayOfWeek = dayFormat.format(calendar.time)
            val dayNumber = dateFormat.format(calendar.time)

            // Determine if weekend (unavailable)
            val dayOfWeekInt = calendar.get(Calendar.DAY_OF_WEEK)
            val isWeekend = dayOfWeekInt == Calendar.SATURDAY || dayOfWeekInt == Calendar.SUNDAY

            dateList.add(
                DateCardItem(
                    id = i,
                    dayOfWeek = dayOfWeek,
                    dayNumber = dayNumber,
                    fullDate = calendar.time,
                    isSelected = i == 0,
                    isAvailable = !isWeekend
                )
            )

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        selectedDatePosition = 0
        dateAdapter.notifyDataSetChanged()
        rvDateCards.scrollToPosition(0)
    }

    private fun onBookAppointmentClick() {
        // Navigate to Book Appointment screen
        val intent = Intent(this, BookAppointmentActivity::class.java).apply {
            putExtra(BookAppointmentActivity.EXTRA_DOCTOR_ID, doctorId)
            putExtra(BookAppointmentActivity.EXTRA_DOCTOR_NAME, doctorName)
            putExtra(BookAppointmentActivity.EXTRA_DOCTOR_SPECIALTY, specialty)
            putExtra(BookAppointmentActivity.EXTRA_DOCTOR_RATING, 4.8f)
            putExtra(BookAppointmentActivity.EXTRA_REVIEW_COUNT, 124)
            putExtra(BookAppointmentActivity.EXTRA_CONSULTATION_FEE, consultationFee)
            putExtra(BookAppointmentActivity.EXTRA_IS_FAVORITE, isFavorite)
        }
        startActivity(intent)
    }

    private fun processBooking(date: DateCardItem, time: TimeSlotItem) {
        // In real app, send booking request to backend
        showSnackbar("Booking confirmed! You will receive a confirmation shortly.")

        // TODO: Navigate to confirmation screen or appointments list
        // finish()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_DOCTOR_ID = "DOCTOR_ID"
        const val EXTRA_DOCTOR_NAME = "DOCTOR_NAME"
        const val EXTRA_DOCTOR_SPECIALTY = "DOCTOR_SPECIALTY"
        const val EXTRA_DOCTOR_LOCATION = "DOCTOR_LOCATION"
        const val EXTRA_CONSULTATION_FEE = "CONSULTATION_FEE"
        const val EXTRA_IS_FAVORITE = "IS_FAVORITE"
    }
}

/**
 * Data class for date card items
 */
data class DateCardItem(
    val id: Int,
    val dayOfWeek: String,
    val dayNumber: String,
    val fullDate: java.util.Date,
    var isSelected: Boolean = false,
    val isAvailable: Boolean = true
)

/**
 * Data class for time slot items
 */
data class TimeSlotItem(
    val id: Int,
    val time: String,
    var isSelected: Boolean = false,
    val isAvailable: Boolean = true
)
