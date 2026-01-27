package com.example.docease.ui.auth

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.docease.R

/**
 * SplashActivity - Professional splash screen for DocEase
 * 
 * Features:
 * - Gradient background with medical aesthetic
 * - Animated app icon with elevation
 * - Smooth progress bar animation
 * - Automatic navigation after 3 seconds
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    // UI Components
    private lateinit var cardIconContainer: CardView
    private lateinit var tvAppName: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvLoading: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvVersion: TextView

    // Constants
    companion object {
        private const val SPLASH_DURATION = 3000L // 3 seconds
        private const val ANIMATION_DURATION = 800L
        private const val PROGRESS_ANIMATION_DURATION = 2500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        setContentView(R.layout.activity_splash)

        // Initialize views
        initViews()

        // Start animations
        startAnimations()

        // Navigate after delay
        navigateAfterDelay()
    }

    /**
     * Enable edge-to-edge display for immersive experience
     */
    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true
        windowInsetsController.isAppearanceLightNavigationBars = true
        
        // Optional: Hide system bars for full immersion
        // windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    /**
     * Initialize all view references
     */
    private fun initViews() {
        cardIconContainer = findViewById(R.id.cardIconContainer)
        tvAppName = findViewById(R.id.tvAppName)
        tvSubtitle = findViewById(R.id.tvSubtitle)
        tvLoading = findViewById(R.id.tvLoading)
        progressBar = findViewById(R.id.progressBar)
        tvVersion = findViewById(R.id.tvVersion)
    }

    /**
     * Start all entrance animations
     */
    private fun startAnimations() {
        // Initially hide elements
        cardIconContainer.alpha = 0f
        tvAppName.alpha = 0f
        tvSubtitle.alpha = 0f
        tvLoading.alpha = 0f
        progressBar.alpha = 0f
        tvVersion.alpha = 0f

        // Animate icon container (scale + fade)
        animateIconContainer()

        // Animate text elements with delay
        Handler(Looper.getMainLooper()).postDelayed({
            animateTextElements()
        }, 300)

        // Animate loading section
        Handler(Looper.getMainLooper()).postDelayed({
            animateLoadingSection()
        }, 600)

        // Animate footer
        Handler(Looper.getMainLooper()).postDelayed({
            animateFooter()
        }, 800)
    }

    /**
     * Animate the icon container with scale and fade
     */
    private fun animateIconContainer() {
        // Scale animation
        val scaleAnimation = ScaleAnimation(
            0.5f, 1f, // X: from 50% to 100%
            0.5f, 1f, // Y: from 50% to 100%
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = ANIMATION_DURATION
            interpolator = DecelerateInterpolator()
        }

        // Fade animation
        val fadeAnimation = AlphaAnimation(0f, 1f).apply {
            duration = ANIMATION_DURATION
        }

        // Combine animations
        val animationSet = AnimationSet(true).apply {
            addAnimation(scaleAnimation)
            addAnimation(fadeAnimation)
        }

        cardIconContainer.startAnimation(animationSet)
        cardIconContainer.alpha = 1f
    }

    /**
     * Animate app name and subtitle
     */
    private fun animateTextElements() {
        // App name - fade and slide up
        val nameAnimation = AnimationSet(true).apply {
            addAnimation(AlphaAnimation(0f, 1f).apply { duration = ANIMATION_DURATION })
            addAnimation(TranslateAnimation(0f, 0f, 30f, 0f).apply { 
                duration = ANIMATION_DURATION 
                interpolator = DecelerateInterpolator()
            })
        }
        tvAppName.startAnimation(nameAnimation)
        tvAppName.alpha = 1f

        // Subtitle with slight delay
        Handler(Looper.getMainLooper()).postDelayed({
            val subtitleAnimation = AnimationSet(true).apply {
                addAnimation(AlphaAnimation(0f, 1f).apply { duration = ANIMATION_DURATION })
                addAnimation(TranslateAnimation(0f, 0f, 20f, 0f).apply { 
                    duration = ANIMATION_DURATION 
                    interpolator = DecelerateInterpolator()
                })
            }
            tvSubtitle.startAnimation(subtitleAnimation)
            tvSubtitle.alpha = 1f
        }, 150)
    }

    /**
     * Animate loading text and progress bar
     */
    private fun animateLoadingSection() {
        // Fade in loading text
        tvLoading.animate()
            .alpha(1f)
            .setDuration(ANIMATION_DURATION / 2)
            .start()

        // Fade in progress bar
        progressBar.animate()
            .alpha(1f)
            .setDuration(ANIMATION_DURATION / 2)
            .withEndAction {
                // Start progress animation
                animateProgress()
            }
            .start()
    }

    /**
     * Animate the progress bar from 0 to 100
     */
    private fun animateProgress() {
        val progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100)
        progressAnimator.duration = PROGRESS_ANIMATION_DURATION
        progressAnimator.interpolator = AccelerateDecelerateInterpolator()
        progressAnimator.start()
    }

    /**
     * Animate footer version text
     */
    private fun animateFooter() {
        tvVersion.animate()
            .alpha(1f)
            .setDuration(ANIMATION_DURATION)
            .start()
    }

    /**
     * Navigate to RoleSelectionActivity after splash duration
     */
    private fun navigateAfterDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, SPLASH_DURATION)
    }

    /**
     * Navigate to the next screen (RoleSelectionActivity)
     */
    private fun navigateToNextScreen() {
        // Navigate to RoleSelectionActivity
        // For now, we'll try to navigate to RoleSelectionActivity if it exists,
        // otherwise we'll finish and let the system handle it
        try {
            val intent = Intent(this, Class.forName("com.example.docease.ui.auth.RoleSelectionActivity"))
            startActivity(intent)
            
            // Apply smooth transition animation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } catch (e: ClassNotFoundException) {
            // RoleSelectionActivity doesn't exist yet, log and finish
            android.util.Log.d("SplashActivity", "RoleSelectionActivity not found, staying on splash")
        }
        
        // Finish splash activity so user can't go back to it
        finish()
    }

    /**
     * Disable back button during splash
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing - prevent back navigation during splash
    }
}
