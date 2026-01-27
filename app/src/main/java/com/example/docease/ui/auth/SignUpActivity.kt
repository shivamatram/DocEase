package com.example.docease.ui.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.addTextChangedListener
import com.example.docease.R
import com.example.docease.firebase.AuthManager
import com.example.docease.firebase.DatabaseManager
import com.example.docease.models.User
import com.example.docease.models.UserRole
import com.example.docease.models.Doctor
import com.example.docease.utils.PreferenceManager
import android.os.Handler
import android.os.Looper

/**
 * SignUpActivity - User registration screen
 * 
 * Features:
 * - Role selection (Patient/Doctor)
 * - Full name, email, password fields
 * - Password confirmation with visibility toggle
 * - Terms & Privacy acceptance
 * - Social sign-up (Google, Apple)
 * - Input validation
 * - Firebase Authentication integration
 */
class SignUpActivity : AppCompatActivity() {

    // ═══════════════════════════════════════════════════════════════════════
    // UI Components
    // ═══════════════════════════════════════════════════════════════════════
    
    private lateinit var btnBack: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    
    // Role Toggle
    private lateinit var tabPatient: TextView
    private lateinit var tabDoctor: TextView
    
    // Input Fields
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnTogglePassword: ImageButton
    private lateinit var btnToggleConfirmPassword: ImageButton
    
    // Input Layouts
    private lateinit var layoutFullNameInput: LinearLayout
    private lateinit var layoutEmailInput: LinearLayout
    private lateinit var layoutPasswordInput: LinearLayout
    private lateinit var layoutConfirmPasswordInput: LinearLayout
    
    // Terms & Buttons
    private lateinit var cbTerms: CheckBox
    private lateinit var tvTermsText: TextView
    private lateinit var btnSignUp: Button
    private lateinit var btnGoogle: LinearLayout
    private lateinit var btnApple: LinearLayout
    private lateinit var tvLoginLink: TextView

    // ═══════════════════════════════════════════════════════════════════════
    // State
    // ═══════════════════════════════════════════════════════════════════════
    
    private var selectedRole: UserRole = UserRole.PATIENT
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private lateinit var tvSetPassword: TextView
    private var wantsToSetPassword = false
    // When true, user is completing profile after social (Google) auth; don't create a new Auth account
    private var isSocialSignIn = false
    
    // Managers
    private lateinit var authManager: AuthManager
    private lateinit var databaseManager: DatabaseManager
    private lateinit var prefManager: PreferenceManager

    // ═══════════════════════════════════════════════════════════════════════
    // Lifecycle
    // ═══════════════════════════════════════════════════════════════════════

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge
        enableEdgeToEdge()
        
        setContentView(R.layout.activity_signup)

        // Initialize managers
        initManagers()

        // Initialize views
        initViews()

        // Setup click listeners
        setupClickListeners()

        // Setup input listeners
        setupInputListeners()

        // Setup terms links
        setupTermsLinks()

        // Setup login link
        setupLoginLink()

        // Handle intent extras (if coming from login with email pre-filled)
        handleIntentExtras()

        // Start entrance animations
        startEntranceAnimations()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Initialization
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Enable edge-to-edge display with light status bar
     */
    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true
        windowInsetsController.isAppearanceLightNavigationBars = true
    }

    /**
     * Initialize managers
     */
    private fun initManagers() {
        authManager = AuthManager.getInstance()
        databaseManager = DatabaseManager.getInstance(this)
        prefManager = PreferenceManager(this)
    }

    /**
     * Initialize view references
     */
    private fun initViews() {
        // Top section
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)
        tvSubtitle = findViewById(R.id.tvSubtitle)
        
        // Role toggle
        tabPatient = findViewById(R.id.tabPatient)
        tabDoctor = findViewById(R.id.tabDoctor)
        
        // Input fields
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnTogglePassword = findViewById(R.id.btnTogglePassword)
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword)
        
        // Input layouts
        layoutFullNameInput = findViewById(R.id.layoutFullNameInput)
        layoutEmailInput = findViewById(R.id.layoutEmailInput)
        layoutPasswordInput = findViewById(R.id.layoutPasswordInput)
        layoutConfirmPasswordInput = findViewById(R.id.layoutConfirmPasswordInput)
        
        // Terms & Buttons
        cbTerms = findViewById(R.id.cbTerms)
        tvTermsText = findViewById(R.id.tvTermsText)
        btnSignUp = findViewById(R.id.btnSignUp)
        btnGoogle = findViewById(R.id.btnGoogle)
        btnApple = findViewById(R.id.btnApple)
        tvLoginLink = findViewById(R.id.tvLoginLink)
    }

    /**
     * Setup all click listeners
     */
    private fun setupClickListeners() {
        // Back button
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Role toggle tabs
        tabPatient.setOnClickListener {
            selectRole(UserRole.PATIENT)
        }
        
        tabDoctor.setOnClickListener {
            selectRole(UserRole.DOCTOR)
        }

        // Password visibility toggles
        btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }
        
        btnToggleConfirmPassword.setOnClickListener {
            toggleConfirmPasswordVisibility()
        }

        // Sign Up button
        btnSignUp.setOnClickListener {
            performSignUp()
        }

        // Social sign-up buttons
        btnGoogle.setOnClickListener {
            performGoogleSignUp()
        }

        btnApple.setOnClickListener {
            performAppleSignUp()
        }

        // Terms checkbox click on text area
        tvTermsText.setOnClickListener {
            cbTerms.isChecked = !cbTerms.isChecked
        }
    }

    /**
     * Setup input field listeners
     */
    private fun setupInputListeners() {
        // Focus change listeners for visual feedback
        etFullName.setOnFocusChangeListener { _, hasFocus ->
            updateInputFieldState(layoutFullNameInput, hasFocus)
        }
        
        etEmail.setOnFocusChangeListener { _, hasFocus ->
            updateInputFieldState(layoutEmailInput, hasFocus)
        }
        
        etPassword.setOnFocusChangeListener { _, hasFocus ->
            updateInputFieldState(layoutPasswordInput, hasFocus)
        }
        
        etConfirmPassword.setOnFocusChangeListener { _, hasFocus ->
            updateInputFieldState(layoutConfirmPasswordInput, hasFocus)
        }

        // Clear errors on text change
        etFullName.addTextChangedListener { clearInputError(layoutFullNameInput) }
        etEmail.addTextChangedListener { clearInputError(layoutEmailInput) }
        etPassword.addTextChangedListener { clearInputError(layoutPasswordInput) }
        etConfirmPassword.addTextChangedListener { clearInputError(layoutConfirmPasswordInput) }
    }

    /**
     * Setup clickable links in terms text
     */
    private fun setupTermsLinks() {
        val termsText = "I agree to the Terms of Service and Privacy Policy."
        val spannableString = SpannableString(termsText)

        // Terms of Service click
        val termsStart = termsText.indexOf("Terms of Service")
        val termsEnd = termsStart + "Terms of Service".length
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    openTermsOfService()
                }
                
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = ContextCompat.getColor(this@SignUpActivity, R.color.docease_primary)
                    ds.isUnderlineText = false
                }
            },
            termsStart,
            termsEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Privacy Policy click
        val privacyStart = termsText.indexOf("Privacy Policy")
        val privacyEnd = privacyStart + "Privacy Policy".length
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    openPrivacyPolicy()
                }
                
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = ContextCompat.getColor(this@SignUpActivity, R.color.docease_primary)
                    ds.isUnderlineText = false
                }
            },
            privacyStart,
            privacyEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvTermsText.text = spannableString
        tvTermsText.movementMethod = LinkMovementMethod.getInstance()
        tvTermsText.highlightColor = Color.TRANSPARENT
    }

    /**
     * Setup clickable "Log In" link in footer
     */
    private fun setupLoginLink() {
        val loginText = "Already have an account? Log In"
        val spannableString = SpannableString(loginText)
        
        val loginStart = loginText.indexOf("Log In")
        val loginEnd = loginStart + "Log In".length
        
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    navigateToLogin()
                }
                
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = ContextCompat.getColor(this@SignUpActivity, R.color.docease_primary)
                    ds.isUnderlineText = false
                    ds.isFakeBoldText = true
                }
            },
            loginStart,
            loginEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvLoginLink.text = spannableString
        tvLoginLink.movementMethod = LinkMovementMethod.getInstance()
        tvLoginLink.highlightColor = Color.TRANSPARENT
    }

    /**
     * Handle intent extras from LoginActivity
     */
    private fun handleIntentExtras() {
        intent?.let {
            val email = it.getStringExtra("email")
            val password = it.getStringExtra("password")

            if (!email.isNullOrEmpty()) {
                etEmail.setText(email)
            }
            if (!password.isNullOrEmpty()) {
                etPassword.setText(password)
            }

            // If redirected from Google sign-in or there's already a Firebase user, enable social-complete mode
            val fromGoogle = it.getBooleanExtra(com.example.docease.utils.Constants.Extras.EXTRA_FROM_GOOGLE, false)
            if (fromGoogle || com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null) {
                isSocialSignIn = true

                // Make password fields visible and editable (optional) so users can set a password directly
                etPassword.visibility = View.VISIBLE
                etConfirmPassword.visibility = View.VISIBLE
                btnTogglePassword.visibility = View.VISIBLE
                btnToggleConfirmPassword.visibility = View.VISIBLE

                // Hint that password is optional for social-complete
                etPassword.hint = "Create a password (optional)"
                etConfirmPassword.hint = "Re-enter password (optional)"

                // Ensure fields are enabled and focusable
                etPassword.isEnabled = true
                etPassword.isFocusable = true
                etPassword.isFocusableInTouchMode = true
                etConfirmPassword.isEnabled = true
                etConfirmPassword.isFocusable = true
                etConfirmPassword.isFocusableInTouchMode = true

                // Update button text and subtitle to indicate continuation
                btnSignUp.text = "Continue"
                tvSubtitle.text = "Complete your profile to finish setting up your account."

                // If user begins typing in the password fields, treat it as wantsToSetPassword = true
                etPassword.addTextChangedListener { text ->
                    if (!text.isNullOrEmpty()) wantsToSetPassword = true
                }
                etConfirmPassword.addTextChangedListener { text ->
                    if (!text.isNullOrEmpty()) wantsToSetPassword = true
                }
            }

            // If caller provided a role, pre-select it and hide the opposite tab so user can't change it
            val roleExtra = it.getStringExtra(com.example.docease.utils.Constants.Extras.EXTRA_USER_ROLE)
            if (!roleExtra.isNullOrEmpty()) {
                val role = try {
                    UserRole.valueOf(roleExtra.uppercase())
                } catch (e: Exception) {
                    UserRole.PATIENT
                }
                selectRole(role)

                when (role) {
                    UserRole.DOCTOR -> {
                        // Hide patient tab to prevent changing
                        tabPatient.visibility = View.GONE
                    }
                    UserRole.PATIENT -> {
                        // Hide doctor tab to prevent changing
                        tabDoctor.visibility = View.GONE
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Role Selection
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Select role (Patient or Doctor)
     */
    private fun selectRole(role: UserRole) {
        selectedRole = role
        
        when (role) {
            UserRole.PATIENT -> {
                // Patient active
                tabPatient.background = ContextCompat.getDrawable(this, R.drawable.bg_role_tab_active)
                tabPatient.setTextColor(ContextCompat.getColor(this, R.color.docease_button_text_primary))
                
                // Doctor inactive
                tabDoctor.background = ContextCompat.getDrawable(this, R.drawable.bg_role_tab_inactive)
                tabDoctor.setTextColor(ContextCompat.getColor(this, R.color.docease_text_secondary))
                
                // Update subtitle
                tvSubtitle.text = "Fill in your details to start booking appointments."
            }
            
            UserRole.DOCTOR -> {
                // Doctor active
                tabDoctor.background = ContextCompat.getDrawable(this, R.drawable.bg_role_tab_active)
                tabDoctor.setTextColor(ContextCompat.getColor(this, R.color.docease_button_text_primary))
                
                // Patient inactive
                tabPatient.background = ContextCompat.getDrawable(this, R.drawable.bg_role_tab_inactive)
                tabPatient.setTextColor(ContextCompat.getColor(this, R.color.docease_text_secondary))
                
                // Update subtitle
                tvSubtitle.text = "Fill in your details to start managing appointments."
            }
        }
        
        // Animate the tab switch
        animateRoleSwitch()
    }

    /**
     * Animate role tab switch
     */
    private fun animateRoleSwitch() {
        val activeTab = if (selectedRole == UserRole.PATIENT) tabPatient else tabDoctor
        activeTab.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                activeTab.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Password Visibility
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Toggle password visibility
     */
    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        
        if (isPasswordVisible) {
            etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or 
                                   android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_visibility)
        } else {
            etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or 
                                   android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_off)
        }
        
        etPassword.setSelection(etPassword.text.length)
    }

    /**
     * Toggle confirm password visibility
     */
    private fun toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible
        
        if (isConfirmPasswordVisible) {
            etConfirmPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or 
                                          android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnToggleConfirmPassword.setImageResource(R.drawable.ic_visibility)
        } else {
            etConfirmPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or 
                                          android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnToggleConfirmPassword.setImageResource(R.drawable.ic_visibility_off)
        }
        
        etConfirmPassword.setSelection(etConfirmPassword.text.length)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Validation
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Validate full name
     */
    private fun isValidFullName(name: String): Boolean {
        return name.trim().length >= 2 && name.contains(" ").not() || name.trim().split(" ").size >= 1
    }

    /**
     * Validate email format
     */
    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validate password strength
     */
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Validate password match
     */
    private fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    /**
     * Validate all input fields
     */
    private fun validateInputs(): Boolean {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()
        
        // Validate full name
        if (fullName.isEmpty()) {
            showInputError(layoutFullNameInput)
            showToast("Please enter your full name")
            etFullName.requestFocus()
            return false
        }
        
        if (fullName.length < 2) {
            showInputError(layoutFullNameInput)
            showToast("Name must be at least 2 characters")
            etFullName.requestFocus()
            return false
        }
        
        // Validate email
        if (!isValidEmail(email)) {
            showInputError(layoutEmailInput)
            if (email.isEmpty()) {
                showToast("Please enter your email address")
            } else {
                showToast("Please enter a valid email address")
            }
            etEmail.requestFocus()
            return false
        }
        
        // Validate password (skip for social sign-in flows unless user chose to set a password)
        if (!isSocialSignIn || wantsToSetPassword) {
            if (!isValidPassword(password)) {
                showInputError(layoutPasswordInput)
                if (password.isEmpty()) {
                    showToast("Please create a password")
                } else {
                    showToast("Password must be at least 6 characters")
                }
                etPassword.requestFocus()
                return false
            }

            // Validate password match
            if (!doPasswordsMatch(password, confirmPassword)) {
                showInputError(layoutConfirmPasswordInput)
                showToast("Passwords do not match")
                etConfirmPassword.requestFocus()
                return false
            }
        }
        
        // Validate terms acceptance
        if (!cbTerms.isChecked) {
            showToast("Please accept the Terms of Service and Privacy Policy")
            // Shake checkbox
            cbTerms.animate()
                .translationX(-10f)
                .setDuration(50)
                .withEndAction {
                    cbTerms.animate()
                        .translationX(10f)
                        .setDuration(50)
                        .withEndAction {
                            cbTerms.animate()
                                .translationX(0f)
                                .setDuration(50)
                                .start()
                        }
                        .start()
                }
                .start()
            return false
        }
        
        return true
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Input Field States
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Update input field visual state
     */
    private fun updateInputFieldState(layout: LinearLayout, hasFocus: Boolean) {
        if (hasFocus) {
            layout.background = ContextCompat.getDrawable(this, R.drawable.bg_input_field_focused)
        } else {
            layout.background = ContextCompat.getDrawable(this, R.drawable.bg_input_field)
        }
    }

    /**
     * Show error state on input field
     */
    private fun showInputError(layout: LinearLayout) {
        layout.background = ContextCompat.getDrawable(this, R.drawable.bg_input_field_focused)
    }

    /**
     * Clear error state from input field
     */
    private fun clearInputError(layout: LinearLayout) {
        if (!layout.hasFocus()) {
            layout.background = ContextCompat.getDrawable(this, R.drawable.bg_input_field)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Sign Up Actions
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Perform sign up with email/password
     */
    private fun performSignUp() {
        if (!validateInputs()) return

        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        // Show loading state
        setLoadingState(true)

        // Safety timeout: if DB write stalls, recover UI after 15s
        val handler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            android.util.Log.w("SignUpActivity", "performSignUp: save timeout")
            setLoadingState(false)
            showToast("Network timeout. Please check your connection and try again.")
        }
        handler.postDelayed(timeoutRunnable, 15_000)

        // If user came from social sign-in, they are already authenticated; only save DB profile
        if (isSocialSignIn) {
            val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (firebaseUser == null) {
                setLoadingState(false)
                showToast("Authentication required. Please sign in with Google first.")
                return
            }

            val userId = firebaseUser.uid

            fun continueSaveProfile() {
                val user = User(
                    uid = userId,
                    email = email,
                    role = selectedRole
                )

                // Save user to database
                databaseManager.saveUser(user) { saveResult ->
                    handler.removeCallbacks(timeoutRunnable)
                    if (saveResult.isSuccess) {
                        prefManager.saveUserRole(selectedRole)
                        prefManager.saveUserId(userId)

                        if (selectedRole == UserRole.DOCTOR) {
                            // Save minimal doctor record
                            val doctor = Doctor(uid = userId, name = fullName, email = email)
                            DatabaseManager.getInstance().getDoctorsRef().child(userId)
                                .setValue(doctor.toMap()).addOnCompleteListener { docTask ->
                                    setLoadingState(false)
                                    if (docTask.isSuccessful) {
                                        try {
                                            val intent = Intent(this, Class.forName("com.example.docease.ui.profile.DoctorProfileSetupActivity"))
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                            finish()
                                        } catch (e: ClassNotFoundException) {
                                            navigateToDashboard()
                                        }
                                    } else {
                                        android.util.Log.e("SignUpActivity", "doctor save failed", docTask.exception)
                                        showToast("Failed to create doctor profile: ${docTask.exception?.message}")
                                    }
                                }
                        } else {
                            setLoadingState(false)
                            try {
                                val intent = Intent(this, Class.forName("com.example.docease.ui.profile.PatientProfileSetupActivity"))
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            } catch (e: ClassNotFoundException) {
                                navigateToDashboard()
                            }
                        }
                    } else {
                        setLoadingState(false)
                        android.util.Log.e("SignUpActivity", "saveUser failed", saveResult.exceptionOrNull())
                        showToast("Failed to create profile: ${saveResult.exceptionOrNull()?.message}")
                    }
                }
            }

            // If user requested to set a password, validate and update password on the Firebase user first
            if (wantsToSetPassword) {
                val password = etPassword.text.toString()
                val confirm = etConfirmPassword.text.toString()
                if (!isValidPassword(password)) {
                    setLoadingState(false)
                    showToast("Password must be at least 6 characters")
                    etPassword.requestFocus()
                    return
                }
                if (!doPasswordsMatch(password, confirm)) {
                    setLoadingState(false)
                    showToast("Passwords do not match")
                    etConfirmPassword.requestFocus()
                    return
                }

                // Update the password for the existing firebase user
                setLoadingState(true)
                firebaseUser.updatePassword(password).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        // Password set successfully; continue to save profile
                        continueSaveProfile()
                    } else {
                        setLoadingState(false)
                        android.util.Log.e("SignUpActivity", "updatePassword failed", updateTask.exception)
                        showToast("Failed to set password: ${updateTask.exception?.message}")
                    }
                }
            } else {
                continueSaveProfile()
            }

            return
        }

        // Regular email/password sign-up (existing behavior)
        android.util.Log.d("SignUpActivity", "performSignUp: creating user $email")
        authManager.createUserWithEmailAndPassword(email, password) { result ->
            android.util.Log.d("SignUpActivity", "createUserWithEmail callback: result=$result")
            result.fold(
                onSuccess = { userId ->
                    android.util.Log.d("SignUpActivity", "createUser success uid=$userId")

                    // Create user profile in database (minimal User model)
                    val user = User(
                        uid = userId,
                        email = email,
                        role = selectedRole
                    )

                    // Save user to database
                    databaseManager.saveUser(user) { saveResult ->
                        android.util.Log.d("SignUpActivity", "saveUser callback: $saveResult")

                        if (selectedRole == UserRole.DOCTOR) {
                            // Create a minimal doctor entry using the entered full name
                            val doctor = Doctor(
                                uid = userId,
                                name = fullName,
                                email = email
                            )

                            DatabaseManager.getInstance().getDoctorsRef().child(userId)
                                .setValue(doctor.toMap()).addOnCompleteListener { docTask ->
                                    handler.removeCallbacks(timeoutRunnable)

                                    if (saveResult.isSuccess && docTask.isSuccessful) {
                                        // Verify credentials by signing in, then navigate to dashboard
                                        authManager.signInWithEmailAndPassword(email, password) { signResult ->
                                            setLoadingState(false)
                                            signResult.fold(
                                                onSuccess = {
                                                    prefManager.saveUserRole(selectedRole)
                                                    prefManager.saveUserId(userId)
                                                    // For doctors, redirect to profile setup to complete details
                                                    try {
                                                        val intent = Intent(this, Class.forName("com.example.docease.ui.profile.DoctorProfileSetupActivity"))
                                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                        startActivity(intent)
                                                        finish()
                                                    } catch (e: ClassNotFoundException) {
                                                        // Fall back to dashboard
                                                        navigateToDashboard()
                                                    }
                                                },
                                                onFailure = { e ->
                                                    android.util.Log.e("SignUpActivity", "signIn after signUp failed", e)
                                                    showToast(e.message ?: "Sign-in failed. Please try logging in.")
                                                }
                                            )
                                        }
                                    } else {
                                        setLoadingState(false)
                                        val ex = saveResult.exceptionOrNull() ?: docTask.exception ?: Exception("Failed to save doctor profile")
                                        android.util.Log.e("SignUpActivity", "doctor save failed", ex)
                                        showToast("Failed to create profile: ${ex.message}")
                                    }
                                }
                        } else {
                            // Patient flow: remove timeout, sign-in to verify and redirect to profile setup
                            handler.removeCallbacks(timeoutRunnable)

                            saveResult.fold(
                                onSuccess = {
                                    authManager.signInWithEmailAndPassword(email, password) { signResult ->
                                        setLoadingState(false)
                                        signResult.fold(
                                            onSuccess = {
                                                prefManager.saveUserRole(selectedRole)
                                                prefManager.saveUserId(userId)
                                                // For patients, redirect to profile setup to complete details
                                                try {
                                                    val intent = Intent(this, Class.forName("com.example.docease.ui.profile.PatientProfileSetupActivity"))
                                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    startActivity(intent)
                                                    finish()
                                                } catch (e: ClassNotFoundException) {
                                                    // Fall back to dashboard
                                                    navigateToDashboard()
                                                }
                                            },
                                            onFailure = { e ->
                                                android.util.Log.e("SignUpActivity", "signIn after signUp failed", e)
                                                showToast(e.message ?: "Sign-in failed. Please try logging in.")
                                            }
                                        )
                                    }
                                },
                                onFailure = { exception ->
                                    setLoadingState(false)
                                    android.util.Log.e("SignUpActivity", "saveUser failed", exception)
                                    showToast("Failed to create profile: ${exception.message}")
                                }
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    handler.removeCallbacks(timeoutRunnable)
                    android.util.Log.e("SignUpActivity", "createUser failed", exception)
                    setLoadingState(false)
                    showToast(exception.message ?: "Sign up failed. Please try again.")
                }
            )
        }
    }

    /**
     * Perform Google Sign-Up
     */
    private fun performGoogleSignUp() {
        showToast("Google Sign-Up coming soon!")
        // TODO: Implement Google Sign-In with Firebase
    }

    /**
     * Perform Apple Sign-Up
     */
    private fun performAppleSignUp() {
        showToast("Apple Sign-Up coming soon!")
        // TODO: Implement Apple Sign-In with Firebase
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Navigation
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Navigate to Login screen
     */
    private fun navigateToLogin() {
        finish() // Just go back to login
    }

    /**
     * Navigate to appropriate dashboard based on user role
     */
    private fun navigateToDashboard() {
        val dashboardClass = when (selectedRole) {
            UserRole.DOCTOR -> "com.example.docease.ui.doctor.DoctorDashboardActivity"
            UserRole.PATIENT -> "com.example.docease.ui.patient.PatientDashboardActivity"
        }
        
        try {
            val intent = Intent(this, Class.forName(dashboardClass))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: ClassNotFoundException) {
            // Dashboard not yet created - go to profile setup
            try {
                val profileSetupClass = when (selectedRole) {
                    UserRole.DOCTOR -> "com.example.docease.ui.profile.DoctorProfileSetupActivity"
                    UserRole.PATIENT -> "com.example.docease.ui.profile.PatientProfileSetupActivity"
                }
                val intent = Intent(this, Class.forName(profileSetupClass))
                intent.putExtra("role", selectedRole.name)
                startActivity(intent)
                finish()
            } catch (e: ClassNotFoundException) {
                showToast("Welcome to DocEase! Dashboard coming soon.")
                finish()
            }
        }
    }

    /**
     * Open Terms of Service
     */
    private fun openTermsOfService() {
        showToast("Terms of Service")
        // TODO: Open Terms of Service URL or Activity
    }

    /**
     * Open Privacy Policy
     */
    private fun openPrivacyPolicy() {
        showToast("Privacy Policy")
        // TODO: Open Privacy Policy URL or Activity
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UI Helpers
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Set loading state
     */
    private fun setLoadingState(isLoading: Boolean) {
        btnSignUp.isEnabled = !isLoading
        btnSignUp.text = if (isLoading) "Creating Account..." else "Sign Up"
        
        // Disable other elements during loading
        tabPatient.isEnabled = !isLoading
        tabDoctor.isEnabled = !isLoading
        btnGoogle.isEnabled = !isLoading
        btnApple.isEnabled = !isLoading
        cbTerms.isEnabled = !isLoading
    }

    /**
     * Show toast message
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    /**
     * Start entrance animations
     */
    private fun startEntranceAnimations() {
        // Fade in title
        tvTitle.alpha = 0f
        tvTitle.translationY = -20f
        tvTitle.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(100)
            .start()

        // Fade in subtitle
        tvSubtitle.alpha = 0f
        tvSubtitle.animate()
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(200)
            .start()
    }
}
