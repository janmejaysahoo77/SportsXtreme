package com.example.sportsxtreme.presentation.profile

import android.os.Bundle
import android.graphics.Bitmap
import android.net.Uri
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.data.di.AuthDependencies
import com.example.sportsxtreme.presentation.auth.MainActivity
import com.example.sportsxtreme.presentation.components.GuestAccessCard
import com.example.sportsxtreme.data.remote.cloudinary.CloudinaryProfilePhotoUploader
import com.example.sportsxtreme.domain.model.User
import com.example.sportsxtreme.domain.model.PendingUserProfile
import com.example.sportsxtreme.domain.model.UserProfile
import com.example.sportsxtreme.domain.model.UserProfileSettings
import com.example.sportsxtreme.domain.model.UserProfileStats
import com.example.sportsxtreme.domain.usecase.AuthUseCases
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import coil3.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext

internal val XtremeBg = Color(0xFF010407)
internal val Card = Color(0xFF0C1419)
internal val Line = Color(0xFF26333A)
internal val Lime = Color(0xFFC7FF1A)
internal val Gold = Color(0xFFFFD66B)
internal val Aqua = Color(0xFF45E9FF)
internal val Platinum = Color(0xFFEAF2F4)
internal val SoftText = Color(0xFFA8B5B9)
internal val DeepPanel = Color(0xFF071116)

private data class ProfileUiState(
    val profile: UserProfile = fallbackProfile(),
    val stats: UserProfileStats = UserProfileStats(userId = ""),
    val settings: UserProfileSettings = UserProfileSettings(userId = ""),
    val profilePhotoPreviewUri: Uri? = null,
    val profilePhotoRefreshKey: Long = 0L,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isAccountDeleted: Boolean = false,
    val requiresReauthentication: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null
)

private data class ProfileChoiceInput(
    val label: String,
    val options: List<String>,
    val currentValue: String,
    val updateProfile: (UserProfile, String) -> UserProfile
)

private enum class ProfileEditSection(val title: String) {
    PersonalDetails("Personal Details"),
    PlayingProfile("Playing Profile")
}

class ProfileActivity : ComponentActivity() {

    private var selectedProfilePhotoUri by mutableStateOf<Uri?>(null)
    private val profilePhotoPicker = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedProfilePhotoUri = uri
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthDependencies.initialize(applicationContext)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        val viewedUserId = intent.getStringExtra(EXTRA_USER_ID)?.trim().orEmpty()
        val fallbackName = intent.getStringExtra(EXTRA_DISPLAY_NAME)?.trim().orEmpty()
        val fallbackPhotoUrl = intent.getStringExtra(EXTRA_PROFILE_PHOTO_URL)
            ?.trim()
            ?.takeUnless { it.isGoogleAccountPhotoUrl() }
            .orEmpty()
        setContent {
            ProfileScreen(
                useCases = AuthDependencies.authUseCases(),
                viewedUserId = viewedUserId,
                fallbackName = fallbackName,
                fallbackPhotoUrl = fallbackPhotoUrl,
                onBack = { finish() },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(
                        Intent(this, MainActivity::class.java)
                            .putExtra(MainActivity.EXTRA_FORCE_LOGIN, true)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                    finish()
                },
                selectedPhotoUri = selectedProfilePhotoUri,
                onPhotoHandled = { selectedProfilePhotoUri = null },
                onPickPhoto = {
                    profilePhotoPicker.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
            )
        }
    }

    companion object {
        const val EXTRA_USER_ID = "profile.extra.USER_ID"
        const val EXTRA_DISPLAY_NAME = "profile.extra.DISPLAY_NAME"
        const val EXTRA_PROFILE_PHOTO_URL = "profile.extra.PROFILE_PHOTO_URL"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(
    useCases: AuthUseCases,
    viewedUserId: String,
    fallbackName: String,
    fallbackPhotoUrl: String,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    selectedPhotoUri: Uri?,
    onPhotoHandled: () -> Unit,
    onPickPhoto: () -> Unit
) {
    var uiState by remember { mutableStateOf(ProfileUiState()) }
    var activeEditSection by remember { mutableStateOf<ProfileEditSection?>(null) }
    var showNameDialog by remember { mutableStateOf(false) }
    var nameDraft by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showProfilePhotoActions by remember { mutableStateOf(false) }
    var showProfileQr by remember { mutableStateOf(false) }
    var draftProfile by remember { mutableStateOf(uiState.profile) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val context = LocalContext.current
    var isGuest by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser == null) }
    DisposableEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { isGuest = it.currentUser == null }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }
    val deleteAccount: () -> Unit = {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            uiState = uiState.copy(errorMessage = "No signed-in account was found.")
        } else scope.launch {
            uiState = uiState.copy(isSaving = true, errorMessage = null, infoMessage = null)
            try {
                FirebaseFunctions.getInstance()
                    .getHttpsCallable("deleteAccount")
                    .call()
                    .await()
                context.deleteDatabase("sports_xtreme.db")
                Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                FirebaseAuth.getInstance().signOut()
                context.startActivity(
                    Intent(context, MainActivity::class.java)
                        .putExtra(MainActivity.EXTRA_FORCE_LOGIN, true)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            } catch (error: Exception) {
                uiState = uiState.copy(
                    isSaving = false,
                    requiresReauthentication = error is FirebaseFunctionsException &&
                        error.code == FirebaseFunctionsException.Code.FAILED_PRECONDITION,
                    errorMessage = if (error is FirebaseFunctionsException &&
                        error.code == FirebaseFunctionsException.Code.FAILED_PRECONDITION
                    ) {
                        "For your security, sign in again before deleting your account."
                    } else {
                        error.localizedMessage ?: "Could not delete the account. Please try again."
                    }
                )
            }
        }
    }
    val authUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isOwnProfile = remember(viewedUserId, authUserId) {
        viewedUserId.isBlank() || viewedUserId == useCases.getCurrentUser()?.id
    }

    LaunchedEffect(viewedUserId, isGuest) {
        if (isGuest) return@LaunchedEffect
        uiState = if (isOwnProfile) {
            loadProfileUiState(useCases)
        } else {
            loadOtherProfileUiState(useCases, viewedUserId, fallbackName, fallbackPhotoUrl)
        }
    }

    // A scorecard opens another player's profile.  Show their identity and avatar
    // immediately, then calculate match totals separately.  Older accounts do not
    // have the cached /users/{id}/stats/summary document, so that document alone
    // would always render zeros for those players.
    LaunchedEffect(viewedUserId, isOwnProfile, uiState.profile.id, isGuest) {
        if (isGuest || uiState.profile.id.isBlank()) return@LaunchedEffect
        val refreshedStats = loadLivePlayerStats(uiState.profile.id, uiState.stats)
        if (refreshedStats != uiState.stats) {
            uiState = uiState.copy(stats = refreshedStats)
        }
    }

    DisposableEffect(uiState.profile.id, isGuest) {
        if (isGuest || !isOwnProfile) return@DisposableEffect onDispose { }
        val userId = uiState.profile.id
        if (userId.isBlank()) return@DisposableEffect onDispose { }

        val firestore = FirebaseFirestore.getInstance()
        var hostedTournaments = uiState.profile.hostedTournaments
        var hostedSeries = uiState.profile.hostedSeries
        var hostedMatches = uiState.profile.hostedMatches

        fun publishHostedCounts() {
            uiState = uiState.copy(
                profile = uiState.profile.copy(
                    hostedTournaments = hostedTournaments,
                    hostedSeries = hostedSeries,
                    hostedMatches = hostedMatches
                )
            )
        }

        val tournamentsListener = firestore.collection("tournaments")
            .whereEqualTo("hostUid", userId)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    hostedTournaments = snapshot.documents.count {
                        !it.getString("type").equals("Series", ignoreCase = true)
                    }
                    hostedSeries = snapshot.documents.count {
                        it.getString("type").equals("Series", ignoreCase = true)
                    }
                    publishHostedCounts()
                }
            }
        val matchesListener = firestore.collection("matches")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    hostedMatches = snapshot.documents.count { match ->
                        match.getString("ownerId") == userId ||
                            match.getString("ownerUserId") == userId ||
                            match.getString("organiserId") == userId ||
                            match.getString("organizerId") == userId
                    }
                    publishHostedCounts()
                }
            }

        onDispose {
            tournamentsListener.remove()
            matchesListener.remove()
        }
    }

    LaunchedEffect(selectedPhotoUri) {
        val photoUri = selectedPhotoUri ?: return@LaunchedEffect
        if (!isOwnProfile || uiState.profile.id.isBlank()) {
            onPhotoHandled()
            return@LaunchedEffect
        }
        uiState = uiState.copy(
            profilePhotoPreviewUri = photoUri,
            isSaving = true,
            infoMessage = null,
            errorMessage = null
        )
        val uploadResult = CloudinaryProfilePhotoUploader(context.applicationContext).upload(photoUri)
        val photoUrl = uploadResult.getOrElse { error ->
            uiState = uiState.copy(
                profilePhotoPreviewUri = null,
                isSaving = false,
                errorMessage = error.message ?: "Could not upload profile photo."
            )
            onPhotoHandled()
            return@LaunchedEffect
        }
        val updatedProfile = uiState.profile.copy(profilePhotoUrl = photoUrl)
        uiState = when (useCases.updateUserProfile(updatedProfile)) {
            is Resource.Success -> uiState.copy(
                profile = updatedProfile,
                // Stop showing the local content URI after saving so this device verifies
                // the same remote URL every other player will receive.
                profilePhotoPreviewUri = null,
                // Cloudinary can overwrite a stable public ID. Bump the displayed URL so
                // Coil renders the newly uploaded image instead of an existing cache entry.
                profilePhotoRefreshKey = System.currentTimeMillis(),
                isSaving = false,
                infoMessage = "Profile photo updated.",
                errorMessage = null
            )
            is Resource.Error -> uiState.copy(
                profilePhotoPreviewUri = null,
                isSaving = false,
                errorMessage = "Photo uploaded, but its profile link could not be saved."
            )
            is Resource.Loading -> uiState.copy(isSaving = true)
        }
        onPhotoHandled()
    }

    Surface(color = XtremeBg, modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            ProfileBackdrop()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (isGuest) Modifier.blur(10.dp) else Modifier)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileTopBar(onBack)
                if (uiState.isLoading) {
                    Spacer(Modifier.height(10.dp))
                    ProfileSkeleton()
                } else if (uiState.isAccountDeleted) {
                    Text(
                        "Account already deleted",
                        color = Platinum,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 56.dp)
                    )
                } else {
                    if (uiState.isSaving || uiState.infoMessage != null || uiState.errorMessage != null) {
                        ProfileStatusLine(uiState)
                    }
                    Spacer(Modifier.height(10.dp))
                    HeroProfileCard(
                        profile = uiState.profile,
                        stats = uiState.stats,
                        profilePhotoPreviewUri = uiState.profilePhotoPreviewUri,
                        profilePhotoRefreshKey = uiState.profilePhotoRefreshKey,
                        editEnabled = isOwnProfile && !uiState.isSaving,
                        onEditAvatar = { showProfilePhotoActions = true },
                        onEditName = {
                            nameDraft = uiState.profile.name
                            showNameDialog = true
                        }
                    )
                    Spacer(Modifier.height(14.dp))
                    HostedSummary(uiState.profile)
                    Spacer(Modifier.height(18.dp))
                    Text(
                        "Your Xtreme Presence",
                        color = Platinum,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    )
                    ProfileStats(uiState.profile, onShowQr = { showProfileQr = true })
                    if (isOwnProfile) {
                        SectionTitle(
                            title = "Personal Details",
                            onEdit = {
                                draftProfile = uiState.profile
                                activeEditSection = ProfileEditSection.PersonalDetails
                            },
                            editEnabled = !uiState.isSaving
                        )
                        DetailGrid(
                            listOf(
                                "Mobile Number" to uiState.profile.phoneNumber.ifBlank { "Not added" },
                                "Gender" to uiState.profile.gender.ifBlank { "Not added" },
                                "Email Address" to uiState.profile.email.ifBlank { "Not added" }
                            )
                        )
                    }
                    SectionTitle(
                        title = "Playing Profile",
                        onEdit = if (isOwnProfile) ({
                            draftProfile = uiState.profile
                            activeEditSection = ProfileEditSection.PlayingProfile
                        }) else null,
                        editEnabled = !uiState.isSaving
                    )
                    DetailGrid(
                        listOf(
                            "Role" to uiState.profile.role.ifBlank { "Not added" },
                            "Batting" to uiState.profile.battingStyle.ifBlank { "Not added" },
                            "Bowling" to uiState.profile.bowlingStyle.ifBlank { "Not added" }
                        )
                    )
                    Spacer(Modifier.height(18.dp))
                    if (isOwnProfile) {
                        SettingsCard(
                            onLogout = onLogout,
                            onDeleteAccount = { showDeleteConfirmation = true },
                            onPrivacyPolicy = {
                                context.startActivity(
                                    com.example.sportsxtreme.presentation.home.WebPageActivity.intent(
                                        context,
                                        "/app-privacy-policy"
                                    )
                                )
                            }
                        )
                        Text(
                            "Version 1.0.0",
                            color = SoftText,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(top = 12.dp, bottom = 10.dp)
                        )
                    }
                }
            }

            if (isGuest) {
                Box(
                    Modifier.fillMaxSize().background(Color.Black.copy(alpha = .42f)).clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 16.dp, top = 16.dp)
                            .size(28.dp)
                            .clickable(onClick = onBack)
                    )
                    GuestAccessCard(
                        title = "Sign in to see your profile",
                        detail = "Login or sign up to view your profile and stats.",
                        onSignup = {
                            context.startActivity(
                                Intent(context, MainActivity::class.java)
                                    .putExtra(MainActivity.EXTRA_FORCE_SIGNUP, true)
                            )
                        },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            activeEditSection?.let { section ->
                ModalBottomSheet(
                    onDismissRequest = { if (!uiState.isSaving) activeEditSection = null },
                    sheetState = bottomSheetState,
                    containerColor = Color(0xFF081014),
                    contentColor = Platinum
                ) {
                    ProfileSectionEditSheet(
                        section = section,
                        draftProfile = draftProfile,
                        isSaving = uiState.isSaving,
                        onProfileChange = { draftProfile = it },
                        onCancel = { activeEditSection = null },
                        onSave = {
                            val emailChanged = !draftProfile.email.equals(uiState.profile.email, ignoreCase = true)
                            val phoneChanged = draftProfile.phoneNumber != uiState.profile.phoneNumber
                            val updatedProfile = draftProfile.copy(
                                email = draftProfile.email.trim(),
                                phoneNumber = draftProfile.phoneNumber.trim(),
                                isEmailVerified = if (emailChanged) false else draftProfile.isEmailVerified,
                                isPhoneVerified = if (phoneChanged) false else draftProfile.isPhoneVerified
                            )
                            if (updatedProfile.email.isBlank() || !updatedProfile.email.contains("@")) {
                                uiState = uiState.copy(errorMessage = "Enter a valid email address.", infoMessage = null)
                                return@ProfileSectionEditSheet
                            }
                            uiState = uiState.copy(isSaving = true, errorMessage = null, infoMessage = null)
                            activeEditSection = null
                            scope.launch {
                                val emailVerificationResult = if (emailChanged) {
                                    useCases.sendEmailChangeVerification(updatedProfile.email)
                                } else {
                                    Resource.Success(Unit)
                                }
                                if (emailVerificationResult is Resource.Error) {
                                    uiState = uiState.copy(
                                        isSaving = false,
                                        errorMessage = emailVerificationResult.message ?: "Could not send verification email.",
                                        infoMessage = null
                                    )
                                    return@launch
                                }

                                uiState = when (useCases.updateUserProfile(updatedProfile)) {
                                    is Resource.Success -> uiState.copy(
                                        profile = updatedProfile,
                                        isSaving = false,
                                        infoMessage = if (emailChanged) "Verification email sent. Please verify your new email." else null,
                                        errorMessage = null
                                    )
                                    is Resource.Error -> uiState.copy(
                                        isSaving = false,
                                        infoMessage = null,
                                        errorMessage = "Could not update profile."
                                    )
                                    is Resource.Loading -> uiState.copy(isSaving = true)
                                }
                            }
                        }
                    )
                }
            }

            if (showProfilePhotoActions) {
                ModalBottomSheet(
                    onDismissRequest = { showProfilePhotoActions = false },
                    sheetState = bottomSheetState,
                    containerColor = Color(0xFF081014),
                    contentColor = Platinum
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ProfilePhotoAction(
                            icon = R.drawable.baseline_edit_24,
                            label = "Edit Profile Pic",
                            tint = Lime
                        ) {
                            showProfilePhotoActions = false
                            onPickPhoto()
                        }
                        ProfilePhotoAction(
                            icon = R.drawable.outline_delete_24,
                            label = "Remove pic",
                            tint = Color(0xFFFF7777),
                            enabled = !uiState.isSaving
                        ) {
                            showProfilePhotoActions = false
                            val updatedProfile = uiState.profile.copy(profilePhotoUrl = "")
                            uiState = uiState.copy(isSaving = true, errorMessage = null, infoMessage = null)
                            scope.launch {
                                uiState = when (useCases.updateUserProfile(updatedProfile)) {
                                    is Resource.Success -> uiState.copy(
                                        profile = updatedProfile,
                                        profilePhotoPreviewUri = null,
                                        profilePhotoRefreshKey = System.currentTimeMillis(),
                                        isSaving = false,
                                        infoMessage = "Profile photo removed.",
                                        errorMessage = null
                                    )
                                    is Resource.Error -> uiState.copy(
                                        isSaving = false,
                                        errorMessage = "Could not remove profile photo."
                                    )
                                    is Resource.Loading -> uiState.copy(isSaving = true)
                                }
                            }
                        }
                    }
                }
            }

            if (showProfileQr && uiState.profile.id.isNotBlank()) {
                val profileUrl = remember(uiState.profile.id) { profileDeepLink(uiState.profile.id) }
                val qrBitmap = remember(profileUrl) { profileQrBitmap(profileUrl) }
                AlertDialog(
                    onDismissRequest = { showProfileQr = false },
                    title = { Text("Scan to view profile", color = Platinum, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (qrBitmap != null) {
                                Image(
                                    bitmap = qrBitmap,
                                    contentDescription = "QR code for ${uiState.profile.name}'s profile",
                                    modifier = Modifier.size(220.dp).padding(8.dp)
                                )
                            } else {
                                Text("Could not create the profile QR code.", color = SoftText)
                            }
                            Text(profileUrl, color = SoftText, fontSize = 10.sp, textAlign = TextAlign.Center)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            runCatching {
                                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, profileUrl)
                                }, "Share profile"))
                            }.onFailure { Toast.makeText(context, "Unable to share profile link", Toast.LENGTH_SHORT).show() }
                        }) { Text("Share", color = Lime) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showProfileQr = false }) { Text("Close", color = Platinum) }
                    },
                    containerColor = Color(0xFF081014),
                    titleContentColor = Platinum,
                    textContentColor = Platinum
                )
            }

            if (showNameDialog) {
                AlertDialog(
                    onDismissRequest = { if (!uiState.isSaving) showNameDialog = false },
                    title = { Text("Change your name", color = Platinum) },
                    text = {
                        OutlinedTextField(
                            value = nameDraft,
                            onValueChange = { nameDraft = it },
                            enabled = !uiState.isSaving,
                            singleLine = true,
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(
                            enabled = !uiState.isSaving && nameDraft.trim().isNotEmpty() && nameDraft.trim() != uiState.profile.name,
                            onClick = {
                                val updatedName = nameDraft.trim().replace(Regex("\\s+"), " ")
                                if (updatedName.length > 80) {
                                    uiState = uiState.copy(errorMessage = "Name must be 80 characters or fewer.")
                                    return@TextButton
                                }
                                uiState = uiState.copy(isSaving = true, errorMessage = null, infoMessage = null)
                                scope.launch {
                                    val updatedProfile = uiState.profile.copy(name = updatedName)
                                    when (useCases.updateUserProfile(updatedProfile)) {
                                        is Resource.Success -> {
                                            runCatching {
                                                FirebaseAuth.getInstance().currentUser
                                                    ?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(updatedName).build())
                                                    ?.await()
                                            }
                                            uiState = uiState.copy(profile = updatedProfile, isSaving = false, infoMessage = "Name updated.", errorMessage = null)
                                            showNameDialog = false
                                        }
                                        is Resource.Error -> uiState = uiState.copy(isSaving = false, errorMessage = "Could not update your name.", infoMessage = null)
                                        is Resource.Loading -> uiState = uiState.copy(isSaving = true)
                                    }
                                }
                            }
                        ) { Text(if (uiState.isSaving) "Saving..." else "Save", color = Lime) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showNameDialog = false }, enabled = !uiState.isSaving) {
                            Text("Cancel", color = Platinum)
                        }
                    },
                    containerColor = Color(0xFF081014),
                    titleContentColor = Platinum,
                    textContentColor = Platinum
                )
            }

            if (showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { if (!uiState.isSaving) showDeleteConfirmation = false },
                    title = { Text("Delete account?", color = Platinum) },
                    text = {
                        Column {
                            Text("This permanently deletes your account and the data you own. This cannot be undone.", color = SoftText)
                            TextButton(
                                onClick = {
                                    context.startActivity(
                                        com.example.sportsxtreme.presentation.home.WebPageActivity.intent(
                                            context,
                                            "/app-privacy-policy"
                                        )
                                    )
                                }
                            ) {
                                Text("Privacy Policy", color = Lime)
                            }
                            uiState.errorMessage?.let {
                                Text(it, color = Color(0xFFFF8D8D), modifier = Modifier.padding(top = 10.dp))
                            }
                            if (uiState.isSaving) {
                                CircularProgressIndicator(color = Lime, modifier = Modifier.padding(top = 12.dp).size(24.dp))
                            }
                        }
                    },
                    confirmButton = {
                        if (uiState.requiresReauthentication) {
                            TextButton(
                                onClick = {
                                    FirebaseAuth.getInstance().signOut()
                                    context.startActivity(
                                        Intent(context, MainActivity::class.java)
                                            .putExtra(MainActivity.EXTRA_FORCE_LOGIN, true)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    )
                                },
                                enabled = !uiState.isSaving
                            ) {
                                Text("Sign in again", color = Lime)
                            }
                        } else {
                            TextButton(onClick = deleteAccount, enabled = !uiState.isSaving) {
                                Text("Delete account", color = Color(0xFFFF8D8D))
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmation = false }, enabled = !uiState.isSaving) {
                            Text("Cancel", color = Platinum)
                        }
                    },
                    containerColor = Card
                )
            }
        }
    }
}

@Composable
private fun ProfileStatusLine(uiState: ProfileUiState) {
    val text = uiState.errorMessage ?: uiState.infoMessage ?: if (uiState.isSaving) "Saving profile..." else "Loading latest profile..."
    Text(
        text,
        color = when {
            uiState.errorMessage != null -> Gold
            uiState.infoMessage != null -> Lime
            else -> SoftText
        },
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )
}

private suspend fun loadProfileUiState(useCases: AuthUseCases): ProfileUiState {
    val currentUser = useCases.getCurrentUser()
    val userId = currentUser?.id.orEmpty()
    val fallbackProfile = currentUser.toFallbackProfile()

    if (userId.isBlank()) {
        return ProfileUiState(
            profile = fallbackProfile,
            stats = fallbackProfile.toFallbackStats(),
            settings = UserProfileSettings(userId = ""),
            isLoading = false,
            errorMessage = "Could not find logged-in user."
        )
    }

    currentUser ?: return ProfileUiState(
        profile = fallbackProfile,
        stats = fallbackProfile.toFallbackStats(),
        settings = UserProfileSettings(userId = ""),
        isLoading = false,
        errorMessage = "Could not find logged-in user."
    )
    val (profileResult, statsResult, settingsResult) = coroutineScope {
        val profile = async { useCases.getUserProfile(userId) }
        val stats = async { useCases.getUserProfileStats(userId) }
        val settings = async { useCases.getUserProfileSettings(userId) }
        Triple(profile.await(), stats.await(), settings.await())
    }

    val profile = profileResult.successData() ?: fallbackProfile
    val storedStats = statsResult.successData() ?: profile.toFallbackStats()
    // Show the saved summary immediately. The live match/scorecard reconciliation
    // runs after the profile is visible so large histories cannot hold the page
    // behind its loading skeleton.
    val stats = storedStats
    val settings = settingsResult.successData() ?: UserProfileSettings(userId = userId)
    val errorMessage = listOf(
        profileResult.errorText(),
        statsResult.errorText(),
        settingsResult.errorText()
    ).firstOrNull()

    return ProfileUiState(
        profile = profile,
        stats = stats,
        settings = settings,
        isLoading = false,
        errorMessage = if (errorMessage == null) null else "Some profile data could not refresh."
    )
}

private suspend fun loadOtherProfileUiState(
    useCases: AuthUseCases,
    userId: String,
    fallbackName: String,
    fallbackPhotoUrl: String
): ProfileUiState {
    val profileResult = useCases.getUserProfile(userId)
    val profile = profileResult.successData()
    if (profile == null) {
        return ProfileUiState(
            profile = fallbackProfile().copy(
                id = userId,
                name = fallbackName.ifBlank { "Player" },
                profilePhotoUrl = fallbackPhotoUrl.takeIf { it.isNotBlank() }
            ),
            stats = UserProfileStats(userId = userId),
            isLoading = false,
            isAccountDeleted = profileResult.errorText()
                ?.contains("User profile not found", ignoreCase = true) == true,
            errorMessage = if (profileResult.errorText()?.contains("PERMISSION_DENIED", true) == true) {
                profileResult.errorText()
            } else null
        )
    }
    // A viewed profile must appear as soon as its document and photo URL arrive.
    // Match totals are refreshed separately after this state is displayed.
    val storedStats = useCases.getUserProfileStats(userId).successData() ?: profile.toFallbackStats()
    return ProfileUiState(
        profile = profile.copy(
            name = profile.name.takeIf { it.isNotBlank() }
                ?: fallbackName.ifBlank { "Player" },
            profilePhotoUrl = profile.profilePhotoUrl
                ?.takeIf { it.isNotBlank() }
                ?: fallbackPhotoUrl.takeIf { it.isNotBlank() }
        ),
        stats = storedStats,
        isLoading = false
    )
}

private suspend fun loadLivePlayerStats(userId: String, stored: UserProfileStats): UserProfileStats = runCatching {
    val firestore = FirebaseFirestore.getInstance()
    val indexedTeamDocuments = firestore.collection("teams")
        .whereArrayContains("memberIds", userId)
        .get()
        .await()
        .documents
    // Teams created before memberIds was added only contain members[].  Include
    // those documents too; otherwise phone-auth players from those teams cannot
    // be linked to their scorecard rows.
    val legacyTeamDocuments = firestore.collection("teams")
        .get()
        .await()
        .documents
        .filter { team ->
            (team.get("members") as? List<*>)
                .orEmpty()
                .filterIsInstance<Map<*, *>>()
                .any { it["userId"] == userId }
        }
    val teamIds = (indexedTeamDocuments + legacyTeamDocuments).mapTo(linkedSetOf()) { it.id }
    val playerIds = teamIds.mapTo(linkedSetOf()) { "$it::$userId" }.apply { add(userId) }
    val allMatches = firestore.collection("matches").get().await().documents
    val relevantMatches = allMatches.filter { match ->
        val teamA = (match.get("teamA") as? Map<*, *>)?.get("teamId") as? String ?: match.getString("teamAId")
        val teamB = (match.get("teamB") as? Map<*, *>)?.get("teamId") as? String ?: match.getString("teamBId")
        val lineupIds = (match.get("playingXI") as? Map<*, *>)?.values.orEmpty()
            .flatMap { ((it as? Map<*, *>)?.get("playerIds") as? List<*>).orEmpty().filterIsInstance<String>() }
        val squadPlayerIds = (match.get("matchSquads") as? Map<*, *>)?.values.orEmpty()
            .flatMap { ((it as? Map<*, *>)?.get("players") as? List<*>).orEmpty() }
            .filterIsInstance<Map<*, *>>()
            .mapNotNull { it["userId"] as? String }
        listOfNotNull(teamA, teamB).any { it in teamIds } ||
            lineupIds.any { it in playerIds } || squadPlayerIds.any { it == userId }
    }

    val scorecardRows = coroutineScope {
        relevantMatches.map { match ->
            async {
                val scorecard = match.reference.collection("scorecard")
                val savedBatting = scorecard.document("batting").collection("entries").get().await().documents
                    .map { it.data.orEmpty() }.filter { it["playerId"] in playerIds }
                val savedBowling = scorecard.document("bowling").collection("entries").get().await().documents
                    .map { it.data.orEmpty() }.filter { it["playerId"] in playerIds }
                // Some completed matches were recorded before scorecard entries
                // were synced.  Their deliveries are still the source used by
                // ScorecardActivity, so derive a player row from them as fallback.
                val deliveries = if (savedBatting.isEmpty() && savedBowling.isEmpty()) {
                    match.reference.collection("deliveries").get().await().documents
                        .mapNotNull { it.data }
                        .filter { delivery ->
                            delivery["eventType"] != "REVERSAL" &&
                                delivery["reversedEventId"] !is String
                        }
                } else {
                    emptyList()
                }
                val batting = savedBatting.ifEmpty {
                    deliveries.filter { it["batsmanId"] in playerIds }
                        .groupBy { "${it["inningsId"]}:${it["batsmanId"]}" }
                        .values
                        .map { balls ->
                            mapOf(
                                "runs" to balls.sumOf { (it["runs"] as? Number)?.toInt() ?: 0 },
                                "balls" to balls.count { it["extraType"] != "WIDE" }
                            )
                        }
                }
                val bowling = savedBowling.ifEmpty {
                    deliveries.filter { it["bowlerId"] in playerIds }
                        .groupBy { "${it["inningsId"]}:${it["bowlerId"]}" }
                        .values
                        .map { balls ->
                            mapOf(
                                "runs" to balls.sumOf { (it["runs"] as? Number)?.toInt() ?: 0 },
                                "wickets" to balls.count { ball ->
                                    (ball["dismissedPlayerId"] as? String).orEmpty().isNotBlank() &&
                                        (ball["dismissalType"] as? String) !in setOf("", "NONE", "RUN_OUT", "RETIRED_OUT", "RETIRED_HURT")
                                }
                            )
                        }
                }
                Triple(match, batting, bowling)
            }
        }.awaitAll()
    }
    val playedMatches = scorecardRows.filter { (_, batting, bowling) -> batting.isNotEmpty() || bowling.isNotEmpty() }
    fun number(row: Map<String, Any?>, key: String) = (row[key] as? Number)?.toInt() ?: 0
    fun matchResult(match: DocumentSnapshot): Int? {
        if ((match.getString("status") ?: "").uppercase() !in setOf("COMPLETED", "FINISHED")) return null
        val scores = (match.get("innings") as? List<*>).orEmpty()
            .filterIsInstance<Map<*, *>>()
            .mapNotNull { inning ->
                val teamId = inning["battingTeamId"] as? String ?: return@mapNotNull null
                teamId to ((inning["score"] as? Number)?.toInt() ?: 0)
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, innings) -> innings.sum() }
        if (scores.size < 2) return null
        val teamA = (match.get("teamA") as? Map<*, *>)?.get("teamId") as? String ?: match.getString("teamAId")
        val teamB = (match.get("teamB") as? Map<*, *>)?.get("teamId") as? String ?: match.getString("teamBId")
        val ownTeam = listOfNotNull(teamA, teamB).firstOrNull { it in teamIds } ?: return null
        val otherTeam = listOfNotNull(teamA, teamB).firstOrNull { it != ownTeam } ?: return null
        return (scores[ownTeam] ?: return null).compareTo(scores[otherTeam] ?: return null)
    }
    val allBattingRows = playedMatches.flatMap { it.second }
    val bestBattingRow = allBattingRows.maxByOrNull { number(it, "runs") }
    val bestBatting = bestBattingRow?.let {
        number(it, "runs").toString() + if ((it["status"] as? String).equals("NOT_OUT", true)) "*" else ""
    }.orEmpty()
    val bestBowling = playedMatches.flatMap { it.third }
        .maxWithOrNull(compareBy<Map<String, Any?>> { number(it, "wickets") }.thenByDescending { number(it, "runs") })
        ?.let { "${number(it, "wickets")}/${number(it, "runs")}" }
        .orEmpty()
    val wins = playedMatches.count { matchResult(it.first)?.let { result -> result > 0 } == true }

    stored.copy(
        matchesPlayed = if (playedMatches.isNotEmpty()) playedMatches.size else stored.matchesPlayed,
        wins = if (playedMatches.isNotEmpty()) wins else stored.wins,
        bestScore = bestBatting.ifBlank { stored.bestScore },
        bestBowling = bestBowling.ifBlank { stored.bestBowling }
    )
}.getOrDefault(stored)

private fun User?.toFallbackProfile(): UserProfile {
    return if (this == null) {
        fallbackProfile()
    } else {
        UserProfile(
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
}

private fun User.toPendingProfile(): PendingUserProfile {
    return PendingUserProfile(
        id = id,
        name = name,
        email = email,
        phoneNumber = mobileNumber,
        profilePhotoUrl = profilePhotoUrl,
        authProvider = authProvider,
        isEmailVerified = isEmailVerified,
        isPhoneVerified = isPhoneVerified
    )
}

private fun fallbackProfile(): UserProfile {
    return UserProfile(
        id = "",
        name = "SportsXtreme Player",
        email = "",
        phoneNumber = ""
    )
}

private fun UserProfile.toFallbackStats(): UserProfileStats {
    return UserProfileStats(
        userId = id,
        matchesPlayed = matchesPlayed,
        wins = wins,
        losses = (matchesPlayed - wins).coerceAtLeast(0),
        bestScore = bestScore,
        trophies = trophies,
        topPerformerStreak = topPerformerStreak
    )
}

private fun <T> Resource<T>.successData(): T? {
    return if (this is Resource.Success) data else null
}

private fun <T> Resource<T>.errorText(): String? {
    return if (this is Resource.Error) message else null
}

@Composable
private fun ProfileBackdrop() {
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A151B), XtremeBg, Color(0xFF020609))
                )
            )
            drawRect(
                Brush.radialGradient(
                    colors = listOf(Color(0x2545E9FF), Color(0x1045E9FF), Color.Transparent),
                    center = Offset(size.width * 0.08f, size.height * 0.12f),
                    radius = size.width * 0.85f
                )
            )
            drawRect(
                Brush.radialGradient(
                    colors = listOf(Color(0x20C7FF1A), Color(0x0CC7FF1A), Color.Transparent),
                    center = Offset(size.width * 0.94f, size.height * 0.58f),
                    radius = size.width * 0.72f
                )
            )
        }
    }
}

@Composable
private fun ProfileTopBar(onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TopIcon("<", onBack)
        Text(
            "Profile",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        )
    }
}

@Composable
private fun TopIcon(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(0x9910191F))
            .border(1.dp, Color(0xFF33424A), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Platinum, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun HeroProfileCard(
    profile: UserProfile,
    stats: UserProfileStats,
    profilePhotoPreviewUri: Uri?,
    profilePhotoRefreshKey: Long,
    editEnabled: Boolean,
    onEditAvatar: () -> Unit,
    onEditName: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(24.dp), clip = false)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF28312B), Color(0xFF061016), Color(0xFF101C22))
                )
            )
            .border(1.dp, Color(0xCCFFD66B), RoundedCornerShape(24.dp))
            .padding(horizontal = 22.dp, vertical = 24.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                Brush.verticalGradient(
                    listOf(Color(0x35FFD66B), Color(0x1645E9FF), Color.Transparent),
                    startY = 0f,
                    endY = size.height * 0.68f
                )
            )
            drawCircle(Color(0x25FFD66B), radius = size.minDimension * 0.42f, center = Offset(size.width * 0.5f, size.height * 0.36f))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("SPORTSXTREME PLAYER PASS", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(15.dp))
            Avatar(
                photoUrl = profile.profilePhotoUrl,
                previewUri = profilePhotoPreviewUri,
                profilePhotoRefreshKey = profilePhotoRefreshKey,
                editEnabled = editEnabled,
                onEditAvatar = onEditAvatar
            )
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(profile.name, color = Platinum, fontSize = 27.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                if (editEnabled) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Lime)
                            .clickable(onClick = onEditName),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.baseline_edit_24),
                            contentDescription = "Change name",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Text(
                listOf(profile.location, profile.joinedLabel)
                    .filter { it.isNotBlank() }
                    .joinToString("  |  ")
                    .ifBlank { "Location not added" },
                color = SoftText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill("Verified Player", Lime, Color.Black)
                Pill(profile.role.ifBlank { "Player" }, Color(0xFF18252B), Platinum)
            }
            Spacer(Modifier.height(17.dp))
            PlayerUidRow(profile.id)
            Spacer(Modifier.height(21.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroMetric(stats.matchesPlayed.toString(), "Played", Lime, Modifier.weight(1f))
                HeroMetric(stats.wins.toString(), "Wins", Gold, Modifier.weight(1f))
                val isBowler = profile.role.contains("bowl", ignoreCase = true)
                val roleBest = if (isBowler) stats.bestBowling else stats.bestScore
                HeroMetric(roleBest.ifBlank { "—" }, "Best", Aqua, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Avatar(
    photoUrl: String?,
    previewUri: Uri?,
    profilePhotoRefreshKey: Long,
    editEnabled: Boolean,
    onEditAvatar: () -> Unit
) {
    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(124.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = Color(0xFF168DFF),
                    spotColor = Color(0xFF168DFF)
                )
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFFEAF2F4), Color(0xFF576369), Color(0xFF11171B)))),
            contentAlignment = Alignment.Center
        ) {
            val originalPhotoUrl = photoUrl?.trim()?.takeIf { it.isNotBlank() }
            var useOriginalPhotoUrl by remember(originalPhotoUrl, profilePhotoRefreshKey) { mutableStateOf(false) }
            var hasPhotoLoadFailed by remember(originalPhotoUrl, profilePhotoRefreshKey) { mutableStateOf(false) }
            val photoModel: Any? = previewUri ?: originalPhotoUrl?.let { sourceUrl ->
                val deliveryUrl = if (useOriginalPhotoUrl) sourceUrl else sourceUrl.toProfilePhotoDeliveryUrl()
                deliveryUrl.withProfilePhotoRefreshKey(profilePhotoRefreshKey)
            }
            if (photoModel == null || hasPhotoLoadFailed) {
                Canvas(Modifier.size(104.dp)) {
                    drawCircle(Color(0xFFE9F2F4), radius = size.minDimension * 0.22f, center = center.copy(y = size.height * 0.28f))
                    drawCircle(Color(0xFF26323B), radius = size.minDimension * 0.31f, center = center.copy(y = size.height * 0.7f))
                    drawArc(Color(0xFFE93B38), 205f, 130f, false, style = Stroke(width = 10f, cap = StrokeCap.Round))
                    drawArc(Lime, 18f, 68f, false, style = Stroke(width = 7f, cap = StrokeCap.Round))
                    drawLine(Gold, Offset(size.width * 0.23f, size.height * 0.86f), Offset(size.width * 0.8f, size.height * 0.36f), strokeWidth = 6f, cap = StrokeCap.Round)
                }
            } else {
                AsyncImage(
                    model = photoModel,
                    contentDescription = "Profile photo",
                    contentScale = ContentScale.Crop,
                    onError = { error ->
                        if (!useOriginalPhotoUrl && originalPhotoUrl != null) {
                            useOriginalPhotoUrl = true
                        } else {
                            Log.w("ProfileActivity", "Could not load profile photo", error.result.throwable)
                            hasPhotoLoadFailed = true
                        }
                    },
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            }
            if (editEnabled && previewUri != null) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0x88010407), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(42.dp),
                        color = Lime,
                        trackColor = Color(0x668A969B),
                        strokeWidth = 4.dp
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (editEnabled) Lime else SoftText)
                .border(2.dp, XtremeBg, CircleShape)
                .clickable(enabled = editEnabled, onClick = onEditAvatar),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.baseline_edit_24),
                contentDescription = "Change profile photo",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun String.withProfilePhotoRefreshKey(refreshKey: Long): String {
    if (refreshKey == 0L) return this
    return "$this${if (contains("?")) "&" else "?"}profile_updated=$refreshKey"
}

private fun String.toProfilePhotoDeliveryUrl(): String {
    val normalizedUrl = trim()
        .replaceFirst("http://", "https://", ignoreCase = true)
        .let { if (it.startsWith("//")) "https:$it" else it }
    val transformedUploadPath = "/image/upload/c_fill,g_auto,h_512,w_512,f_jpg,q_auto/"
    if (normalizedUrl.contains(transformedUploadPath, ignoreCase = true)) return normalizedUrl
    return normalizedUrl.replace(
        oldValue = "/image/upload/",
        newValue = transformedUploadPath,
        ignoreCase = true
    )
}

private fun String.isGoogleAccountPhotoUrl(): Boolean {
    val host = runCatching { Uri.parse(this).host.orEmpty() }.getOrDefault("").lowercase()
    return host == "googleusercontent.com" || host.endsWith(".googleusercontent.com")
}

@Composable
private fun Pill(text: String, bg: Color, fg: Color) {
    Text(
        text,
        color = fg,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .border(1.dp, Color.White.copy(alpha = 0.11f), RoundedCornerShape(50.dp))
            .padding(horizontal = 14.dp, vertical = 7.dp)
    )
}

@Composable
private fun PlayerUidRow(userId: String) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0xB8070C10))
            .border(1.dp, Color(0xFF3D4B50), RoundedCornerShape(50.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("Player UID  ${userId.takeLast(7).ifBlank { "Pending" }}", color = Platinum, fontSize = 12.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.width(8.dp))
        CopyIcon()
    }
}

@Composable
private fun CopyIcon() {
    Canvas(Modifier.size(14.dp)) {
        val stroke = 1.6f
        drawRoundRect(
            color = SoftText,
            topLeft = Offset(size.width * 0.16f, size.height * 0.05f),
            size = Size(size.width * 0.58f, size.height * 0.58f),
            cornerRadius = CornerRadius(2.5f, 2.5f),
            style = Stroke(width = stroke)
        )
        drawRoundRect(
            color = Lime,
            topLeft = Offset(size.width * 0.34f, size.height * 0.28f),
            size = Size(size.width * 0.58f, size.height * 0.62f),
            cornerRadius = CornerRadius(2.5f, 2.5f),
            style = Stroke(width = stroke)
        )
    }
}

@Composable
private fun HeroMetric(value: String, label: String, accent: Color, modifier: Modifier, showDot: Boolean = false) {
    Column(
        modifier
            .shadow(10.dp, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF16242B), DeepPanel)))
            .border(1.dp, accent.copy(alpha = 0.64f), RoundedCornerShape(14.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showDot) {
            Box(Modifier.size(18.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.height(4.dp))
        }
        Text(value, color = accent, fontSize = if (showDot) 13.sp else 23.sp, fontWeight = FontWeight.Black, maxLines = 1)
        Text(label, color = SoftText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HostedSummary(profile: UserProfile) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF101A1F), Color(0xFF071014))))
            .border(1.dp, Color(0x5545E9FF), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(5.dp).height(22.dp).clip(RoundedCornerShape(8.dp)).background(Brush.verticalGradient(listOf(Gold, Aqua))))
            Spacer(Modifier.width(10.dp))
            Text("Hosted", color = Platinum, fontSize = 17.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HostedTile(profile.hostedTournaments.toString(), "Tournament", Gold, Modifier.weight(1f))
            HostedTile(profile.hostedSeries.toString(), "Series", Aqua, Modifier.weight(1f))
            HostedTile(profile.hostedMatches.toString(), "Matches", Lime, Modifier.weight(1f))
        }
    }
}

@Composable
private fun HostedTile(value: String, label: String, accent: Color, modifier: Modifier) {
    Column(
        modifier
            .shadow(10.dp, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF17262C), DeepPanel)))
            .border(1.dp, accent.copy(alpha = 0.48f), RoundedCornerShape(14.dp))
            .padding(vertical = 15.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = accent, fontSize = 24.sp, fontWeight = FontWeight.Black, maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text(label, color = SoftText, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1)
    }
}

@Composable
private fun ProfileStats(profile: UserProfile, onShowQr: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatTile(profile.followers.toString(), "Followers", Lime, Modifier.weight(1f), comingSoon = true)
        StatTile(profile.following.toString(), "Following", Aqua, Modifier.weight(1f), comingSoon = true)
        StatTile("Profile", "QR", Gold, Modifier.weight(1f), showQr = true, onClick = onShowQr, qrValue = profileDeepLink(profile.id))
    }
}

@Composable
private fun StatTile(value: String, label: String, accent: Color, modifier: Modifier, showQr: Boolean = false, comingSoon: Boolean = false, onClick: (() -> Unit)? = null, qrValue: String? = null) {
    val qrBitmap = remember(showQr, qrValue) { if (showQr && !qrValue.isNullOrBlank()) profileQrBitmap(qrValue) else null }
    Column(
        modifier
            .shadow(12.dp, RoundedCornerShape(15.dp), clip = false)
            .clip(RoundedCornerShape(15.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF142229), Card)))
            .border(1.dp, accent.copy(alpha = 0.42f), RoundedCornerShape(15.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 15.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showQr) {
            if (qrBitmap != null) Image(bitmap = qrBitmap, contentDescription = "Profile QR", modifier = Modifier.size(42.dp))
            else Text("QR", color = Platinum, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(6.dp))
        } else {
            Text(value, color = Platinum, fontSize = 22.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Spacer(Modifier.height(4.dp))
        }
        Text(if (showQr) "$value $label" else label, color = SoftText, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1)
        if (comingSoon) {
            Spacer(Modifier.height(7.dp))
            Text(
                "Coming soon",
                color = accent,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(accent.copy(alpha = 0.12f))
                    .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp),
                maxLines = 1
            )
        }
    }
}

private fun profileDeepLink(userId: String): String =
    "https://sportsxtreme-95fbb.web.app/profile?userId=$userId"

private fun profileQrBitmap(value: String): ImageBitmap? = runCatching {
    val matrix = QRCodeWriter().encode(
        value,
        BarcodeFormat.QR_CODE,
        512,
        512,
        mapOf(EncodeHintType.MARGIN to 1)
    )
    Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888).apply {
        for (y in 0 until matrix.height) for (x in 0 until matrix.width) {
            setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }.asImageBitmap()
}.getOrNull()

@Composable
private fun ProfilePhotoAction(
    icon: Int,
    label: String,
    tint: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tint),
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(label, color = if (enabled) Platinum else SoftText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionTitle(title: String, onEdit: (() -> Unit)? = null, editEnabled: Boolean = true) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(5.dp).height(24.dp).clip(RoundedCornerShape(8.dp)).background(Brush.verticalGradient(listOf(Gold, Aqua))))
        Spacer(Modifier.width(10.dp))
        Text(title, color = Platinum, fontSize = 17.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
        if (onEdit != null) {
            Text(
                "Edit",
                color = if (editEnabled) Color.Black else Color(0xFF59656A),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (editEnabled) Lime else Color(0xFF1B252A))
                    .clickable(enabled = editEnabled) { onEdit() }
                    .padding(horizontal = 17.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun DetailGrid(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        items.forEach { (label, value) ->
            DetailCard(label, value)
        }
    }
}

@Composable
private fun DetailCard(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(15.dp), clip = false)
            .clip(RoundedCornerShape(15.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF121E24), Card)))
            .border(1.dp, Line.copy(alpha = 0.9f), RoundedCornerShape(15.dp))
            .padding(17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(5.dp).height(46.dp).clip(RoundedCornerShape(6.dp)).background(Brush.verticalGradient(listOf(Gold, Aqua))))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = SoftText, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Text(value, color = Platinum, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SettingsCard(
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onPrivacyPolicy: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF10191D), Color(0xFF080F13))))
            .border(1.dp, Color(0xFF344249), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().height(34.dp).clickable(onClick = onPrivacyPolicy),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Privacy & Data", color = Lime, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth().height(34.dp).clickable(onClick = onLogout),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Logout", color = Color(0xFFFF8D8D), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth().height(34.dp).clickable(onClick = onDeleteAccount),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Delete Account", color = Color(0xFFFF6B6B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProfileSectionEditSheet(
    section: ProfileEditSection,
    draftProfile: UserProfile,
    isSaving: Boolean,
    onProfileChange: (UserProfile) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    val inputs = when (section) {
        ProfileEditSection.PersonalDetails -> personalDetailChoiceInputs(draftProfile)
        ProfileEditSection.PlayingProfile -> playingProfileInputs(draftProfile)
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(section.title, color = Platinum, fontSize = 18.sp, fontWeight = FontWeight.Black)
        if (section == ProfileEditSection.PersonalDetails) {
            ProfileTextInput(
                label = "Mobile Number",
                value = draftProfile.phoneNumber,
                keyboardType = KeyboardType.Phone,
                enabled = !isSaving,
                onValueChange = { onProfileChange(draftProfile.copy(phoneNumber = it)) }
            )
            ProfileTextInput(
                label = "Email Address",
                value = draftProfile.email,
                keyboardType = KeyboardType.Email,
                enabled = !isSaving,
                onValueChange = { onProfileChange(draftProfile.copy(email = it)) }
            )
        }
        inputs.forEach { input ->
            ChoiceInputGroup(
                input = input,
                enabled = !isSaving,
                onSelect = { value -> onProfileChange(input.updateProfile(draftProfile, value)) }
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SheetActionButton(
                text = "Cancel",
                enabled = !isSaving,
                modifier = Modifier.weight(1f),
                background = Color(0xFF172228),
                foreground = Platinum,
                onClick = onCancel
            )
            SheetActionButton(
                text = if (isSaving) "Saving..." else "Save",
                enabled = !isSaving,
                modifier = Modifier.weight(1f),
                background = Lime,
                foreground = Color.Black,
                onClick = onSave
            )
        }
    }
}

@Composable
private fun ProfileTextInput(
    label: String,
    value: String,
    keyboardType: KeyboardType,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ChoiceInputGroup(input: ProfileChoiceInput, enabled: Boolean, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(input.label, color = SoftText, fontSize = 12.sp, fontWeight = FontWeight.Black)
        input.options.forEachIndexed { index, option ->
            ChoiceRow(
                number = index + 1,
                option = option,
                selected = option == input.currentValue,
                enabled = enabled,
                onClick = { onSelect(option) }
            )
        }
    }
}

@Composable
private fun ChoiceRow(
    number: Int,
    option: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0x332BFF88) else Color(0xFF10191E))
            .border(1.dp, if (selected) Lime else Line, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$number.", color = if (selected) Lime else Gold, fontSize = 13.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.width(10.dp))
        Text(option, color = Platinum, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        if (selected) {
            Text("Selected", color = Lime, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun SheetActionButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier,
    background: Color,
    foreground: Color,
    onClick: () -> Unit
) {
    Box(
        modifier
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) background else Color(0xFF263138))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (enabled) foreground else SoftText, fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
}

private fun personalDetailChoiceInputs(profile: UserProfile): List<ProfileChoiceInput> {
    return listOf(
        ProfileChoiceInput(
            label = "Gender",
            options = listOf("Male", "Female", "Hinjida"),
            currentValue = profile.gender,
            updateProfile = { currentProfile, value -> currentProfile.copy(gender = value) }
        )
    )
}

private fun playingProfileInputs(profile: UserProfile): List<ProfileChoiceInput> {
    return listOf(
        ProfileChoiceInput(
            label = "Role",
            options = listOf("BatsMan", "Bowler", "All Rounder"),
            currentValue = profile.role,
            updateProfile = { currentProfile, value -> currentProfile.copy(role = value) }
        ),
        ProfileChoiceInput(
            label = "Batting",
            options = listOf("Right hand batter", "LeftHand Batter"),
            currentValue = profile.battingStyle,
            updateProfile = { currentProfile, value -> currentProfile.copy(battingStyle = value) }
        ),
        ProfileChoiceInput(
            label = "Bowling",
            options = listOf("Right hand bowler", "Left Hand Bowler"),
            currentValue = profile.bowlingStyle,
            updateProfile = { currentProfile, value -> currentProfile.copy(bowlingStyle = value) }
        )
    )
}
