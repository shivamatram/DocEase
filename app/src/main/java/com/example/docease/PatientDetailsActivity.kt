package com.example.docease

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.adapters.NoteType
import com.example.docease.adapters.PatientDetailsTab
import com.example.docease.adapters.PatientNoteAdapter
import com.example.docease.adapters.PatientNoteItem
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * PatientDetailsActivity displays comprehensive patient information
 * including profile, health metrics, and medical notes
 */
class PatientDetailsActivity : AppCompatActivity() {

    // Views
    private lateinit var btnBack: ImageView
    private lateinit var btnMenu: ImageView
    private lateinit var ivPatientAvatar: ShapeableImageView
    private lateinit var ivVerifiedBadge: ImageView
    private lateinit var tvPatientName: TextView
    private lateinit var tvPatientInfo: TextView
    private lateinit var tvTreatmentStatus: TextView
    private lateinit var btnCall: LinearLayout
    private lateinit var btnMessage: LinearLayout
    private lateinit var tvBloodPressureValue: TextView
    private lateinit var tvHeartRateValue: TextView
    private lateinit var tvWeightValue: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var tvSeeAll: TextView
    private lateinit var rvNotes: RecyclerView
    private lateinit var btnStartConsultation: MaterialButton

    // Adapter
    private lateinit var notesAdapter: PatientNoteAdapter

    // Data
    private var patientId: String? = null
    private var patientPhone: String? = null
    private var currentTab = PatientDetailsTab.MEDICAL_NOTES
    private var allNotes = mutableListOf<PatientNoteItem>()

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    companion object {
        const val EXTRA_PATIENT_ID = "patient_id"
        const val EXTRA_PATIENT_NAME = "patient_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_patient_details)

        patientId = intent.getStringExtra(EXTRA_PATIENT_ID)

        initViews()
        setupRecyclerView()
        setupTabs()
        setupClickListeners()
        loadPatientDetails()
        loadPatientNotes()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnMenu = findViewById(R.id.btnMenu)
        ivPatientAvatar = findViewById(R.id.ivPatientAvatar)
        ivVerifiedBadge = findViewById(R.id.ivVerifiedBadge)
        tvPatientName = findViewById(R.id.tvPatientName)
        tvPatientInfo = findViewById(R.id.tvPatientInfo)
        tvTreatmentStatus = findViewById(R.id.tvTreatmentStatus)
        btnCall = findViewById(R.id.btnCall)
        btnMessage = findViewById(R.id.btnMessage)
        tvBloodPressureValue = findViewById(R.id.tvBloodPressureValue)
        tvHeartRateValue = findViewById(R.id.tvHeartRateValue)
        tvWeightValue = findViewById(R.id.tvWeightValue)
        tabLayout = findViewById(R.id.tabLayout)
        tvSeeAll = findViewById(R.id.tvSeeAll)
        rvNotes = findViewById(R.id.rvNotes)
        btnStartConsultation = findViewById(R.id.btnStartConsultation)
    }

    private fun setupRecyclerView() {
        notesAdapter = PatientNoteAdapter { note ->
            openNoteDetails(note)
        }

        rvNotes.apply {
            layoutManager = LinearLayoutManager(this@PatientDetailsActivity)
            adapter = notesAdapter
        }
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = when (tab?.position) {
                    0 -> PatientDetailsTab.MEDICAL_NOTES
                    1 -> PatientDetailsTab.HISTORY
                    2 -> PatientDetailsTab.PRESCRIPTIONS
                    else -> PatientDetailsTab.MEDICAL_NOTES
                }
                filterNotes()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnMenu.setOnClickListener {
            showMenuOptions()
        }

        btnCall.setOnClickListener {
            initiateCall()
        }

        btnMessage.setOnClickListener {
            openChat()
        }

        tvSeeAll.setOnClickListener {
            Toast.makeText(this, "View all notes", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to full notes list
        }

        btnStartConsultation.setOnClickListener {
            startConsultation()
        }
    }

    private fun loadPatientDetails() {
        if (patientId.isNullOrEmpty()) {
            // Load sample data
            loadSamplePatientData()
            return
        }

        database.child("patients").child(patientId!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").getValue(String::class.java) ?: "Unknown"
                        val age = snapshot.child("age").getValue(Int::class.java) ?: 0
                        val gender = snapshot.child("gender").getValue(String::class.java) ?: "N/A"
                        patientPhone = snapshot.child("phone").getValue(String::class.java)
                        val isActive = snapshot.child("isActive").getValue(Boolean::class.java) ?: true

                        tvPatientName.text = name
                        tvPatientInfo.text = "$age Years, $gender"
                        tvTreatmentStatus.text = if (isActive) "ACTIVE TREATMENT" else "COMPLETED"

                        // Load health metrics
                        loadHealthMetrics()
                    } else {
                        loadSamplePatientData()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    loadSamplePatientData()
                }
            })
    }

    private fun loadSamplePatientData() {
        tvPatientName.text = "Sarah Jenkins"
        tvPatientInfo.text = "28 Years, Female"
        tvTreatmentStatus.text = "ACTIVE TREATMENT"
        patientPhone = "+1234567890"

        // Sample health metrics
        tvBloodPressureValue.text = "120/80"
        tvHeartRateValue.text = "72"
        tvWeightValue.text = "65"
    }

    private fun loadHealthMetrics() {
        if (patientId.isNullOrEmpty()) return

        database.child("health_metrics").child(patientId!!)
            .orderByChild("timestamp")
            .limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (metricSnapshot in snapshot.children) {
                        val bloodPressure = metricSnapshot.child("bloodPressure").getValue(String::class.java)
                        val heartRate = metricSnapshot.child("heartRate").getValue(Int::class.java)
                        val weight = metricSnapshot.child("weight").getValue(Double::class.java)

                        bloodPressure?.let { tvBloodPressureValue.text = it }
                        heartRate?.let { tvHeartRateValue.text = it.toString() }
                        weight?.let { tvWeightValue.text = it.toInt().toString() }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Use default values
                }
            })
    }

    private fun loadPatientNotes() {
        if (patientId.isNullOrEmpty()) {
            loadSampleNotes()
            return
        }

        database.child("patient_notes")
            .orderByChild("patientId")
            .equalTo(patientId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    allNotes.clear()
                    for (noteSnapshot in snapshot.children) {
                        val note = parseNote(noteSnapshot)
                        if (note != null) {
                            allNotes.add(note)
                        }
                    }
                    allNotes.sortByDescending { it.timestamp }
                    filterNotes()
                }

                override fun onCancelled(error: DatabaseError) {
                    loadSampleNotes()
                }
            })
    }

    private fun parseNote(snapshot: DataSnapshot): PatientNoteItem? {
        return try {
            val id = snapshot.key ?: return null
            val title = snapshot.child("title").getValue(String::class.java) ?: ""
            val doctorName = snapshot.child("doctorName").getValue(String::class.java) ?: ""
            val description = snapshot.child("description").getValue(String::class.java) ?: ""
            val dateLabel = snapshot.child("dateLabel").getValue(String::class.java) ?: ""
            val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L
            val typeStr = snapshot.child("type").getValue(String::class.java) ?: "GENERAL"

            PatientNoteItem(
                id = id,
                title = title,
                doctorName = doctorName,
                description = description,
                dateLabel = dateLabel,
                timestamp = timestamp,
                type = NoteType.valueOf(typeStr),
                patientId = patientId
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun loadSampleNotes() {
        allNotes = mutableListOf(
            PatientNoteItem(
                id = "1",
                title = "Severe Migraine",
                doctorName = "Dr. Michael Chen",
                description = "Patient reported severe headache on the left side, sensitivity to light. Prescribed pain medication and recommended rest.",
                dateLabel = "Today",
                timestamp = System.currentTimeMillis(),
                type = NoteType.MIGRAINE
            ),
            PatientNoteItem(
                id = "2",
                title = "Routine Blood Work",
                doctorName = "Dr. Emily Watson",
                description = "Complete blood count and metabolic panel ordered. Results expected within 24-48 hours. Fasting blood sugar levels normal.",
                dateLabel = "Oct 24",
                timestamp = System.currentTimeMillis() - 86400000 * 3,
                type = NoteType.BLOOD_WORK
            ),
            PatientNoteItem(
                id = "3",
                title = "Flu Vaccination",
                doctorName = "Dr. Sarah Johnson",
                description = "Annual flu vaccination administered. Patient tolerated well with no immediate adverse reactions. Follow-up in 2 weeks if any concerns.",
                dateLabel = "Sep 15",
                timestamp = System.currentTimeMillis() - 86400000 * 30,
                type = NoteType.VACCINATION
            )
        )
        filterNotes()
    }

    private fun filterNotes() {
        val filtered = when (currentTab) {
            PatientDetailsTab.MEDICAL_NOTES -> allNotes
            PatientDetailsTab.HISTORY -> allNotes.filter { 
                it.type == NoteType.BLOOD_WORK || it.type == NoteType.VACCINATION 
            }
            PatientDetailsTab.PRESCRIPTIONS -> allNotes.filter { 
                it.type == NoteType.PRESCRIPTION 
            }
        }
        notesAdapter.submitList(filtered)
    }

    private fun showMenuOptions() {
        val options = arrayOf("Edit Patient", "View History", "Export Records", "Delete Patient")
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> Toast.makeText(this, "Edit patient", Toast.LENGTH_SHORT).show()
                    1 -> {
                        tabLayout.getTabAt(1)?.select()
                    }
                    2 -> Toast.makeText(this, "Export records", Toast.LENGTH_SHORT).show()
                    3 -> confirmDeletePatient()
                }
            }
            .show()
    }

    private fun confirmDeletePatient() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Patient")
            .setMessage("Are you sure you want to delete this patient record? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                Toast.makeText(this, "Patient deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun initiateCall() {
        if (patientPhone.isNullOrEmpty()) {
            Toast.makeText(this, "No phone number available", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Call Patient")
            .setMessage("Call ${tvPatientName.text}?")
            .setPositiveButton("Call") { _, _ ->
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$patientPhone")
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openChat() {
        Toast.makeText(this, "Opening chat with ${tvPatientName.text}", Toast.LENGTH_SHORT).show()
        // TODO: Navigate to chat screen
        // val intent = Intent(this, ChatActivity::class.java)
        // intent.putExtra("patient_id", patientId)
        // startActivity(intent)
    }

    private fun openNoteDetails(note: PatientNoteItem) {
        Toast.makeText(this, "Opening note: ${note.title}", Toast.LENGTH_SHORT).show()
        // TODO: Navigate to note details screen
    }

    private fun startConsultation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Start Consultation")
            .setMessage("Start a new consultation with ${tvPatientName.text}?")
            .setPositiveButton("Start") { _, _ ->
                Toast.makeText(this, "Starting consultation...", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to consultation screen
                // val intent = Intent(this, ConsultationActivity::class.java)
                // intent.putExtra("patient_id", patientId)
                // startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
