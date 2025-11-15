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
 *
 * Behavior:
 * - Shows splash for 3000ms (3 seconds)
 * - Uses a lifecycle-safe Handler with removal in onDestroy to avoid leaks
 * - Starts MainActivity and calls finish() to prevent back navigation to splash
 */
class SplashActivity : FragmentActivity() {

    // Keep the splash visible for 3 seconds as requested.
    private val splashDelayMs: Long = 3000L

    // Use a main-thread Handler and a reusable runnable so it can be removed in onDestroy.
    private val handler = Handler(Looper.getMainLooper())
    private val navigateRunnable = Runnable {
        // Guard against multiple triggers
        if (!isFinishing && !isDestroyed) {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            // Finish so that back does not return to splash
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use a minimal layout without interaction
        setContentView(R.layout.activity_splash)

        // Post the navigation with the specified delay
        handler.postDelayed(navigateRunnable, splashDelayMs)
    }

    override fun onDestroy() {
        // Remove any pending callbacks to avoid leaks if the activity is destroyed early
        handler.removeCallbacks(navigateRunnable)
        super.onDestroy()
    }
}
