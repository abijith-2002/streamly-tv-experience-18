package com.android.streamly

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

/**
 * PUBLIC_INTERFACE
 * Main Activity for Android TV - Streamly
 * 
 * Extends FragmentActivity for Leanback compatibility.
 * Loads HomeFragment which implements the native TV home page
 * with pixel-perfect design matching Figma specifications.
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load HomeFragment on initial creation
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }
    }
}
