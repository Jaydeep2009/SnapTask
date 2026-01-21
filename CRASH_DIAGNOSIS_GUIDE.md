# Crash Diagnosis Guide - Kaamly Marketplace

## Issue: App crashes after signup on role selection screen

This guide will help you diagnose and fix the crash issue.

---

## Most Common Causes

### 1. Firebase Not Properly Configured ⚠️ **MOST LIKELY**

#### Symptoms:
- App crashes immediately after signup
- Crash happens when navigating to role selection
- App crashes when reopening after signup

#### Check These:

**A. Verify google-services.json exists and is correct:**
```bash
# Check if file exists
ls app/google-services.json

# Verify package name matches
# Open app/google-services.json and check:
# "package_name": "com.jaydeep.kaamly"
```

**B. Verify Firebase Authentication is enabled:**
1. Go to Firebase Console: https://console.firebase.google.com/
2. Select your project
3. Go to "Authentication" → "Sign-in method"
4. Verify "Email/Password" is **ENABLED**
5. Status should show as "Enabled" (not "Disabled")

**C. Verify Firestore Database is created:**
1. Go to Firebase Console
2. Go to "Firestore Database"
3. You should see a database (not a "Create database" button)
4. Check if it's in "Test mode" or has proper rules

**D. Verify Firestore Security Rules:**
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow all reads and writes in test mode
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

**E. Check Firebase Storage is enabled:**
1. Go to Firebase Console
2. Go to "Storage"
3. Verify storage bucket exists
4. Check rules are in test mode

---

### 2. Internet Connection Issues

#### Symptoms:
- App crashes when trying to save data
- Timeout errors in logcat

#### Fix:
- Ensure device/emulator has internet connection
- Check AndroidManifest.xml has internet permission:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

### 3. Firestore Data Type Mismatch

#### Symptoms:
- Crash when reading user data from Firestore
- ClassCastException in logs

#### Issue:
The role field might be stored as a String instead of the UserRole enum.

#### Fix Applied:
I've already fixed this in `AuthRepositoryImpl.kt` - the role is now stored as the enum object, not as a string.

---

### 4. Missing Hilt Dependencies

#### Symptoms:
- Crash with "No implementation found" error
- Dependency injection errors

#### Check:
Verify `@AndroidEntryPoint` annotation is on MainActivity:
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // ...
}
```

---

## How to Get Crash Logs

To properly diagnose the crash, you need to see the actual error message:

### Method 1: Android Studio Logcat
1. Open Android Studio
2. Click on "Logcat" tab at the bottom
3. Run the app
4. When it crashes, look for red error messages
5. Look for lines starting with "FATAL EXCEPTION" or "AndroidRuntime"

### Method 2: ADB Command
```bash
adb logcat -d > crash_log.txt
```

### What to Look For:
- **FirebaseException**: Firebase configuration issue
- **NullPointerException**: Missing data or null user
- **ClassCastException**: Data type mismatch
- **SecurityException**: Firestore permission denied
- **NetworkException**: Internet connection issue

---

## Step-by-Step Debugging Process

### Step 1: Verify Firebase Setup

Run this checklist:

- [ ] `google-services.json` exists in `app/` directory
- [ ] Package name in `google-services.json` matches `com.jaydeep.kaamly`
- [ ] Firebase Authentication is enabled in console
- [ ] Email/Password sign-in method is enabled
- [ ] Firestore Database is created
- [ ] Firestore is in "Test mode" (for development)
- [ ] Firebase Storage is enabled
- [ ] Internet permission is in AndroidManifest.xml

### Step 2: Test Firebase Connection

Add this test code to MainActivity to verify Firebase works:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Test Firebase connection
    val firestore = FirebaseFirestore.getInstance()
    firestore.collection("test").document("test")
        .set(mapOf("test" to "value"))
        .addOnSuccessListener {
            Log.d("Firebase", "✅ Firebase connected successfully!")
        }
        .addOnFailureListener { e ->
            Log.e("Firebase", "❌ Firebase connection failed: ${e.message}")
        }
    
    // Continue with normal setup...
}
```

### Step 3: Check User Data After Signup

Add logging to see what's happening:

In `AuthRepositoryImpl.kt`, add logs:
```kotlin
override suspend fun signUp(email: String, password: String, name: String): BaseRepository.Result<User> {
    return try {
        Log.d("Auth", "Starting signup for: $email")
        
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: return BaseRepository.Result.Error(Exception("Failed to create user"))
        
        Log.d("Auth", "Firebase user created: ${firebaseUser.uid}")
        
        // ... rest of code
        
        firestore.collection(USERS_COLLECTION)
            .document(user.id)
            .set(user)
            .await()
            
        Log.d("Auth", "User saved to Firestore: ${user.id}, role: ${user.role}")
        
        BaseRepository.Result.Success(user)
    } catch (e: Exception) {
        Log.e("Auth", "Signup failed: ${e.message}", e)
        BaseRepository.Result.Error(e)
    }
}
```

### Step 4: Check Role Selection

Add logging to `AuthViewModel.kt`:
```kotlin
fun selectRole(role: UserRole) {
    val user = _currentUser.value
    Log.d("AuthViewModel", "selectRole called with: $role, current user: ${user?.id}")
    
    if (user == null) {
        Log.e("AuthViewModel", "Cannot select role - user is null!")
        return
    }
    
    // ... rest of code
}
```

---

## Quick Fixes to Try

### Fix 1: Clear App Data
Sometimes cached data causes issues:
1. Go to device Settings → Apps → Kaamly
2. Click "Storage"
3. Click "Clear Data" and "Clear Cache"
4. Restart the app

### Fix 2: Uninstall and Reinstall
```bash
adb uninstall com.jaydeep.kaamly
./gradlew installDebug
```

### Fix 3: Use Firebase Emulator (for testing without internet)
```bash
# Install Firebase CLI
npm install -g firebase-tools

# Start emulators
firebase emulators:start
```

Then add to `KaamlyApplication.kt`:
```kotlin
class KaamlyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Use Firebase Emulator for local testing
        if (BuildConfig.DEBUG) {
            FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080)
            FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
            FirebaseStorage.getInstance().useEmulator("10.0.2.2", 9199)
        }
    }
}
```

### Fix 4: Simplify Navigation
If the issue is navigation-related, try this temporary fix in `NavGraph.kt`:

```kotlin
composable(Screen.Auth.RoleSelection.route) {
    RoleSelectionScreen(
        onRoleSelected = { role ->
            // Simplified navigation - just go to user dashboard
            navController.navigate(Screen.User.Dashboard.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    )
}
```

---

## Expected Behavior vs Current Behavior

### Expected Flow:
1. User signs up with email/password ✅
2. User is created in Firebase Auth ✅
3. User document is saved to Firestore ✅
4. Navigate to Role Selection screen ✅
5. User selects role ❌ **CRASHES HERE**
6. Role is updated in Firestore
7. Navigate to dashboard

### What's Likely Happening:
- Role selection screen loads ✅
- User clicks a role ✅
- `selectRole()` is called ✅
- Firestore update fails ❌ **CRASH**
- OR: Navigation happens before Firestore update completes ❌ **CRASH**
- OR: Dashboard tries to load before user data is ready ❌ **CRASH**

---

## Specific Error Messages and Solutions

### Error: "Permission denied"
**Cause:** Firestore security rules are too restrictive
**Solution:** Set Firestore to test mode:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

### Error: "No such document"
**Cause:** User document wasn't created during signup
**Solution:** Check `AuthRepositoryImpl.signUp()` - ensure the user document is being saved

### Error: "ClassCastException: String cannot be cast to UserRole"
**Cause:** Role stored as String instead of enum
**Solution:** Already fixed - role is now stored as enum object

### Error: "NullPointerException: currentUser is null"
**Cause:** Dashboard loads before user data is fetched
**Solution:** Already fixed - added null check in UserDashboardScreen

---

## Testing Checklist

After applying fixes, test this flow:

1. [ ] Uninstall app completely
2. [ ] Clear Firebase Authentication users (delete test users)
3. [ ] Install fresh build
4. [ ] Open app - should show login screen
5. [ ] Click "Sign Up"
6. [ ] Enter email: test@example.com
7. [ ] Enter password: test123456
8. [ ] Enter name: Test User
9. [ ] Click "Sign Up" - should succeed
10. [ ] Role selection screen should appear
11. [ ] Click "Post Tasks" role
12. [ ] Should navigate to User Dashboard (no crash)
13. [ ] Dashboard should show "Welcome, Test User!"

---

## If Still Crashing

**Please provide me with:**

1. **Logcat output** - The actual crash error message
2. **Firebase Console screenshots** showing:
   - Authentication is enabled
   - Firestore database exists
   - Storage is enabled
3. **Confirmation that:**
   - `google-services.json` exists in `app/` directory
   - Package name matches `com.jaydeep.kaamly`
   - Internet connection is working

**To get logcat:**
```bash
# Run this after the crash
adb logcat -d > crash_log.txt

# Then share the crash_log.txt file
```

---

## Emergency Workaround

If you need to demo quickly and can't fix the crash, use this workaround:

1. Skip role selection entirely
2. Set default role to USER in signup
3. Navigate directly to dashboard

In `AuthRepositoryImpl.kt`:
```kotlin
val user = User(
    id = firebaseUser.uid,
    email = email,
    name = name,
    role = UserRole.USER, // Always USER by default
    createdAt = System.currentTimeMillis()
)
```

In `NavGraph.kt`:
```kotlin
composable(Screen.Auth.Signup.route) {
    SignupScreen(
        onNavigateToLogin = { /* ... */ },
        onSignupSuccess = {
            // Skip role selection, go straight to dashboard
            navController.navigate(Screen.User.Dashboard.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    )
}
```

---

## Contact for Help

If you're still stuck, please share:
1. The exact error message from logcat
2. Screenshots of Firebase Console (Authentication, Firestore, Storage)
3. Confirmation of which fixes you've tried

I'll help you resolve it!
