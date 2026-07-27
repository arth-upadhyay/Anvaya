# Anvaya

A basic Android auto-reply utility for WhatsApp.

**Anvaya** (अन्वय) is a Sanskrit word meaning *connection*, *logical sequence*, or *following through*. The name was chosen because the app sits in the background and continues your conversations for you when you are away from your device -- acting as a logical extension of your replies.

---

## What it does

- Monitors incoming WhatsApp notifications using Android's NotificationListenerService
- Automatically replies with a user-defined text message
- Skips group chats using a simple title-based heuristic
- Prevents rapid-fire replies with a 10-second cooldown per contact
- Detects media types (text, photo, audio, video) and replies with different templates
- Filters out echoes of its own replies to prevent loops

---

## Requirements

- Android device with Notification Access settings
- WhatsApp or WhatsApp Business installed
- Notification Access permission granted to Anvaya (the app will block you until you do)

---

## Setup

1. Install the APK
2. Open the app -- it will immediately prompt you to grant Notification Access
3. Tap "Open Settings", find Anvaya in the list, and enable it
4. Return to the app. If the permission is granted, the UI unlocks
5. Enable the toggle, enter your reply texts, and save

Optional: Disable battery optimization so the OS doesn't kill the background service.

---

## How it works

The app registers a NotificationListenerService that fires when WhatsApp posts a notification. It inspects the notification bundle for the sender name and message text, checks against persistent cooldown storage, detects media type via heuristics, then uses RemoteInput to inject a reply directly into the notification action without opening WhatsApp.

---

## Current behavior

| Feature | Status |
|---|---|
| WhatsApp direct messages | Works |
| Custom reply text | Works |
| Media-aware replies (photo/audio/video) | Works via heuristics |
| Background operation | Works |
| Group chat filtering | Partial -- uses string matching on notification titles (:, @, () |
| Cooldown per contact | Works, persists across service restarts |
| Self-message echo filter | Works, 15-second window |
| Pop-up feedback | Works |
| Other messaging apps | Not implemented |
| Encrypted storage | Not implemented |

---

## Known limitations

- WhatsApp only. Package names are hardcoded.
- Media detection is heuristic-based. Captions on media may be treated as text replies.
- Group detection is heuristic-based. May produce false positives or negatives.
- Plaintext storage. Settings are saved in unencrypted SharedPreferences.
- No signature verification. The app trusts the notification source based on package name alone.
- The app cannot function without Notification Access and will refuse to open its main UI until granted.

---

## Permissions used

- android.permission.BIND_NOTIFICATION_LISTENER_SERVICE -- Required to read and interact with notifications

---

## Building

Open in Android Studio, sync Gradle, build APK. Minimum SDK and target SDK are defined in build.gradle.

---

## License

MIT License -- see LICENSE for details.

---

This is a utility project built with standard Android APIs. It is not affiliated with WhatsApp or Meta.
