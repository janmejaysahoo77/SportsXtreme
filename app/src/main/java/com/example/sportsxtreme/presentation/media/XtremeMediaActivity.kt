package com.example.sportsxtreme.presentation.media

import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.tournament.*
import com.example.sportsxtreme.presentation.components.*
import com.example.sportsxtreme.presentation.auth.*
import com.example.sportsxtreme.presentation.scoring.*
import com.example.sportsxtreme.presentation.match.*
import com.example.sportsxtreme.presentation.media.*
import com.example.sportsxtreme.presentation.home.*
import com.example.sportsxtreme.presentation.team.*
import com.example.sportsxtreme.presentation.profile.*
import com.example.sportsxtreme.presentation.store.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

class XtremeMediaActivity : ComponentActivity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        setContentView(
            XtremeSectionView(
                context = this,
                title = getString(R.string.str_xtrememedia),
                selectedMode = XtremeSectionView.SectionMode.MEDIA,
                logoRes = R.drawable.xtrememediaicon,
                bodyText = getString(R.string.str_xtrememedia),
                bodyContent = { context ->
                    ComposeView(context).apply {
                        setContent {
                            XtremeMediaScreen()
                        }
                    }
                }
            )
        )
    }
}
