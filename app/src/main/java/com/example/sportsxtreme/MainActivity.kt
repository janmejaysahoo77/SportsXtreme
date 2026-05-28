package com.example.sportsxtreme

import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat

class MainActivity : AppCompatActivity() {

    private var isCustomSplashReady = false
    private var homeScreenView: HomeScreenView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        // Keep the system splash on screen until our custom splash view has drawn
        splashScreen.setKeepOnScreenCondition { !isCustomSplashReady }

        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        showSplash()
    }

    private fun showSplash() {
        homeScreenView = null
        val splashView = SportsSplashView(this)
        setContentView(splashView)

        // Signal ready once the custom splash view has been measured and drawn
        splashView.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    isCustomSplashReady = true
                    splashView.viewTreeObserver.removeOnPreDrawListener(this)
                    return true
                }
            }
        )

        splashView.postDelayed({
            showMainScreen()
        }, 2800L)
    }

    private fun showMainScreen() {
        homeScreenView = null
        setContentView(OnboardingScreenView(this))
    }

    fun showSignupScreen() {
        homeScreenView = null
        setContentView(SignupScreenView(this))
    }

    fun showLoginScreen() {
        homeScreenView = null
        setContentView(LoginScreenView(this))
    }

    fun showSportSelectionScreen() {
        homeScreenView = null
        setContentView(SportSelectionView(this))
    }

    fun showHomeScreen() {
        homeScreenView = HomeScreenView(this)
        setContentView(homeScreenView)
    }

    override fun onResume() {
        super.onResume()
        homeScreenView?.refreshAfterResume()
    }
}
