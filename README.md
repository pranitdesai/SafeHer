# 🛡️ SafeHer - Women Safety Application

SafeHer is a smart women safety mobile application designed to provide **instant emergency support** with **SOS alerts, live location tracking, and emergency contact notifications**.  
The app focuses on fast response, reliability, and real-world usability during emergency situations.

---

## 📌 Features

✅ **SOS Button**
- One-tap emergency SOS trigger  
- Sends alert immediately to emergency contacts  

✅ **Live Location Tracking**
- Sends live GPS location updates  
- Supports last known + cached + live location strategies for faster response  

✅ **Emergency Contact System**
- Add and manage emergency contacts
- Sends emergency message + location details

✅ **Firebase Realtime Database Integration**
- Stores SOS status, user data, and tracking updates securely
- Real-time sync between user and guardian devices

✅ **Notifications & Alert System**
- Push notification to guardian/parent when SOS is triggered
- Supports showing emergency alert dialog even when app is closed (if enabled)

✅ **Authentication**
- Firebase Authentication
- Phone OTP based login / verification

✅ **Modern UI**
- Clean and responsive UI built with Material Design
- Attractive screens with animations and smooth layouts

---

## 🧑‍💻 Tech Stack

- **Android (Java + XML)**
- **Firebase Authentication**
- **Firebase Realtime Database**
- **Firebase Cloud Messaging (FCM)**
- **Geolocator / Location Services**
- **Google Maps API**
- **Android Services / Background Execution**
- **Encryption Support (AES/RSA for secure media handling)**

---

## 📲 How SafeHer Works

1. User logs into the app and adds emergency contacts.
2. When SOS is pressed:
   - Emergency status is triggered in Firebase
   - Emergency contacts receive SMS/notification
   - User’s location starts updating in real time
3. Guardian device receives alert and gets user's live location instantly.

---

## 🛠️ Installation & Setup

### ✅ Prerequisites
- Android Studio / VS Code (Flutter)
- Firebase Project Setup
- Google Maps API Key
- `google-services.json` added to app module

### 🔥 Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/SafeHer.git
