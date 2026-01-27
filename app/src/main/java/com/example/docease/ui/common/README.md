package com.example.docease.ui.common

/**
 * UI Package: Common/Shared UI Components
 * 
 * This package will contain shared UI components used across both doctor and patient interfaces:
 * 
 * - NotificationActivity/Fragment - View all notifications
 * - SettingsActivity/Fragment - App settings
 * - AboutActivity/Fragment - About app
 * - HelpActivity/Fragment - Help & support
 * - SplashActivity - App splash screen
 * 
 * Adapters:
 * - DoctorAdapter - RecyclerView adapter for doctor list
 * - AppointmentAdapter - RecyclerView adapter for appointments
 * - NotificationAdapter - RecyclerView adapter for notifications
 * - SlotAdapter - RecyclerView adapter for time slots
 * 
 * Dialog Fragments:
 * - LoadingDialog - Loading progress dialog
 * - ConfirmationDialog - Confirmation dialog
 * - DatePickerDialog - Date selection dialog
 * - TimeSlotDialog - Time slot selection dialog
 * 
 * Custom Views:
 * - ProfileImageView - Circular profile image with initials fallback
 * - RatingView - Custom rating display
 * - EmptyStateView - Empty state placeholder
 * 
 * IMPORTANT: These are shared components.
 * Create them based on your specific UI requirements.
 * 
 * Example Adapter Structure:
 * 
 * class AppointmentAdapter(
 *     private val onItemClick: (Appointment) -> Unit
 * ) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {
 *     
 *     private var appointments = listOf<Appointment>()
 *     
 *     fun submitList(list: List<Appointment>) {
 *         appointments = list
 *         notifyDataSetChanged()
 *     }
 *     
 *     inner class ViewHolder(private val binding: ItemAppointmentBinding) :
 *         RecyclerView.ViewHolder(binding.root) {
 *         
 *         fun bind(appointment: Appointment) {
 *             binding.apply {
 *                 tvDoctorName.text = appointment.doctorName
 *                 tvDate.text = appointment.date
 *                 tvTime.text = "${appointment.startTime} - ${appointment.endTime}"
 *                 tvStatus.text = appointment.status.name
 *                 
 *                 root.setOnClickListener {
 *                     onItemClick(appointment)
 *                 }
 *             }
 *         }
 *     }
 *     
 *     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
 *         val binding = ItemAppointmentBinding.inflate(
 *             LayoutInflater.from(parent.context),
 *             parent,
 *             false
 *         )
 *         return ViewHolder(binding)
 *     }
 *     
 *     override fun onBindViewHolder(holder: ViewHolder, position: Int) {
 *         holder.bind(appointments[position])
 *     }
 *     
 *     override fun getItemCount() = appointments.size
 * }
 */

// Placeholder file - Create actual shared components here
