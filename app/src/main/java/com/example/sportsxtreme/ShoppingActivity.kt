package com.example.sportsxtreme

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity

class ShoppingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySportsXtremeWindowStyle()
        setContent {
            XtremeSectionScreen(
                title = getString(R.string.str_shopping),
                bodyText = getString(R.string.str_shopping),
                selectedMode = XtremeSectionMode.CART,
                useCartLogo = true,
                actions = XtremeSectionActions(
                    openSports = { openSportsXtreme() },
                    openMedia = { openXtremeMedia() }
                )
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun openSportsXtreme() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        })
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }

    @Suppress("DEPRECATION")
    private fun openXtremeMedia() {
        startActivity(Intent(this, XtremeMediaActivity::class.java))
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }
}
