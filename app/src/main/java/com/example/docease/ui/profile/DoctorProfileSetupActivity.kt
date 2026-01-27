package com.example.docease.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.docease.R
import com.example.docease.firebase.AuthManager
import com.example.docease.firebase.DatabaseManager
import com.example.docease.repository.DoctorRepository
import com.example.docease.utils.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import android.os.Handler
import android.os.Looper

/**
 * DoctorProfileSetupActivity - Doctor profile setup screen
 * 
 * Allows doctors to set up their profile with:
 * - Profile photo
 * - Full name
 * - Specialization
 * - Clinic address
 * - Consultation fee
 */
class DoctorProfileSetupActivity : AppCompatActivity() {

    // ═══════════════════════════════════════════════════════════════════════
    // UI Components
    // ═══════════════════════════════════════════════════════════════════════
    
    private lateinit var rootView: View
    private lateinit var btnBack: ImageButton
    private lateinit var avatarCard: MaterialCardView
    private lateinit var imgAvatar: ImageView
    private lateinit var btnCamera: FrameLayout
    private lateinit var tvChangePhoto: TextView
    
    // Form Fields
    private lateinit var etFullName: EditText
    private lateinit var dropdownSpecialization: LinearLayout
    private lateinit var tvSpecialization: TextView
    private lateinit var etClinicAddress: EditText
    private lateinit var etConsultationFee: EditText
    
    // Button
    private lateinit var btnSave: Button

    // ═══════════════════════════════════════════════════════════════════════
    // Data
    // ═══════════════════════════════════════════════════════════════════════
    
    private var selectedSpecialization: String? = null
    private var selectedImageUri: Uri? = null
    private var selectedImageBitmap: Bitmap? = null
    
    private val specializations by lazy {
        resources.getStringArray(R.array.specializations).drop(1) // Remove "Select Specialization"
    }
    
    private lateinit var prefManager: PreferenceManager

    // ═══════════════════════════════════════════════════════════════════════
    // Activity Result Launchers
    // ═══════════════════════════════════════════════════════════════════════
    
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                imgAvatar.setImageURI(uri)
                imgAvatar.scaleType = ImageView.ScaleType.CENTER_CROP
            }
        }
    }
    
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            bitmap?.let {
                selectedImageBitmap = it
                imgAvatar.setImageBitmap(it)
                imgAvatar.scaleType = ImageView.ScaleType.CENTER_CROP
            }
        }
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            showImagePickerDialog()
        } else {
            showSnackbar("Camera and storage permissions are required to change photo")
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Lifecycle
    // ═══════════════════════════════════════════════════════════════════════
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_doctor_profile_setup)
        
        // Initialize PreferenceManager
        prefManager = PreferenceManager(this)

        // Apply window insets
        setupWindowInsets()

        // Initialize UI components
        initViews()

        // Setup click listeners
        setupClickListeners()

        // Setup text watchers
        setupTextWatchers()

        // Play entrance animations
        playEntranceAnimations()
        
        // Load existing profile data if available
        loadExistingProfile()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Setup Methods
    // ═══════════════════════════════════════════════════════════════════════
    
    private fun setupWindowInsets() {
        rootView = findViewById(R.id.scrollView)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        avatarCard = findViewById(R.id.avatarCard)
        imgAvatar = findViewById(R.id.imgAvatar)
        btnCamera = findViewById(R.id.btnCamera)
        tvChangePhoto = findViewById(R.id.tvChangePhoto)
        
        etFullName = findViewById(R.id.etFullName)
        dropdownSpecialization = findViewById(R.id.dropdownSpecialization)
        tvSpecialization = findViewById(R.id.tvSpecialization)
        etClinicAddress = findViewById(R.id.etClinicAddress)
        etConsultationFee = findViewById(R.id.etConsultationFee)
        
        btnSave = findViewById(R.id.btnSave)
    }

    private fun setupClickListeners() {
        // Back button
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        // Photo selection
        btnCamera.setOnClickListener { checkPermissionsAndShowPicker() }
        tvChangePhoto.setOnClickListener { checkPermissionsAndShowPicker() }
        avatarCard.setOnClickListener { checkPermissionsAndShowPicker() }
        
        // Specialization dropdown
        dropdownSpecialization.setOnClickListener {
            showSpecializationPicker()
        }
        
        // Save button
        btnSave.setOnClickListener {
            if (validateForm()) {
                saveProfile()
            }
        }
    }

    private fun setupTextWatchers() {
        // Update Save button state based on form validity
        val textWatcher = { 
            btnSave.isEnabled = isFormValid()
        }
        
        etFullName.addTextChangedListener { textWatcher() }
        etClinicAddress.addTextChangedListener { textWatcher() }
        etConsultationFee.addTextChangedListener { textWatcher() }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Permission Handling
    // ═══════════════════════════════════════════════════════════════════════
    
    private fun checkPermissionsAndShowPicker() {
        val permissions = mutableListOf<String>()
        
        // Camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }
        
        // Storage permission (for older Android versions)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }
        
        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        } else {
            showImagePickerDialog()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Image Picker
    // ═══════════════════════════════════════════════════════════════════════
    
    private fun showImagePickerDialog() {
        val bottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_image_picker, null)
        
        view.findViewById<LinearLayout>(R.id.optionCamera)?.setOnClickListener {
            bottomSheet.dismiss()
            openCamera()
        }
        
        view.findViewById<LinearLayout>(R.id.optionGallery)?.setOnClickListener {
            bottomSheet.dismiss()
            openGallery()
        }
        
        view.findViewById<LinearLayout>(R.id.optionRemove)?.setOnClickListener {
            bottomSheet.dismiss()
            removePhoto()
        }
        
        bottomSheet.setContentView(view)
        bottomSheet.show()
    }
    
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }
    
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }
    
    private fun removePhoto() {
        selectedImageUri = null
        selectedImageBitmap = null
        imgAvatar.setImageResource(R.drawable.ic_doctor_avatar)
        imgAvatar.scaleType = ImageView.ScaleType.FIT_CENTER
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Specialization Picker
    // ═══════════════════════════════════════════════════════════════════════
    
    private fun showSpecializationPicker() {
        val currentIndex = specializations.indexOf(selectedSpecialization).takeIf { it >= 0 } ?: -1
        
        AlertDialog.Builder(this, R.style.DocEaseAlertDialog)
            .setTitle("Select Specialization")
            .setSingleChoiceItems(
                specializations.toTypedArray(),
                currentIndex
            ) { dialog, which ->
                selectedSpecialization = specializations[which]
                tvSpecialization.text = selectedSpecialization
                tvSpecialization.setTextColor(ContextCompat.getColor(this, R.color.docease_text_primary))
                btnSave.isEnabled = isFormValid()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Form Validation
    // ═══════════════════════════════════════════════════════════════════════
    
    private fun isFormValid(): Boolean {
        val name = etFullName.text.toString().trim()
        val address = etClinicAddress.text.toString().trim()
        val fee = etConsultationFee.text.toString().trim()
        
        return name.isNotEmpty() && 
               selectedSpecialization != null && 
               address.isNotEmpty() && 
               fee.isNotEmpty()
    }

    private fun validateForm(): Boolean {
        val name = etFullName.text.toString().trim()
        val address = etClinicAddress.text.toString().trim()
        val fee = etConsultationFee.text.toString().trim()
        
        // Validate name
        if (name.isEmpty()) {
            showFieldError(etFullName, "Please enter your full name")
            return false
        }
        
        if (name.length < 3) {
            showFieldError(etFullName, "Name must be at least 3 characters")
            return false
        }
        
        // Validate specialization
        if (selectedSpecialization == null) {
            showSnackbar("Please select your specialization")
            shakeView(dropdownSpecialization)
            return false
        }
        
        // Validate address
        if (address.isEmpty()) {
            showFieldError(etClinicAddress, "Please enter your clinic address")
            return false
        }
        
        if (address.length < 10) {
            showFieldError(etClinicAddress, "Please enter a complete address")
            return false
        }
        
        // Validate fee
        if (fee.isEmpty()) {
            showFieldError(etConsultationFee, "Please enter your consultation fee")
            return false
        }
        
        val feeValue = fee.toDoubleOrNull()
        if (feeValue == null || feeValue <= 0) {
            showFieldError(etConsultationFee, "Please enter a valid fee amount")
            return false
        }
        
        return true
    }

    private fun showFieldError(editText: EditText, message: String) {
        editText.error = message
        shakeView(editText.parent as View)
        editText.requestFocus()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Save Profile
    // ═══════════════════════════════════════════════════════════════════════
    
    private fun saveProfile() {
        val name = etFullName.text.toString().trim()
        val specialization = selectedSpecialization ?: return
        val address = etClinicAddress.text.toString().trim()
        val fee = etConsultationFee.text.toString().trim().toDoubleOrNull() ?: 0.0

        // Show loading state
        btnSave.isEnabled = false
        btnSave.text = "Saving..."

        android.util.Log.d("DoctorProfileSetup", "saveProfile: name=$name specialization=$specialization fee=$fee")

        // Get current user ID
        val userId = AuthManager.getInstance().getCurrentUserId()

        if (userId == null) {
            showSnackbar("Please login again")
            btnSave.isEnabled = true
            btnSave.text = "Save Profile"
            return
        }

        // Create profile data map
        val profileData = mutableMapOf<String, Any>(
            "name" to name,
            "specialization" to specialization,
            "clinicAddress" to address,
            "consultationFee" to fee,
            "profileComplete" to true,
            "updatedAt" to System.currentTimeMillis()
        )

        // Safety timeout: recover UI if save stalls
        val handler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            android.util.Log.w("DoctorProfileSetup", "saveProfile: timeout")
            btnSave.isEnabled = true
            btnSave.text = "Save Profile"
            showSnackbar("Save timed out. Please check your connection and try again.")
        }
        handler.postDelayed(timeoutRunnable, 15_000)

        try {
            val usersRef = DatabaseManager.getInstance().getUsersRef().child(userId)
            val doctorRef = DatabaseManager.getInstance().getDoctorsRef().child(userId)

            // Ensure user's role is 'doctor' before writing doctor profile
            usersRef.child("role").get().addOnCompleteListener { roleTask ->
                if (roleTask.isSuccessful) {
                    val currentRole = roleTask.result?.getValue(String::class.java)
                    android.util.Log.d("DoctorProfileSetup", "currentRole=$currentRole")

                    val proceedToWriteDoctor = {
                        // Perform an atomic multi-path update so role + doctor node are updated together.
                        val rootRef = DatabaseManager.getInstance().getReference("/")

                        val updates = mutableMapOf<String, Any?>(
                            "doctors/$userId/uid" to userId,
                            "doctors/$userId/name" to name,
                            "doctors/$userId/email" to AuthManager.getInstance().getCurrentUser()?.email,
                            "doctors/$userId/specialization" to specialization,
                            "doctors/$userId/clinicAddress" to address,
                            "doctors/$userId/consultationFee" to fee,
                            "doctors/$userId/profileComplete" to true,
                            "doctors/$userId/updatedAt" to System.currentTimeMillis()
                        )

                        // If role not already doctor, set users/{uid}/role to 'doctor' in same update
                        if (currentRole != "doctor") {
                            updates["users/$userId/role"] = "doctor"
                        }

                        android.util.Log.d("DoctorProfileSetup", "perform atomic update: keys=${updates.keys}")

                        rootRef.updateChildren(updates).addOnCompleteListener { task ->
                            handler.removeCallbacks(timeoutRunnable)

                            runOnUiThread {
                                if (task.isSuccessful) {
                                    // Save locally
                                    prefManager.saveDoctorName(name)
                                    prefManager.setProfileComplete(true)

                                    showSnackbar("Profile saved successfully!")

                                    // Navigate to dashboard after short delay
                                    btnSave.postDelayed({
                                        navigateToDashboard()
                                    }, 1000)
                                } else {
                                    btnSave.isEnabled = true
                                    btnSave.text = "Save Profile"
                                    val msg = task.exception?.message ?: "Failed to save profile. Please try again."
                                    if (msg.contains("Permission denied", ignoreCase = true)) {
                                        showSnackbar("Permission denied. Check database rules: users/{uid}/role must be 'doctor' and you must be the authenticated user.")
                                    } else {
                                        showSnackbar(msg)
                                    }
                                    android.util.Log.e("DoctorProfileSetup", "atomic update failed", task.exception)
                                }
                            }
                        }.addOnFailureListener { e ->
                            handler.removeCallbacks(timeoutRunnable)
                            runOnUiThread {
                                btnSave.isEnabled = true
                                btnSave.text = "Save Profile"
                                showSnackbar("Error saving profile: ${e.message}")
                            }
                            android.util.Log.e("DoctorProfileSetup", "atomic update exception", e)
                        }
                    }

                    if (currentRole == "doctor") {
                        // role already set, proceed
                        proceedToWriteDoctor()
                    } else {
                        // update full user record to include role=doctor (must satisfy users validation)
                        val userMap = mapOf(
                            "uid" to userId,
                            "email" to (AuthManager.getInstance().getCurrentUser()?.email ?: ""),
                            "role" to "doctor",
                            "createdAt" to System.currentTimeMillis()
                        )

                        usersRef.setValue(userMap).addOnCompleteListener { setUserTask ->
                            if (setUserTask.isSuccessful) {
                                android.util.Log.d("DoctorProfileSetup", "users node updated with role=doctor, proceeding to save doctor profile")
                                proceedToWriteDoctor()
                            } else {
                                handler.removeCallbacks(timeoutRunnable)
                                runOnUiThread {
                                    btnSave.isEnabled = true
                                    btnSave.text = "Save Profile"
                                    val msg = setUserTask.exception?.message ?: "Failed to update user role."
                                    showSnackbar(msg)
                                }
                                android.util.Log.e("DoctorProfileSetup", "set user failed", setUserTask.exception)
                            }
                        }.addOnFailureListener { e ->
                            handler.removeCallbacks(timeoutRunnable)
                            runOnUiThread {
                                btnSave.isEnabled = true
                                btnSave.text = "Save Profile"
                                showSnackbar("Error updating user role: ${e.message}")
                            }
                            android.util.Log.e("DoctorProfileSetup", "set user exception", e)
                        }
                    }
                } else {
                    handler.removeCallbacks(timeoutRunnable)
                    btnSave.isEnabled = true
                    btnSave.text = "Save Profile"
                    val msg = roleTask.exception?.message ?: "Failed to read user role"
                    showSnackbar(msg)
                    android.util.Log.e("DoctorProfileSetup", "read role failed", roleTask.exception)
                }
            }
        } catch (e: Exception) {
            handler.removeCallbacks(timeoutRunnable)
            btnSave.isEnabled = true
            btnSave.text = "Save Profile"
            showSnackbar("Error: ${e.message}")
            android.util.Log.e("DoctorProfileSetup", "saveProfile exception", e)
        }
    }

    private fun navigateToDashboard() {
        try {
            val intent = Intent(this, Class.forName("com.example.docease.ui.dashboard.DoctorDashboardActivity"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: ClassNotFoundException) {
            showSnackbar("Dashboard screen coming soon!")
            finish()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Load Existing Profile
    // ═══════════════════════════════════════════════════════════════════════
    
    private fun loadExistingProfile() {
        val userId = AuthManager.getInstance().getCurrentUserId() ?: return
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                DatabaseManager.getInstance().getDoctorsRef().child(userId).get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").getValue(String::class.java)
                        val specialization = snapshot.child("specialization").getValue(String::class.java)
                        val address = snapshot.child("clinicAddress").getValue(String::class.java)
                        val fee = snapshot.child("consultationFee").getValue(Double::class.java)
                        
                        // Update UI on main thread
                        CoroutineScope(Dispatchers.Main).launch {
                            name?.let { etFullName.setText(it) }
                            specialization?.let { 
                                selectedSpecialization = it
                                tvSpecialization.text = it
                                tvSpecialization.setTextColor(
                                    ContextCompat.getColor(this@DoctorProfileSetupActivity, R.color.docease_text_primary)
                                )
                            }
                            address?.let { etClinicAddress.setText(it) }
                            fee?.let { etConsultationFee.setText(String.format("%.2f", it)) }
                        }
                    }
                }
            } catch (e: Exception) {
                // Silently fail - this is optional loading
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Animation & UI Helpers
    // ═══════════════════════════════════════════════════════════════════════
    
    private fun playEntranceAnimations() {
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in).apply {
            duration = 500
        }
        
        val slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left).apply {
            duration = 400
            startOffset = 200
        }
        
        findViewById<View>(R.id.avatarContainer).startAnimation(fadeIn)
        findViewById<View>(R.id.tvChangePhoto).startAnimation(fadeIn)
    }

    private fun shakeView(view: View) {
        val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
        view.startAnimation(shake)
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
    }
}
