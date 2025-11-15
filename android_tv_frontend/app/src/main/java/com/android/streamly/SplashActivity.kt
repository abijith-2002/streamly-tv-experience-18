package com.android.streamly

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentActivity
import coil.Coil
import coil.request.CachePolicy
import coil.request.ImageRequest

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
 * - Preloads the home hero image into Coil caches for smoother first render
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

        // Warm up image caches for Home hero (memory + disk where applicable).
        // Using resource hero_main; Coil will at least warm memory cache for instant draw.
        try {
            val loader = Coil.imageLoader(this)
            val request = ImageRequest.Builder(this)
                .data(R.drawable.hero_main)
                .crossfade(false)
                .allowHardware(true)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                // ORIGINAL size is fine for resource prefetch; decode cost is minimal for local resource
                .build()
            loader.enqueue(request)
        } catch (_: Throwable) {
            // Absorb any initialization error; splash should not crash the app
        }

        // Post the navigation with the specified delay
        handler.postDelayed(navigateRunnable, splashDelayMs)
    }

    override fun onDestroy() {
        // Remove any pending callbacks to avoid leaks if the activity is destroyed early
        handler.removeCallbacks(navigateRunnable)
        super.onDestroy()
    }
}
