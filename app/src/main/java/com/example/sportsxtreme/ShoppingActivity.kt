package com.example.sportsxtreme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

class ShoppingActivity : AppCompatActivity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        setContentView(
            XtremeSectionView(
                context = this,
                title = getString(R.string.str_shopping),
                bodyText = getString(R.string.str_shopping),
                selectedMode = XtremeSectionView.SectionMode.CART,
                useCartLogo = true
            )
        )
    }
}
