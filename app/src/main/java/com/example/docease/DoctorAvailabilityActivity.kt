package com.example.docease

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.adapters.CalendarDay
import com.example.docease.adapters.CalendarDayAdapter
import com.example.docease.adapters.DayStatus
import com.example.docease.adapters.Session
import com.example.docease.adapters.SlotStatus
import com.example.docease.adapters.TimeSlot
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * DoctorAvailabilityActivity allows doctors to manage their schedule
 * - View and navigate through calendar
 * - Select available time slots
 * - Mark days off
 * - Copy previous day's schedule
 * - Add breaks
 * - Save availability to Firebase
 */
class DoctorAvailabilityActivity : AppCompatActivity() {

    // Views
    private lateinit var btnBack: ImageButton
    private lateinit var btnSave: TextView
    private lateinit var tvCurrentMonth: TextView
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton
    private lateinit var rvCalendarDays: RecyclerView
    private lateinit var btnMarkDayOff: LinearLayout
    private lateinit var btnCopyPrev: LinearLayout
    private lateinit var btnAddBreak: LinearLayout
    private lateinit var morningSlots: FlexboxLayout
    private lateinit var afternoonSlots: FlexboxLayout
    private lateinit var eveningSlots: FlexboxLayout
    private lateinit var eveningEmptyState: LinearLayout
    private lateinit var coffeeBreakCard: LinearLayout
    private lateinit var btnAddEveningSlot: TextView
    private lateinit var fabAddSlot: FloatingActionButton

    // Adapters
    private lateinit var calendarAdapter: CalendarDayAdapter

    // Data
    private val currentCalendar = Calendar.getInstance()
    private var selectedDate: String = ""
    private val availabilityData = mutableMapOf<String, DayAvailability>()
    private var hasUnsavedChanges = false

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_doctor_availability)

        initViews()
        setupCalendar()
        setupClickListeners()
        loadAvailabilityData()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnSave = findViewById(R.id.btnSave)
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        rvCalendarDays = findViewById(R.id.rvCalendarDays)
        btnMarkDayOff = findViewById(R.id.btnMarkDayOff)
        btnCopyPrev = findViewById(R.id.btnCopyPrev)
        btnAddBreak = findViewById(R.id.btnAddBreak)
        morningSlots = findViewById(R.id.morningSlots)
        afternoonSlots = findViewById(R.id.afternoonSlots)
        eveningSlots = findViewById(R.id.eveningSlots)
        eveningEmptyState = findViewById(R.id.eveningEmptyState)
        coffeeBreakCard = findViewById(R.id.coffeeBreakCard)
        btnAddEveningSlot = findViewById(R.id.btnAddEveningSlot)
        fabAddSlot = findViewById(R.id.fabAddSlot)

        // Set today as initially selected
        selectedDate = dateFormat.format(Date())
    }

    private fun setupCalendar() {
        calendarAdapter = CalendarDayAdapter { day ->
            selectedDate = day.date
            loadSlotsForDate(selectedDate)
        }

        rvCalendarDays.apply {
            layoutManager = GridLayoutManager(this@DoctorAvailabilityActivity, 7)
            adapter = calendarAdapter
            isNestedScrollingEnabled = false
        }

        updateCalendarDisplay()
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            if (hasUnsavedChanges) {
                showUnsavedChangesDialog()
            } else {
                finish()
            }
        }

        btnSave.setOnClickListener {
            saveAvailability()
        }

        btnPrevMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateCalendarDisplay()
        }

        btnNextMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateCalendarDisplay()
        }

        btnMarkDayOff.setOnClickListener {
            markDayOff()
        }

        btnCopyPrev.setOnClickListener {
            copyPreviousDay()
        }

        btnAddBreak.setOnClickListener {
            showAddBreakDialog()
        }

        btnAddEveningSlot.setOnClickListener {
            showAddSlotDialog(Session.EVENING)
        }

        fabAddSlot.setOnClickListener {
            showAddSlotDialog(null)
        }
    }

    private fun updateCalendarDisplay() {
        tvCurrentMonth.text = monthYearFormat.format(currentCalendar.time)
        val days = generateCalendarDays()
        calendarAdapter.submitList(days)
    }

    private fun generateCalendarDays(): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val calendar = currentCalendar.clone() as Calendar
        val today = Calendar.getInstance()
        val todayStr = dateFormat.format(today.time)

        // Set to first day of month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        
        // Get the day of week for the first day (0 = Sunday)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        
        // Add empty cells for days before the first day of the month
        for (i in 0 until firstDayOfWeek) {
            days.add(CalendarDay(0, "", isCurrentMonth = false))
        }

        // Get number of days in month
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // Add days of the month
        for (day in 1..daysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val dateStr = dateFormat.format(calendar.time)
            val isToday = dateStr == todayStr
            val isSelected = dateStr == selectedDate

            // Get status from availability data
            val status = when {
                availabilityData[dateStr]?.isDayOff == true -> DayStatus.NONE
                availabilityData[dateStr]?.hasAvailableSlots() == true -> DayStatus.AVAILABLE
                availabilityData[dateStr]?.hasBookedSlots() == true -> DayStatus.BOOKED
                else -> DayStatus.NONE
            }

            days.add(
                CalendarDay(
                    dayNumber = day,
                    date = dateStr,
                    isSelected = isSelected,
                    isToday = isToday,
                    isCurrentMonth = true,
                    status = status
                )
            )
        }

        return days
    }

    private fun loadAvailabilityData() {
        val doctorId = auth.currentUser?.uid ?: return
        
        database.child("availability").child(doctorId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    availabilityData.clear()
                    for (dateSnapshot in snapshot.children) {
                        val date = dateSnapshot.key ?: continue
                        val dayAvailability = parseDayAvailability(dateSnapshot)
                        availabilityData[date] = dayAvailability
                    }
                    updateCalendarDisplay()
                    loadSlotsForDate(selectedDate)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@DoctorAvailabilityActivity,
                        "Failed to load availability: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun parseDayAvailability(snapshot: DataSnapshot): DayAvailability {
        val isDayOff = snapshot.child("isDayOff").getValue(Boolean::class.java) ?: false
        val slots = mutableListOf<TimeSlot>()
        
        snapshot.child("slots").children.forEach { slotSnapshot ->
            val id = slotSnapshot.child("id").getValue(String::class.java) ?: UUID.randomUUID().toString()
            val time = slotSnapshot.child("time").getValue(String::class.java) ?: ""
            val startTime = slotSnapshot.child("startTime").getValue(Long::class.java) ?: 0L
            val endTime = slotSnapshot.child("endTime").getValue(Long::class.java) ?: 0L
            val statusStr = slotSnapshot.child("status").getValue(String::class.java) ?: "AVAILABLE"
            val sessionStr = slotSnapshot.child("session").getValue(String::class.java) ?: "MORNING"
            val bookedBy = slotSnapshot.child("bookedBy").getValue(String::class.java)
            
            slots.add(
                TimeSlot(
                    id = id,
                    time = time,
                    startTime = startTime,
                    endTime = endTime,
                    status = SlotStatus.valueOf(statusStr),
                    session = Session.valueOf(sessionStr),
                    bookedBy = bookedBy
                )
            )
        }
        
        return DayAvailability(isDayOff, slots)
    }

    private fun loadSlotsForDate(date: String) {
        val dayAvailability = availabilityData[date] ?: DayAvailability()
        
        if (dayAvailability.isDayOff) {
            // Show day off state
            showDayOffState()
            return
        }
        
        displaySlots(dayAvailability.slots)
    }

    private fun displaySlots(slots: List<TimeSlot>) {
        // Clear existing slots
        morningSlots.removeAllViews()
        afternoonSlots.removeAllViews()
        eveningSlots.removeAllViews()

        val morningList = slots.filter { it.session == Session.MORNING }
        val afternoonList = slots.filter { it.session == Session.AFTERNOON }
        val eveningList = slots.filter { it.session == Session.EVENING }

        // Add morning slots
        if (morningList.isEmpty()) {
            addDefaultSlots(morningSlots, Session.MORNING)
        } else {
            morningList.forEach { slot -> addSlotView(morningSlots, slot) }
        }

        // Add afternoon slots
        if (afternoonList.isEmpty()) {
            addDefaultSlots(afternoonSlots, Session.AFTERNOON)
        } else {
            afternoonList.forEach { slot -> addSlotView(afternoonSlots, slot) }
        }

        // Check for break in afternoon
        val hasBreak = afternoonList.any { it.status == SlotStatus.BREAK }
        coffeeBreakCard.visibility = if (hasBreak) View.VISIBLE else View.GONE

        // Add evening slots
        if (eveningList.isEmpty()) {
            eveningSlots.visibility = View.GONE
            eveningEmptyState.visibility = View.VISIBLE
        } else {
            eveningSlots.visibility = View.VISIBLE
            eveningEmptyState.visibility = View.GONE
            eveningList.forEach { slot -> addSlotView(eveningSlots, slot) }
        }
    }

    private fun addDefaultSlots(container: FlexboxLayout, session: Session) {
        val (startHour, endHour) = when (session) {
            Session.MORNING -> 8 to 12
            Session.AFTERNOON -> 13 to 17
            Session.EVENING -> 17 to 20
        }

        for (hour in startHour until endHour) {
            for (minute in listOf(0, 30)) {
                val time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                val slot = TimeSlot(
                    id = UUID.randomUUID().toString(),
                    time = time,
                    startTime = 0,
                    endTime = 0,
                    status = SlotStatus.AVAILABLE,
                    session = session
                )
                addSlotView(container, slot)
            }
        }
    }

    private fun addSlotView(container: FlexboxLayout, slot: TimeSlot) {
        val slotView = LayoutInflater.from(this)
            .inflate(R.layout.item_time_slot, container, false)
        
        val slotContainer = slotView.findViewById<View>(R.id.slotContainer)
        val slotBackground = (slotContainer as ViewGroup).getChildAt(0) as LinearLayout
        val tvSlotTime = slotView.findViewById<TextView>(R.id.tvSlotTime)
        val ivLock = slotView.findViewById<View>(R.id.ivLock)
        
        tvSlotTime.text = slot.time
        
        when (slot.status) {
            SlotStatus.AVAILABLE -> {
                slotBackground.setBackgroundResource(R.drawable.bg_slot_available)
                tvSlotTime.setTextColor(ContextCompat.getColor(this, R.color.docease_primary))
                ivLock.visibility = View.GONE
                slotContainer.alpha = 1f
            }
            SlotStatus.SELECTED -> {
                slotBackground.setBackgroundResource(R.drawable.bg_slot_selected)
                tvSlotTime.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                ivLock.visibility = View.GONE
                slotContainer.alpha = 1f
            }
            SlotStatus.BOOKED -> {
                slotBackground.setBackgroundResource(R.drawable.bg_slot_booked)
                tvSlotTime.setTextColor(ContextCompat.getColor(this, R.color.text_hint))
                ivLock.visibility = View.VISIBLE
                slotContainer.alpha = 0.7f
            }
            SlotStatus.BREAK -> {
                slotBackground.setBackgroundResource(R.drawable.bg_break_indicator)
                tvSlotTime.setTextColor(ContextCompat.getColor(this, R.color.break_color))
                ivLock.visibility = View.GONE
                slotContainer.alpha = 1f
            }
        }
        
        slotView.setOnClickListener {
            if (slot.status != SlotStatus.BOOKED) {
                toggleSlotSelection(slot, slotBackground, tvSlotTime)
            }
        }
        
        container.addView(slotView)
    }

    private fun toggleSlotSelection(slot: TimeSlot, background: LinearLayout, textView: TextView) {
        val dayAvailability = availabilityData.getOrPut(selectedDate) { DayAvailability() }
        val slots = dayAvailability.slots.toMutableList()
        
        val existingIndex = slots.indexOfFirst { it.id == slot.id }
        val newStatus = if (slot.status == SlotStatus.SELECTED) SlotStatus.AVAILABLE else SlotStatus.SELECTED
        
        if (existingIndex >= 0) {
            slots[existingIndex] = slots[existingIndex].copy(status = newStatus)
        } else {
            slots.add(slot.copy(status = newStatus))
        }
        
        availabilityData[selectedDate] = dayAvailability.copy(slots = slots)
        hasUnsavedChanges = true
        
        // Update UI
        if (newStatus == SlotStatus.SELECTED) {
            background.setBackgroundResource(R.drawable.bg_slot_selected)
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        } else {
            background.setBackgroundResource(R.drawable.bg_slot_available)
            textView.setTextColor(ContextCompat.getColor(this, R.color.docease_primary))
        }
    }

    private fun showDayOffState() {
        morningSlots.removeAllViews()
        afternoonSlots.removeAllViews()
        eveningSlots.removeAllViews()
        
        // Add "Day Off" message to each section
        listOf(morningSlots, afternoonSlots).forEach { container ->
            val textView = TextView(this).apply {
                text = "Day Off - No appointments"
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                textSize = 14f
                setPadding(16, 24, 16, 24)
            }
            container.addView(textView)
        }
        
        eveningSlots.visibility = View.GONE
        eveningEmptyState.visibility = View.VISIBLE
    }

    private fun markDayOff() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Mark Day Off")
            .setMessage("This will mark $selectedDate as a day off. Any existing available slots will be removed. Booked appointments will not be affected.")
            .setPositiveButton("Mark Day Off") { _, _ ->
                val dayAvailability = availabilityData.getOrPut(selectedDate) { DayAvailability() }
                val bookedSlots = dayAvailability.slots.filter { it.status == SlotStatus.BOOKED }
                availabilityData[selectedDate] = dayAvailability.copy(
                    isDayOff = true,
                    slots = bookedSlots
                )
                hasUnsavedChanges = true
                showDayOffState()
                Toast.makeText(this, "Day marked as off", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun copyPreviousDay() {
        val calendar = Calendar.getInstance()
        try {
            calendar.time = dateFormat.parse(selectedDate) ?: return
        } catch (e: Exception) {
            return
        }
        
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val previousDate = dateFormat.format(calendar.time)
        val previousAvailability = availabilityData[previousDate]
        
        if (previousAvailability == null || previousAvailability.slots.isEmpty()) {
            Toast.makeText(this, "No schedule found for previous day", Toast.LENGTH_SHORT).show()
            return
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Copy Previous Day")
            .setMessage("Copy schedule from $previousDate to $selectedDate?")
            .setPositiveButton("Copy") { _, _ ->
                // Copy non-booked slots only
                val newSlots = previousAvailability.slots
                    .filter { it.status != SlotStatus.BOOKED }
                    .map { it.copy(id = UUID.randomUUID().toString()) }
                
                availabilityData[selectedDate] = DayAvailability(
                    isDayOff = previousAvailability.isDayOff,
                    slots = newSlots
                )
                hasUnsavedChanges = true
                loadSlotsForDate(selectedDate)
                Toast.makeText(this, "Schedule copied", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddBreakDialog() {
        val sessions = arrayOf("Morning (08:00 - 12:00)", "Afternoon (13:00 - 17:00)", "Evening (17:00 - 20:00)")
        var selectedSession = 1 // Default to afternoon
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Add Break")
            .setSingleChoiceItems(sessions, selectedSession) { _, which ->
                selectedSession = which
            }
            .setPositiveButton("Add Break") { _, _ ->
                val session = when (selectedSession) {
                    0 -> Session.MORNING
                    1 -> Session.AFTERNOON
                    else -> Session.EVENING
                }
                addBreakToSession(session)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addBreakToSession(session: Session) {
        coffeeBreakCard.visibility = View.VISIBLE
        
        val dayAvailability = availabilityData.getOrPut(selectedDate) { DayAvailability() }
        val slots = dayAvailability.slots.toMutableList()
        
        // Add a break slot
        val breakSlot = TimeSlot(
            id = UUID.randomUUID().toString(),
            time = "Break",
            startTime = 0,
            endTime = 0,
            status = SlotStatus.BREAK,
            session = session
        )
        slots.add(breakSlot)
        
        availabilityData[selectedDate] = dayAvailability.copy(slots = slots)
        hasUnsavedChanges = true
        
        Toast.makeText(this, "Break added to ${session.name.lowercase()} session", Toast.LENGTH_SHORT).show()
    }

    private fun showAddSlotDialog(preSelectedSession: Session?) {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_slot, null)
        dialog.setContentView(view)

        val sessionChipGroup = view.findViewById<ChipGroup>(R.id.sessionChipGroup)
        val chipMorning = view.findViewById<Chip>(R.id.chipMorning)
        val chipAfternoon = view.findViewById<Chip>(R.id.chipAfternoon)
        val chipEvening = view.findViewById<Chip>(R.id.chipEvening)
        val startTimeContainer = view.findViewById<LinearLayout>(R.id.startTimeContainer)
        val tvStartTime = view.findViewById<TextView>(R.id.tvStartTime)
        val endTimeContainer = view.findViewById<LinearLayout>(R.id.endTimeContainer)
        val tvEndTime = view.findViewById<TextView>(R.id.tvEndTime)
        val durationChipGroup = view.findViewById<ChipGroup>(R.id.durationChipGroup)
        val btnAddSlot = view.findViewById<MaterialButton>(R.id.btnAddSlot)

        var startHour = 8
        var startMinute = 0
        var endHour = 12
        var endMinute = 0
        var slotDuration = 30

        // Pre-select session if provided
        when (preSelectedSession) {
            Session.MORNING -> chipMorning.isChecked = true
            Session.AFTERNOON -> chipAfternoon.isChecked = true
            Session.EVENING -> chipEvening.isChecked = true
            null -> chipMorning.isChecked = true
        }

        startTimeContainer.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(startHour)
                .setMinute(startMinute)
                .setTitleText("Select Start Time")
                .build()
            
            picker.addOnPositiveButtonClickListener {
                startHour = picker.hour
                startMinute = picker.minute
                tvStartTime.text = String.format("%02d:%02d", startHour, startMinute)
                tvStartTime.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            }
            picker.show(supportFragmentManager, "start_time")
        }

        endTimeContainer.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(endHour)
                .setMinute(endMinute)
                .setTitleText("Select End Time")
                .build()
            
            picker.addOnPositiveButtonClickListener {
                endHour = picker.hour
                endMinute = picker.minute
                tvEndTime.text = String.format("%02d:%02d", endHour, endMinute)
                tvEndTime.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            }
            picker.show(supportFragmentManager, "end_time")
        }

        durationChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            slotDuration = when {
                checkedIds.contains(R.id.chip15Min) -> 15
                checkedIds.contains(R.id.chip30Min) -> 30
                checkedIds.contains(R.id.chip45Min) -> 45
                checkedIds.contains(R.id.chip60Min) -> 60
                else -> 30
            }
        }

        btnAddSlot.setOnClickListener {
            val session = when {
                chipMorning.isChecked -> Session.MORNING
                chipAfternoon.isChecked -> Session.AFTERNOON
                chipEvening.isChecked -> Session.EVENING
                else -> Session.MORNING
            }

            if (tvStartTime.text == "Select start time" || tvEndTime.text == "Select end time") {
                Toast.makeText(this, "Please select start and end times", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addSlotsToSession(session, startHour, startMinute, endHour, endMinute, slotDuration)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addSlotsToSession(
        session: Session,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        duration: Int
    ) {
        val dayAvailability = availabilityData.getOrPut(selectedDate) { DayAvailability() }
        val slots = dayAvailability.slots.toMutableList()
        
        var currentHour = startHour
        var currentMinute = startMinute
        
        while (currentHour < endHour || (currentHour == endHour && currentMinute < endMinute)) {
            val time = String.format(Locale.getDefault(), "%02d:%02d", currentHour, currentMinute)
            
            // Check if slot already exists
            if (slots.none { it.time == time && it.session == session }) {
                slots.add(
                    TimeSlot(
                        id = UUID.randomUUID().toString(),
                        time = time,
                        startTime = 0,
                        endTime = 0,
                        status = SlotStatus.SELECTED,
                        session = session
                    )
                )
            }
            
            // Increment time
            currentMinute += duration
            if (currentMinute >= 60) {
                currentHour += currentMinute / 60
                currentMinute %= 60
            }
        }
        
        availabilityData[selectedDate] = dayAvailability.copy(
            isDayOff = false,
            slots = slots.sortedBy { it.time }
        )
        hasUnsavedChanges = true
        loadSlotsForDate(selectedDate)
        
        Toast.makeText(this, "Slots added successfully", Toast.LENGTH_SHORT).show()
    }

    private fun saveAvailability() {
        val doctorId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        btnSave.isEnabled = false
        btnSave.alpha = 0.5f

        val updates = mutableMapOf<String, Any?>()
        
        availabilityData.forEach { (date, dayAvailability) ->
            val dayMap = mutableMapOf<String, Any?>(
                "isDayOff" to dayAvailability.isDayOff
            )
            
            val slotsMap = mutableMapOf<String, Any>()
            dayAvailability.slots.forEachIndexed { index, slot ->
                slotsMap[index.toString()] = mapOf(
                    "id" to slot.id,
                    "time" to slot.time,
                    "startTime" to slot.startTime,
                    "endTime" to slot.endTime,
                    "status" to slot.status.name,
                    "session" to slot.session.name,
                    "bookedBy" to slot.bookedBy
                )
            }
            dayMap["slots"] = slotsMap
            
            updates["availability/$doctorId/$date"] = dayMap
        }

        database.updateChildren(updates)
            .addOnSuccessListener {
                hasUnsavedChanges = false
                btnSave.isEnabled = true
                btnSave.alpha = 1f
                Toast.makeText(this, "Schedule saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                btnSave.isEnabled = true
                btnSave.alpha = 1f
                Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showUnsavedChangesDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Unsaved Changes")
            .setMessage("You have unsaved changes. Do you want to save them before leaving?")
            .setPositiveButton("Save") { _, _ ->
                saveAvailability()
                finish()
            }
            .setNegativeButton("Discard") { _, _ ->
                finish()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (hasUnsavedChanges) {
            showUnsavedChangesDialog()
        } else {
            super.onBackPressed()
        }
    }
}

/**
 * Data class representing availability for a single day
 */
data class DayAvailability(
    val isDayOff: Boolean = false,
    val slots: List<TimeSlot> = emptyList()
) {
    fun hasAvailableSlots(): Boolean = slots.any { it.status == SlotStatus.AVAILABLE || it.status == SlotStatus.SELECTED }
    fun hasBookedSlots(): Boolean = slots.any { it.status == SlotStatus.BOOKED }
}
