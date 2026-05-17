# tap tap lock 🔒

A lightweight, modern Android utility that allows you to lock your screen with a simple tap on your home screen, while keeping biometric unlock (Fingerprint/Face Unlock) fully functional.

## ✨ Key Features
- **Soft Lock:** Mimics the physical power button using Accessibility Services, ensuring fingerprints still work.
- **Multiple Widget Designs:** Choose from **Classic**, **Minimal**, or **Text** styles to match your home screen.
- **Tap Preferences:** Toggle between **Single Tap** or **Double Tap** to prevent accidental locks.
- **Modern Dashboard:** A clean Material 3 interface to manage all your settings.
- **Full Customization:** 
  - **Color Palette:** Choose from 24 vibrant colors to theme the entire app.
  - **Display Modes:** Support for Light, Dark, and System-follow modes.
- **Banking App Compatible:** Easy one-tap toggle to disable the service when using sensitive banking applications.

---

## 🚀 Setup Guide

1. **Install the App:** Open the app on your device.
2. **Enable Permissions:** 
   - Tap **"ENABLE POWER KEY"** on the dashboard.
   - Find **tap tap lock** in the Accessibility settings list.
   - Toggle it **ON** and select "Allow".
3. **Add the Widget:**
   - Go to your phone's Home Screen.
   - Long-press and select **Widgets**.
   - Find **tap tap lock** and choose your favorite design (Classic, Minimal, or Text).
   - Drag it to your home screen.

---

## 🏦 Banking Apps & Security
Some banking and high-security apps may block your phone if an Accessibility Service is active. 

**To use your banking app:**
1. Open the **tap tap lock** dashboard.
2. Tap the red **"DISABLE FOR BANKING"** button.
3. Toggle the service **OFF** in system settings.
4. Open your banking app.
5. Once finished, return to the dashboard and tap **"ENABLE POWER KEY"** to resume using your soft lock widget.

---

## 🎨 Personalization
Tap the **Floating Palette Button** (bottom right) to open the Theme Settings:
- **Display Mode:** Switch between Light, Dark, or System modes.
- **App Color:** Pick a color from the 24-color grid to instantly re-theme the app's interface.

---

## 🛠 Technical Note
This app uses the **AccessibilityService API** specifically to perform the `GLOBAL_ACTION_LOCK_SCREEN` action. 
- **Privacy:** We do not collect, store, or transmit any user data. The service is "blind" and does not observe your screen content.
- **Why not Device Admin?** Device Administrator mode forces a "Hard Lock," which disables fingerprints until you enter your PIN. This app uses Accessibility to provide a "Soft Lock" that keeps your biometrics active.

---

## 📱 System Requirements
- Android 9.0 (API 28) or higher.
- Support for Home Screen Widgets.
- Biometric hardware (Optional, for fingerprint/face unlock features).
