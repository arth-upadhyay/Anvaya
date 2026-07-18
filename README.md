###Anvaya
Anvaya (अन्वय) — Sanskrit for connection or logical sequence. A simple WhatsApp auto-reply bot for Android.
**What It Actually Does**
Listens to incoming WhatsApp notifications via Android's NotificationListenerService
Sends a single custom text reply automatically
Ignores group chats (basic heuristic: checks if the sender title contains :, @, or ()
Enforces a 10-second cooldown per contact to avoid reply loops
Stores your custom reply text and on/off state in SharedPreferences
That's it. No AI. No context awareness. No media detection.
**How It Works**
You enable the master switch and type a reply message in the app
You grant Notification Access in Android settings
When a WhatsApp DM comes in, the service grabs the notification action, fills in your saved text via RemoteInput, and fires the reply intent
The app never opens WhatsApp's UI — it injects the reply directly into the notification pipeline
**Setup**
Install the APK
Go to Settings → Apps → Special Access → Notification Access and enable Anvaya
Open Anvaya, turn on the switch, set your message, hit Save
(Optional) Disable battery optimization for the app so Android doesn't kill the background service
Limitations
Table
**Limitations**
WhatsApp only	Hardcoded package name check (com.whatsapp, com.whatsapp.w4b)
Text replies only	No detection of images, voice notes, or videos — it replies the same text to everything
No self-awareness	If WhatsApp echoes your own outgoing messages as notifications, the bot may reply to itself
Group detection is fragile	Relies on title string heuristics that can misfire
Cooldown is in-memory only	If the service restarts, the cooldown map resets
No encryption	Your reply text is stored in plain SharedPreferences
Requires Notification Access	Android will warn you that the app can read all notifications — because it does
**Tech Stack**
Kotlin
NotificationListenerService + RemoteInput
XML layouts with Material Design components
PopupWindow for toast-like feedback
Future Ideas (Not Implemented)
Detect media types and reply accordingly
Filter out your own sent messages
Support Telegram, Instagram DMs, SMS
Persistent cooldown storage
Encrypt saved preferences
### **Disclaimer**
This is a personal utility project. It uses Android accessibility-level permissions to interact with another app's notifications. Use at your own risk. This is not affiliated with WhatsApp or Meta
