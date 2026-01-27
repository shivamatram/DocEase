# Firebase Realtime Database Security Rules

## 📋 Overview
This file contains production-ready security rules for the DocEase Firebase Realtime Database.

## 🔒 Security Principles

### 1. **Authentication Required**
- All read/write operations require authentication (`auth != null`)
- No anonymous access to any data

### 2. **Role-Based Access Control**
- Users can only access data relevant to their role (doctor/patient)
- Doctors can modify their own profile in `doctors/` node
- Patients can modify their own profile in `patients/` node

### 3. **Data Validation**
- Required fields are enforced using `.validate` rules
- Data types and value ranges are validated
- Prevents malformed data entry

### 4. **Indexed Queries**
- Important fields are indexed for efficient queries
- `doctors`: indexed on specialization, rating, isAvailable
- `appointments`: indexed on doctorId, patientId, status, date

## 🚀 Deployment Instructions

### Option 1: Firebase Console (Recommended for First Time)
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Navigate to **Realtime Database** → **Rules**
4. Copy the contents of `database-rules.json`
5. Paste into the rules editor
6. Click **Publish**

### Option 2: Firebase CLI
```bash
# Install Firebase CLI (if not installed)
npm install -g firebase-tools

# Login to Firebase
firebase login

# Initialize Firebase in your project (if not done)
firebase init database

# Deploy rules
firebase deploy --only database
```

## 📝 Rule Breakdown

### `/users` Node
```json
{
  ".read": "User can read their own data",
  ".write": "User can write their own data",
  "role": "Must be 'doctor' or 'patient'"
}
```

### `/doctors` Node
```json
{
  ".read": "Anyone authenticated can read doctor profiles",
  ".write": "Only the doctor can modify their profile",
  "rating": "Must be between 0-5"
}
```

### `/patients` Node
```json
{
  ".read": "Patient or any doctor can read",
  ".write": "Only the patient can modify",
  "age": "Must be between 0-150"
}
```

### `/appointments` Node
```json
{
  ".read": "Any authenticated user can read",
  ".write": "Only doctor or patient involved can modify",
  "status": "Must be PENDING/CONFIRMED/COMPLETED/CANCELLED"
}
```

### `/availability` Node
```json
{
  ".read": "Anyone can view doctor's availability",
  ".write": "Only the doctor can modify their slots"
}
```

### `/notifications` Node
```json
{
  ".read": "Only the user can read their notifications",
  ".write": "System can create, user can mark as read"
}
```

### `/tokens` Node
```json
{
  ".read": "No one can read tokens (security)",
  ".write": "Only the user can update their token"
}
```

## 🧪 Testing Rules

### Test Rule Simulator (Firebase Console)
1. Go to Realtime Database → Rules
2. Click **Rules Playground** tab
3. Test scenarios:
   - Authenticated user reading their profile ✅
   - User A trying to read User B's patient data ❌
   - Doctor updating their availability ✅
   - Patient trying to update doctor's profile ❌

### Test with Firebase CLI
```bash
firebase emulators:start --only database
```

## ⚠️ Important Notes

1. **Never use test rules in production:**
   ```json
   // ❌ DANGEROUS - DO NOT USE IN PRODUCTION
   {
     "rules": {
       ".read": true,
       ".write": true
     }
   }
   ```

2. **Always validate on server side too:**
   - Client-side validation can be bypassed
   - These rules are enforced by Firebase (server-side)

3. **Monitor Rule Violations:**
   - Go to Firebase Console → Realtime Database → Usage
   - Check for denied read/write operations

4. **Backup Before Changing:**
   - Always export current rules before making changes
   - Rules can be backed up using Firebase CLI

## 🔐 Admin Operations

For admin operations (e.g., bulk updates, data migration), use:

### Firebase Admin SDK (Node.js)
```javascript
const admin = require('firebase-admin');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccountKey),
  databaseURL: 'https://your-project.firebaseio.com'
});

// Admin SDK bypasses security rules
const db = admin.database();
db.ref('users').once('value', snapshot => {
  // Admin has full access
});
```

## 📊 Performance Optimization

### Indexing Strategy
Current indexes:
- `doctors/.indexOn = ["specialization", "rating", "isAvailable"]`
- `appointments/.indexOn = ["doctorId", "patientId", "status", "date"]`

Add more indexes if you see warnings in Firebase Console like:
> "Using an unspecified index. Consider adding .indexOn..."

## 🚨 Common Security Issues (Avoided)

✅ **This implementation prevents:**
- Unauthorized data access
- Data tampering by wrong users
- Malformed data entries
- Privilege escalation attacks
- Token exposure

## 📞 Support

If you encounter issues with rules:
1. Check Firebase Console → Realtime Database → Usage for error logs
2. Use Firebase Rules Playground to test specific scenarios
3. Refer to [Firebase Security Rules Documentation](https://firebase.google.com/docs/database/security)

---

**Last Updated:** January 6, 2026
**Version:** 1.0.0
**Status:** Production Ready ✅
