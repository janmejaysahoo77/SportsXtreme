package com.example.sportsxtreme.presentation.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.sportsxtreme.domain.model.User
import com.example.sportsxtreme.domain.model.PendingUserProfile
import com.example.sportsxtreme.domain.model.UserProfile
import com.example.sportsxtreme.domain.model.UserProfileSettings
import com.example.sportsxtreme.domain.model.UserProfileStats
import com.example.sportsxtreme.domain.usecase.AuthUseCases
import kotlinx.coroutines.launch

private val XtremeBg = Color(0xFF010407)
private val Card = Color(0xFF0C1419)
private val Line = Color(0xFF26333A)
private val Lime = Color(0xFFC7FF1A)
private val Gold = Color(0xFFFFD66B)
private val Aqua = Color(0xFF45E9FF)
private val Platinum = Color(0xFFEAF2F4)
private val SoftText = Color(0xFFA8B5B9)

private data class ProfileUiState(
    val profile: UserProfile = fallbackProfile(),
    val stats: UserProfileStats = UserProfileStats(userId = ""),
    val settings: UserProfileSettings = UserProfileSettings(userId = ""),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
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

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthDependencies.initialize(applicationContext)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        setContent {
            ProfileScreen(
                useCases = AuthDependencies.authUseCases(),
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(useCases: AuthUseCases, onBack: () -> Unit) {
    var uiState by remember { mutableStateOf(ProfileUiState()) }
    var activeEditSection by remember { mutableStateOf<ProfileEditSection?>(null) }
    var draftProfile by remember { mutableStateOf(uiState.profile) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    LaunchedEffect(Unit) {
        uiState = loadProfileUiState(useCases)
    }

    Surface(color = XtremeBg, modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            ProfileBackdrop()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileTopBar(onBack)
                if (uiState.isLoading || uiState.isSaving || uiState.infoMessage != null || uiState.errorMessage != null) {
                    ProfileStatusLine(uiState)
                }
                Spacer(Modifier.height(14.dp))
                HeroProfileCard(uiState.profile, uiState.stats)
                Spacer(Modifier.height(12.dp))
                HostedSummary(uiState.profile)
                Spacer(Modifier.height(14.dp))
                Text(
                    "Your Xtreme Presence",
                    color = Platinum,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                )
                ProfileStats(uiState.profile)
                SectionTitle(
                    title = "Personal Details",
                    onEdit = {
                        draftProfile = uiState.profile
                        activeEditSection = ProfileEditSection.PersonalDetails
                    },
                    editEnabled = !uiState.isLoading && !uiState.isSaving
                )
                DetailGrid(
                    listOf(
                        "Mobile Number" to uiState.profile.phoneNumber.ifBlank { "Not added" },
                        "Gender" to uiState.profile.gender.ifBlank { "Not added" },
                        "Email Address" to uiState.profile.email.ifBlank { "Not added" }
                    )
                )
                SectionTitle(
                    title = "Playing Profile",
                    onEdit = {
                        draftProfile = uiState.profile
                        activeEditSection = ProfileEditSection.PlayingProfile
                    },
                    editEnabled = !uiState.isLoading && !uiState.isSaving
                )
                DetailGrid(
                    listOf(
                        "Role" to uiState.profile.role.ifBlank { "Not added" },
                        "Batting" to uiState.profile.battingStyle.ifBlank { "Not added" },
                        "Bowling" to uiState.profile.bowlingStyle.ifBlank { "Not added" }
                    )
                )
                Spacer(Modifier.height(18.dp))
                SettingsCard(uiState.settings)
                Text(
                    "Version 1.0.0",
                    color = SoftText,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(top = 12.dp, bottom = 10.dp)
                )
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

    val signedInUser = currentUser ?: return ProfileUiState(
        profile = fallbackProfile,
        stats = fallbackProfile.toFallbackStats(),
        settings = UserProfileSettings(userId = ""),
        isLoading = false,
        errorMessage = "Could not find logged-in user."
    )
    useCases.createOrUpdateUserProfile(signedInUser.toPendingProfile())

    val profileResult = useCases.getUserProfile(userId)
    val statsResult = useCases.getUserProfileStats(userId)
    val settingsResult = useCases.getUserProfileSettings(userId)

    val profile = profileResult.successData() ?: fallbackProfile
    val stats = statsResult.successData() ?: profile.toFallbackStats()
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
                    listOf(Color(0xFF071016), XtremeBg, Color(0xFF03080B))
                )
            )
            drawLine(Color(0x30FFD66B), Offset(0f, size.height * 0.11f), Offset(size.width, size.height * 0.06f), strokeWidth = 1.4f)
            drawLine(Color(0x2445E9FF), Offset(0f, size.height * 0.31f), Offset(size.width, size.height * 0.24f), strokeWidth = 1.2f)
            drawLine(Color(0x18EAF2F4), Offset(0f, size.height * 0.58f), Offset(size.width, size.height * 0.52f), strokeWidth = 1f)
            for (i in 0..7) {
                val y = size.height * (0.16f + i * 0.1f)
                drawLine(Color(0x08000000), Offset(0f, y), Offset(size.width, y - 26f), strokeWidth = 18f)
            }
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
private fun HeroProfileCard(profile: UserProfile, stats: UserProfileStats) {
    Box(
        Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(18.dp), clip = false)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1C2424), Color(0xFF071015), Color(0xFF10171B))
                )
            )
            .border(1.dp, Color(0x80FFD66B), RoundedCornerShape(18.dp))
            .padding(20.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                Brush.verticalGradient(
                    listOf(Color(0x22FFD66B), Color(0x1045E9FF), Color.Transparent),
                    startY = 0f,
                    endY = size.height * 0.56f
                )
            )
            drawLine(Gold.copy(alpha = 0.78f), Offset(size.width * 0.08f, size.height * 0.07f), Offset(size.width * 0.92f, size.height * 0.07f), strokeWidth = 2.5f)
            drawLine(Aqua.copy(alpha = 0.38f), Offset(size.width * 0.06f, size.height * 0.16f), Offset(size.width * 0.94f, size.height * 0.1f), strokeWidth = 1.8f)
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(Color(0x22EAF2F4), Color.Transparent)),
                topLeft = Offset(size.width * 0.03f, size.height * 0.78f),
                size = Size(size.width * 0.94f, 1.5f),
                cornerRadius = CornerRadius(1f, 1f)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("ELITE PLAYER CARD", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(12.dp))
            Avatar()
            Spacer(Modifier.height(14.dp))
            Text(profile.name, color = Platinum, fontSize = 23.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text(
                listOf(profile.location, profile.joinedLabel)
                    .filter { it.isNotBlank() }
                    .joinToString("  |  ")
                    .ifBlank { "Location not added" },
                color = SoftText,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill("Verified Player", Lime, Color.Black)
                Pill(profile.role, Color(0xFF18252B), Platinum)
            }
            Spacer(Modifier.height(15.dp))
            PlayerUidRow(profile.id)
            Spacer(Modifier.height(19.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroMetric(stats.matchesPlayed.toString(), "Played", Lime, Modifier.weight(1f))
                HeroMetric(stats.wins.toString(), "Wins", Gold, Modifier.weight(1f))
                HeroMetric(stats.bestScore, "Best", Aqua, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Avatar() {
    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF4A5456), Color(0xFF11171B))))
                .border(3.dp, Gold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(90.dp)) {
                drawCircle(Color(0xFFE9F2F4), radius = size.minDimension * 0.22f, center = center.copy(y = size.height * 0.28f))
                drawCircle(Color(0xFF26323B), radius = size.minDimension * 0.31f, center = center.copy(y = size.height * 0.7f))
                drawArc(Color(0xFFE93B38), 205f, 130f, false, style = Stroke(width = 10f, cap = StrokeCap.Round))
                drawArc(Lime, 18f, 68f, false, style = Stroke(width = 7f, cap = StrokeCap.Round))
                drawLine(Gold, Offset(size.width * 0.23f, size.height * 0.86f), Offset(size.width * 0.8f, size.height * 0.36f), strokeWidth = 6f, cap = StrokeCap.Round)
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(Brush.horizontalGradient(listOf(Platinum, Gold)))
                .border(1.dp, Color(0x66101416), RoundedCornerShape(30.dp))
                .padding(horizontal = 11.dp, vertical = 4.dp)
        ) {
            Text("PRO", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
    }
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
            .shadow(8.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF121D22), Color(0xFF071014))))
            .border(1.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showDot) {
            Box(Modifier.size(18.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.height(4.dp))
        }
        Text(value, color = accent, fontSize = if (showDot) 13.sp else 20.sp, fontWeight = FontWeight.Black, maxLines = 1)
        Text(label, color = SoftText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HostedSummary(profile: UserProfile) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            "Hosted",
            color = Platinum,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 10.dp)
        )
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
            .shadow(8.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF10191E), Card)))
            .border(1.dp, accent.copy(alpha = 0.36f), RoundedCornerShape(12.dp))
            .padding(vertical = 13.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = accent, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text(label, color = SoftText, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun ProfileStats(profile: UserProfile) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatTile(profile.followers.toString(), "Followers", Lime, Modifier.weight(1f))
        StatTile(profile.following.toString(), "Following", Aqua, Modifier.weight(1f))
        StatTile("Profile", "QR", Gold, Modifier.weight(1f), showQr = true)
    }
}

@Composable
private fun StatTile(value: String, label: String, accent: Color, modifier: Modifier, showQr: Boolean = false) {
    Column(
        modifier
            .shadow(9.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF111B20), Card)))
            .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
            .padding(vertical = 13.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showQr) {
            Image(
                painter = painterResource(id = R.drawable.img),
                contentDescription = "Profile QR",
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(6.dp))
        } else {
            Text(value, color = Platinum, fontSize = 19.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Spacer(Modifier.height(4.dp))
        }
        Text(if (showQr) "$value $label" else label, color = SoftText, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun SectionTitle(title: String, onEdit: (() -> Unit)? = null, editEnabled: Boolean = true) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(4.dp).height(20.dp).clip(RoundedCornerShape(8.dp)).background(Brush.verticalGradient(listOf(Gold, Aqua))))
        Spacer(Modifier.width(10.dp))
        Text(title, color = Platinum, fontSize = 15.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
        if (onEdit != null) {
            Text(
                "Edit",
                color = if (editEnabled) Color.Black else Color(0xFF59656A),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (editEnabled) Lime else Color(0xFF1B252A))
                    .clickable(enabled = editEnabled) { onEdit() }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            )
        }
    }
}

@Composable
private fun DetailGrid(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
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
            .shadow(8.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF10191E), Card)))
            .border(1.dp, Line.copy(alpha = 0.82f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(4.dp).height(42.dp).clip(RoundedCornerShape(6.dp)).background(Brush.verticalGradient(listOf(Gold, Aqua))))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = SoftText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Platinum, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SettingsCard(settings: UserProfileSettings) {
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF10191D), Color(0xFF080F13))))
            .border(1.dp, Color(0xFF344249), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        SettingsRow("Change Language", settings.language, Platinum)
        SettingsDivider()
        SettingsRow("Purchase History", ">", Platinum)
        SettingsDivider()
        SettingsRow("Logout", "", Color(0xFFFF8D8D))
    }
}

@Composable
private fun SettingsRow(title: String, value: String, titleColor: Color) {
    Row(Modifier.fillMaxWidth().height(34.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = titleColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(value, color = SoftText, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
    }
}

@Composable
private fun SettingsDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF202C34)))
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
            options = listOf("Right hand batter", "LeftHand Batter", "MiddleHand Batter"),
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
