package com.example.docease.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.docease.R
import com.google.android.material.button.MaterialButton

/**
 * Adapter for displaying notifications in a RecyclerView
 * Supports different view types: section headers and notification items
 */
class NotificationsAdapter(
    private val items: MutableList<NotificationListItem>,
    private val onNotificationClick: (NotificationItem) -> Unit,
    private val onActionClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_NOTIFICATION = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is NotificationListItem.Header -> VIEW_TYPE_HEADER
            is NotificationListItem.Notification -> VIEW_TYPE_NOTIFICATION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_notification_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_notification, parent, false)
                NotificationViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is NotificationListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is NotificationListItem.Notification -> (holder as NotificationViewHolder).bind(item.notification)
        }
    }

    override fun getItemCount(): Int = items.size

    fun markAllAsRead() {
        items.forEachIndexed { index, item ->
            if (item is NotificationListItem.Notification && item.notification.isUnread) {
                item.notification.isUnread = false
                notifyItemChanged(index)
            }
        }
    }

    fun updateItems(newItems: List<NotificationListItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSectionHeader: TextView = itemView.findViewById(R.id.tvSectionHeader)

        fun bind(header: NotificationListItem.Header) {
            tvSectionHeader.text = header.title
        }
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconContainer: FrameLayout = itemView.findViewById(R.id.iconContainer)
        private val iconBackground: View = itemView.findViewById(R.id.iconBackground)
        private val ivNotificationIcon: ImageView = itemView.findViewById(R.id.ivNotificationIcon)
        private val unreadIndicator: View = itemView.findViewById(R.id.unreadIndicator)
        private val tvNotificationTitle: TextView = itemView.findViewById(R.id.tvNotificationTitle)
        private val tvNotificationDescription: TextView = itemView.findViewById(R.id.tvNotificationDescription)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val btnAction: MaterialButton = itemView.findViewById(R.id.btnAction)

        fun bind(notification: NotificationItem) {
            tvNotificationTitle.text = notification.title
            tvNotificationDescription.text = notification.description
            tvTime.text = notification.timeAgo

            // Set unread indicator visibility
            unreadIndicator.visibility = if (notification.isUnread) View.VISIBLE else View.GONE

            // Set icon and background based on notification type
            setNotificationAppearance(notification.type)

            // Show/hide action button
            if (notification.type == NotificationType.VIDEO_CONSULTATION) {
                btnAction.visibility = View.VISIBLE
                btnAction.text = notification.actionText ?: "Join Now"
                btnAction.setOnClickListener {
                    onActionClick(notification)
                }
            } else {
                btnAction.visibility = View.GONE
            }

            // Item click
            itemView.setOnClickListener {
                notification.isUnread = false
                unreadIndicator.visibility = View.GONE
                onNotificationClick(notification)
            }
        }

        private fun setNotificationAppearance(type: NotificationType) {
            val context = itemView.context
            
            when (type) {
                NotificationType.APPOINTMENT_CONFIRMED -> {
                    iconBackground.setBackgroundResource(R.drawable.bg_notification_icon_teal)
                    ivNotificationIcon.setImageResource(R.drawable.ic_notification_confirmed)
                }
                NotificationType.VIDEO_CONSULTATION -> {
                    iconBackground.setBackgroundResource(R.drawable.bg_notification_icon_blue)
                    ivNotificationIcon.setImageResource(R.drawable.ic_notification_video)
                }
                NotificationType.RESCHEDULE_REQUEST -> {
                    iconBackground.setBackgroundResource(R.drawable.bg_notification_icon_orange)
                    ivNotificationIcon.setImageResource(R.drawable.ic_notification_reschedule)
                }
                NotificationType.PRESCRIPTION_READY -> {
                    iconBackground.setBackgroundResource(R.drawable.bg_notification_icon_purple)
                    ivNotificationIcon.setImageResource(R.drawable.ic_notification_prescription)
                }
                NotificationType.RATE_VISIT -> {
                    iconBackground.setBackgroundResource(R.drawable.bg_notification_icon_yellow)
                    ivNotificationIcon.setImageResource(R.drawable.ic_notification_rate)
                }
                NotificationType.LAB_RESULTS -> {
                    iconBackground.setBackgroundResource(R.drawable.bg_notification_icon_red)
                    ivNotificationIcon.setImageResource(R.drawable.ic_notification_lab)
                }
            }
        }
    }
}

/**
 * Sealed class representing items in the notification list
 */
sealed class NotificationListItem {
    data class Header(val title: String) : NotificationListItem()
    data class Notification(val notification: NotificationItem) : NotificationListItem()
}

/**
 * Data class representing a single notification
 */
data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val title: String,
    val description: String,
    val timeAgo: String,
    var isUnread: Boolean = false,
    val actionText: String? = null,
    val payload: Map<String, String>? = null
)

/**
 * Enum for notification types
 */
enum class NotificationType {
    APPOINTMENT_CONFIRMED,
    VIDEO_CONSULTATION,
    RESCHEDULE_REQUEST,
    PRESCRIPTION_READY,
    RATE_VISIT,
    LAB_RESULTS
}
