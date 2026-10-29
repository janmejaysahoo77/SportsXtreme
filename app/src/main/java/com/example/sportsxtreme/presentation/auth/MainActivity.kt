package com.example.sportsxtreme.presentation.auth

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Build
import android.os.Looper
import android.util.Log
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.animation.doOnEnd
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
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
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.example.sportsxtreme.R
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.data.di.AuthDependencies
import com.example.sportsxtreme.presentation.phoneAuth.PhoneAuth
import com.example.sportsxtreme.presentation.home.HomeScreenView
import com.example.sportsxtreme.presentation.profile.ProfileActivity
import com.example.sportsxtreme.presentation.home.LiveMatchViewModel
import com.example.sportsxtreme.presentation.media.XtremeMediaActivity
import com.example.sportsxtreme.presentation.store.ShoppingActivity
import com.example.sportsxtreme.presentation.tournament.HostTournamentsViewModel
import com.example.sportsxtreme.presentation.clubs.AddMemberInClubActivity
import com.example.sportsxtreme.presentation.clubs.MembersScreen
import com.example.sportsxtreme.presentation.notifications.NotificationActivity
import com.example.sportsxtreme.presentation.tournament.TournamentTeamInviteActivity
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
        Home,
        Members
    }

    private var isComposeInitialized = false
    private var homeScreenView: HomeScreenView? = null
    private var emailVerificationScreenView: EmailVerificationScreenView? = null
    private var pendingOtpContact = ""
    private var pendingPhoneSignupNumber: String? = null
    private var signupPhoneVerificationPending = false
    private var openMyCricketTeamsAfterInvite by mutableStateOf(false)
    private var openHostQuickScoringAfterReturn by mutableStateOf(false)
    private var teamWelcomeName by mutableStateOf<String?>(null)
    private val authViewModel by lazy { AuthDependencies.authViewModel() }
    private val inviteLinkViewModel: InviteLinkViewModel by viewModels()
    private val inviteClaimViewModel: InviteClaimViewModel by viewModels()
    private val liveMatchViewModel: LiveMatchViewModel by viewModels()
    private val hostTournamentsViewModel: HostTournamentsViewModel by viewModels()
    private val locationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var locationPermissionPromptedThisSession = false
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        getSharedPreferences("notification_permission", MODE_PRIVATE)
            .edit().putBoolean("home_prompted", true).apply()
    }

    private fun requestNotificationPermissionOnFirstHomeEntry() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val preferences = getSharedPreferences("notification_permission", MODE_PRIVATE)
        if (preferences.getBoolean("home_prompted", false)) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            preferences.edit().putBoolean("home_prompted", true).apply()
            return
        }
        window.decorView.post {
            if (currentScreen == Screen.Home) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    private var locationSettingsCheckInProgress = false
    private var homeLocationLabel by mutableStateOf("Finding your location…")
    private var returnToHomeAfterAuthCancel = false
    private var currentScreen by mutableStateOf(Screen.Splash)
    private var pendingSharedFeedPostId: String? = null
    private var pendingSharedTeamId: String? = null
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            checkLocationSettingsAndUpdate()
        } else if (currentScreen == Screen.Home) {
            publishHomeLocationLabel("Location unavailable")
        }
    }
    private val locationSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        locationSettingsCheckInProgress = false
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            updateCurrentUserLocation()
        } else if (currentScreen == Screen.Home) {
            publishHomeLocationLabel("Location unavailable")
        }
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        // Instantly dismiss the OS system splash overlay without any animation or icon zoom
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            splashScreenViewProvider.remove()
        }

        super.onCreate(savedInstanceState)
        extractProfileUserId(intent)?.let { userId ->
            openProfileFromLink(userId)
            finish()
            return
        }
        receiveIncomingInviteToken(intent)
        pendingSharedFeedPostId = extractSharedFeedPostId(intent)
        pendingSharedTeamId = extractSharedTeamId(intent)
        val hasIncomingAuthLink = handleIncomingAuthLink(intent)
        val forceLogin = intent.getBooleanExtra(EXTRA_FORCE_LOGIN, false)
        val forceSignup = intent.getBooleanExtra(EXTRA_FORCE_SIGNUP, false)
        val startSportSelection = intent.getStringExtra(EXTRA_START_DESTINATION) == DESTINATION_SPORT_SELECTION

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
                    Screen.SportSelection,
                    Screen.Members -> returnFromAuthFlow()

                    Screen.Onboarding,
                    Screen.Splash -> finish()
                }
            }
        })

        if (forceSignup) {
            currentScreen = Screen.Signup
            initComposeContent()
        } else if (forceLogin) {
            currentScreen = Screen.Login
            initComposeContent()
        } else if (startSportSelection) {
            currentScreen = Screen.SportSelection
            initComposeContent()
        } else if (hasIncomingAuthLink) {
            currentScreen = Screen.Login
            initComposeContent()
        } else {
            showSplashDirectly()
        }
    }

    private fun showSplashDirectly() {
        currentScreen = Screen.Splash
        setContentView(R.layout.activity_splash)
        val splash = findViewById<SportsSplashView>(R.id.sportsSplashView)
        splash?.postDelayed({
            if (currentScreen == Screen.Splash) {
                routeAfterSplash()
            }
        }, 2800L)
    }

    private fun initComposeContent() {
        if (isComposeInitialized) return
        isComposeInitialized = true
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
                    com.example.sportsxtreme.presentation.components.GlobalInAppMessageOverlay(
                        onPrimaryAction = { message ->
                            message.targetUrl?.let { url ->
                                if (url.isNotBlank()) {
                                    runCatching {
                                        if (url.startsWith("http://") || url.startsWith("https://")) {
                                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        } else if (url.contains("notification", ignoreCase = true)) {
                                            startActivity(Intent(this@MainActivity, NotificationActivity::class.java))
                                        } else if (url.contains("store", ignoreCase = true) || url.contains("shop", ignoreCase = true)) {
                                            startActivity(Intent(this@MainActivity, ShoppingActivity::class.java))
                                        } else if (url.contains("score", ignoreCase = true)) {
                                            startActivity(Intent(this@MainActivity, com.example.sportsxtreme.presentation.scoring.ViewAllScoreCardActivity::class.java))
                                        } else {
                                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun ensureComposeInitialized() {
        if (!isComposeInitialized) {
            initComposeContent()
        }
    }

    @Composable
    private fun SportsXtremeApp() {
        val pendingInviteToken by inviteLinkViewModel.pendingInviteToken.collectAsState()
        val pendingTeamInviteToken by inviteLinkViewModel.pendingTeamInviteToken.collectAsState()
        val pendingTournamentInviteToken by inviteLinkViewModel.pendingTournamentInviteToken.collectAsState()
        val authState by authViewModel.state.collectAsState()
        LaunchedEffect(pendingInviteToken, authState.authenticatedUser?.id) {
            val token = pendingInviteToken
            if (token != null && authState.authenticatedUser != null) {
                inviteClaimViewModel.resolveMatchInvite(token) { result ->
                    when (result) {
                        is Resource.Success -> result.data?.let { invitation ->
                            startActivity(
                                Intent(this@MainActivity, TournamentTeamInviteActivity::class.java)
                                    .putExtra(TournamentTeamInviteActivity.EXTRA_MATCH_TOKEN, token)
                                    .putExtra(TournamentTeamInviteActivity.EXTRA_MATCH_ID, invitation.matchId)
                                    .putExtra(TournamentTeamInviteActivity.EXTRA_MATCH_SLOT, invitation.teamSide.name)
                            )
                        }
                        is Resource.Error -> Toast.makeText(
                            this@MainActivity,
                            result.message ?: "Unable to open match invitation",
                            Toast.LENGTH_LONG
                        ).show()
                        is Resource.Loading -> Unit
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
        LaunchedEffect(pendingTournamentInviteToken, authState.authenticatedUser?.id) {
            val token = pendingTournamentInviteToken
            if (token != null && authState.authenticatedUser != null) {
                inviteLinkViewModel.consumeTournamentInviteToken(token)
                startActivity(Intent(this@MainActivity, TournamentTeamInviteActivity::class.java).putExtra(TournamentTeamInviteActivity.EXTRA_TOKEN, token))
            }
        }
        when (currentScreen) {
            Screen.Splash -> AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    homeScreenView = null
                    SportsSplashView(context).apply {
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

            Screen.Home -> key(openMyCricketTeamsAfterInvite, openHostQuickScoringAfterReturn) {
                AndroidView(
                    factory = { context ->
                        HomeScreenView(
                            context,
                            liveMatchViewModel = liveMatchViewModel,
                            hostTournamentsViewModel = hostTournamentsViewModel,
                            startInMyCricketTeams = openMyCricketTeamsAfterInvite,
                            startInHostTab = intent.getBooleanExtra(EXTRA_OPEN_HOST_TAB, false),
                            startInQuickScoringTab = openHostQuickScoringAfterReturn,
                            sharedPostId = pendingSharedFeedPostId
                        ).also {
                            it.updateLocationLabel(homeLocationLabel)
                            homeScreenView = it
                            pendingSharedFeedPostId = null
                        }
                    }
                )
            }

            Screen.Members -> MembersScreen(
                onBack = { showHomeScreen() },
                onAddMember = {
                    startActivity(Intent(this@MainActivity, AddMemberInClubActivity::class.java))
                }
            )

        }
    }

    private fun showMainScreen() {
        homeScreenView = null
        currentScreen = Screen.Onboarding
        ensureComposeInitialized()
    }

    private fun routeAfterSplash() {
        if (authViewModel.state.value.authenticatedUser != null) {
            showHomeScreen()
        } else if (GuestExperiencePreferences.selectedSport(this) != null) {
            showHomeScreen()
        } else if (GuestExperiencePreferences.hasCompletedOnboarding(this)) {
            showSportSelectionScreen()
        } else {
            showMainScreen()
        }
    }

    private fun returnFromAuthFlow() {
        if (returnToHomeAfterAuthCancel) showHomeScreen() else showMainScreen()
    }

    fun showSignupScreen(verifiedPhoneNumber: String? = null) {
        returnToHomeAfterAuthCancel = currentScreen == Screen.Home || returnToHomeAfterAuthCancel
        homeScreenView = null
        signupPhoneVerificationPending = false
        pendingPhoneSignupNumber = verifiedPhoneNumber
        currentScreen = Screen.Signup
        ensureComposeInitialized()
    }

    fun showLoginScreen() {
        returnToHomeAfterAuthCancel = currentScreen == Screen.Home || returnToHomeAfterAuthCancel
        homeScreenView = null
        currentScreen = Screen.Login
        ensureComposeInitialized()
    }

    fun showEmailVerificationScreen() {
        homeScreenView = null
        emailVerificationScreenView = null
        currentScreen = Screen.EmailVerification
        ensureComposeInitialized()
    }

    fun showPhoneAuthScreen(forSignupCompletion: Boolean = false) {
        homeScreenView = null
        pendingPhoneSignupNumber = null
        signupPhoneVerificationPending = forSignupCompletion
        currentScreen = Screen.PhoneAuth
        ensureComposeInitialized()
    }

    fun isSignupPhoneVerificationPending(): Boolean = signupPhoneVerificationPending

    fun showOtpVerificationScreen(contact: String) {
        homeScreenView = null
        pendingOtpContact = contact
        currentScreen = Screen.OtpVerification
        ensureComposeInitialized()
    }

    fun showVerificationCompleteScreen() {
        homeScreenView = null
        emailVerificationScreenView = null
        currentScreen = Screen.VerificationComplete
        ensureComposeInitialized()
    }

    fun showSportSelectionScreen() {
        homeScreenView = null
        signupPhoneVerificationPending = false
        currentScreen = Screen.SportSelection
        ensureComposeInitialized()
    }

    fun showHomeScreen() {
        homeScreenView = null
        emailVerificationScreenView = null
        returnToHomeAfterAuthCancel = false
        signupPhoneVerificationPending = false
        openHostQuickScoringAfterReturn = false
        currentScreen = Screen.Home
        ensureComposeInitialized()
        requestNotificationPermissionOnFirstHomeEntry()
        window.decorView.post {
            if (currentScreen == Screen.Home) {
                requestLocationPermissionAndUpdate()
                openPendingSharedTeam()
            }
        }
    }

    private fun showHostQuickScoringTab() {
        homeScreenView = null
        emailVerificationScreenView = null
        openMyCricketTeamsAfterInvite = false
        openHostQuickScoringAfterReturn = true
        currentScreen = Screen.Home
        ensureComposeInitialized()
        requestNotificationPermissionOnFirstHomeEntry()
        window.decorView.post {
            if (currentScreen == Screen.Home) requestLocationPermissionAndUpdate()
        }
    }

    fun showMembersScreen() {
        homeScreenView = null
        emailVerificationScreenView = null
        currentScreen = Screen.Members
        ensureComposeInitialized()
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
        extractSharedTeamId(intent)?.let { teamId ->
            pendingSharedTeamId = teamId
            if (currentScreen == Screen.Home) openPendingSharedTeam()
            else if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null) showHomeScreen()
            return
        }
        extractProfileUserId(intent)?.let { userId ->
            openProfileFromLink(userId)
            return
        }
        if (intent.getBooleanExtra(EXTRA_OPEN_HOST_TAB, false)) {
            showHomeScreen()
            return
        }
        if (intent.getBooleanExtra(EXTRA_OPEN_HOST_QUICK_SCORING, false)) {
            showHostQuickScoringTab()
            return
        }
        if (intent.getBooleanExtra(EXTRA_FORCE_LOGIN, false)) {
            homeScreenView = null
            currentScreen = Screen.Login
            return
        }
        if (intent.getBooleanExtra(EXTRA_FORCE_SIGNUP, false)) {
            showSignupScreen()
            return
        }
        extractSharedFeedPostId(intent)?.let { postId ->
            pendingSharedFeedPostId = postId
            if (currentScreen == Screen.Home && homeScreenView != null) {
                homeScreenView?.openFeedPost(postId)
                pendingSharedFeedPostId = null
            } else {
                showHomeScreen()
            }
            return
        }
        receiveIncomingInviteToken(intent)
        if (handleIncomingAuthLink(intent)) {
            showLoginScreen()
        }
    }

    private fun receiveIncomingInviteToken(intent: Intent?) {
        extractTournamentInviteToken(intent)?.let(inviteLinkViewModel::receiveTournamentInviteToken)
            ?: extractTeamInviteToken(intent)?.let(inviteLinkViewModel::receiveTeamInviteToken)
            ?: extractInviteToken(intent)?.let(inviteLinkViewModel::receiveInviteToken)
    }

    private fun isSupportedHost(host: String?): Boolean {
        return host == INVITE_HOST || host == ALT_INVITE_HOST
    }

    private fun extractInviteToken(intent: Intent?): String? {
        val uri = intent?.data ?: return null
        if (intent.action != Intent.ACTION_VIEW ||
            uri.scheme != INVITE_SCHEME ||
            !isSupportedHost(uri.host) ||
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
            !isSupportedHost(uri.host) ||
            uri.path != TEAM_INVITE_PATH
        ) return null
        return uri.getQueryParameter(TEAM_INVITE_QUERY_PARAMETER)
            ?.trim()
            ?.takeIf { it.matches(TEAM_INVITE_TOKEN_PATTERN) }
    }

    private fun extractTournamentInviteToken(intent: Intent?): String? {
        val uri = intent?.data ?: return null
        if (intent.action != Intent.ACTION_VIEW || uri.scheme != INVITE_SCHEME || !isSupportedHost(uri.host) || uri.path != TOURNAMENT_INVITE_PATH) return null
        return uri.getQueryParameter(TEAM_INVITE_QUERY_PARAMETER)?.trim()?.takeIf { it.matches(TEAM_INVITE_TOKEN_PATTERN) }
    }

    private fun handleIncomingAuthLink(intent: Intent?): Boolean {
        return authViewModel.handleIncomingEmailLink(intent?.dataString)
    }

    private fun extractSharedFeedPostId(intent: Intent?): String? {
        val uri = intent?.data ?: return null
        if (intent.action != Intent.ACTION_VIEW ||
            uri.scheme != INVITE_SCHEME ||
            !isSupportedHost(uri.host) ||
            uri.pathSegments.size != 2 ||
            uri.pathSegments[0] != SHARED_POST_PATH_SEGMENT
        ) return null
        return uri.pathSegments[1].takeIf { it.matches(SOCIAL_POST_ID_PATTERN) }
    }

    private fun extractSharedTeamId(intent: Intent?): String? {
        val uri = intent?.data ?: return null
        if (intent.action != Intent.ACTION_VIEW || uri.scheme != INVITE_SCHEME || !isSupportedHost(uri.host) || uri.path != TEAM_VIEW_PATH) return null
        return uri.getQueryParameter(TEAM_VIEW_QUERY_PARAMETER)?.trim()?.takeIf { it.matches(TEAM_ID_PATTERN) }
    }

    private fun openPendingSharedTeam() {
        val teamId = pendingSharedTeamId ?: return
        if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser == null) return
        pendingSharedTeamId = null
        startActivity(
            Intent(this, com.example.sportsxtreme.presentation.team.TeamProfileActivity::class.java)
                .putExtra(com.example.sportsxtreme.presentation.team.TeamProfileActivity.EXTRA_TEAM_ID, teamId)
        )
    }

    private fun extractProfileUserId(intent: Intent?): String? {
        val uri = intent?.data ?: return null
        if (intent.action != Intent.ACTION_VIEW ||
            uri.scheme != INVITE_SCHEME ||
            !isSupportedHost(uri.host) ||
            uri.path != PROFILE_PATH
        ) return null
        return uri.getQueryParameter(PROFILE_USER_ID_PARAMETER)
            ?.trim()
            ?.takeIf { it.matches(PROFILE_USER_ID_PATTERN) }
    }

    private fun openProfileFromLink(userId: String) {
        startActivity(
            Intent(this, ProfileActivity::class.java)
                .putExtra(ProfileActivity.EXTRA_USER_ID, userId)
        )
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
            checkLocationSettingsAndUpdate()
        } else if (!locationPermissionPromptedThisSession) {
            locationPermissionPromptedThisSession = true
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun checkLocationSettingsAndUpdate() {
        if (locationSettingsCheckInProgress) return
        locationSettingsCheckInProgress = true
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_REQUEST_INTERVAL_MS
        ).build()
        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()

        LocationServices.getSettingsClient(this)
            .checkLocationSettings(settingsRequest)
            .addOnSuccessListener {
                locationSettingsCheckInProgress = false
                updateCurrentUserLocation()
            }
            .addOnFailureListener { error ->
                if (error is ResolvableApiException) {
                    runCatching {
                        locationSettingsLauncher.launch(
                            IntentSenderRequest.Builder(error.resolution).build()
                        )
                    }.onFailure {
                        locationSettingsCheckInProgress = false
                        publishHomeLocationLabel("Location unavailable")
                    }
                } else {
                    locationSettingsCheckInProgress = false
                    publishHomeLocationLabel("Location unavailable")
                }
            }
    }

    private fun updateCurrentUserLocation() {
        locationScope.launch {
            publishHomeLocationLabel("Finding your location…")
            val location = findCurrentLocation()
            if (location == null) {
                publishHomeLocationLabel("Location unavailable")
                return@launch
            }
            val locationLabel = formatLocation(location)
            if (locationLabel.isBlank()) {
                publishHomeLocationLabel("Location unavailable")
                return@launch
            }
            publishHomeLocationLabel(locationLabel)

            val user = authViewModel.state.value.authenticatedUser ?: AuthDependencies.authUseCases()
                .getCurrentUser() ?: return@launch

            val useCases = AuthDependencies.authUseCases()
            val profile = when (val profileResult = useCases.getUserProfile(user.id)) {
                is Resource.Success -> profileResult.data
                else -> null
            } ?: user.toLocationFallbackProfile()

            useCases.updateUserProfile(profile.copy(location = locationLabel))
        }
    }

    private fun publishHomeLocationLabel(label: String) {
        homeLocationLabel = label
        homeScreenView?.updateLocationLabel(label)
    }

    fun readHomeLocationLabel(): String = homeLocationLabel

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

            val recentAccurateLocation = lastLocation?.takeIf {
                System.currentTimeMillis() - it.time <= LOCATION_MAX_CACHE_AGE_MS &&
                        it.accuracy <= LOCATION_MAX_CACHE_ACCURACY_METERS
            }
            if (recentAccurateLocation != null) {
                return@withContext recentAccurateLocation
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
        const val EXTRA_FORCE_LOGIN = "extra_force_login"
        const val EXTRA_FORCE_SIGNUP = "extra_force_signup"
        const val EXTRA_OPEN_HOST_QUICK_SCORING = "open_host_quick_scoring"
        const val DESTINATION_SPORT_SELECTION = "sport_selection"
        const val EXTRA_OPEN_HOST_TAB = "open_host_tab"
        private const val INVITE_SCHEME = "https"
        private const val INVITE_HOST = "sportsxtreme-95fbb.web.app"
        private const val ALT_INVITE_HOST = "sportsxtreme-95fbb.firebaseapp.com"
        private const val INVITE_PATH = "/join"
        private const val INVITE_QUERY_PARAMETER = "invite"
        private const val TEAM_INVITE_PATH = "/team-invite"
        private const val TOURNAMENT_INVITE_PATH = "/tournament-invite"
        private const val SHARED_POST_PATH_SEGMENT = "post"
        private const val TEAM_VIEW_PATH = "/team"
        private const val TEAM_VIEW_QUERY_PARAMETER = "teamId"
        private val TEAM_ID_PATTERN = Regex("^[A-Za-z0-9_-]{1,150}$")
        private const val PROFILE_PATH = "/profile"
        private const val PROFILE_USER_ID_PARAMETER = "userId"
        private val PROFILE_USER_ID_PATTERN = Regex("^[A-Za-z0-9_-]{1,128}$")
        private val SOCIAL_POST_ID_PATTERN = Regex("^[A-Za-z0-9_-]{1,150}$")
        private const val TEAM_INVITE_QUERY_PARAMETER = "token"
        private val TEAM_INVITE_TOKEN_PATTERN = Regex("^[A-Za-z0-9_-]{43}$")
        private const val LOCATION_FETCH_TIMEOUT_MS = 12000L
        private const val LOCATION_REQUEST_INTERVAL_MS = 10000L
        private const val LOCATION_MAX_CACHE_AGE_MS = 2 * 60 * 1000L
        private const val LOCATION_MAX_CACHE_ACCURACY_METERS = 5_000f
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
