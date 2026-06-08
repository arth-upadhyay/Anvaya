package com.example.anvaya

import android.app.PendingIntent
import android.content.Context  // <-- Here is the magic line that fixes the error!
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.RemoteInput
import android.util.Log

class AnvayaService : NotificationListenerService() {

    private val recentReplies = HashMap<String, Long>()

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val packageName = sbn?.packageName
        if (packageName == "com.whatsapp") {

            val extras = sbn.notification?.extras ?: return

            // --- THE MASTER SWITCH CHECK ---
            // Read from the bridge to see if the user turned the bot off
            val sharedPrefs = getSharedPreferences("AnvayaPrefs", Context.MODE_PRIVATE)
            val isBotActive = sharedPrefs.getBoolean("isBotActive", true)

            if (!isBotActive) {
                Log.d("AnvayaBeta", "Master switch is OFF. Ignoring message.")
                return // Kills the process entirely!
            }

            // Grab the user's custom text from the bridge
            val userCustomReply = sharedPrefs.getString("customReply", "I'm currently busy!") ?: "I'm currently busy!"

            val isGroup = extras.getBoolean("android.isGroupConversation", false)
            val title = extras.getString("android.title") ?: ""
            val isLikelyGroup = title.contains(":") || title.contains("@")

            if (isGroup || isLikelyGroup) {
                Log.d("AnvayaBeta", "Group message detected. Ignoring completely.")
                return
            }

            val currentTime = System.currentTimeMillis()
            val lastReplyTime = recentReplies[title] ?: 0L

            if (currentTime - lastReplyTime < 10000) {
                Log.d("AnvayaBeta", "Cooldown active for $title. Loop blocked.")
                return
            }

            val messageText = extras.getCharSequence("android.text").toString()
            Log.d("AnvayaBeta", "New direct message: $messageText")

            if (messageText.startsWith("You:") || messageText.startsWith("You ") || messageText.contains("You:")) {
                Log.d("AnvayaBeta", "Self-reply detected. Blocking loop.")
                return
            }

            val isSummary = (sbn.notification?.flags ?: 0) and android.app.Notification.FLAG_GROUP_SUMMARY != 0
            if (isSummary) {
                return
            }

            val customReply = when {
                messageText.contains("📷") || messageText.contains("Photo") ->
                    "I am away from my phone right now and can't load pictures! 🙈"

                messageText.contains("🎤") || messageText.contains("Voice message") ->
                    "I can't listen to audio notes right now, please type it out or I'll check it later! 🎧"

                messageText.contains("📹") || messageText.contains("Video") ->
                    "Can't watch videos right now, I'm busy! 🎬"

                else -> userCustomReply
            }

            val actions = sbn.notification?.actions
            if (actions != null) {
                for (action in actions) {
                    val remoteInputs = action.remoteInputs
                    if (remoteInputs != null) {
                        for (remoteInput in remoteInputs) {
                            val intent = Intent()
                            val bundle = Bundle()

                            bundle.putCharSequence(remoteInput.resultKey, customReply)
                            RemoteInput.addResultsToIntent(arrayOf(remoteInput), intent, bundle)

                            try {
                                action.actionIntent.send(this, 0, intent)
                                Log.d("AnvayaBeta", "SUCCESS: Smart reply sent!")

                                recentReplies[title] = currentTime

                            } catch (e: Exception) {
                                Log.e("AnvayaBeta", "ERROR: Could not send reply", e)
                            }
                        }
                    }
                }
            }
        }
    }
}