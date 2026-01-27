package com.example.docease.ui.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
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
import com.example.docease.ui.doctor.DoctorDetailsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

/**
 * Search Doctor / Find Specialist Activity
 * Allows patients to search and filter doctors by specialty
 */
class SearchDoctorActivity : AppCompatActivity() {

    // UI Components - Header
    private lateinit var rootLayout: View
    private lateinit var headerTitle: TextView

    // UI Components - Search
    private lateinit var searchEditText: EditText
    private lateinit var filterIcon: ImageView

    // UI Components - Filters
    private lateinit var filterChipsRecyclerView: RecyclerView

    // UI Components - Specialists
    private lateinit var topSpecialistsTitle: TextView
    private lateinit var seeAllButton: TextView
    private lateinit var specialistsRecyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout

    // UI Components - Bottom
    private lateinit var searchFab: FloatingActionButton
    private lateinit var bottomNavigation: BottomNavigationView

    // Adapters
    private lateinit var filterChipAdapter: FilterChipAdapter
    private lateinit var specialistAdapter: SearchSpecialistAdapter

    // Data
    private val filterChipsList = mutableListOf<FilterChipItem>()
    private val allSpecialistsList = mutableListOf<SearchSpecialistItem>()
    private val filteredSpecialistsList = mutableListOf<SearchSpecialistItem>()

    // Current filter
    private var currentFilter = "all"
    private var currentSearchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_search_doctor)

        // Apply window insets
        setupWindowInsets()

        // Initialize UI components
        initViews()

        // Setup data
        setupFilterChips()
        setupSpecialistsList()

        // Setup search functionality
        setupSearch()

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
        headerTitle = findViewById(R.id.headerTitle)

        // Search
        searchEditText = findViewById(R.id.searchEditText)
        filterIcon = findViewById(R.id.filterIcon)

        // Filters
        filterChipsRecyclerView = findViewById(R.id.filterChipsRecyclerView)

        // Specialists
        topSpecialistsTitle = findViewById(R.id.topSpecialistsTitle)
        seeAllButton = findViewById(R.id.seeAllButton)
        specialistsRecyclerView = findViewById(R.id.specialistsRecyclerView)
        emptyState = findViewById(R.id.emptyState)

        // Bottom
        searchFab = findViewById(R.id.searchFab)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun setupFilterChips() {
        // Populate filter chips
        filterChipsList.clear()
        filterChipsList.addAll(
            listOf(
                FilterChipItem("all", "All"),
                FilterChipItem("general", "General"),
                FilterChipItem("dentist", "Dentist"),
                FilterChipItem("cardiology", "Cardiology")
            )
        )

        // Setup adapter
        filterChipAdapter = FilterChipAdapter(filterChipsList) { chip, position ->
            onFilterChipClick(chip)
        }

        filterChipsRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                this@SearchDoctorActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = filterChipAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSpecialistsList() {
        // Populate sample specialists data
        allSpecialistsList.clear()
        allSpecialistsList.addAll(
            listOf(
                SearchSpecialistItem(
                    id = "1",
                    name = "Dr. Dianne Russell",
                    specialty = "Dermatologist",
                    hospital = "City Hospital",
                    rating = 4.8f,
                    availability = AvailabilityStatus.TODAY,
                    nextSlot = "2:00 PM",
                    isFavorite = false
                ),
                SearchSpecialistItem(
                    id = "2",
                    name = "Dr. Bessie Cooper",
                    specialty = "Cardiologist",
                    hospital = "Heart Care Center",
                    rating = 4.9f,
                    availability = AvailabilityStatus.TODAY,
                    nextSlot = "3:30 PM",
                    isFavorite = true
                ),
                SearchSpecialistItem(
                    id = "3",
                    name = "Dr. Esther Howard",
                    specialty = "Dentist",
                    hospital = "Smile Dental Clinic",
                    rating = 4.7f,
                    availability = AvailabilityStatus.TOMORROW,
                    nextSlot = "10:00 AM",
                    isFavorite = false
                ),
                SearchSpecialistItem(
                    id = "4",
                    name = "Dr. Cameron Williamson",
                    specialty = "General Physician",
                    hospital = "Metro Health",
                    rating = 4.6f,
                    availability = AvailabilityStatus.TODAY,
                    nextSlot = "4:00 PM",
                    isFavorite = false
                ),
                SearchSpecialistItem(
                    id = "5",
                    name = "Dr. Brooklyn Simmons",
                    specialty = "Cardiologist",
                    hospital = "Cardiac Institute",
                    rating = 4.9f,
                    availability = AvailabilityStatus.NEXT_WEEK,
                    nextSlot = "Mon 9:00 AM",
                    isFavorite = true
                ),
                SearchSpecialistItem(
                    id = "6",
                    name = "Dr. Leslie Alexander",
                    specialty = "Dentist",
                    hospital = "Bright Dental",
                    rating = 4.5f,
                    availability = AvailabilityStatus.TOMORROW,
                    nextSlot = "11:30 AM",
                    isFavorite = false
                )
            )
        )

        // Initialize filtered list with all specialists
        filteredSpecialistsList.clear()
        filteredSpecialistsList.addAll(allSpecialistsList)

        // Setup adapter
        specialistAdapter = SearchSpecialistAdapter(
            specialists = filteredSpecialistsList,
            onSpecialistClick = { specialist -> onSpecialistClick(specialist) },
            onBookClick = { specialist -> onBookClick(specialist) },
            onFavoriteClick = { specialist, position -> onFavoriteClick(specialist, position) }
        )

        specialistsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchDoctorActivity)
            adapter = specialistAdapter
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
        }
    }

    private fun setupSearch() {
        // Text change listener for real-time search
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentSearchQuery = s?.toString()?.trim() ?: ""
                filterSpecialists()
            }
        })

        // Search action on keyboard
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun setupClickListeners() {
        // Filter icon click
        filterIcon.setOnClickListener {
            showSnackbar("Opening filters...")
            // TODO: Show advanced filter dialog
        }

        // See all button click
        seeAllButton.setOnClickListener {
            showSnackbar("Showing all specialists...")
            // TODO: Navigate to full specialists list
        }

        // Search FAB click
        searchFab.setOnClickListener {
            performSearch()
        }
    }

    private fun setupBottomNavigation() {
        // Set Search as selected
        bottomNavigation.selectedItemId = R.id.nav_search

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showSnackbar("Navigating to Home...")
                    // TODO: Navigate to patient dashboard
                    // startActivity(Intent(this, PatientDashboardActivity::class.java))
                    // finish()
                    true
                }
                R.id.nav_search -> {
                    // Already on search, do nothing
                    true
                }
                R.id.nav_bookings -> {
                    showSnackbar("Navigating to Bookings...")
                    // TODO: Navigate to bookings/appointments
                    true
                }
                R.id.nav_profile -> {
                    showSnackbar("Navigating to Profile...")
                    // TODO: Navigate to profile
                    true
                }
                else -> false
            }
        }
    }

    private fun onFilterChipClick(chip: FilterChipItem) {
        currentFilter = chip.id
        filterSpecialists()
    }

    private fun filterSpecialists() {
        val filtered = allSpecialistsList.filter { specialist ->
            // Filter by category
            val matchesCategory = when (currentFilter) {
                "all" -> true
                "general" -> specialist.specialty.contains("General", ignoreCase = true)
                "dentist" -> specialist.specialty.contains("Dentist", ignoreCase = true)
                "cardiology" -> specialist.specialty.contains("Cardiologist", ignoreCase = true) ||
                        specialist.specialty.contains("Cardiology", ignoreCase = true)
                else -> true
            }

            // Filter by search query
            val matchesSearch = if (currentSearchQuery.isEmpty()) {
                true
            } else {
                specialist.name.contains(currentSearchQuery, ignoreCase = true) ||
                        specialist.specialty.contains(currentSearchQuery, ignoreCase = true) ||
                        specialist.hospital.contains(currentSearchQuery, ignoreCase = true)
            }

            matchesCategory && matchesSearch
        }

        specialistAdapter.updateList(filtered)
        updateEmptyState(filtered.isEmpty())
    }

    private fun performSearch() {
        // Hide keyboard
        searchEditText.clearFocus()
        
        // Filter is already applied via text watcher
        val query = searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            showSnackbar("Searching for: $query")
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            emptyState.visibility = View.VISIBLE
            specialistsRecyclerView.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            specialistsRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun onSpecialistClick(specialist: SearchSpecialistItem) {
        // Navigate to Doctor Details screen
        val intent = Intent(this, DoctorDetailsActivity::class.java).apply {
            putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_ID, specialist.id)
            putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_NAME, specialist.name)
            putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_SPECIALTY, specialist.specialty)
            putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_LOCATION, specialist.hospital)
            putExtra(DoctorDetailsActivity.EXTRA_IS_FAVORITE, specialist.isFavorite)
            putExtra(DoctorDetailsActivity.EXTRA_CONSULTATION_FEE, 40.00)
        }
        startActivity(intent)
    }

    private fun onBookClick(specialist: SearchSpecialistItem) {
        // Navigate directly to Doctor Details for booking
        val intent = Intent(this, DoctorDetailsActivity::class.java).apply {
            putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_ID, specialist.id)
            putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_NAME, specialist.name)
            putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_SPECIALTY, specialist.specialty)
            putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_LOCATION, specialist.hospital)
            putExtra(DoctorDetailsActivity.EXTRA_IS_FAVORITE, specialist.isFavorite)
            putExtra(DoctorDetailsActivity.EXTRA_CONSULTATION_FEE, 40.00)
        }
        startActivity(intent)
        // val intent = Intent(this, BookAppointmentActivity::class.java)
        // intent.putExtra("doctorId", specialist.id)
        // startActivity(intent)
    }

    private fun onFavoriteClick(specialist: SearchSpecialistItem, position: Int) {
        val newFavoriteState = !specialist.isFavorite
        specialistAdapter.updateFavorite(position, newFavoriteState)
        
        if (newFavoriteState) {
            showSnackbar("${specialist.name} added to favorites")
        } else {
            showSnackbar("${specialist.name} removed from favorites")
        }
        
        // TODO: Save favorite state to repository/database
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(getColor(R.color.docease_primary))
            .setTextColor(getColor(R.color.docease_background))
            .show()
    }
}
