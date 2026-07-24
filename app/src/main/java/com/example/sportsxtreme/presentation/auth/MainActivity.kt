package com.example.sportsxtreme.presentation.auth

import android.Manifest
import android.annotation.SuppressLint
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
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.ViewTreeObserver
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.data.di.AuthDependencies
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.withContext

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
    private val locationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var currentScreen by mutableStateOf(Screen.Splash)
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            updateCurrentUserLocation()
        }
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        AuthDependencies.initialize(applicationContext)
        val splashScreen = installSplashScreen()

        // Keep the system splash on screen until our custom splash view has drawn
        splashScreen.setKeepOnScreenCondition { !isCustomSplashReady }

        super.onCreate(savedInstanceState)
        requestLocationPermissionAndUpdate()
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
        requestLocationPermissionAndUpdate()
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

    private fun requestLocationPermissionAndUpdate() {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            updateCurrentUserLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun updateCurrentUserLocation() {
        val user = authViewModel.state.value.authenticatedUser ?: AuthDependencies.authUseCases().getCurrentUser() ?: return
        locationScope.launch {
            val location = findCurrentLocation() ?: return@launch
            val locationLabel = formatLocation(location)
            if (locationLabel.isBlank()) return@launch

            val useCases = AuthDependencies.authUseCases()
            val profile = when (val profileResult = useCases.getUserProfile(user.id)) {
                is Resource.Success -> profileResult.data
                else -> null
            } ?: user.toLocationFallbackProfile()

            useCases.updateUserProfile(profile.copy(location = locationLabel))
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun findCurrentLocation(): Location? {
        return withContext(Dispatchers.Main) {
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val knownProviders = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )
            val lastLocation = knownProviders
                .mapNotNull { providerName ->
                    runCatching { locationManager.getLastKnownLocation(providerName) }.getOrNull()
                }
                .sortedWith(compareByDescending<Location> { it.time }.thenBy { it.accuracy })
                .firstOrNull()

            if (lastLocation != null) {
                return@withContext lastLocation
            }

            val enabledProviders = runCatching { locationManager.getProviders(true) }
                .getOrDefault(emptyList())
                .filter { it == LocationManager.GPS_PROVIDER || it == LocationManager.NETWORK_PROVIDER }

            if (enabledProviders.isEmpty()) {
                return@withContext null
            }

            withTimeoutOrNull(LOCATION_FETCH_TIMEOUT_MS) {
                suspendCancellableCoroutine { continuation ->
                    val listener = object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            if (continuation.isActive) {
                                continuation.resume(location)
                            }
                            locationManager.removeUpdates(this)
                        }
                    }

                    continuation.invokeOnCancellation { locationManager.removeUpdates(listener) }
                    var requestStarted = false
                    enabledProviders.forEach { provider ->
                        runCatching {
                            locationManager.requestLocationUpdates(
                                provider,
                                0L,
                                0f,
                                listener,
                                Looper.getMainLooper()
                            )
                            requestStarted = true
                        }
                    }
                    if (!requestStarted && continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            }
        }
    }

    private suspend fun formatLocation(location: Location): String {
        return withContext(Dispatchers.IO) {
            val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
            val address = runCatching {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
            }.getOrNull()
            listOfNotNull(address?.locality, address?.adminArea, address?.countryName)
                .distinct()
                .joinToString(", ")
                .ifBlank {
                    "%.4f, %.4f".format(Locale.US, location.latitude, location.longitude)
                }
        }
    }

    private fun com.example.sportsxtreme.domain.model.User.toLocationFallbackProfile(): com.example.sportsxtreme.domain.model.UserProfile {
        return com.example.sportsxtreme.domain.model.UserProfile(
            id = id,
            name = name.ifBlank { "SportsXtreme Player" },
            email = email,
            phoneNumber = mobileNumber,
            profilePhotoUrl = profilePhotoUrl,
            authProvider = authProvider,
            isEmailVerified = isEmailVerified,
            isPhoneVerified = isPhoneVerified
        )
    }

    companion object {
        const val EXTRA_START_DESTINATION = "extra_start_destination"
        const val DESTINATION_SPORT_SELECTION = "sport_selection"
        private const val LOCATION_FETCH_TIMEOUT_MS = 12000L
    }
}
