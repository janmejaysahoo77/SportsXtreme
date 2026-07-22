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
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.data.di.AuthDependencies

class MainActivity : ComponentActivity() {

    private enum class Screen {
        Splash,
        Onboarding,
        Signup,
        Login,
        EmailVerification,
        VerificationComplete,
        OtpVerification,
        SportSelection,
        Home
    }

    private var isCustomSplashReady = false
    private var homeScreenView: HomeScreenView? = null
    private var emailVerificationScreenView: EmailVerificationScreenView? = null
    private var pendingOtpContact = ""
    private val authViewModel by lazy { AuthDependencies.authViewModel() }
    private var currentScreen by mutableStateOf(Screen.Splash)

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        AuthDependencies.initialize(applicationContext)
        val splashScreen = installSplashScreen()

        // Keep the system splash on screen until our custom splash view has drawn
        splashScreen.setKeepOnScreenCondition { !isCustomSplashReady }

        super.onCreate(savedInstanceState)
        val hasIncomingAuthLink = handleIncomingAuthLink(intent)
        if (intent.getStringExtra(EXTRA_START_DESTINATION) == DESTINATION_SPORT_SELECTION) {
            isCustomSplashReady = true
            currentScreen = Screen.SportSelection
        } else if (hasIncomingAuthLink) {
            isCustomSplashReady = true
            currentScreen = Screen.Login
        } else if (authViewModel.state.value.authenticatedUser != null) {
            isCustomSplashReady = true
            currentScreen = Screen.Home
        }
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (currentScreen) {
                    Screen.Home -> finish()
                    Screen.Signup,
                    Screen.Login,
                    Screen.EmailVerification,
                    Screen.VerificationComplete,
                    Screen.OtpVerification,
                    Screen.SportSelection -> showMainScreen()
                    Screen.Onboarding,
                    Screen.Splash -> finish()
                }
            }
        })
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
                            routeAfterSplash()
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

            Screen.EmailVerification -> AndroidView(
                factory = { context ->
                    homeScreenView = null
                    EmailVerificationScreenView(context).also {
                        emailVerificationScreenView = it
                    }
                }
            )

            Screen.VerificationComplete -> AndroidView(
                factory = { context ->
                    homeScreenView = null
                    VerificationCompleteScreenView(context)
                }
            )

            Screen.OtpVerification -> AndroidView(
                factory = { context ->
                    homeScreenView = null
                    OtpVerificationScreenView(context, pendingOtpContact)
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

    private fun routeAfterSplash() {
        if (authViewModel.state.value.authenticatedUser != null) {
            showHomeScreen()
        } else {
            showMainScreen()
        }
    }

    fun showSignupScreen() {
        homeScreenView = null
        currentScreen = Screen.Signup
    }

    fun showLoginScreen() {
        homeScreenView = null
        currentScreen = Screen.Login
    }

    fun showEmailVerificationScreen() {
        homeScreenView = null
        emailVerificationScreenView = null
        currentScreen = Screen.EmailVerification
    }

    fun showOtpVerificationScreen(contact: String) {
        homeScreenView = null
        pendingOtpContact = contact
        currentScreen = Screen.OtpVerification
    }

    fun showVerificationCompleteScreen() {
        homeScreenView = null
        emailVerificationScreenView = null
        currentScreen = Screen.VerificationComplete
    }

    fun showSportSelectionScreen() {
        homeScreenView = null
        currentScreen = Screen.SportSelection
    }

    fun showHomeScreen() {
        homeScreenView = null
        emailVerificationScreenView = null
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
        if (currentScreen == Screen.EmailVerification) {
            emailVerificationScreenView?.autoCheckEmailVerification()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (handleIncomingAuthLink(intent)) {
            showLoginScreen()
        }
    }

    private fun handleIncomingAuthLink(intent: Intent?): Boolean {
        return authViewModel.handleIncomingEmailLink(intent?.dataString)
    }

    companion object {
        const val EXTRA_START_DESTINATION = "extra_start_destination"
        const val DESTINATION_SPORT_SELECTION = "sport_selection"
    }
}
