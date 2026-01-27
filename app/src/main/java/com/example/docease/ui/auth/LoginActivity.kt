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
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.addTextChangedListener
import com.example.docease.R
import com.example.docease.firebase.AuthManager
import com.example.docease.firebase.DatabaseManager
import com.example.docease.models.User
import com.example.docease.models.UserRole
import com.example.docease.utils.Constants
import com.example.docease.utils.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

/**
 * LoginActivity - User authentication screen
 * 
 * Features:
 * - Email/Password login
 * - Social login (Google, Apple)
 * - Login/SignUp toggle
 * - Password visibility toggle
 * - Forgot password link
 * - Input validation
 * - Firebase Authentication ready
 */
class LoginActivity : AppCompatActivity() {

    // ═══════════════════════════════════════════════════════════════════════
    // UI Components
    // ═══════════════════════════════════════════════════════════════════════
    
    private lateinit var btnBack: ImageButton
    private lateinit var cardIllustration: CardView
    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    
    // Tabs
    private lateinit var tabLogin: TextView
    private lateinit var tabSignUp: TextView
    
    // Input Fields
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnTogglePassword: ImageButton
    private lateinit var layoutEmailInput: LinearLayout
    private lateinit var layoutPasswordInput: LinearLayout
    
    // Buttons
    private lateinit var tvForgotPassword: TextView
    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: LinearLayout
    private lateinit var btnApple: LinearLayout
    
    // Footer
    private lateinit var tvFooter: TextView

    // ═══════════════════════════════════════════════════════════════════════
    // State
    // ═══════════════════════════════════════════════════════════════════════
    
    private var isPasswordVisible = false
    private var isLoginMode = true
    
    // Managers
    private lateinit var authManager: AuthManager
    private lateinit var prefManager: PreferenceManager

    // Google sign-in client
    private lateinit var googleSignInClient: GoogleSignInClient
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // ═══════════════════════════════════════════════════════════════════════
    // Lifecycle
    // ═══════════════════════════════════════════════════════════════════════

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge
        enableEdgeToEdge()
        
        setContentView(R.layout.activity_login)

        // Initialize managers
        initManagers()

        // Initialize views
        initViews()

        // Setup click listeners
        setupClickListeners()

        // Setup input listeners
        setupInputListeners()

        // Handle intent extras (role/email prefill)
        handleIntentExtras()

        // Setup footer links
        setupFooterLinks()

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
        prefManager = PreferenceManager(this)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    /**
     * Initialize view references
     */
    private fun initViews() {
        // Top section
        btnBack = findViewById(R.id.btnBack)
        cardIllustration = findViewById(R.id.cardIllustration)
        tvTitle = findViewById(R.id.tvTitle)
        tvSubtitle = findViewById(R.id.tvSubtitle)
        
        // Tabs
        tabLogin = findViewById(R.id.tabLogin)
        tabSignUp = findViewById(R.id.tabSignUp)
        
        // Input fields
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnTogglePassword = findViewById(R.id.btnTogglePassword)
        layoutEmailInput = findViewById(R.id.layoutEmailInput)
        layoutPasswordInput = findViewById(R.id.layoutPasswordInput)
        
        // Buttons
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)
        btnApple = findViewById(R.id.btnApple)
        
        // Footer
        tvFooter = findViewById(R.id.tvFooter)
    }

    /**
     * Setup all click listeners
     */
    private fun setupClickListeners() {
        // Back button - close this activity and return to previous screen
        btnBack.setOnClickListener {
            finish()
        }

        // Login/SignUp tabs
        tabLogin.setOnClickListener {
            switchToLoginMode()
        }
        
        tabSignUp.setOnClickListener {
            switchToSignUpMode()
        }

        // Password visibility toggle
        btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        // Forgot password
        tvForgotPassword.setOnClickListener {
            navigateToForgotPassword()
        }

        // Login button
        btnLogin.setOnClickListener {
            if (isLoginMode) {
                performLogin()
            } else {
                performSignUp()
            }
        }

        // Social login buttons
        btnGoogle.setOnClickListener {
            performGoogleSignIn()
        }

        btnApple.setOnClickListener {
            performAppleSignIn()
        }
    }

    /**
     * Setup input field listeners for validation feedback
     */
    private fun setupInputListeners() {
        // Email field focus change
        etEmail.setOnFocusChangeListener { _, hasFocus ->
            updateInputFieldState(layoutEmailInput, hasFocus)
        }

        // Password field focus change
        etPassword.setOnFocusChangeListener { _, hasFocus ->
            updateInputFieldState(layoutPasswordInput, hasFocus)
        }

        // Real-time validation
        etEmail.addTextChangedListener {
            clearInputError(layoutEmailInput)
        }

        etPassword.addTextChangedListener {
            clearInputError(layoutPasswordInput)
        }
    }

    /**
     * Setup clickable links in footer text
     */
    private fun setupFooterLinks() {
        val footerText = "By continuing, you agree to our Terms of Service and Privacy Policy."
        val spannableString = SpannableString(footerText)

        // Terms of Service click
        val termsStart = footerText.indexOf("Terms of Service")
        val termsEnd = termsStart + "Terms of Service".length
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    openTermsOfService()
                }
                
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = ContextCompat.getColor(this@LoginActivity, R.color.docease_primary)
                    ds.isUnderlineText = false
                }
            },
            termsStart,
            termsEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Privacy Policy click
        val privacyStart = footerText.indexOf("Privacy Policy")
        val privacyEnd = privacyStart + "Privacy Policy".length
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    openPrivacyPolicy()
                }
                
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = ContextCompat.getColor(this@LoginActivity, R.color.docease_primary)
                    ds.isUnderlineText = false
                }
            },
            privacyStart,
            privacyEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvFooter.text = spannableString
        tvFooter.movementMethod = LinkMovementMethod.getInstance()
        tvFooter.highlightColor = Color.TRANSPARENT
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Tab Switching
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Switch to Login mode
     */
    private fun switchToLoginMode() {
        if (isLoginMode) return
        
        isLoginMode = true
        
        // Update tab appearance
        tabLogin.background = ContextCompat.getDrawable(this, R.drawable.bg_tab_active)
        tabLogin.setTextColor(ContextCompat.getColor(this, R.color.docease_text_primary))
        tabLogin.elevation = 2f
        
        tabSignUp.background = ContextCompat.getDrawable(this, R.drawable.bg_tab_inactive)
        tabSignUp.setTextColor(ContextCompat.getColor(this, R.color.docease_text_secondary))
        tabSignUp.elevation = 0f
        
        // Update button text
        btnLogin.text = "Log In"
        
        // Show forgot password
        tvForgotPassword.visibility = View.VISIBLE
    }

    /**
     * Switch to Sign Up mode
     */
    private fun switchToSignUpMode() {
        if (!isLoginMode) return
        
        isLoginMode = false
        
        // Update tab appearance
        tabSignUp.background = ContextCompat.getDrawable(this, R.drawable.bg_tab_active)
        tabSignUp.setTextColor(ContextCompat.getColor(this, R.color.docease_text_primary))
        tabSignUp.elevation = 2f
        
        tabLogin.background = ContextCompat.getDrawable(this, R.drawable.bg_tab_inactive)
        tabLogin.setTextColor(ContextCompat.getColor(this, R.color.docease_text_secondary))
        tabLogin.elevation = 0f
        
        // Update button text
        btnLogin.text = "Sign Up"
        
        // Hide forgot password in sign up mode
        tvForgotPassword.visibility = View.INVISIBLE
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
            // Show password
            etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or 
                                   android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_visibility)
        } else {
            // Hide password
            etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or 
                                   android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_off)
        }
        
        // Move cursor to end
        etPassword.setSelection(etPassword.text.length)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Validation
    // ═══════════════════════════════════════════════════════════════════════

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
     * Validate all input fields
     */
    private fun validateInputs(): Boolean {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        
        var isValid = true
        
        // Validate email
        if (!isValidEmail(email)) {
            showInputError(layoutEmailInput)
            if (email.isEmpty()) {
                showToast("Please enter your email address")
            } else {
                showToast("Please enter a valid email address")
            }
            isValid = false
        }
        
        // Validate password
        if (!isValidPassword(password)) {
            showInputError(layoutPasswordInput)
            if (password.isEmpty()) {
                showToast("Please enter your password")
            } else {
                showToast("Password must be at least 6 characters")
            }
            isValid = false
        }
        
        return isValid
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
        // Shake animation could be added here
    }

    /**
     * Clear error state from input field
     */
    private fun clearInputError(layout: LinearLayout) {
        if (!etEmail.hasFocus() && layout == layoutEmailInput) {
            layout.background = ContextCompat.getDrawable(this, R.drawable.bg_input_field)
        }
        if (!etPassword.hasFocus() && layout == layoutPasswordInput) {
            layout.background = ContextCompat.getDrawable(this, R.drawable.bg_input_field)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Authentication Actions
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Perform login with email/password
     */
    private fun performLogin() {
        if (!validateInputs()) return
        
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        
        // Show loading state
        setLoadingState(true)
        
        // Firebase Authentication
        android.util.Log.d("LoginActivity", "performLogin: signing in $email")
        authManager.signInWithEmailAndPassword(email, password) { result ->
            android.util.Log.d("LoginActivity", "signIn callback: $result")
            result.fold(
                onSuccess = { uid ->
                    // Keep loading while we validate/create user record
                    setLoadingState(true)
                    android.util.Log.d("LoginActivity", "signIn success uid=$uid")
                    prefManager.saveUserId(uid)

                    // Check if user profile exists in Realtime Database
                    DatabaseManager.getInstance().getUsersRef().child(uid).get().addOnCompleteListener { task: com.google.android.gms.tasks.Task<com.google.firebase.database.DataSnapshot> ->
                        setLoadingState(false)
                        if (task.isSuccessful) {
                            val snapshot = task.result
                            if (snapshot != null && snapshot.exists()) {
                                // Existing user - read role and continue
                                val roleStr = snapshot.child("role").getValue(String::class.java)
                                android.util.Log.d("LoginActivity", "user record exists role=$roleStr")
                                val role = try {
                                    if (roleStr.isNullOrEmpty()) UserRole.PATIENT else UserRole.valueOf(roleStr.uppercase())
                                } catch (e: Exception) {
                                    UserRole.PATIENT
                                }
                                prefManager.saveUserRole(role)
                                
                                // For patients, check if profile is complete
                                if (role == UserRole.PATIENT) {
                                    DatabaseManager.getInstance().getDatabase().reference.child("patients").child(uid).get().addOnCompleteListener { patientTask ->
                                        if (patientTask.isSuccessful && patientTask.result.exists()) {
                                            // Patient profile exists
                                            showToast("Welcome back!")
                                            navigateToDashboard()
                                        } else {
                                            // Patient profile not found - redirect to setup
                                            showToast("Complete your profile")
                                            try {
                                                val intent = Intent(this, Class.forName("com.example.docease.ui.profile.PatientProfileSetupActivity"))
                                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                startActivity(intent)
                                                finish()
                                            } catch (e: ClassNotFoundException) {
                                                navigateToDashboard()
                                            }
                                        }
                                    }
                                } else {
                                    // For doctors, just navigate to dashboard
                                    showToast("Welcome back!")
                                    navigateToDashboard()
                                }
                            } else {
                                // First time login - create minimal profile (do NOT store password)
                                android.util.Log.d("LoginActivity", "user profile not found - creating default patient profile")
                                val newUser = User(
                                    uid = uid,
                                    email = email,
                                    role = UserRole.PATIENT
                                )
                                DatabaseManager.getInstance().saveUser(newUser) { saveRes ->
                                    saveRes.fold(
                                        onSuccess = {
                                            prefManager.saveUserRole(UserRole.PATIENT)
                                            // Redirect to profile setup
                                            try {
                                                val intent = Intent(this, Class.forName("com.example.docease.ui.profile.PatientProfileSetupActivity"))
                                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                startActivity(intent)
                                                finish()
                                            } catch (e: ClassNotFoundException) {
                                                navigateToDashboard()
                                            }
                                        },
                                        onFailure = { e ->
                                            android.util.Log.e("LoginActivity", "failed to create user record", e)
                                            showToast("Login succeeded but failed to create profile. Please try again.")
                                        }
                                    )
                                }
                            }
                        } else {
                            android.util.Log.e("LoginActivity", "failed to read user record", task.exception)
                            setLoadingState(false)
                            showToast("Login succeeded but failed to validate profile. Please try again.")
                        }
                    }
                },
                onFailure = { exception ->
                    setLoadingState(false)
                    android.util.Log.e("LoginActivity", "signIn failed", exception)
                    showToast(exception.message ?: "Login failed. Please try again.")
                }
            )
        }
    }

    /**
     * Perform sign up with email/password
     */
    private fun performSignUp() {
        if (!validateInputs()) return
        
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        
        // Navigate to SignUpActivity for full registration
        try {
            val intent = Intent(this, Class.forName("com.example.docease.ui.auth.SignUpActivity"))
            intent.putExtra("email", email)
            intent.putExtra("password", password)
            // Pass selected role (if any) so SignUp can pre-select and lock the role
            intent.putExtra(com.example.docease.utils.Constants.Extras.EXTRA_USER_ROLE, prefManager.getUserRole()?.name)
            startActivity(intent)
        } catch (e: ClassNotFoundException) {
            // SignUpActivity not yet created - perform basic signup
            setLoadingState(true)
            
            authManager.createUserWithEmailAndPassword(email, password) { result ->
                setLoadingState(false)

                result.fold(
                    onSuccess = { user ->
                        // Directly navigate to dashboard on success
                        navigateToDashboard()
                    },
                    onFailure = { exception ->
                        showToast(exception.message ?: "Sign up failed. Please try again.")
                    }
                )
            }
        }
    }

    /**
     * Perform Google Sign-In
     */
    private fun performGoogleSignIn() {
        // Start Google sign-in flow
        try {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, com.example.docease.utils.Constants.RequestCodes.REQUEST_GOOGLE_SIGN_IN)
        } catch (e: Exception) {
            android.util.Log.e("LoginActivity", "Google sign-in start failed", e)
            showToast("Google Sign-In failed to start. Please try again.")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == com.example.docease.utils.Constants.RequestCodes.REQUEST_GOOGLE_SIGN_IN) {
            // Handle Google Sign-In result
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    // Authenticate with Firebase using Google credential
                    firebaseAuthWithGoogle(idToken)
                } else {
                    showToast("Google sign-in failed: no ID token")
                }
            } catch (e: ApiException) {
                android.util.Log.e("LoginActivity", "Google sign-in failed", e)
                showToast("Google sign-in failed: ${e.statusCode}")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        setLoadingState(true)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { authTask ->
            setLoadingState(false)
            if (authTask.isSuccessful) {
                val user = firebaseAuth.currentUser
                handleGoogleSignedInUser(user)
            } else {
                android.util.Log.e("LoginActivity", "signInWithCredential failed", authTask.exception)
                showToast(authTask.exception?.message ?: "Authentication failed")
            }
        }
    }

    private fun handleGoogleSignedInUser(firebaseUser: FirebaseUser?) {
        if (firebaseUser == null) {
            showToast("Google authentication failed")
            return
        }

        val uid = firebaseUser.uid
        val email = firebaseUser.email ?: ""
        val selectedRole = prefManager.getUserRole() // May be null if user didn't choose

        fun denySignIn(foundRole: com.example.docease.models.UserRole?) {
            val roleText = foundRole?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "this role"
            showToast("This account is registered as $roleText. Please select the correct role to sign in.")
            // Sign out from Firebase and Google to avoid leaving user signed in
            try {
                firebaseAuth.signOut()
                googleSignInClient.signOut()
            } catch (e: Exception) {
                android.util.Log.e("LoginActivity", "Error signing out after role mismatch", e)
            }
        }

        // First, look up by UID
        DatabaseManager.getInstance().getUsersRef().child(uid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result
                    if (snapshot != null && snapshot.exists()) {
                        val roleStr = snapshot.child("role").getValue(String::class.java)
                        val role = try {
                            if (roleStr.isNullOrEmpty()) com.example.docease.models.UserRole.PATIENT else com.example.docease.models.UserRole.valueOf(roleStr.uppercase())
                        } catch (e: Exception) {
                            com.example.docease.models.UserRole.PATIENT
                        }

                        // If user selected a role and it doesn't match their registered role, deny
                        if (selectedRole != null && selectedRole != role) {
                            denySignIn(role)
                            return@addOnCompleteListener
                        }

                        // Roles match (or no selection) — proceed with normal login flow
                        prefManager.saveUserRole(role)
                        prefManager.saveUserId(uid)

                        if (role == com.example.docease.models.UserRole.PATIENT) {
                            DatabaseManager.getInstance().getDatabase().reference.child("patients").child(uid).get().addOnCompleteListener { patientTask ->
                                if (patientTask.isSuccessful && patientTask.result.exists()) {
                                    showToast("Welcome back!")
                                    navigateToDashboard()
                                } else {
                                    showToast("Complete your profile")
                                    try {
                                        val intent = Intent(this, Class.forName("com.example.docease.ui.profile.PatientProfileSetupActivity"))
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                        finish()
                                    } catch (e: ClassNotFoundException) {
                                        navigateToDashboard()
                                    }
                                }
                            }
                        } else {
                            showToast("Welcome back!")
                            navigateToDashboard()
                        }
                    } else {
                        // No user with this UID — check by email (catch accounts created earlier with different provider)
                        DatabaseManager.getInstance().getUsersRef()
                            .orderByChild("email")
                            .equalTo(email)
                            .get()
                            .addOnCompleteListener { emailQuery ->
                                if (emailQuery.isSuccessful) {
                                    val result = emailQuery.result
                                    if (result != null && result.exists()) {
                                        // Found a user with same email
                                        val first = result.children.iterator().next()
                                        val roleStr = first.child("role").getValue(String::class.java)
                                        val role = try {
                                            if (roleStr.isNullOrEmpty()) com.example.docease.models.UserRole.PATIENT else com.example.docease.models.UserRole.valueOf(roleStr.uppercase())
                                        } catch (e: Exception) {
                                            com.example.docease.models.UserRole.PATIENT
                                        }

                                        // If selected role doesn't match registered role, deny
                                        if (selectedRole != null && selectedRole != role) {
                                            denySignIn(role)
                                            return@addOnCompleteListener
                                        }

                                        // Roles match — proceed to SignUp completion so user can link/finish profile
                                        try {
                                            val intent = Intent(this, Class.forName("com.example.docease.ui.auth.SignUpActivity"))
                                            intent.putExtra("email", email)
                                            intent.putExtra(com.example.docease.utils.Constants.Extras.EXTRA_FROM_GOOGLE, true)
                                            intent.putExtra(com.example.docease.utils.Constants.Extras.EXTRA_USER_ROLE, prefManager.getUserRole()?.name)
                                            startActivity(intent)
                                        } catch (e: ClassNotFoundException) {
                                            showToast("Sign up screen not found. Please contact support.")
                                        }
                                    } else {
                                        // No user found by email — treat as a new social signup
                                        try {
                                            val intent = Intent(this, Class.forName("com.example.docease.ui.auth.SignUpActivity"))
                                            intent.putExtra("email", email)
                                            intent.putExtra(com.example.docease.utils.Constants.Extras.EXTRA_FROM_GOOGLE, true)
                                            intent.putExtra(com.example.docease.utils.Constants.Extras.EXTRA_USER_ROLE, prefManager.getUserRole()?.name)
                                            startActivity(intent)
                                        } catch (e: ClassNotFoundException) {
                                            showToast("Sign up screen not found. Please contact support.")
                                        }
                                    }
                                } else {
                                    android.util.Log.e("LoginActivity", "Email lookup failed", emailQuery.exception)
                                    try {
                                        val intent = Intent(this, Class.forName("com.example.docease.ui.auth.SignUpActivity"))
                                        intent.putExtra("email", email)
                                        intent.putExtra(com.example.docease.utils.Constants.Extras.EXTRA_FROM_GOOGLE, true)
                                        intent.putExtra(com.example.docease.utils.Constants.Extras.EXTRA_USER_ROLE, prefManager.getUserRole()?.name)
                                        startActivity(intent)
                                    } catch (e: ClassNotFoundException) {
                                        showToast("Sign up screen not found. Please contact support.")
                                    }
                                }
                            }
                    }
                } else {
                    // DB lookup failed - fall back to SignUp with email prefilled
                    android.util.Log.e("LoginActivity", "User lookup failed", task.exception)
                    try {
                        val intent = Intent(this, Class.forName("com.example.docease.ui.auth.SignUpActivity"))
                        intent.putExtra("email", email)
                        intent.putExtra(com.example.docease.utils.Constants.Extras.EXTRA_FROM_GOOGLE, true)
                        intent.putExtra(com.example.docease.utils.Constants.Extras.EXTRA_USER_ROLE, prefManager.getUserRole()?.name)
                        startActivity(intent)
                    } catch (e: ClassNotFoundException) {
                        showToast("Sign up screen not found. Please contact support.")
                    }
                }
            }
    }

    /**
     * Perform Apple Sign-In
     */
    private fun performAppleSignIn() {
        showToast("Apple Sign-In coming soon!")
        // TODO: Implement Apple Sign-In with Firebase
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Intent / Role handling
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Handle intent extras (role or email prefills)
     */
    private fun handleIntentExtras() {
        intent?.let { itIntent ->
            // Prefill email if provided
            val emailExtra = itIntent.getStringExtra("email")
            if (!emailExtra.isNullOrEmpty()) {
                etEmail.setText(emailExtra)
            }

            // Check for role extra
            val roleExtra = itIntent.getStringExtra(com.example.docease.utils.Constants.Extras.EXTRA_USER_ROLE)
            if (!roleExtra.isNullOrEmpty()) {
                val role = try {
                    com.example.docease.models.UserRole.valueOf(roleExtra.uppercase())
                } catch (e: Exception) {
                    com.example.docease.models.UserRole.PATIENT
                }
                updateForRole(role)
                return
            }

            // Else fall back to saved preference
            updateForRole(prefManager.getUserRole())
        }
    }

    /**
     * Update UI text for given role
     */
    private fun updateForRole(role: com.example.docease.models.UserRole?) {
        if (role == com.example.docease.models.UserRole.DOCTOR) {
            tvTitle.text = getString(R.string.login_title_doctor)
            tvSubtitle.text = getString(R.string.login_subtitle_doctor)
        } else {
            tvTitle.text = getString(R.string.login_title_patient)
            tvSubtitle.text = getString(R.string.login_subtitle_patient)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Navigation
    // ═══════════════════════════════════════════════════════════════

    /**
     * Navigate to Forgot Password screen
     */
    private fun navigateToForgotPassword() {
        try {
            val intent = Intent(this, Class.forName("com.example.docease.ui.auth.ForgotPasswordActivity"))
            intent.putExtra("email", etEmail.text.toString().trim())
            startActivity(intent)
        } catch (e: ClassNotFoundException) {
            // ForgotPasswordActivity not yet created - fallback to inline reset flow
            val email = etEmail.text.toString().trim()
            if (!isValidEmail(email)) {
                showToast("Please enter your email address first")
                return
            }
            // Check if email exists in our database before attempting reset
            DatabaseManager.getInstance().getUsersRef()
                .orderByChild("email")
                .equalTo(email)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val snapshot = task.result
                        if (snapshot != null && snapshot.exists()) {
                            authManager.sendPasswordResetEmail(email) { result ->
                                result.fold(
                                    onSuccess = {
                                        showToast("Password reset email sent to $email")
                                    },
                                    onFailure = { exception ->
                                        showToast(exception.message ?: "Failed to send reset email")
                                    }
                                )
                            }
                        } else {
                            // Try lowercase email before giving up
                            val lower = email.lowercase()
                            if (lower != email) {
                                DatabaseManager.getInstance().getUsersRef()
                                    .orderByChild("email")
                                    .equalTo(lower)
                                    .get()
                                    .addOnCompleteListener { t2 ->
                                        if (t2.isSuccessful && t2.result != null && t2.result.exists()) {
                                            authManager.sendPasswordResetEmail(lower) { result ->
                                                result.fold(
                                                    onSuccess = {
                                                        showToast("Password reset email sent to $email")
                                                    },
                                                    onFailure = { exception ->
                                                        showToast(exception.message ?: "Failed to send reset email")
                                                    }
                                                )
                                            }
                                        } else {
                                            showToast("No account found with this email address")
                                        }
                                    }
                            } else {
                                showToast("No account found with this email address")
                            }
                        }
                    } else {
                        // DB lookup failed - log and fallback to Auth send
                        android.util.Log.e("LoginActivity", "DB lookup failed", task.exception)
                        authManager.sendPasswordResetEmail(email) { result ->
                            result.fold(
                                onSuccess = {
                                    showToast("If an account exists, a reset link has been sent to $email")
                                },
                                onFailure = { exception ->
                                    showToast(exception.message ?: "Failed to send reset email")
                                }
                            )
                        }
                    }
                }
        }
    }

    /**
     * Navigate to appropriate dashboard based on user role
     */
    private fun navigateToDashboard() {
        val userRole = prefManager.getUserRole()

        val dashboardClass = when (userRole?.name) {
            "DOCTOR" -> "com.example.docease.ui.dashboard.DoctorDashboardActivity"
            "PATIENT" -> "com.example.docease.ui.patient.PatientDashboardActivity"
            else -> "com.example.docease.MainActivity"
        }

        try {
            val intent = Intent(this, Class.forName(dashboardClass))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: ClassNotFoundException) {
            showToast("Dashboard screen coming soon!")
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
     * Set loading state for button
     */
    private fun setLoadingState(isLoading: Boolean) {
        btnLogin.isEnabled = !isLoading
        btnLogin.text = if (isLoading) "Please wait..." else if (isLoginMode) "Log In" else "Sign Up"
        
        // Disable other interactive elements during loading
        tabLogin.isEnabled = !isLoading
        tabSignUp.isEnabled = !isLoading
        btnGoogle.isEnabled = !isLoading
        btnApple.isEnabled = !isLoading
    }

    /**
     * Show toast message
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Start entrance animations
     */
    private fun startEntranceAnimations() {
        // Fade in illustration
        cardIllustration.alpha = 0f
        cardIllustration.translationY = -30f
        cardIllustration.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(100)
            .start()

        // Fade in title
        tvTitle.alpha = 0f
        tvTitle.animate()
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(200)
            .start()

        // Fade in subtitle
        tvSubtitle.alpha = 0f
        tvSubtitle.animate()
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(300)
            .start()
    }
}
