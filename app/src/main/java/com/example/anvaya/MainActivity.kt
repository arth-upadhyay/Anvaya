package com.example.anvaya

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val switch = findViewById<Switch>(R.id.masterSwitch)
        val input = findViewById<EditText>(R.id.replyInput)
        val saveBtn = findViewById<Button>(R.id.saveButton)

        switch.setOnCheckedChangeListener { _, isChecked ->
            val msg = if (isChecked) "Auto-reply ON" else "Auto-reply OFF"
            showPopup(switch, msg)
        }

        saveBtn.setOnClickListener {
            val text = input.text.toString()
            if (text.isBlank()) {
                showPopup(saveBtn, "Please enter a reply!")
            } else {
                showPopup(saveBtn, "Settings saved!")
            }
        }
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

            Handler(Looper.getMainLooper()).postDelayed({
                popup.dismiss()
            }, 1500)

        } catch (e: Exception) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}