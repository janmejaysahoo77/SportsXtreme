package com.example.sportsxtreme.presentation.team

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.presentation.ui.theme.*
import com.example.sportsxtreme.R
import coil3.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.example.sportsxtreme.data.remote.cloudinary.CloudinaryTournamentImageUploader
import com.example.sportsxtreme.domain.model.matchFormatDisplayName
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.widget.Toast
import com.google.firebase.functions.FirebaseFunctions
import java.util.UUID
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween

class TeamProfileActivity : ComponentActivity() {
    private var pendingTeamId: String = ""
    private var isUploadingTeamPhoto by mutableStateOf(false)
    private val teamPhotoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val imageUri = uri ?: return@registerForActivityResult
        val targetTeamId = pendingTeamId
        if (targetTeamId.isBlank()) return@registerForActivityResult
        isUploadingTeamPhoto = true
        lifecycleScope.launch {
            try {
                val uploadResult = CloudinaryTournamentImageUploader(applicationContext)
                    .upload(imageUri, UUID.randomUUID().toString(), "teamLogo")
                val logoUrl = uploadResult.getOrElse { error ->
                    isUploadingTeamPhoto = false
                    Toast.makeText(this@TeamProfileActivity, error.message ?: "Unable to upload team photo", Toast.LENGTH_LONG).show()
                    return@launch
                }
                FirebaseFunctions.getInstance("us-central1").getHttpsCallable("updateTeamProfile")
                    .call(mapOf("teamId" to targetTeamId, "changes" to mapOf("logoUrl" to logoUrl)))
                    .addOnSuccessListener {
                        isUploadingTeamPhoto = false
                        Toast.makeText(this@TeamProfileActivity, "Team photo updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { error ->
                        isUploadingTeamPhoto = false
                        Toast.makeText(this@TeamProfileActivity, error.message ?: "Unable to update team photo", Toast.LENGTH_LONG).show()
                    }
            } catch (error: Exception) {
                isUploadingTeamPhoto = false
                Toast.makeText(this@TeamProfileActivity, error.message ?: "Unable to upload team photo", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun pickTeamPhoto(teamId: String) {
        pendingTeamId = teamId
        teamPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = XtremeDarkBlueHex.toInt()
        window.navigationBarColor = XtremeDarkBlueHex.toInt()
        val teamId = intent.getStringExtra(EXTRA_TEAM_ID).orEmpty()
        setContent {
            TeamProfileScreen(
                teamId = teamId,
                onBack = ::finish,
                onDeleteTeam = { teamId ->
                    FirebaseFunctions.getInstance("us-central1").getHttpsCallable("deleteTeam")
                        .call(mapOf("teamId" to teamId))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Team deleted", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(this, error.message ?: "Unable to delete team", Toast.LENGTH_LONG).show()
                        }
                },
                onPickTeamPhoto = { pickTeamPhoto(teamId) },
                isUploadingTeamPhoto = isUploadingTeamPhoto,
                onSaveTeamName = { newName, onComplete ->
                    if (teamId.isNotBlank()) {
                        val changes = hashMapOf("teamName" to newName.trim())
                        val payload = hashMapOf<String, Any>(
                            "teamId" to teamId,
                            "teamName" to newName.trim(),
                            "changes" to changes
                        )
                        FirebaseFunctions.getInstance("us-central1").getHttpsCallable("updateTeamProfile")
                            .call(payload)
                            .addOnSuccessListener { Toast.makeText(this@TeamProfileActivity, "Team name updated", Toast.LENGTH_SHORT).show(); onComplete(true) }
                            .addOnFailureListener { error -> Toast.makeText(this@TeamProfileActivity, error.message ?: "Unable to update team name", Toast.LENGTH_LONG).show(); onComplete(false) }
                    } else onComplete(false)
                },
                onAddPlayer = { teamName ->
                    startActivity(
                        Intent(this, AddPlayerActivity::class.java)
                            .putExtra(ManagePlayersInsideTeamActivity.EXTRA_TEAM_ID, teamId)
                            .putExtra(ManagePlayersInsideTeamActivity.EXTRA_TEAM_NAME, teamName)
                    )
                },
                onShareTeam = { teamName ->
                    val link = "https://sportsxtreme-95fbb.web.app/team?teamId=${android.net.Uri.encode(teamId)}"
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "View $teamName on SportsXtreme:\n$link")
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share $teamName"))
                }
            )
        }
    }

    companion object {
        const val EXTRA_TEAM_ID = "com.example.sportsxtreme.extra.TEAM_ID"
    }
}

private val ProfileBackground = XtremeBgBlue
private val ProfileCard = XtremeCardBlue
private val ProfileStroke = Color(0xFF263A48)
private val ProfileAccent = Color(0xFFC9FF16)
private val ProfileMuted = Color(0xFF9BAAB4)
private val ProfileGlass = Color(0xCC101E28)

private data class TeamProfileData(
    val name: String = "Addd",
    val logoUrl: String = "",
    val teamCode: String = "",
    val location: String = "Bhubaneswar",
    val description: String = "",
    val founded: String = "",
    val homeGround: String = "",
    val captainName: String = "",
    val captainNumber: String = "",
    val coachManager: String = "",
    val teamMotto: String = ""
)

private data class EditableProfileField(val key: String, val label: String, val icon: String, val value: String, val iconRes: Int? = null)

private val TeamProfileTabOrder = listOf("Profile", "Matches", "Leaderboard", "Stats", "Members")

private data class TeamMemberData(
    val userId: String,
    val name: String,
    val teamRoles: List<String>,
    val playingRole: String,
    val profilePhotoUrl: String = "",
    val battingStyle: String = "",
    val bowlingStyle: String = "",
    val joinedAtEpochMs: Long
)

private data class MemberProfileData(
    val displayName: String,
    val playingRole: String,
    val profilePhotoUrl: String,
    val battingStyle: String,
    val bowlingStyle: String
)

private data class TeamMatchData(
    val id: String,
    val title: String,
    val format: String,
    val teamName: String,
    val teamId: String,
    val teamLogoUrl: String,
    val opponentName: String,
    val opponentId: String,
    val opponentLogoUrl: String,
    val status: String,
    val dateEpochMs: Long?,
    val matchTime: String,
    val venue: String,
    val teamScore: String,
    val opponentScore: String,
    val result: String?
)

@Composable
private fun TeamProfileScreen(
    teamId: String,
    onBack: () -> Unit,
    onDeleteTeam: (String) -> Unit,
    onPickTeamPhoto: () -> Unit,
    isUploadingTeamPhoto: Boolean,
    onSaveTeamName: (String, (Boolean) -> Unit) -> Unit,
    onAddPlayer: (String) -> Unit,
    onShareTeam: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { TeamProfileTabOrder.size })
    var team by remember(teamId) { mutableStateOf(TeamProfileData()) }
    var members by remember(teamId) { mutableStateOf<List<TeamMemberData>>(emptyList()) }
    var matches by remember(teamId) { mutableStateOf<List<TeamMatchData>>(emptyList()) }
    var teamLoading by remember(teamId) { mutableStateOf(true) }
    var membersLoading by remember(teamId) { mutableStateOf(true) }
    var matchesLoading by remember(teamId) { mutableStateOf(true) }
    var initialSkeletonFinished by remember(teamId) { mutableStateOf(false) }
    var canManageTeam by remember(teamId) { mutableStateOf(false) }
    var viewerRoles by remember(teamId) { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(teamId) {
        delay(650)
        initialSkeletonFinished = true
    }
    DisposableEffect(teamId) {
        val firestore = FirebaseFirestore.getInstance()
        var registration: ListenerRegistration? = null
        var teamARegistration: ListenerRegistration? = null
        var teamBRegistration: ListenerRegistration? = null
        val teamAMatches = mutableMapOf<String, TeamMatchData>()
        val teamBMatches = mutableMapOf<String, TeamMatchData>()
        var teamAMatchesReady = false
        var teamBMatchesReady = false

        fun publishMatches() {
            matches = (teamAMatches.values + teamBMatches.values)
                .distinctBy { it.id }
                .sortedByDescending { it.dateEpochMs ?: Long.MIN_VALUE }
        }

        fun loadMissingTeamLogos(match: TeamMatchData, target: MutableMap<String, TeamMatchData>) {
            listOf(match.teamId to match.teamLogoUrl, match.opponentId to match.opponentLogoUrl)
                .filter { (id, logo) -> id.isNotBlank() && logo.isBlank() }
                .forEach { (id, _) ->
                    firestore.collection("teams").document(id).get().addOnSuccessListener { document ->
                        if (!document.exists()) return@addOnSuccessListener
                        val logo = listOf("logoUrl", "teamLogoUrl", "photoUrl")
                            .firstNotNullOfOrNull { document.getString(it)?.takeIf(String::isNotBlank) }.orEmpty()
                        if (logo.isNotBlank()) {
                            target[match.id]?.let { current ->
                                target[match.id] = if (current.teamId == id) current.copy(teamLogoUrl = logo)
                                else if (current.opponentId == id) current.copy(opponentLogoUrl = logo) else current
                                publishMatches()
                            }
                        }
                    }
                }
        }

        if (teamId.isNotBlank()) {
            registration = firestore.collection("teams").document(teamId)
                .addSnapshotListener { document, _ ->
                    teamLoading = false
                    if (document != null && document.exists()) {
                        membersLoading = true
                        fun firstValue(vararg keys: String): String = keys
                            .asSequence()
                            .mapNotNull { document.get(it)?.toString() }
                            .firstOrNull { it.isNotBlank() }
                            .orEmpty()
                        team = TeamProfileData(
                            name = firstValue("teamName", "name").ifBlank { "Addd" },
                            logoUrl = firstValue("logoUrl", "teamLogoUrl", "photoUrl"),
                            teamCode = firstValue("teamCode", "code", "inviteCode").ifBlank { teamId },
                            location = firstValue("city", "cityTown", "location").ifBlank { "Bhubaneswar" },
                            description = firstValue("description", "about"),
                            founded = firstValue("founded"),
                            homeGround = firstValue("homeGround"),
                            captainName = firstValue("captainName", "captain", "teamCaptainName"),
                            captainNumber = firstValue("captainMobile", "captainNumber", "mobile", "phoneNumber", "number"),
                            coachManager = firstValue("coachManager"),
                            teamMotto = firstValue("teamMotto")
                        )

                        // `members` is only changed after a user has joined. Do not use
                        // invitations or placeholder player records for this screen.
                        val joinedMembers = (document.get("members") as? List<*>)
                            .orEmpty()
                            .mapNotNull { it as? Map<*, *> }
                            .mapNotNull { member ->
                                val userId = member["userId"] as? String ?: return@mapNotNull null
                                TeamMemberData(
                                    userId = userId,
                                    name = "",
                                    teamRoles = member.teamRoles(),
                                    playingRole = (member["playingRole"] as? String).orEmpty(),
                                    joinedAtEpochMs = (member["joinedAtEpochMs"] as? Number)?.toLong() ?: 0L
                                )
                            }
                            .distinctBy { it.userId }
                        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                        viewerRoles = joinedMembers
                            .firstOrNull { it.userId == currentUserId }
                            ?.teamRoles
                            .orEmpty()
                        canManageTeam = viewerRoles.any { it == "ADMIN" || it == "CAPTAIN" }

                        if (joinedMembers.isEmpty()) {
                            members = emptyList()
                            membersLoading = false
                        } else {
                            members = joinedMembers
                            fetchTeamMemberProfiles(teamId, joinedMembers) { profileByUserId ->
                                members = joinedMembers.map { member ->
                                    profileByUserId[member.userId]?.let { profile ->
                                        member.copy(
                                            name = profile.displayName,
                                            playingRole = member.playingRole.ifBlank { profile.playingRole },
                                            profilePhotoUrl = profile.profilePhotoUrl,
                                            battingStyle = profile.battingStyle,
                                            bowlingStyle = profile.bowlingStyle
                                        )
                                    } ?: member.copy(
                                        name = "Team member",
                                        playingRole = member.playingRole.ifBlank { "Player" }
                                    )
                                }.sortedBy { it.joinedAtEpochMs }
                                membersLoading = false
                            }
                        }
                    } else {
                        members = emptyList()
                        membersLoading = false
                    }
                }
            teamARegistration = firestore.collection("matches")
                .whereEqualTo("teamA.teamId", teamId)
                .addSnapshotListener { snapshot, _ ->
                    teamAMatchesReady = true
                    matchesLoading = !(teamAMatchesReady && teamBMatchesReady)
                    teamAMatches.clear()
                    snapshot?.documents.orEmpty().forEach { match ->
                        match.toTeamMatchData(teamId)?.let { teamAMatches[it.id] = it; loadMissingTeamLogos(it, teamAMatches) }
                    }
                    publishMatches()
                }
            teamBRegistration = firestore.collection("matches")
                .whereEqualTo("teamB.teamId", teamId)
                .addSnapshotListener { snapshot, _ ->
                    teamBMatchesReady = true
                    matchesLoading = !(teamAMatchesReady && teamBMatchesReady)
                    teamBMatches.clear()
                    snapshot?.documents.orEmpty().forEach { match ->
                        match.toTeamMatchData(teamId)?.let { teamBMatches[it.id] = it; loadMissingTeamLogos(it, teamBMatches) }
                    }
                    publishMatches()
                }
        } else {
            teamLoading = false
            membersLoading = false
            matchesLoading = false
        }
        onDispose {
            registration?.remove()
            teamARegistration?.remove()
            teamBRegistration?.remove()
        }
    }
    val screenHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp
    val minPagerHeight = (screenHeight - 114.dp).coerceAtLeast(400.dp)
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF070D12)).drawBehind {
            drawRect(
                Brush.radialGradient(
                    listOf(ProfileAccent.copy(alpha = 0.07f), Color.Transparent),
                    center = Offset(size.width * .5f, size.height * .10f),
                    radius = size.height * .72f
                )
            )
        }
    ) {
      Column(Modifier.fillMaxSize()) {
        TopBar(
            onBack = onBack,
            canAddPlayers = viewerRoles.any { it == "ADMIN" || it == "CAPTAIN" || it == "VICE_CAPTAIN" },
            onAddPlayer = { onAddPlayer(team.name) },
            onShare = { onShareTeam(team.name) },
            canDelete = viewerRoles.any { it == "ADMIN" },
            onDelete = { onDeleteTeam(teamId) }
        )
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            TeamHero(team, canManageTeam, isUploadingTeamPhoto, onPickTeamPhoto, onSaveTeamName)
            ProfileTabs(
                selectedTab = TeamProfileTabOrder.getOrElse(pagerState.targetPage) { "Profile" },
                onSelect = { tabName ->
                    val targetIndex = TeamProfileTabOrder.indexOf(tabName)
                    if (targetIndex >= 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(targetIndex)
                        }
                    }
                }
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minPagerHeight),
                verticalAlignment = Alignment.Top
            ) { page ->
                when (TeamProfileTabOrder.getOrElse(page) { "Profile" }) {
                    "Profile" -> if (!initialSkeletonFinished || teamLoading || matchesLoading) TeamTabSkeleton("Profile") else ProfileTabContent(
                        team = team,
                        matches = matches,
                        canEdit = canManageTeam,
                        onViewAllMatches = {
                            coroutineScope.launch {
                                val matchesIndex = TeamProfileTabOrder.indexOf("Matches")
                                if (matchesIndex >= 0) {
                                    pagerState.animateScrollToPage(matchesIndex)
                                }
                            }
                        },
                        onSave = { key, value ->
                            if (teamId.isNotBlank()) {
                                com.google.firebase.functions.FirebaseFunctions.getInstance()
                                    .getHttpsCallable("updateTeamProfile")
                                    .call(mapOf("teamId" to teamId, "changes" to mapOf(key to value.trim())))
                            }
                        }
                    )
                    "Matches" -> if (!initialSkeletonFinished || matchesLoading) TeamTabSkeleton("Matches") else MatchesTabContent(matches)
                    "Stats" -> if (!initialSkeletonFinished || matchesLoading) TeamTabSkeleton("Stats") else StatsTabContent(matches)
                    "Leaderboard" -> if (!initialSkeletonFinished || teamLoading) TeamTabSkeleton("Leaderboard") else LeaderboardTabContent(teamId)
                    "Members" -> if (!initialSkeletonFinished || teamLoading || membersLoading) TeamTabSkeleton("Members") else MembersTabContent(teamId, members, viewerRoles)
                }
            }
        }
      }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit, canAddPlayers: Boolean, onAddPlayer: () -> Unit, onShare: () -> Unit, canDelete: Boolean, onDelete: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("‹", color = Color.White, fontSize = 34.sp, modifier = Modifier.size(44.dp).clickable { onBack() })
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text("Team Profile", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        Image(
            painter = painterResource(R.drawable.ic_share),
            contentDescription = "Share team link",
            modifier = Modifier.size(40.dp).clickable(onClick = onShare).padding(8.dp)
        )
        if (canAddPlayers || canDelete) {
            Box {
                Text("⋮", color = Color.White, fontSize = 25.sp, modifier = Modifier.size(32.dp).clickable { menuExpanded = true }, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text("Add player") }, onClick = { menuExpanded = false; onAddPlayer() })
                    if (canDelete) DropdownMenuItem(text = { Text("Delete team", color = Color(0xFFD32F2F)) }, onClick = { menuExpanded = false; confirmDelete = true })
                }
            }
        } else Spacer(Modifier.width(32.dp))
    }
    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text("Delete team?") },
        text = { Text("This will permanently delete this team. This action cannot be undone.") },
        confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Delete", color = Color(0xFFD32F2F)) } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
    )
}

@Composable
private fun TeamHero(team: TeamProfileData, canEditPhoto: Boolean, isUploadingPhoto: Boolean, onPickTeamPhoto: () -> Unit, onSaveName: (String, (Boolean) -> Unit) -> Unit) {
    var editingName by remember { mutableStateOf(false) }
    var isSavingName by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                Modifier.size(132.dp)
                    .shadow(20.dp, CircleShape, clip = false, ambientColor = Color(0xFF168DFF), spotColor = Color(0xFF168DFF))
                    .clip(CircleShape)
                    .background(Color(0xFF080E11))
                    .border(1.dp, Color(0xFF33414A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (team.logoUrl.isNotBlank()) {
                    AsyncImage(model = team.logoUrl, contentDescription = "${team.name} logo", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Text(team.name.initials(), color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
                }
                if (isUploadingPhoto) {
                    Box(Modifier.fillMaxSize().background(Color(0x88010407), CircleShape), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(42.dp), color = ProfileAccent, trackColor = Color(0x668A969B), strokeWidth = 4.dp)
                    }
                }
            }
            if (canEditPhoto) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(ProfileAccent)
                        .border(2.dp, Color(0xFF070D12), CircleShape)
                        .clickable(enabled = !isUploadingPhoto, onClick = onPickTeamPhoto),
                    contentAlignment = Alignment.Center
                ) {
                    Image(painter = painterResource(R.drawable.baseline_edit_24), contentDescription = "Change team photo", modifier = Modifier.size(20.dp))
                }
            }
        }
        Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(team.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 14.dp))
            if (canEditPhoto) {
                Image(
                    painter = painterResource(R.drawable.baseline_edit_24),
                    contentDescription = "Edit team name",
                    colorFilter = ColorFilter.tint(Color(0xFF22C55E)),
                    modifier = Modifier.size(20.dp).clickable(enabled = !isSavingName) { editingName = true }
                )
            }
        }
        if (editingName) {
            ProfileFieldEditor(
                field = EditableProfileField("teamName", "Team name", "", team.name),
                onDismiss = { if (!isSavingName) editingName = false },
                isSaving = isSavingName
            ) { value ->
                val newName = value.trim()
                if (newName.isNotBlank() && newName.length <= 30 && newName != team.name) {
                    isSavingName = true
                    onSaveName(newName) { saved ->
                        isSavingName = false
                        if (saved) editingName = false
                    }
                } else if (newName.isNotBlank()) editingName = false
            }
        }
        val foundedDate = team.founded.trim()
        val locationAndFounded = listOf(team.location.trim(), foundedDate.takeIf { it.isNotBlank() }?.let { "Since $it" })
            .filterNotNull().filter { it.isNotBlank() }.joinToString("  ·  ")
        if (locationAndFounded.isNotBlank()) {
            Text("⌖  $locationAndFounded", color = ProfileMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp))
        }
        team.teamMotto.trim().takeIf { it.isNotBlank() }?.let { motto ->
            Text(motto, color = ProfileMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun ProfileTabs(selectedTab: String, onSelect: (String) -> Unit) {
    val tabs = listOf(
        Triple("Profile", R.drawable.whiteuser, ""),
        Triple("Matches", R.drawable.tick, ""),
        Triple("Leaderboard", R.drawable.tower, ""),
        Triple("Stats", R.drawable.statsicon, ""),
        Triple("Members", R.drawable.groups, "")
    )
    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        tabs.forEach { (tab, iconRes, iconText) ->
            Column(Modifier.weight(1f).height(72.dp).clip(RoundedCornerShape(14.dp)).background(if (tab == selectedTab) Color(0xFF10221A) else Color.Transparent).clickable { onSelect(tab) }.padding(horizontal = 2.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                if (iconRes != null) {
                    Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(25.dp))
                } else {
                    Text(iconText, color = if (tab == selectedTab) Color(0xFF07120B) else ProfileMuted, fontSize = 19.sp)
                }
                Text(tab, color = if (tab == selectedTab) Color.White else ProfileMuted, fontSize = if (tab == "Leaderboard") 9.sp else 11.sp, fontWeight = if (tab == selectedTab) FontWeight.Bold else FontWeight.Medium, maxLines = 1, softWrap = false)
                Box(Modifier.padding(top = 4.dp).width(34.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(if (tab == selectedTab) ProfileAccent else Color.Transparent))
            }
        }
    }
}

@Composable
private fun ProfileTabContent(team: TeamProfileData, matches: List<TeamMatchData>, canEdit: Boolean, onViewAllMatches: () -> Unit, onSave: (String, String) -> Unit) {
    Column(Modifier.padding(horizontal = 13.dp, vertical = 4.dp)) {
        AboutCard(team, canEdit, onSave)
        RecentMatchesSection(matches, onViewAllMatches)
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun AboutCard(team: TeamProfileData, canEdit: Boolean, onSave: (String, String) -> Unit) {
    Column(Modifier.fillMaxWidth().background(ProfileCard, RoundedCornerShape(20.dp)).border(1.dp, ProfileStroke, RoundedCornerShape(20.dp)).padding(20.dp)) {
        var editingField by remember { mutableStateOf<EditableProfileField?>(null) }
        var editMode by remember { mutableStateOf(false) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(5.dp).height(18.dp).background(ProfileAccent, RoundedCornerShape(4.dp)))
            Text("About the team", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 10.dp).weight(1f))
            if (canEdit) {
                Row(Modifier.border(1.dp, ProfileAccent, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).clickable { editMode = !editMode }.padding(horizontal = 11.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (editMode) "✓" else "✎", color = ProfileAccent, fontSize = 12.sp)
                    Text(if (editMode) " Done" else " Edit", color = ProfileAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text(team.description.ifBlank { "Add a description for your team." }, color = ProfileMuted, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 16.dp))
        if (editMode && canEdit) {
            Text("Tap a detail to edit it. Team Code stays locked.", color = ProfileMuted, fontSize = 9.sp, modifier = Modifier.padding(top = 9.dp))
            Row(Modifier.fillMaxWidth().clickable { editingField = EditableProfileField("description", "About the team", "", team.description) }.padding(top = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Edit description", color = ProfileAccent, fontSize = 10.sp, modifier = Modifier.weight(1f))
                EditButton { editingField = EditableProfileField("description", "About the team", "", team.description) }
            }
        }
        DetailsBox(team, canEdit && editMode) { editingField = it }
        editingField?.let { field ->
            ProfileFieldEditor(field, onDismiss = { editingField = null }) { value -> onSave(field.key, value); editingField = null }
        }
    }
}

@Composable
private fun DetailsBox(team: TeamProfileData, canEdit: Boolean, onEdit: (EditableProfileField) -> Unit) {
    val fields = listOf(
        EditableProfileField("captainName", "Team Captain", "", team.captainName, R.drawable.profileicon),
        EditableProfileField("founded", "Founded On", "", team.founded, R.drawable.claender),
        EditableProfileField("homeGround", "Home Ground", "", team.homeGround, R.drawable.stadiumicon),
        EditableProfileField("captainMobile", "Captain Number", "☎", team.captainNumber),
        EditableProfileField("coachManager", "Coach/Manager", "", team.coachManager, R.drawable.groups1),
        EditableProfileField("teamMotto", "Team Motto", "⚑", team.teamMotto)
    )
    Column(Modifier.fillMaxWidth().padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        fields.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                pair.forEach { field -> ProfileInfoTile(field, canEdit, onEdit, Modifier.weight(1f)) }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        TeamCodeTile(team.teamCode, Modifier.fillMaxWidth())
    }
}

@Composable
private fun TeamCodeTile(teamCode: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val displayedCode = teamCode.takeIf { it.isNotBlank() }?.takeLast(4) ?: "Unavailable"
    Row(
        modifier.height(82.dp).background(Color(0xFF101E28), RoundedCornerShape(14.dp))
            .border(1.dp, ProfileStroke, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("♜", color = ProfileAccent, fontSize = 15.sp)
                Text("Team Code", color = ProfileMuted, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp))
            }
            Text(displayedCode, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 7.dp))
        }
        Image(
            painter = painterResource(R.drawable.copyicon),
            contentDescription = "Copy team code",
            modifier = Modifier.size(36.dp).clickable(enabled = teamCode.isNotBlank()) {
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Team code", teamCode))
                Toast.makeText(context, "Team code copied", Toast.LENGTH_SHORT).show()
            }.padding(6.dp)
        )
    }
}

@Composable
private fun ProfileInfoTile(field: EditableProfileField, canEdit: Boolean, onEdit: (EditableProfileField) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.height(82.dp).background(Color(0xFF101E28), RoundedCornerShape(14.dp)).border(1.dp, ProfileStroke, RoundedCornerShape(14.dp)).clickable(enabled = canEdit) { onEdit(field) }.padding(horizontal = 13.dp, vertical = 11.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (field.iconRes != null) {
                Image(painter = painterResource(field.iconRes), contentDescription = null, modifier = Modifier.size(15.dp))
            } else {
                Text(field.icon, color = ProfileAccent, fontSize = 15.sp)
            }
            Text(field.label, color = ProfileMuted, fontSize = 10.sp, modifier = Modifier.weight(1f).padding(start = 8.dp))
            if (canEdit) EditButton { onEdit(field) }
        }
        Text(field.value.ifBlank { "Not set" }, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.padding(top = 7.dp))
    }
}

@Composable
private fun RecentMatchesSection(matches: List<TeamMatchData>, onViewAll: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("◷  Recent Matches", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("View All  ›", color = ProfileAccent, fontSize = 12.sp, modifier = Modifier.clickable(onClick = onViewAll).padding(8.dp))
        }
        val recent = matches.filter { it.status == "COMPLETED" || it.status == "ABANDONED" }.sortedByDescending { it.dateEpochMs ?: Long.MIN_VALUE }.take(3)
        if (recent.isEmpty()) EmptyTeamTab("No recent matches yet.") else recent.forEach { TeamMatchCard(it) }
    }
}

@Composable
private fun EditButton(onClick: () -> Unit) = Icon(
    painter = painterResource(R.drawable.baseline_edit_24),
    contentDescription = "Edit",
    tint = ProfileAccent,
    modifier = Modifier.size(18.dp).clickable(onClick = onClick)
)

@Composable
private fun ProfileFieldEditor(field: EditableProfileField, onDismiss: () -> Unit, isSaving: Boolean = false, onSave: (String) -> Unit) {
    var value by remember(field.key, field.value) { mutableStateOf(field.value) }
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Edit ${field.label}") },
        text = { OutlinedTextField(value = value, onValueChange = { input -> if (field.key != "teamName" || input.length <= 30) value = input }, label = { Text(field.label) }, singleLine = field.key != "description" && field.key != "teamMotto") },
        confirmButton = { TextButton(onClick = { onSave(value) }, enabled = !isSaving) { if (isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancel") } }
    )
}

private fun String.initials(): String {
    val words = split(Regex("\\s+")).filter { it.isNotBlank() }
    return if (words.size >= 2) words.take(2).joinToString("") { it.first().uppercase() }
    else words.firstOrNull()?.take(2)?.uppercase().orEmpty().ifBlank { "TM" }
}

private fun DocumentSnapshot.toTeamMatchData(teamId: String): TeamMatchData? {
    val teamA = get("teamA") as? Map<*, *> ?: return null
    val teamB = get("teamB") as? Map<*, *> ?: return null
    val isTeamA = teamA["teamId"] == teamId
    val ownTeam = if (isTeamA) teamA else teamB
    val opponent = if (isTeamA) teamB else teamA
    val innings = get("innings") as? List<*> ?: emptyList<Any>()
    val scores = innings.mapNotNull { it as? Map<*, *> }
        .groupBy { it["battingTeamId"] as? String }
        .mapValues { (_, entries) ->
            entries.sumOf { (it["score"] as? Number)?.toInt() ?: 0 } to
                entries.sumOf { (it["wickets"] as? Number)?.toInt() ?: 0 }
        }
    fun scoreFor(id: String): String = scores[id]?.let { "${it.first}/${it.second}" }.orEmpty()
    val ownId = ownTeam["teamId"] as? String ?: return null
    val opponentId = opponent["teamId"] as? String ?: return null
    val status = getString("status").orEmpty().ifBlank { "CREATED" }
    val result = if (status == "COMPLETED") {
        val ownScore = scores[ownId]?.first
        val opponentScore = scores[opponentId]?.first
        when {
            ownScore == null || opponentScore == null -> "Completed"
            ownScore > opponentScore -> "Won"
            ownScore < opponentScore -> "Lost"
            else -> "Tied"
        }
    } else null
    return TeamMatchData(
        id = id,
        title = getString("title").orEmpty().ifBlank { "Match" },
        format = getString("format").orEmpty(),
        teamName = (ownTeam["name"] as? String).orEmpty().ifBlank { "Your team" },
        teamId = ownId,
        teamLogoUrl = listOf("logoUrl", "teamLogoUrl", "photoUrl").firstNotNullOfOrNull { ownTeam[it] as? String }?.takeIf(String::isNotBlank).orEmpty(),
        opponentName = (opponent["name"] as? String).orEmpty().ifBlank { "Opponent" },
        opponentId = opponentId,
        opponentLogoUrl = listOf("logoUrl", "teamLogoUrl", "photoUrl").firstNotNullOfOrNull { opponent[it] as? String }?.takeIf(String::isNotBlank).orEmpty(),
        status = status,
        dateEpochMs = getLong("matchDateEpochMs"),
        matchTime = getString("matchTime").orEmpty(),
        venue = getString("venue").orEmpty(),
        teamScore = scoreFor(ownId),
        opponentScore = scoreFor(opponentId),
        result = result
    )
}

private fun TeamMatchData.dateLabel(): String = dateEpochMs?.let {
    java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM).format(java.util.Date(it))
}.orEmpty()

@Composable
private fun AchievementsCard() {
    Column(Modifier.fillMaxWidth().padding(top = 14.dp).background(ProfileCard, RoundedCornerShape(18.dp)).border(1.dp, ProfileStroke, RoundedCornerShape(18.dp)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Text("♕", color = ProfileAccent, fontSize = 23.sp); Text("Achievements", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 10.dp)) }
        Achievement("2 Tournament Wins")
        Achievement("3 Runners-up")
    }
}

@Composable
private fun Achievement(text: String) {
    Row(Modifier.padding(top = 13.dp), verticalAlignment = Alignment.CenterVertically) { Text("●", color = Color(0xFF88FF2E), fontSize = 16.sp); Text(text, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(start = 12.dp)) }
}

@Composable
private fun MatchesTabContent(matches: List<TeamMatchData>) {
    val filters = listOf("All", "Upcoming", "Live", "Completed")
    var selectedFilter by remember { mutableStateOf("All") }
    val live = matches.filter { it.status.uppercase() in LIVE_MATCH_STATUSES }
        .sortedByDescending { it.dateEpochMs ?: Long.MIN_VALUE }
    val completed = matches.filter { it.status.uppercase() in COMPLETED_MATCH_STATUSES }
        .sortedByDescending { it.dateEpochMs ?: Long.MIN_VALUE }
    val upcoming = matches.filter { it.status.uppercase() !in LIVE_MATCH_STATUSES + COMPLETED_MATCH_STATUSES }
        .sortedBy { it.dateEpochMs ?: Long.MAX_VALUE }

    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 10.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                val selected = selectedFilter == filter
                Text(
                    filter,
                    color = if (selected) Color(0xFF07120B) else ProfileMuted,
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.clip(CircleShape)
                        .background(if (selected) ProfileAccent else Color(0xFF101923))
                        .border(1.dp, if (selected) ProfileAccent else ProfileStroke, CircleShape)
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }

        if (selectedFilter == "All" || selectedFilter == "Upcoming") {
            MatchSectionHeader("Upcoming Matches", "Your next scheduled games") { selectedFilter = "Upcoming" }
            if (upcoming.isEmpty()) EmptyTeamTab("No upcoming matches scheduled.")
            else upcoming.forEach { FeaturedTeamMatchCard(it, live = false) }
        }
        if (selectedFilter == "All" || selectedFilter == "Live") {
            if (selectedFilter == "Live" || selectedFilter == "All" && live.isNotEmpty()) {
                MatchSectionHeader("Live Matches", "Games happening now") { selectedFilter = "Live" }
            }
            if (live.isEmpty() && selectedFilter == "Live") EmptyTeamTab("No live matches right now.")
            else live.forEach { FeaturedTeamMatchCard(it, live = true) }
        }
        if (selectedFilter == "All" || selectedFilter == "Completed") {
            MatchSectionHeader("Past Matches", "Recent results and performance") { selectedFilter = "Completed" }
            if (completed.isEmpty()) EmptyTeamTab("No completed matches yet.")
            else completed.forEach { PastTeamMatchCard(it) }
        }
        Spacer(Modifier.height(24.dp))
    }
}

private val LIVE_MATCH_STATUSES = setOf("LIVE", "IN_PROGRESS", "INNINGS_BREAK", "PAUSED", "MATCH_BREAK", "BREAK")
private val COMPLETED_MATCH_STATUSES = setOf("COMPLETED", "FINISHED", "ABANDONED")

@Composable
private fun MatchSectionHeader(title: String, subtitle: String, onViewAll: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = ProfileMuted, fontSize = 11.sp)
        }
        Text("View All  ›", color = ProfileAccent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onViewAll).padding(8.dp))
    }
}

@Composable
private fun FeaturedTeamMatchCard(match: TeamMatchData, live: Boolean) {
    Column(
        Modifier.fillMaxWidth().padding(bottom = 12.dp)
            .background(Color(0xFF09151B), RoundedCornerShape(18.dp))
            .border(1.dp, if (live) Color(0xFF55313A) else Color(0xFF203A37), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                match.format.takeIf { it.isNotBlank() }?.let { "${matchFormatDisplayName(it)} League" }
                    ?: match.title.takeIf { it.isNotBlank() && it != "Match" }
                    ?: "Cricket Match",
                color = ProfileMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                if (live) "● LIVE" else "UPCOMING",
                color = if (live) Color(0xFFFF6570) else ProfileAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(CircleShape)
                    .background(if (live) Color(0x332E151A) else Color(0x222AFF88))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                TeamBadge(match.teamName, match.teamLogoUrl, Modifier.size(52.dp))
                Text(match.teamName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.padding(top = 7.dp))
                if (live) Text(match.teamScore.ifBlank { "—" }, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.width(106.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (live) {
                    Text("LIVE", color = Color(0xFFFF6570), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Text("VS", color = ProfileAccent, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 4.dp))
                    Text("In progress", color = ProfileMuted, fontSize = 9.sp)
                } else {
                    Text("VS", color = ProfileAccent, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text(match.dateLabel().ifBlank { "Date to be announced" }, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(top = 7.dp))
                    if (match.matchTime.isNotBlank()) Text(match.matchTime, color = ProfileMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                TeamBadge(match.opponentName, match.opponentLogoUrl, Modifier.size(52.dp))
                Text(match.opponentName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.padding(top = 7.dp))
                if (live) Text(match.opponentScore.ifBlank { "—" }, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 10.dp)
                .border(1.dp, ProfileStroke, RoundedCornerShape(12.dp)).padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⌖  ${match.venue.ifBlank { "Venue to be announced" }}", color = ProfileMuted, fontSize = 10.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            if (!live) {
                Text("Match Details  ›", color = Color(0xFF07120B), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(CircleShape).background(ProfileAccent).padding(horizontal = 12.dp, vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun PastTeamMatchCard(match: TeamMatchData) {
    val won = match.result == "Won"
    val statusColor = if (won) Color(0xFF21C77A) else if (match.result == "Lost") Color(0xFFFF6464) else ProfileMuted
    Row(
        Modifier.fillMaxWidth().padding(bottom = 10.dp)
            .background(Color(0xFF101923), RoundedCornerShape(16.dp))
            .border(1.dp, ProfileStroke, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamBadge(match.teamName, match.teamLogoUrl, Modifier.size(38.dp))
        Column(Modifier.weight(1f).padding(start = 9.dp)) {
            Text(match.teamName, color = ProfileMuted, fontSize = 9.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text(match.teamScore.ifBlank { "—" }, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
            Text("VS", color = ProfileMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Box(Modifier.padding(top = 4.dp).width(1.dp).height(28.dp).background(ProfileStroke))
        }
        Column(Modifier.weight(1f)) {
            Text(match.opponentName, color = ProfileMuted, fontSize = 9.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text(match.opponentScore.ifBlank { "—" }, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        TeamBadge(match.opponentName, match.opponentLogoUrl, Modifier.size(38.dp))
        Column(Modifier.width(72.dp).padding(start = 8.dp), horizontalAlignment = Alignment.End) {
            Text(match.result ?: "Completed", color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(match.dateLabel(), color = ProfileMuted, fontSize = 8.sp, maxLines = 1)
            Text(match.venue, color = ProfileMuted, fontSize = 8.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun EmptyTeamTab(message: String) = Text(message, color = ProfileMuted, fontSize = 11.sp, modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)

@Composable
private fun TeamTabSkeleton(tab: String) {
    val transition = rememberInfiniteTransition(label = "team-tab-skeleton")
    val pulse by transition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(animation = tween(850), repeatMode = RepeatMode.Reverse),
        label = "team-tab-skeleton-pulse"
    )
    val placeholder = Color(0xFF9FB0BF).copy(alpha = .2f + pulse * .65f)
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
        when (tab) {
            "Profile" -> {
                SkeletonBlock(Modifier.width(150.dp).height(20.dp), placeholder)
                SkeletonBlock(Modifier.padding(top = 8.dp).width(220.dp).height(12.dp), placeholder)
                SkeletonBlock(Modifier.fillMaxWidth().padding(top = 16.dp).height(150.dp), placeholder, RoundedCornerShape(16.dp))
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(2) { SkeletonBlock(Modifier.weight(1f).height(84.dp), placeholder, RoundedCornerShape(12.dp)) }
                }
                SkeletonBlock(Modifier.fillMaxWidth().padding(top = 12.dp).height(118.dp), placeholder, RoundedCornerShape(14.dp))
            }
            "Matches" -> {
                SkeletonBlock(Modifier.fillMaxWidth(.55f).height(20.dp), placeholder)
                SkeletonBlock(Modifier.fillMaxWidth().padding(top = 12.dp).height(38.dp), placeholder, RoundedCornerShape(20.dp))
                repeat(3) { SkeletonBlock(Modifier.fillMaxWidth().padding(top = 12.dp).height(118.dp), placeholder, RoundedCornerShape(14.dp)) }
            }
            "Stats" -> {
                SkeletonBlock(Modifier.width(165.dp).height(20.dp), placeholder)
                SkeletonBlock(Modifier.width(230.dp).padding(top = 8.dp).height(12.dp), placeholder)
                repeat(4) {
                    Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        repeat(2) { SkeletonBlock(Modifier.weight(1f).height(112.dp), placeholder, RoundedCornerShape(13.dp)) }
                    }
                }
            }
            "Leaderboard" -> {
                SkeletonBlock(Modifier.width(175.dp).height(20.dp), placeholder)
                SkeletonBlock(Modifier.width(225.dp).padding(top = 8.dp).height(12.dp), placeholder)
                SkeletonBlock(Modifier.fillMaxWidth().padding(top = 12.dp).height(36.dp), placeholder, RoundedCornerShape(9.dp))
                repeat(8) { SkeletonBlock(Modifier.fillMaxWidth().padding(top = 7.dp).height(58.dp), placeholder, RoundedCornerShape(8.dp)) }
            }
            else -> {
                SkeletonBlock(Modifier.width(190.dp).height(23.dp), placeholder)
                repeat(5) { SkeletonBlock(Modifier.fillMaxWidth().padding(top = 9.dp).height(82.dp), placeholder, RoundedCornerShape(15.dp)) }
            }
        }
    }
}

@Composable
private fun SkeletonBlock(
    modifier: Modifier,
    color: Color,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    Box(modifier.background(color, shape))
}

@Composable
private fun TeamMatchCard(match: TeamMatchData) {
    val won = match.result == "Won"
    Row(Modifier.fillMaxWidth().padding(top = 10.dp).height(92.dp).background(Color(0xFF101923), RoundedCornerShape(16.dp)).border(1.dp, ProfileStroke, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        TeamBadge(match.teamName, match.teamLogoUrl)
        Column(Modifier.weight(1f).padding(start = 8.dp)) {
            Text(match.teamName, color = ProfileMuted, fontSize = 10.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text(match.teamScore.ifBlank { "—" }, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("${match.title} · ${match.dateLabel()}", color = ProfileMuted, fontSize = 9.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
        Text("VS", color = ProfileAccent, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp).background(Color(0xFF10140D), CircleShape).padding(7.dp))
        TeamBadge(match.opponentName, match.opponentLogoUrl)
        Column(Modifier.weight(1f).padding(start = 8.dp), horizontalAlignment = Alignment.End) {
            Text(match.opponentName, color = ProfileMuted, fontSize = 10.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text(match.opponentScore.ifBlank { "—" }, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            val label = match.result ?: match.status.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
            Text(label, color = if (won) Color(0xFF69E6AA) else Color(0xFFFF7777), fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun RecentMatch(league: String, home: String, homeScore: String, away: String, awayScore: String, result: String, won: Boolean) {
    Column(Modifier.fillMaxWidth().padding(top = 9.dp).background(ProfileCard, RoundedCornerShape(10.dp)).border(1.dp, ProfileStroke, RoundedCornerShape(10.dp)).padding(10.dp)) {
        Row { Text("♛  $league", color = ProfileMuted, fontSize = 8.sp, modifier = Modifier.weight(1f)); Text("12 Aug 2026  •  Kalinga Stadium", color = ProfileMuted, fontSize = 8.sp) }
        Row(Modifier.fillMaxWidth().padding(top = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            TeamBadge(home); Column(Modifier.weight(1f).padding(start = 8.dp)) { Text(home, color = Color.White, fontSize = 11.sp); Text(homeScore, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            Text("vs", color = ProfileMuted, fontSize = 10.sp)
            Column(Modifier.weight(1f).padding(start = 8.dp)) { Text(awayScore, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text(away, color = Color.White, fontSize = 11.sp) }
        }
        Text(result, color = Color.White, fontSize = 10.sp, modifier = Modifier.fillMaxWidth().padding(top = 7.dp).background(if (won) Color(0xFF075E38) else Color(0xFF78242B), RoundedCornerShape(4.dp)).padding(vertical = 3.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun UpcomingMatch(league: String, opponent: String, date: String, time: String) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp).height(65.dp).background(ProfileCard, RoundedCornerShape(9.dp)).border(1.dp, ProfileStroke, RoundedCornerShape(9.dp)).padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text("♛  $league", color = ProfileMuted, fontSize = 8.sp); Row(verticalAlignment = Alignment.CenterVertically) { TeamBadge("Addd"); Text("Addd     vs     $opponent", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(start = 7.dp)) } }
        Column(horizontalAlignment = Alignment.End) { Text(date, color = ProfileMuted, fontSize = 8.sp); Text(time, color = Color.White, fontSize = 10.sp); Text("Upcoming", color = Color.White, fontSize = 8.sp, modifier = Modifier.background(Color(0xFF0752A9), RoundedCornerShape(7.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) }
    }
}

@Composable
private fun TeamBadge(name: String, logoUrl: String = "", modifier: Modifier = Modifier.size(26.dp)) {
    Box(modifier.background(Color(0xFF111A21), CircleShape).border(1.dp, Color.White.copy(alpha = .75f), CircleShape).clip(CircleShape), contentAlignment = Alignment.Center) {
        if (logoUrl.isNotBlank()) {
            AsyncImage(model = logoUrl, contentDescription = "$name team logo", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Text(name.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").ifBlank { "?" }, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
    }
}

@Composable
private fun StatsTabContent(matches: List<TeamMatchData>) {
    val completed = matches.filter { it.status == "COMPLETED" }
    val won = completed.count { it.result == "Won" }
    val lost = completed.count { it.result == "Lost" }
    val tied = completed.count { it.result == "Tied" }
    val winRate = if (completed.isEmpty()) "0%" else "${(won * 100.0 / completed.size).let { "%.1f".format(it) }}%"
    Column(Modifier.padding(horizontal = 12.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Team Statistics", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold); Text("Complete overview of our performance", color = ProfileMuted, fontSize = 11.sp) }; Text("All Tournaments ⌄", color = Color.White, fontSize = 9.sp, modifier = Modifier.border(1.dp, ProfileStroke, RoundedCornerShape(10.dp)).padding(10.dp)) }
        StatGrid(matches.size, matches.count { it.status != "COMPLETED" && it.status != "ABANDONED" }, won, lost, tied, winRate)
        Row(Modifier.fillMaxWidth().padding(top = 13.dp).background(Color(0xFF0A2830), RoundedCornerShape(10.dp)).border(1.dp, Color(0xFF315D38), RoundedCornerShape(10.dp)).padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Text("💡", fontSize = 20.sp); Column(Modifier.padding(start = 10.dp).weight(1f)) { Text("Key Insight", color = ProfileAccent, fontSize = 10.sp); Text("$won wins from ${completed.size} completed matches.", color = ProfileMuted, fontSize = 10.sp) }; Text("→", color = Color.White) }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun StatGrid(matches: Int, upcoming: Int, won: Int, lost: Int, tied: Int, winRate: String) {
    data class StatItem(val title: String, val value: String, val icon: Int? = null, val symbol: String? = null, val tint: Color = ProfileAccent)
    val stats = listOf(
        StatItem("Matches", matches.toString(), icon = R.drawable.tick),
        StatItem("Upcoming", upcoming.toString(), symbol = "▣"),
        StatItem("Won", won.toString(), icon = R.drawable.victory3),
        StatItem("Lost", lost.toString(), symbol = "✕"),
        StatItem("Tie", tied.toString(), symbol = "⌁"),
        StatItem("Drawn", "0", symbol = "═"),
        StatItem("NR", "0", symbol = "⊗"),
        StatItem("Win %", winRate, icon = R.drawable.whitevictory)
    )
    Column(Modifier.fillMaxWidth().padding(top = 16.dp).background(ProfileCard, RoundedCornerShape(16.dp)).border(1.dp, ProfileStroke, RoundedCornerShape(16.dp)).padding(10.dp)) {
        stats.chunked(2).forEachIndexed { rowIndex, row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { stat ->
                    Column(
                        Modifier.weight(1f).height(116.dp)
                            .background(Color(0xFF0B1D30), RoundedCornerShape(13.dp))
                            .border(1.dp, Color(0xFF23354A), RoundedCornerShape(13.dp))
                            .padding(horizontal = 13.dp, vertical = 11.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(stat.title, color = Color(0xFFAAB6C7), fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            if (stat.icon != null) {
                                Image(painterResource(stat.icon), contentDescription = stat.title, modifier = Modifier.size(25.dp), contentScale = ContentScale.Fit,
                                    colorFilter = if (stat.title == "Win %") ColorFilter.tint(ProfileAccent) else null)
                            } else {
                                Text(stat.symbol.orEmpty(), color = stat.tint, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(stat.value, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                    }
                }
            }
            if (rowIndex < 3) Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable private fun StatTitle(title: String, subtitle: String) = Column(Modifier.padding(top = 14.dp)) { Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = ProfileMuted, fontSize = 11.sp) }
@Composable
private fun SplitStatCard(icon1: String, label1: String, value1: String, percent1: String, icon2: String, label2: String, value2: String, percent2: String) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp).height(89.dp).background(ProfileCard, RoundedCornerShape(10.dp)).border(1.dp, ProfileStroke, RoundedCornerShape(10.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) { SplitStat(icon1, label1, value1, percent1, Modifier.weight(1f)); Spacer(Modifier.width(1.dp).fillMaxHeight().background(ProfileStroke)); SplitStat(icon2, label2, value2, percent2, Modifier.weight(1f)) }
}
@Composable private fun SplitStat(icon: String, label: String, value: String, percent: String, modifier: Modifier) = Row(modifier.padding(horizontal = 5.dp), verticalAlignment = Alignment.CenterVertically) { Text(icon, color = ProfileAccent, fontSize = 24.sp); Column(Modifier.padding(start = 10.dp).weight(1f)) { Text(label, color = ProfileMuted, fontSize = 10.sp); Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("out of 24", color = ProfileMuted, fontSize = 9.sp) }; Box(Modifier.size(48.dp).border(6.dp, ProfileAccent, CircleShape), contentAlignment = Alignment.Center) { Text(percent, color = Color.White, fontSize = 9.sp) } }

private data class TeamLeaderboardPlayer(
    val id: String,
    val name: String,
    val photoUrl: String,
    val matches: Long,
    val runs: Long,
    val battingPoints: Long,
    val outs: Long,
    val balls: Long,
    val wickets: Long,
    val runsConceded: Long,
    val legalBalls: Long,
    val catches: Long,
    val runOuts: Long,
    val stumpings: Long,
    val fieldingPoints: Long
)

@Composable
private fun LeaderboardTabContent(teamId: String) {
    var players by remember(teamId) { mutableStateOf<List<TeamLeaderboardPlayer>>(emptyList()) }
    var loading by remember(teamId) { mutableStateOf(true) }
    var loadError by remember(teamId) { mutableStateOf(false) }
    DisposableEffect(teamId) {
        val registration = FirebaseFirestore.getInstance()
            .collection("leaderboards").document("playerAggregates").collection("entries")
            .whereEqualTo("teamId", teamId)
            .addSnapshotListener { snapshot, error ->
                loading = false
                loadError = error != null
                if (error != null) return@addSnapshotListener
                val records = snapshot?.documents.orEmpty()
                    .filter { it.getString("sport") == "CRICKET" && it.getString("tournamentId").orEmpty().isBlank() && it.getString("format") == "LIMITED_OVERS" }
                    .map { doc ->
                        fun number(key: String) = doc.getLong(key) ?: 0L
                        TeamLeaderboardPlayer(
                            id = doc.getString("canonicalPlayerId")?.takeIf(String::isNotBlank) ?: doc.getString("playerId").orEmpty(),
                            name = doc.getString("name").orEmpty().ifBlank { "Team player" },
                            photoUrl = doc.getString("profilePhotoUrl").orEmpty(),
                            matches = number("matches"), runs = number("runs"), battingPoints = number("battingPoints"),
                            outs = number("outs"), balls = number("balls"), wickets = number("wickets"),
                            runsConceded = number("runsConceded"), legalBalls = number("legalBalls"),
                            catches = number("catches"), runOuts = number("runOuts"), stumpings = number("stumpings"),
                            fieldingPoints = number("fieldingPoints")
                        )
                    }
                    .filter { it.id.isNotBlank() }
                    .groupBy { it.id }
                    .values
                    .map { samePlayer -> samePlayer.maxBy { it.battingPoints + it.wickets * 20 + it.fieldingPoints } }
                    .sortedByDescending { it.battingPoints + it.wickets * 20 + it.fieldingPoints }
                players = records
            }
        onDispose { registration.remove() }
    }
    var selectedCategory by remember { mutableIntStateOf(0) }
    val categories = listOf("Batting", "Bowling", "Fielding")
    Column(Modifier.padding(horizontal = 12.dp)) {
        Text("Team Leaderboard", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        Text("Top performers from our team", color = ProfileMuted, fontSize = 11.sp)
        Row(Modifier.fillMaxWidth().padding(top = 11.dp).height(34.dp).background(Color(0xFF09111C), RoundedCornerShape(9.dp)).border(1.dp, ProfileStroke, RoundedCornerShape(9.dp)), verticalAlignment = Alignment.CenterVertically) {
            categories.forEachIndexed { i, tab ->
                val selected = selectedCategory == i
                Text(tab, color = if (selected) Color(0xFF111709) else Color(0xFFB6C1D0), fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).fillMaxHeight().padding(2.dp).clip(RoundedCornerShape(7.dp)).background(if (selected) ProfileAccent else Color.Transparent).clickable { selectedCategory = i }.wrapContentHeight(Alignment.CenterVertically),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 3.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("#", color = ProfileMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
            Spacer(Modifier.width(40.dp))
            Text("PLAYER", color = ProfileMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            val labels = when (selectedCategory) { 1 -> listOf("WKT", "AVG", "ECO", "MAT"); 2 -> listOf("PTS", "CT", "RO", "MAT"); else -> listOf("RUNS", "AVG", "SR", "MAT") }
            labels.forEachIndexed { i, label -> Text(label, color = ProfileMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.End, modifier = Modifier.width(listOf(42.dp, 40.dp, 42.dp, 28.dp)[i])) }
        }
        when {
            loading -> Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ProfileAccent, modifier = Modifier.size(24.dp), strokeWidth = 2.dp) }
            loadError -> Text("Unable to load this team's leaderboard.", color = ProfileMuted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 18.dp))
            players.isEmpty() -> EmptyTeamTab("Player rankings will appear after this team's matches are completed.")
            else -> players.sortedByDescending { player -> when (selectedCategory) { 1 -> player.wickets * 20; 2 -> player.fieldingPoints; else -> player.battingPoints } }
                .forEachIndexed { index, player -> LeaderboardRow(index + 1, player, selectedCategory) }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun LeaderboardRow(rank: Int, player: TeamLeaderboardPlayer, category: Int) {
    val gold = Color(0xFFFFC928)
    Row(Modifier.fillMaxWidth().height(64.dp).drawBehind { drawLine(ProfileStroke, Offset(0f, size.height), Offset(size.width, size.height), 0.7.dp.toPx()) }.padding(horizontal = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("$rank", color = if (rank <= 3) gold else Color(0xFFD8E0EA), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Box(Modifier.padding(start = 7.dp).size(36.dp).border(1.3.dp, Color(0xFF34465D), CircleShape).background(Color(0xFF111C2A), CircleShape), contentAlignment = Alignment.Center) {
            if (player.photoUrl.isNotBlank()) AsyncImage(model = player.photoUrl, contentDescription = "${player.name} profile photo", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
            else Text(player.name.split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString(""), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f).padding(start = 9.dp)) { Text(player.name, color = Color(0xFFF3F5F8), fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1); Text(when (category) { 1 -> "Bowling"; 2 -> "Fielding"; else -> "Batting" }, color = Color(0xFF8F9CAF), fontSize = 10.sp, maxLines = 1) }
        val figures = when (category) {
            1 -> listOf(player.wickets.toString(), (player.runsConceded.toDouble() / player.wickets.coerceAtLeast(1)).formatLeaderboard(), (player.runsConceded.toDouble() * 6 / player.legalBalls.coerceAtLeast(1)).formatLeaderboard(), player.matches.toString())
            2 -> listOf(player.fieldingPoints.toString(), player.catches.toString(), player.runOuts.toString(), player.matches.toString())
            else -> listOf(player.runs.toString(), (player.runs.toDouble() / player.outs.coerceAtLeast(1)).formatLeaderboard(), (player.runs.toDouble() * 100 / player.balls.coerceAtLeast(1)).formatLeaderboard(), player.matches.toString())
        }
        figures.forEachIndexed { i, value -> Text(value, color = if (i == 0) ProfileAccent else Color(0xFFD0D7E1), fontSize = 11.sp, fontWeight = if (i == 0) FontWeight.Bold else FontWeight.Normal, textAlign = androidx.compose.ui.text.style.TextAlign.End, modifier = Modifier.width(listOf(42.dp, 40.dp, 42.dp, 28.dp)[i])) }
    }
}

private fun Double.formatLeaderboard(): String = if (isFinite()) String.format(java.util.Locale.US, "%.1f", this) else "0.0"

@Composable
private fun MembersTabContent(
    teamId: String,
    members: List<TeamMemberData>,
    viewerRoles: List<String>
) {
    Column(Modifier.padding(horizontal = 12.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(5.dp).height(27.dp).background(ProfileAccent, RoundedCornerShape(4.dp)))
            Text("Team Members (${members.size})", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 10.dp))
        }
        if (members.isEmpty()) {
            EmptyTeamTab("No members have joined this team yet.")
        } else {
            members.forEachIndexed { index, member ->
                MemberRow(index, teamId, member, viewerRoles)
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun MemberRow(
    index: Int,
    teamId: String,
    member: TeamMemberData,
    viewerRoles: List<String>
) {
    val context = LocalContext.current
    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
    val isManager = viewerRoles.any { it == "ADMIN" || it == "CAPTAIN" }
    val isViceCaptain = "VICE_CAPTAIN" in viewerRoles
    val isSelf = member.userId == currentUserId
    val targetIsAdmin = "ADMIN" in member.teamRoles
    val targetHasLeadershipRole = member.teamRoles.isNotEmpty()
    val canShowActions = isManager || isViceCaptain
    val canAssignRoles = isManager && !isSelf
    val canRemove = !isSelf && (
        isManager && !targetIsAdmin ||
            isViceCaptain && !targetHasLeadershipRole
        )
    val isWicketKeeper = member.playingRole.equals("WICKET_KEEPER", ignoreCase = true)
    var actionsExpanded by remember(member.userId) { mutableStateOf(false) }
    var isUpdatingMember by remember(member.userId) { mutableStateOf(false) }
    val isLeader = member.teamRoles.any { it == "ADMIN" || it == "CAPTAIN" }
    Row(
        Modifier.fillMaxWidth().padding(top = 7.dp).heightIn(min = 82.dp)
            .shadow(8.dp, RoundedCornerShape(15.dp), ambientColor = Color.White.copy(alpha = .18f), spotColor = Color.White.copy(alpha = .22f))
            .background(Color(0xFF071A25), RoundedCornerShape(15.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(String.format("%02d", index + 1), color = Color(0xFFD6DFEA), fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(31.dp))
        Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.fillMaxSize().border(2.dp, ProfileAccent, CircleShape).padding(3.dp).clip(CircleShape).background(Color(0xFF132A38)), contentAlignment = Alignment.Center) {
                if (member.profilePhotoUrl.isNotBlank()) {
                    AsyncImage(model = member.profilePhotoUrl, contentDescription = "${member.name} profile photo", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                }
            }
            if (isUpdatingMember) {
                Box(Modifier.fillMaxSize().background(Color(0xAA071A25), CircleShape), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(25.dp), color = Color(0xFF22C55E), strokeWidth = 2.5.dp)
                }
            }
        }
        val memberPosition = when {
            "CAPTAIN" in member.teamRoles -> "Captain"
            "VICE_CAPTAIN" in member.teamRoles -> "Vice Captain"
            else -> member.playingRole.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
        }
        Column(Modifier.weight(1.05f).padding(start = 10.dp)) {
            Text(member.name, color = Color(0xFFF5F7FB), fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Row(Modifier.padding(top = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(memberPosition, color = if (isLeader) ProfileAccent else Color(0xFFBCC9D8), fontSize = 11.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                if ("ADMIN" in member.teamRoles) {
                    val adminRed = Color(0xFFFF4058)
                    Box(
                        Modifier.shadow(7.dp, RoundedCornerShape(5.dp), ambientColor = adminRed.copy(alpha = .8f), spotColor = adminRed.copy(alpha = .8f))
                            .background(adminRed.copy(alpha = .13f), RoundedCornerShape(5.dp))
                            .border(1.dp, adminRed, RoundedCornerShape(5.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text("Admin", color = adminRed, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                    }
                }
            }
        }
        Box(Modifier.padding(horizontal = 10.dp).width(1.dp).height(48.dp).background(Color(0xFF254252)))
        Column(Modifier.weight(.9f)) {
            Text(member.battingStyle.ifBlank { "—" }, color = Color(0xFFDCE5EF), fontSize = 10.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text(member.bowlingStyle.ifBlank { "—" }, color = Color(0xFFBCC9D8), fontSize = 10.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.padding(top = 5.dp))
        }
        if (isUpdatingMember) {
            CircularProgressIndicator(modifier = Modifier.padding(horizontal = 8.dp).size(22.dp), color = Color(0xFF22C55E), strokeWidth = 2.dp)
        } else if (canShowActions && !isSelf) {
            Box {
                Text(
                    "⋮",
                    color = Color.White,
                    fontSize = 26.sp,
                    modifier = Modifier
                        .clickable { actionsExpanded = true }
                        .padding(start = 6.dp, end = 2.dp)
                )
                DropdownMenu(
                    expanded = actionsExpanded,
                    onDismissRequest = { actionsExpanded = false }
                ) {
                    if (canAssignRoles) {
                        val isCaptain = "CAPTAIN" in member.teamRoles
                        TeamMemberAction(if (isCaptain) "Remove captain" else "Make captain") {
                            actionsExpanded = false
                            isUpdatingMember = true
                            updateMemberRole(teamId, member.userId, if (isCaptain) "PLAYER" else "CAPTAIN", null, context) { isUpdatingMember = false }
                        }
                        val isViceCaptain = "VICE_CAPTAIN" in member.teamRoles
                        TeamMemberAction(if (isViceCaptain) "Remove vice captain" else "Make vice-captain") {
                            actionsExpanded = false
                            isUpdatingMember = true
                            updateMemberRole(teamId, member.userId, if (isViceCaptain) "PLAYER" else "VICE_CAPTAIN", null, context) { isUpdatingMember = false }
                        }
                        TeamMemberAction(if (isWicketKeeper) "Remove wicketkeeper" else "Make wicketkeeper") {
                            actionsExpanded = false
                            isUpdatingMember = true
                            updateMemberRole(teamId, member.userId, null, if (isWicketKeeper) "PLAYER" else "WICKET_KEEPER", context) { isUpdatingMember = false }
                        }
                    }
                    if (canRemove) {
                        TeamMemberAction("Remove player") {
                            actionsExpanded = false
                            isUpdatingMember = true
                            removeMember(teamId, member.userId, context) { isUpdatingMember = false }
                        }
                    }
                    if (!canAssignRoles && !canRemove) {
                        DropdownMenuItem(
                            text = { Text("No actions available") },
                            onClick = {},
                            enabled = false
                        )
                    }
                }
            }
        } else if (!isSelf) {
            Text("›", color = Color.White, fontSize = 20.sp)
        }
    }
}

@Composable
private fun TeamMemberAction(label: String, onClick: () -> Unit) {
    DropdownMenuItem(text = { Text(label) }, onClick = onClick)
}

private fun updateMemberRole(
    teamId: String,
    targetUserId: String,
    teamRole: String?,
    playingRole: String?,
    context: android.content.Context,
    onComplete: () -> Unit
) {
    val payload = mutableMapOf<String, Any>("teamId" to teamId, "targetUserId" to targetUserId)
    teamRole?.let { payload["teamRole"] = it }
    playingRole?.let { payload["playingRole"] = it }
    com.google.firebase.functions.FirebaseFunctions.getInstance()
        .getHttpsCallable("updateTeamMemberRole")
        .call(payload)
        .addOnSuccessListener { onComplete() }
        .addOnFailureListener { error ->
            android.widget.Toast.makeText(context, error.message ?: "Unable to update the player role.", android.widget.Toast.LENGTH_SHORT).show()
            onComplete()
        }
}

private fun removeMember(teamId: String, targetUserId: String, context: android.content.Context, onComplete: () -> Unit) {
    com.google.firebase.functions.FirebaseFunctions.getInstance()
        .getHttpsCallable("removeTeamMember")
        .call(mapOf("teamId" to teamId, "targetUserId" to targetUserId))
        .addOnSuccessListener { onComplete() }
        .addOnFailureListener { error ->
            android.widget.Toast.makeText(context, error.message ?: "Unable to remove the player.", android.widget.Toast.LENGTH_SHORT).show()
            onComplete()
        }
}

private fun Map<*, *>.teamRoles(): List<String> {
    val explicitRoles = (this["roles"] as? List<*>).orEmpty().filterIsInstance<String>()
    if (explicitRoles.isNotEmpty()) return explicitRoles
    return when (this["role"] as? String) {
        "OWNER" -> listOf("ADMIN", "CAPTAIN")
        "CAPTAIN" -> listOf("CAPTAIN")
        "VICE_CAPTAIN" -> listOf("VICE_CAPTAIN")
        else -> emptyList()
    }
}

private fun fetchTeamMemberProfiles(
    teamId: String,
    members: List<TeamMemberData>,
    onLoaded: (Map<String, MemberProfileData>) -> Unit
) {
    com.google.firebase.functions.FirebaseFunctions.getInstance()
        .getHttpsCallable("getTeamMemberProfiles")
        .call(mapOf("teamId" to teamId))
        .addOnSuccessListener { result ->
            val data = result.data as? Map<*, *> ?: return@addOnSuccessListener
            val profiles = (data["members"] as? List<*>)
                .orEmpty()
                .mapNotNull { it as? Map<*, *> }
                .mapNotNull { profile ->
                    val userId = profile["userId"] as? String ?: return@mapNotNull null
                    userId to MemberProfileData(
                        displayName = (profile["displayName"] as? String).orEmpty().ifBlank { "Team member" },
                        playingRole = (profile["playingRole"] as? String).orEmpty().ifBlank { "Player" },
                        profilePhotoUrl = (profile["profilePhotoUrl"] as? String).orEmpty(),
                        battingStyle = (profile["battingStyle"] as? String).orEmpty(),
                        bowlingStyle = (profile["bowlingStyle"] as? String).orEmpty()
                    )
                }
                .toMap()
            onLoaded(members.associate { member ->
                val publicProfile = profiles[member.userId]
                member.userId to MemberProfileData(
                    displayName = publicProfile?.displayName?.takeIf { it.isNotBlank() } ?: member.name.ifBlank { "Team member" },
                    playingRole = publicProfile?.playingRole?.takeIf { it.isNotBlank() } ?: member.playingRole.ifBlank { "Player" },
                    profilePhotoUrl = publicProfile?.profilePhotoUrl?.takeIf { it.isNotBlank() } ?: member.profilePhotoUrl,
                    battingStyle = publicProfile?.battingStyle?.takeIf { it.isNotBlank() } ?: member.battingStyle,
                    bowlingStyle = publicProfile?.bowlingStyle?.takeIf { it.isNotBlank() } ?: member.bowlingStyle
                )
            })
        }
        .addOnFailureListener {
            // The roster remains usable with safe placeholders if the network
            // is unavailable; a later team snapshot will retry this request.
            onLoaded(members.associate { member ->
                member.userId to MemberProfileData(
                    displayName = member.name.ifBlank { "Team member" },
                    playingRole = member.playingRole.ifBlank { "Player" },
                    profilePhotoUrl = member.profilePhotoUrl,
                    battingStyle = member.battingStyle,
                    bowlingStyle = member.bowlingStyle
                )
            })
        }
}
