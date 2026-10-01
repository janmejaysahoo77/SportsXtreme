package com.example.sportsxtreme.presentation.home

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportsxtreme.R
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Tournament
import com.example.sportsxtreme.presentation.tournament.HostTournamentsViewModel
import com.example.sportsxtreme.presentation.tournament.RegisterTournamentFinalPageActivity
import com.example.sportsxtreme.presentation.team.TeamProfileActivity
import com.example.sportsxtreme.presentation.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

private val CricketAccent = XtremeLime
private val CricketBg = XtremeBgBlue
private val CricketPanel = XtremeCardBlue
private val CricketCard = XtremeCardBlue
private val CricketMuted = XtremeMuted
private val CricketStroke = XtremeCardBorder
private val CricketBlue = Color(0xFF2ED8FF)

private val ClubCardBlack = XtremeCardBlue
private val ClubCardBorder = XtremeCardBorder
private val ClubMuted = XtremeMuted

private val cricketTabs = listOf("Matches", "Tournaments", "Teams", "Stats")

private data class CricketMatch(
    val type: String,
    val title: String,
    val status: String,
    val left: String,
    val leftScore: String? = null,
    val right: String,
    val rightScore: String? = null,
    val scoreCenter: String? = null,
    val meta: String,
    val location: String? = null,
    val result: String? = null,
    val potm: String? = null,
    val live: Boolean = false,
    val liveSoon: Boolean = false,
    val accent: Color = CricketAccent,
    val dateEpochMs: Long = 0L
)

private data class PlayerMatchesState(
    val loading: Boolean = true,
    val matches: List<CricketMatch> = emptyList(),
    val hostedMatches: List<CricketMatch> = emptyList(),
    val error: String? = null
)

private data class CricketTeam(
    val id: String,
    val name: String,
    val initials: String,
    val location: String,
    val accent: Color
)

private data class CricketTournament(
    val name: String,
    val date: String,
    val location: String,
    val status: String,
    val accent: Color,
    val visual: TournamentVisual
)

private enum class TournamentVisual { NEON, FIELD, TROPHY, SLAM }

@Composable
private fun rememberJoinedTeams(): List<CricketTeam> {
    var teams by remember { mutableStateOf(emptyList<CricketTeam>()) }
    DisposableEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            teams = emptyList()
            onDispose { }
        } else {
            val registration = FirebaseFirestore.getInstance().collection("teams")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        teams = emptyList()
                    } else {
                        teams = snapshot?.documents.orEmpty()
                            .filter { document -> document.belongsTo(userId) }
                            .map { document -> document.toCricketTeam() }
                            .sortedBy { it.name.lowercase() }
                    }
                }
            onDispose { registration.remove() }
        }
    }
    return teams
}

@Composable
private fun rememberPlayerMatches(userId: String?, playerTeamIds: Set<String>): PlayerMatchesState {
    var state by remember(userId) { mutableStateOf(PlayerMatchesState()) }
    DisposableEffect(userId, playerTeamIds) {
        if (userId.isNullOrBlank()) {
            state = PlayerMatchesState(loading = false)
            return@DisposableEffect onDispose { }
        }

        state = PlayerMatchesState()
        val registration = FirebaseFirestore.getInstance().collection("matches")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    state = PlayerMatchesState(loading = false, error = error.message)
                } else {
                    val documents = snapshot?.documents.orEmpty()
                    val matches = documents
                        .filter { document -> document.isPlayerMatch(userId, playerTeamIds) }
                        .mapNotNull { document -> document.toPlayerCricketMatch() }
                        .sortedWith(compareBy<CricketMatch> { it.status == "Finished" }.thenBy { it.dateEpochMs })
                    val hostedMatches = documents
                        .filter { document -> document.isHostedBy(userId) }
                        .mapNotNull { document -> document.toPlayerCricketMatch() }
                        .sortedWith(compareBy<CricketMatch> { it.status == "Finished" }.thenBy { it.dateEpochMs })
                    state = PlayerMatchesState(
                        loading = false,
                        matches = matches,
                        hostedMatches = hostedMatches
                    )
                }
            }
        onDispose { registration.remove() }
    }
    return state
}

private fun DocumentSnapshot.isHostedBy(userId: String): Boolean =
    getString("ownerId") == userId ||
        getString("ownerUserId") == userId ||
        getString("organiserId") == userId ||
        getString("organizerId") == userId

private fun DocumentSnapshot.isPlayerMatch(userId: String, playerTeamIds: Set<String>): Boolean {
    val teamA = get("teamA") as? Map<*, *>
    val teamB = get("teamB") as? Map<*, *>
    val involvedTeamIds = setOfNotNull(
        teamA?.get("teamId") as? String,
        teamB?.get("teamId") as? String,
        getString("teamAId"),
        getString("teamBId")
    )
    val playingXI = get("playingXI") as? Map<*, *>
    val lineupIncludesUser = playingXI?.values.orEmpty().any { xi ->
        val playerIds = (xi as? Map<*, *>)?.get("playerIds") as? List<*>
        userId in playerIds.orEmpty().filterIsInstance<String>()
    }
    val claimedByUser = (get("teamAClaim") as? Map<*, *>)?.get("userId") == userId ||
        (get("teamBClaim") as? Map<*, *>)?.get("userId") == userId
    return involvedTeamIds.any { it in playerTeamIds } || lineupIncludesUser || claimedByUser
}

private fun DocumentSnapshot.toPlayerCricketMatch(): CricketMatch? {
    val teamA = get("teamA") as? Map<*, *>
    val teamB = get("teamB") as? Map<*, *>
    val liveScore = get("liveScore") as? Map<*, *>
    val teamAName = getString("teamAName") ?: teamA?.get("name") as? String ?: "Team A"
    val teamBName = getString("teamBName") ?: teamB?.get("name") as? String ?: "Team B"
    val statusValue = (liveScore?.get("status") as? String)
        ?: getString("status")
        ?: getString("scheduleStatus")
        ?: "SCHEDULED"
    val finished = statusValue.equals("COMPLETED", true) || statusValue.equals("FINISHED", true)
    val live = statusValue.equals("LIVE", true) ||
        statusValue.equals("IN_PROGRESS", true) ||
        statusValue.equals("INNINGS_BREAK", true)
    val liveScoreText = liveScore?.let { score ->
        val runs = (score["score"] as? Number)?.toInt() ?: return@let null
        val wickets = (score["wickets"] as? Number)?.toInt() ?: 0
        val overs = score["overs"] as? String ?: "0.0"
        "$runs/$wickets ($overs)"
    }
    val battingTeamId = liveScore?.get("battingTeamId") as? String
    val teamAId = teamA?.get("teamId") as? String ?: getString("teamAId")
    val leftScore = liveScoreText.takeIf { battingTeamId != null && battingTeamId == teamAId }
    val teamBId = teamB?.get("teamId") as? String ?: getString("teamBId")
    val innings = get("innings") as? List<*>
    fun inningsScore(teamId: String?): String? = innings.orEmpty()
        .mapNotNull { it as? Map<*, *> }
        .lastOrNull { it["battingTeamId"] == teamId }
        ?.let { entry ->
            val runs = (entry["score"] as? Number)?.toInt() ?: return@let null
            val wickets = (entry["wickets"] as? Number)?.toInt() ?: 0
            val balls = (entry["legalBalls"] as? Number)?.toInt() ?: 0
            "$runs/$wickets (${balls / 6}.${balls % 6})"
        }
    val finalLeftScore = leftScore ?: inningsScore(teamAId)
    val rightScore = liveScoreText.takeIf { battingTeamId != null && battingTeamId == teamBId }
        ?: inningsScore(teamBId)
    val dateMillis = (get("matchDateEpochMs") as? Number)?.toLong() ?: 0L
    val dateLabel = if (dateMillis > 0L) {
        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            .format(java.util.Date(dateMillis))
    } else "Date not set"
    val time = getString("matchTime").orEmpty()
    val tournamentName = getString("tournamentName").orEmpty()
    val matchFormat = getString("format").orEmpty().ifBlank { getString("matchType").orEmpty() }
    val status = when {
        live -> "Live"
        finished -> "Finished"
        else -> "Upcoming"
    }
    return CricketMatch(
        type = when {
            tournamentName.isNotBlank() || getString("tournamentId") != null ->
                tournamentName.ifBlank { "TOURNAMENT MATCH" }.uppercase()
            matchFormat.isNotBlank() -> matchFormat.replace('_', ' ').uppercase()
            else -> "CRICKET MATCH"
        },
        title = getString("title")?.takeIf { it.isNotBlank() } ?: "$teamAName vs $teamBName",
        status = status,
        left = teamAName,
        leftScore = finalLeftScore,
        right = teamBName,
        rightScore = rightScore,
        scoreCenter = if (live) liveScoreText ?: "LIVE" else "VS",
        meta = listOf(dateLabel, time).filter { it.isNotBlank() }.joinToString(" • "),
        location = getString("venue")?.takeIf { it.isNotBlank() },
        live = live,
        accent = if (live) Color(0xFFFF5C65) else CricketBlue,
        dateEpochMs = dateMillis
    )
}

private fun DocumentSnapshot.belongsTo(userId: String): Boolean {
    if (getString("ownerUserId") == userId || getString("ownerId") == userId) return true

    val ids = (get("memberIds") as? List<*>)?.filterIsInstance<String>().orEmpty()
    if (userId in ids) return true

    val people = listOf("members", "players").flatMap { field ->
        (get(field) as? List<*>)?.filterIsInstance<Map<*, *>>().orEmpty()
    }
    return people.any { person ->
        person["userId"] == userId || person["linkedUserId"] == userId || person["playerId"] == userId
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toCricketTeam(): CricketTeam {
    val name =
        getString("teamName").orEmpty().ifBlank { getString("name").orEmpty().ifBlank { id } }
    val location = getString("city").orEmpty().ifBlank {
        getString("cityTown").orEmpty()
            .ifBlank { getString("location").orEmpty().ifBlank { "Team" } }
    }
    val initials = name.split(Regex("\\s+")).filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }.ifBlank { "TM" }
    val accents = listOf(
        Color(0xFF1E6CF1),
        Color(0xFF7B32D9),
        Color(0xFF079F89),
        Color(0xFFD51D49),
        Color(0xFFFFB340)
    )
    return CricketTeam(
        id,
        name,
        initials,
        location,
        accents[(id.hashCode() and Int.MAX_VALUE) % accents.size]
    )
}

private val sampleMatches = listOf(
    CricketMatch(
        type = "T20 FRIENDLY MATCH",
        title = "Neon Syndicate vs Thunder Strikers",
        status = "Upcoming",
        left = "Syndicate",
        right = "Thunder",
        scoreCenter = "VS",
        meta = "Today, 6:30 PM",
        accent = CricketBlue
    ),
    CricketMatch(
        type = "CORPORATE LEAGUE",
        title = "Phoenix XI vs Titans CC",
        status = "LIVE SOON",
        left = "Phoenix XI",
        right = "Titans CC",
        scoreCenter = "9:00 AM",
        meta = "Tomorrow",
        location = "Riverside Ground",
        liveSoon = true
    ),
    CricketMatch(
        type = "WEEKEND CUP • FINAL",
        title = "Warriors CC vs Blue Hawks",
        status = "Finished",
        left = "Warriors CC",
        leftScore = "184/6 (20)",
        right = "Blue Hawks",
        rightScore = "166/9 (20)",
        meta = "Warriors won by 18 runs",
        result = "Warriors won by 18 runs",
        potm = "R. Sharma (74)",
        accent = Color(0xFFFFCA64)
    ),
    CricketMatch(
        type = "INTER COLLEGE TROPHY",
        title = "SITAM CSE vs Apex College",
        status = "Scheduled",
        left = "SITAM CSE",
        right = "Apex College",
        scoreCenter = "VS",
        meta = "League fixture",
        accent = Color(0xFFFF8FB0)
    ),
    CricketMatch(
        type = "PRACTICE MATCH",
        title = "Challengers vs Royals",
        status = "Result",
        left = "Challengers",
        leftScore = "128/10",
        right = "Royals",
        rightScore = "131/5",
        meta = "Royals won by 5 wickets",
        result = "Royals won by 5 wickets"
    )
)

private val tournaments = listOf(
    CricketTournament(
        "Neon Pro League",
        "20 Jun - 30 Jun 2026",
        "BHUBANESWAR",
        "UPCOMING",
        CricketBlue,
        TournamentVisual.NEON
    ),
    CricketTournament(
        "Corporate Cricket Cup",
        "15 Jun - 25 Jun 2026",
        "HYDERABAD",
        "LIVE",
        CricketAccent,
        TournamentVisual.FIELD
    ),
    CricketTournament(
        "Balisahi Premier League (BPL)",
        "03 Jun - 05 Jun 2026",
        "ODISHA",
        "PAST",
        Color(0xFF88909A),
        TournamentVisual.TROPHY
    ),
    CricketTournament(
        "Summer Slam T20",
        "10 Jul - 20 Jul 2026",
        "MUMBAI",
        "SCHEDULED",
        Color(0xFFBFD8FF),
        TournamentVisual.SLAM
    )
)

@Composable
fun MyCricketScreen(
    onMenuClick: () -> Unit = {},
    onStartMatch: () -> Unit = {},
    initialTab: Int = 0,
    hostTournamentsViewModel: HostTournamentsViewModel? = null
) {
    val pagerState = rememberPagerState(
        initialPage = initialTab.coerceIn(0, cricketTabs.lastIndex),
        pageCount = { cricketTabs.size }
    )
    val coroutineScope = rememberCoroutineScope()
    val selectedTab = pagerState.currentPage
    var selectedMatchesTab by remember { mutableIntStateOf(0) }
    var selectedTournamentSegment by remember { mutableIntStateOf(0) }
    var tournamentPendingDeletion by remember { mutableStateOf<Tournament?>(null) }
    val joinedTeams = rememberJoinedTeams()
    val context = LocalContext.current
    val playerMatchesState = rememberPlayerMatches(
        FirebaseAuth.getInstance().currentUser?.uid,
        joinedTeams.mapTo(linkedSetOf()) { it.id }
    )
    val hostedTournamentsState by if (hostTournamentsViewModel != null) {
        hostTournamentsViewModel.uiState.collectAsState()
    } else {
        remember {
            mutableStateOf<HostTournamentsViewModel.UiState>(
                HostTournamentsViewModel.UiState.Content(emptyList())
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CricketBg)
    ) {
        CricketTopStrip(onMenuClick, selectedTab)
        CricketTabs(selectedTab = selectedTab, onSelect = { page ->
            coroutineScope.launch { pagerState.animateScrollToPage(page) }
        })
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 10.dp,
                    top = 10.dp,
                    end = 10.dp,
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
            when (page) {
                0 -> {
                    item { StartMatchPrompt(onStartMatch) }
                    item {
                        SegmentPills(
                            listOf("Player", "Host"),
                            active = selectedMatchesTab,
                            onTabClick = { selectedMatchesTab = it }
                        )
                    }
                    val matchesToShow = if (selectedMatchesTab == 0) {
                        playerMatchesState.matches
                    } else {
                        playerMatchesState.hostedMatches
                    }
                    val emptyTitle = if (selectedMatchesTab == 0) {
                        "No player matches yet"
                    } else {
                        "No hosted matches yet"
                    }
                    val emptyDetail = if (selectedMatchesTab == 0) {
                        "Matches involving your teams will appear here."
                    } else {
                        "Matches you host will appear here."
                    }
                    when {
                        playerMatchesState.loading -> item { AndroidView(factory = { context -> SkeletonMatchCardView(context) }, modifier = Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 16.dp, vertical = 8.dp)) }
                        playerMatchesState.error != null -> item {
                            TournamentFeedbackCard(
                                title = "Couldn't load your matches",
                                detail = playerMatchesState.error ?: "Please try again."
                            )
                        }
                        matchesToShow.isEmpty() -> item {
                            TournamentFeedbackCard(title = emptyTitle, detail = emptyDetail)
                        }
                        else -> items(matchesToShow) { match -> MatchCard(match) }
                    }
                }

                1 -> {
                    item { HostTournamentPrompt() }
                    item {
                        SegmentPills(
                            listOf("Your", "Participate", "Network"),
                            active = selectedTournamentSegment,
                            activeColor = CricketBlue,
                            onTabClick = { selectedTournamentSegment = it }
                        )
                    }
                    when (selectedTournamentSegment) {
                        0 -> hostedTournamentsContent(
                            state = hostedTournamentsState,
                            onTournamentClick = { tournamentId ->
                                context.startActivity(
                                    Intent(context, RegisterTournamentFinalPageActivity::class.java)
                                        .putExtra(RegisterTournamentFinalPageActivity.EXTRA_TOURNAMENT_ID, tournamentId)
                                )
                            },
                            onDeleteClick = { tournament -> tournamentPendingDeletion = tournament }
                        )
                        else -> items(tournaments) { tournament -> TournamentCard(tournament) }
                    }
                }

                2 -> {
                    item { CreateTeamPrompt() }
                    item { TeamFilterPills() }
                    item { QuickSearchBar() }
                    item { ActiveTeamsHeader(joinedTeams.size) }
                    if (joinedTeams.isEmpty()) {
                        item { NoJoinedTeamsCard() }
                    } else {
                        items(joinedTeams) { team -> TeamCard(team) }
                    }
                }

                3 -> {
                    item {
                        StatsContent(
                            userId = FirebaseAuth.getInstance().currentUser?.uid,
                            teamIds = joinedTeams.mapTo(linkedSetOf()) { it.id }
                        )
                    }
                }

            }
            item {
                Spacer(
                    Modifier
                        .height(30.dp)
                )
            }
            }
        }

        tournamentPendingDeletion?.let { tournament ->
            AlertDialog(
                onDismissRequest = { tournamentPendingDeletion = null },
                title = { Text("Delete tournament?") },
                text = { Text("${tournament.name} and its associated data will be deleted.") },
                confirmButton = {
                    TextButton(onClick = {
                        val viewModel = hostTournamentsViewModel
                        if (viewModel == null) {
                            Toast.makeText(context, "Unable to delete tournament", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.deleteTournament(tournament.id) { result ->
                                when (result) {
                                    is Resource.Success -> Toast.makeText(context, "Tournament deleted", Toast.LENGTH_SHORT).show()
                                    is Resource.Error -> Toast.makeText(
                                        context,
                                        result.message ?: "Unable to delete tournament",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    is Resource.Loading -> Unit
                                }
                            }
                        }
                        tournamentPendingDeletion = null
                    }) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { tournamentPendingDeletion = null }) { Text("Cancel") }
                }
            )
        }

    }
}

private fun LazyListScope.hostedTournamentsContent(
    state: HostTournamentsViewModel.UiState,
    onTournamentClick: (String) -> Unit,
    onDeleteClick: (Tournament) -> Unit
) {
    when (state) {
        HostTournamentsViewModel.UiState.Loading -> item { AndroidView(factory = { context -> SkeletonMatchCardView(context) }, modifier = Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 16.dp, vertical = 8.dp)) }

        is HostTournamentsViewModel.UiState.Error -> item {
            TournamentFeedbackCard(
                title = "Couldn't load your tournaments",
                detail = state.message
            )
        }

        is HostTournamentsViewModel.UiState.Content -> {
            if (state.tournaments.isEmpty()) {
                item {
                    TournamentFeedbackCard(
                        title = "No hosted tournaments yet",
                        detail = "Tournaments you register will appear here."
                    )
                }
            } else {
                items(state.tournaments, key = { tournament -> tournament.id }) { tournament ->
                    TournamentCard(
                        tournament = tournament.toCricketTournament(),
                        onClick = { onTournamentClick(tournament.id) },
                        onDelete = { onDeleteClick(tournament) }
                    )
                }
            }
        }
    }
}

private fun Tournament.toCricketTournament(): CricketTournament {
    val visuals = TournamentVisual.entries
    val displayLocation = city.ifBlank { ground }.ifBlank { "Location to be announced" }
    val displayDate = when {
        startDate.isNotBlank() -> startDate
        dateToBeAnnounced -> "Date to be announced"
        else -> "Start date to be announced"
    }
    return CricketTournament(
        name = name.ifBlank { "Untitled tournament" },
        date = displayDate,
        location = displayLocation.uppercase(),
        status = "HOSTED",
        accent = CricketBlue,
        visual = visuals[(id.hashCode() and Int.MAX_VALUE) % visuals.size]
    )
}

@Composable
private fun TournamentFeedbackCard(title: String, detail: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CricketCard)
            .border(1.dp, CricketStroke, RoundedCornerShape(18.dp))
            .padding(20.dp)
    ) {
        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(detail, color = CricketMuted, fontSize = 12.sp)
    }
}


@Composable
private fun CricketTopStrip(onMenuClick: () -> Unit, selectedTab: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(CricketBg)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderMenuButton(onMenuClick)
        Spacer(Modifier.width(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(R.drawable.appicon),
                contentDescription = "SportsXtreme app icon",
                modifier = Modifier.size(34.dp)
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = "Sports",
                color = Color(0xFFE8F1F6),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )
            Text(
                text = "Xtreme",
                color = Color(0xFF007FFF),
                fontSize = 16.5.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
private fun CricketTabs(selectedTab: Int, onSelect: (Int) -> Unit) {
    TabRow(
        modifier = Modifier.selectableGroup(),
        selectedTabIndex = selectedTab,
        containerColor = CricketBg,
        contentColor = CricketAccent,
        divider = {},
        indicator = { positions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.tabIndicatorOffset(positions[selectedTab]),
                height = 3.dp,
                color = CricketAccent
            )
        }
    ) {
        cricketTabs.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .selectable(
                        selected = index == selectedTab,
                        onClick = { onSelect(index) },
                        role = Role.Tab
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (index == selectedTab) CricketAccent else Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = if (index == selectedTab) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StartMatchPrompt(onStartMatch: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CricketCard)
            .border(1.dp, CricketStroke, RoundedCornerShape(18.dp))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Ready to organize your next match?",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Setup custom matches, invite teams, and track live scores effortlessly.",
                color = CricketMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CricketAccent)
                    .clickable(onClick = onStartMatch)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.baseline_add_24),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(Color.Black)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Start Match",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SegmentPills(
    labels: List<String>,
    active: Int,
    activeColor: Color = CricketAccent,
    onTabClick: (Int) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        labels.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (index == active) activeColor else CricketPanel)
                    .border(
                        1.dp,
                        if (index == active) activeColor else CricketStroke,
                        RoundedCornerShape(18.dp)
                    )
                    .clickable { onTabClick(index) }
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (index == active) {
                        if (activeColor == CricketAccent) Color.Black else Color.White
                    } else {
                        Color.White.copy(alpha = 0.7f)
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MatchCard(match: CricketMatch) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CricketCard)
            .border(1.dp, CricketStroke, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        match.type,
                        color = match.accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        match.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                StatusBadge(match.status, match.live, match.liveSoon)
            }
            Spacer(Modifier.height(20.dp))

            if (match.live) {
                LiveMatchLayout(match)
            } else {
                when (match.status) {
                    "Upcoming" -> UpcomingMatchLayout(match)
                    "LIVE SOON" -> LiveSoonMatchLayout(match)
                    "Finished" -> FinishedMatchLayout(match)
                    "Scheduled" -> ScheduledMatchLayout(match)
                    "Result" -> ResultMatchLayout(match)
                    else -> UpcomingMatchLayout(match)
                }
            }

            match.location?.let {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.loca),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(it, color = CricketMuted, fontSize = 12.sp)
                }
            }

            match.result?.let {
                Spacer(Modifier.height(12.dp))
                Text(
                    it,
                    color = CricketAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )
            }

            match.potm?.let {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "POTM: $it",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveMatchLayout(match: CricketMatch) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScoreRow(match.left, match.leftScore ?: "Yet to bat")
        ScoreRow(match.right, match.rightScore ?: "Yet to bat")
        Text("LIVE", color = Color(0xFFFF5C65), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun UpcomingMatchLayout(match: CricketMatch) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TeamNode(match.left, match.accent, Modifier.weight(1f))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(100.dp)
        ) {
            Text(
                match.scoreCenter ?: "VS",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(match.meta, color = CricketAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        TeamNode(match.right, match.accent, Modifier.weight(1f))
    }
}

@Composable
private fun LiveSoonMatchLayout(match: CricketMatch) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TeamNode(match.left, match.accent, Modifier.weight(1f))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(100.dp)
        ) {
            Text("Tomorrow", color = CricketMuted, fontSize = 10.sp)
            Text(
                match.scoreCenter ?: "9:00",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text("AM", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        TeamNode(match.right, match.accent, Modifier.weight(1f))
    }
}

@Composable
private fun FinishedMatchLayout(match: CricketMatch) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScoreRow(match.left, match.leftScore ?: "")
        ScoreRow(match.right, match.rightScore ?: "")
    }
}

@Composable
private fun ScheduledMatchLayout(match: CricketMatch) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        BigTeamNode(match.left, match.accent, Modifier.weight(1f))
        Text(
            "vs",
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 16.sp,
            fontStyle = FontStyle.Italic
        )
        BigTeamNode(match.right, match.accent, Modifier.weight(1f))
    }
}

@Composable
private fun ResultMatchLayout(match: CricketMatch) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(match.left, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Text(
                match.leftScore ?: "",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(match.right, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Text(
                match.rightScore ?: "",
                color = CricketAccent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                match.result ?: "",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ScoreRow(team: String, score: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.1f))
        )
        Spacer(Modifier.width(12.dp))
        Text(
            team,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(score, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BigTeamNode(name: String, accent: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(name.take(1), color = accent, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Text(name, color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun TeamNode(name: String, accent: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ball),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            name,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
private fun StatusBadge(label: String, live: Boolean, liveSoon: Boolean = false) {
    val bgColor = when {
        live -> CricketAccent.copy(alpha = 0.15f)
        liveSoon -> CricketAccent.copy(alpha = 0.1f)
        label == "Finished" -> Color.White.copy(alpha = 0.1f)
        else -> Color.White.copy(alpha = 0.05f)
    }

    val textColor = when {
        live -> CricketAccent
        liveSoon -> CricketAccent
        else -> Color.White.copy(alpha = 0.7f)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (liveSoon) {
                Box(modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(CricketAccent))
                Spacer(Modifier.width(6.dp))
            }
            Text(label, color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String, showInfo: Boolean = false) {
    Row(
        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = Color.White,
                fontSize = 19.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                subtitle,
                color = CricketMuted,
                fontSize = 9.5.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 7.dp)
            )
        }
        if (showInfo) {
            Image(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.6f))
            )
        }
    }
}

private data class StatItem(val label: String, val value: String)

private data class CricketStatsState(
    val loading: Boolean = true,
    val error: String? = null,
    val batting: List<StatItem> = emptyList(),
    val bowling: List<StatItem> = emptyList(),
    val fielding: List<StatItem> = emptyList(),
    val captaincy: List<StatItem> = emptyList()
)

private data class MatchStatRows(
    val matchId: String,
    val match: DocumentSnapshot,
    val batting: List<Map<String, Any?>>,
    val bowling: List<Map<String, Any?>>,
    val deliveries: List<Map<String, Any?>>
)

@Composable
private fun rememberPlayerStats(userId: String?, teamIds: Set<String>): CricketStatsState {
    var state by remember(userId, teamIds) { mutableStateOf(CricketStatsState()) }
    DisposableEffect(userId, teamIds) {
        if (userId.isNullOrBlank() || teamIds.isEmpty()) {
            state = emptyPlayerStats(loading = false)
            return@DisposableEffect onDispose { }
        }

        val firestore = FirebaseFirestore.getInstance()
        var matchDocuments = emptyList<DocumentSnapshot>()
        var captainTeamIds = emptySet<String>()
        var loadVersion = 0

        fun refreshStats() {
            val playerIds = teamIds.mapTo(linkedSetOf()) { "$it::$userId" }.apply { add(userId) }
            val relevantMatches = matchDocuments.filter { match ->
                val matchTeams = match.statsTeamIds()
                val lineup = match.statsLineupIds()
                matchTeams.any { it in teamIds } || lineup.any { it in playerIds }
            }
            if (relevantMatches.isEmpty()) {
                state = emptyPlayerStats(loading = false)
                return
            }

            val version = ++loadVersion
            state = CricketStatsState(loading = true)
            val rows = mutableListOf<MatchStatRows>()
            var remaining = relevantMatches.size
            var failures = 0
            val lock = Any()

            fun finishedOne() {
                synchronized(lock) {
                    remaining -= 1
                    if (remaining != 0 || version != loadVersion) return
                    state = if (rows.isEmpty() && failures > 0) {
                        CricketStatsState(loading = false, error = "Scorecards are temporarily unavailable.")
                    } else {
                        aggregatePlayerStats(rows, userId, teamIds, captainTeamIds)
                    }
                }
            }

            relevantMatches.forEach { match ->
                val ref = match.reference
                val battingTask = ref.collection("scorecard").document("batting").collection("entries").get()
                val bowlingTask = ref.collection("scorecard").document("bowling").collection("entries").get()
                val deliveriesTask = ref.collection("deliveries").get()
                com.google.android.gms.tasks.Tasks.whenAllSuccess<com.google.firebase.firestore.QuerySnapshot>(
                    listOf(battingTask, bowlingTask, deliveriesTask)
                ).addOnSuccessListener { snapshots ->
                    val battingRows = snapshots.getOrNull(0)?.documents.orEmpty().map { it.data.orEmpty() }
                    val bowlingRows = snapshots.getOrNull(1)?.documents.orEmpty().map { it.data.orEmpty() }
                    val deliveryRows = snapshots.getOrNull(2)?.documents.orEmpty().map { it.data.orEmpty() }
                    synchronized(lock) { rows += MatchStatRows(match.id, match, battingRows, bowlingRows, deliveryRows) }
                    finishedOne()
                }.addOnFailureListener {
                    synchronized(lock) { failures += 1 }
                    finishedOne()
                }
            }
        }

        val matchesListener = firestore.collection("matches").addSnapshotListener { snapshot, error ->
            if (error != null) {
                state = CricketStatsState(loading = false, error = error.message ?: "Unable to load matches.")
            } else {
                matchDocuments = snapshot?.documents.orEmpty()
                refreshStats()
            }
        }
        val teamsListener = firestore.collection("teams")
            .whereArrayContains("memberIds", userId)
            .addSnapshotListener { snapshot, _ ->
                captainTeamIds = snapshot?.documents.orEmpty().filter { team ->
                    val member = (team.get("members") as? List<*>)
                        .orEmpty().filterIsInstance<Map<*, *>>()
                        .firstOrNull { it["userId"] == userId }
                    val roles = (member?.get("roles") as? List<*>)?.filterIsInstance<String>().orEmpty()
                    val legacyRole = member?.get("role") as? String
                    "CAPTAIN" in roles || "ADMIN" in roles || legacyRole == "CAPTAIN" ||
                        team.getString("ownerUserId") == userId || team.getString("ownerId") == userId
                }.mapTo(linkedSetOf()) { it.id }
                refreshStats()
            }
        onDispose {
            matchesListener.remove()
            teamsListener.remove()
        }
    }
    return state
}

private fun emptyPlayerStats(loading: Boolean): CricketStatsState = CricketStatsState(
    loading = loading,
    batting = listOf("MAT", "INNS", "NO", "RUNS", "HS", "AVG", "SR", "30s", "50s", "100s", "4s", "6s", "DUCKS", "WON", "LOSS").map { StatItem(it, "0") },
    bowling = listOf("MAT", "INNS", "OVERS", "MAIDENS", "RUNS", "WKTS", "BB", "3 WKTS", "5 WKTS", "ECO", "SR", "AVG", "WD", "NB", "DOTS", "4S", "6S").map { StatItem(it, "0") },
    fielding = listOf("MAT", "CATCHES", "C.B", "R/O", "ST").map { StatItem(it, "0") },
    captaincy = listOf("MAT", "TOSS WON", "WON", "LOST", "WIN %", "LOSS %").map { StatItem(it, "0") }
)

private fun DocumentSnapshot.statsTeamIds(): Set<String> {
    val teamA = get("teamA") as? Map<*, *>
    val teamB = get("teamB") as? Map<*, *>
    return setOfNotNull(
        teamA?.get("teamId") as? String,
        teamB?.get("teamId") as? String,
        getString("teamAId"),
        getString("teamBId")
    )
}

private fun DocumentSnapshot.statsLineupIds(): Set<String> =
    (get("playingXI") as? Map<*, *>)?.values.orEmpty()
        .flatMap { ((it as? Map<*, *>)?.get("playerIds") as? List<*>).orEmpty().filterIsInstance<String>() }
        .toSet()

private fun aggregatePlayerStats(
    matches: List<MatchStatRows>,
    userId: String,
    teamIds: Set<String>,
    captainTeamIds: Set<String>
): CricketStatsState {
    val playerIds = teamIds.mapTo(linkedSetOf()) { "$it::$userId" }.apply { add(userId) }
    val batting = matches.flatMap { match ->
        match.batting.filter { it["playerId"] in playerIds }.map { match to it }
    }.filter { (_, row) ->
        val status = (row["status"] as? String).orEmpty().uppercase()
        val balls = (row["balls"] as? Number)?.toInt() ?: 0
        val runs = (row["runs"] as? Number)?.toInt() ?: 0
        status !in setOf("YET_TO_BAT", "NOT_BATTED") && (status.isNotBlank() || balls > 0 || runs > 0)
    }
    val bowling = matches.flatMap { match ->
        match.bowling.filter { it["playerId"] in playerIds }.map { match to it }
    }
    val fielding = matches.flatMap { match ->
        match.deliveries.filter { it["fielderId"] in playerIds }.map { match to it }
    }

    fun number(row: Map<String, Any?>, key: String): Int = (row[key] as? Number)?.toInt() ?: 0
    fun formatRate(value: Double): String = if (value.isFinite()) String.format(java.util.Locale.getDefault(), "%.2f", value) else "0.00"
    fun inningsScores(match: DocumentSnapshot): Map<String, Int> = (match.get("innings") as? List<*>)
        .orEmpty().filterIsInstance<Map<*, *>>()
        .mapNotNull { inning ->
            val teamId = inning["battingTeamId"] as? String ?: return@mapNotNull null
            teamId to ((inning["score"] as? Number)?.toInt() ?: 0)
        }.groupBy({ it.first }, { it.second }).mapValues { (_, scores) -> scores.sum() }

    fun matchResult(match: DocumentSnapshot, teams: Set<String>): Int? {
        if ((match.getString("status") ?: "").uppercase() !in setOf("COMPLETED", "FINISHED")) return null
        val scores = inningsScores(match)
        if (scores.size < 2) return null
        val teamA = ((match.get("teamA") as? Map<*, *>)?.get("teamId") as? String) ?: match.getString("teamAId")
        val teamB = ((match.get("teamB") as? Map<*, *>)?.get("teamId") as? String) ?: match.getString("teamBId")
        val ownTeam = listOfNotNull(teamA, teamB).firstOrNull { it in teams } ?: return null
        val otherTeam = listOfNotNull(teamA, teamB).firstOrNull { it != ownTeam } ?: return null
        val ownScore = scores[ownTeam] ?: return null
        val otherScore = scores[otherTeam] ?: return null
        return ownScore.compareTo(otherScore)
    }

    val battingMatches = batting.map { it.first.matchId }.distinct().size
    val batRuns = batting.sumOf { number(it.second, "runs") }
    val batBalls = batting.sumOf { number(it.second, "balls") }
    val notOuts = batting.count { (it.second["status"] as? String).equals("NOT_OUT", true) || (it.second["status"] as? String).equals("RETIRED_HURT", true) }
    val outs = batting.count { (it.second["status"] as? String).equals("OUT", true) || (it.second["status"] as? String).equals("RETIRED_OUT", true) }
    val scores = batting.map { number(it.second, "runs") }
    val highestInnings = batting.maxByOrNull { number(it.second, "runs") }
    val winsLosses = batting.map { it.first }.distinctBy { it.matchId }.mapNotNull { match ->
        matchResult(match.match, match.match.statsTeamIds())
    }
    val fours = batting.sumOf { number(it.second, "fours") }
    val sixes = batting.sumOf { number(it.second, "sixes") }
    val battingStats = listOf(
        StatItem("MAT", battingMatches.toString()), StatItem("INNS", batting.size.toString()), StatItem("NO", notOuts.toString()),
        StatItem("RUNS", batRuns.toString()), StatItem("HS", (highestInnings?.let { number(it.second, "runs") } ?: 0).toString() + if ((highestInnings?.second?.get("status") as? String).equals("NOT_OUT", true)) "*" else ""),
        StatItem("AVG", if (outs == 0) "-" else formatRate(batRuns.toDouble() / outs)),
        StatItem("SR", formatRate(if (batBalls == 0) 0.0 else batRuns * 100.0 / batBalls)),
        StatItem("30s", scores.count { it in 30..49 }.toString()), StatItem("50s", scores.count { it in 50..99 }.toString()),
        StatItem("100s", scores.count { it >= 100 }.toString()), StatItem("4s", fours.toString()), StatItem("6s", sixes.toString()),
        StatItem("DUCKS", batting.count { number(it.second, "runs") == 0 && (it.second["status"] as? String).equals("OUT", true) }.toString()),
        StatItem("WON", winsLosses.count { it > 0 }.toString()), StatItem("LOSS", winsLosses.count { it < 0 }.toString())
    )

    val legalBalls = bowling.sumOf { number(it.second, "legalBalls") }
    val bowlRuns = bowling.sumOf { number(it.second, "runs") }
    val wickets = bowling.sumOf { number(it.second, "wickets") }
    val bestBowling = bowling.map { number(it.second, "wickets") to number(it.second, "runs") }
        .maxWithOrNull(compareBy<Pair<Int, Int>> { it.first }.thenByDescending { it.second }) ?: (0 to 0)
    fun deliveriesForBowlingRow(match: MatchStatRows, row: Map<String, Any?>): List<Map<String, Any?>> =
        match.deliveries.filter { delivery ->
            delivery["bowlerId"] == row["playerId"] &&
                (row["inningsId"] == null || delivery["inningsId"] == row["inningsId"])
        }
    val dotBalls = bowling.sumOf { (match, row) ->
        deliveriesForBowlingRow(match, row).count { number(it, "runs") == 0 && number(it, "extras") == 0 }
    }
    val foursConceded = bowling.sumOf { (match, row) ->
        deliveriesForBowlingRow(match, row).count { number(it, "runs") == 4 }
    }
    val sixesConceded = bowling.sumOf { (match, row) ->
        deliveriesForBowlingRow(match, row).count { number(it, "runs") == 6 }
    }
    val bowlingStats = listOf(
        StatItem("MAT", bowling.map { it.first.matchId }.distinct().size.toString()), StatItem("INNS", bowling.size.toString()),
        StatItem("OVERS", "${legalBalls / 6}.${legalBalls % 6}"), StatItem("MAIDENS", bowling.sumOf { number(it.second, "maidens") }.toString()),
        StatItem("RUNS", bowlRuns.toString()), StatItem("WKTS", wickets.toString()), StatItem("BB", "${bestBowling.first}/${bestBowling.second}"),
        StatItem("3 WKTS", bowling.count { number(it.second, "wickets") >= 3 }.toString()),
        StatItem("5 WKTS", bowling.count { number(it.second, "wickets") >= 5 }.toString()),
        StatItem("ECO", formatRate(if (legalBalls == 0) 0.0 else bowlRuns * 6.0 / legalBalls)),
        StatItem("SR", formatRate(if (wickets == 0) 0.0 else legalBalls.toDouble() / wickets)),
        StatItem("AVG", formatRate(if (wickets == 0) 0.0 else bowlRuns.toDouble() / wickets)),
        StatItem("WD", bowling.sumOf { number(it.second, "wides") }.toString()),
        StatItem("NB", bowling.sumOf { number(it.second, "noBalls") }.toString()),
        StatItem("DOTS", dotBalls.toString()), StatItem("4S", foursConceded.toString()),
        StatItem("6S", sixesConceded.toString())
    )

    val caughtBowled = fielding.count { (match, row) ->
        (row["dismissalType"] as? String).equals("CAUGHT", true) && row["fielderId"] == row["bowlerId"]
    }
    val catches = fielding.count { (_, row) -> (row["dismissalType"] as? String).equals("CAUGHT", true) } - caughtBowled
    val runOuts = fielding.count { (_, row) -> (row["dismissalType"] as? String).equals("RUN_OUT", true) }
    val stumpings = fielding.count { (_, row) -> (row["dismissalType"] as? String).equals("STUMPED", true) }
    val fieldingStats = listOf(
        StatItem("MAT", matches.filter { match -> match.match.statsLineupIds().any { it in playerIds } }.size.toString()),
        StatItem("CATCHES", catches.toString()), StatItem("C.B", caughtBowled.toString()),
        StatItem("R/O", runOuts.toString()), StatItem("ST", stumpings.toString())
    )

    val captainMatches = matches.filter { row ->
        row.match.statsTeamIds().any { it in captainTeamIds } &&
            (row.match.getString("status") ?: "").uppercase() in setOf("COMPLETED", "FINISHED", "LIVE", "IN_PROGRESS", "INNINGS_BREAK")
    }
    val completedCaptainMatches = captainMatches.filter { (it.match.getString("status") ?: "").uppercase() in setOf("COMPLETED", "FINISHED") }
    val captainResults = completedCaptainMatches.mapNotNull { row -> matchResult(row.match, row.match.statsTeamIds().intersect(captainTeamIds)) }
    val tossWins = captainMatches.count { row ->
        val toss = row.match.get("toss") as? Map<*, *>
        (toss?.get("winnerTeamId") as? String) in captainTeamIds
    }
    val captainWins = captainResults.count { it > 0 }
    val captainLosses = captainResults.count { it < 0 }
    val captainDecisions = captainWins + captainLosses
    val captaincyStats = listOf(
        StatItem("MAT", captainMatches.size.toString()), StatItem("TOSS WON", tossWins.toString()),
        StatItem("WON", captainWins.toString()), StatItem("LOST", captainLosses.toString()),
        StatItem("WIN %", formatRate(if (captainDecisions == 0) 0.0 else captainWins * 100.0 / captainDecisions) + "%"),
        StatItem("LOSS %", formatRate(if (captainDecisions == 0) 0.0 else captainLosses * 100.0 / captainDecisions) + "%")
    )
    return CricketStatsState(loading = false, batting = battingStats, bowling = bowlingStats, fielding = fieldingStats, captaincy = captaincyStats)
}

@Composable
private fun StatsContent(userId: String?, teamIds: Set<String>) {
    var selectedStatTab by remember { mutableIntStateOf(0) }
    val stats = rememberPlayerStats(userId, teamIds)
    Column {
        SegmentPills(
            labels = listOf("Batting", "Bowling", "Fielding", "Captaincy"),
            active = selectedStatTab,
            onTabClick = { selectedStatTab = it }
        )

        Spacer(Modifier.height(20.dp))
        when {
            stats.loading -> TournamentFeedbackCard("Loading your stats…", "Fetching your match scorecards.")
            stats.error != null -> TournamentFeedbackCard("Couldn't load your stats", stats.error)
            else -> StatCategoryContent(selectedStatTab, stats)
        }
    }
}

@Composable
private fun StatCategoryContent(tabIndex: Int, state: CricketStatsState) {
    val categoryStats = when (tabIndex) {
        0 -> state.batting
        1 -> state.bowling
        2 -> state.fielding
        else -> state.captaincy
    }

    StatSection("Overall", categoryStats)
}

@Composable
private fun StatSection(title: String, stats: List<StatItem>, isTennis: Boolean = false) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            if (isTennis) {
                Image(
                    painter = painterResource(R.drawable.badmintonlogo),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
            } else {
                Box(
                    Modifier
                        .width(4.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF246CE6))
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
            if (!isTennis) {
                Box(
                    Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.groups),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            colorFilter = ColorFilter.tint(CricketAccent)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Compare",
                            color = CricketAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Image(
                        painter = painterResource(R.drawable.network),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(CricketAccent)
                    )
                    Text("WW", color = CricketAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Image(
                        painter = painterResource(R.drawable.list),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.6f))
                    )
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        StatGrid(stats)
    }
}

@Composable
private fun StatGrid(items: List<StatItem>) {
    // Custom grid using Rows to avoid nested scrolling issues in LazyColumn if any
    val rows = items.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { item ->
                    StatBox(item, Modifier.weight(1f))
                }
                // Fill empty slots if last row has fewer than 3 items
                repeat(3 - rowItems.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatBox(item: StatItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CricketPanel)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            item.value,
            color = if (item.value.contains("%")) CricketAccent else Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            item.label,
            color = CricketMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HostTournamentPrompt() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CricketCard)
            .border(1.dp, CricketStroke, RoundedCornerShape(18.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1D5EF7).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFF1D5EF7), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.jod),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color(0xFFBFD8FF))
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = "Want to host\na tournament?",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(CricketAccent)
                    .clickable { /* Handle register */ }
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Register",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TournamentCard(
    tournament: CricketTournament,
    onClick: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    var menuExpanded by remember(tournament.name) { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .background(CricketCard)
            .border(
                1.dp,
                if (tournament.status == "LIVE") CricketAccent else CricketStroke,
                RoundedCornerShape(18.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            TournamentVisualPanel(tournament)

            // Gradient overlay for title readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 300f
                        )
                    )
            )

            TournamentStatusTag(
                label = tournament.status,
                live = tournament.status == "LIVE",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            )

            if (onDelete != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 48.dp, end = 8.dp)
                ) {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Tournament options",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete tournament") },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Text(
                tournament.name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                TournamentMetaLine(tournament.date, iconRes = R.drawable.calender)
                Spacer(Modifier.height(6.dp))
                TournamentMetaLine(tournament.location, iconRes = R.drawable.loca)
            }
            TournamentArrow(if (tournament.status == "LIVE") CricketAccent else Color.White)
        }
    }
}

@Composable
private fun TournamentVisualPanel(tournament: CricketTournament) {
    val imageRes = when (tournament.visual) {
        TournamentVisual.NEON -> R.drawable.stadium
        TournamentVisual.FIELD -> R.drawable.club_stadium
        TournamentVisual.TROPHY -> R.drawable.trophyfull
        TournamentVisual.SLAM -> R.drawable.cricketblast
    }

    Image(
        painter = painterResource(imageRes),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}



@Composable
private fun TournamentStatusTag(label: String, live: Boolean, modifier: Modifier = Modifier) {
    val bgColor = when {
        live -> CricketAccent
        label == "PAST" -> Color.White.copy(alpha = 0.1f)
        else -> CricketStroke.copy(alpha = 0.4f)
    }
    val textColor = when {
        live -> Color.Black
        label == "PAST" -> Color.White.copy(alpha = 0.5f)
        else -> Color.White
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (live) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TournamentMetaLine(text: String, iconRes: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.6f))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TournamentArrow(accent: Color) {
    Canvas(Modifier.size(24.dp)) {
        drawLine(
            accent,
            Offset(size.width * 0.36f, size.height * 0.2f),
            Offset(size.width * 0.68f, size.height * 0.5f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            accent,
            Offset(size.width * 0.68f, size.height * 0.5f),
            Offset(size.width * 0.36f, size.height * 0.8f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun CreateTeamPrompt() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ClubCardBlack)
            .border(1.dp, ClubCardBorder, RoundedCornerShape(18.dp))
            .drawBehind {
                drawLine(
                    color = CricketAccent,
                    start = Offset(4.dp.toPx(), 16.dp.toPx()),
                    end = Offset(4.dp.toPx(), size.height - 16.dp.toPx()),
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            .padding(start = 24.dp, top = 20.dp, end = 20.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Want to create a new team?",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Start your own legacy in the community.",
                color = ClubMuted,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Box(
            modifier = Modifier
                .height(40.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CricketAccent)
                .clickable { /* Create team action */ }
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Create",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.W800
            )
        }
    }
}

@Composable
private fun TeamFilterPills() {
    val labels = listOf("Your teams", "Opponents", "Following")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        labels.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (index == 0) CricketAccent else Color.White.copy(alpha = 0.1f))
                    .then(
                        if (index != 0) Modifier.border(
                            1.dp,
                            ClubCardBorder,
                            RoundedCornerShape(20.dp)
                        ) else Modifier
                    )
                    .clickable { /* Filter action */ }
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (index == 0) Color.Black else Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun QuickSearchBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(ClubCardBlack)
            .border(1.dp, ClubCardBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.search),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(ClubMuted)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Quick search",
                color = ClubMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ActiveTeamsHeader(teamCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(CricketAccent)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "ACTIVE TEAMS",
            color = CricketAccent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun NoJoinedTeamsCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ClubCardBlack)
            .border(1.dp, ClubCardBorder, RoundedCornerShape(18.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "No teams joined yet",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Teams you join will appear here automatically.",
            color = ClubMuted,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TeamCard(team: CricketTeam) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ClubCardBlack)
            .border(1.dp, ClubCardBorder, RoundedCornerShape(18.dp))
            .clickable {
                context.startActivity(
                    Intent(
                        context,
                        TeamProfileActivity::class.java
                    ).putExtra(TeamProfileActivity.EXTRA_TEAM_ID, team.id)
                )
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(team.accent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                team.initials,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                team.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.loca),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    colorFilter = ColorFilter.tint(ClubMuted)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    team.location,
                    color = ClubMuted,
                    fontSize = 14.sp
                )
            }
        }
        Text(
            "⋮",
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 20.sp
        )
    }
}

@Composable
private fun StatsMatrix() {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatTile("Matches", "42", "+8 this month", CricketAccent, Modifier.weight(1f))
            StatTile("Win Rate", "68%", "last 12 games", CricketBlue, Modifier.weight(1f))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatTile("Runs", "5.8K", "team total", Color(0xFFFFCA64), Modifier.weight(1f))
            StatTile("Wickets", "214", "all squads", Color(0xFFFF8FB0), Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    note: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(108.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(CricketPanel)
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(11.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = CricketMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(
            value,
            color = Color.White,
            fontSize = 25.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(note, color = accent, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun MomentumCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF07101A),
                        Color(0xFF0B2038),
                        Color(0xFF172608)
                    )
                )
            )
            .border(1.dp, CricketStroke, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                "AI Momentum",
                color = CricketAccent,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Syndicate form is climbing",
                color = Color.White,
                fontSize = 17.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 7.dp)
            )
            Text(
                "Bowling economy improved by 11% across the last five matches.",
                color = CricketMuted,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun TimelineCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(CricketPanel)
            .border(1.dp, CricketStroke, RoundedCornerShape(11.dp))
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Saved scorecards",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
        )
        listOf(
            "Phoenix XI beat Titans CC",
            "Royals chased 132 in 18.4 overs",
            "Warriors CC lifted Weekend Cup"
        ).forEach {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(CricketAccent))
                Spacer(Modifier.width(9.dp))
                Text(it, color = Color(0xFFDDE7E3), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HeaderMenuButton(onMenuClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(7.dp))
            .clickable(onClick = onMenuClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(18.dp)) {
            val tint = Color(0xFF8E9E99)
            val stroke = 1.8.dp.toPx()
            drawLine(
                tint,
                Offset(size.width * 0.16f, size.height * 0.28f),
                Offset(size.width * 0.84f, size.height * 0.28f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                tint,
                Offset(size.width * 0.16f, size.height * 0.5f),
                Offset(size.width * 0.84f, size.height * 0.5f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                tint,
                Offset(size.width * 0.16f, size.height * 0.72f),
                Offset(size.width * 0.84f, size.height * 0.72f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}
