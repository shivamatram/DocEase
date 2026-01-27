# 🏗️ MVVM ARCHITECTURE GUIDE - DocEase

## 📋 Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Package Structure](#package-structure)
3. [MVVM Components](#mvvm-components)
4. [Data Flow](#data-flow)
5. [Implementation Examples](#implementation-examples)
6. [Best Practices](#best-practices)

---

## 🎯 Architecture Overview

DocEase implements **MVVM (Model-View-ViewModel)** architecture pattern for clean separation of concerns:

```
┌─────────────────────────────────────────────────┐
│                     VIEW                        │
│  (Activities/Fragments - UI Layer)             │
│  - Displays data                                │
│  - Captures user input                          │
│  - Observes ViewModel                           │
└─────────────┬───────────────────────────────────┘
              │ Observes LiveData
              ▼
┌─────────────────────────────────────────────────┐
│                  VIEWMODEL                      │
│  (Business Logic Layer)                         │
│  - Prepares data for UI                         │
│  - Handles UI logic                             │
│  - Survives configuration changes               │
└─────────────┬───────────────────────────────────┘
              │ Calls repository methods
              ▼
┌─────────────────────────────────────────────────┐
│                 REPOSITORY                      │
│  (Data Layer)                                   │
│  - Single source of truth                       │
│  - Manages data sources                         │
│  - Provides clean API                           │
└─────────────┬───────────────────────────────────┘
              │ Firebase operations
              ▼
┌─────────────────────────────────────────────────┐
│           FIREBASE REALTIME DATABASE            │
│  (Remote Data Source)                           │
└─────────────────────────────────────────────────┘
```

---

## 📁 Package Structure

```
com.example.docease/
├── DocEaseApplication.kt          # Application class
├── MainActivity.kt                 # Entry point
│
├── models/                         # 📦 DATA MODELS (6 files)
│   ├── User.kt                    # User authentication model
│   ├── Doctor.kt                  # Doctor profile model
│   ├── Patient.kt                 # Patient profile model
│   ├── Appointment.kt             # Appointment model
│   ├── Availability.kt            # Time slot model
│   └── Notification.kt            # Notification model
│
├── firebase/                       # 🔥 FIREBASE MANAGERS (2 files)
│   ├── AuthManager.kt             # Authentication singleton
│   └── DatabaseManager.kt         # Database references singleton
│
├── repository/                     # 🗄️ REPOSITORIES (6 files)
│   ├── UserRepository.kt          # User data operations
│   ├── DoctorRepository.kt        # Doctor data operations
│   ├── PatientRepository.kt       # Patient data operations
│   ├── AppointmentRepository.kt   # Appointment operations
│   ├── AvailabilityRepository.kt  # Slot management
│   └── NotificationRepository.kt  # Notification operations
│
├── viewmodel/                      # 🎬 VIEWMODELS (5 files)
│   ├── AuthViewModel.kt           # Authentication logic
│   ├── DoctorViewModel.kt         # Doctor profile & management
│   ├── PatientViewModel.kt        # Patient profile management
│   ├── AppointmentViewModel.kt    # Booking & appointments
│   └── NotificationViewModel.kt   # Notifications & FCM
│
├── ui/                             # 🎨 UI LAYER (Create manually)
│   ├── auth/                      # Authentication screens
│   │   ├── LoginActivity.kt
│   │   ├── SignUpActivity.kt
│   │   └── ForgotPasswordActivity.kt
│   ├── doctor/                    # Doctor features
│   │   ├── DoctorDashboardActivity.kt
│   │   ├── DoctorProfileActivity.kt
│   │   ├── ManageAvailabilityActivity.kt
│   │   └── AppointmentListActivity.kt
│   ├── patient/                   # Patient features
│   │   ├── PatientDashboardActivity.kt
│   │   ├── PatientProfileActivity.kt
│   │   ├── DoctorListActivity.kt
│   │   ├── SearchDoctorActivity.kt
│   │   └── BookAppointmentActivity.kt
│   └── common/                    # Shared components
│       ├── NotificationActivity.kt
│       ├── adapters/
│       ├── dialogs/
│       └── widgets/
│
├── service/                        # 📲 BACKGROUND SERVICES (1 file)
│   └── DocEaseFCMService.kt       # Firebase Cloud Messaging
│
├── utils/                          # 🛠️ UTILITIES (3 files)
│   ├── Constants.kt               # App constants
│   ├── Extensions.kt              # Kotlin extensions
│   └── PreferenceManager.kt       # SharedPreferences helper
│
└── examples/                       # 📚 EXAMPLES
    └── FirebaseUsageExamples.kt   # 22 working examples
```

---

## 🧩 MVVM Components

### 1️⃣ MODEL Layer
**Location**: `models/` package  
**Purpose**: Define data structures and business entities  
**Already Created**: ✅ All 6 models complete

```kotlin
// Example: Doctor.kt
data class Doctor(
    val uid: String = "",
    val name: String = "",
    val specialization: String = "",
    val consultationFee: Double = 0.0,
    val rating: Double = 0.0
)
```

### 2️⃣ REPOSITORY Layer
**Location**: `repository/` package  
**Purpose**: Manage data sources and provide clean API  
**Already Created**: ✅ All 6 repositories complete

**Key Features**:
- CRUD operations
- Real-time listeners using Kotlin Flow
- Error handling with Result<T>
- Single source of truth

```kotlin
// Example: DoctorRepository.kt
class DoctorRepository(private val dbManager: DatabaseManager) {
    
    suspend fun getDoctorById(uid: String): Result<Doctor?> {
        return try {
            val snapshot = dbManager.doctorsRef.child(uid).get().await()
            val doctor = snapshot.getValue(Doctor::class.java)
            Result.success(doctor)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun observeDoctor(uid: String): Flow<Result<Doctor?>> = callbackFlow {
        // Real-time listener implementation
    }
}
```

### 3️⃣ VIEWMODEL Layer
**Location**: `viewmodel/` package  
**Purpose**: Prepare data for UI and handle UI logic  
**Already Created**: ✅ All 5 ViewModels complete

**Key Features**:
- Expose LiveData for UI observation
- Handle user actions
- Survive configuration changes
- Manage loading states and errors

```kotlin
// Example: DoctorViewModel.kt
class DoctorViewModel : ViewModel() {
    
    private val _doctor = MutableLiveData<Doctor?>()
    val doctor: LiveData<Doctor?> = _doctor
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    fun getDoctorProfile(uid: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = doctorRepository.getDoctorById(uid)
            _loading.value = false
            
            if (result.isSuccess) {
                _doctor.value = result.getOrNull()
            }
        }
    }
}
```

### 4️⃣ VIEW Layer
**Location**: `ui/` package  
**Purpose**: Display data and capture user input  
**Status**: ⚠️ CREATE MANUALLY

**Responsibilities**:
- Inflate layouts
- Initialize ViewModels
- Observe LiveData
- Handle user interactions
- Update UI

```kotlin
// Example: DoctorDashboardActivity.kt
class DoctorDashboardActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDoctorDashboardBinding
    private val doctorViewModel: DoctorViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        doctorViewModel.initialize(DatabaseManager.getInstance())
        setupObservers()
        loadData()
    }
    
    private fun setupObservers() {
        doctorViewModel.doctor.observe(this) { doctor ->
            doctor?.let { updateUI(it) }
        }
        
        doctorViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }
    }
}
```

---

## 🔄 Data Flow

### Example: Patient Books Appointment

```
1. USER ACTION (View)
   └─> Patient clicks "Book Appointment" button
       └─> BookAppointmentActivity.onBookClick()

2. VIEWMODEL CALL
   └─> appointmentViewModel.bookAppointment(...)
       └─> Sets _loading.value = true
       └─> Launches coroutine

3. REPOSITORY OPERATIONS
   └─> availabilityRepository.bookSlot() [ATOMIC TRANSACTION]
       └─> SUCCESS ✅
           └─> appointmentRepository.createAppointment()
               └─> SUCCESS ✅

4. LIVEDATA UPDATE
   └─> _bookingSuccess.value = appointmentId
   └─> _loading.value = false

5. UI OBSERVATION (View)
   └─> bookingSuccess.observe() triggers
       └─> Show success message
       └─> Navigate to appointment details
```

---

## 💡 Implementation Examples

### Example 1: User Login Flow

```kotlin
// 1. View (LoginActivity.kt)
class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        authViewModel.initialize(DatabaseManager.getInstance())
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupObservers() {
        authViewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.btnLogin.isEnabled = false
                }
                is AuthState.SignInSuccess -> {
                    binding.progressBar.isVisible = false
                    navigateToDashboard(state.role)
                }
                is AuthState.Error -> {
                    binding.progressBar.isVisible = false
                    binding.btnLogin.isEnabled = true
                    showError(state.message)
                }
                else -> {
                    binding.progressBar.isVisible = false
                    binding.btnLogin.isEnabled = true
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            authViewModel.signIn(email, password)
        }
    }
    
    private fun navigateToDashboard(role: UserRole) {
        val intent = when (role) {
            UserRole.DOCTOR -> Intent(this, DoctorDashboardActivity::class.java)
            UserRole.PATIENT -> Intent(this, PatientDashboardActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}
```

### Example 2: Doctor Profile Display

```kotlin
// 1. View (DoctorProfileActivity.kt)
class DoctorProfileActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDoctorProfileBinding
    private val doctorViewModel: DoctorViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val doctorId = intent.getStringExtra(Constants.Extras.EXTRA_DOCTOR_ID) ?: return
        
        doctorViewModel.initialize(DatabaseManager.getInstance())
        doctorViewModel.observeDoctorProfile(doctorId)
        
        setupObservers()
    }
    
    private fun setupObservers() {
        doctorViewModel.doctor.observe(this) { doctor ->
            doctor?.let { displayDoctorInfo(it) }
        }
    }
    
    private fun displayDoctorInfo(doctor: Doctor) {
        binding.apply {
            tvName.text = doctor.name
            tvSpecialization.text = doctor.specialization
            tvExperience.text = "${doctor.yearsOfExperience} years"
            tvRating.text = doctor.rating.toRatingString()
            tvFee.text = doctor.consultationFee.toCurrency()
            tvAddress.text = doctor.address
        }
    }
}
```

### Example 3: Appointment Booking

```kotlin
// 1. View (BookAppointmentActivity.kt)
class BookAppointmentActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityBookAppointmentBinding
    private val appointmentViewModel: AppointmentViewModel by viewModels()
    
    private lateinit var doctor: Doctor
    private lateinit var selectedDate: String
    private var selectedSlot: Slot? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        appointmentViewModel.initialize(DatabaseManager.getInstance())
        
        setupObservers()
        setupDatePicker()
        setupBookButton()
    }
    
    private fun setupObservers() {
        // Observe available slots
        appointmentViewModel.availableSlots.observe(this) { slots ->
            displayAvailableSlots(slots)
        }
        
        // Observe booking success
        appointmentViewModel.bookingSuccess.observe(this) { appointmentId ->
            if (appointmentId != null) {
                showToast(Constants.SuccessMessages.APPOINTMENT_BOOKED)
                finish()
            }
        }
        
        // Observe errors
        appointmentViewModel.error.observe(this) { error ->
            error?.let { showToast(it) }
        }
    }
    
    private fun setupBookButton() {
        binding.btnBookAppointment.setOnClickListener {
            val slot = selectedSlot ?: return@setOnClickListener
            val patientId = PreferenceManager(this).getUserId() ?: return@setOnClickListener
            val symptoms = binding.etSymptoms.text.toString()
            
            appointmentViewModel.bookAppointment(
                doctorId = doctor.uid,
                doctorName = doctor.name,
                patientId = patientId,
                patientName = "Patient Name", // Get from patient profile
                date = selectedDate,
                slot = slot,
                consultationFee = doctor.consultationFee,
                symptoms = symptoms
            )
        }
    }
}
```

---

## ✅ Best Practices

### 1. ViewModel Best Practices

```kotlin
✅ DO:
- Use LiveData for UI observation
- Launch coroutines in viewModelScope
- Initialize ViewModel in onCreate()
- Clear error messages after showing
- Expose immutable LiveData (val instead of var)

❌ DON'T:
- Pass Context to ViewModel
- Hold references to View/Activity
- Perform I/O operations synchronously
- Store UI-related data
```

### 2. Repository Best Practices

```kotlin
✅ DO:
- Return Result<T> for error handling
- Use suspend functions for async operations
- Use Flow for real-time data
- Handle exceptions properly
- Use transactions for atomic operations

❌ DON'T:
- Expose mutable data structures
- Mix UI logic with data logic
- Ignore error cases
- Make synchronous database calls
```

### 3. View Best Practices

```kotlin
✅ DO:
- Use ViewBinding
- Observe LiveData in lifecycle-aware manner
- Clear binding in onDestroyView() (Fragments)
- Initialize ViewModel with by viewModels()
- Show loading indicators

❌ DON'T:
- Hold references to ViewModels statically
- Ignore loading/error states
- Perform business logic in Activity/Fragment
- Make direct Firebase calls from UI
```

### 4. Data Flow Best Practices

```
✅ CORRECT FLOW:
View → ViewModel → Repository → Firebase

❌ WRONG FLOW:
View → Firebase (directly)
View → Repository (skipping ViewModel)
```

---

## 🎯 Next Steps

### 1. Create UI Layouts (XML)
Create layout files in `res/layout/`:
- `activity_login.xml`
- `activity_doctor_dashboard.xml`
- `activity_book_appointment.xml`
- `item_doctor.xml` (for RecyclerView)
- etc.

### 2. Create Activities/Fragments
Follow the examples in:
- `ui/auth/README.md`
- `ui/doctor/README.md`
- `ui/patient/README.md`
- `ui/common/README.md`

### 3. Create Adapters
Create RecyclerView adapters:
- `DoctorAdapter`
- `AppointmentAdapter`
- `NotificationAdapter`
- `SlotAdapter`

### 4. Add Navigation
Implement navigation between screens:
- Use Intents for Activity navigation
- Use Navigation Component for Fragment navigation
- Pass data using Constants.Extras

### 5. Test Features
Test all MVVM components:
- Login/Signup
- Profile management
- Appointment booking
- Notifications
- Real-time updates

---

## 📚 Related Documentation

- [FIREBASE_DATABASE_ARCHITECTURE.md](../FIREBASE_DATABASE_ARCHITECTURE.md) - Database structure
- [IMPLEMENTATION_GUIDE.md](../IMPLEMENTATION_GUIDE.md) - Firebase implementation
- [QUICK_START.md](../QUICK_START.md) - Quick start guide
- [examples/FirebaseUsageExamples.kt](../app/src/main/java/com/example/docease/examples/FirebaseUsageExamples.kt) - Working examples

---

## 🎓 Key Takeaways

1. **Separation of Concerns**: Each layer has a specific responsibility
2. **Testability**: ViewModels and Repositories can be unit tested
3. **Lifecycle Awareness**: ViewModels survive configuration changes
4. **Reactive Programming**: LiveData automatically updates UI
5. **Error Handling**: Result<T> provides type-safe error handling
6. **Real-time Updates**: Flow provides reactive data streams
7. **Clean Architecture**: Easy to maintain and scale

---

**🎉 All MVVM Components are Ready!**

✅ Models (6 files)  
✅ Repositories (6 files)  
✅ ViewModels (5 files)  
✅ Utilities (3 files)  
✅ Service (1 file)  
⚠️ UI Layer (Create manually based on requirements)

**Total Created**: 21 new files + existing 26 Firebase files = **47 files** 🚀
