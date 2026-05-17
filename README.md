# tap tap lock 🔒

A professional, lightweight Android utility designed to give you a "Soft Power Key" right on your home screen. Lock your device instantly while keeping **Fingerprint and Face Unlock** fully functional.

## ✨ Key Features
- **Biometric-Friendly Soft Lock:** Uses Accessibility Services to mimic a physical power button press, ensuring you aren't forced to enter your PIN on every unlock.
- **7+ Responsive Widget Designs:**
  - **Classic & Minimal:** Modern circular buttons.
  - **Textual:** Bold "TAP" typography.
  - **M3 Tile:** Professional Material 3 card style.
  - **Nothing Style:** Signature monochrome aesthetic with the iconic red dot.
  - **Dual Action:** Combined "Lock & Disable" widgets in both Horizontal and Vertical layouts.
- **Dynamic Status Colors:** Service status buttons on widgets turn **Green** when active and **Red** when disabled for instant visual confirmation.
- **Tap Customization:** Choose between **Single Tap** or **Double Tap** triggers to suit your usage style.
- **Modern M3 Dashboard:** A beautiful Jetpack Compose interface to manage settings and themes.
- **Deep Personalization:**
  - **Dynamic Theme Engine:** A 24-color palette that re-themes the entire app and all widgets.
  - **Display Modes:** Toggle between Light, Dark, and System-follow modes.
- **Banking Mode Optimized:** Programmatic one-tap disabling to quickly satisfy banking app security requirements.

---

## 🚀 Setup Guide

1. **Install & Open:** Launch **tap tap lock** on your device.
2. **Enable Soft Power Key:** 
   - Tap **"ENABLE POWER KEY"** on the dashboard.
   - You will be taken directly to the Accessibility settings.
   - Find **tap tap lock** (usually under Installed Apps/Services) and toggle it **ON**.
3. **Choose Your Style:**
   - Long-press your home screen and select **Widgets**.
   - Find **tap tap lock** to see live previews of all designs.
   - Drag your preferred widget(s) to your home screen.
4. **Configure:** Use the app dashboard to set your tap preference (Single/Double) and pick your favorite theme color.

---

## 🏦 Banking Apps & Security
Banking apps often block devices with active Accessibility Services. **tap tap lock** makes this easy to manage:

- **One-Tap Disable:** Tap the red button in the app dashboard to instantly kill the service.
- **Safety Widget:** Add the standalone "Red Cross" widget or use the dual widget. Tapping the cross button instantly disables the service from your home screen.
- **Status Check:** If your widget buttons are **Red**, the service is off and you are safe to open banking apps. If they are **Green**, the service is active.

---

## 🛠 Technical Notes
- **Privacy First:** This app has **no internet permission**. We cannot and do not collect or transmit any data.
- **Accessibility API:** Used strictly for the `GLOBAL_ACTION_LOCK_SCREEN` command. The service does not read your screen or track any input.
- **Shape Logic:** Circular widgets are engineered to stay **perfectly round** regardless of how you resize them or your launcher's grid size.

---

## 📱 System Requirements
- **Android 12.0 (API 31)** or higher.
- Support for Home Screen Widgets.
- Biometric hardware (Optional, for fingerprint/face unlock features).
