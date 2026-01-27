package com.example.docease.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R
import com.google.android.material.snackbar.Snackbar

/**
 * Activity displaying user notifications
 * Features grouped sections (Today, Yesterday, Earlier) with different notification types
 */
class NotificationsActivity : AppCompatActivity() {

    // Views
    private lateinit var btnBack: ImageButton
    private lateinit var tvMarkAllRead: TextView
    private lateinit var rvNotifications: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var layoutLoading: FrameLayout

    // Adapter
    private lateinit var notificationsAdapter: NotificationsAdapter
    private val notificationItems = mutableListOf<NotificationListItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notifications)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        initViews()
        setupWindowInsets()
        setupClickListeners()
        setupRecyclerView()
        loadNotifications()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvMarkAllRead = findViewById(R.id.tvMarkAllRead)
        rvNotifications = findViewById(R.id.rvNotifications)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        layoutLoading = findViewById(R.id.layoutLoading)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        tvMarkAllRead.setOnClickListener {
            markAllAsRead()
        }
    }

    private fun setupRecyclerView() {
        notificationsAdapter = NotificationsAdapter(
            items = notificationItems,
            onNotificationClick = { notification ->
                handleNotificationClick(notification)
            },
            onActionClick = { notification ->
                handleActionClick(notification)
            }
        )

        rvNotifications.apply {
            layoutManager = LinearLayoutManager(this@NotificationsActivity)
            adapter = notificationsAdapter
        }
    }

    private fun loadNotifications() {
        // Show loading state
        layoutLoading.visibility = View.VISIBLE
        rvNotifications.visibility = View.GONE
        layoutEmptyState.visibility = View.GONE

        // Simulate network delay then load sample data
        rvNotifications.postDelayed({
            val notifications = getSampleNotifications()
            
            if (notifications.isEmpty()) {
                showEmptyState()
            } else {
                showNotifications(notifications)
            }
        }, 500)
    }

    private fun getSampleNotifications(): List<NotificationListItem> {
        val items = mutableListOf<NotificationListItem>()

        // Today section
        items.add(NotificationListItem.Header("Today"))
        items.add(
            NotificationListItem.Notification(
                NotificationItem(
                    id = "1",
                    type = NotificationType.APPOINTMENT_CONFIRMED,
                    title = "Appointment Confirmed",
                    description = "Your appointment with Dr. Sarah Johnson has been confirmed for Dec 20, 2024 at 10:00 AM",
                    timeAgo = "2m ago",
                    isUnread = true
                )
            )
        )
        items.add(
            NotificationListItem.Notification(
                NotificationItem(
                    id = "2",
                    type = NotificationType.VIDEO_CONSULTATION,
                    title = "Video Consultation Starting",
                    description = "Your video consultation with Dr. Michael Chen is starting in 5 minutes",
                    timeAgo = "15m ago",
                    isUnread = true,
                    actionText = "Join Now"
                )
            )
        )
        items.add(
            NotificationListItem.Notification(
                NotificationItem(
                    id = "3",
                    type = NotificationType.PRESCRIPTION_READY,
                    title = "Prescription Ready",
                    description = "Your prescription from Dr. Emily White is ready to download",
                    timeAgo = "1h ago",
                    isUnread = true
                )
            )
        )

        // Yesterday section
        items.add(NotificationListItem.Header("Yesterday"))
        items.add(
            NotificationListItem.Notification(
                NotificationItem(
                    id = "4",
                    type = NotificationType.RESCHEDULE_REQUEST,
                    title = "Reschedule Request",
                    description = "Dr. John Doe has requested to reschedule your appointment to Dec 22, 2024",
                    timeAgo = "1d ago",
                    isUnread = false
                )
            )
        )
        items.add(
            NotificationListItem.Notification(
                NotificationItem(
                    id = "5",
                    type = NotificationType.RATE_VISIT,
                    title = "Rate Your Visit",
                    description = "How was your visit with Dr. Sarah Johnson? Share your experience",
                    timeAgo = "1d ago",
                    isUnread = false
                )
            )
        )

        // Earlier section
        items.add(NotificationListItem.Header("Earlier"))
        items.add(
            NotificationListItem.Notification(
                NotificationItem(
                    id = "6",
                    type = NotificationType.LAB_RESULTS,
                    title = "Lab Results Available",
                    description = "Your blood test results are now available. Tap to view details",
                    timeAgo = "3d ago",
                    isUnread = false
                )
            )
        )
        items.add(
            NotificationListItem.Notification(
                NotificationItem(
                    id = "7",
                    type = NotificationType.APPOINTMENT_CONFIRMED,
                    title = "Appointment Confirmed",
                    description = "Your follow-up appointment with Dr. Emily White has been confirmed",
                    timeAgo = "5d ago",
                    isUnread = false
                )
            )
        )
        items.add(
            NotificationListItem.Notification(
                NotificationItem(
                    id = "8",
                    type = NotificationType.VIDEO_CONSULTATION,
                    title = "Video Consultation Completed",
                    description = "Your video consultation with Dr. Michael Chen has been completed",
                    timeAgo = "1w ago",
                    isUnread = false
                )
            )
        )

        return items
    }

    private fun showEmptyState() {
        layoutLoading.visibility = View.GONE
        rvNotifications.visibility = View.GONE
        layoutEmptyState.visibility = View.VISIBLE
    }

    private fun showNotifications(items: List<NotificationListItem>) {
        layoutLoading.visibility = View.GONE
        layoutEmptyState.visibility = View.GONE
        rvNotifications.visibility = View.VISIBLE

        notificationItems.clear()
        notificationItems.addAll(items)
        notificationsAdapter.notifyDataSetChanged()
    }

    private fun markAllAsRead() {
        notificationsAdapter.markAllAsRead()
        
        Snackbar.make(
            findViewById(android.R.id.content),
            "All notifications marked as read",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun handleNotificationClick(notification: NotificationItem) {
        when (notification.type) {
            NotificationType.APPOINTMENT_CONFIRMED -> {
                openAppointmentStatus(notification)
            }
            NotificationType.VIDEO_CONSULTATION -> {
                openVideoConsultation(notification)
            }
            NotificationType.RESCHEDULE_REQUEST -> {
                openRescheduleDetails(notification)
            }
            NotificationType.PRESCRIPTION_READY -> {
                openPrescriptionDownload(notification)
            }
            NotificationType.RATE_VISIT -> {
                openRatingScreen(notification)
            }
            NotificationType.LAB_RESULTS -> {
                openLabResults(notification)
            }
        }
    }

    private fun handleActionClick(notification: NotificationItem) {
        when (notification.type) {
            NotificationType.VIDEO_CONSULTATION -> {
                joinVideoCall(notification)
            }
            else -> {
                handleNotificationClick(notification)
            }
        }
    }

    private fun openAppointmentStatus(notification: NotificationItem) {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Opening appointment details...",
            Snackbar.LENGTH_SHORT
        ).show()
        
        // In a real app:
        // val intent = Intent(this, AppointmentStatusActivity::class.java)
        // intent.putExtra("appointment_id", notification.payload?.get("appointment_id"))
        // startActivity(intent)
    }

    private fun openVideoConsultation(notification: NotificationItem) {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Opening video consultation...",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun joinVideoCall(notification: NotificationItem) {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Joining video call...",
            Snackbar.LENGTH_SHORT
        ).show()
        
        // In a real app:
        // val intent = Intent(this, VideoCallActivity::class.java)
        // intent.putExtra("room_id", notification.payload?.get("room_id"))
        // startActivity(intent)
    }

    private fun openRescheduleDetails(notification: NotificationItem) {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Opening reschedule request...",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun openPrescriptionDownload(notification: NotificationItem) {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Downloading prescription...",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun openRatingScreen(notification: NotificationItem) {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Opening rating screen...",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun openLabResults(notification: NotificationItem) {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Opening lab results...",
            Snackbar.LENGTH_SHORT
        ).show()
    }
}
