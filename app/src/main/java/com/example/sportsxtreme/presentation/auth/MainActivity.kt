package com.example.sportsxtreme.presentation.auth

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
import android.content.Intent
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {

    private enum class Screen {
        Splash,
        Onboarding,
        Signup,
        Login,
        SportSelection,
        Home
    }

    private var isCustomSplashReady = false
    private var homeScreenView: HomeScreenView? = null
    private var currentScreen by mutableStateOf(Screen.Splash)

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        // Keep the system splash on screen until our custom splash view has drawn
        splashScreen.setKeepOnScreenCondition { !isCustomSplashReady }

        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        setContent {
            SportsXtremeApp()
        }
    }

    @Composable
    private fun SportsXtremeApp() {
        when (currentScreen) {
            Screen.Splash -> AndroidView(
                factory = { context ->
                    homeScreenView = null
                    SportsSplashView(context).apply {
                        viewTreeObserver.addOnPreDrawListener(
                            object : ViewTreeObserver.OnPreDrawListener {
                                override fun onPreDraw(): Boolean {
                                    isCustomSplashReady = true
                                    viewTreeObserver.removeOnPreDrawListener(this)
                                    return true
                                }
                            }
                        )

                        postDelayed({
                            showMainScreen()
                        }, 2800L)
                    }
                }
            )

            Screen.Onboarding -> AndroidView(
                factory = { context ->
                    homeScreenView = null
                    OnboardingScreenView(context)
                }
            )

            Screen.Signup -> AndroidView(
                factory = { context ->
                    homeScreenView = null
                    SignupScreenView(context)
                }
            )

            Screen.Login -> AndroidView(
                factory = { context ->
                    homeScreenView = null
                    LoginScreenView(context)
                }
            )

            Screen.SportSelection -> AndroidView(
                factory = { context ->
                    homeScreenView = null
                    SportSelectionView(context)
                }
            )

            Screen.Home -> AndroidView(
                factory = { context ->
                    HomeScreenView(context).also {
                        homeScreenView = it
                    }
                }
            )

        }
    }

    private fun showMainScreen() {
        homeScreenView = null
        currentScreen = Screen.Onboarding
    }

    fun showSignupScreen() {
        homeScreenView = null
        currentScreen = Screen.Signup
    }

    fun showLoginScreen() {
        homeScreenView = null
        currentScreen = Screen.Login
    }

    fun showSportSelectionScreen() {
        homeScreenView = null
        currentScreen = Screen.SportSelection
    }

    fun showHomeScreen() {
        homeScreenView = null
        currentScreen = Screen.Home
    }

    fun showXtremeMediaScreen() {
        startActivity(Intent(this, XtremeMediaActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    fun showXtremeCartScreen() {
        startActivity(Intent(this, ShoppingActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun onResume() {
        super.onResume()
        homeScreenView?.refreshAfterResume()
    }
}
