# Role Selection Auto-Navigation Fix

## Issue
After signup, the role selection screen appeared for only a microsecond before automatically navigating to the create task page, without giving the user a chance to select their role.

## Root Cause
The `LaunchedEffect(authState)` in `RoleSelectionScreen.kt` was observing the authentication state. When a user signed up, they were assigned a default role (`UserRole.USER`), so the `authState` was already `AuthState.Authenticated`. This caused the screen to immediately call `onRoleSelected()` upon loading, triggering auto-navigation.

## Solution
Modified the navigation logic to only trigger after the user explicitly clicks the "Continue" button:

1. **Added a flag**: `hasClickedContinue` to track whether the user has clicked the Continue button
2. **Updated LaunchedEffect**: Changed from `LaunchedEffect(authState)` to `LaunchedEffect(authState, hasClickedContinue)` 
3. **Conditional navigation**: Navigation only occurs when BOTH conditions are met:
   - User has clicked Continue (`hasClickedContinue == true`)
   - Role update succeeded (`authState is AuthState.Authenticated`)
4. **Error handling**: Reset the flag if role update fails, allowing the user to try again

## Changes Made
**File**: `app/src/main/java/com/jaydeep/kaamly/ui/screens/auth/RoleSelectionScreen.kt`

### Before:
```kotlin
LaunchedEffect(authState) {
    when (val state = authState) {
        is AuthState.Authenticated -> {
            // Immediately navigates on any Authenticated state
            onRoleSelected(state.user.role)
        }
        // ...
    }
}
```

### After:
```kotlin
var hasClickedContinue by remember { mutableStateOf(false) }

LaunchedEffect(authState, hasClickedContinue) {
    if (hasClickedContinue) {
        when (val state = authState) {
            is AuthState.Authenticated -> {
                // Only navigates after user clicks Continue
                onRoleSelected(state.user.role)
            }
            is AuthState.Error -> {
                // Reset flag on error so user can retry
                hasClickedContinue = false
            }
            // ...
        }
    }
}

// In button onClick:
onClick = {
    selectedRole?.let { role ->
        hasClickedContinue = true  // Set flag before calling selectRole
        viewModel.selectRole(role)
    }
}
```

## Expected Behavior Now
1. User signs up → navigates to role selection screen
2. Role selection screen displays and waits for user input
3. User selects a role (USER, WORKER, or BOTH)
4. User clicks "Continue" button
5. App updates the role in Firestore
6. Upon successful update, navigates to appropriate dashboard

## Testing
To verify the fix:
1. Sign up with a new account
2. Verify the role selection screen stays visible
3. Select a role and click Continue
4. Verify navigation to the correct dashboard based on selected role
5. Test error handling by disconnecting network and attempting to select a role

## Status
✅ Fix implemented and compiled successfully
✅ No compilation errors
⏳ Ready for user testing
