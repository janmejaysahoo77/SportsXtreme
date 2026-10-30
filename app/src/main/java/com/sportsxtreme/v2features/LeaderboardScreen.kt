package com.sportsxtreme.v2features

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.components.SportsXtremeAppBar
import com.example.sportsxtreme.presentation.profile.ProfileActivity
import com.example.sportsxtreme.presentation.team.TeamProfileActivity
import com.example.sportsxtreme.presentation.profile.Lime
import coil3.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import android.content.Intent
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Color Palette matching pixel-perfect screenshots
private val GreenAccent = Color(0xFFC1FF00)
private val CyanAccent = Color(0xFF00D2FF)
private val GoldAccent = Color(0xFFFFB800)
private val ScreenBg = Color(0xFF010509)
private val CardBg = Color(0xFF080F14)
private val MutedText = Color(0xFF93A09D)
private val TableBorder = Color(0x1F6A7480)
private val PillBorder = Color(0xFFC1FF00)
private val TrendUpColor = Color(0xFF28D158)
private val TrendDownColor = Color(0xFFFF3B30)

enum class LeaderboardTab {
    PLAYER, TEAMS
}

enum class TrendType {
    UP, DOWN, SAME
}

data class Trend(
    val type: TrendType,
    val amount: Int = 0
)

data class PlayerRank(
    val rank: Int,
    val trend: Trend,
    val initials: String,
    val name: String,
    val country: String,
    val teamCode: String,
    val points: Int,
    val avatarBorderColor: Color,
    val photoUrl: String = "",
    val statistics: String = "",
    val userId: String = ""
)

data class ClubRank(
    val rank: Int,
    val trend: Trend,
    val code: String,
    val fullName: String,
    val points: Int,
    val logoUrl: String = "",
    val statistics: String = "",
    val tieBreakValue: Double = 0.0,
    val teamId: String = ""
)

@Composable
fun LeaderboardScreen(
    onMenuClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(LeaderboardTab.PLAYER) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Filter states
    var selectedSkill by remember { mutableStateOf("Batting") }
    var selectedType by remember { mutableStateOf("Leather Ball") }
    var selectedFormat by remember { mutableStateOf("Limited Overs") }
    var selectedTournamentId by remember { mutableStateOf("") }
    var selectedTournamentName by remember { mutableStateOf("Overall") }

    // Dropdown visibility states
    var isSkillDropdownOpen by remember { mutableStateOf(false) }
    var isTypeDropdownOpen by remember { mutableStateOf(false) }
    var isFormatDropdownOpen by remember { mutableStateOf(false) }
    var isTournamentDropdownOpen by remember { mutableStateOf(false) }

    val skills = listOf("Batting", "Bowling", "All-rounder")
    val types = listOf("Tennis Ball", "Leather Ball", "Other Ball")
    val formats = listOf("Limited Overs", "T20", "T10", "Test")
    val playerRows = remember { androidx.compose.runtime.mutableStateListOf<PlayerRank>() }
    val teamRows = remember { androidx.compose.runtime.mutableStateListOf<ClubRank>() }
    val tournamentOptions = remember { androidx.compose.runtime.mutableStateListOf<Pair<String, String>>() }
    var leaderboardError by remember { mutableStateOf<String?>(null) }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val leaderboardScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    DisposableEffect(firestore) {
        val registration = firestore.collection("tournaments").addSnapshotListener { snapshot, _ ->
            tournamentOptions.clear()
            tournamentOptions.add("" to "Overall")
            tournamentOptions.addAll(snapshot?.documents.orEmpty().map { it.id to (it.getString("name") ?: "Tournament") }.sortedBy { it.second })
        }
        onDispose { registration.remove() }
    }

    DisposableEffect(firestore, selectedTab, selectedType, selectedFormat, selectedSkill, selectedTournamentId) {
        val ballKey = when (selectedType) { "Tennis Ball" -> "TENNIS"; "Other Ball" -> "OTHER"; else -> "LEATHER" }
        val formatKey = when (selectedFormat) { "Limited Overs" -> "LIMITED_OVERS"; "Test" -> "TEST"; else -> selectedFormat.uppercase() }
        val activeTournamentId = if (selectedTab == LeaderboardTab.PLAYER) "" else selectedTournamentId
        val registrations = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()
        var effectActive = true
        var playerSnapshotRevision = 0L
        var teamSnapshotRevision = 0L
        if (selectedTab == LeaderboardTab.PLAYER) {
            val registration = firestore.collection("leaderboards").document("playerAggregates").collection("entries")
                .whereEqualTo("sport", "CRICKET")
                .addSnapshotListener { snapshot, error ->
                    playerRows.clear()
                    if (error != null) {
                        leaderboardError = "${error.code}: ${error.localizedMessage ?: "Unable to load player leaderboard"} (leaderboards/playerAggregates/entries)"
                        return@addSnapshotListener
                    }
                    leaderboardError = null
                    val documents = snapshot?.documents.orEmpty().filter { doc ->
                        doc.getString("ballType") == ballKey &&
                            doc.getString("format") == formatKey &&
                            doc.getString("tournamentId").orEmpty() == activeTournamentId
                    }
                    val revision = ++playerSnapshotRevision
                    leaderboardScope.launch {
                        // Older deleted accounts may still have aggregate rows from
                        // before deleteAccount began pruning leaderboard contributions.
                        // A canonical ID distinct from the player ID identifies a
                        // linked account; hide that row if its user profile is gone.
                        val linkedAccountIds = documents.mapNotNull { doc ->
                            val playerId = doc.getString("playerId")?.trim().orEmpty()
                            val canonicalId = doc.getString("canonicalPlayerId")?.trim()
                                ?.takeIf(String::isNotBlank)
                                ?: playerId.substringAfterLast("::").trim()
                            canonicalId.takeIf { it.isNotBlank() && it != playerId }
                        }.distinct()
                        val profileLookupIds = documents.mapNotNull { doc ->
                            val playerId = doc.getString("playerId")?.trim().orEmpty()
                            doc.getString("canonicalPlayerId")?.trim()?.takeIf(String::isNotBlank)
                                ?: playerId.substringAfterLast("::").trim().takeIf(String::isNotBlank)
                        }.distinct()
                        val publicProfiles = runCatching {
                            profileLookupIds.chunked(30).flatMap { accountIds ->
                                firestore.collection("publicProfiles")
                                    .whereIn(FieldPath.documentId(), accountIds)
                                    .get()
                                    .await()
                                    .documents
                            }.associateBy { it.id }
                        }.getOrDefault(emptyMap())
                        val existingAccountIds = if (publicProfiles.isNotEmpty() || profileLookupIds.isEmpty()) {
                            publicProfiles.keys
                        } else {
                            // A temporary profile lookup failure should not hide
                            // otherwise valid leaderboard rows.
                            linkedAccountIds.toSet()
                        }
                        if (!effectActive || revision != playerSnapshotRevision) return@launch
                        val missingLinkedAccounts = linkedAccountIds.toSet() - existingAccountIds
                        val records = documents.map { doc ->
                        val playerId = doc.getString("playerId")?.trim().orEmpty()
                        // New aggregate rows carry the linked account ID. Older rows
                        // used team-scoped IDs (`teamId::userId`), so use the shared
                        // user ID suffix as a backward-compatible identity key.
                        val id = doc.getString("canonicalPlayerId")?.trim()
                            ?.takeIf(String::isNotBlank)
                            ?: playerId.substringAfterLast("::").trim()
                        val value = when (selectedSkill) {
                            "Bowling" -> doc.getLong("bowlingPoints") ?: 0L
                            "All-rounder" -> doc.getLong("allRounderPoints") ?: 0L
                            else -> doc.getLong("battingPoints") ?: 0L
                        }
                        val currentProfileName = publicProfiles[id]?.getString("name")
                            ?.trim()
                            ?.takeIf(String::isNotBlank)
                        val name = currentProfileName ?: doc.getString("name").orEmpty().ifBlank { playerId }
                        val stats = when (selectedSkill) {
                            "Bowling" -> "W ${doc.getLong("wickets") ?: 0} • ECO ${rate((doc.getLong("runsConceded") ?: 0L).toDouble() * 6 / (doc.getLong("legalBalls") ?: 0L).coerceAtLeast(1))} • AVG ${rate((doc.getLong("runsConceded") ?: 0L).toDouble() / (doc.getLong("wickets") ?: 0L).coerceAtLeast(1))}"
                            "All-rounder" -> "BAT ${doc.getLong("battingPoints") ?: 0} • BOWL ${doc.getLong("bowlingPoints") ?: 0} • FIELD ${doc.getLong("fieldingPoints") ?: 0}"
                            else -> "RUNS ${doc.getLong("runs") ?: 0} • AVG ${rate((doc.getLong("runs") ?: 0L).toDouble() / (doc.getLong("outs") ?: 0L).coerceAtLeast(1))} • SR ${rate((doc.getLong("runs") ?: 0L).toDouble() * 100 / (doc.getLong("balls") ?: 0L).coerceAtLeast(1))}"
                        }
                        val displayStats = if (selectedSkill == "All-rounder") stats else "$stats • FIELD ${doc.getLong("fieldingPoints") ?: 0}"
                        Triple(value, id, PlayerRank(0, Trend(TrendType.SAME), name.initials(), name,
                            "", doc.getString("teamShortName").orEmpty().ifBlank { doc.getString("teamName").orEmpty() },
                            value.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), CyanAccent,
                            doc.getString("profilePhotoUrl").orEmpty(), displayStats, id))
                        }
                            .filter { it.second.isNotBlank() }
                            .filterNot { row -> row.second in missingLinkedAccounts }
                            .sortedWith(compareByDescending<Triple<Long, String, PlayerRank>> { it.first }
                                .thenBy { it.third.name.lowercase(Locale.ROOT) }
                                .thenBy { it.second })
                            // Multiple aggregate documents can reference one player. Keep that
                            // player's highest-scoring entry so they appear only once.
                            .distinctBy { it.second }
                        playerRows.addAll(records.mapIndexed { index, row -> row.third.copy(rank = index + 1) })
                    }
                }
            registrations += registration
        } else {
            val registration = firestore.collection("leaderboards").document("teamAggregates").collection("entries")
                .whereEqualTo("sport", "CRICKET")
                .addSnapshotListener { snapshot, error ->
                    teamRows.clear()
                    if (error != null) {
                        leaderboardError = "${error.code}: ${error.localizedMessage ?: "Unable to load team leaderboard"} (leaderboards/teamAggregates/entries)"
                        return@addSnapshotListener
                    }
                    leaderboardError = null
                    val documents = snapshot?.documents.orEmpty().filter { doc ->
                        doc.getString("ballType") == ballKey &&
                            doc.getString("format") == formatKey &&
                            doc.getString("tournamentId").orEmpty() == activeTournamentId
                    }
                    val revision = ++teamSnapshotRevision
                    leaderboardScope.launch {
                        val records = documents.map { doc ->
                        val id = doc.getString("teamId").orEmpty().ifBlank { doc.id }
                        val points = doc.getLong("points") ?: 0L
                        val wins = doc.getLong("wins") ?: 0L
                        val nrr = doc.getDouble("netRunRate") ?: 0.0
                        val stats = "M ${doc.getLong("matchesPlayed") ?: 0} • W $wins • L ${doc.getLong("losses") ?: 0} • T ${doc.getLong("ties") ?: 0}"
                        val logoUrl = listOf("logoUrl", "teamLogoUrl", "photoUrl")
                            .firstNotNullOfOrNull { key -> doc.getString(key)?.takeIf(String::isNotBlank) }.orEmpty()
                        Triple(points, wins, ClubRank(0, Trend(TrendType.SAME), doc.getString("shortName").orEmpty().ifBlank { id.take(3) },
                            doc.getString("name").orEmpty(), points.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), logoUrl, "$stats • NRR ${rate(nrr)}", nrr, id) to id)
                        }.sortedWith(compareByDescending<Triple<Long, Long, Pair<ClubRank, String>>> { it.first }
                            .thenByDescending { it.second }
                            .thenByDescending { if (selectedTournamentId.isNotBlank() && formatKey != "TEST") it.third.first.tieBreakValue else 0.0 }
                            .thenBy { it.third.second })
                        val missingProfileIds = records.mapNotNull { row ->
                            row.third.first.teamId.takeIf {
                                it.isNotBlank() && (row.third.first.logoUrl.isBlank() || row.third.first.fullName.isBlank())
                            }
                        }.distinct()
                        val teamProfiles = runCatching {
                            missingProfileIds.chunked(30).flatMap { ids ->
                                firestore.collection("teams").whereIn(FieldPath.documentId(), ids).get().await().documents
                            }.associateBy { it.id }
                        }.getOrDefault(emptyMap())
                        if (!effectActive || revision != teamSnapshotRevision) return@launch
                        teamRows.addAll(records.mapIndexed { index, row ->
                            val rank = row.third.first
                            val profile = teamProfiles[rank.teamId]
                            val logo = rank.logoUrl.ifBlank {
                                listOf("logoUrl", "teamLogoUrl", "photoUrl")
                                    .firstNotNullOfOrNull { key -> profile?.getString(key)?.takeIf(String::isNotBlank) }.orEmpty()
                            }
                            val name = rank.fullName.ifBlank {
                                profile?.getString("teamName")?.takeIf(String::isNotBlank)
                                    ?: profile?.getString("name")?.takeIf(String::isNotBlank)
                                    ?: rank.teamId
                            }
                            rank.copy(rank = index + 1, fullName = name, logoUrl = logo)
                        })
                    }
                }
            registrations += registration
        }
        onDispose {
            effectActive = false
            registrations.forEach { it.remove() }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .drawBehind {
                drawRect(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0A151B), ScreenBg, Color(0xFF020609))
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
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Top Header Bar
            SportsXtremeAppBar(
                onMenuClick = onMenuClick,
                showSearch = true,
                onSearchClick = { isSearchExpanded = true },
                searchExpanded = isSearchExpanded,
                searchQuery = searchQuery,
                searchPlaceholder = if (selectedTab == LeaderboardTab.PLAYER) "Search player name" else "Search team name",
                onSearchQueryChange = { searchQuery = it },
                onSearchClose = {
                    searchQuery = ""
                    isSearchExpanded = false
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().pointerInput(selectedTab, isSearchExpanded) {
                    var horizontalDrag = 0f
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            if (!isSearchExpanded) horizontalDrag += dragAmount
                        },
                        onDragEnd = {
                            if (!isSearchExpanded && horizontalDrag < -72f) selectedTab = LeaderboardTab.TEAMS
                            if (!isSearchExpanded && horizontalDrag > 72f) selectedTab = LeaderboardTab.PLAYER
                            horizontalDrag = 0f
                        },
                        onDragCancel = { horizontalDrag = 0f }
                    )
                },
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 114.dp)
            ) {
                // 2. Title & Subtitle
                item {
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Text(
                            text = "Leaderboard",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Real-Time performance ranking",
                            color = MutedText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 3. Navigation Tabs (Player / Teams)
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    TabsRow(
                        selectedTab = selectedTab,
                        onTabSelected = { tab ->
                            selectedTab = tab
                            // Close dropdowns when changing tab
                            isSkillDropdownOpen = false
                            isTypeDropdownOpen = false
                            isFormatDropdownOpen = false
                            isTournamentDropdownOpen = false
                        }
                    )
                }

                // 4. Filters Row & Dropdown Menus (Only for Player & Teams tabs)
                if (selectedTab == LeaderboardTab.PLAYER || selectedTab == LeaderboardTab.TEAMS) {
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        FilterSection(
                            showSkill = selectedTab == LeaderboardTab.PLAYER,
                            selectedSkill = selectedSkill,
                            selectedTournament = selectedTournamentName,
                            showTournament = selectedTab != LeaderboardTab.PLAYER,
                            selectedType = selectedType,
                            selectedFormat = selectedFormat,
                            isSkillDropdownOpen = isSkillDropdownOpen,
                            isTypeDropdownOpen = isTypeDropdownOpen,
                            isFormatDropdownOpen = isFormatDropdownOpen,
                            isTournamentDropdownOpen = isTournamentDropdownOpen,
                            skills = skills,
                            tournaments = tournamentOptions.map { it.second },
                            types = types,
                            formats = formats,
                            onToggleSkill = {
                                isSkillDropdownOpen = !isSkillDropdownOpen
                                isTypeDropdownOpen = false
                                isFormatDropdownOpen = false
                                isTournamentDropdownOpen = false
                            },
                            onToggleType = {
                                isTypeDropdownOpen = !isTypeDropdownOpen
                                isSkillDropdownOpen = false
                                isFormatDropdownOpen = false
                                isTournamentDropdownOpen = false
                            },
                            onToggleFormat = {
                                isFormatDropdownOpen = !isFormatDropdownOpen
                                isSkillDropdownOpen = false
                                isTypeDropdownOpen = false
                                isTournamentDropdownOpen = false
                            },
                            onToggleTournament = {
                                isTournamentDropdownOpen = !isTournamentDropdownOpen
                                isSkillDropdownOpen = false
                                isTypeDropdownOpen = false
                                isFormatDropdownOpen = false
                            },
                            onSelectSkill = {
                                selectedSkill = it
                                isSkillDropdownOpen = false
                            },
                            onSelectType = {
                                selectedType = it
                                isTypeDropdownOpen = false
                            },
                            onSelectFormat = {
                                selectedFormat = it
                                isFormatDropdownOpen = false
                            },
                            onSelectTournament = { name ->
                                selectedTournamentName = name
                                selectedTournamentId = tournamentOptions.firstOrNull { it.second == name }?.first.orEmpty()
                                isTournamentDropdownOpen = false
                            }
                        )
                    }
                }

                // 5. Table Header
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    TableHeader(tab = selectedTab)
                }

                leaderboardError?.let { error ->
                    item {
                        Text(error, color = Color(0xFFFF7A7A), fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), textAlign = TextAlign.Center)
                    }
                }

                // 6. Ranking Rows
                when (selectedTab) {
                    LeaderboardTab.PLAYER -> {
                        items(playerRows.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }) { player ->
                            PlayerRankingRow(player = player, onClick = {
                                if (player.userId.isNotBlank()) {
                                    context.startActivity(
                                        Intent(context, ProfileActivity::class.java)
                                            .putExtra(ProfileActivity.EXTRA_USER_ID, player.userId)
                                            .putExtra(ProfileActivity.EXTRA_DISPLAY_NAME, player.name)
                                            .putExtra(ProfileActivity.EXTRA_PROFILE_PHOTO_URL, player.photoUrl)
                                    )
                                }
                            })
                        }
                    }
                    LeaderboardTab.TEAMS -> {
                        items(teamRows.filter {
                            it.fullName.contains(searchQuery.trim(), ignoreCase = true) ||
                                it.code.contains(searchQuery.trim(), ignoreCase = true)
                        }) { team ->
                            ClubRankingRow(club = team, onClick = {
                                if (team.teamId.isNotBlank()) {
                                    context.startActivity(
                                        Intent(context, TeamProfileActivity::class.java)
                                            .putExtra(TeamProfileActivity.EXTRA_TEAM_ID, team.teamId)
                                    )
                                }
                            })
                        }
                    }
                }

                val hasSearchResults = when (selectedTab) {
                    LeaderboardTab.PLAYER -> playerRows.any { it.name.contains(searchQuery.trim(), ignoreCase = true) }
                    else -> teamRows.any {
                        it.fullName.contains(searchQuery.trim(), ignoreCase = true) ||
                            it.code.contains(searchQuery.trim(), ignoreCase = true)
                    }
                }
                if (leaderboardError == null && !hasSearchResults) {
                    item {
                        Text(
                            if (searchQuery.isBlank()) "No finalized match rankings for these filters yet."
                            else if (selectedTab == LeaderboardTab.PLAYER) "No players match this search."
                            else "No teams match this search.",
                            color = MutedText, fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), textAlign = TextAlign.Center)
                    }
                }

                // 34.dp bottom padding item
                item {
                    Spacer(modifier = Modifier.height(34.dp))
                }
            }
        }
    }
}

// ==========================================
// COMPOSABLE COMPONENTS
// ==========================================

@Composable
private fun HeaderBar(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hamburger Menu
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onMenuClick),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(20.dp)) {
                val stroke = 2.dp.toPx()
                val color = Color.White
                drawLine(color, Offset(0f, size.height * 0.2f), Offset(size.width, size.height * 0.2f), stroke, StrokeCap.Round)
                drawLine(color, Offset(0f, size.height * 0.5f), Offset(size.width, size.height * 0.5f), stroke, StrokeCap.Round)
                drawLine(color, Offset(0f, size.height * 0.8f), Offset(size.width, size.height * 0.8f), stroke, StrokeCap.Round)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // SportsXtreme Logo with @drawable/splash_brand_tight_logo
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.splash_brand_tight_logo),
                contentDescription = "SportsXtreme Brand Logo",
                modifier = Modifier
                    .size(35.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Sports",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Xtreme",
                color = CyanAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Search Icon
        Canvas(
            modifier = Modifier
                .size(20.dp)
                .clickable { }
        ) {
            val stroke = 2.dp.toPx()
            drawCircle(Color.White, radius = size.width * 0.35f, center = Offset(size.width * 0.4f, size.height * 0.4f), style = Stroke(stroke))
            drawLine(Color.White, Offset(size.width * 0.65f, size.height * 0.65f), Offset(size.width * 0.95f, size.height * 0.95f), stroke, StrokeCap.Round)
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Notification Icon using @drawable/ghanti
        Image(
            painter = painterResource(id = R.drawable.ghanti),
            contentDescription = "Notifications",
            modifier = Modifier
                .size(30.dp)
                .clickable { }
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Message Icon
        Canvas(
            modifier = Modifier
                .size(20.dp)
                .clickable { }
        ) {
            val stroke = 1.8.dp.toPx()
            val rectPath = Path().apply {
                moveTo(size.width * 0.1f, size.height * 0.2f)
                lineTo(size.width * 0.9f, size.height * 0.2f)
                lineTo(size.width * 0.9f, size.height * 0.7f)
                lineTo(size.width * 0.4f, size.height * 0.7f)
                lineTo(size.width * 0.2f, size.height * 0.9f)
                lineTo(size.width * 0.2f, size.height * 0.7f)
                lineTo(size.width * 0.1f, size.height * 0.7f)
                close()
            }
            drawPath(rectPath, Color.White, style = Stroke(stroke, cap = StrokeCap.Round))
            drawLine(Color.White, Offset(size.width * 0.3f, size.height * 0.4f), Offset(size.width * 0.7f, size.height * 0.4f), stroke, StrokeCap.Round)
            drawLine(Color.White, Offset(size.width * 0.3f, size.height * 0.55f), Offset(size.width * 0.55f, size.height * 0.55f), stroke, StrokeCap.Round)
        }
    }
}

@Composable
private fun TabsRow(
    selectedTab: LeaderboardTab,
    onTabSelected: (LeaderboardTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LeaderboardTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            val tabName = when (tab) {
                LeaderboardTab.PLAYER -> "Player"
                LeaderboardTab.TEAMS -> "Teams"
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = tabName,
                    color = if (isSelected) GreenAccent else MutedText,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(2.5.dp)
                            .background(GreenAccent, shape = RoundedCornerShape(2.dp))
                    )
                } else {
                    Spacer(modifier = Modifier.height(2.5.dp))
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    showSkill: Boolean,
    selectedSkill: String,
    selectedTournament: String,
    showTournament: Boolean,
    selectedType: String,
    selectedFormat: String,
    isSkillDropdownOpen: Boolean,
    isTypeDropdownOpen: Boolean,
    isFormatDropdownOpen: Boolean,
    isTournamentDropdownOpen: Boolean,
    skills: List<String>,
    tournaments: List<String>,
    types: List<String>,
    formats: List<String>,
    onToggleSkill: () -> Unit,
    onToggleType: () -> Unit,
    onToggleFormat: () -> Unit,
    onToggleTournament: () -> Unit,
    onSelectSkill: (String) -> Unit,
    onSelectType: (String) -> Unit,
    onSelectFormat: (String) -> Unit,
    onSelectTournament: (String) -> Unit
) {
    Column {
        // Pills Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showSkill) {
                FilterPill(
                    iconType = FilterIconType.PERSON,
                    label = selectedSkill,
                    isOpen = isSkillDropdownOpen,
                    onClick = onToggleSkill
                )
            }

            // Pill 2: Ball type
            FilterPill(
                iconType = FilterIconType.BALL,
                label = selectedType,
                isOpen = isTypeDropdownOpen,
                onClick = onToggleType
            )

            // Pill 3: Match format
            FilterPill(
                iconType = FilterIconType.TROPHY,
                label = selectedFormat,
                isOpen = isFormatDropdownOpen,
                onClick = onToggleFormat
            )
            if (showTournament) {
                FilterPill(
                    iconType = FilterIconType.MORE,
                    label = selectedTournament,
                    isOpen = isTournamentDropdownOpen,
                    onClick = onToggleTournament
                )
            }
        }

        // Expanded Dropdown Overlay (Image 2)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Skill Dropdown Box
            if (showSkill && isSkillDropdownOpen) {
                DropdownMenuCard(
                    items = skills.map { skill ->
                        DropdownItemData(
                            label = skill,
                            iconType = when (skill) {
                                "Batting" -> FilterIconType.PERSON
                                "Bowling" -> FilterIconType.BALL
                                else -> FilterIconType.STAR
                            },
                            isSelected = skill == selectedSkill
                        )
                    },
                    onItemClick = { onSelectSkill(it.label) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Type Dropdown Box
            if (isTypeDropdownOpen) {
                DropdownMenuCard(
                    items = types.map { type ->
                        DropdownItemData(
                            label = type,
                            iconType = when (type) {
                                "Leather Ball", "Tennis Ball" -> FilterIconType.BALL
                                else -> FilterIconType.MORE
                            },
                            isSelected = type == selectedType
                        )
                    },
                    onItemClick = { onSelectType(it.label) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Format Dropdown Box
            if (isFormatDropdownOpen) {
                DropdownMenuCard(
                    items = formats.map { format ->
                        DropdownItemData(
                            label = format,
                            iconType = FilterIconType.TROPHY,
                            isSelected = format == selectedFormat,
                        )
                    },
                    onItemClick = { onSelectFormat(it.label) },
                    modifier = Modifier.weight(1f)
                )
            }
            if (showTournament && isTournamentDropdownOpen) {
                DropdownMenuCard(
                    items = tournaments.map { DropdownItemData(it, FilterIconType.TROPHY, it == selectedTournament) },
                    onItemClick = { onSelectTournament(it.label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

enum class FilterIconType {
    PERSON, BALL, TROPHY, STAR, MORE
}

@Composable
private fun FilterPill(
    iconType: FilterIconType,
    label: String,
    isOpen: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0xFF091217))
            .border(1.2.dp, PillBorder, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Left Icon
            FilterIcon(iconType = iconType, tint = GreenAccent, modifier = Modifier.size(16.dp))

            Spacer(modifier = Modifier.width(8.dp))

            // Label
            Text(
                text = label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Down Chevron Arrow
            Canvas(modifier = Modifier.size(10.dp)) {
                val stroke = 1.8.dp.toPx()
                val path = Path().apply {
                    if (isOpen) {
                        moveTo(0f, size.height * 0.7f)
                        lineTo(size.width * 0.5f, size.height * 0.2f)
                        lineTo(size.width, size.height * 0.7f)
                    } else {
                        moveTo(0f, size.height * 0.3f)
                        lineTo(size.width * 0.5f, size.height * 0.8f)
                        lineTo(size.width, size.height * 0.3f)
                    }
                }
                drawPath(path, Color.White, style = Stroke(stroke, cap = StrokeCap.Round))
            }
        }
    }
}

private data class DropdownItemData(
    val label: String,
    val iconType: FilterIconType,
    val isSelected: Boolean
)

@Composable
private fun DropdownMenuCard(
    items: List<DropdownItemData>,
    onItemClick: (DropdownItemData) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF09141B))
            .border(1.dp, PillBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 6.dp)
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterIcon(
                    iconType = item.iconType,
                    tint = if (item.isSelected) GreenAccent else MutedText,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = item.label,
                    color = if (item.isSelected) Color.White else MutedText,
                    fontSize = 13.sp,
                    fontWeight = if (item.isSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                if (item.isSelected) {
                    // Green Checkmark
                    Canvas(modifier = Modifier.size(14.dp)) {
                        val path = Path().apply {
                            moveTo(size.width * 0.15f, size.height * 0.5f)
                            lineTo(size.width * 0.45f, size.height * 0.8f)
                            lineTo(size.width * 0.9f, size.height * 0.2f)
                        }
                        drawPath(path, GreenAccent, style = Stroke(2.2.dp.toPx(), cap = StrokeCap.Round))
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterIcon(
    iconType: FilterIconType,
    tint: Color,
    modifier: Modifier = Modifier
) {
    when (iconType) {
        FilterIconType.BALL -> {
            Image(
                painter = painterResource(id = R.drawable.ball),
                contentDescription = "Ball Icon",
                modifier = modifier
            )
        }
        FilterIconType.TROPHY -> {
            Image(
                painter = painterResource(id = R.drawable.white_trophy),
                contentDescription = "Limited Over Icon",
                modifier = modifier,
                colorFilter = ColorFilter.tint(
                    Lime
                )
            )
        }
        FilterIconType.PERSON -> {
            Canvas(modifier = modifier) {
                drawCircle(tint, radius = size.width * 0.25f, center = Offset(size.width * 0.5f, size.height * 0.3f), style = Stroke(1.5.dp.toPx()))
                val arcPath = Path().apply {
                    moveTo(size.width * 0.15f, size.height * 0.85f)
                    cubicTo(size.width * 0.2f, size.height * 0.55f, size.width * 0.8f, size.height * 0.55f, size.width * 0.85f, size.height * 0.85f)
                }
                drawPath(arcPath, tint, style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round))
            }
        }
        FilterIconType.STAR -> {
            Canvas(modifier = modifier) {
                val star = Path().apply {
                    moveTo(size.width * 0.5f, 0f)
                    lineTo(size.width * 0.65f, size.height * 0.35f)
                    lineTo(size.width, size.height * 0.35f)
                    lineTo(size.width * 0.72f, size.height * 0.6f)
                    lineTo(size.width * 0.82f, size.height)
                    lineTo(size.width * 0.5f, size.height * 0.75f)
                    lineTo(size.width * 0.18f, size.height)
                    lineTo(size.width * 0.18f, size.height)
                    lineTo(size.width * 0.28f, size.height * 0.6f)
                    lineTo(0f, size.height * 0.35f)
                    lineTo(size.width * 0.35f, size.height * 0.35f)
                    close()
                }
                drawPath(star, tint, style = Stroke(1.2.dp.toPx()))
            }
        }
        FilterIconType.MORE -> {
            Canvas(modifier = modifier) {
                drawCircle(tint, radius = 2.dp.toPx(), center = Offset(size.width * 0.2f, size.height * 0.5f))
                drawCircle(tint, radius = 2.dp.toPx(), center = Offset(size.width * 0.5f, size.height * 0.5f))
                drawCircle(tint, radius = 2.dp.toPx(), center = Offset(size.width * 0.8f, size.height * 0.5f))
            }
        }
    }
}

@Composable
private fun TableHeader(tab: LeaderboardTab) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(Color(0xFF070D12))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Rank",
            color = MutedText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(if (tab == LeaderboardTab.PLAYER) 36.dp else 48.dp)
        )

        val col2Text = when (tab) {
            LeaderboardTab.PLAYER -> "PLAYER"
            LeaderboardTab.TEAMS -> "TEAM"
        }

        // Keep the heading aligned with the player name / team row content.
        Spacer(modifier = Modifier.width(if (tab == LeaderboardTab.PLAYER) 47.dp else 50.dp))

        Text(
            text = if (tab == LeaderboardTab.PLAYER) "PLAYER / TEAM" else col2Text,
            color = MutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "POINTS",
            color = MutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(60.dp)
        )
    }
}

@Composable
private fun PlayerRankingRow(player: PlayerRank, onClick: () -> Unit) {
    val isTopThree = player.rank in 1..3
    val rankColor = if (isTopThree) GoldAccent else Color.White
    val rowShape = RoundedCornerShape(10.dp)
    val goldSweepProgress = if (isTopThree) {
        val transition = rememberInfiniteTransition(label = "podiumGoldSweep")
        val progress by transition.animateFloat(
            initialValue = -0.25f,
            targetValue = 1.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "goldSweepProgress"
        )
        progress
    } else 0f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clickable(onClick = onClick)
            .shadow(
                elevation = if (isTopThree) 5.dp else 0.dp,
                shape = rowShape,
                ambientColor = if (isTopThree) GoldAccent.copy(alpha = 0.08f) else Color.Transparent,
                spotColor = if (isTopThree) GoldAccent.copy(alpha = 0.16f) else Color.Transparent,
                clip = false
            )
            .background(if (isTopThree) Color(0xFF100F0A) else CardBg, rowShape)
            .drawBehind {
                if (isTopThree) {
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.Transparent, GoldAccent.copy(alpha = 0.12f), Color.Transparent),
                            start = Offset(size.width * (goldSweepProgress - 0.3f), -size.height * 0.3f),
                            end = Offset(size.width * (goldSweepProgress + 0.1f), size.height * 1.3f)
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
                    )
                }
            }
            .border(0.5.dp, TableBorder, rowShape)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank & Trend
        Column(
            modifier = Modifier.width(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape)
                    .background(if (isTopThree) rankColor.copy(alpha = 0.14f) else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.rank.toString(),
                    color = rankColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            TrendBadge(trend = player.trend)
        }

        PlayerLeaderboardAvatar(player = player)

        Spacer(modifier = Modifier.width(9.dp))

        // Player Name & Country
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = player.name,
                color = rankColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = player.statistics,
                    color = MutedText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (player.teamCode.isNotBlank()) {
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = player.teamCode,
                        color = CyanAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .width(56.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(CyanAccent.copy(alpha = 0.10f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Points
        Text(
            text = player.points.toString(),
            color = rankColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(54.dp)
        )
    }
}

@Composable
private fun PlayerLeaderboardAvatar(player: PlayerRank) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color(0xFF0F1B22))
            .border(1.5.dp, player.avatarBorderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (player.photoUrl.isNotBlank()) {
            AsyncImage(
                model = player.photoUrl,
                contentDescription = "${player.name} profile photo",
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        } else {
            Text(
                text = player.initials,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ClubRankingRow(club: ClubRank, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clickable(onClick = onClick)
            .background(CardBg)
            .border(0.5.dp, TableBorder)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank & Trend
        Column(
            modifier = Modifier.width(48.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = club.rank.toString(),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            TrendBadge(trend = club.trend)
        }

        // Club Logo
        TeamLogoView(teamCode = club.code, logoUrl = club.logoUrl, modifier = Modifier.size(38.dp))

        Spacer(modifier = Modifier.width(12.dp))

        // Club Title & Subtitle
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = club.fullName,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = club.statistics,
                color = MutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Points
        Text(
            text = club.points.toString(),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(60.dp)
        )
    }
}

@Composable
private fun TrendBadge(trend: Trend) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        when (trend.type) {
            TrendType.UP -> {
                Canvas(modifier = Modifier.size(8.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.5f, 0f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(path, TrendUpColor)
                }
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = trend.amount.toString(),
                    color = TrendUpColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            TrendType.DOWN -> {
                Canvas(modifier = Modifier.size(8.dp)) {
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width * 0.5f, size.height)
                        close()
                    }
                    drawPath(path, TrendDownColor)
                }
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = trend.amount.toString(),
                    color = TrendDownColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            TrendType.SAME -> {
                Text(
                    text = "-",
                    color = MutedText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TeamLogoView(teamCode: String, logoUrl: String = "", modifier: Modifier = Modifier) {
    if (logoUrl.isNotBlank()) {
        AsyncImage(model = logoUrl, contentDescription = "$teamCode team logo", modifier = modifier.clip(CircleShape))
    } else when (teamCode.uppercase()) {
        "RCB" -> {
            Image(
                painter = painterResource(id = R.drawable.rcb),
                contentDescription = "RCB Logo",
                modifier = modifier
            )
        }
        "GT" -> {
            Image(
                painter = painterResource(id = R.drawable.gujarat),
                contentDescription = "GT Logo",
                modifier = modifier
            )
        }
        "DC" -> {
            Image(
                painter = painterResource(id = R.drawable.delhi),
                contentDescription = "DC Logo",
                modifier = modifier
            )
        }
        else -> {
            // Render custom vector emblems for other IPL teams (MI, RR, SRH, LSG, CSK, KKR, PBKS)
            val colors = when (teamCode.uppercase()) {
                "MI" -> listOf(Color(0xFF004BA0), Color(0xFFFFD700))
                "RR" -> listOf(Color(0xFFEA1A85), Color(0xFF002B66))
                "SRH" -> listOf(Color(0xFFF26522), Color(0xFF111111))
                "LSG" -> listOf(Color(0xFF00A8E8), Color(0xFFE31E24))
                "CSK" -> listOf(Color(0xFFFCCA06), Color(0xFF00529B))
                "KKR" -> listOf(Color(0xFF3A225D), Color(0xFFF2C94C))
                "PBKS" -> listOf(Color(0xFFED1B24), Color(0xFFA6A6A6))
                else -> listOf(Color(0xFF1E303A), GreenAccent)
            }

            Box(
                modifier = modifier
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = colors
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = teamCode.take(3),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun String.initials(): String = trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").ifBlank { "?" }
private fun rate(value: Double): String = if (!value.isFinite()) "0.00" else String.format(Locale.US, "%.2f", value)
