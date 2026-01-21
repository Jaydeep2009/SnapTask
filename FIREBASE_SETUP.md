# Firebase Setup Instructions

This document provides instructions for setting up Firebase for the Kaamly marketplace app.

## Prerequisites

1. A Google account
2. Access to the [Firebase Console](https://console.firebase.google.com/)

## Setup Steps

### 1. Create a Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project"
3. Enter project name: "Kaamly" (or your preferred name)
4. Follow the setup wizard (you can disable Google Analytics for development)

### 2. Add Android App to Firebase Project

1. In the Firebase Console, click the Android icon to add an Android app
2. Enter the package name: `com.jaydeep.kaamly`
3. Enter app nickname: "Kaamly Android"
4. Download the `google-services.json` file
5. Place the `google-services.json` file in the `app/` directory of your project

**Note:** The `google-services.json` file is already present in the project. If you're setting up a new Firebase project, replace it with your own.

### 3. Enable Firebase Authentication

1. In the Firebase Console, go to "Authentication"
2. Click "Get started"
3. Enable "Email/Password" sign-in method
4. Click "Save"

### 4. Set Up Cloud Firestore

1. In the Firebase Console, go to "Firestore Database"
2. Click "Create database"
3. Choose "Start in test mode" for development (we'll apply security rules later)
4. Select a Cloud Firestore location (choose closest to your target users)
5. Click "Enable"

### 5. Apply Firestore Security Rules

1. In the Firebase Console, go to "Firestore Database" > "Rules"
2. Copy the contents of `firestore.rules` from the project root
3. Paste into the Firebase Console rules editor
4. Click "Publish"

### 6. Set Up Firebase Storage

1. In the Firebase Console, go to "Storage"
2. Click "Get started"
3. Choose "Start in test mode" for development
4. Select a storage location (same as Firestore location recommended)
5. Click "Done"

### 7. Apply Storage Security Rules

1. In the Firebase Console, go to "Storage" > "Rules"
2. Copy the contents of `storage.rules` from the project root
3. Paste into the Firebase Console rules editor
4. Click "Publish"

### 8. Create Firestore Collections

The app will automatically create collections when data is first written. However, you can manually create them for testing:

1. In Firestore Database, click "Start collection"
2. Create the following collections:
   - `users`
   - `userProfiles`
   - `workerProfiles`
   - `tasks`
   - `bids`
   - `reviews`
   - `escrow`
   - `notifications`

### 9. Optional: Set Up Firebase Emulator Suite (for local development)

For local development without using production Firebase:

1. Install Firebase CLI: `npm install -g firebase-tools`
2. Login: `firebase login`
3. Initialize Firebase in project: `firebase init`
4. Select Firestore, Storage, and Emulators
5. Use the existing `firestore.rules` and `storage.rules` files
6. Start emulators: `firebase emulators:start`

To use emulators in the app, add this code in `KaamlyApplication.kt`:

```kotlin
if (BuildConfig.DEBUG) {
    Firebase.firestore.useEmulator("10.0.2.2", 8080)
    Firebase.auth.useEmulator("10.0.2.2", 9099)
    Firebase.storage.useEmulator("10.0.2.2", 9199)
}
```

## Verification

After setup, verify everything is working:

1. Build and run the app
2. The app should connect to Firebase without errors
3. Check Firebase Console to see if the app appears in the project

## Troubleshooting

### App crashes on startup
- Verify `google-services.json` is in the correct location (`app/` directory)
- Check that the package name in `google-services.json` matches your app's package name

### Authentication not working
- Verify Email/Password authentication is enabled in Firebase Console
- Check internet permissions are added in AndroidManifest.xml

### Firestore permission denied errors
- Verify security rules are published in Firebase Console
- Check that you're using test mode or have proper authentication

### Storage upload fails
- Verify Storage is enabled in Firebase Console
- Check storage security rules are published
- Verify file size is under 5MB limit

## Security Notes

**Important:** The current security rules are configured for development. Before deploying to production:

1. Review and tighten security rules
2. Enable proper authentication checks
3. Add rate limiting
4. Set up proper data validation
5. Consider using Firebase App Check for additional security

## Next Steps

After Firebase setup is complete, you can proceed with implementing the authentication module (Task 2).
