# Anvaya

A basic Android auto-reply utility for WhatsApp. Named after the Sanskrit word for connection.

---

## What it does

- Monitors incoming WhatsApp notifications using Android's NotificationListenerService
- Automatically replies with a user-defined text message
- Skips group chats using a simple title-based heuristic
- Prevents rapid-fire replies with a 10-second cooldown per contact

---

## Requirements

- Android device with Notification Access settings
- WhatsApp or WhatsApp Business installed

---

## Setup

1. Install the APK
2. Grant Notification Access to Anvaya in system settings
3. Open the app, enable the toggle, enter your reply text, and save

Optional: Disable battery optimization so the OS doesn't kill the background service.

---

## How it works

The app registers a NotificationListenerService that fires when WhatsApp posts a notification. It inspects the notification bundle for the sender name and message text, checks against an in-memory cooldown map, then uses RemoteInput to inject a reply directly into the notification action without opening WhatsApp.

---

## Current behavior

| Feature | Status |
|---|---|
| WhatsApp direct messages | Works |
| Custom reply text | Works |
| Background operation | Works |
| Group chat filtering | Partial -- uses string matching on notification titles (:, @, () |
| Cooldown per contact | Works, but resets if the service is killed |
| Pop-up feedback | Works |
| Media type detection | Not implemented |
| Self-message filtering | Not implemented |
| Other messaging apps | Not implemented |
| Encrypted storage | Not implemented |

---

## Known limitations

- WhatsApp only. Package names are hardcoded.
- No media awareness. Replies with the same text whether the incoming message is text, photo, video, or voice note.
- Cooldown is volatile. Stored in a HashMap that clears when the service restarts.
- Group detection is heuristic-based. May produce false positives or negatives depending on how WhatsApp formats the notification title.
- Plaintext storage. Settings are saved in unencrypted SharedPreferences.
- No signature verification. The app trusts the notification source based on package name alone.

---

## Permissions used

- android.permission.BIND_NOTIFICATION_LISTENER_SERVICE -- Required to read and interact with notifications

---

## Building

Open in Android Studio, sync Gradle, build APK. Minimum SDK and target SDK are defined in build.gradle.

---

## License

[MIT License --  see [LICENSE](LISENSE) FOR DETAILS.

---

This is a utility project built with standard Android APIs. It is not affiliated with WhatsApp or Meta.
