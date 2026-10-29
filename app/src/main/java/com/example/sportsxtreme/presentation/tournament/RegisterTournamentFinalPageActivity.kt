package com.example.sportsxtreme.presentation.tournament

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import coil3.compose.AsyncImage
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.ui.theme.*
import com.example.sportsxtreme.domain.model.Tournament
import com.example.sportsxtreme.data.remote.cloudinary.CloudinaryTournamentImageUploader
import com.example.sportsxtreme.presentation.home.HomeScreenView
import com.example.sportsxtreme.presentation.auth.MainActivity
import com.example.sportsxtreme.presentation.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.sportsxtreme.presentation.match.StartMatchActivity
import com.example.sportsxtreme.presentation.match.RegisteredTeamsOfATournament
import com.example.sportsxtreme.presentation.match.SelectPlayingTeamsActivity
import com.google.firebase.functions.FirebaseFunctions
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID

@AndroidEntryPoint
class RegisterTournamentFinalPageActivity : ComponentActivity() {
    private val viewModel: TournamentFlowViewModel by viewModels()
    private var logoUploading by mutableStateOf(false)
    private var bannerUploading by mutableStateOf(false)
    private val logoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) lifecycleScope.launch {
            logoUploading = true
            CloudinaryTournamentImageUploader(applicationContext).upload(uri, UUID.randomUUID().toString(), "logo")
                .onSuccess { url ->
                    viewModel.saveTournamentImage(logoUrl = url) { result ->
                        logoUploading = false
                        when (result) {
                            is com.example.sportsxtreme.common.Resource.Success -> Toast.makeText(this@RegisterTournamentFinalPageActivity, "Tournament logo updated", Toast.LENGTH_SHORT).show()
                            is com.example.sportsxtreme.common.Resource.Error -> Toast.makeText(this@RegisterTournamentFinalPageActivity, result.message ?: "Unable to save logo", Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }
                .onFailure { error ->
                    logoUploading = false
                    Toast.makeText(this@RegisterTournamentFinalPageActivity, error.message ?: "Could not upload tournament logo", Toast.LENGTH_LONG).show()
                }
        }
    }
    private val bannerPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) lifecycleScope.launch {
            bannerUploading = true
            CloudinaryTournamentImageUploader(applicationContext).upload(uri, UUID.randomUUID().toString(), "banner")
                .onSuccess { url ->
                    viewModel.saveTournamentImage(bannerUrl = url) { result ->
                        bannerUploading = false
                        when (result) {
                            is com.example.sportsxtreme.common.Resource.Success -> Toast.makeText(this@RegisterTournamentFinalPageActivity, "Tournament banner updated", Toast.LENGTH_SHORT).show()
                            is com.example.sportsxtreme.common.Resource.Error -> Toast.makeText(this@RegisterTournamentFinalPageActivity, result.message ?: "Unable to save banner", Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }
                .onFailure { error ->
                    bannerUploading = false
                    Toast.makeText(this@RegisterTournamentFinalPageActivity, error.message ?: "Could not upload tournament banner", Toast.LENGTH_LONG).show()
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = navigateBackFromTournament()
        })
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val pageBackground = android.graphics.Color.rgb(10, 16, 28)
        window.statusBarColor = pageBackground
        window.navigationBarColor = pageBackground
        val tournamentId = intent.getStringExtra(EXTRA_TOURNAMENT_ID)
            ?.takeIf { it.isNotBlank() }
            ?: intent.data?.getQueryParameter("id").orEmpty()
        val initialTab = intent.getIntExtra(EXTRA_INITIAL_TAB, 0).coerceIn(0, 4)
        val initialMatchTab = intent.getIntExtra(EXTRA_INITIAL_MATCH_TAB, 0).coerceIn(0, 2)
        viewModel.load(tournamentId)
        setContent {
            RegisterTournamentFinalPage(
                tournament = viewModel.tournament.collectAsState().value,
                initialTab = initialTab,
                initialMatchTab = initialMatchTab,
                onBack = ::navigateBackFromTournament,
                onPickLogo = { if (viewModel.tournament.value?.hostUid == FirebaseAuth.getInstance().currentUser?.uid) logoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                onPickBanner = { if (viewModel.tournament.value?.hostUid == FirebaseAuth.getInstance().currentUser?.uid) bannerPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                logoUploading = logoUploading,
                bannerUploading = bannerUploading,
                onSaveTournamentDetails = viewModel::saveTournamentDetails,
                onSaveTeamDetails = viewModel::saveTeamDetails,
                onDeleteTournament = {
                    viewModel.deleteTournament { result ->
                        when (result) {
                            is com.example.sportsxtreme.common.Resource.Success -> {
                                Toast.makeText(this, "Tournament deleted", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            is com.example.sportsxtreme.common.Resource.Error ->
                                Toast.makeText(this, result.message ?: "Unable to delete tournament", Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }
            )
        }
    }

    private fun navigateBackFromTournament() {
        if (intent.getBooleanExtra(EXTRA_JUST_REGISTERED, false)) {
            android.app.AlertDialog.Builder(this)
                .setTitle("Leave tournament setup?")
                .setMessage("You Can Manage Your Tournament From Your Tournament Tab of Host screen")
                .setNegativeButton("Stay", null)
                .setPositiveButton("Go Back") { _, _ ->
                    startActivity(
                        Intent(this, MainActivity::class.java)
                            .putExtra(MainActivity.EXTRA_OPEN_HOST_TAB, true)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    )
                    finish()
                }
                .show()
            return
        }
        if (isTaskRoot) {
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }
        finish()
    }

    companion object {
        const val EXTRA_TOURNAMENT_ID = "tournament_id"
        const val EXTRA_JUST_REGISTERED = "tournament_just_registered"
        const val EXTRA_INITIAL_TAB = "initial_tab"
        const val EXTRA_INITIAL_MATCH_TAB = "initial_match_tab"
        const val TEAMS_TAB_INDEX = 1
    }
}

private val FinalBg = Color(0xFF0A101C)
private val FinalPanel = Color(0xFF111D2D)
private val FinalPanelLight = Color(0xFF142338)
private val FinalCardShape = RoundedCornerShape(16.dp)
private val FinalAccent = XtremeLime
private val FinalMuted = XtremeMuted
private val FinalDivider = XtremeCardBorder

@Composable
private fun RegisterTournamentFinalPage(
    tournament: Tournament?,
    initialTab: Int,
    initialMatchTab: Int,
    onBack: () -> Unit,
    onPickLogo: () -> Unit,
    onPickBanner: () -> Unit,
    logoUploading: Boolean,
    bannerUploading: Boolean,
    onSaveTournamentDetails: (String, String, String, String) -> Unit,
    onSaveTeamDetails: (String, String) -> Unit,
    onDeleteTournament: () -> Unit
) {
    if (tournament == null) {
        Box(Modifier.fillMaxSize().background(FinalBg), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.material3.CircularProgressIndicator(color = FinalAccent)
                Text("Loading tournament…", color = FinalMuted, modifier = Modifier.padding(top = 12.dp))
            }
        }
        return
    }
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var pointsGroups by remember { mutableStateOf<List<PointsGroup>>(emptyList()) }
    var selectedPointsGroup by remember { mutableStateOf<String?>(null) }
    val isOrganizer = tournament != null && tournament.hostUid.isNotBlank() && tournament.hostUid == com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
    DisposableEffect(tournament?.id) {
        val tournamentId = tournament?.id.orEmpty()
        if (tournamentId.isBlank()) return@DisposableEffect onDispose { }
        val registration = FirebaseFirestore.getInstance().collection("tournaments").document(tournamentId)
            .collection("pointsTable").document("current")
            .addSnapshotListener { snapshot, _ ->
                @Suppress("UNCHECKED_CAST")
                val savedGroups = snapshot?.get("groups") as? List<Map<String, Any?>>
                val parsedGroups = savedGroups.orEmpty().mapNotNull { item ->
                    val name = item["name"] as? String ?: return@mapNotNull null
                    val savedTeams = item["teams"] as? List<Map<String, String>>
                    val parsedTeams = savedTeams.orEmpty().mapNotNull { team ->
                        val id = team["id"].orEmpty()
                        if (id.isBlank()) null else PointsGroupTeam(id, team["name"].orEmpty().ifBlank { "Team" }, team["logoUrl"].orEmpty())
                    }
                    PointsGroup(name, parsedTeams)
                }
                // Read the previous single-group document format so existing tournaments migrate on save.
                val loadedGroups = if (parsedGroups.isNotEmpty()) parsedGroups else {
                    val legacyName = snapshot?.getString("groupName")?.takeIf(String::isNotBlank)
                    if (legacyName == null) emptyList() else {
                        val legacyTeams = snapshot?.get("teams") as? List<Map<String, String>>
                        listOf(PointsGroup(legacyName, legacyTeams.orEmpty().mapNotNull { team ->
                            val id = team["id"].orEmpty()
                            if (id.isBlank()) null else PointsGroupTeam(id, team["name"].orEmpty().ifBlank { "Team" }, team["logoUrl"].orEmpty())
                        }))
                    }
                }
                pointsGroups = loadedGroups
                selectedPointsGroup = snapshot?.getString("selectedGroup")
                    ?.takeIf { selected -> loadedGroups.any { it.name == selected } }
                    ?: loadedGroups.firstOrNull()?.name
            }
        onDispose { registration.remove() }
    }
    fun savePointsTable(groups: List<PointsGroup>, selectedGroup: String?) {
        if (!isOrganizer || tournament.id.isBlank()) return
        val reference = FirebaseFirestore.getInstance().collection("tournaments").document(tournament.id)
            .collection("pointsTable").document("current")
        val selected = groups.firstOrNull { it.name == selectedGroup } ?: groups.firstOrNull()
        if (groups.isEmpty()) reference.delete()
        else reference.set(mapOf(
            "groups" to groups.map { group -> mapOf("name" to group.name, "teams" to group.teams.map { mapOf("id" to it.id, "name" to it.name, "logoUrl" to it.logoUrl) }) },
            "selectedGroup" to selected?.name,
            // Keep the original fields populated for older app versions.
            "groupName" to selected?.name,
            "teams" to selected?.teams.orEmpty().map { mapOf("id" to it.id, "name" to it.name, "logoUrl" to it.logoUrl) }
        ))
    }
    DisposableEffect(tournament.id, pointsGroups) {
        pointsGroups.flatMap { it.teams }.filter { it.logoUrl.isBlank() }.forEach { team ->
            FirebaseFirestore.getInstance().collection("teams").document(team.id).get().addOnSuccessListener { details ->
                val logo = listOf("logoUrl", "teamLogoUrl", "photoUrl").firstNotNullOfOrNull { details.getString(it)?.takeIf(String::isNotBlank) }.orEmpty()
                if (logo.isNotBlank()) {
                    val updatedGroups = pointsGroups.map { group ->
                        group.copy(teams = group.teams.map { if (it.id == team.id) it.copy(logoUrl = logo) else it })
                    }
                    pointsGroups = updatedGroups
                    savePointsTable(updatedGroups, selectedPointsGroup)
                }
            }
        }
        onDispose { }
    }
    val tabs = listOf("Overview", "Teams", "Matches", "Points", "Leaderboard")
    Column(Modifier.fillMaxSize().background(FinalBg)) {
        FinalTopBar(onBack, if (isOrganizer) onDeleteTournament else null)
        FinalTournamentHeader(tournament, onPickLogo, onPickBanner, logoUploading, bannerUploading, isOrganizer)
        FinalTournamentTabs(tabs, selectedTab) { selectedTab = it }
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(selectedTab) {
                var horizontalDrag = 0f
                var verticalDrag = 0f
                detectDragGestures(
                    onDragStart = {
                        horizontalDrag = 0f
                        verticalDrag = 0f
                    },
                    onDrag = { _, dragAmount ->
                        horizontalDrag += dragAmount.x
                        verticalDrag += dragAmount.y
                    },
                    onDragEnd = {
                        if (kotlin.math.abs(horizontalDrag) > kotlin.math.abs(verticalDrag)) {
                            when {
                                horizontalDrag <= -80 && selectedTab < tabs.lastIndex -> selectedTab++
                                horizontalDrag >= 80 && selectedTab > 0 -> selectedTab--
                            }
                        }
                    },
                    onDragCancel = {
                        horizontalDrag = 0f
                        verticalDrag = 0f
                    }
                )
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> AboutTab(tournament, onSaveTournamentDetails, onSaveTeamDetails, isOrganizer)
                        1 -> TeamsTab(tournament, isOrganizer)
                        2 -> MatchesTab(tournament, initialMatchTab)
                        3 -> PointsTab(
                            tournamentId = tournament?.id.orEmpty(),
                            groups = pointsGroups,
                            selectedGroupName = selectedPointsGroup,
                            onSelectGroup = { name ->
                                selectedPointsGroup = name
                                savePointsTable(pointsGroups, name)
                            },
                            onCreateGroup = { name ->
                                val existing = pointsGroups.firstOrNull { it.name.equals(name, ignoreCase = true) }
                                if (existing != null) {
                                    selectedPointsGroup = existing.name
                                } else {
                                    pointsGroups = pointsGroups + PointsGroup(name, emptyList())
                                    selectedPointsGroup = name
                                }
                                savePointsTable(pointsGroups, selectedPointsGroup)
                            },
                            onDeleteGroup = { name ->
                                pointsGroups = pointsGroups.filterNot { it.name == name }
                                selectedPointsGroup = pointsGroups.firstOrNull()?.name
                                savePointsTable(pointsGroups, selectedPointsGroup)
                            },
                            onTeamsChange = { updatedTeams ->
                                val activeName = selectedPointsGroup
                                pointsGroups = pointsGroups.map { group -> if (group.name == activeName) group.copy(teams = updatedTeams) else group }
                                savePointsTable(pointsGroups, activeName)
                            },
                            canEdit = isOrganizer
                        )
                        else -> LeaderboardTab()
                    }
                }
            }
        }
    }
}

@Composable
private fun FinalTournamentTabs(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(FinalPanel)
            .border(1.dp, FinalDivider, RoundedCornerShape(14.dp))
            .horizontalScroll(rememberScrollState())
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            val selected = selectedTab == index
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selected) Brush.horizontalGradient(listOf(FinalAccent.copy(alpha = .20f), FinalAccent.copy(alpha = .07f)))
                        else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .border(1.dp, if (selected) FinalAccent.copy(alpha = .55f) else Color.Transparent, RoundedCornerShape(10.dp))
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    label.uppercase(),
                    color = if (selected) FinalAccent else FinalMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Box(
                    Modifier.width(if (selected) 18.dp else 0.dp)
                        .height(3.dp)
                        .shadow(if (selected) 8.dp else 0.dp, RoundedCornerShape(2.dp), clip = false)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (selected) FinalAccent else Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun FinalTopBar(onBack: () -> Unit, onDeleteTournament: (() -> Unit)?) {
    var menuExpanded by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().height(62.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(FinalPanelLight).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
            BackGlyph(Modifier.size(21.dp))
        }
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Tournament Hub", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Box(Modifier.size(6.dp).clip(CircleShape).background(FinalAccent))
            }
            Text("Manage every detail in one place", color = FinalMuted, fontSize = 9.sp)
        }
        Spacer(Modifier.weight(1f))
        if (onDeleteTournament != null) Box(Modifier.size(40.dp).clip(CircleShape).background(FinalPanelLight), contentAlignment = Alignment.Center) {
            Text("⋮", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Box(Modifier.matchParentSize().clickable { menuExpanded = true })
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Delete Tournament", color = Color(0xFFB3261E)) },
                    onClick = {
                        menuExpanded = false
                        confirmDelete = true
                    }
                )
            }
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete tournament?") },
            text = { Text("This will permanently delete this tournament and its registered team entries.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    confirmDelete = false
                    onDeleteTournament?.invoke()
                }) { Text("Delete", color = Color(0xFFB3261E)) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun FinalTournamentHeader(
    tournament: Tournament?,
    onPickLogo: () -> Unit,
    onPickBanner: () -> Unit,
    logoUploading: Boolean,
    bannerUploading: Boolean,
    isOrganizer: Boolean
) {
    val context = LocalContext.current
    val tournamentName = tournament?.name?.ifBlank { "Tournament" } ?: "Loading tournament…"
    val tournamentDate = tournament?.startDate?.ifBlank { "Date to be announced" } ?: ""
    val location = listOf(tournament?.ground, tournament?.city).filterNotNull().filter { it.isNotBlank() }.joinToString(" · ")
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
            .shadow(8.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(FinalPanel)
    ) {
        Box(Modifier.fillMaxWidth().height(118.dp).background(FinalPanelLight)) {
            if (!tournament?.bannerUrl.isNullOrBlank()) {
                AsyncImage(
                    model = tournament?.bannerUrl,
                    contentDescription = "Tournament banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.batsman_onboarding2),
                    contentDescription = "Tournament banner placeholder",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .42f)))))
            if (bannerUploading) {
                Text("Uploading banner…", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomStart).padding(12.dp))
            }
            if (isOrganizer) Box(
                Modifier.align(Alignment.TopEnd).padding(10.dp).size(32.dp).clip(CircleShape)
                    .background(Color.Black.copy(alpha = .62f)).clickable(enabled = !bannerUploading, onClick = onPickBanner),
                contentAlignment = Alignment.Center
            ) { Text("✎", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(58.dp)) {
                Box(Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)).background(FinalPanelLight), contentAlignment = Alignment.Center) {
                    if (tournament?.logoUrl?.isNotBlank() == true) {
                        AsyncImage(
                            model = tournament.logoUrl,
                            contentDescription = "Tournament logo",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("🏏", fontSize = 23.sp)
                    }
                }
                if (isOrganizer) Box(
                    Modifier.align(Alignment.BottomEnd).size(22.dp).clip(CircleShape)
                        .background(FinalAccent).clickable(enabled = !logoUploading, onClick = onPickLogo),
                    contentAlignment = Alignment.Center
                ) { Text("✎", color = FinalBg, fontSize = 12.sp, fontWeight = FontWeight.Black) }
            }
            Column(Modifier.padding(start = 11.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        tournamentName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Box(Modifier.clip(CircleShape).background(Color(0xFF063A32)).padding(horizontal = 7.dp, vertical = 4.dp)) {
                        Text("ACTIVE", color = Color(0xFF37E0AC), fontSize = 8.sp, fontWeight = FontWeight.Black, maxLines = 1, softWrap = false)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("▣  ${tournamentDate.ifBlank { "Date to be announced" }}", color = FinalMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (location.isNotBlank()) Text("•  $location", color = FinalMuted.copy(alpha = .9f), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box(
                Modifier.clip(RoundedCornerShape(10.dp)).background(FinalPanelLight).clickable {
                    val tournamentId = tournament?.id.orEmpty()
                    if (tournamentId.isBlank()) {
                        Toast.makeText(context, "Tournament is still loading", Toast.LENGTH_SHORT).show()
                    } else {
                        val tournamentUrl = Uri.parse(context.getString(R.string.tournament_share_base_url))
                            .buildUpon().appendQueryParameter("id", tournamentId).build().toString()
                        val shareText = "View $tournamentName${tournamentDate.takeIf { it.isNotBlank() }?.let { " • $it" }.orEmpty()}\n$tournamentUrl"
                        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }, "Share tournament"))
                    }
                }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Image(painter = painterResource(R.drawable.ic_share), contentDescription = null, modifier = Modifier.size(14.dp))
                    Text("Share", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AboutTab(
    tournament: Tournament?,
    onSaveTournamentDetails: (String, String, String, String) -> Unit,
    onSaveTeamDetails: (String, String) -> Unit,
    isOrganizer: Boolean
) {
    val requirements = tournament?.requirements
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OverviewStatsCard(
            teams = requirements?.numberOfTeams?.ifBlank { "0" } ?: "0",
            matches = "0",
            startDate = tournament?.startDate?.ifBlank { "Not set" } ?: "Not set",
            location = listOf(tournament?.ground, tournament?.city).filterNotNull().filter { it.isNotBlank() }.joinToString(", ").ifBlank { "Not set" }
        )
        TournamentDetailsCard(tournament, onSaveTournamentDetails, isOrganizer)
        TeamDetailsCard(tournament, onSaveTeamDetails, isOrganizer)
        OrganizerCard(tournament)
        TournamentQrCard(tournament)
        SetupHelpCard()
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun OverviewStatsCard(teams: String, matches: String, startDate: String, location: String) {
    Column(
        Modifier.fillMaxWidth().shadow(7.dp, FinalCardShape).clip(FinalCardShape).background(FinalBg).padding(9.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            OverviewMetric("TEAMS", teams, "registered", Modifier.weight(1f))
            OverviewMetric("MATCHES", matches, "scheduled", Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth()) {
            OverviewMetric("START DATE", startDate, "", Modifier.weight(1f))
            OverviewMetric("LOCATION", location, "", Modifier.weight(1f))
        }
    }
}

@Composable
private fun OverviewMetric(label: String, value: String, caption: String, modifier: Modifier) {
    Column(modifier.padding(3.dp).shadow(5.dp, RoundedCornerShape(13.dp)).clip(RoundedCornerShape(13.dp)).background(FinalPanel).padding(12.dp)) {
        Text(label, color = FinalMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(FinalAccent))
            Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (caption.isNotBlank()) Text(caption, color = FinalMuted, fontSize = 8.sp, modifier = Modifier.padding(start = 12.dp), maxLines = 1)
    }
}

@Composable
private fun SetupHelpCard() {
    Column(
        Modifier.fillMaxWidth().shadow(7.dp, FinalCardShape).clip(FinalCardShape).background(FinalPanel).padding(16.dp)
    ) {
        Text("Tournament setup guide/help", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(13.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(FinalDivider))
        Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Help videos", color = FinalMuted, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Box(Modifier.clip(RoundedCornerShape(6.dp)).border(1.dp, FinalAccent.copy(alpha = .85f), RoundedCornerShape(6.dp)).padding(horizontal = 18.dp, vertical = 6.dp)) {
                Text("View", color = FinalAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(17.dp))
        Text("SportsXtreme helpline", color = FinalMuted, fontSize = 13.sp)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            HelpContactButton("Call", Modifier.weight(1f))
            HelpContactButton("WhatsApp", Modifier.weight(1f))
        }
    }
}

@Composable
private fun OrganizerCard(tournament: Tournament?) {
    val organizerName = tournament?.organizerName?.ifBlank { "Organizer" } ?: "Organizer"
    var photoUrl by remember(tournament?.hostUid) { mutableStateOf("") }
    val context = LocalContext.current
    DisposableEffect(tournament?.hostUid) {
        val hostUid = tournament?.hostUid.orEmpty()
        if (hostUid.isBlank()) return@DisposableEffect onDispose { }
        val registration = FirebaseFirestore.getInstance().collection("publicProfiles").document(hostUid)
            .addSnapshotListener { snapshot, _ ->
                photoUrl = listOf("profilePhotoUrl", "photoUrl", "imageUrl").firstNotNullOfOrNull { snapshot?.getString(it)?.takeIf(String::isNotBlank) }.orEmpty()
            }
        onDispose { registration.remove() }
    }
    Column(
        Modifier.fillMaxWidth().shadow(7.dp, FinalCardShape).clip(FinalCardShape).background(FinalPanel).clickable {
            if (!tournament?.hostUid.isNullOrBlank()) context.startActivity(Intent(context, ProfileActivity::class.java).putExtra(ProfileActivity.EXTRA_USER_ID, tournament?.hostUid).putExtra(ProfileActivity.EXTRA_DISPLAY_NAME, organizerName).putExtra(ProfileActivity.EXTRA_PROFILE_PHOTO_URL, photoUrl))
        }.padding(18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(FinalPanelLight), contentAlignment = Alignment.Center) {
                if (photoUrl.isNotBlank()) AsyncImage(model = photoUrl, contentDescription = "Organizer profile photo", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                else Text(organizerName.take(1).uppercase(), color = FinalAccent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.padding(start = 11.dp).weight(1f)) {
                Text("Organizer", color = FinalMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(organizerName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("›", color = FinalMuted, fontSize = 24.sp)
        }
    }
}

@Composable
private fun TournamentDetailsCard(
    tournament: Tournament?,
    onSave: (String, String, String, String) -> Unit,
    canEdit: Boolean
) {
    var isEditing by remember(tournament?.id) { mutableStateOf(false) }
    var name by remember(tournament?.id) { mutableStateOf(tournament?.name.orEmpty()) }
    var startDate by remember(tournament?.id) { mutableStateOf(tournament?.startDate.orEmpty()) }
    var ground by remember(tournament?.id) { mutableStateOf(tournament?.ground.orEmpty()) }
    var ballType by remember(tournament?.id) { mutableStateOf(tournament?.ballType.orEmpty()) }
    Column(
        Modifier.fillMaxWidth().shadow(7.dp, FinalCardShape).clip(FinalCardShape).background(FinalPanel).padding(18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(3.dp).height(14.dp).clip(CircleShape).background(FinalAccent))
            Text("Tournament Details", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 7.dp))
            if (canEdit) Text(
                if (isEditing) "SAVE" else "✎ Edit",
                color = FinalAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    if (isEditing) onSave(name.trim(), startDate.trim(), ground.trim(), ballType.trim())
                    isEditing = !isEditing
                }.padding(4.dp)
            )
        }
        Spacer(Modifier.height(9.dp))
        OverviewEditableDetail("Name", name, isEditing, { name = it })
        OverviewEditableDetail("Date", startDate, isEditing, { startDate = it })
        OverviewEditableDetail("Grounds", ground, isEditing, { ground = it }, locationMarker = true)
        OverviewEditableDetail("Ball type", ballType, isEditing, { ballType = it }, chip = true)
        OverviewEditableDetail("Category", tournament?.type?.ifBlank { "Tournament" } ?: "Tournament", false, {}, chip = true)
        OverviewTournamentId(tournament?.id.orEmpty())
    }
}

@Composable
private fun TeamDetailsCard(tournament: Tournament?, onSave: (String, String) -> Unit, canEdit: Boolean) {
    var isEditing by remember(tournament?.id) { mutableStateOf(false) }
    var entryFee by remember(tournament?.id) { mutableStateOf(tournament?.requirements?.entryFee.orEmpty()) }
    var teamCapacity by remember(tournament?.id) { mutableStateOf(tournament?.requirements?.numberOfTeams.orEmpty()) }
    Column(
        Modifier.fillMaxWidth().shadow(7.dp, FinalCardShape).clip(FinalCardShape).background(FinalPanel).padding(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(3.dp).height(14.dp).clip(CircleShape).background(FinalAccent))
            Text("Team Details", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 7.dp))
            if (canEdit) Text(
                if (isEditing) "SAVE" else "✎ Edit",
                color = FinalAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    if (isEditing) onSave(entryFee.trim(), teamCapacity.trim())
                    isEditing = !isEditing
                }.padding(4.dp)
            )
        }
        Spacer(Modifier.height(9.dp))
        OverviewEditableDetail("Entry fee", entryFee, isEditing, { entryFee = it }, emptyLabel = "0")
        OverviewEditableDetail("Team capacity", teamCapacity, isEditing, { teamCapacity = it })
    }
}

@Composable
private fun OverviewEditableDetail(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    valueColor: Color = Color.White,
    emptyLabel: String = "Not set",
    chip: Boolean = false,
    locationMarker: Boolean = false
) {
    if (isEditing) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = FinalBg,
                unfocusedContainerColor = FinalBg,
                cursorColor = FinalAccent,
                focusedBorderColor = FinalAccent,
                unfocusedBorderColor = FinalDivider,
                focusedLabelColor = FinalAccent,
                unfocusedLabelColor = FinalMuted
            )
        )
    } else {
        val displayedValue = value.ifBlank { emptyLabel }
        if (label == "Ball type") {
            val ballColor = when {
                displayedValue.contains("tennis", ignoreCase = true) -> Color(0xFF37DDEB)
                displayedValue.contains("leather", ignoreCase = true) -> Color(0xFFFF5B5B)
                else -> FinalAccent
            }
            OverviewDetail(label, displayedValue, ballColor, chip = true)
        } else {
            OverviewDetail(label, displayedValue, valueColor, chip, locationMarker = locationMarker)
        }
    }
}

@Composable
private fun OverviewDetail(
    label: String,
    value: String,
    valueColor: Color = Color.White,
    chip: Boolean = false,
    locationMarker: Boolean = false
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = FinalMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (locationMarker) Box(Modifier.padding(end = 5.dp).size(6.dp).clip(CircleShape).background(Color(0xFF45D9E8)))
        Box(
            Modifier.shadow(if (chip) 4.dp else 0.dp, RoundedCornerShape(7.dp))
                .clip(RoundedCornerShape(7.dp)).background(if (chip) FinalPanelLight else Color.Transparent)
                .padding(horizontal = if (chip) 8.dp else 0.dp, vertical = if (chip) 5.dp else 0.dp)
        ) {
            Text(value, color = valueColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun OverviewTournamentId(id: String) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    Column(Modifier.fillMaxWidth().padding(top = 3.dp)) {
        Text("Tournament ID", color = FinalMuted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(9.dp)).clip(RoundedCornerShape(9.dp)).background(FinalPanelLight).padding(horizontal = 9.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(id.ifBlank { "Not available" }, color = Color.White, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            if (id.isNotBlank()) Text("▢ Copy", color = FinalMuted, fontSize = 9.sp, modifier = Modifier.clickable {
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Tournament ID", id))
                Toast.makeText(context, "Tournament ID copied", Toast.LENGTH_SHORT).show()
            }.padding(start = 8.dp))
        }
    }
}

@Composable
private fun TournamentQrCard(tournament: Tournament?) {
    val context = LocalContext.current
    val tournamentLink = tournament?.id
        ?.takeIf { it.isNotBlank() }
        ?.let { tournamentId ->
            Uri.parse(context.getString(R.string.tournament_share_base_url))
                .buildUpon()
                .appendQueryParameter("id", tournamentId)
                .build()
                .toString()
        }
    val qrBitmap = remember(tournamentLink) {
        tournamentLink?.let(::tournamentQrBitmap)
    }
    Column(
        Modifier.fillMaxWidth().shadow(7.dp, FinalCardShape).clip(FinalCardShape).background(FinalPanel).padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Tournament QR code", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("↗  ↶", color = FinalMuted, fontSize = 16.sp)
        }
        Spacer(Modifier.height(16.dp))
        Box(Modifier.size(142.dp).clip(RoundedCornerShape(10.dp)).background(Color.White).padding(8.dp)) {
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Tournament QR code",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            if (qrBitmap == null) "Preparing tournament QR code…" else "Let cricketers find this tournament\neasily with QR code.",
            color = FinalMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp
        )
    }
}

private fun tournamentQrBitmap(value: String): Bitmap {
    val matrix = QRCodeWriter().encode(
        value,
        BarcodeFormat.QR_CODE,
        512,
        512,
        mapOf(EncodeHintType.MARGIN to 1)
    )
    return Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888).apply {
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
    }
}

@Composable
private fun HelpContactButton(label: String, modifier: Modifier) {
    Box(modifier.height(36.dp).clip(RoundedCornerShape(10.dp)).background(FinalPanelLight).border(1.dp, FinalDivider, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AboutStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(FinalPanel).border(1.dp, FinalDivider, RoundedCornerShape(14.dp)).padding(11.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(5.dp))
        Text(label, color = FinalMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun AboutCard(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(FinalPanel).border(1.dp, FinalDivider, RoundedCornerShape(16.dp)).padding(15.dp)
    ) {
        Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun AboutDetail(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = FinalMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MatchesTab(tournament: Tournament?, initialMatchTab: Int) {
    val context = LocalContext.current
    val homeCardFactory = remember(context) { HomeScreenView(context) }
    val tournamentName = tournament?.name?.ifBlank { "Tournament" } ?: "Tournament"
    var selectedMatchTab by remember(tournament?.id) { mutableIntStateOf(initialMatchTab.coerceIn(0, 2)) }
    val matchTabs = listOf("Live", "Upcoming", "Completed")
    var tournamentMatches by remember(tournament?.id) { mutableStateOf(emptyList<ScheduledTournamentMatch>()) }
    var isLoadingMatches by remember(tournament?.id) { mutableStateOf(true) }
    var matchesError by remember(tournament?.id) { mutableStateOf<String?>(null) }
    val liveStatuses = setOf("LIVE", "IN_PROGRESS", "PAUSED", "INNINGS_BREAK", "MATCH_BREAK", "BREAK")
    val completedStatuses = setOf("COMPLETED", "FINISHED", "ABANDONED", "FINALIZATION_PENDING", "FINALIZING", "FINALIZED")
    val liveMatches = tournamentMatches.filter { it.status in liveStatuses }
    val upcomingMatches = tournamentMatches.filter { it.status !in liveStatuses && it.status !in completedStatuses && it.scheduleStatus == "SCHEDULED" }
    val completedMatches = tournamentMatches.filter { it.status in completedStatuses }
    DisposableEffect(tournament?.id) {
        val tournamentId = tournament?.id.orEmpty()
        if (tournamentId.isBlank()) {
            isLoadingMatches = false
            return@DisposableEffect onDispose { }
        }
        val registration = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("matches")
            .whereEqualTo("tournamentId", tournamentId)
            .addSnapshotListener { snapshots, error ->
                isLoadingMatches = false
                if (error != null) {
                    matchesError = error.message ?: "Unable to load tournament matches"
                    tournamentMatches = emptyList()
                } else {
                    matchesError = null
                    tournamentMatches = snapshots?.documents.orEmpty().map { document ->
                        val teamAMap = document.get("teamA") as? Map<*, *>
                        val teamBMap = document.get("teamB") as? Map<*, *>
                        val teamA = document.getString("teamAName") ?: teamAMap?.get("name") as? String ?: "Team A"
                        val teamB = document.getString("teamBName") ?: teamBMap?.get("name") as? String ?: "Team B"
                        val teamAId = document.getString("teamAId") ?: (teamAMap?.get("teamId") as? String).orEmpty()
                        val teamBId = document.getString("teamBId") ?: (teamBMap?.get("teamId") as? String).orEmpty()
                        val liveScore = document.get("liveScore") as? Map<*, *> ?: emptyMap<Any, Any>()
                        val innings = document.get("innings") as? List<Map<String, Any?>> ?: emptyList()
                        val inningsScores = innings.mapNotNull { inning ->
                            val teamId = inning["battingTeamId"] as? String
                            if (teamId == null) return@mapNotNull null
                            val score = (inning["score"] as? Number)?.toInt() ?: 0
                            val wickets = (inning["wickets"] as? Number)?.toInt() ?: 0
                            val balls = (inning["legalBalls"] as? Number)?.toInt() ?: 0
                            teamId to ("$score/$wickets" to "${balls / 6}.${balls % 6} OV")
                        }.toMap()
                        val latestInnings = innings.lastOrNull()
                        val liveTeamId = latestInnings?.get("battingTeamId") as? String
                        val liveScoreText = "${(liveScore["score"] as? Number)?.toInt() ?: (latestInnings?.get("score") as? Number)?.toInt() ?: 0}/${(liveScore["wickets"] as? Number)?.toInt() ?: (latestInnings?.get("wickets") as? Number)?.toInt() ?: 0}"
                        val liveOvers = liveScore["overs"] as? String ?: run {
                            val balls = (latestInnings?.get("legalBalls") as? Number)?.toInt() ?: 0
                            "${balls / 6}.${balls % 6}"
                        }
                        val scoreA = inningsScores[teamAId] ?: ("—" to "TEAM A")
                        val scoreB = inningsScores[teamBId] ?: ("—" to "TEAM B")
                        val status = (document.getString("status") ?: document.getString("scheduleStatus").orEmpty()).uppercase()
                        val dateMillis = (document.get("matchDateEpochMs") as? Number)?.toLong() ?: 0L
                        ScheduledTournamentMatch(
                            id = document.id,
                            tournamentName = document.getString("tournamentName").orEmpty().ifBlank { tournamentName },
                            round = document.getString("roundName").orEmpty().ifBlank { tournament?.requirements?.tournamentFormat.orEmpty().ifBlank { "Tournament fixture" } },
                            teamA = teamA,
                            teamB = teamB,
                            date = if (dateMillis > 0L) java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(dateMillis)) else "Date not set",
                            time = document.getString("matchTime").orEmpty().ifBlank { "Time not set" },
                            venue = document.getString("venue").orEmpty().ifBlank { "Venue not set" },
                            status = status,
                            scheduleStatus = document.getString("scheduleStatus").orEmpty().uppercase(),
                            teamAScore = if (liveTeamId == teamAId) liveScoreText else scoreA.first,
                            teamAOvers = if (liveTeamId == teamAId) "$liveOvers OV" else scoreA.second,
                            teamBScore = if (liveTeamId == teamBId) liveScoreText else scoreB.first,
                            teamBOvers = if (liveTeamId == teamBId) "$liveOvers OV" else scoreB.second,
                            note = liveScore["matchStatusNote"] as? String ?: document.getString("result").orEmpty()
                        )
                    }.sortedBy { match ->
                        snapshots?.documents?.firstOrNull { it.id == match.id }?.getLong("matchDateEpochMs") ?: Long.MAX_VALUE
                    }
                }
            }
        onDispose { registration.remove() }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Matches", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
                Text("Manage every tournament fixture", color = FinalMuted, fontSize = 12.sp)
            }
            val matchCountColor = if (selectedMatchTab == 1) Color(0xFFFFC107) else FinalAccent
            val matchCountLabel = when (selectedMatchTab) {
                1 -> "${upcomingMatches.size} UPCOMING"
                2 -> "${completedMatches.size} COMPLETED"
                else -> "${liveMatches.size} LIVE"
            }
            Box(Modifier.clip(RoundedCornerShape(16.dp)).background(matchCountColor.copy(alpha = .13f)).padding(horizontal = 11.dp, vertical = 7.dp)) {
                Text(matchCountLabel, color = matchCountColor, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
        MatchStatusTabs(matchTabs, selectedMatchTab) { selectedMatchTab = it }
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MatchStatusContent(
                selectedTab = selectedMatchTab,
                tournamentName = tournamentName,
                homeCardFactory = homeCardFactory,
                liveMatches = liveMatches,
                upcomingMatches = upcomingMatches,
                completedMatches = completedMatches,
                isLoadingMatches = isLoadingMatches,
                matchesError = matchesError
            )
        }
        MatchActions(
            modifier = Modifier,
            onSchedule = {
                context.startActivity(
                    Intent(context, StartMatchActivity::class.java)
                        .putExtra(StartMatchActivity.EXTRA_SCHEDULE_FLOW, true)
                        .putExtra(RegisterTournamentFinalPageActivity.EXTRA_TOURNAMENT_ID, tournament?.id)
                )
            },
            onStart = {
                context.startActivity(
                    Intent(context, StartMatchActivity::class.java)
                        .putExtra(StartMatchActivity.EXTRA_TOURNAMENT_ONLY_FLOW, true)
                        .putExtra(RegisterTournamentFinalPageActivity.EXTRA_TOURNAMENT_ID, tournament?.id)
                )
            }
        )
    }
}

@Composable
private fun MatchStatusTabs(tabs: List<String>, selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp).clip(FinalCardShape).background(FinalPanel).border(1.dp, FinalDivider, FinalCardShape).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            val selected = selectedTab == index
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(11.dp))
                    .background(if (selected) Brush.horizontalGradient(listOf(FinalAccent.copy(alpha = .20f), FinalAccent.copy(alpha = .07f))) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)))
                    .border(1.dp, if (selected) FinalAccent.copy(alpha = .55f) else Color.Transparent, RoundedCornerShape(11.dp))
                    .clickable { onTabSelected(index) }.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) { Text(label, color = if (selected) FinalAccent else FinalMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

private data class ScheduledTournamentMatch(
    val id: String,
    val tournamentName: String,
    val round: String,
    val teamA: String,
    val teamB: String,
    val date: String,
    val time: String,
    val venue: String,
    val status: String,
    val scheduleStatus: String,
    val teamAScore: String,
    val teamAOvers: String,
    val teamBScore: String,
    val teamBOvers: String,
    val note: String
)

@Composable
private fun MatchStatusContent(
    selectedTab: Int,
    tournamentName: String,
    homeCardFactory: HomeScreenView,
    liveMatches: List<ScheduledTournamentMatch>,
    upcomingMatches: List<ScheduledTournamentMatch>,
    completedMatches: List<ScheduledTournamentMatch>,
    isLoadingMatches: Boolean,
    matchesError: String?
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val visibleMatches = when (selectedTab) {
            0 -> liveMatches
            1 -> upcomingMatches
            else -> completedMatches
        }
        if (visibleMatches.isNotEmpty()) {
            visibleMatches.forEach { match ->
                val isLive = match.status in setOf("LIVE", "IN_PROGRESS", "PAUSED", "INNINGS_BREAK", "MATCH_BREAK", "BREAK")
                val isUpcoming = match.scheduleStatus == "SCHEDULED" && !isLive
                AndroidView(
                    factory = { context ->
                        homeCardFactory.createHeroScoreCard(
                            context = context,
                            league = match.tournamentName.uppercase(),
                            round = match.round,
                            leftName = match.teamA,
                            leftScore = if (isUpcoming) "—" else match.teamAScore,
                            leftOvers = if (isUpcoming) "TEAM A" else match.teamAOvers,
                            rightName = match.teamB,
                            rightScore = if (isUpcoming) "—" else match.teamBScore,
                            rightOvers = if (isUpcoming) "TEAM B" else match.teamBOvers,
                            target = if (isUpcoming) match.date else "",
                            rrr = match.time,
                            win = match.venue.take(12).uppercase(),
                            note = when {
                                isUpcoming -> "Venue: ${match.venue}"
                                match.note.isNotBlank() -> match.note
                                else -> "Venue: ${match.venue}"
                            },
                            statusTag = when {
                                isUpcoming -> "UPCOMING"
                                match.status == "PAUSED" -> "PAUSED"
                                match.status in setOf("INNINGS_BREAK", "MATCH_BREAK", "BREAK") -> "MATCH BREAK"
                                selectedTab == 2 -> "COMPLETED"
                                else -> "LIVE"
                            },
                            statusColor = when {
                                isUpcoming -> android.graphics.Color.rgb(255, 193, 7)
                                match.status == "PAUSED" -> android.graphics.Color.rgb(255, 192, 77)
                                selectedTab == 2 -> android.graphics.Color.rgb(184, 198, 193)
                                else -> android.graphics.Color.rgb(255, 38, 54)
                            },
                            scheduledDate = match.date.takeIf { isUpcoming },
                            scheduledTime = match.time.takeIf { isUpcoming },
                            scheduledVenue = match.venue.take(12).uppercase().takeIf { isUpcoming },
                            canOpenScorecard = !isUpcoming,
                            matchId = match.id
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(272.dp)
                )
            }
        } else {
            val title = when {
                isLoadingMatches -> "Loading tournament matches…"
                matchesError != null -> "Couldn't load tournament matches"
                selectedTab == 0 -> "No live matches"
                selectedTab == 1 -> "No upcoming matches"
                else -> "No completed matches"
            }
            val message = when {
                matchesError != null -> matchesError
                selectedTab == 0 -> "Live, paused, and match break fixtures will appear here."
                selectedTab == 1 -> "Scheduled tournament fixtures will appear here."
                else -> "Finished scorecards will appear here."
            }
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(FinalPanel).border(1.dp, FinalDivider, RoundedCornerShape(16.dp)).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(7.dp))
                Text(message, color = FinalMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun MatchActions(modifier: Modifier, onSchedule: () -> Unit, onStart: () -> Unit) {
    Row(modifier.fillMaxWidth().background(FinalBg).padding(horizontal = 18.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(14.dp)).border(1.dp, FinalAccent, RoundedCornerShape(14.dp)).clickable(onClick = onSchedule), contentAlignment = Alignment.Center) {
            Text("Schedule Match", color = FinalAccent, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
        Box(Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(14.dp)).background(FinalAccent).clickable(onClick = onStart), contentAlignment = Alignment.Center) {
            Text("Start A Match", color = FinalBg, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun TeamsTab(tournament: Tournament?, canManage: Boolean) {
    val context = LocalContext.current
    val firestore = remember { com.google.firebase.firestore.FirebaseFirestore.getInstance() }
    var isCreatingInvite by remember { mutableStateOf(false) }
    var isAddingTeamId by remember { mutableStateOf<String?>(null) }
    var isRemovingTeamId by remember { mutableStateOf<String?>(null) }
    var teamPendingRemoval by remember { mutableStateOf<TournamentJoinedTeam?>(null) }
    var showAddTeamsMenu by remember { mutableStateOf(false) }
    var showManualTeamPicker by remember { mutableStateOf(false) }
    var availableTeams by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var joinedTeams by remember(tournament?.id) { mutableStateOf(emptyList<TournamentJoinedTeam>()) }
    DisposableEffect(tournament?.id) {
        val tournamentId = tournament?.id.orEmpty()
        if (tournamentId.isBlank()) return@DisposableEffect onDispose { }
        val registration = firestore
            .collection("tournaments").document(tournamentId).collection("teams")
            .addSnapshotListener { snapshots, _ ->
                val loadedTeams = snapshots?.documents.orEmpty().map { document ->
                    TournamentJoinedTeam(
                        id = document.id,
                        name = document.getString("teamName")?.trim().orEmpty().ifBlank { "Team" },
                        captainUserId = document.getString("captainUserId").orEmpty(),
                        logoUrl = listOf("logoUrl", "teamLogoUrl", "photoUrl").firstNotNullOfOrNull { document.getString(it)?.takeIf(String::isNotBlank) }.orEmpty()
                    )
                }.sortedBy { it.name.lowercase() }
                joinedTeams = loadedTeams
                loadedTeams.filter { it.logoUrl.isBlank() }.forEach { team ->
                    firestore.collection("teams").document(team.id).get().addOnSuccessListener { details ->
                        val logo = listOf("logoUrl", "teamLogoUrl", "photoUrl").firstNotNullOfOrNull { details.getString(it)?.takeIf(String::isNotBlank) }.orEmpty()
                        if (logo.isNotBlank()) joinedTeams = joinedTeams.map { if (it.id == team.id) it.copy(logoUrl = logo) else it }
                    }
                }
            }
        onDispose { registration.remove() }
    }
    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    DisposableEffect(currentUserId) {
        if (currentUserId.isBlank()) return@DisposableEffect onDispose { }
        val registration = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("teams")
            .whereArrayContains("memberIds", currentUserId)
            .addSnapshotListener { snapshots, _ ->
                availableTeams = snapshots?.documents.orEmpty().mapNotNull { document ->
                    val name = document.getString("teamName")?.trim().orEmpty()
                        .ifBlank { document.getString("name")?.trim().orEmpty() }
                        .ifBlank { "Team" }
                    document.id to name
                }.sortedBy { it.second.lowercase() }
            }
        onDispose { registration.remove() }
    }
    fun shareInvite() {
        val tournamentId = tournament?.id.orEmpty()
        if (tournamentId.isBlank()) {
            Toast.makeText(context, "Tournament is still loading", Toast.LENGTH_SHORT).show()
            return
        }
        isCreatingInvite = true
        FirebaseFunctions.getInstance().getHttpsCallable("createTournamentInvite")
            .call(mapOf("tournamentId" to tournamentId))
            .addOnSuccessListener { result ->
                isCreatingInvite = false
                val data = result.data as? Map<*, *> ?: return@addOnSuccessListener
                val url = data["invitationUrl"] as? String ?: return@addOnSuccessListener
                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Register your team for ${tournament?.name ?: "this tournament"}: $url")
                }, "Share tournament invitation"))
            }
            .addOnFailureListener { error ->
                isCreatingInvite = false
                Toast.makeText(context, error.message ?: "Unable to create invitation", Toast.LENGTH_LONG).show()
            }
    }
    fun addTeamManually(teamId: String, teamName: String) {
        val tournamentId = tournament?.id.orEmpty()
        if (tournamentId.isBlank()) {
            Toast.makeText(context, "Tournament is still loading", Toast.LENGTH_SHORT).show()
            return
        }
        isAddingTeamId = teamId
        FirebaseFunctions.getInstance().getHttpsCallable("addTournamentTeamManually")
            .call(mapOf("tournamentId" to tournamentId, "teamId" to teamId))
            .addOnSuccessListener {
                isAddingTeamId = null
                showManualTeamPicker = false
                Toast.makeText(context, "$teamName added to tournament", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                isAddingTeamId = null
                Toast.makeText(context, error.message ?: "Unable to add team", Toast.LENGTH_LONG).show()
            }
    }
    fun removeTournamentTeam(team: TournamentJoinedTeam) {
        val tournamentId = tournament?.id.orEmpty()
        if (!canManage || tournamentId.isBlank()) return
        isRemovingTeamId = team.id
        FirebaseFunctions.getInstance().getHttpsCallable("removeTournamentTeam")
            .call(mapOf("tournamentId" to tournamentId, "teamId" to team.id))
            .addOnSuccessListener {
                isRemovingTeamId = null
                teamPendingRemoval = null
                Toast.makeText(context, "${team.name} removed from tournament", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                isRemovingTeamId = null
                Toast.makeText(context, error.message ?: "Unable to remove team", Toast.LENGTH_LONG).show()
            }
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Teams", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("Build your lineup before fixtures begin", color = FinalMuted, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Box(Modifier.clip(RoundedCornerShape(20.dp)).background(FinalAccent.copy(alpha = .13f)).padding(horizontal = 11.dp, vertical = 7.dp)) {
                    Text("${joinedTeams.size} TEAMS", color = FinalAccent, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
                if (canManage && joinedTeams.isNotEmpty()) {
                    Box {
                        Row(
                            Modifier.shadow(10.dp, RoundedCornerShape(14.dp), ambientColor = FinalAccent, spotColor = FinalAccent)
                                .clip(RoundedCornerShape(14.dp))
                                .background(FinalAccent)
                                .clickable { showAddTeamsMenu = true }
                                .padding(horizontal = 11.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text("+", color = Color(0xFF101604), fontSize = 18.sp, fontWeight = FontWeight.Black, lineHeight = 18.sp)
                            Text("ADD TEAMS", color = Color(0xFF101604), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = .3.sp)
                        }
                        DropdownMenu(expanded = showAddTeamsMenu, onDismissRequest = { showAddTeamsMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("SHARE INVITE LINK") },
                                onClick = { showAddTeamsMenu = false; shareInvite() },
                                enabled = !isCreatingInvite
                            )
                            DropdownMenuItem(
                                text = { Text("ADD TEAM MANUALLY") },
                                onClick = { showAddTeamsMenu = false; showManualTeamPicker = true }
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(14.dp)).background(FinalPanel), contentAlignment = Alignment.CenterStart) {
            Text("⌕   Search teams", color = FinalMuted, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp))
        }
        Spacer(Modifier.height(16.dp))
        if (joinedTeams.isNotEmpty()) {
            Text("REGISTERED TEAMS", color = FinalMuted, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            joinedTeams.forEachIndexed { index, team ->
                TournamentTeamCard(team, index + 1, canManage, isRemovingTeamId == team.id) { teamPendingRemoval = team }
                Spacer(Modifier.height(10.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        if (joinedTeams.isEmpty() && canManage) Box(Modifier.fillMaxWidth().clip(FinalCardShape).background(FinalPanel).border(1.dp, FinalDivider, FinalCardShape).padding(horizontal = 20.dp, vertical = 26.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(84.dp).clip(CircleShape).background(FinalAccent.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                    Text("01", color = FinalAccent, fontSize = 26.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(20.dp))
                Text("Invite captains to add teams", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                Spacer(Modifier.height(9.dp))
                Text("Share one link and let captains submit their\nteams and player details directly.", color = FinalMuted, fontSize = 13.sp, lineHeight = 19.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                FinalActionButton(if (isCreatingInvite) "CREATING LINK…" else "SHARE INVITE LINK", filled = true, enabled = !isCreatingInvite, onClick = ::shareInvite)
                Text("OR", color = Color(0xFF65718A), fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 17.dp))
                FinalActionButton("ADD A TEAM MANUALLY", filled = false, onClick = { showManualTeamPicker = true })
            }
        }
        if (joinedTeams.isEmpty() && !canManage) {
            Box(Modifier.fillMaxWidth().clip(FinalCardShape).background(FinalPanel).border(1.dp, FinalDivider, FinalCardShape).padding(24.dp), contentAlignment = Alignment.Center) {
                Text("No teams have registered yet.", color = FinalMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }
        if (showManualTeamPicker) {
            AlertDialog(
                onDismissRequest = { if (isAddingTeamId == null) showManualTeamPicker = false },
                title = { Text("ADD A TEAM MANUALLY", color = Color.White, fontWeight = FontWeight.Black) },
                text = {
                    val teamsToAdd = availableTeams.filterNot { (teamId, _) -> joinedTeams.any { it.id == teamId } }
                    if (teamsToAdd.isEmpty()) {
                        Text("No teams are available to add.", color = FinalMuted)
                    } else {
                        Column(Modifier.height(240.dp).verticalScroll(rememberScrollState())) {
                            teamsToAdd.forEach { (teamId, teamName) ->
                                Row(
                                    Modifier.fillMaxWidth().clickable(enabled = isAddingTeamId == null) {
                                        addTeamManually(teamId, teamName)
                                    }.padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(teamName, color = Color.White, modifier = Modifier.weight(1f))
                                    Text(if (isAddingTeamId == teamId) "ADDING…" else "ADD", color = FinalAccent, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = { Text("CLOSE", color = FinalAccent, modifier = Modifier.clickable { if (isAddingTeamId == null) showManualTeamPicker = false }.padding(12.dp)) },
                containerColor = FinalPanel
            )
        }
        teamPendingRemoval?.let { team ->
            AlertDialog(
                onDismissRequest = { if (isRemovingTeamId == null) teamPendingRemoval = null },
                title = { Text("REMOVE TEAM?", color = Color.White, fontWeight = FontWeight.Black) },
                text = { Text("Remove ${team.name} from this tournament?", color = FinalMuted) },
                confirmButton = {
                    Text(
                        if (isRemovingTeamId == team.id) "REMOVING…" else "REMOVE",
                        color = Color(0xFFFF625D),
                        modifier = Modifier.clickable(enabled = isRemovingTeamId == null) { removeTournamentTeam(team) }.padding(12.dp)
                    )
                },
                dismissButton = { Text("CANCEL", color = FinalMuted, modifier = Modifier.clickable(enabled = isRemovingTeamId == null) { teamPendingRemoval = null }.padding(12.dp)) },
                containerColor = FinalPanel
            )
        }
        Spacer(Modifier.height(20.dp))
        Text("You can edit teams and players anytime before\npublishing the tournament.", color = FinalMuted, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 17.sp)
    }
}

private data class TournamentJoinedTeam(val id: String, val name: String, val captainUserId: String, val logoUrl: String = "")

@Composable
private fun TournamentTeamCard(team: TournamentJoinedTeam, position: Int, canManage: Boolean, isRemoving: Boolean, onRemove: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(FinalPanel).border(1.dp, FinalDivider, RoundedCornerShape(15.dp)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(FinalAccent.copy(alpha = .13f)), contentAlignment = Alignment.Center) {
            if (team.logoUrl.isNotBlank()) {
                AsyncImage(model = team.logoUrl, contentDescription = "${team.name} team logo", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
            } else {
                Text(position.toString().padStart(2, '0'), color = FinalAccent, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(team.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("REGISTERED TEAM", color = FinalAccent, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 3.dp))
        }
        if (canManage) {
            Image(
                painter = painterResource(R.drawable.outline_delete_24),
                contentDescription = "Remove ${team.name} from tournament",
                modifier = Modifier.size(24.dp).clickable(enabled = !isRemoving, onClick = onRemove),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(if (isRemoving) FinalMuted else Color(0xFFFF625D))
            )
        } else {
            Text("✓", color = FinalAccent, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
    }
}

private data class PointsGroupTeam(val id: String, val name: String, val logoUrl: String = "")
private data class PointsGroup(val name: String, val teams: List<PointsGroupTeam>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PointsTab(
    tournamentId: String,
    groups: List<PointsGroup>,
    selectedGroupName: String?,
    onSelectGroup: (String) -> Unit,
    onCreateGroup: (String) -> Unit,
    onDeleteGroup: (String) -> Unit,
    onTeamsChange: (List<PointsGroupTeam>) -> Unit,
    canEdit: Boolean
) {
    var draftGroupName by remember { mutableStateOf("") }
    var showCreateGroupSheet by remember { mutableStateOf(false) }
    var showNameSuggestions by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val activeGroup = groups.firstOrNull { it.name == selectedGroupName } ?: groups.firstOrNull()
    val registeredTeamPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val selectedId = result.data?.getStringExtra(SelectPlayingTeamsActivity.EXTRA_SELECTED_TEAM_ID).orEmpty()
            val selectedName = result.data?.getStringExtra(SelectPlayingTeamsActivity.EXTRA_SELECTED_TEAM_NAME).orEmpty()
            val selectedLogo = result.data?.getStringExtra("selected_team_logo_url").orEmpty()
            if (selectedId.isNotBlank()) {
                val selectedTeams = activeGroup?.teams.orEmpty()
                if (selectedTeams.none { it.id == selectedId }) {
                    onTeamsChange(selectedTeams + PointsGroupTeam(selectedId, selectedName.ifBlank { "Team" }, selectedLogo))
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (groups.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groups.forEach { group ->
                        val selected = group.name == activeGroup?.name
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selected) FinalAccent else FinalBg)
                                .border(1.dp, if (selected) FinalAccent else FinalDivider, RoundedCornerShape(20.dp))
                                .clickable { onSelectGroup(group.name) }
                                .padding(horizontal = 16.dp, vertical = 9.dp)
                        ) {
                            Text(group.name, color = if (selected) FinalBg else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (activeGroup != null) {
                    GroupPointsTab(
                        groupName = activeGroup.name,
                        teams = activeGroup.teams,
                        onAddTeam = {
                            if (canEdit && tournamentId.isNotBlank()) {
                                registeredTeamPicker.launch(
                                    Intent(context, RegisteredTeamsOfATournament::class.java)
                                        .putExtra(SelectPlayingTeamsActivity.EXTRA_TOURNAMENT_ID, tournamentId)
                                )
                            } else if (canEdit) {
                                Toast.makeText(context, "Tournament is still loading", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDeleteGroup = { if (canEdit) onDeleteGroup(activeGroup.name) },
                        onRemoveTeam = { team ->
                            if (canEdit) onTeamsChange(activeGroup.teams.filterNot { it.id == team.id })
                        },
                        canEdit = canEdit
                    )
                } else {
                    NoGroupsPointsTab(canEdit)
                }
            }
        }
        if (canEdit) CreateGroupButton {
            draftGroupName = ""
            showCreateGroupSheet = true
        }
    }

    if (showCreateGroupSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateGroupSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = FinalPanel,
            contentColor = Color.White,
            scrimColor = Color.Black.copy(alpha = 0.68f),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Create new group", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("Name the group for its teams and points table.", color = FinalMuted, fontSize = 13.sp)
                OutlinedTextField(
                    value = draftGroupName,
                    onValueChange = { if (it.length <= 30) draftGroupName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) keyboardController?.show()
                        },
                    label = { Text("Group name") },
                    placeholder = { Text("Type a custom name or choose below") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = FinalBg,
                        unfocusedContainerColor = FinalBg,
                        disabledContainerColor = FinalBg,
                        cursorColor = FinalAccent,
                        focusedBorderColor = FinalAccent,
                        unfocusedBorderColor = FinalMuted.copy(alpha = 0.75f),
                        focusedLabelColor = FinalAccent,
                        unfocusedLabelColor = FinalMuted,
                        focusedPlaceholderColor = FinalMuted,
                        unfocusedPlaceholderColor = FinalMuted
                    )
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(FinalBg)
                        .border(1.dp, FinalDivider, RoundedCornerShape(10.dp))
                        .clickable { showNameSuggestions = !showNameSuggestions }
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Suggested group", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Choose a standard group name", color = FinalMuted, fontSize = 11.sp)
                        }
                        Text(if (showNameSuggestions) "▴" else "▾", color = FinalAccent, fontSize = 18.sp)
                    }
                    if (showNameSuggestions) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("GROUP A", "GROUP B", "GROUP C", "GROUP D", "GROUP E").forEach { name ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (draftGroupName.equals(name, ignoreCase = true)) FinalAccent.copy(alpha = .16f) else FinalPanel)
                                        .clickable {
                                            draftGroupName = name
                                            showNameSuggestions = false
                                        }
                                        .padding(horizontal = 12.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    if (draftGroupName.equals(name, ignoreCase = true)) {
                                        Text("✓", color = FinalAccent, fontSize = 15.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(9.dp))
                            .border(1.dp, FinalAccent.copy(alpha = .7f), RoundedCornerShape(9.dp))
                            .clickable { showCreateGroupSheet = false },
                        contentAlignment = Alignment.Center
                    ) { Text("CANCEL", color = FinalAccent, fontSize = 12.sp, fontWeight = FontWeight.Black) }
                    Box(
                        Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(9.dp)).background(FinalAccent)
                            .clickable {
                                onCreateGroup(draftGroupName.trim().ifBlank { "GROUP A" })
                                showCreateGroupSheet = false
                            },
                        contentAlignment = Alignment.Center
                    ) { Text("OK", color = FinalBg, fontSize = 12.sp, fontWeight = FontWeight.Black) }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun NoGroupsPointsTab(canEdit: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 3.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(FinalBg)
                .border(1.dp, FinalDivider.copy(alpha = .72f), RoundedCornerShape(18.dp))
                .padding(horizontal = 28.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.nonstriker),
                contentDescription = "Cricketer",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(22.dp))
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .border(1.dp, FinalAccent.copy(alpha = .45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                TeamGlyph(Modifier.size(28.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(
                if (canEdit) "No Groups Yet" else "NO Points Tabale Created Yet By Organiser",
                color = Color.White,
                fontSize = if (canEdit) 22.sp else 18.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            if (canEdit) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Create groups and add teams\nto generate a points table.",
                    color = FinalMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CreateGroupButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .height(50.dp)
            .shadow(12.dp, RoundedCornerShape(7.dp), clip = false)
            .clip(RoundedCornerShape(7.dp))
            .background(FinalAccent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(19.dp)
                    .border(1.5.dp, FinalBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = FinalBg, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Text("CREATE NEW GROUP", color = FinalBg, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun GroupPointsTab(
    groupName: String,
    teams: List<PointsGroupTeam>,
    onAddTeam: () -> Unit,
    onDeleteGroup: () -> Unit,
    onRemoveTeam: (PointsGroupTeam) -> Unit,
    canEdit: Boolean
) {
    var contextTeam by remember { mutableStateOf<PointsGroupTeam?>(null) }
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(10.dp))
                .background(FinalBg).border(1.dp, FinalDivider, RoundedCornerShape(10.dp)).padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(groupName.uppercase(), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            if (canEdit) GroupAction(R.drawable.whiteuser, "Add Team", onAddTeam)
            if (canEdit) GroupAction(R.drawable.outline_delete_24, "Remove", onDeleteGroup)
        }
        if (teams.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WicketGlyph(Modifier.size(184.dp))
                    Spacer(Modifier.height(24.dp))
                    Text("This group has no teams", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(10.dp))
                    Text("Add registered teams to\ngenerate the points table.", color = FinalMuted, fontSize = 13.sp, lineHeight = 19.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(20.dp))
                    Box(
                        Modifier.height(40.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .border(1.dp, FinalAccent.copy(alpha = .7f), RoundedCornerShape(7.dp))
                            .clickable(onClick = onAddTeam)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(R.drawable.whiteuser),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(9.dp))
                            Text("Add Team", color = FinalAccent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        } else {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .background(FinalBg).border(1.dp, FinalDivider, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .padding(horizontal = 12.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TEAM", color = FinalAccent, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                listOf("M", "W", "L", "P", "NRR").forEach { title ->
                    Text(title, color = FinalAccent, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(if (title == "NRR") 42.dp else 30.dp), textAlign = TextAlign.Center)
                }
            }
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                    .background(FinalBg)
            ) {
                items(teams, key = { it.id }) { team ->
                    Box {
                        Row(
                            Modifier.fillMaxWidth()
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = { if (canEdit) contextTeam = team }
                                )
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(30.dp).clip(CircleShape).background(FinalAccent.copy(alpha = .18f)), contentAlignment = Alignment.Center) {
                                    if (team.logoUrl.isNotBlank()) {
                                        AsyncImage(model = team.logoUrl, contentDescription = "${team.name} team logo", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                                    } else {
                                        Text(team.name.take(1).uppercase(), color = FinalAccent, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                                Text(team.name.uppercase(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 10.dp))
                            }
                            listOf("0", "0", "0", "0", "0.00").forEachIndexed { index, value ->
                                Text(value, color = if (index == 3) Color.White else FinalMuted, fontSize = 11.sp, fontWeight = if (index == 3) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.width(if (index == 4) 42.dp else 30.dp), textAlign = TextAlign.Center)
                            }
                        }
                        DropdownMenu(
                            expanded = canEdit && contextTeam?.id == team.id,
                            onDismissRequest = { contextTeam = null },
                            modifier = Modifier.background(FinalPanel)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Remove from group", color = Color.White) },
                                onClick = {
                                    contextTeam = null
                                    onRemoveTeam(team)
                                },
                                colors = androidx.compose.material3.MenuDefaults.itemColors(textColor = Color.White)
                            )
                        }
                    }
                }
            }
            if (canEdit) {
                Text(
                    "Long press to delete team from the group",
                    color = FinalMuted,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun GroupAction(iconRes: Int, label: String, onClick: () -> Unit) {
    Column(
        Modifier.clip(RoundedCornerShape(7.dp)).clickable(onClick = onClick).padding(horizontal = 4.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Text(label, color = Color.White, fontSize = 7.sp, maxLines = 1)
    }
}

@Composable
private fun LeaderboardTab() {
    var selectedLeaderboardTab by remember { mutableIntStateOf(0) }
    val leaderboardTabs = listOf("Bat", "Bowl", "Field", "MVP")

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        Text("Leaderboard", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(4.dp))
        Text("Top performers from this tournament", color = FinalMuted, fontSize = 12.sp)
        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(FinalCardShape).background(FinalPanel).border(1.dp, FinalDivider, FinalCardShape).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            leaderboardTabs.forEachIndexed { index, label ->
                val selected = selectedLeaderboardTab == index
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                        .background(if (selected) Brush.horizontalGradient(listOf(FinalAccent.copy(alpha = .20f), FinalAccent.copy(alpha = .07f))) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)))
                        .border(1.dp, if (selected) FinalAccent.copy(alpha = .55f) else Color.Transparent, RoundedCornerShape(10.dp))
                        .clickable { selectedLeaderboardTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = if (selected) FinalAccent else FinalMuted, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        val category = leaderboardTabs[selectedLeaderboardTab]
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(FinalPanel)
                .border(1.dp, FinalDivider, RoundedCornerShape(16.dp)).padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No $category leaders yet", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Player rankings will appear here after matches are scored.", color = FinalMuted, fontSize = 13.sp, lineHeight = 19.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun PointTableRow(team: String, played: String, won: String, points: String, highlighted: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(team, color = if (highlighted) Color.White else FinalMuted, fontSize = 12.sp, fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
        Text(played, color = FinalMuted, fontSize = 12.sp, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
        Text(won, color = FinalMuted, fontSize = 12.sp, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
        Text(points, color = FinalAccent, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(38.dp), textAlign = TextAlign.Center)
    }
}

@Composable
private fun FinalActionButton(label: String, filled: Boolean, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit = {}) {
    Box(modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(14.dp)).background(if (filled) FinalAccent else FinalPanelLight).border(if (filled) 0.dp else 1.dp, FinalAccent.copy(alpha = .55f), RoundedCornerShape(14.dp)).clickable(enabled = enabled, onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, color = Color(0xFF101604).takeIf { filled } ?: FinalAccent, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}

@Composable
private fun ComingSoonTab(label: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$label will appear here once teams are added.", color = FinalMuted, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(32.dp))
    }
}

@Composable
private fun TeamGlyph(modifier: Modifier) = Canvas(modifier) {
    val stroke = Stroke(2.7.dp.toPx(), cap = StrokeCap.Round)
    val color = FinalAccent
    drawCircle(color, radius = size.width * .16f, center = androidx.compose.ui.geometry.Offset(size.width * .36f, size.height * .33f), style = stroke)
    drawCircle(color, radius = size.width * .16f, center = androidx.compose.ui.geometry.Offset(size.width * .67f, size.height * .33f), style = stroke)
    drawRoundRect(color, topLeft = androidx.compose.ui.geometry.Offset(size.width * .16f, size.height * .55f), size = androidx.compose.ui.geometry.Size(size.width * .68f, size.height * .3f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()), style = stroke)
}

@Composable
private fun WicketGlyph(modifier: Modifier) = Canvas(modifier) {
    val accent = FinalAccent.copy(alpha = .85f)
    val faint = FinalAccent.copy(alpha = .13f)
    val stroke = 1.5.dp.toPx()
    val baseY = size.height * .82f
    val topY = size.height * .22f
    val leftX = size.width * .36f
    val middleX = size.width * .5f
    val rightX = size.width * .64f
    drawCircle(faint, radius = size.width * .46f, center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2), style = Stroke(stroke))
    drawLine(faint, androidx.compose.ui.geometry.Offset(size.width * .08f, baseY), androidx.compose.ui.geometry.Offset(size.width * .92f, baseY), strokeWidth = stroke)
    listOf(leftX, middleX, rightX).forEach { x ->
        drawLine(accent, androidx.compose.ui.geometry.Offset(x, topY), androidx.compose.ui.geometry.Offset(x, baseY), strokeWidth = stroke)
    }
    drawLine(accent, androidx.compose.ui.geometry.Offset(leftX - 5.dp.toPx(), topY), androidx.compose.ui.geometry.Offset(middleX - 4.dp.toPx(), topY), strokeWidth = stroke)
    drawLine(accent, androidx.compose.ui.geometry.Offset(middleX + 4.dp.toPx(), topY), androidx.compose.ui.geometry.Offset(rightX + 5.dp.toPx(), topY), strokeWidth = stroke)
    drawCircle(FinalAccent, radius = 2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width * .18f, size.height * .24f))
    drawCircle(FinalAccent, radius = 2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width * .81f, size.height * .48f))
}

@Composable
private fun DeleteGlyph(modifier: Modifier) = Canvas(modifier) {
    val stroke = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round)
    val c = FinalAccent
    val left = size.width * .28f
    val right = size.width * .72f
    val top = size.height * .3f
    val bottom = size.height * .78f
    drawLine(c, androidx.compose.ui.geometry.Offset(left, top), androidx.compose.ui.geometry.Offset(right, top), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(c, androidx.compose.ui.geometry.Offset(left + 2.dp.toPx(), top), androidx.compose.ui.geometry.Offset(left + 3.dp.toPx(), bottom), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(c, androidx.compose.ui.geometry.Offset(right - 2.dp.toPx(), top), androidx.compose.ui.geometry.Offset(right - 3.dp.toPx(), bottom), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(c, androidx.compose.ui.geometry.Offset(left + 3.dp.toPx(), bottom), androidx.compose.ui.geometry.Offset(right - 3.dp.toPx(), bottom), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(c, androidx.compose.ui.geometry.Offset(size.width * .42f, size.height * .17f), androidx.compose.ui.geometry.Offset(size.width * .58f, size.height * .17f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(c, androidx.compose.ui.geometry.Offset(size.width * .43f, size.height * .41f), androidx.compose.ui.geometry.Offset(size.width * .43f, size.height * .65f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(c, androidx.compose.ui.geometry.Offset(size.width * .57f, size.height * .41f), androidx.compose.ui.geometry.Offset(size.width * .57f, size.height * .65f), strokeWidth = stroke.width, cap = StrokeCap.Round)
}

@Composable
private fun BackGlyph(modifier: Modifier) = Canvas(modifier) {
    val stroke = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
    val c = Color.White
    drawLine(c, androidx.compose.ui.geometry.Offset(size.width * .75f, size.height * .5f), androidx.compose.ui.geometry.Offset(size.width * .2f, size.height * .5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(c, androidx.compose.ui.geometry.Offset(size.width * .2f, size.height * .5f), androidx.compose.ui.geometry.Offset(size.width * .48f, size.height * .22f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(c, androidx.compose.ui.geometry.Offset(size.width * .2f, size.height * .5f), androidx.compose.ui.geometry.Offset(size.width * .48f, size.height * .78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
}
