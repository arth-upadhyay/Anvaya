package com.example.anvaya
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: android.content.SharedPreferences
    private var permissionDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("AnvayaPrefs", Context.MODE_PRIVATE)

        val switch = findViewById<Switch>(R.id.masterSwitch)
        val defaultInput = findViewById<EditText>(R.id.replyInput)
        val photoInput = findViewById<EditText>(R.id.replyPhotoInput)
        val audioInput = findViewById<EditText>(R.id.replyAudioInput)
        val videoInput = findViewById<EditText>(R.id.replyVideoInput)
        val saveBtn = findViewById<Button>(R.id.saveButton)

        // Load saved states
        switch.isChecked = prefs.getBoolean("isBotActive", false)
        defaultInput.setText(prefs.getString("custom_reply", "I'm currently busy. I'll get back to you soon!"))
        photoInput.setText(prefs.getString("reply_photo", "I'm away from my phone right now and can't load pictures!"))
        audioInput.setText(prefs.getString("reply_audio", "I can't listen to audio notes right now, I'll check later."))
        videoInput.setText(prefs.getString("reply_video", "Can't watch videos right now, I'm busy!"))

        switch.setOnCheckedChangeListener { _, isChecked ->
            if (!isNotificationAccessGranted()) {
                switch.isChecked = false
                showPermissionGate()
                return@setOnCheckedChangeListener
            }
            prefs.edit().putBoolean("isBotActive", isChecked).apply()
            val msg = if (isChecked) "Auto-reply ON" else "Auto-reply OFF"
            showPopup(switch, msg)
        }

        saveBtn.setOnClickListener {
            if (!isNotificationAccessGranted()) {
                showPermissionGate()
                return@setOnClickListener
            }

            val defaultText = defaultInput.text.toString()
            val photoText = photoInput.text.toString()
            val audioText = audioInput.text.toString()
            val videoText = videoInput.text.toString()

            if (defaultText.isBlank()) {
                showPopup(saveBtn, "Default reply cannot be empty!")
                return@setOnClickListener
            }

            prefs.edit()
                .putString("custom_reply", defaultText)
                .putString("reply_photo", photoText.ifBlank { null } ?: "I'm away from my phone right now and can't load pictures!")
                .putString("reply_audio", audioText.ifBlank { null } ?: "I can't listen to audio notes right now, I'll check later.")
                .putString("reply_video", videoText.ifBlank { null } ?: "Can't watch videos right now, I'm busy!")
                .apply()

            showPopup(saveBtn, "Settings saved!")
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isNotificationAccessGranted()) {
            showPermissionGate()
        } else {
            permissionDialog?.dismiss()
            permissionDialog = null
        }
    }

    private fun isNotificationAccessGranted(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (flat.isNullOrBlank()) return false
        val component = ComponentName(packageName, packageName + ".AnvayaService")
        return flat.contains(component.flattenToString()) || flat.contains(component.flattenToShortString())
    }

    private fun showPermissionGate() {
        if (permissionDialog?.isShowing == true) return

        permissionDialog = AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Anvaya needs Notification Access to read and reply to WhatsApp messages. The app cannot function without this permission.")
            .setCancelable(false)
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Exit App") { _, _ ->
                finish()
            }
            .show()
    }

    private fun showPopup(anchor: android.view.View, message: String) {
        try {
            val popupView = LayoutInflater.from(this).inflate(R.layout.popup_tooltip, null)
            popupView.findViewById<TextView>(R.id.popupText).text = message

            val popup = PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
            )
            popup.isOutsideTouchable = true
            popup.showAsDropDown(anchor, 0, -20, Gravity.CENTER)

            Handler(Looper.getMainLooper()).postDelayed({ popup.dismiss() }, 1500)

        } catch (e: Exception) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}