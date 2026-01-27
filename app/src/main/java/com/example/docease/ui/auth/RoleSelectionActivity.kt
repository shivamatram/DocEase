package com.example.docease.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.docease.R
import com.example.docease.models.UserRole
import com.example.docease.utils.Constants
import com.example.docease.utils.PreferenceManager

/**
 * RoleSelectionActivity - User selects their role (Doctor or Patient)
 * 
 * This screen follows the DocEase design language:
 * - Gradient background
 * - Cyan/teal accent color
 * - Card-based selection
 * - Smooth animations
 */
class RoleSelectionActivity : AppCompatActivity() {

    // UI Components
    private lateinit var cardDoctor: CardView
    private lateinit var cardPatient: CardView
    private lateinit var btnContinue: Button
    private lateinit var layoutDoctorContent: ConstraintLayout
    private lateinit var layoutPatientContent: ConstraintLayout

    // State
    private var selectedRole: UserRole? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge
        enableEdgeToEdge()
        
        setContentView(R.layout.activity_role_selection)

        // Initialize views
        initViews()

        // Setup click listeners
        setupClickListeners()

        // Start entrance animations
        startEntranceAnimations()
    }

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
     * Initialize view references
     */
    private fun initViews() {
        cardDoctor = findViewById(R.id.cardDoctor)
        cardPatient = findViewById(R.id.cardPatient)
        btnContinue = findViewById(R.id.btnContinue)
        
        // Get the inner constraint layouts for selection state
        layoutDoctorContent = cardDoctor.getChildAt(0) as ConstraintLayout
        layoutPatientContent = cardPatient.getChildAt(0) as ConstraintLayout
    }

    /**
     * Setup click listeners for cards and button
     */
    private fun setupClickListeners() {
        // Doctor card click
        cardDoctor.setOnClickListener {
            selectRole(UserRole.DOCTOR)
        }

        // Patient card click
        cardPatient.setOnClickListener {
            selectRole(UserRole.PATIENT)
        }

        // Continue button click
        btnContinue.setOnClickListener {
            navigateToLogin()
        }
    }

    /**
     * Handle role selection
     */
    private fun selectRole(role: UserRole) {
        selectedRole = role

        // Update visual states
        when (role) {
            UserRole.DOCTOR -> {
                layoutDoctorContent.isSelected = true
                layoutPatientContent.isSelected = false
                
                // Animate selection
                animateCardSelection(cardDoctor)
            }
            UserRole.PATIENT -> {
                layoutDoctorContent.isSelected = false
                layoutPatientContent.isSelected = true
                
                // Animate selection
                animateCardSelection(cardPatient)
            }
        }

        // Enable continue button
        btnContinue.isEnabled = true
        
        // Animate button if first selection
        if (btnContinue.alpha < 1f) {
            btnContinue.animate()
                .alpha(1f)
                .setDuration(200)
                .start()
        }
    }

    /**
     * Animate card when selected
     */
    private fun animateCardSelection(card: CardView) {
        card.animate()
            .scaleX(0.98f)
            .scaleY(0.98f)
            .setDuration(100)
            .withEndAction {
                card.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    /**
     * Start entrance animations
     */
    private fun startEntranceAnimations() {
        // Fade in cards with delay
        cardDoctor.alpha = 0f
        cardPatient.alpha = 0f
        btnContinue.alpha = 0f

        cardDoctor.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(100)
            .setDuration(400)
            .start()

        cardPatient.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(200)
            .setDuration(400)
            .start()

        btnContinue.animate()
            .alpha(0.5f) // Start dimmed until selection
            .setStartDelay(300)
            .setDuration(400)
            .start()
    }

    /**
     * Navigate to login screen with selected role
     */
    private fun navigateToLogin() {
        selectedRole?.let { role ->
            // Save selected role to preferences
            val prefManager = PreferenceManager(this)
            prefManager.saveUserRole(role)

            // Navigate to LoginActivity
            try {
                val intent = Intent(this, Class.forName("com.example.docease.ui.auth.LoginActivity"))
                intent.putExtra(Constants.Extras.EXTRA_USER_ROLE, role.name)
                startActivity(intent)
                
                // Apply transition animation
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            } catch (e: ClassNotFoundException) {
                // LoginActivity doesn't exist yet
                android.util.Log.d("RoleSelection", "LoginActivity not found. Role selected: $role")
                
                // For now, show a toast
                android.widget.Toast.makeText(
                    this,
                    "Selected: ${role.name}. Login screen coming soon!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Handle back press
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Allow back navigation but confirm exit
        super.onBackPressed()
    }
}
