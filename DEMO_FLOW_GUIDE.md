# Kaamly Marketplace - Demo Flow Guide

## Overview
This guide outlines the complete demo flow for the Kaamly marketplace app. The entire flow should complete in under 2 minutes.

## Prerequisites
1. Firebase emulator running (optional for local testing)
2. App installed on device/emulator
3. Demo data seeded (optional - can be done during demo)

## Complete Demo Flow (< 2 minutes)

### Step 1: Authentication (15 seconds)
**Action:** Sign up or login
- Open app
- If new user: Click "Sign Up"
  - Email: `demo@kaamly.com`
  - Password: `demo123456`
  - Name: `Demo User`
  - Click "Sign Up"
- If existing: Click "Login" and use credentials
- Select role: "I want to post tasks" (USER role)

**Expected Result:** Navigate to User Dashboard

### Step 2: Create Task (20 seconds)
**Action:** Post a new task
- Click "Create Task" FAB
- Click "Use Demo Data" button (auto-fills form)
- Review pre-filled data:
  - Title: "Fix Leaking Bathroom Tap"
  - Description: Professional description
  - Category: PLUMBING
  - Equipment: Wrench, Plumber's tape
  - Location: Mumbai, Andheri West
  - Date: Tomorrow
  - Time: 10:00 AM
  - Budget: ₹500
- Click "Create Task"

**Expected Result:** Task created, navigate back to dashboard showing new task

### Step 3: Switch to Worker Role (10 seconds)
**Action:** Switch roles to view tasks as worker
- Click role switcher FAB (bottom right, secondary FAB)
- Or click role indicator chip in top bar
- Select "Worker" role

**Expected Result:** Navigate to Worker Dashboard showing available tasks

### Step 4: View and Bid on Task (25 seconds)
**Action:** Find and bid on the created task
- View task feed (should show the task created in Step 2)
- Click on the task card
- Review task details
- Click "Place Bid" button
- Enter bid details:
  - Amount: `450` (or use demo data)
  - Message: "I can fix this today! Experienced plumber."
- Click "Submit Bid"

**Expected Result:** Bid submitted successfully, navigate back to task feed

### Step 5: Switch Back to User Role (10 seconds)
**Action:** Switch back to user to accept bid
- Click role switcher FAB
- Select "User" role

**Expected Result:** Navigate to User Dashboard

### Step 6: Accept Bid and Complete Task (30 seconds)
**Action:** Accept worker's bid and complete task flow
- Click on the task with bids
- Click "View Bids" button
- Review bid from worker
- Click worker profile to view ratings (optional)
- Click "Accept Bid" button
- Confirm payment screen (mock):
  - Bid amount: ₹450
  - Platform fee (10%): ₹45
  - Total: ₹495
- Click "Confirm Payment"
- **Mock escrow locked message appears**

**Expected Result:** Task state changes to "In Progress", worker assigned

### Step 7: Worker Completes Task (20 seconds)
**Action:** Simulate worker completing the task
- Switch to Worker role (FAB)
- Navigate to "Active Tasks"
- Click on the assigned task
- Click "Mark Arrived" button
- Click "Upload Photo" button (select any image or skip)
- Click "Mark Completed" button

**Expected Result:** Completion request sent to user

### Step 8: User Approves and Reviews (20 seconds)
**Action:** Approve completion and leave review
- Switch back to User role (FAB)
- Navigate to task or click notification
- View completion photo (if uploaded)
- Click "Approve Task" button
- **Mock escrow released message appears**
- Review form appears:
  - Star rating: 5 stars
  - Text review: "Excellent work! Fixed quickly."
  - Task-specific ratings:
    - Punctuality: 5
    - Quality: 5
    - Communication: 5
- Click "Submit Review"

**Expected Result:** Task marked as completed, review submitted, worker rating updated

## Total Time: ~2 minutes

## Key Features Demonstrated

### Core Functionality
- ✅ User authentication (signup/login)
- ✅ Role selection and switching
- ✅ Task creation with AI assistance option
- ✅ Location-based task discovery
- ✅ Bidding system
- ✅ Worker profile with ratings
- ✅ Mock payment and escrow
- ✅ Task execution flow
- ✅ Photo upload for completion proof
- ✅ Review and rating system

### UI/UX Features
- ✅ Material Design 3 theming
- ✅ Smooth animations and transitions
- ✅ Real-time updates (Firestore listeners)
- ✅ Role switcher FAB
- ✅ Role indicator chip
- ✅ Empty states
- ✅ Loading states
- ✅ Error handling
- ✅ Form validation

### Demo Mode Features
- ✅ "Use Demo Data" buttons
- ✅ Quick role switching
- ✅ Demo data seeding
- ✅ Current role indicator

## Troubleshooting

### Issue: Task not appearing in worker feed
**Solution:** 
- Ensure task location matches worker's city
- Check task state is "Open"
- Pull to refresh the task feed

### Issue: Cannot accept bid
**Solution:**
- Ensure task is still in "Open" state
- Check that bid exists and is in "Pending" status
- Verify user is the task owner

### Issue: Real-time updates not working
**Solution:**
- Check Firebase connection
- Ensure Firestore listeners are active
- Restart app if needed

### Issue: Role switch not working
**Solution:**
- Verify user has permission to switch roles
- Check AuthViewModel state
- Ensure navigation is properly configured

## Demo Tips

1. **Pre-seed demo data** before the demo for backup scenarios
2. **Use "Use Demo Data" buttons** to save time on form filling
3. **Keep the flow moving** - don't spend too much time on any single screen
4. **Highlight key features** as you go through the flow
5. **Show real-time updates** by having two devices/emulators side by side (optional)
6. **Emphasize the mock systems** (AI, payments, verification) to show understanding of scope
7. **Have a backup plan** - if something fails, have demo data ready to show

## Alternative Quick Demo (30 seconds)

If time is very limited, demonstrate:
1. Login with existing demo account
2. Show User Dashboard with existing tasks
3. Switch to Worker role
4. Show Worker Dashboard with task feed
5. Click on a task to show details
6. Show bidding interface
7. Switch back to User
8. Show bid list and worker profiles

## Post-Demo Discussion Points

- **Scalability:** How the architecture supports growth
- **Real implementations:** What would be needed for production (real AI API, payment gateway, Aadhaar verification)
- **Additional features:** Chat system, push notifications, advanced search, worker verification levels
- **Business model:** Platform fee structure, subscription tiers
- **Security:** Data encryption, secure payments, identity verification

## Demo Data Reference

### Demo Users
- **User 1:** Priya Sharma (Mumbai)
- **User 2:** Rahul Verma (Bangalore)

### Demo Workers
- **Worker 1:** Amit Kumar (Mumbai) - Plumbing, Electrical, Repair - 4.8★
- **Worker 2:** Sunita Devi (Mumbai) - Cleaning, Cooking, Gardening - 4.9★
- **Worker 3:** Rajesh Singh (Bangalore) - Delivery, Moving, Assembly - 4.6★

### Demo Tasks
- Fix Leaking Kitchen Tap (Mumbai, ₹500)
- Deep Clean 2BHK Apartment (Mumbai, ₹1500)
- Deliver Package Across City (Bangalore, ₹300, Instant)
- Install Ceiling Fan (Bangalore, ₹800)
- Assemble IKEA Furniture (Mumbai, ₹1000)

## Success Criteria

The demo is successful if:
- ✅ Complete flow executes without crashes
- ✅ All state transitions work correctly
- ✅ Real-time updates are visible
- ✅ UI is responsive and polished
- ✅ Mock systems are clearly indicated
- ✅ Demo completes in under 2 minutes
- ✅ Judges understand the product value proposition
