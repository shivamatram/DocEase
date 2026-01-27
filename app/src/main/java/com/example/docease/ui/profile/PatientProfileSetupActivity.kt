package com.example.docease.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.docease.R
import com.example.docease.firebase.AuthManager
import com.example.docease.firebase.DatabaseManager
import com.example.docease.models.Patient
import com.example.docease.models.Gender
import com.example.docease.utils.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import coil.load

/**
 * Patient Profile Setup Activity
 * Allows patients to complete their profile after sign-up
 */
class PatientProfileSetupActivity : AppCompatActivity() {

    private lateinit var patientAvatar: ImageView
    private lateinit var uploadPhotoButton: Button
    private lateinit var etPatientName: EditText
    private lateinit var etAge: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var etPhoneNumber: EditText
    private lateinit var etMedicalHistory: EditText
    private lateinit var etBloodGroup: EditText
    private lateinit var btnSaveProfile: Button
    private lateinit var btnSkip: Button

    private lateinit var authManager: AuthManager
    private lateinit var databaseManager: DatabaseManager
    private lateinit var prefManager: PreferenceManager

    private var profileImageUrl: String = ""
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_patient_profile_setup)

        // Initialize managers
        authManager = AuthManager.getInstance()
        databaseManager = DatabaseManager.getInstance()
        prefManager = PreferenceManager(this)

        // Initialize UI components
        initViews()

        // Setup gender spinner
        setupGenderSpinner()

        // Setup click listeners
        setupClickListeners()
    }

    private fun initViews() {
        patientAvatar = findViewById(R.id.patientAvatar)
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton)
        etPatientName = findViewById(R.id.etPatientName)
        etAge = findViewById(R.id.etAge)
        spinnerGender = findViewById(R.id.spinnerGender)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etMedicalHistory = findViewById(R.id.etMedicalHistory)
        etBloodGroup = findViewById(R.id.etBloodGroup)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)
        btnSkip = findViewById(R.id.btnSkip)
    }

    private fun setupGenderSpinner() {
        val genderOptions = arrayOf("Select Gender", "Male", "Female", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderOptions)
        spinnerGender.adapter = adapter
    }

    private fun setupClickListeners() {
        uploadPhotoButton.setOnClickListener {
            pickImageFromGallery()
        }

        btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        btnSkip.setOnClickListener {
            // Skip profile setup and go to dashboard
            navigateToDashboard()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                patientAvatar.load(imageUri)
                // Keep the selected URI for upload; don't save local URI directly as profileImageUrl
                selectedImageUri = imageUri
            }
        }
    }

    private fun saveProfile() {
        val patientName = etPatientName.text.toString().trim()
        val ageStr = etAge.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()
        val medicalHistory = etMedicalHistory.text.toString().trim()
        val bloodGroup = etBloodGroup.text.toString().trim()

        // Validation
        if (patientName.isEmpty()) {
            showToast("Please enter your name")
            return
        }

        if (spinnerGender.selectedItemPosition == 0) {
            showToast("Please select your gender")
            return
        }

        val age = ageStr.toIntOrNull() ?: 0
        if (age <= 0) {
            showToast("Please enter a valid age")
            return
        }

        // Get current user ID
        val currentUserId = authManager.getCurrentUserId()
        if (currentUserId.isNullOrEmpty()) {
            showToast("User not authenticated")
            return
        }

        // Show loading state
        btnSaveProfile.isEnabled = false
        btnSaveProfile.text = "Saving..."

        // Safety timeout
        val handler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            android.util.Log.w("PatientProfileSetup", "saveProfile: timeout")
            btnSaveProfile.isEnabled = true
            btnSaveProfile.text = "Save Profile"
            showToast("Network timeout. Please check your connection and try again.")
        }
        handler.postDelayed(timeoutRunnable, 15_000)

        // Get gender
        val genderValue = when (spinnerGender.selectedItem.toString()) {
            "Male" -> Gender.MALE
            "Female" -> Gender.FEMALE
            else -> Gender.OTHER
        }

        // If an image was selected, upload it first and then save the patient profile
        fun savePatientWithImageUrl(imageUrl: String) {
            val patient = Patient(
                uid = currentUserId,
                name = patientName,
                email = authManager.getCurrentUser()?.email ?: "",
                age = age,
                gender = genderValue,
                phoneNumber = phoneNumber,
                medicalHistory = medicalHistory,
                bloodGroup = bloodGroup,
                profileImageUrl = imageUrl
            )

            android.util.Log.d("PatientProfileSetup", "saveProfile: saving patient $currentUserId")

            // Save patient profile to RTDB using atomic update (also ensure users/{uid}/role set to 'patient')
            val updates = mutableMapOf<String, Any>()
            updates["/patients/$currentUserId"] = patient.toMap()
            updates["/users/$currentUserId"] = mapOf(
                "uid" to currentUserId,
                "email" to authManager.getCurrentUser()?.email.orEmpty(),
                "role" to "patient",
                "createdAt" to System.currentTimeMillis()
            )

            databaseManager.getDatabase().reference.updateChildren(updates).addOnCompleteListener { task ->
                handler.removeCallbacks(timeoutRunnable)
                btnSaveProfile.isEnabled = true
                btnSaveProfile.text = "Save Profile"

                if (task.isSuccessful) {
                    android.util.Log.d("PatientProfileSetup", "saveProfile: success")
                    prefManager.saveUserName(patientName)
                    showToast("Profile saved successfully!")
                    navigateToDashboard()
                } else {
                    android.util.Log.e("PatientProfileSetup", "saveProfile: failed", task.exception)
                    val errorMsg = when {
                        task.exception?.message?.contains("Permission denied") == true ->
                            "Permission denied. Please try again."
                        else -> task.exception?.message ?: "Failed to save profile"
                    }
                    showToast(errorMsg)
                }
            }
        }

        // Upload image first if needed
        if (selectedImageUri != null) {
            // Update UI
            btnSaveProfile.text = "Uploading..."

            val storageRef = FirebaseStorage.getInstance().reference
                .child("patients/$currentUserId/profile_${System.currentTimeMillis()}.jpg")

            storageRef.putFile(selectedImageUri!!).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    savePatientWithImageUrl(downloadUri.toString())
                }.addOnFailureListener { e ->
                    handler.removeCallbacks(timeoutRunnable)
                    btnSaveProfile.isEnabled = true
                    btnSaveProfile.text = "Save Profile"
                    android.util.Log.e("PatientProfileSetup", "upload downloadUrl failed", e)
                    showToast("Failed to upload image: ${e.message}")
                }
            }.addOnFailureListener { e ->
                handler.removeCallbacks(timeoutRunnable)
                btnSaveProfile.isEnabled = true
                btnSaveProfile.text = "Save Profile"
                android.util.Log.e("PatientProfileSetup", "image upload failed", e)
                showToast("Failed to upload image: ${e.message}")
            }
        } else {
            // No image selected, proceed with empty URL
            savePatientWithImageUrl(profileImageUrl)
        }
    }

    private fun navigateToDashboard() {
        try {
            val intent = Intent(this, Class.forName("com.example.docease.ui.patient.PatientDashboardActivity"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: ClassNotFoundException) {
            android.util.Log.e("PatientProfileSetup", "PatientDashboardActivity not found", e)
            showToast("Dashboard not available")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
