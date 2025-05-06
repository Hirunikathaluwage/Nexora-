package com.example.financetracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply dark theme before view loads
        setTheme(R.style.Theme_FinanceTracker)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Navigate to Login after 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
        }, 2000)
    }
}
