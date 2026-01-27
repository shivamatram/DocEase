package com.example.docease.ui.finddoctor

import android.content.Intent
import android.net.Uri
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R
import com.example.docease.ui.doctor.DoctorDetailsActivity
import com.example.docease.ui.search.FilterChipAdapter
import com.example.docease.ui.search.FilterChipItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/**
 * Find a Doctor Activity
 * Displays categorized list of doctors with filter chips and native ad placement
 */
class FindDoctorActivity : AppCompatActivity() {

    // Views
    private lateinit var btnBack: ImageButton
    private lateinit var btnFilter: ImageButton
    private lateinit var tvSectionTitle: TextView
    private lateinit var rvCategoryChips: RecyclerView
    private lateinit var rvDoctors: RecyclerView
    private lateinit var layoutLoading: FrameLayout
    private lateinit var layoutEmptyState: LinearLayout

    // Adapters
    private lateinit var chipAdapter: FilterChipAdapter
    private lateinit var doctorAdapter: FindDoctorAdapter

    // Data
    private val categoryChips = mutableListOf<FilterChipItem>()
    private val allDoctors = mutableListOf<FindDoctorItem>()
    private val filteredItems = mutableListOf<FindDoctorListItem>()
    private var currentCategory = "General"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_find_doctor)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        initViews()
        setupWindowInsets()
        setupClickListeners()
        setupCategoryChips()
        setupDoctorsList()
        loadDoctors()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnFilter = findViewById(R.id.btnFilter)
        tvSectionTitle = findViewById(R.id.tvSectionTitle)
        rvCategoryChips = findViewById(R.id.rvCategoryChips)
        rvDoctors = findViewById(R.id.rvDoctors)
        layoutLoading = findViewById(R.id.layoutLoading)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun setupCategoryChips() {
        // Initialize category chips (id, displayName)
        categoryChips.addAll(
            listOf(
                FilterChipItem("general", "General"),
                FilterChipItem("dentist", "Dentist"),
                FilterChipItem("cardio", "Cardiologist"),
                FilterChipItem("derma", "Dermatologist"),
                FilterChipItem("neuro", "Neurologist"),
                FilterChipItem("pediatric", "Pediatrician"),
                FilterChipItem("ortho", "Orthopedist"),
                FilterChipItem("ophthalm", "Ophthalmologist")
            )
        )

        chipAdapter = FilterChipAdapter(categoryChips) { chip, position ->
            // Update selection in adapter and filter the list
            chipAdapter.setSelectedPosition(position)
            currentCategory = chip.name
            filterDoctorsByCategory(chip.name)
        }

        // Select first chip by default
        chipAdapter.setSelectedPosition(0)

        rvCategoryChips.apply {
            layoutManager = LinearLayoutManager(this@FindDoctorActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = chipAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupDoctorsList() {
        doctorAdapter = FindDoctorAdapter(
            items = filteredItems,
            onDoctorClick = { doctor ->
                navigateToDoctorDetails(doctor)
            },
            onAdClick = { ad ->
                handleAdClick(ad)
            },
            onAdCtaClick = { ad ->
                handleAdCtaClick(ad)
            }
        )

        rvDoctors.apply {
            layoutManager = LinearLayoutManager(this@FindDoctorActivity)
            adapter = doctorAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun loadDoctors() {
        showLoading(true)

        // Sample doctor data - In production, this would come from Firebase/API
        allDoctors.clear()
        allDoctors.addAll(getSampleDoctors())

        // Initial filter
        filterDoctorsByCategory(currentCategory)
        showLoading(false)
    }

    private fun getSampleDoctors(): List<FindDoctorItem> {
        return listOf(
            // General
            FindDoctorItem(
                id = "doc_1",
                name = "Dr. Sarah Johnson",
                specialty = "Cardiologist",
                category = "General",
                rating = 4.9f,
                reviewCount = 120,
                avatarUrl = "",
                isOnline = true
            ),
            FindDoctorItem(
                id = "doc_2",
                name = "Dr. Michael Chen",
                specialty = "Dentist",
                category = "General",
                rating = 4.8f,
                reviewCount = 95,
                avatarUrl = "",
                isOnline = false
            ),
            // After first 2, we insert ad
            FindDoctorItem(
                id = "doc_3",
                name = "Dr. James Wilson",
                specialty = "Neurologist",
                category = "General",
                rating = 4.7f,
                reviewCount = 89,
                avatarUrl = "",
                isOnline = false
            ),
            FindDoctorItem(
                id = "doc_4",
                name = "Dr. Emily Brooks",
                specialty = "Pediatrician",
                category = "General",
                rating = 5.0f,
                reviewCount = 215,
                avatarUrl = "",
                isOnline = true
            ),
            FindDoctorItem(
                id = "doc_5",
                name = "Dr. Alan Grant",
                specialty = "Orthopedist",
                category = "General",
                rating = 4.6f,
                reviewCount = 60,
                avatarUrl = "",
                isOnline = false
            ),
            // Dentist
            FindDoctorItem(
                id = "doc_6",
                name = "Dr. Lisa Park",
                specialty = "Dentist",
                category = "Dentist",
                rating = 4.9f,
                reviewCount = 180,
                avatarUrl = "",
                isOnline = true
            ),
            FindDoctorItem(
                id = "doc_7",
                name = "Dr. Robert Kim",
                specialty = "Dentist",
                category = "Dentist",
                rating = 4.7f,
                reviewCount = 142,
                avatarUrl = "",
                isOnline = false
            ),
            // Cardiologist
            FindDoctorItem(
                id = "doc_8",
                name = "Dr. Amanda White",
                specialty = "Cardiologist",
                category = "Cardiologist",
                rating = 4.8f,
                reviewCount = 205,
                avatarUrl = "",
                isOnline = true
            ),
            FindDoctorItem(
                id = "doc_9",
                name = "Dr. David Martinez",
                specialty = "Cardiologist",
                category = "Cardiologist",
                rating = 4.9f,
                reviewCount = 178,
                avatarUrl = "",
                isOnline = false
            ),
            // Dermatologist
            FindDoctorItem(
                id = "doc_10",
                name = "Dr. Jennifer Lee",
                specialty = "Dermatologist",
                category = "Dermatologist",
                rating = 4.8f,
                reviewCount = 156,
                avatarUrl = "",
                isOnline = true
            ),
            // Neurologist
            FindDoctorItem(
                id = "doc_11",
                name = "Dr. Mark Thompson",
                specialty = "Neurologist",
                category = "Neurologist",
                rating = 4.7f,
                reviewCount = 98,
                avatarUrl = "",
                isOnline = false
            ),
            // Pediatrician
            FindDoctorItem(
                id = "doc_12",
                name = "Dr. Rachel Green",
                specialty = "Pediatrician",
                category = "Pediatrician",
                rating = 5.0f,
                reviewCount = 312,
                avatarUrl = "",
                isOnline = true
            ),
            // Orthopedist
            FindDoctorItem(
                id = "doc_13",
                name = "Dr. Steven Adams",
                specialty = "Orthopedist",
                category = "Orthopedist",
                rating = 4.6f,
                reviewCount = 87,
                avatarUrl = "",
                isOnline = false
            )
        )
    }

    private fun filterDoctorsByCategory(category: String) {
        filteredItems.clear()

        // Filter doctors by category
        val doctors = if (category == "General") {
            allDoctors.take(5) // Show first 5 for "General"
        } else {
            allDoctors.filter { it.category == category || it.specialty == category }
        }

        if (doctors.isEmpty()) {
            showEmptyState(true)
            return
        }

        showEmptyState(false)

        // Build list with ad placement after 2nd doctor
        doctors.forEachIndexed { index, doctor ->
            filteredItems.add(FindDoctorListItem.DoctorItem(doctor))

            // Insert ad after 2nd doctor
            if (index == 1) {
                filteredItems.add(
                    FindDoctorListItem.AdItem(
                        FindDoctorAd(
                            id = "ad_1",
                            sponsor = "HealthPlus",
                            title = "Full Body Checkup",
                            description = "Get 50% off your first comprehensive health screening. Limited time offer.",
                            ctaText = "Book Now",
                            imageUrl = "",
                            targetUrl = "https://healthplus.com/checkup"
                        )
                    )
                )
            }
        }

        // Update section title based on category
        tvSectionTitle.text = if (category == "General") {
            "Top Rated Specialists"
        } else {
            "Top $category Specialists"
        }

        doctorAdapter.notifyDataSetChanged()
    }

    private fun navigateToDoctorDetails(doctor: FindDoctorItem) {
        val intent = Intent(this, DoctorDetailsActivity::class.java).apply {
            putExtra("doctor_id", doctor.id)
            putExtra("doctor_name", doctor.name)
            putExtra("doctor_specialty", doctor.specialty)
            putExtra("doctor_rating", doctor.rating)
            putExtra("doctor_review_count", doctor.reviewCount)
        }
        startActivity(intent)
    }

    private fun handleAdClick(ad: FindDoctorAd) {
        // Track ad impression/click
        openAdUrl(ad.targetUrl)
    }

    private fun handleAdCtaClick(ad: FindDoctorAd) {
        // Track CTA click
        openAdUrl(ad.targetUrl)
    }

    private fun openAdUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            showSnackbar("Unable to open link")
        }
    }

    private fun showFilterDialog() {
        val filters = arrayOf(
            "Rating: High to Low",
            "Rating: Low to High",
            "Most Reviews",
            "Online Now",
            "Clear Filters"
        )

        MaterialAlertDialogBuilder(this, R.style.Theme_DocEase_Dialog)
            .setTitle("Filter Doctors")
            .setItems(filters) { _, which ->
                applyFilter(which)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun applyFilter(filterIndex: Int) {
        val currentDoctors = filteredItems.filterIsInstance<FindDoctorListItem.DoctorItem>()
            .map { it.doctor }
            .toMutableList()

        when (filterIndex) {
            0 -> { // Rating: High to Low
                currentDoctors.sortByDescending { it.rating }
                showSnackbar("Sorted by highest rating")
            }
            1 -> { // Rating: Low to High
                currentDoctors.sortBy { it.rating }
                showSnackbar("Sorted by lowest rating")
            }
            2 -> { // Most Reviews
                currentDoctors.sortByDescending { it.reviewCount }
                showSnackbar("Sorted by most reviews")
            }
            3 -> { // Online Now
                currentDoctors.sortByDescending { it.isOnline }
                showSnackbar("Showing online doctors first")
            }
            4 -> { // Clear Filters
                filterDoctorsByCategory(currentCategory)
                showSnackbar("Filters cleared")
                return
            }
        }

        // Rebuild list with sorted doctors
        filteredItems.clear()
        currentDoctors.forEachIndexed { index, doctor ->
            filteredItems.add(FindDoctorListItem.DoctorItem(doctor))
            if (index == 1) {
                filteredItems.add(
                    FindDoctorListItem.AdItem(
                        FindDoctorAd(
                            id = "ad_1",
                            sponsor = "HealthPlus",
                            title = "Full Body Checkup",
                            description = "Get 50% off your first comprehensive health screening. Limited time offer.",
                            ctaText = "Book Now",
                            imageUrl = "",
                            targetUrl = "https://healthplus.com/checkup"
                        )
                    )
                )
            }
        }
        doctorAdapter.notifyDataSetChanged()
    }

    private fun showLoading(show: Boolean) {
        layoutLoading.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(show: Boolean) {
        layoutEmptyState.visibility = if (show) View.VISIBLE else View.GONE
        rvDoctors.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(getColor(R.color.docease_primary))
            .setTextColor(getColor(R.color.white))
            .show()
    }
}
