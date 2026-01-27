package com.example.docease.ui.auth

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.docease.R
import com.example.docease.firebase.AuthManager
import com.example.docease.firebase.DatabaseManager
import com.google.android.material.snackbar.Snackbar

/**
 * ForgotPasswordActivity - Password reset screen
 * 
 * Features:
 * - Email input for password reset
 * - Firebase password reset email
 * - Success state with resend option
 * - Input validation
 * - Clean, minimal design matching DocEase theme
 */
class ForgotPasswordActivity : AppCompatActivity() {

    // ═══════════════════════════════════════════════════════════════════════
    // UI Components
    // ═══════════════════════════════════════════════════════════════════════
    
    private lateinit var btnBack: ImageButton
    private lateinit var tvAppBarTitle: TextView
    private lateinit var cardIllustration: CardView
    private lateinit var ivLockIllustration: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    
    // Input section
    private lateinit var layoutEmailInput: LinearLayout
    private lateinit var etEmail: EditText
    private lateinit var btnSendResetLink: Button
    private lateinit var tvBackToLogin: TextView
    
    // Success section
    private lateinit var layoutSuccess: LinearLayout
    private lateinit var tvSuccessMessage: TextView
    private lateinit var btnBackToLoginSuccess: Button
    private lateinit var tvResendEmail: TextView
    
    // Root layout for Snackbar
    private lateinit var rootLayout: ConstraintLayout

    // ═══════════════════════════════════════════════════════════════════════
    // State
    // ═══════════════════════════════════════════════════════════════════════
    
    private lateinit var authManager: AuthManager
    private var lastSentEmail: String = ""
    private var resendTimer: CountDownTimer? = null
    private var canResend = true

    // ═══════════════════════════════════════════════════════════════════════
    // Lifecycle
    // ═══════════════════════════════════════════════════════════════════════

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge
        enableEdgeToEdge()
        
        setContentView(R.layout.activity_forgot_password)

        // Initialize
        initManagers()
        initViews()
        setupClickListeners()
        setupInputListeners()
        handleIntentExtras()
        startEntranceAnimations()
    }

    override fun onDestroy() {
        super.onDestroy()
        resendTimer?.cancel()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Initialization
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Enable edge-to-edge display
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
    }

    /**
     * Initialize view references
     */
    private fun initViews() {
        val layoutEmail = findViewById<View>(R.id.layoutEmailInput)
        rootLayout = layoutEmail.parent as ConstraintLayout
        
        // Top section
        btnBack = findViewById(R.id.btnBack)
        tvAppBarTitle = findViewById(R.id.tvAppBarTitle)
        cardIllustration = findViewById(R.id.cardIllustration)
        ivLockIllustration = findViewById(R.id.ivLockIllustration)
        tvTitle = findViewById(R.id.tvTitle)
        tvDescription = findViewById(R.id.tvDescription)
        
        // Input section
        layoutEmailInput = findViewById(R.id.layoutEmailInput)
        etEmail = findViewById(R.id.etEmail)
        btnSendResetLink = findViewById(R.id.btnSendResetLink)
        tvBackToLogin = findViewById(R.id.tvBackToLogin)
        
        // Success section
        layoutSuccess = findViewById(R.id.layoutSuccess)
        tvSuccessMessage = findViewById(R.id.tvSuccessMessage)
        btnBackToLoginSuccess = findViewById(R.id.btnBackToLoginSuccess)
        tvResendEmail = findViewById(R.id.tvResendEmail)
    }

    /**
     * Setup click listeners
     */
    private fun setupClickListeners() {
        // Back button
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Send reset link button
        btnSendResetLink.setOnClickListener {
            sendPasswordResetEmail()
        }

        // Back to login (input state)
        tvBackToLogin.setOnClickListener {
            navigateToLogin()
        }

        // Back to login (success state)
        btnBackToLoginSuccess.setOnClickListener {
            navigateToLogin()
        }

        // Resend email
        tvResendEmail.setOnClickListener {
            if (canResend) {
                resendPasswordResetEmail()
            }
        }
    }

    /**
     * Setup input field listeners
     */
    private fun setupInputListeners() {
        // Focus change for visual feedback
        etEmail.setOnFocusChangeListener { _, hasFocus ->
            updateInputFieldState(hasFocus)
        }

        // Clear error on text change
        etEmail.addTextChangedListener {
            clearInputError()
        }

        // Handle IME action
        etEmail.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                sendPasswordResetEmail()
                true
            } else {
                false
            }
        }
    }

    /**
     * Handle intent extras (pre-fill email from login screen)
     */
    private fun handleIntentExtras() {
        intent?.let {
            val email = it.getStringExtra("email")
            if (!email.isNullOrEmpty()) {
                etEmail.setText(email)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Password Reset Logic
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Send password reset email
     */
    private fun sendPasswordResetEmail() {
        val email = etEmail.text.toString().trim()

        // Validate email
        if (!validateEmail(email)) return

        // Show loading state
        setLoadingState(true)

        // Helper to send via Firebase Auth and handle UI
        fun sendViaAuth(emailToSend: String) {
            authManager.sendPasswordResetEmail(emailToSend) { result ->
                setLoadingState(false)
                result.fold(
                    onSuccess = {
                        lastSentEmail = emailToSend
                        showSuccessState(emailToSend)
                    },
                    onFailure = { exception ->
                        showError(exception.message ?: "Failed to send reset email. Please try again.")
                    }
                )
            }
        }

        val usersRef = DatabaseManager.getInstance().getUsersRef()

        // First try exact match
        usersRef.orderByChild("email").equalTo(email).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                if (snapshot != null && snapshot.exists()) {
                    // Exact match found
                    sendViaAuth(email)
                } else {
                    // Try lowercase email (users may be stored lowercase)
                    val lower = email.lowercase()
                    if (lower != email) {
                        usersRef.orderByChild("email").equalTo(lower).get().addOnCompleteListener { t2 ->
                            if (t2.isSuccessful && t2.result != null && t2.result.exists()) {
                                sendViaAuth(lower)
                            } else {
                                setLoadingState(false)
                                showSnackbar("No account found with this email address")
                            }
                        }
                    } else {
                        setLoadingState(false)
                        showSnackbar("No account found with this email address")
                    }
                }
            } else {
                // DB lookup failed (network or security rules) - fallback to Auth send
                android.util.Log.e("ForgotPassword", "DB lookup failed", task.exception)
                sendViaAuth(email)
            }
        }
    }

    /**
     * Resend password reset email
     */
    private fun resendPasswordResetEmail() {
        if (lastSentEmail.isEmpty()) return
        
        // Start cooldown timer
        startResendCooldown()
        
        // Send reset email
        authManager.sendPasswordResetEmail(lastSentEmail) { result ->
            result.fold(
                onSuccess = {
                    showSnackbar("Reset link sent again to $lastSentEmail")
                },
                onFailure = { exception ->
                    showSnackbar(exception.message ?: "Failed to resend email")
                    // Reset cooldown on failure
                    canResend = true
                    tvResendEmail.text = "Didn't receive email? Resend"
                    tvResendEmail.setTextColor(ContextCompat.getColor(this, R.color.docease_text_secondary))
                }
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Validation
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Validate email input
     */
    private fun validateEmail(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                showInputError()
                showSnackbar("Please enter your email address")
                etEmail.requestFocus()
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showInputError()
                showSnackbar("Please enter a valid email address")
                etEmail.requestFocus()
                false
            }
            else -> true
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UI State Management
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Show success state after email is sent
     */
    private fun showSuccessState(email: String) {
        // Update success message
        tvSuccessMessage.text = "We have sent a password reset link to\n$email"
        
        // Setup resend link with clickable span
        setupResendLink()
        
        // Hide input elements with animation
        fadeOutInputElements()
        
        // Show success layout with animation
        layoutSuccess.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(200)
                .start()
        }
    }

    /**
     * Setup resend email link with clickable "Resend" text
     */
    private fun setupResendLink() {
        val resendText = "Didn't receive email? Resend"
        val spannableString = SpannableString(resendText)
        
        val resendStart = resendText.indexOf("Resend")
        val resendEnd = resendStart + "Resend".length
        
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    if (canResend) {
                        resendPasswordResetEmail()
                    }
                }
                
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = if (canResend) {
                        ContextCompat.getColor(this@ForgotPasswordActivity, R.color.docease_primary)
                    } else {
                        ContextCompat.getColor(this@ForgotPasswordActivity, R.color.docease_text_hint)
                    }
                    ds.isUnderlineText = false
                    ds.isFakeBoldText = true
                }
            },
            resendStart,
            resendEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvResendEmail.text = spannableString
        tvResendEmail.movementMethod = LinkMovementMethod.getInstance()
        tvResendEmail.highlightColor = Color.TRANSPARENT
    }

    /**
     * Fade out input elements when transitioning to success state
     */
    private fun fadeOutInputElements() {
        val viewsToHide = listOf(
            cardIllustration, tvTitle, tvDescription,
            layoutEmailInput, btnSendResetLink, tvBackToLogin
        )
        
        viewsToHide.forEach { view ->
            view.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    view.visibility = View.GONE
                }
                .start()
        }
        
        // Also hide app bar title
        tvAppBarTitle.animate()
            .alpha(0f)
            .setDuration(200)
            .start()
    }

    /**
     * Start resend cooldown timer (60 seconds)
     */
    private fun startResendCooldown() {
        canResend = false
        
        resendTimer?.cancel()
        resendTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                tvResendEmail.text = "Resend available in ${seconds}s"
                tvResendEmail.setTextColor(ContextCompat.getColor(
                    this@ForgotPasswordActivity, 
                    R.color.docease_text_hint
                ))
            }

            override fun onFinish() {
                canResend = true
                setupResendLink()
            }
        }.start()
    }

    /**
     * Set loading state for button
     */
    private fun setLoadingState(isLoading: Boolean) {
        btnSendResetLink.isEnabled = !isLoading
        btnSendResetLink.text = if (isLoading) "Sending..." else "Send Reset Link"
        etEmail.isEnabled = !isLoading
    }

    /**
     * Update input field visual state
     */
    private fun updateInputFieldState(hasFocus: Boolean) {
        if (hasFocus) {
            layoutEmailInput.background = ContextCompat.getDrawable(this, R.drawable.bg_input_field_focused)
        } else {
            layoutEmailInput.background = ContextCompat.getDrawable(this, R.drawable.bg_input_field)
        }
    }

    /**
     * Show error state on input field
     */
    private fun showInputError() {
        layoutEmailInput.background = ContextCompat.getDrawable(this, R.drawable.bg_input_field_focused)
        
        // Shake animation
        layoutEmailInput.animate()
            .translationX(-10f)
            .setDuration(50)
            .withEndAction {
                layoutEmailInput.animate()
                    .translationX(10f)
                    .setDuration(50)
                    .withEndAction {
                        layoutEmailInput.animate()
                            .translationX(-5f)
                            .setDuration(50)
                            .withEndAction {
                                layoutEmailInput.animate()
                                    .translationX(0f)
                                    .setDuration(50)
                                    .start()
                            }
                            .start()
                    }
                    .start()
            }
            .start()
    }

    /**
     * Clear error state from input field
     */
    private fun clearInputError() {
        if (!etEmail.hasFocus()) {
            layoutEmailInput.background = ContextCompat.getDrawable(this, R.drawable.bg_input_field)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Navigation
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Navigate back to login screen
     */
    private fun navigateToLogin() {
        finish()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UI Helpers
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Show error message
     */
    private fun showError(message: String) {
        showSnackbar(message)
    }

    /**
     * Show snackbar message
     */
    private fun showSnackbar(message: String) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.docease_secondary))
            .setTextColor(ContextCompat.getColor(this, R.color.docease_background))
            .show()
    }

    /**
     * Start entrance animations
     */
    private fun startEntranceAnimations() {
        // Illustration card
        cardIllustration.alpha = 0f
        cardIllustration.translationY = -30f
        cardIllustration.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(100)
            .start()

        // Title
        tvTitle.alpha = 0f
        tvTitle.animate()
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(200)
            .start()

        // Description
        tvDescription.alpha = 0f
        tvDescription.animate()
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(300)
            .start()

        // Input field
        layoutEmailInput.alpha = 0f
        layoutEmailInput.translationY = 20f
        layoutEmailInput.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(400)
            .start()

        // Button
        btnSendResetLink.alpha = 0f
        btnSendResetLink.animate()
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(500)
            .start()
    }
}
