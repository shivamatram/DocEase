package com.example.docease.ui.booking

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Book Appointment Activity
 * Allows patients to select date, time, add notes, and confirm appointments
 */
class BookAppointmentActivity : AppCompatActivity() {

    // UI Components - Top Bar
    private lateinit var rootLayout: View
    private lateinit var btnBack: ImageView

    // UI Components - Doctor Summary
    private lateinit var doctorSummaryCard: MaterialCardView
    private lateinit var ivDoctorPhoto: ShapeableImageView
    private lateinit var viewOnlineStatus: View
    private lateinit var tvDoctorName: TextView
    private lateinit var tvSpecialty: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvReviews: TextView
    private lateinit var btnFavorite: ImageView

    // UI Components - Date Selection
    private lateinit var tvSelectedMonth: TextView
    private lateinit var monthSelector: LinearLayout
    private lateinit var rvDateChips: RecyclerView

    // UI Components - Time Selection
    private lateinit var rvTimeChips: RecyclerView

    // UI Components - Note
    private lateinit var etNoteToDoctor: EditText

    // UI Components - Payment
    private lateinit var tvConsultationFee: TextView
    private lateinit var tvAdminFee: TextView
    private lateinit var tvTotalPayment: TextView

    // UI Components - Confirm Button
    private lateinit var btnConfirmAppointment: MaterialButton

    // Adapters
    private lateinit var dateChipAdapter: BookingDateChipAdapter
    private lateinit var timeChipAdapter: BookingTimeChipAdapter

    // Data
    private val dateList = mutableListOf<BookingDateItem>()
    private val timeList = mutableListOf<BookingTimeItem>()

    // State
    private var isFavorite = false
    private var selectedDatePosition = 0
    private var selectedTimePosition = -1
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)

    // Doctor data (from intent)
    private var doctorId = ""
    private var doctorName = "Dr. Sarah Johnson"
    private var specialty = "Cardiologist"
    private var rating = 4.8f
    private var reviewCount = 124

    // Pricing
    private var consultationFee = 60.00
    private var adminFee = 2.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_book_appointment)

        // Extract data from intent
        extractIntentData()

        // Initialize UI components
        initViews()

        // Apply window insets
        setupWindowInsets()

        // Populate data
        populateDoctorInfo()
        setupDateChips()
        setupTimeChips()
        updatePaymentSummary()

        // Setup click listeners
        setupClickListeners()
    }

    private fun extractIntentData() {
        intent?.let {
            doctorId = it.getStringExtra(EXTRA_DOCTOR_ID) ?: ""
            doctorName = it.getStringExtra(EXTRA_DOCTOR_NAME) ?: doctorName
            specialty = it.getStringExtra(EXTRA_DOCTOR_SPECIALTY) ?: specialty
            rating = it.getFloatExtra(EXTRA_DOCTOR_RATING, rating)
            reviewCount = it.getIntExtra(EXTRA_REVIEW_COUNT, reviewCount)
            consultationFee = it.getDoubleExtra(EXTRA_CONSULTATION_FEE, consultationFee)
            isFavorite = it.getBooleanExtra(EXTRA_IS_FAVORITE, false)
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun initViews() {
        rootLayout = findViewById(R.id.rootLayout)
        btnBack = findViewById(R.id.btnBack)

        // Doctor Summary
        doctorSummaryCard = findViewById(R.id.doctorSummaryCard)
        ivDoctorPhoto = findViewById(R.id.ivDoctorPhoto)
        viewOnlineStatus = findViewById(R.id.viewOnlineStatus)
        tvDoctorName = findViewById(R.id.tvDoctorName)
        tvSpecialty = findViewById(R.id.tvSpecialty)
        tvRating = findViewById(R.id.tvRating)
        tvReviews = findViewById(R.id.tvReviews)
        btnFavorite = findViewById(R.id.btnFavorite)

        // Date Selection
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth)
        monthSelector = findViewById(R.id.monthSelector)
        rvDateChips = findViewById(R.id.rvDateChips)

        // Time Selection
        rvTimeChips = findViewById(R.id.rvTimeChips)

        // Note
        etNoteToDoctor = findViewById(R.id.etNoteToDoctor)

        // Payment
        tvConsultationFee = findViewById(R.id.tvConsultationFee)
        tvAdminFee = findViewById(R.id.tvAdminFee)
        tvTotalPayment = findViewById(R.id.tvTotalPayment)

        // Confirm Button
        btnConfirmAppointment = findViewById(R.id.btnConfirmAppointment)
    }

    private fun populateDoctorInfo() {
        tvDoctorName.text = doctorName
        tvSpecialty.text = specialty
        tvRating.text = String.format("%.1f", rating)
        tvReviews.text = "($reviewCount reviews)"

        // Update favorite icon
        updateFavoriteIcon()
    }

    private fun setupDateChips() {
        dateList.clear()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, currentMonth)
        calendar.set(Calendar.YEAR, currentYear)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        // Move to today if in current month
        val today = Calendar.getInstance()
        if (currentMonth == today.get(Calendar.MONTH) && currentYear == today.get(Calendar.YEAR)) {
            calendar.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))
        }

        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("d", Locale.getDefault())

        // Generate next 14 days
        for (i in 0 until 14) {
            val dayOfWeek = dayFormat.format(calendar.time).uppercase()
            val dayNumber = dateFormat.format(calendar.time)

            // Check if today
            val isToday = isSameDay(calendar, today)
            val label = if (isToday) "TODAY" else dayOfWeek

            dateList.add(
                BookingDateItem(
                    id = i,
                    dayLabel = label,
                    dayNumber = dayNumber,
                    fullDate = calendar.time,
                    isSelected = i == 0,
                    isAvailable = true
                )
            )

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Update month display
        updateMonthDisplay()

        // Setup adapter
        dateChipAdapter = BookingDateChipAdapter(dateList) { item, position ->
            onDateChipClick(item, position)
        }

        rvDateChips.apply {
            layoutManager = LinearLayoutManager(
                this@BookAppointmentActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = dateChipAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupTimeChips() {
        timeList.clear()

        // Generate time slots from 9 AM to 5 PM
        val times = listOf(
            "09:00 AM", "09:30 AM", "10:00 AM",
            "10:30 AM", "11:00 AM", "11:30 AM",
            "02:00 PM", "02:30 PM", "03:00 PM",
            "03:30 PM", "04:00 PM", "04:30 PM"
        )

        // Mark some as unavailable (example: 11:00 AM is booked)
        val unavailableSlots = setOf(4) // 11:00 AM

        times.forEachIndexed { index, time ->
            timeList.add(
                BookingTimeItem(
                    id = index,
                    time = time,
                    isSelected = false,
                    isAvailable = !unavailableSlots.contains(index)
                )
            )
        }

        // Setup adapter
        timeChipAdapter = BookingTimeChipAdapter(timeList) { item, position ->
            onTimeChipClick(item, position)
        }

        rvTimeChips.apply {
            layoutManager = GridLayoutManager(this@BookAppointmentActivity, 3)
            adapter = timeChipAdapter
            setHasFixedSize(false)
        }
    }

    private fun updatePaymentSummary() {
        tvConsultationFee.text = String.format("$%.2f", consultationFee)
        tvAdminFee.text = String.format("$%.2f", adminFee)

        val total = consultationFee + adminFee
        tvTotalPayment.text = String.format("$%.2f", total)
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

        // Month selector
        monthSelector.setOnClickListener {
            showMonthPicker()
        }

        // Confirm appointment
        btnConfirmAppointment.setOnClickListener {
            onConfirmAppointmentClick()
        }
    }

    private fun onDateChipClick(item: BookingDateItem, position: Int) {
        if (!item.isAvailable) {
            showSnackbar("This date is not available")
            return
        }

        // Update selection
        val previousPosition = selectedDatePosition
        selectedDatePosition = position

        dateList.forEachIndexed { index, dateItem ->
            dateItem.isSelected = index == position
        }

        dateChipAdapter.notifyItemChanged(previousPosition)
        dateChipAdapter.notifyItemChanged(position)

        // Refresh time slots for new date
        refreshTimeSlotsForDate(item)
    }

    private fun onTimeChipClick(item: BookingTimeItem, position: Int) {
        if (!item.isAvailable) {
            showSnackbar("This time slot is not available")
            return
        }

        // Update selection
        val previousPosition = selectedTimePosition
        selectedTimePosition = position

        timeList.forEachIndexed { index, timeItem ->
            timeItem.isSelected = index == position
        }

        if (previousPosition >= 0) {
            timeChipAdapter.notifyItemChanged(previousPosition)
        }
        timeChipAdapter.notifyItemChanged(position)
    }

    private fun refreshTimeSlotsForDate(date: BookingDateItem) {
        // In real app, fetch available slots from backend for selected date
        // For demo, reset selections and randomize availability
        selectedTimePosition = -1

        timeList.forEach { it.isSelected = false }
        timeChipAdapter.notifyDataSetChanged()
    }

    private fun toggleFavorite() {
        isFavorite = !isFavorite
        updateFavoriteIcon()

        val message = if (isFavorite) "Added to favorites" else "Removed from favorites"
        showSnackbar(message)
    }

    private fun updateFavoriteIcon() {
        val icon = if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        btnFavorite.setImageResource(icon)

        val tint = if (isFavorite) {
            ContextCompat.getColor(this, R.color.docease_favorite_red)
        } else {
            ContextCompat.getColor(this, R.color.docease_text_secondary)
        }
        btnFavorite.setColorFilter(tint)
    }

    private fun updateMonthDisplay() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, currentMonth)
        calendar.set(Calendar.YEAR, currentYear)

        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvSelectedMonth.text = monthFormat.format(calendar.time)
    }

    private fun showMonthPicker() {
        val months = arrayOf(
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"
        )

        AlertDialog.Builder(this)
            .setTitle("Select Month")
            .setItems(months) { _, which ->
                currentMonth = which
                setupDateChips()
            }
            .show()
    }

    private fun onConfirmAppointmentClick() {
        // Validate selection
        if (selectedTimePosition < 0) {
            showSnackbar("Please select a time slot")
            return
        }

        val selectedDate = dateList[selectedDatePosition]
        val selectedTime = timeList[selectedTimePosition]
        val note = etNoteToDoctor.text.toString().trim()

        // Format confirmation message
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(selectedDate.fullDate)
        val totalPayment = consultationFee + adminFee

        val message = StringBuilder().apply {
            append("Doctor: $doctorName\n")
            append("Date: $formattedDate\n")
            append("Time: ${selectedTime.time}\n")
            append("Total: $${String.format("%.2f", totalPayment)}\n")
            if (note.isNotEmpty()) {
                append("\nNote: $note")
            }
        }.toString()

        AlertDialog.Builder(this)
            .setTitle("Confirm Appointment")
            .setMessage(message)
            .setPositiveButton("Confirm") { _, _ ->
                processAppointmentBooking(selectedDate, selectedTime, note)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun processAppointmentBooking(
        date: BookingDateItem,
        time: BookingTimeItem,
        note: String
    ) {
        // In real app, send booking request to backend
        showSnackbar("Appointment booked successfully!")

        // TODO: Navigate to confirmation screen or payment flow
        // finish()
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_DOCTOR_ID = "DOCTOR_ID"
        const val EXTRA_DOCTOR_NAME = "DOCTOR_NAME"
        const val EXTRA_DOCTOR_SPECIALTY = "DOCTOR_SPECIALTY"
        const val EXTRA_DOCTOR_RATING = "DOCTOR_RATING"
        const val EXTRA_REVIEW_COUNT = "REVIEW_COUNT"
        const val EXTRA_CONSULTATION_FEE = "CONSULTATION_FEE"
        const val EXTRA_IS_FAVORITE = "IS_FAVORITE"
    }
}

/**
 * Data class for booking date chip items
 */
data class BookingDateItem(
    val id: Int,
    val dayLabel: String,
    val dayNumber: String,
    val fullDate: Date,
    var isSelected: Boolean = false,
    val isAvailable: Boolean = true
)

/**
 * Data class for booking time chip items
 */
data class BookingTimeItem(
    val id: Int,
    val time: String,
    var isSelected: Boolean = false,
    val isAvailable: Boolean = true
)
