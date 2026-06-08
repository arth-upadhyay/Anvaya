# Anvaya 🤖

An intelligent, self-aware background auto-responder for Android. Anvaya intercepts incoming WhatsApp notifications and injects smart, context-aware custom replies directly into the OS pipeline—all without opening the app or waking the screen. 

**Note: the ui is just for the show and it will be updated in the next update.**

### 📖 The Meaning of Anvaya
**Anvaya** (अन्वय) is derived from Sanskrit, meaning *connection*, *logical sequence*, or *following through*. This app was named Anvaya because it acts as the logical continuation of your conversations, ensuring your connections remain seamless and informed even when you are actively away from your device.

## ⚡ Key Features

* **True Background Execution:** Anvaya operates completely autonomously. It works perfectly even if the phone is locked, the screen is off, or the device is in a deep idle state.
* **Smart Media Filtering:** The engine detects the type of incoming message and replies contextually:
  * 📷 Photos: *"I am away from my phone right now and can't load pictures!"*
  * 🎤 Audio Notes: *"I can't listen to audio notes right now..."*
  * 📹 Videos: *"Can't watch videos right now, I'm busy!"*
* **Anti-Spam Cooldown Engine:** Built-in short-term memory bank prevents infinite bot-loop conversations by enforcing a strict 10-second cooldown per contact.
* **Self-Awareness Shield:** Intelligently detects and ignores your own sent messages to prevent echoing.
* **Group Chat Shield:** Automatically ignores all group chat activity to prevent spamming multiple users at once.

## 🛠️ Tech Stack & Architecture

* **Language:** Kotlin
* **Core API:** `NotificationListenerService` (Silent notification eavesdropper)
* **Injection:** `RemoteInput` and `PendingIntent` (Bypasses the WhatsApp UI to send payloads directly to the Android notification pipeline)
* **Architecture:** Headless Background Service

## 🚀 Installation & Setup

Because Anvaya operates at the system level to read incoming messages, it requires specific Android permissions to function.

### 1. Download & Install
You do not need to build this app from source.
1. Go to the **Releases** section on the right side of this GitHub repository.
2. Download the latest `.apk` file (e.g., `Anvaya_v1.0.apk`) directly to your Android device.
3. Open the downloaded file to install it. *(Note: Your phone may ask you to allow "Install from Unknown Sources" since it's not from the Google Play Store. This is normal).*

### 2. Grant Notification Access (Crucial Step)
Anvaya relies on a silent notification listener to detect incoming messages. **The app will not work until this is enabled.**
1. Open your Android phone's **Settings**.
2. Search for **Notification Access** (or *Device & App Notifications*).
3. Find **Anvaya** in the list of apps.
4. Toggle the switch to **Allow notification access**.

### 3. Battery Optimization (For aggressive OS like realme UI / MIUI)
To ensure Anvaya isn't killed in the background by your phone's battery manager:
1. Long-press the Anvaya app icon and tap **App Info**.
2. Go to **Battery Usage**.
3. Enable **Allow background activity** (or set to "Unrestricted").

## 💻 For Developers (Building from Source)
If you want to modify the code: clone this repository, open the project in Android Studio, and sync the Gradle files. You can then build a custom APK or push directly to a connected device.

## ⚠️ Current Status
Version 1.0 focuses entirely on the background engine and loop-prevention logic. This only works on the WhatsApp chats for this version. The front-end interface is currently a placeholder and is not connected to the background service. Dynamic UI configuration will be introduced in V1.1.
