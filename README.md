# 💊 MediTrapApp – Smart Medicine Reminder Application

MediTrapApp is a comprehensive Android-based **medication reminder and tracking system** designed to help users manage their prescribed medicine schedules effectively.  
It provides timely alerts, maintains dosage history, and even notifies caregivers when doses are missed — ensuring complete accountability and improved medication adherence.

## 📖 Overview

Medication non-adherence is a major challenge, especially among elderly individuals and patients with chronic illnesses.  
**MediTrapApp** addresses this issue by combining intuitive design with robust functionality — offering smart reminders, history tracking, and cloud synchronization using Firebase.

Built with **Java**, **XML**, and **Firebase**, the app ensures reliability, even in device sleep or Doze mode, through the use of **AlarmManager**, **BroadcastReceiver**, and **full-screen notifications**.


## ✨ Key Features

- 🕒 **Smart Reminders:** Set precise alarms for each medicine using Android’s alarm clock API.  
- 💊 **Multi-Type Support:** Manage tablets, injections, liquids, creams, and inhalers.  
- 📅 **Medicine History:** Track taken, skipped, and completed doses with real-time statistics.  
- 🔔 **Persistent Notifications:** Receive full-screen reminders even when the device is locked.  
- ☁️ **Firebase Sync:** Real-time cloud database integration ensures data consistency.  
- 📍 **Nearby Pharmacies:** Locate nearby medical stores using the Google Maps API.  
- 📩 **SMS Alerts:** Automatically inform family members if a dose is skipped.  
- ⚙️ **Custom Settings:** Enable/disable ringtone alerts and configure preferences.  
- 📈 **Visual Statistics:** View progress and adherence performance over time.


## 🛠️ Tech Stack

| Component | Technology Used |
|------------|-----------------|
| **Programming Language** | Java |
| **UI Design** | XML (Material Design) |
| **Database** | Firebase Realtime Database |
| **Development Environment** | Android Studio |
| **Build System** | Gradle |
| **Version Control** | Git / GitHub |
| **APIs Used** | Google Maps API, Firebase SDK |


## ⚙️ System Requirements

| Requirement | Minimum Specification |
|--------------|------------------------|
| **Operating System** | Android 8.0 (Oreo) or higher |
| **Processor** | Quad-Core 1.8 GHz or above |
| **RAM** | 2 GB (4 GB recommended) |
| **Storage** | 200 MB free space |
| **Battery** | ≥ 3000 mAh |
| **Network** | Wi-Fi / Mobile Data (for Firebase & SMS) |


## 📱 App Modules

- **Login & Sign-Up:** User authentication and secure access.  
- **Dashboard:** Centralized access to all major features.  
- **Add Medicine:** Input medicine name, dosage, schedule, and reminder time.  
- **Medicine Schedule:** Displays current and upcoming doses.  
- **Medicine History:** Shows all, skipped, and completed medicines.  
- **SMS Status:** Logs and displays caregiver notifications.  
- **Nearby Location:** Shows nearby pharmacies using Google Maps.  
- **Settings:** Customize alert tones and reminder preferences.

---

## 🧩 Functional Flow

1. User registers or logs in.  
2. Adds medicine details (type, dosage, and timing).  
3. Sets reminder alarms for daily or custom schedules.  
4. Receives full-screen notifications at reminder times.  
5. Marks doses as “Taken” or “Skipped.”  
6. If skipped, an SMS alert is sent automatically to a registered family contact.  
7. All activity is logged and displayed in the History screen.  
8. Users can view adherence statistics and nearby pharmacy details.


## 🧠 Firebase Integration

- **Realtime Database:** Stores and syncs user, medicine, and history data.  
- **Authentication:** Ensures secure login and personalized data access.  
- **Cloud Updates:** Provides live synchronization across sessions and devices.


## 🧪 Testing & Validation

- **Tools Used:** Android Emulator and physical device testing.  
- **Validation Areas:**
  - Alarm scheduling accuracy  
  - Notification handling in Doze mode  
  - Firebase synchronization integrity  
  - SMS delivery confirmation  

## 📊 Results & Impact

MediTrapApp successfully:
- Improved medication adherence through reliable alerts.  
- Enabled caregiver accountability via SMS notifications.  
- Ensured data reliability using Firebase’s real-time synchronization.  
- Delivered a user-friendly interface suitable for all age groups.

## 🧾 Conclusion

The MediTrapApp project demonstrates how thoughtful Android development can directly improve healthcare outcomes.  
By integrating precise alarm scheduling, cloud connectivity, and caregiver alerts, it provides a **complete digital solution for medication management**.  
The app is production-ready, scalable, and adaptable for future enhancements like prescription scanning and wearable integration.

## 🔗 Resources
- 📦 **GitHub Repository:** [MediTrapApp](https://github.com/Santhoshkumar0913/MediTrapApp)  

