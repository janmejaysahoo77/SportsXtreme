package com.example.sportsxtreme.presentation.clubs

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.presentation.ui.theme.*

class ClubMemberPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // System bars handled by MembersScreen and theme
        setContent { 
            MembersScreen(
                onBack = { finish() },
                onAddMember = {
                    startActivity(Intent(this, AddMemberInClubActivity::class.java))
                }
            )
        }
    }
}
