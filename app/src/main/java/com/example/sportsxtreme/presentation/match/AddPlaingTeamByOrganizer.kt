package com.example.sportsxtreme.presentation.match

import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.team.*
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import coil3.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import android.widget.Toast

class AddPlayingTeamByOrganizer : ComponentActivity() {
    private var selectedPlayerIds by mutableStateOf<Set<String>>(emptySet())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        val teamId = intent.getStringExtra(SelectPlayingTeamsActivity.EXTRA_SELECTED_TEAM_ID).orEmpty()
        val matchId = intent.getStringExtra(SelectPlayingTeamsActivity.EXTRA_MATCH_ID).orEmpty()
        val teamSlot = intent.getStringExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_SLOT).orEmpty()
        val selectedTeamName = intent.getStringExtra(SelectPlayingTeamsActivity.EXTRA_SELECTED_TEAM_NAME).orEmpty()
        setContent {
            val uiState = rememberOrganizerTeamDetails(teamId)
            ViewDetailsStartMatchScreen(
                uiState = uiState,
                selectedPlayerIds = selectedPlayerIds,
                onTogglePlayer = { player -> selectedPlayerIds = if (player.userId in selectedPlayerIds) selectedPlayerIds - player.userId else selectedPlayerIds + player.userId },
                onBack = { finish() },
                onAddPlayer = {
                    startActivity(Intent(this, AddPlayerActivity::class.java)
                        .putExtra(ManagePlayersInsideTeamActivity.EXTRA_TEAM_ID, teamId)
                        .putExtra(ManagePlayersInsideTeamActivity.EXTRA_TEAM_NAME, uiState.details.name.ifBlank { selectedTeamName }))
                },
                onNext = {
                    val selected = uiState.details.members.filter { it.userId in selectedPlayerIds }
                    if (selected.isEmpty()) {
                        Toast.makeText(this, "Select at least one player for this match.", Toast.LENGTH_SHORT).show()
                    } else if (matchId.isBlank() || teamSlot.isBlank()) {
                        Toast.makeText(this, "Match information is missing. Reopen team selection and try again.", Toast.LENGTH_LONG).show()
                    } else {
                        saveOrganizerMatchSquad(matchId, teamId, teamSlot, selectedTeamName.ifBlank { uiState.details.name }, selected) {
                            val resultIntent = Intent().apply {
                                putExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_SLOT, teamSlot)
                                if (teamSlot == "A") {
                                    putExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_A_ID, teamId)
                                    putExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_A_NAME, selectedTeamName.ifBlank { uiState.details.name })
                                } else {
                                    putExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_B_ID, teamId)
                                    putExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_B_NAME, selectedTeamName.ifBlank { uiState.details.name })
                                }
                            }
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        }
                    }
                }
            )
        }
    }

    private fun saveOrganizerMatchSquad(matchId: String, teamId: String, teamSlot: String, teamName: String, players: List<OrganizerPlayer>, onSaved: () -> Unit) {
        val squad = mapOf(
            "teamId" to teamId,
            "teamName" to teamName,
            "players" to players.map { player -> mapOf("userId" to player.userId, "displayName" to player.name, "playingRole" to "Player", "permanentTeamRoles" to emptyList<String>(), "matchRoles" to emptyList<String>()) },
            "updatedAtEpochMs" to System.currentTimeMillis()
        )
        FirebaseFirestore.getInstance().collection("matches").document(matchId)
            .set(mapOf("matchSquads" to mapOf(teamSlot to squad)), com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener { onSaved() }
            .addOnFailureListener { error -> Toast.makeText(this, error.message ?: "Unable to save match players", Toast.LENGTH_LONG).show() }
    }

}

private val DetailsAccent = Color(0xFFC1FF00)
private val DetailsMuted = Color(0xFFADB8BD)

private data class OrganizerPlayer(
    val userId: String,
    val name: String,
    val photoUrl: String = "",
    val matches: Int = 0,
    val playedLastMatch: Boolean = false,
    val selected: Boolean = true
)

private data class OrganizerTeamDetails(
    val name: String = "",
    val location: String = "",
    val logoUrl: String = "",
    val members: List<OrganizerPlayer> = emptyList()
)

private data class OrganizerTeamUiState(
    val details: OrganizerTeamDetails = OrganizerTeamDetails(),
    val isLoadingPlayers: Boolean = true
)

@Composable
private fun rememberOrganizerTeamDetails(teamId: String): OrganizerTeamUiState {
    var state by remember(teamId) { mutableStateOf(OrganizerTeamUiState()) }
    androidx.compose.runtime.DisposableEffect(teamId) {
        if (teamId.isBlank()) return@DisposableEffect onDispose { }
        val firestore = FirebaseFirestore.getInstance()
        var matchListener: ListenerRegistration? = null
        val teamListener = firestore.collection("teams").document(teamId).addSnapshotListener { team, _ ->
            if (team == null || !team.exists()) return@addSnapshotListener
            val roster = (team.get("members") as? List<*>).orEmpty()
                .mapNotNull { it as? Map<*, *> }
                .mapNotNull { member ->
                    val userId = member["userId"] as? String ?: return@mapNotNull null
                    OrganizerPlayer(
                        userId = userId,
                        name = (member["displayName"] as? String).orEmpty().ifBlank { "Team member" },
                        photoUrl = (member["profilePhotoUrl"] as? String).orEmpty()
                    )
                }.distinctBy { it.userId }
            state = OrganizerTeamUiState(details = OrganizerTeamDetails(
                name = team.getString("teamName") ?: team.getString("name").orEmpty(),
                location = team.getString("city") ?: team.getString("cityTown") ?: team.getString("location").orEmpty(),
                logoUrl = listOf("logoUrl", "teamLogoUrl", "photoUrl").firstNotNullOfOrNull { team.getString(it)?.takeIf(String::isNotBlank) }.orEmpty(),
                members = roster
            ), isLoadingPlayers = roster.isNotEmpty())
            FirebaseFunctions.getInstance().getHttpsCallable("getTeamMemberProfiles")
                .call(mapOf("teamId" to teamId))
                .addOnSuccessListener { result ->
                    val profiles = ((result.data as? Map<*, *>)?.get("members") as? List<*>).orEmpty()
                        .mapNotNull { it as? Map<*, *> }
                        .associateBy { (it["userId"] as? String).orEmpty() }
                    val enriched = roster.map { player ->
                        val profile = profiles[player.userId]
                        player.copy(
                            name = (profile?.get("displayName") as? String).orEmpty().ifBlank { player.name },
                            photoUrl = (profile?.get("profilePhotoUrl") as? String).orEmpty().ifBlank { player.photoUrl }
                        )
                    }
                    state = state.copy(details = state.details.copy(members = enriched), isLoadingPlayers = false)
                }
                .addOnFailureListener { state = state.copy(isLoadingPlayers = false) }
            matchListener?.remove()
            matchListener = firestore.collection("matches").addSnapshotListener { matches, _ ->
                val docs = matches?.documents.orEmpty()
                val withCounts = roster.map { player ->
                    val playerKeys = setOf(player.userId, "$teamId::${player.userId}")
                    val appearances = docs.filter { match ->
                        val lineups = (match.get("playingXI") as? Map<*, *>).orEmpty().values
                            .flatMap { ((it as? Map<*, *>)?.get("playerIds") as? List<*>).orEmpty().filterIsInstance<String>() }
                        val squadPlayers = (match.get("matchSquads") as? Map<*, *>).orEmpty().values
                            .flatMap { ((it as? Map<*, *>)?.get("players") as? List<*>).orEmpty() }
                            .filterIsInstance<Map<*, *>>()
                            .mapNotNull { it["userId"] as? String }
                        lineups.any { it in playerKeys } || player.userId in squadPlayers
                    }
                    val latestMatch = docs.maxByOrNull { it.getLong("updatedAtEpochMs") ?: it.getLong("createdAtEpochMs") ?: 0L }
                    player.copy(matches = appearances.size, playedLastMatch = appearances.any { it.id == latestMatch?.id })
                }
                state = state.copy(details = state.details.copy(members = withCounts.map { counted ->
                    val profile = state.details.members.firstOrNull { it.userId == counted.userId }
                    counted.copy(name = profile?.name ?: counted.name, photoUrl = profile?.photoUrl ?: counted.photoUrl)
                }))
            }
        }
        onDispose {
            teamListener.remove()
            matchListener?.remove()
        }
    }
    return state
}

@Composable
private fun ViewDetailsStartMatchScreen(uiState: OrganizerTeamUiState, selectedPlayerIds: Set<String>, onTogglePlayer: (OrganizerPlayer) -> Unit, onBack: () -> Unit, onAddPlayer: () -> Unit, onNext: () -> Unit) {
    val details = uiState.details
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A101C))
            .drawBehind {
                drawRect(Brush.radialGradient(listOf(DetailsAccent.copy(alpha = 0.15f), Color.Transparent), center = Offset(size.width / 2f, -50f), radius = size.height * 0.6f))
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailsTopBar(onBack)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 11.dp, vertical = 21.dp)
            ) {
                TeamSummaryCard(details)
                AvailablePlayersSection(details.members, uiState.isLoadingPlayers, selectedPlayerIds, onTogglePlayer, onAddPlayer = onAddPlayer)
                Spacer(Modifier.height(100.dp))
            }
        }
        BottomActions(Modifier.align(Alignment.BottomCenter), onAddPlayer = onAddPlayer, onNext = onNext)
    }
}

@Composable
private fun DetailsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .background(Color(0xFF0A101C))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DetailsArrow(Modifier.size(25.dp).clickable(onClick = onBack), Color.White)
        Text(
            "Add Playing Team",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 59.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun TeamSummaryCard(details: OrganizerTeamDetails) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
            .height(130.dp)
            .shadow(20.dp, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF152238), Color(0xFF09101C))))
            .drawBehind {
                drawLine(
                    brush = Brush.horizontalGradient(listOf(Color.Transparent, DetailsAccent.copy(alpha = 0.5f), Color.Transparent)),
                    start = Offset(size.width * 0.1f, size.height),
                    end = Offset(size.width * 0.9f, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF202838)),
                contentAlignment = Alignment.Center
            ) {
                if (details.logoUrl.isNotBlank()) {
                    AsyncImage(model = details.logoUrl, contentDescription = "${details.name} team photo", modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
                } else {
                    val fallbackColors = listOf(Color(0xFF1E73FF), Color(0xFF007A70), Color(0xFF7B32D9), Color(0xFFD51D49))
                    val color = fallbackColors[(details.name.hashCode() and Int.MAX_VALUE) % fallbackColors.size]
                    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(color, color.copy(alpha = 0.55f)))), contentAlignment = Alignment.Center) {
                        val initials = details.name.split(Regex("\\s+")).filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "TM" }
                        Text(initials, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
            Column(modifier = Modifier.padding(start = 15.dp).weight(1f)) {
                Text(details.name.ifBlank { "Team" }, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black, maxLines = 1)
                Text(details.location.ifBlank { "Location unavailable" }, color = DetailsMuted, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
        Row(
            modifier = Modifier.padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GroupIcon(Modifier.size(18.dp), Color(0xFF64C8FF))
            Text("${details.members.size} ${if (details.members.size == 1) "Player" else "Players"}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 7.dp))
            ShieldIcon(Modifier.padding(start = 64.dp).size(18.dp), DetailsAccent)
            Text("Janaman Gana", color = DetailsMuted, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun AvailablePlayersSection(players: List<OrganizerPlayer>, isLoading: Boolean, selectedPlayerIds: Set<String>, onTogglePlayer: (OrganizerPlayer) -> Unit, onAddPlayer: () -> Unit) {
    SearchBox(Modifier.padding(top = 18.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("AVAILABLE PLAYERS", color = Color(0xFF97A9BA), fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.clickable(onClick = onAddPlayer),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("+", color = DetailsAccent, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text(" ADD PLAYER", color = DetailsAccent, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
    Text(
        "Choose players participating in this match.",
        color = Color(0xFF9CA8B7),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 23.dp)
    )
    Column(
        modifier = Modifier.padding(top = 15.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            isLoading -> repeat(4) { index -> PlayerSkeletonRow(index) }
            players.isEmpty() -> Text("No players found in this team.", color = DetailsMuted, fontSize = 13.sp, modifier = Modifier.padding(vertical = 12.dp))
            else -> players.forEach { player -> PlayerSelectionRow(player = player.copy(selected = player.userId in selectedPlayerIds), onClick = { onTogglePlayer(player) }) }
        }
    }
}

@Composable
private fun PlayerSkeletonRow(index: Int) {
    val transition = rememberInfiniteTransition(label = "organizer-player-skeleton-$index")
    val alpha by transition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(tween(850), repeatMode = RepeatMode.Reverse),
        label = "organizer-player-skeleton-alpha-$index"
    )
    val bone = Color(0xFF708199).copy(alpha = alpha)
    Row(
        Modifier.fillMaxWidth().height(76.dp).clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF101929), Color(0xFF0A101C))))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(45.dp).clip(CircleShape).background(bone))
        Column(Modifier.weight(1f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxWidth(0.68f).height(12.dp).clip(RoundedCornerShape(6.dp)).background(bone))
            Box(Modifier.fillMaxWidth(0.4f).height(8.dp).clip(RoundedCornerShape(5.dp)).background(bone.copy(alpha = alpha * 0.7f)))
        }
        Box(Modifier.size(22.dp, 28.dp).clip(RoundedCornerShape(5.dp)).background(bone))
    }
}

@Composable
private fun SearchBox(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .shadow(12.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0B1322))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchIcon(Modifier.size(18.dp), Color(0xFF8192AB))
        Text("Quick search", color = Color(0xFF91A0B5), fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun PlayerSelectionRow(player: OrganizerPlayer, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
            .clickable(onClick = onClick)
            .height(76.dp)
            .shadow(if (player.selected) 16.dp else 8.dp, RoundedCornerShape(14.dp), spotColor = if (player.selected) DetailsAccent.copy(alpha = 0.6f) else Color.Black, clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF101929), Color(0xFF0A101C))))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A2840))
                    .border(1.dp, Color(0xFF364158), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (player.photoUrl.isNotBlank()) {
                    AsyncImage(model = player.photoUrl, contentDescription = "${player.name} profile photo", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                } else PlayerAvatar(Modifier.size(36.dp), player.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase())
            }
            if (player.selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(DetailsAccent),
                    contentAlignment = Alignment.Center
                ) {
                    CheckIcon(Modifier.size(9.dp), Color(0xFF111604))
                }
            }
        }
        Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
            Text(player.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(modifier = Modifier.padding(top = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                    .background(if (player.playedLastMatch) DetailsAccent else Color(0xFF708199))
                )
                Text(
                    if (player.playedLastMatch) "Played in recent match" else "Available",
                    color = Color(0xFF9EACBB),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(player.matches.toString(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
            Text("MAT", color = Color(0xFF8392A8), fontSize = 8.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun BottomActions(modifier: Modifier = Modifier, onAddPlayer: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xE60A101C), Color(0xFF0A101C))))
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .bounceClick(onAddPlayer)
                .height(56.dp)
                .shadow(12.dp, RoundedCornerShape(14.dp), clip = false)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF1A263D), Color(0xFF0C1322)))),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AddPersonIcon(Modifier.size(20.dp), Color.White)
            Text("Add Player", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 10.dp))
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .bounceClick(onNext)
                .height(56.dp)
                .shadow(20.dp, RoundedCornerShape(14.dp), spotColor = DetailsAccent, clip = false)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(DetailsAccent, Color(0xFF98CC00)))),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Next", color = Color(0xFF111604), fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun DetailsArrow(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.66f, size.height * 0.24f)
            lineTo(size.width * 0.34f, size.height * 0.5f)
            lineTo(size.width * 0.66f, size.height * 0.76f)
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
private fun BatBallIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.22f, size.height * 0.28f), Offset(size.width * 0.58f, size.height * 0.74f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawRoundRect(tint, topLeft = Offset(size.width * 0.17f, size.height * 0.17f), size = Size(size.width * 0.18f, size.height * 0.43f), style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.12f, center = Offset(size.width * 0.76f, size.height * 0.28f), style = stroke)
    }
}

@Composable
private fun EditPencilIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.24f, size.height * 0.75f), Offset(size.width * 0.75f, size.height * 0.24f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.19f, size.height * 0.81f), Offset(size.width * 0.35f, size.height * 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun GroupIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.12f, center = Offset(size.width * 0.38f, size.height * 0.36f), style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.1f, center = Offset(size.width * 0.67f, size.height * 0.4f), style = stroke)
        drawArc(tint, 205f, 130f, false, topLeft = Offset(size.width * 0.18f, size.height * 0.52f), size = Size(size.width * 0.42f, size.height * 0.28f), style = stroke)
        drawArc(tint, 205f, 130f, false, topLeft = Offset(size.width * 0.48f, size.height * 0.55f), size = Size(size.width * 0.32f, size.height * 0.24f), style = stroke)
    }
}

@Composable
private fun ShieldIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.12f)
            lineTo(size.width * 0.78f, size.height * 0.24f)
            lineTo(size.width * 0.72f, size.height * 0.62f)
            lineTo(size.width * 0.5f, size.height * 0.86f)
            lineTo(size.width * 0.28f, size.height * 0.62f)
            lineTo(size.width * 0.22f, size.height * 0.24f)
            close()
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
private fun SearchIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.28f, center = Offset(size.width * 0.42f, size.height * 0.42f), style = stroke)
        drawLine(tint, Offset(size.width * 0.62f, size.height * 0.62f), Offset(size.width * 0.84f, size.height * 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun CheckIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.52f)
            lineTo(size.width * 0.42f, size.height * 0.72f)
            lineTo(size.width * 0.82f, size.height * 0.28f)
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
private fun PlayerAvatar(modifier: Modifier, initials: String) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color(0xFF1E2A2D), radius = size.minDimension * 0.48f)
            drawCircle(Color(0xFFD0B190), radius = size.minDimension * 0.16f, center = Offset(size.width * 0.5f, size.height * 0.28f))
            drawRoundRect(Color(0xFF273C57), topLeft = Offset(size.width * 0.24f, size.height * 0.48f), size = Size(size.width * 0.52f, size.height * 0.36f))
        }
        Text(initials, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun PlayerSilhouette(modifier: Modifier) {
    Canvas(modifier) {
        drawCircle(Color(0xFF1E2A2D), radius = size.minDimension * 0.48f)
        drawCircle(Color(0xFFD0B190), radius = size.minDimension * 0.16f, center = Offset(size.width * 0.5f, size.height * 0.28f))
        drawRoundRect(Color(0xFF1C5635), topLeft = Offset(size.width * 0.28f, size.height * 0.48f), size = Size(size.width * 0.44f, size.height * 0.36f))
    }
}

@Composable
private fun KebabIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        repeat(3) { index ->
            drawCircle(tint, radius = size.minDimension * 0.055f, center = Offset(size.width * 0.5f, size.height * (0.32f + index * 0.18f)))
        }
    }
}

@Composable
private fun PersonIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.13f, center = Offset(size.width * 0.5f, size.height * 0.34f), style = stroke)
        drawArc(tint, 205f, 130f, false, topLeft = Offset(size.width * 0.26f, size.height * 0.52f), size = Size(size.width * 0.48f, size.height * 0.34f), style = stroke)
    }
}

@Composable
private fun AddPersonIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.11f, center = Offset(size.width * 0.36f, size.height * 0.36f), style = stroke)
        drawArc(tint, 205f, 130f, false, topLeft = Offset(size.width * 0.15f, size.height * 0.54f), size = Size(size.width * 0.42f, size.height * 0.28f), style = stroke)
        drawLine(tint, Offset(size.width * 0.68f, size.height * 0.38f), Offset(size.width * 0.68f, size.height * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.51f, size.height * 0.55f), Offset(size.width * 0.85f, size.height * 0.55f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

fun Modifier.bounceClick(onClick: (() -> Unit)? = null) = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )
    this
        .scale(scale)
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                isPressed = true
                val up = waitForUpOrCancellation()
                isPressed = false
                if (up != null && onClick != null) {
                    onClick()
                }
            }
        }
}

