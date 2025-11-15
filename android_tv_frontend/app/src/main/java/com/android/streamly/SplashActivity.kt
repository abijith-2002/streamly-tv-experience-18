package com.android.streamly

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentActivity

/**
 * PUBLIC_INTERFACE
 * SplashActivity
 * This activity serves as a lightweight, themed splash screen for Android TV.
 * It displays a dark background consistent with the app theme and a centered app title text only.
 * It has no user interaction and transitions to MainActivity after a short delay.
 */
class SplashActivity : FragmentActivity() {

    // Small delay to let the splash be visible. Keep short for TV responsiveness.
    private val splashDelayMs: Long = 600L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use a minimal layout without interaction
        setContentView(R.layout.activity_splash)

        // Transition to MainActivity after a small delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            // Finish so that back does not return to splash
            finish()
        }, splashDelayMs)
    }
}
