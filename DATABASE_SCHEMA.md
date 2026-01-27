# 🗺️ DocEase Database Schema Diagram

## Visual Representation of Firebase Realtime Database Structure

```
Firebase Realtime Database Root
│
├── 👤 users/
│   └── {uid}/
│       ├── uid: String
│       ├── email: String
│       ├── role: String ["doctor" | "patient"]
│       └── createdAt: Long
│
├── 👨‍⚕️ doctors/
│   └── {uid}/
│       ├── uid: String
│       ├── name: String
│       ├── email: String
│       ├── specialization: String
│       ├── experience: String
│       ├── clinicAddress: String
│       ├── consultationFee: Double
│       ├── phoneNumber: String
│       ├── profileImageUrl: String
│       ├── rating: Double (0-5)
│       ├── totalPatients: Int
│       ├── createdAt: Long
│       └── isAvailable: Boolean
│
├── 👥 patients/
│   └── {uid}/
│       ├── uid: String
│       ├── name: String
│       ├── email: String
│       ├── age: Int
│       ├── gender: String ["MALE" | "FEMALE" | "OTHER"]
│       ├── phoneNumber: String
│       ├── medicalHistory: String
│       ├── bloodGroup: String
│       ├── totalVisits: Int
│       ├── lastVisitTimestamp: Long
│       └── createdAt: Long
│
├── 📅 appointments/
│   └── {appointmentId}/
│       ├── appointmentId: String (auto-generated)
│       ├── doctorId: String → references doctors/{uid}
│       ├── doctorName: String (denormalized)
│       ├── patientId: String → references patients/{uid}
│       ├── patientName: String (denormalized)
│       ├── date: String (yyyy-MM-dd)
│       ├── startTime: String (HH:mm)
│       ├── endTime: String (HH:mm)
│       ├── consultationFee: Double
│       ├── status: String ["PENDING" | "CONFIRMED" | "COMPLETED" | "CANCELLED"]
│       ├── symptoms: String
│       ├── createdAt: Long
│       └── updatedAt: Long
│
├── 🕒 availability/
│   └── {doctorId}/
│       └── {date}/           (yyyy-MM-dd)
│           └── {slotId}/
│               ├── slotId: String
│               ├── startTime: String (HH:mm)
│               ├── endTime: String (HH:mm)
│               ├── isBooked: Boolean
│               └── appointmentId: String? → references appointments/{id}
│
├── 🔔 notifications/
│   └── {userId}/
│       └── {notificationId}/
│           ├── notificationId: String (auto-generated)
│           ├── title: String
│           ├── message: String
│           ├── type: String ["APPOINTMENT_CONFIRMED" | "APPOINTMENT_CANCELLED" | ...]
│           ├── relatedId: String? (appointmentId or other entity)
│           ├── isRead: Boolean
│           └── timestamp: Long
│
└── 🔐 tokens/
    └── {uid}/
        ├── fcmToken: String
        └── updatedAt: Long
```

---

## 🔗 Relationships & Data Flow

### 1️⃣ User Registration Flow
```
Firebase Auth Signup
         ↓
   Create users/{uid}
         ↓
    Check role
    ├─→ doctor → Create doctors/{uid}
    └─→ patient → Create patients/{uid}
```

### 2️⃣ Appointment Booking Flow
```
Patient selects doctor
         ↓
View availability/{doctorId}/{date}/
         ↓
Select available slot (isBooked: false)
         ↓
ATOMIC TRANSACTION: Book slot
├─→ Set isBooked: true
├─→ Set appointmentId
└─→ If fails: Slot already booked
         ↓
Create appointments/{appointmentId}
         ↓
Send notifications to doctor & patient
         ↓
Increment patient's totalVisits
         ↓
Increment doctor's totalPatients
```

### 3️⃣ Real-Time Updates Flow
```
Doctor confirms appointment
         ↓
Update appointments/{id}/status
         ↓
Firebase pushes update to all listeners
         ↓
Patient's app receives real-time update
         ↓
UI updates automatically
```

---

## 🔍 Query Patterns

### Search Doctors by Specialization
```
doctors/
  .orderByChild("specialization")
  .equalTo("Cardiologist")
```

### Get Doctor's Appointments
```
appointments/
  .orderByChild("doctorId")
  .equalTo(doctorId)
```

### Get Patient's History
```
appointments/
  .orderByChild("patientId")
  .equalTo(patientId)
```

### Get Available Slots
```
availability/{doctorId}/{date}/
  Filter where isBooked == false
```

### Get Unread Notifications
```
notifications/{userId}/
  .orderByChild("isRead")
  .equalTo(false)
```

---

## 📊 Indexes for Performance

```json
{
  "rules": {
    "doctors": {
      ".indexOn": ["specialization", "rating", "isAvailable"]
    },
    "appointments": {
      ".indexOn": ["doctorId", "patientId", "status", "date"]
    },
    "notifications": {
      "$userId": {
        ".indexOn": ["isRead", "timestamp"]
      }
    }
  }
}
```

---

## 🎯 Data Denormalization Strategy

### Why Denormalize?
In NoSQL databases, we intentionally duplicate data to optimize reads.

### Example: Appointments Node
```json
{
  "appointments": {
    "appt123": {
      "doctorId": "uid1",
      "doctorName": "Dr. John Smith",  ← Denormalized (copied from doctors/)
      "patientId": "uid2",
      "patientName": "Jane Doe"        ← Denormalized (copied from patients/)
    }
  }
}
```

**Benefits:**
- ✅ Read appointment data without fetching doctor + patient separately
- ✅ Faster UI rendering (1 read instead of 3)
- ✅ Works offline (all data in one place)

**Trade-off:**
- ❌ If doctor changes name, old appointments still show old name
- ✅ This is acceptable - historical records should be immutable

---

## 🔐 Security Rules Mapping

```
users/
  ├─ Read: Own data only
  └─ Write: Own data only

doctors/
  ├─ Read: Anyone authenticated
  └─ Write: Only if role === "doctor"

patients/
  ├─ Read: Self or any doctor
  └─ Write: Only if role === "patient"

appointments/
  ├─ Read: Anyone authenticated
  └─ Write: Only involved parties

availability/
  ├─ Read: Anyone
  └─ Write: Only doctor (owner)

notifications/
  ├─ Read: Owner only
  └─ Write: System + owner

tokens/
  ├─ Read: No one (security)
  └─ Write: Owner only
```

---

## 📈 Scalability Considerations

### Horizontal Scaling (Already Optimized)
- ✅ Flat structure (no deep nesting)
- ✅ Each doctor's slots are independent
- ✅ Each user's notifications are separate
- ✅ Appointments are indexed by doctorId and patientId

### Vertical Scaling (For Future)
If you reach 100,000+ users:
1. **Partition availability by month**
   ```
   availability/{doctorId}/{year}/{month}/{date}/{slotId}
   ```

2. **Archive old appointments**
   ```
   appointments_archive/{year}/{appointmentId}
   ```

3. **Paginate doctor search**
   ```
   doctors/
     .limitToFirst(20)
     .startAfter(lastLoadedKey)
   ```

---

## 🔄 Data Consistency Rules

### When to Update Multiple Nodes

#### Booking Appointment
```
1. availability/{doctorId}/{date}/{slotId} → isBooked: true
2. appointments/{id} → Create new record
3. patients/{patientId} → Increment totalVisits
4. doctors/{doctorId} → Increment totalPatients
5. notifications/{doctorId}/{id} → Create notification
6. notifications/{patientId}/{id} → Create notification
```

#### Cancelling Appointment
```
1. appointments/{id} → status: "CANCELLED"
2. availability/{doctorId}/{date}/{slotId} → isBooked: false, appointmentId: null
3. notifications/{userId}/{id} → Create cancellation notice
```

---

## 🧠 Memory & Storage Optimization

### Average Node Sizes
```
users/{uid}           ~200 bytes
doctors/{uid}         ~500 bytes
patients/{uid}        ~400 bytes
appointments/{id}     ~300 bytes
availability/slot     ~100 bytes
notifications/{id}    ~200 bytes
tokens/{uid}          ~150 bytes
```

### Storage Calculation (1000 active users)
```
users:          1000 × 200 B    = 200 KB
doctors:         500 × 500 B    = 250 KB
patients:        500 × 400 B    = 200 KB
appointments:   5000 × 300 B    = 1.5 MB
availability:  60000 × 100 B    = 6 MB
notifications: 20000 × 200 B    = 4 MB
tokens:         1000 × 150 B    = 150 KB
────────────────────────────────────────
TOTAL:                           12.3 MB
```

**Conclusion:** Can easily handle 10,000+ users on Firebase free tier (1 GB limit)

---

## 📱 Real-Time Listener Strategy

### Always Listen (Keep Synced)
```kotlin
// Current user's appointments
appointmentRepository.observePatientAppointments(userId)

// Current user's notifications
notificationRepository.observeNotifications(userId)
```

### Listen When Screen Active
```kotlin
// Doctor list (only in DoctorListActivity)
doctorRepository.observeAllDoctors()

// Slot availability (only in BookingActivity)
availabilityRepository.observeSlots(doctorId, date)
```

### Don't Keep Synced (One-Time Reads)
```kotlin
// User profile (rarely changes)
userRepository.getUserById(uid)

// Historical data
appointmentRepository.getAppointmentHistory(patientId)
```

---

## 🎯 Summary

This database structure is:
- ✅ **Normalized where needed** (users, doctors, patients separate)
- ✅ **Denormalized for speed** (names copied to appointments)
- ✅ **Indexed for queries** (specialization, doctorId, patientId)
- ✅ **Scalable** (flat structure, no deep nesting)
- ✅ **Secure** (role-based rules)
- ✅ **Real-time ready** (all nodes support listeners)
- ✅ **Offline-first** (persistence enabled)

**Perfect for a production healthcare app! 🏥**

---

**Created:** January 6, 2026
**Version:** 1.0.0
