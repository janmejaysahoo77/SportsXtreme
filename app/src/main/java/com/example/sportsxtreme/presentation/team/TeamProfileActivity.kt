package com.example.sportsxtreme.presentation.team

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.presentation.ui.theme.*
import com.example.sportsxtreme.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration

class TeamProfileActivity : ComponentActivity() {
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
                onShareTeam = { teamName ->
                    startActivity(
                        Intent(this, AddPlayerActivity::class.java)
                            .putExtra(ManagePlayersInsideTeamActivity.EXTRA_TEAM_ID, teamId)
                            .putExtra(ManagePlayersInsideTeamActivity.EXTRA_TEAM_NAME, teamName)
                    )
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
    val location: String = "Bhubaneswar",
    val description: String = "",
    val founded: String = "",
    val homeGround: String = "",
    val captainName: String = "",
    val captainNumber: String = "",
    val coachManager: String = "",
    val teamMotto: String = ""
)

private data class EditableProfileField(val key: String, val label: String, val icon: String, val value: String)

private data class TeamMemberData(
    val userId: String,
    val name: String,
    val teamRoles: List<String>,
    val playingRole: String,
    val joinedAtEpochMs: Long
)

private data class MemberProfileData(
    val displayName: String,
    val playingRole: String
)

private data class TeamMatchData(
    val id: String,
    val title: String,
    val teamName: String,
    val opponentName: String,
    val status: String,
    val dateEpochMs: Long?,
    val venue: String,
    val teamScore: String,
    val opponentScore: String,
    val result: String?
)

@Composable
private fun TeamProfileScreen(
    teamId: String,
    onBack: () -> Unit,
    onShareTeam: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf("Profile") }
    var team by remember(teamId) { mutableStateOf(TeamProfileData()) }
    var members by remember(teamId) { mutableStateOf<List<TeamMemberData>>(emptyList()) }
    var matches by remember(teamId) { mutableStateOf<List<TeamMatchData>>(emptyList()) }
    var canManageTeam by remember(teamId) { mutableStateOf(false) }
    var viewerRoles by remember(teamId) { mutableStateOf<List<String>>(emptyList()) }
    DisposableEffect(teamId) {
        val firestore = FirebaseFirestore.getInstance()
        var registration: ListenerRegistration? = null
        var teamARegistration: ListenerRegistration? = null
        var teamBRegistration: ListenerRegistration? = null
        val teamAMatches = mutableMapOf<String, TeamMatchData>()
        val teamBMatches = mutableMapOf<String, TeamMatchData>()

        fun publishMatches() {
            matches = (teamAMatches.values + teamBMatches.values)
                .distinctBy { it.id }
                .sortedByDescending { it.dateEpochMs ?: Long.MAX_VALUE }
        }

        if (teamId.isNotBlank()) {
            registration = firestore.collection("teams").document(teamId)
                .addSnapshotListener { document, _ ->
                    if (document != null && document.exists()) {
                        fun firstValue(vararg keys: String): String = keys
                            .asSequence()
                            .mapNotNull { document.get(it)?.toString() }
                            .firstOrNull { it.isNotBlank() }
                            .orEmpty()
                        team = TeamProfileData(
                            name = firstValue("teamName", "name").ifBlank { "Addd" },
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
                        } else {
                            members = joinedMembers
                            fetchTeamMemberProfiles(teamId, joinedMembers) { profileByUserId ->
                                members = joinedMembers.map { member ->
                                    profileByUserId[member.userId]?.let { profile ->
                                        member.copy(
                                            name = profile.displayName,
                                            playingRole = member.playingRole.ifBlank { profile.playingRole }
                                        )
                                    } ?: member.copy(
                                        name = "Team member",
                                        playingRole = member.playingRole.ifBlank { "Player" }
                                    )
                                }.sortedBy { it.joinedAtEpochMs }
                            }
                        }
                    }
                }
            teamARegistration = firestore.collection("matches")
                .whereEqualTo("teamA.teamId", teamId)
                .addSnapshotListener { snapshot, _ ->
                    teamAMatches.clear()
                    snapshot?.documents.orEmpty().forEach { match ->
                        match.toTeamMatchData(teamId)?.let { teamAMatches[it.id] = it }
                    }
                    publishMatches()
                }
            teamBRegistration = firestore.collection("matches")
                .whereEqualTo("teamB.teamId", teamId)
                .addSnapshotListener { snapshot, _ ->
                    teamBMatches.clear()
                    snapshot?.documents.orEmpty().forEach { match ->
                        match.toTeamMatchData(teamId)?.let { teamBMatches[it.id] = it }
                    }
                    publishMatches()
                }
        }
        onDispose {
            registration?.remove()
            teamARegistration?.remove()
            teamBRegistration?.remove()
        }
    }
    Column(Modifier.fillMaxSize().background(ProfileBackground)) {
        TopBar(
            onBack = onBack,
            canShare = viewerRoles.any { it == "ADMIN" || it == "CAPTAIN" || it == "VICE_CAPTAIN" },
            onShare = { onShareTeam(team.name) }
        )
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            TeamHero(team)
            ProfileTabs(selectedTab) { selectedTab = it }
            when (selectedTab) {
                "Profile" -> ProfileTabContent(team, canManageTeam) { key, value ->
                    if (teamId.isNotBlank()) {
                        com.google.firebase.functions.FirebaseFunctions.getInstance()
                            .getHttpsCallable("updateTeamProfile")
                            .call(mapOf("teamId" to teamId, "changes" to mapOf(key to value.trim())))
                    }
                }
                "Matches" -> MatchesTabContent(matches)
                "Stats" -> StatsTabContent(matches)
                "Leaderboard" -> LeaderboardTabContent()
                "Members" -> MembersTabContent(teamId, members, viewerRoles)
                "Photos" -> PhotosTabContent()
            }
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit, canShare: Boolean, onShare: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().height(66.dp).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("‹", color = Color.White, fontSize = 34.sp, modifier = Modifier.clickable { onBack() }.padding(end = 15.dp))
        Column(Modifier.weight(1f)) {
            Row { Text("sports", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black); Text("xtreme", color = ProfileAccent, fontSize = 19.sp, fontWeight = FontWeight.Black) }
            Text("PLAY  •  SCORE  •  BELONG", color = ProfileMuted, fontSize = 7.sp, letterSpacing = 1.sp)
        }
        Text("⌕", color = Color.White, fontSize = 25.sp, modifier = Modifier.padding(end = 18.dp))
        if (canShare) {
            Box {
                Text(
                    "⋮",
                    color = Color.White,
                    fontSize = 26.sp,
                    modifier = Modifier
                        .clickable { menuExpanded = true }
                        .padding(start = 4.dp)
                )
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = {
                            menuExpanded = false
                            onShare()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamHero(team: TeamProfileData) {
    Box(Modifier.fillMaxWidth().height(238.dp).background(Brush.linearGradient(listOf(Color(0xFF071B28), Color(0xFF0B2830), Color(0xFF07130E)))).drawBehind {
        repeat(6) { i -> drawCircle(Color(0x1A71B3C9), radius = size.width * .16f, center = Offset(size.width * (i / 5f), size.height * .16f)) }
        drawCircle(Color(0x5C70E42E), radius = size.width * .55f, center = Offset(size.width * .76f, size.height * .86f))
        drawCircle(Color(0x302ED3FF), radius = size.width * .34f, center = Offset(size.width * .14f, size.height * .05f))
    }) {
        Text("TEAM PROFILE", color = ProfileAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, modifier = Modifier.align(Alignment.TopStart).padding(start = 22.dp, top = 20.dp))
        Column(Modifier.align(Alignment.BottomStart).padding(start = 22.dp, end = 18.dp, bottom = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(76.dp).background(Brush.linearGradient(listOf(Color(0xFFEF7B55), Color(0xFF6B2B50))), CircleShape).border(2.dp, ProfileAccent, CircleShape), contentAlignment = Alignment.Center) { Text(team.name.initials(), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black) }
                Column(Modifier.padding(start = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text(team.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black); Text("  VERIFIED", color = Color(0xFF07140B), fontSize = 7.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 7.dp).background(ProfileAccent, RoundedCornerShape(5.dp)).padding(horizontal = 6.dp, vertical = 3.dp)) }
                    Text("⌾  ${team.location}  •  Cricket", color = Color.White, fontSize = 11.sp)
                    Text("28 followers  ·  Est. 2026", color = ProfileMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp))
                }
            }
            Text("A passionate team with big dreams, building a stronger cricketing community.", color = Color(0xFFD5E0E5), fontSize = 11.sp, lineHeight = 15.sp, modifier = Modifier.padding(top = 13.dp))
            Row(Modifier.padding(top = 13.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { HeroPill("24", "MATCHES"); HeroPill("66.7%", "WIN RATE"); HeroPill("#04", "CITY RANK") }
        }
    }
}

@Composable private fun HeroPill(value: String, label: String) = Column(Modifier.background(ProfileGlass, RoundedCornerShape(10.dp)).border(1.dp, Color(0x3343D6D0), RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) { Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold); Text(label, color = ProfileMuted, fontSize = 7.sp, letterSpacing = .5.sp) }

@Composable
private fun ProfileTabs(selectedTab: String, onSelect: (String) -> Unit) {
    val tabs = listOf("Profile", "Matches", "Stats", "Leaderboard", "Members", "Photos")
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp).height(39.dp).background(Color(0xFF0A141C), RoundedCornerShape(20.dp)).border(1.dp, Color(0xFF1C303B), RoundedCornerShape(20.dp)).horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
        tabs.forEach { tab ->
            Text(tab, color = if (tab == selectedTab) Color(0xFF07120B) else ProfileMuted, fontSize = 10.sp, fontWeight = if (tab == selectedTab) FontWeight.Bold else FontWeight.Medium, modifier = Modifier.height(31.dp).clickable { onSelect(tab) }.background(if (tab == selectedTab) ProfileAccent else Color.Transparent, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 8.dp))
        }
    }
}

@Composable
private fun ProfileTabContent(team: TeamProfileData, canEdit: Boolean, onSave: (String, String) -> Unit) {
    Column(Modifier.padding(horizontal = 13.dp, vertical = 4.dp)) {
        AboutCard(team, canEdit, onSave)
        AchievementsCard()
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun AboutCard(team: TeamProfileData, canEdit: Boolean, onSave: (String, String) -> Unit) {
    Column(Modifier.fillMaxWidth().background(ProfileCard, RoundedCornerShape(18.dp)).border(1.dp, ProfileStroke, RoundedCornerShape(18.dp)).padding(16.dp)) {
        var editingField by remember { mutableStateOf<EditableProfileField?>(null) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(28.dp).background(Color(0xFF1C3823), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Text("i", color = ProfileAccent, fontWeight = FontWeight.Bold) }
            Text("About the team", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 9.dp).weight(1f))
            if (canEdit) EditButton { editingField = EditableProfileField("description", "About the team", "", team.description) }
        }
        Text(team.description, color = ProfileMuted, fontSize = 11.sp, lineHeight = 15.sp, modifier = Modifier.padding(top = 14.dp))
        DetailsBox(team, canEdit) { editingField = it }
        editingField?.let { field ->
            ProfileFieldEditor(field, onDismiss = { editingField = null }) { value -> onSave(field.key, value); editingField = null }
        }
    }
}

@Composable
private fun DetailsBox(team: TeamProfileData, canEdit: Boolean, onEdit: (EditableProfileField) -> Unit) {
    val rows = listOf(
        EditableProfileField("founded", "Founded", "▣", team.founded),
        EditableProfileField("homeGround", "Home Ground", "▤", team.homeGround),
        EditableProfileField("captainName", "Captain", "♟", team.captainName),
        EditableProfileField("captainMobile", "Captain Number", "☎", team.captainNumber),
        EditableProfileField("coachManager", "Coach / Manager", "⬟", team.coachManager),
        EditableProfileField("teamMotto", "Team Motto", "⚑", team.teamMotto)
    )
    Column(Modifier.fillMaxWidth().padding(top = 15.dp).background(Color(0xFF0A192B), RoundedCornerShape(12.dp)).border(1.dp, ProfileStroke, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp)) {
        rows.forEachIndexed { index, field ->
            Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(field.icon, color = Color(0xFFD8E6FF), fontSize = 18.sp, modifier = Modifier.width(34.dp))
                Text(field.label, color = ProfileMuted, fontSize = 10.sp, modifier = Modifier.weight(1f))
                Text(field.value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.25f))
                if (canEdit) EditButton { onEdit(field) }
            }
            if (index < rows.lastIndex) Spacer(Modifier.fillMaxWidth().height(1.dp).background(ProfileStroke))
        }
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
private fun ProfileFieldEditor(field: EditableProfileField, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var value by remember(field.key, field.value) { mutableStateOf(field.value) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${field.label}") },
        text = { OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text(field.label) }, singleLine = field.key != "description" && field.key != "teamMotto") },
        confirmButton = { TextButton(onClick = { onSave(value) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun String.initials(): String = split(Regex("\\s+")).filter { it.isNotBlank() }.take(2)
    .joinToString("") { it.first().uppercase() }.ifBlank { "TM" }

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
        teamName = (ownTeam["name"] as? String).orEmpty().ifBlank { "Your team" },
        opponentName = (opponent["name"] as? String).orEmpty().ifBlank { "Opponent" },
        status = status,
        dateEpochMs = getLong("matchDateEpochMs"),
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
    val completed = matches.filter { it.status == "COMPLETED" || it.status == "ABANDONED" }
    val upcoming = matches.filter { it.status != "COMPLETED" && it.status != "ABANDONED" }
    Column(Modifier.padding(horizontal = 12.dp)) {
        MatchSectionHeader("Recent Matches", "Latest match results and performance.")
        if (completed.isEmpty()) EmptyTeamTab("No completed matches yet.") else completed.forEach { TeamMatchCard(it) }
        MatchSectionHeader("Upcoming Matches", "Stay tuned for our next challenges.")
        if (upcoming.isEmpty()) EmptyTeamTab("No upcoming matches scheduled.") else upcoming.forEach { TeamMatchCard(it) }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun EmptyTeamTab(message: String) = Text(message, color = ProfileMuted, fontSize = 11.sp, modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)

@Composable
private fun TeamMatchCard(match: TeamMatchData) {
    val won = match.result == "Won"
    Column(Modifier.fillMaxWidth().padding(top = 9.dp).background(ProfileCard, RoundedCornerShape(10.dp)).border(1.dp, ProfileStroke, RoundedCornerShape(10.dp)).padding(10.dp)) {
        Row { Text("♛  ${match.title}", color = ProfileMuted, fontSize = 8.sp, modifier = Modifier.weight(1f)); Text(listOf(match.dateLabel(), match.venue).filter { it.isNotBlank() }.joinToString("  •  "), color = ProfileMuted, fontSize = 8.sp) }
        Row(Modifier.fillMaxWidth().padding(top = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            TeamBadge(match.teamName); Column(Modifier.weight(1f).padding(start = 8.dp)) { Text(match.teamName, color = Color.White, fontSize = 11.sp); Text(match.teamScore, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            Text("vs", color = ProfileMuted, fontSize = 10.sp)
            Column(Modifier.weight(1f).padding(start = 8.dp)) { Text(match.opponentScore, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text(match.opponentName, color = Color.White, fontSize = 11.sp) }
        }
        val label = match.result ?: match.status.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
        Text(label, color = Color.White, fontSize = 10.sp, modifier = Modifier.fillMaxWidth().padding(top = 7.dp).background(if (won) Color(0xFF075E38) else Color(0xFF0752A9), RoundedCornerShape(4.dp)).padding(vertical = 3.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun MatchSectionHeader(title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = ProfileMuted, fontSize = 10.sp) }
        Text("View All  →", color = ProfileAccent, fontSize = 10.sp)
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

@Composable private fun TeamBadge(name: String) = Box(Modifier.size(26.dp).background(Color(0xFF925443), CircleShape).border(1.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) { Text(name.take(1), color = Color.White, fontSize = 11.sp) }

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
    val stats = listOf("♟|Matches|$matches", "▣|Upcoming|$upcoming", "♕|Won|$won", "✕|Lost|$lost", "⌁|Tie|$tied", "═|Drawn|0", "⊗|NR|0", "↗|Win %|$winRate")
    Column(Modifier.fillMaxWidth().padding(top = 13.dp).background(ProfileCard, RoundedCornerShape(10.dp)).border(1.dp, ProfileStroke, RoundedCornerShape(10.dp)).padding(8.dp)) { stats.chunked(4).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { item -> val parts = item.split("|"); Column(Modifier.weight(1f).height(61.dp).background(Color(0xFF0B1D30), RoundedCornerShape(7.dp)).padding(8.dp)) { Text(parts[0], color = ProfileAccent, fontSize = 17.sp); Text(parts[1], color = ProfileMuted, fontSize = 9.sp); Text(parts[2], color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) } } }; Spacer(Modifier.height(8.dp)) } }
}

@Composable private fun StatTitle(title: String, subtitle: String) = Column(Modifier.padding(top = 14.dp)) { Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = ProfileMuted, fontSize = 11.sp) }
@Composable
private fun SplitStatCard(icon1: String, label1: String, value1: String, percent1: String, icon2: String, label2: String, value2: String, percent2: String) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp).height(89.dp).background(ProfileCard, RoundedCornerShape(10.dp)).border(1.dp, ProfileStroke, RoundedCornerShape(10.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) { SplitStat(icon1, label1, value1, percent1, Modifier.weight(1f)); Spacer(Modifier.width(1.dp).fillMaxHeight().background(ProfileStroke)); SplitStat(icon2, label2, value2, percent2, Modifier.weight(1f)) }
}
@Composable private fun SplitStat(icon: String, label: String, value: String, percent: String, modifier: Modifier) = Row(modifier.padding(horizontal = 5.dp), verticalAlignment = Alignment.CenterVertically) { Text(icon, color = ProfileAccent, fontSize = 24.sp); Column(Modifier.padding(start = 10.dp).weight(1f)) { Text(label, color = ProfileMuted, fontSize = 10.sp); Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("out of 24", color = ProfileMuted, fontSize = 9.sp) }; Box(Modifier.size(48.dp).border(6.dp, ProfileAccent, CircleShape), contentAlignment = Alignment.Center) { Text(percent, color = Color.White, fontSize = 9.sp) } }

@Composable
private fun LeaderboardTabContent() {
    val players = listOf("Rahul Sharma|All-rounder|642|53.5|142.1|18", "Amit Verma|Batsman|521|43.4|138.6|18", "Siddharth Das|Batsman|398|36.2|132.4|16", "Karan Patel|Wicket Keeper|312|28.4|129.1|15", "Rohit Sahu|All-rounder|287|26.1|118.4|14", "Manish Nayak|Batsman|246|22.3|121.6|12", "Aditya Rout|All-rounder|198|19.8|118.4|12", "Vikram Singh|Batsman|176|18.2|111.1|11")
    Column(Modifier.padding(horizontal = 12.dp)) {
        Text("Team Leaderboard", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)); Text("Top performers from our team", color = ProfileMuted, fontSize = 11.sp)
        Row(Modifier.fillMaxWidth().padding(top = 10.dp).height(33.dp).background(Color(0xFF0A1B2B), RoundedCornerShape(8.dp))) { listOf("✎  Bat", "◉  Bowl", "♙  Field", "♟  P'ship").forEachIndexed { i, tab -> Text(tab, color = if (i == 0) Color(0xFF111709) else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).fillMaxHeight().background(if (i == 0) ProfileAccent else Color.Transparent, RoundedCornerShape(8.dp)).padding(vertical = 10.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center) } }
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp)) { listOf("#", "Player", "Runs", "Avg", "SR", "Matches").forEach { Text(it, color = ProfileMuted, fontSize = 8.sp, modifier = Modifier.weight(if (it == "Player") 2.2f else 1f)) } }
        players.forEachIndexed { index, player -> LeaderboardRow(index + 1, player) }
        Row(Modifier.fillMaxWidth().padding(top = 13.dp).background(Color(0xFF0A2830), RoundedCornerShape(10.dp)).border(1.dp, Color(0xFF315D38), RoundedCornerShape(10.dp)).padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Text("▥", color = ProfileAccent, fontSize = 22.sp); Column(Modifier.padding(start = 10.dp).weight(1f)) { Text("Top Performer", color = ProfileAccent, fontSize = 10.sp); Text("Rahul Sharma leads with 642 runs\nat an average of 53.5", color = ProfileMuted, fontSize = 10.sp) }; Text("→", color = Color.White) }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun LeaderboardRow(rank: Int, record: String) {
    val p = record.split("|")
    Row(Modifier.fillMaxWidth().padding(top = 4.dp).height(40.dp).background(if (rank == 1) Color(0xFF12291E) else ProfileCard, RoundedCornerShape(7.dp)).border(if (rank == 1) 1.dp else 0.dp, if (rank == 1) ProfileAccent else Color.Transparent, RoundedCornerShape(7.dp)).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) { Text(if (rank == 1) "♛" else "$rank", color = if (rank == 1) ProfileAccent else Color.White, fontSize = 10.sp, modifier = Modifier.width(24.dp)); Box(Modifier.size(27.dp).background(Color(0xFF925443), CircleShape), contentAlignment = Alignment.Center) { Text(p[0].take(1), color = Color.White, fontSize = 11.sp) }; Column(Modifier.weight(2.2f).padding(start = 6.dp)) { Row { Text(p[0], color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold); if (rank == 1) Text("  C", color = Color(0xFF111709), fontSize = 8.sp, modifier = Modifier.background(ProfileAccent, RoundedCornerShape(2.dp))) }; Text(p[1], color = ProfileMuted, fontSize = 8.sp) }; Text(p[2], color = Color.White, fontSize = 9.sp, modifier = Modifier.weight(1f)); Text(p[3], color = Color.White, fontSize = 9.sp, modifier = Modifier.weight(1f)); Text(p[4], color = Color.White, fontSize = 9.sp, modifier = Modifier.weight(1f)); Text(p[5], color = Color.White, fontSize = 9.sp, modifier = Modifier.weight(1f)) }
}

@Composable
private fun MembersTabContent(
    teamId: String,
    members: List<TeamMemberData>,
    viewerRoles: List<String>
) {
    Column(Modifier.padding(horizontal = 12.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("Team Members", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold); Text("Meet the warriors behind our journey", color = ProfileMuted, fontSize = 10.sp) }
            Text("${members.size} ${if (members.size == 1) "Member" else "Members"}", color = ProfileMuted, fontSize = 10.sp)
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
    var actionsExpanded by remember(member.userId) { mutableStateOf(false) }
    val isLeader = member.teamRoles.any { it == "ADMIN" || it == "CAPTAIN" }
    val teamRole = member.teamRoles.joinToString(" • ") { role ->
        role.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
    }
    Row(
        Modifier.fillMaxWidth().padding(top = 7.dp).height(40.dp).background(if (isLeader) Color(0xFF12291E) else ProfileCard, RoundedCornerShape(7.dp)).border(if (isLeader) 1.dp else 0.dp, if (isLeader) ProfileAccent else Color.Transparent, RoundedCornerShape(7.dp)).padding(horizontal = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("#${index + 1}", color = Color.White, fontSize = 9.sp, modifier = Modifier.width(32.dp))
        Box(Modifier.size(31.dp).background(Color(0xFF925443), CircleShape).border(1.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) { Text(member.name.take(1), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        Column(Modifier.width(140.dp).padding(start = 7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(member.name, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                if (teamRole.isNotBlank()) Text("  $teamRole", color = if (isLeader) Color(0xFF122300) else Color(0xFF071421), fontSize = 6.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 2.dp).background(if (isLeader) ProfileAccent else Color(0xFF9EB6DE), RoundedCornerShape(3.dp)).padding(horizontal = 4.dp, vertical = 2.dp))
            }
            Text(member.playingRole, color = ProfileMuted, fontSize = 8.sp)
        }
        Column(Modifier.weight(1f)) { Text("▰  Joined", color = Color.White, fontSize = 7.sp); Text("Team member", color = ProfileMuted, fontSize = 7.sp) }
        Column(Modifier.weight(1f)) { Text("◉  Role", color = Color.White, fontSize = 7.sp); Text(teamRole.ifBlank { "Player" }, color = ProfileMuted, fontSize = 7.sp) }
        if (canShowActions) {
            Box {
                Text(
                    "⋮",
                    color = Color.White,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .clickable { actionsExpanded = true }
                        .padding(start = 6.dp, end = 2.dp)
                )
                DropdownMenu(
                    expanded = actionsExpanded,
                    onDismissRequest = { actionsExpanded = false }
                ) {
                    if (canAssignRoles && "CAPTAIN" !in member.teamRoles) {
                        TeamMemberAction("Make captain") {
                            actionsExpanded = false
                            updateMemberRole(teamId, member.userId, "CAPTAIN", null, context)
                        }
                    }
                    if (canAssignRoles && "VICE_CAPTAIN" !in member.teamRoles) {
                        TeamMemberAction("Make vice-captain") {
                            actionsExpanded = false
                            updateMemberRole(teamId, member.userId, "VICE_CAPTAIN", null, context)
                        }
                    }
                    if (canAssignRoles && member.playingRole != "WICKET_KEEPER") {
                        TeamMemberAction("Make wicketkeeper") {
                            actionsExpanded = false
                            updateMemberRole(teamId, member.userId, null, "WICKET_KEEPER", context)
                        }
                    }
                    if (canRemove) {
                        TeamMemberAction("Remove player") {
                            actionsExpanded = false
                            removeMember(teamId, member.userId, context)
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
        } else {
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
    context: android.content.Context
) {
    val payload = mutableMapOf<String, Any>("teamId" to teamId, "targetUserId" to targetUserId)
    teamRole?.let { payload["teamRole"] = it }
    playingRole?.let { payload["playingRole"] = it }
    com.google.firebase.functions.FirebaseFunctions.getInstance()
        .getHttpsCallable("updateTeamMemberRole")
        .call(payload)
        .addOnFailureListener { error ->
            android.widget.Toast.makeText(context, error.message ?: "Unable to update the player role.", android.widget.Toast.LENGTH_SHORT).show()
        }
}

private fun removeMember(teamId: String, targetUserId: String, context: android.content.Context) {
    com.google.firebase.functions.FirebaseFunctions.getInstance()
        .getHttpsCallable("removeTeamMember")
        .call(mapOf("teamId" to teamId, "targetUserId" to targetUserId))
        .addOnFailureListener { error ->
            android.widget.Toast.makeText(context, error.message ?: "Unable to remove the player.", android.widget.Toast.LENGTH_SHORT).show()
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
                        playingRole = (profile["playingRole"] as? String).orEmpty().ifBlank { "Player" }
                    )
                }
                .toMap()
            onLoaded(profiles)
        }
        .addOnFailureListener {
            // The roster remains usable with safe placeholders if the network
            // is unavailable; a later team snapshot will retry this request.
            onLoaded(members.associate { member ->
                member.userId to MemberProfileData(
                    displayName = member.name.ifBlank { "Team member" },
                    playingRole = member.playingRole.ifBlank { "Player" }
                )
            })
        }
}

@Composable
private fun PhotosTabContent() {
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Team Photos", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Moments that make us stronger", color = ProfileMuted, fontSize = 10.sp)
            }
            Text("+  Add Photos", color = ProfileAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.border(1.dp, ProfileAccent, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 8.dp))
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PhotoCategory("All", true)
            PhotoCategory("♛  Matches", false)
            PhotoCategory("⚑  Practice", false)
            PhotoCategory("♟  Team", false)
            PhotoCategory("▣  Events", false)
            PhotoCategory("▧  Behind the Scenes", false)
        }
        Spacer(Modifier.height(420.dp))
    }
}

@Composable
private fun PhotoCategory(label: String, selected: Boolean) {
    Text(label, color = if (selected) Color(0xFF121709) else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(if (selected) ProfileAccent else Color(0xFF0A1B2B), RoundedCornerShape(9.dp)).border(if (selected) 0.dp else 1.dp, if (selected) Color.Transparent else ProfileStroke, RoundedCornerShape(9.dp)).padding(horizontal = 12.dp, vertical = 8.dp))
}
