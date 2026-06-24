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

    private val recentReplies = HashMap<String, Long>()
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        sharedPrefs = getSharedPreferences("AnvayaPrefs", Context.MODE_PRIVATE)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName


        if (packageName != "com.whatsapp" && packageName != "com.whatsapp.w4b") {
            return
        }


        if (!sharedPrefs.getBoolean("isBotActive", false)) {
            return
        }

        val notification = sbn.notification ?: return


        if ((notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) {
            Log.d("AnvayaBeta", "Summary notification ignored.")
            return
        }

        val extras = notification.extras ?: return

        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val isGroup = title.contains(":") || title.contains("@") || title.contains("(")

        if (isGroup) {
            Log.d("AnvayaBeta", "Group message detected. Ignoring.")
            return
        }

        val messageText = extras.getCharSequence("android.text")?.toString() ?: ""
        if (messageText.isBlank()) {
            Log.d("AnvayaBeta", "Empty message. Ignoring.")
            return
        }

        val currentTime = System.currentTimeMillis()
        recentReplies.entries.removeAll { currentTime - it.value > 10000 }

        val lastReplyTime = recentReplies[title] ?: 0L
        if (currentTime - lastReplyTime < 10000) {
            Log.d("AnvayaBeta", "Cooldown active for $title")
            return
        }

        Log.d("AnvayaBeta", "New message from $title: $messageText")

        val customReply = sharedPrefs.getString("custom_reply", "I'm currently busy. I'll get back to you soon!") ?: "I'm currently busy."

        val actions = notification.actions
        if (actions == null || actions.isEmpty()) {
            Log.e("AnvayaBeta", "No actions found")
            return
        }

        for (action in actions) {
            val remoteInputs = action.remoteInputs
            if (remoteInputs != null && remoteInputs.isNotEmpty()) {

                val intent = Intent().addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                val bundle = Bundle()

                bundle.putCharSequence(remoteInputs[0].resultKey, customReply)
                RemoteInput.addResultsToIntent(remoteInputs, intent, bundle)

                try {
                    action.actionIntent.send(this, 0, intent)
                    Log.d("AnvayaBeta", "Reply sent to $title")
                    recentReplies[title] = currentTime
                    return
                } catch (e: Exception) {
                    Log.e("AnvayaBeta", "Failed to send reply", e)
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {

    }
}