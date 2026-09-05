package com.example.sportsxtreme.presentation.auth

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.data.di.AuthDependencies
import com.example.sportsxtreme.presentation.phoneAuth.PhoneAuth
import com.example.sportsxtreme.presentation.home.HomeScreenView
import com.example.sportsxtreme.presentation.home.LiveMatchViewModel
import com.example.sportsxtreme.presentation.media.XtremeMediaActivity
import com.example.sportsxtreme.presentation.store.ShoppingActivity
import com.example.sportsxtreme.presentation.tournament.HostTournamentsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.getValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private enum class Screen {
        Splash,
        Onboarding,
        Signup,
        Login,
        EmailVerification,
        VerificationComplete,
        PhoneAuth,
        OtpVerification,
        SportSelection,
        Home
    }

    private var isCustomSplashReady = false
    private var homeScreenView: HomeScreenView? = null
    private var emailVerificationScreenView: EmailVerificationScreenView? = null
    private var pendingOtpContact = ""
    private var pendingPhoneSignupNumber: String? = null
    private var openMyCricketTeamsAfterInvite by mutableStateOf(false)
    private var teamWelcomeName by mutableStateOf<String?>(null)
    private val authViewModel by lazy { AuthDependencies.authViewModel() }
    private val inviteLinkViewModel: InviteLinkViewModel by viewModels()
    private val inviteClaimViewModel: InviteClaimViewModel by viewModels()
    private val liveMatchViewModel: LiveMatchViewModel by viewModels()
    private val hostTournamentsViewModel: HostTournamentsViewModel by viewModels()
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
        receiveIncomingInviteToken(intent)
        val hasIncomingAuthLink = handleIncomingAuthLink(intent)
        if (intent.getStringExtra(EXTRA_START_DESTINATION) == DESTINATION_SPORT_SELECTION) {
            isCustomSplashReady = true
            currentScreen = Screen.SportSelection
        } else if (hasIncomingAuthLink) {
            isCustomSplashReady = true
            currentScreen = Screen.Login
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (currentScreen) {
                    Screen.Home -> finish()
                    Screen.Signup,
                    Screen.Login,
                    Screen.EmailVerification,
                    Screen.VerificationComplete,
                    Screen.PhoneAuth,
                    Screen.OtpVerification,
                    Screen.SportSelection -> showMainScreen()

                    Screen.Onboarding,
                    Screen.Splash -> finish()
                }
            }
        })
        setContent {
            Scaffold(
                containerColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f)
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(color = androidx.compose.ui.graphics.Color.Black)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            SportsXtremeApp()
                        }

                    }
                    teamWelcomeName?.let { teamName ->
                        TeamInviteWelcome(
                            teamName = teamName,
                            onFinished = {
                                teamWelcomeName = null
                                showHomeScreen()
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SportsXtremeApp() {
        val pendingInviteToken by inviteLinkViewModel.pendingInviteToken.collectAsState()
        val pendingTeamInviteToken by inviteLinkViewModel.pendingTeamInviteToken.collectAsState()
        val authState by authViewModel.state.collectAsState()
        LaunchedEffect(pendingInviteToken) {
            pendingInviteToken?.let { token ->
                inviteClaimViewModel.claim(token) { result ->
                    if (result is Resource.Error) {
                        Log.e("MatchInvite", "Invite claim failed: ${result.message}")
                        Toast.makeText(
                            this@MainActivity,
                            result.message ?: "Invite claim failed",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (result is Resource.Success) {
                        result.data?.let { claim ->
                            Log.d(
                                "MatchInvite",
                                "Invite claimed: ${claim.matchId} ${claim.teamSlot}"
                            )
                        }
                    }
                    inviteLinkViewModel.consumeInviteToken(token)
                }
            }
        }
        LaunchedEffect(pendingTeamInviteToken, authState.authenticatedUser?.id) {
            val token = pendingTeamInviteToken
            if (token != null && authState.authenticatedUser != null) {
                Toast.makeText(this@MainActivity, "Joining team…", Toast.LENGTH_SHORT).show()
                inviteClaimViewModel.claimTeamInvite(token) { result ->
                    when (result) {
                        is Resource.Success -> {
                            openMyCricketTeamsAfterInvite = true
                            if (result.data?.alreadyMember == true) {
                                showHomeScreen()
                                Toast.makeText(
                                    this@MainActivity,
                                    "You are already in this team",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                teamWelcomeName = result.data?.teamName ?: "the team"
                            }
                        }
                        is Resource.Error -> Toast.makeText(
                            this@MainActivity,
                            result.message ?: "Unable to join team",
                            Toast.LENGTH_LONG
                        ).show()
                        is Resource.Loading -> Unit
                    }
                    // Invalid, expired, revoked, and used links must not replay.
                    inviteLinkViewModel.consumeTeamInviteToken(token)
                }
            }
        }
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
                    SignupScreenView(context, verifiedPhoneNumber = pendingPhoneSignupNumber)
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

            Screen.PhoneAuth -> PhoneAuth()

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

            Screen.Home -> key(openMyCricketTeamsAfterInvite) {
                AndroidView(
                    factory = { context ->
                        HomeScreenView(
                            context,
                            liveMatchViewModel = liveMatchViewModel,
                            hostTournamentsViewModel = hostTournamentsViewModel,
                            startInMyCricketTeams = openMyCricketTeamsAfterInvite
                        ).also {
                            homeScreenView = it
                        }
                    }
                )
            }

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

    fun showSignupScreen(verifiedPhoneNumber: String? = null) {
        homeScreenView = null
        pendingPhoneSignupNumber = verifiedPhoneNumber
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

    fun showPhoneAuthScreen() {
        homeScreenView = null
        pendingPhoneSignupNumber = null
        currentScreen = Screen.PhoneAuth
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
        receiveIncomingInviteToken(intent)
        if (handleIncomingAuthLink(intent)) {
            showLoginScreen()
        }
    }

    private fun receiveIncomingInviteToken(intent: Intent?) {
        extractTeamInviteToken(intent)?.let(inviteLinkViewModel::receiveTeamInviteToken)
            ?: extractInviteToken(intent)?.let(inviteLinkViewModel::receiveInviteToken)
    }

    private fun extractInviteToken(intent: Intent?): String? {
        val uri = intent?.data ?: return null
        if (intent.action != Intent.ACTION_VIEW ||
            uri.scheme != INVITE_SCHEME ||
            uri.host != INVITE_HOST ||
            uri.path != INVITE_PATH
        ) {
            return null
        }
        return uri.getQueryParameter(INVITE_QUERY_PARAMETER)
            ?.trim()
            ?.takeIf(String::isNotBlank)
    }

    private fun extractTeamInviteToken(intent: Intent?): String? {
        val uri = intent?.data ?: return null
        if (intent.action != Intent.ACTION_VIEW ||
            uri.scheme != INVITE_SCHEME ||
            uri.host != INVITE_HOST ||
            uri.path != TEAM_INVITE_PATH
        ) return null
        return uri.getQueryParameter(TEAM_INVITE_QUERY_PARAMETER)
            ?.trim()
            ?.takeIf { it.matches(TEAM_INVITE_TOKEN_PATTERN) }
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
        val user = authViewModel.state.value.authenticatedUser ?: AuthDependencies.authUseCases()
            .getCurrentUser() ?: return
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
        private const val INVITE_SCHEME = "https"
        private const val INVITE_HOST = "sportsxtreme-95fbb.web.app"
        private const val INVITE_PATH = "/join"
        private const val INVITE_QUERY_PARAMETER = "invite"
        private const val TEAM_INVITE_PATH = "/team-invite"
        private const val TEAM_INVITE_QUERY_PARAMETER = "token"
        private val TEAM_INVITE_TOKEN_PATTERN = Regex("^[A-Za-z0-9_-]{43}$")
        private const val LOCATION_FETCH_TIMEOUT_MS = 12000L
    }
}

@Composable
private fun TeamInviteWelcome(teamName: String, onFinished: () -> Unit) {
    val iconScale = remember { Animatable(2.25f) }
    val messageAlpha = remember { Animatable(0f) }

    LaunchedEffect(teamName) {
        iconScale.animateTo(
            targetValue = 0.82f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
        messageAlpha.animateTo(1f, animationSpec = tween(durationMillis = 350))
        kotlinx.coroutines.delay(1500)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color(0xFF061C12)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.appicon),
                contentDescription = "SportsXtreme",
                modifier = Modifier
                    .size(112.dp)
                    .graphicsLayer {
                        scaleX = iconScale.value
                        scaleY = iconScale.value
                    }
            )
            Spacer(Modifier.height(42.dp))
            Text(
                text = "Welcome to\n$teamName",
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp,
                modifier = Modifier.alpha(messageAlpha.value)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "You’re now part of the squad.",
                color = androidx.compose.ui.graphics.Color(0xFFB7D9BE),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(messageAlpha.value)
            )
        }
    }
}
