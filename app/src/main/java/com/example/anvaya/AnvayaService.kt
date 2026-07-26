package com.example.anvaya
import android.app.Notification
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class AnvayaService : NotificationListenerService() {

    private lateinit var prefs: SharedPreferences

    companion object {
        private const val PREFS_NAME = "AnvayaPrefs"
        private const val COOLDOWN_PREFIX = "cd_"
        private const val SELF_REPLY_PREFIX = "sr_"
        private const val COOLDOWN_MS = 10_000L
        private const val SELF_REPLY_TTL_MS = 15_000L
    }

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName

        if (packageName != "com.whatsapp" && packageName != "com.whatsapp.w4b") return

        if (!prefs.getBoolean("isBotActive", false)) return

        val notification = sbn.notification ?: return

        if ((notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) {
            Log.d("Anvaya", "Summary notification ignored")
            return
        }

        val extras = notification.extras ?: return
        val title = extras.getCharSequence("android.title")?.toString() ?: ""

        val isGroup = title.contains(":") || title.contains("@") || title.contains("(")
        if (isGroup) {
            Log.d("Anvaya", "Group message ignored: $title")
            return
        }

        val messageText = extras.getCharSequence("android.text")?.toString() ?: ""

        if (isOwnReplyEcho(title, messageText)) {
            Log.d("Anvaya", "Self-message echo ignored: $title")
            return
        }

        if (messageText.isBlank() && !hasMediaAttachment(extras)) {
            Log.d("Anvaya", "Empty non-media message ignored")
            return
        }

        if (isOnCooldown(title)) {
            Log.d("Anvaya", "Cooldown active for: $title")
            return
        }

        val mediaType = detectMediaType(extras, messageText)
        val replyText = getReplyForMedia(mediaType)

        if (replyText.isBlank()) {
            Log.d("Anvaya", "No reply configured for $mediaType")
            return
        }

        Log.d("Anvaya", "Replying to $title ($mediaType): $replyText")

        sendReply(notification, title, replyText)
    }

    private fun detectMediaType(extras: Bundle, messageText: String): MediaType {
        if (messageText.isBlank() && hasMediaAttachment(extras)) {
            return MediaType.PHOTO
        }

        val lower = messageText.lowercase()

        return when {
            lower.contains("photo") || lower.contains("image") || lower.contains("picture") -> MediaType.PHOTO
            lower.contains("voice") || lower.contains("audio note") || lower.contains("sound") -> MediaType.AUDIO
            lower.contains("video") || lower.contains("clip") || lower.contains("gif") -> MediaType.VIDEO
            messageText.isBlank() -> MediaType.PHOTO
            else -> MediaType.TEXT
        }
    }

    private fun hasMediaAttachment(extras: Bundle): Boolean {
        return extras.containsKey(Notification.EXTRA_PICTURE) ||
                extras.containsKey(Notification.EXTRA_LARGE_ICON) ||
                extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.isBlank() == true
    }

    private fun getReplyForMedia(type: MediaType): String {
        return when (type) {
            MediaType.TEXT -> prefs.getString("custom_reply", "I'm currently busy. I'll get back to you soon!") ?: ""
            MediaType.PHOTO -> prefs.getString("reply_photo", "I'm away from my phone right now and can't load pictures!") ?: ""
            MediaType.AUDIO -> prefs.getString("reply_audio", "I can't listen to audio notes right now, I'll check later.") ?: ""
            MediaType.VIDEO -> prefs.getString("reply_video", "Can't watch videos right now, I'm busy!") ?: ""
        }
    }

    private fun isOwnReplyEcho(contact: String, messageText: String): Boolean {
        val now = System.currentTimeMillis()
        val key = SELF_REPLY_PREFIX + contact.hashCode()
        val stored = prefs.getString(key, null) ?: return false

        val parts = stored.split("|", limit = 2)
        if (parts.size != 2) {
            prefs.edit().remove(key).apply()
            return false
        }

        val timestamp = parts[0].toLongOrNull() ?: return false
        val sentText = parts[1]

        if (now - timestamp > SELF_REPLY_TTL_MS) {
            prefs.edit().remove(key).apply()
            return false
        }

        return sentText == messageText
    }

    private fun isOnCooldown(contact: String): Boolean {
        val now = System.currentTimeMillis()
        val key = COOLDOWN_PREFIX + contact.hashCode()
        val lastReply = prefs.getLong(key, 0L)

        if (lastReply > 0 && now - lastReply > COOLDOWN_MS) {
            prefs.edit().remove(key).apply()
            return false
        }

        return now - lastReply < COOLDOWN_MS
    }

    private fun sendReply(notification: Notification, contact: String, replyText: String) {
        val actions = notification.actions
        if (actions.isNullOrEmpty()) {
            Log.e("Anvaya", "No actions found in notification")
            return
        }

        val now = System.currentTimeMillis()

        for (action in actions) {
            val remoteInputs = action.remoteInputs
            if (!remoteInputs.isNullOrEmpty()) {
                val intent = Intent().addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                val bundle = Bundle()
                bundle.putCharSequence(remoteInputs[0].resultKey, replyText)
                RemoteInput.addResultsToIntent(remoteInputs, intent, bundle)

                try {
                    action.actionIntent.send(this, 0, intent)

                    prefs.edit().putLong(COOLDOWN_PREFIX + contact.hashCode(), now).apply()
                    prefs.edit().putString(
                        SELF_REPLY_PREFIX + contact.hashCode(),
                        "$now|$replyText"
                    ).apply()

                    Log.d("Anvaya", "Reply sent to $contact")
                    return
                } catch (e: Exception) {
                    Log.e("Anvaya", "Failed to send reply to $contact", e)
                }
            }
        }
    }

    enum class MediaType { TEXT, PHOTO, AUDIO, VIDEO }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {}
}