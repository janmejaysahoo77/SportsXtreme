package com.example.sportsxtreme

import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

@Suppress("DEPRECATION")
fun ComponentActivity.applySportsXtremeWindowStyle() {
    WindowCompat.setDecorFitsSystemWindows(window, true)
    window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
    window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
}
