package com.sportsxtreme.app.presentation.store

import com.sportsxtreme.app.R
import com.sportsxtreme.app.presentation.tournament.*
import com.sportsxtreme.app.presentation.components.*
import com.sportsxtreme.app.presentation.auth.*
import com.sportsxtreme.app.presentation.scoring.*
import com.sportsxtreme.app.presentation.match.*
import com.sportsxtreme.app.presentation.media.*
import com.sportsxtreme.app.presentation.home.*
import com.sportsxtreme.app.presentation.team.*
import com.sportsxtreme.app.presentation.profile.*
import com.sportsxtreme.app.presentation.store.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

class ShoppingActivity : ComponentActivity() {

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
