package com.example.docease.ui.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.docease.R
import com.example.docease.ui.auth.LoginActivity
import com.example.docease.ui.profile.PatientProfileActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar

/**
 * Settings Activity for DocEase healthcare app
 * Provides user preferences, support options, and account management
 */
class SettingsActivity : AppCompatActivity() {

    // Views - Toolbar
    private lateinit var toolbar: MaterialToolbar

    // Views - General Section
    private lateinit var layoutPushNotifications: LinearLayout
    private lateinit var switchPushNotifications: MaterialSwitch
    private lateinit var layoutProfileSettings: LinearLayout
    private lateinit var layoutLanguage: LinearLayout
    private lateinit var tvCurrentLanguage: TextView

    // Views - Support & About Section
    private lateinit var layoutHelpSupport: LinearLayout
    private lateinit var layoutPrivacyPolicy: LinearLayout
    private lateinit var layoutTermsService: LinearLayout
    private lateinit var layoutRateApp: LinearLayout

    // Views - Logout & Version
    private lateinit var btnLogout: MaterialButton
    private lateinit var tvAppVersion: TextView

    // SharedPreferences
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val PREFS_NAME = "docease_settings"
        private const val KEY_PUSH_NOTIFICATIONS = "push_notifications_enabled"
        private const val KEY_LANGUAGE = "app_language"
        
        // URLs - Replace with actual URLs
        private const val URL_HELP = "https://docease.com/help"
        private const val URL_PRIVACY = "https://docease.com/privacy"
        private const val URL_TERMS = "https://docease.com/terms"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        initViews()
        setupWindowInsets()
        setupToolbar()
        loadSettings()
        setupClickListeners()
        displayAppVersion()
    }

    private fun initViews() {
        // Toolbar
        toolbar = findViewById(R.id.toolbar)

        // General Section
        layoutPushNotifications = findViewById(R.id.layoutPushNotifications)
        switchPushNotifications = findViewById(R.id.switchPushNotifications)
        layoutProfileSettings = findViewById(R.id.layoutProfileSettings)
        layoutLanguage = findViewById(R.id.layoutLanguage)
        tvCurrentLanguage = findViewById(R.id.tvCurrentLanguage)

        // Support & About Section
        layoutHelpSupport = findViewById(R.id.layoutHelpSupport)
        layoutPrivacyPolicy = findViewById(R.id.layoutPrivacyPolicy)
        layoutTermsService = findViewById(R.id.layoutTermsService)
        layoutRateApp = findViewById(R.id.layoutRateApp)

        // Logout & Version
        btnLogout = findViewById(R.id.btnLogout)
        tvAppVersion = findViewById(R.id.tvAppVersion)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadSettings() {
        // Load push notifications preference
        val pushEnabled = prefs.getBoolean(KEY_PUSH_NOTIFICATIONS, true)
        switchPushNotifications.isChecked = pushEnabled

        // Load language preference
        val language = prefs.getString(KEY_LANGUAGE, "English") ?: "English"
        tvCurrentLanguage.text = language
    }

    private fun setupClickListeners() {
        // Push Notifications Toggle
        switchPushNotifications.setOnCheckedChangeListener { _, isChecked ->
            savePushNotificationSetting(isChecked)
            val message = if (isChecked) {
                "Push notifications enabled"
            } else {
                "Push notifications disabled"
            }
            showSnackbar(message)
        }

        // Profile Settings
        layoutProfileSettings.setOnClickListener {
            navigateToProfileSettings()
        }

        // Language
        layoutLanguage.setOnClickListener {
            showLanguageDialog()
        }

        // Help & Support
        layoutHelpSupport.setOnClickListener {
            openUrl(URL_HELP)
        }

        // Privacy Policy
        layoutPrivacyPolicy.setOnClickListener {
            openUrl(URL_PRIVACY)
        }

        // Terms of Service
        layoutTermsService.setOnClickListener {
            openUrl(URL_TERMS)
        }

        // Rate App
        layoutRateApp.setOnClickListener {
            openPlayStore()
        }

        // Logout
        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun savePushNotificationSetting(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PUSH_NOTIFICATIONS, enabled).apply()
    }

    private fun navigateToProfileSettings() {
        val intent = Intent(this, PatientProfileActivity::class.java)
        startActivity(intent)
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Spanish", "French", "German", "Hindi", "Chinese")
        val currentLanguage = prefs.getString(KEY_LANGUAGE, "English") ?: "English"
        var selectedIndex = languages.indexOf(currentLanguage).coerceAtLeast(0)

        AlertDialog.Builder(this, R.style.Theme_DocEase_Dialog)
            .setTitle("Select Language")
            .setSingleChoiceItems(languages, selectedIndex) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton("Apply") { dialog, _ ->
                val newLanguage = languages[selectedIndex]
                saveLanguageSetting(newLanguage)
                tvCurrentLanguage.text = newLanguage
                showSnackbar("Language changed to $newLanguage")
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun saveLanguageSetting(language: String) {
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
        // Note: In a real app, you would also apply locale changes here
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            showSnackbar("Unable to open link")
        }
    }

    private fun openPlayStore() {
        try {
            // Try opening Play Store app
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to browser
            try {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
                startActivity(intent)
            } catch (e2: Exception) {
                showSnackbar("Unable to open Play Store")
            }
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this, R.style.Theme_DocEase_Dialog)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out of your account?")
            .setPositiveButton("Log Out") { dialog, _ ->
                performLogout()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performLogout() {
        // Clear user session/preferences
        prefs.edit().clear().apply()

        // Clear any Firebase auth if using
        // FirebaseAuth.getInstance().signOut()

        // Navigate to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun displayAppVersion() {
        try {
            val pm = packageManager
            val info = pm.getPackageInfo(packageName, 0)
            val versionName = info.versionName ?: "1.0.0"
            tvAppVersion.text = "Version $versionName"
        } catch (e: Exception) {
            tvAppVersion.text = "Version 1.0.0"
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(getColor(R.color.docease_primary))
            .setTextColor(getColor(R.color.white))
            .show()
    }
}
