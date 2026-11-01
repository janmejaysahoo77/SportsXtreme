package com.sportsxtreme.app.presentation.clubs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.sportsxtreme.app.presentation.ui.theme.*

class MatchesPlayedByClubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { 
            MatchesScreen(onBack = { finish() })
        }
    }
}
