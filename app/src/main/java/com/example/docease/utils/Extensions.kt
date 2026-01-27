package com.example.docease.utils

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

/**
 * Extension functions for common operations
 */

// ==================== STRING EXTENSIONS ====================

/**
 * Check if string is a valid email
 */
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

/**
 * Check if string is a valid phone number
 */
fun String.isValidPhone(): Boolean {
    return this.length in Constants.Validation.MIN_PHONE_LENGTH..Constants.Validation.MAX_PHONE_LENGTH
            && this.all { it.isDigit() || it == '+' || it == '-' || it == ' ' }
}

/**
 * Capitalize first letter of each word
 */
fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}

/**
 * Truncate string with ellipsis
 */
fun String.truncate(maxLength: Int): String {
    return if (this.length > maxLength) {
        "${this.substring(0, maxLength)}..."
    } else {
        this
    }
}

// ==================== DATE & TIME EXTENSIONS ====================

/**
 * Convert timestamp to formatted date string
 */
fun Long.toDateString(format: String = Constants.DateFormats.DISPLAY_DATE_FORMAT): String {
    return try {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        sdf.format(Date(this))
    } catch (e: Exception) {
        ""
    }
}

/**
 * Convert timestamp to formatted time string
 */
fun Long.toTimeString(format: String = Constants.DateFormats.DISPLAY_TIME_FORMAT): String {
    return try {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        sdf.format(Date(this))
    } catch (e: Exception) {
        ""
    }
}

/**
 * Convert timestamp to formatted datetime string
 */
fun Long.toDateTimeString(format: String = Constants.DateFormats.DISPLAY_DATETIME_FORMAT): String {
    return try {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        sdf.format(Date(this))
    } catch (e: Exception) {
        ""
    }
}

/**
 * Get time ago string (e.g., "5 minutes ago", "2 hours ago")
 */
fun Long.toTimeAgo(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    
    return when {
        diff < Constants.Time.MILLISECONDS_IN_MINUTE -> "Just now"
        diff < Constants.Time.MILLISECONDS_IN_HOUR -> {
            val minutes = (diff / Constants.Time.MILLISECONDS_IN_MINUTE).toInt()
            "$minutes minute${if (minutes > 1) "s" else ""} ago"
        }
        diff < Constants.Time.MILLISECONDS_IN_DAY -> {
            val hours = (diff / Constants.Time.MILLISECONDS_IN_HOUR).toInt()
            "$hours hour${if (hours > 1) "s" else ""} ago"
        }
        diff < Constants.Time.MILLISECONDS_IN_DAY * 7 -> {
            val days = (diff / Constants.Time.MILLISECONDS_IN_DAY).toInt()
            "$days day${if (days > 1) "s" else ""} ago"
        }
        else -> this.toDateString()
    }
}

/**
 * Check if date is today
 */
fun String.isToday(): Boolean {
    return try {
        val sdf = SimpleDateFormat(Constants.DateFormats.DATE_FORMAT, Locale.getDefault())
        val date = sdf.parse(this)
        val today = sdf.format(Date())
        this == today
    } catch (e: Exception) {
        false
    }
}

/**
 * Check if date is in past
 */
fun String.isPast(): Boolean {
    return try {
        val sdf = SimpleDateFormat(Constants.DateFormats.DATE_FORMAT, Locale.getDefault())
        val date = sdf.parse(this)
        val now = Date()
        date?.before(now) ?: false
    } catch (e: Exception) {
        false
    }
}

/**
 * Get current date as formatted string
 */
fun getCurrentDate(format: String = Constants.DateFormats.DATE_FORMAT): String {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return sdf.format(Date())
}

/**
 * Get current time as formatted string
 */
fun getCurrentTime(format: String = Constants.DateFormats.TIME_FORMAT): String {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return sdf.format(Date())
}

/**
 * Parse date string to timestamp
 */
fun String.toTimestamp(format: String = Constants.DateFormats.DATE_FORMAT): Long {
    return try {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        sdf.parse(this)?.time ?: 0L
    } catch (e: Exception) {
        0L
    }
}

/**
 * Add days to date string
 */
fun String.addDays(days: Int, format: String = Constants.DateFormats.DATE_FORMAT): String {
    return try {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        val date = sdf.parse(this)
        val calendar = Calendar.getInstance()
        calendar.time = date!!
        calendar.add(Calendar.DAY_OF_YEAR, days)
        sdf.format(calendar.time)
    } catch (e: Exception) {
        this
    }
}

// ==================== CONTEXT EXTENSIONS ====================

/**
 * Show toast message
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Show long toast message
 */
fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

// ==================== NUMBER EXTENSIONS ====================

/**
 * Format double as currency
 */
fun Double.toCurrency(): String {
    return "₹%.2f".format(this)
}

/**
 * Format rating (e.g., 4.5 -> "4.5 ★")
 */
fun Double.toRatingString(): String {
    return "%.1f ★".format(this)
}

/**
 * Check if number is within range
 */
fun Int.isInRange(min: Int, max: Int): Boolean {
    return this in min..max
}

// ==================== COLLECTION EXTENSIONS ====================

/**
 * Safe get element at index
 */
fun <T> List<T>.safeGet(index: Int): T? {
    return if (index in indices) this[index] else null
}

/**
 * Check if list is not null or empty
 */
fun <T> List<T>?.isNotNullOrEmpty(): Boolean {
    return this != null && this.isNotEmpty()
}

// ==================== VALIDATION HELPERS ====================

/**
 * Validate password strength
 */
fun String.isStrongPassword(): Boolean {
    // At least 8 characters, 1 uppercase, 1 lowercase, 1 digit
    val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")
    return passwordRegex.matches(this)
}

/**
 * Validate age
 */
fun Int.isValidAge(): Boolean {
    return this in Constants.Validation.MIN_AGE..Constants.Validation.MAX_AGE
}

/**
 * Validate rating
 */
fun Double.isValidRating(): Boolean {
    return this in Constants.Rating.MIN_RATING..Constants.Rating.MAX_RATING
}

// ==================== SAFE OPERATIONS ====================

/**
 * Safe string to int conversion
 */
fun String.toIntOrDefault(default: Int = 0): Int {
    return this.toIntOrNull() ?: default
}

/**
 * Safe string to double conversion
 */
fun String.toDoubleOrDefault(default: Double = 0.0): Double {
    return this.toDoubleOrNull() ?: default
}

/**
 * Safe string to long conversion
 */
fun String.toLongOrDefault(default: Long = 0L): Long {
    return this.toLongOrNull() ?: default
}

// ==================== UI HELPERS ====================

/**
 * Get initials from name (e.g., "John Doe" -> "JD")
 */
fun String.getInitials(): String {
    val parts = this.trim().split(" ")
    return when {
        parts.isEmpty() -> ""
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "${parts[0].first()}${parts.last().first()}".uppercase()
    }
}

/**
 * Mask phone number (e.g., "1234567890" -> "******7890")
 */
fun String.maskPhoneNumber(): String {
    return if (this.length >= 10) {
        "*".repeat(this.length - 4) + this.takeLast(4)
    } else {
        this
    }
}

/**
 * Mask email (e.g., "john@example.com" -> "j***@example.com")
 */
fun String.maskEmail(): String {
    val parts = this.split("@")
    return if (parts.size == 2 && parts[0].isNotEmpty()) {
        "${parts[0].first()}${"*".repeat(parts[0].length - 1)}@${parts[1]}"
    } else {
        this
    }
}
