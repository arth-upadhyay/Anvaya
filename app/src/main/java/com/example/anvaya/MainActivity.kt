package com.example.anvaya

import android.app.Activity
import android.os.Bundle

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This is the magic line that tells the app to use your XML design!
        setContentView(R.layout.activity_main)
    }
}