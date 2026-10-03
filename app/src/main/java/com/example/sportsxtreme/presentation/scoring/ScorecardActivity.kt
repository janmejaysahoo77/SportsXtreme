package com.example.sportsxtreme.presentation.scoring

import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.tournament.*
import com.example.sportsxtreme.presentation.components.*
import com.example.sportsxtreme.presentation.auth.*
import com.example.sportsxtreme.presentation.scoring.*
import com.example.sportsxtreme.presentation.match.*
import com.example.sportsxtreme.presentation.media.*
import com.example.sportsxtreme.presentation.home.*
import com.example.sportsxtreme.presentation.team.*
import com.example.sportsxtreme.presentation.profile.*
import com.example.sportsxtreme.presentation.store.*
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import java.util.Locale

@AndroidEntryPoint
class ScorecardActivity : ComponentActivity() {

    @Inject lateinit var firestore: FirebaseFirestore
    private val functions by lazy { FirebaseFunctions.getInstance() }
    private var matchListener: ListenerRegistration? = null
    private var deliveriesListener: ListenerRegistration? = null

    companion object {
        const val EXTRA_LEAGUE = "scorecard.extra.LEAGUE"
        const val EXTRA_ROUND = "scorecard.extra.ROUND"
        const val EXTRA_MATCH_ID = "scorecard.extra.MATCH_ID"
        const val EXTRA_LEFT_NAME = "scorecard.extra.LEFT_NAME"
        const val EXTRA_LEFT_SCORE = "scorecard.extra.LEFT_SCORE"
        const val EXTRA_LEFT_OVERS = "scorecard.extra.LEFT_OVERS"
        const val EXTRA_RIGHT_NAME = "scorecard.extra.RIGHT_NAME"
        const val EXTRA_RIGHT_SCORE = "scorecard.extra.RIGHT_SCORE"
        const val EXTRA_RIGHT_OVERS = "scorecard.extra.RIGHT_OVERS"
        const val EXTRA_TARGET = "scorecard.extra.TARGET"
        const val EXTRA_RRR = "scorecard.extra.RRR"
        const val EXTRA_WIN = "scorecard.extra.WIN"
        const val EXTRA_NOTE = "scorecard.extra.NOTE"
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)

        val match = mutableStateOf(readMatch())
        val resolvablePlayerIds = mutableMapOf<String, String>()
        val playerProfilePhotoUrls = mutableMapOf<String, String>()
        val rosterUserIdsByName = mutableMapOf<String, String>()
        val rosterPhotoUrlsByName = mutableMapOf<String, String>()
        setContent {
            ScorecardScreen(
                match = match.value,
                onBack = { finish() },
                onShare = { shareScorecard(match.value) },
                onOpenPlayer = { playerId, displayName ->
                    val normalizedName = displayName.normalizePlayerProfileName()
                    val userId = resolvablePlayerIds[playerId]
                        ?: rosterUserIdsByName[normalizedName]
                        ?: playerId.substringAfter("::", missingDelimiterValue = playerId)
                    startActivity(Intent(this, ProfileActivity::class.java).apply {
                        putExtra(ProfileActivity.EXTRA_USER_ID, userId)
                        putExtra(ProfileActivity.EXTRA_DISPLAY_NAME, displayName)
                        putExtra(
                            ProfileActivity.EXTRA_PROFILE_PHOTO_URL,
                            playerProfilePhotoUrls[playerId] ?: rosterPhotoUrlsByName[normalizedName]
                        )
                    })
                }
            )
        }

        val matchId = resolveMatchId(intent)
        if (matchId.isNotBlank()) {
            if (savedInstanceState == null) {
                firestore.collection("matches").document(matchId)
                    .update("scorecardViews", FieldValue.increment(1))
                    .addOnFailureListener { error ->
                        Log.w("ScorecardActivity", "Could not increment scorecard views", error)
                    }
            }
            var deliveryDocuments = emptyList<DocumentSnapshot>()
            val playerNames = mutableMapOf<String, String>()
            val playerNameLookups = mutableSetOf<String>()
            val pendingPlayerNameLookups = mutableSetOf<String>()
            fun refreshPartnership() {
                val current = match.value.copy(
                    currentBatters = match.value.currentBatters.map { batter ->
                        batter.copy(name = playerNames.resolvePlayerName(batter.id, batter.name))
                    },
                    currentBowlerName = playerNames.resolvePlayerName(
                        match.value.currentBowlerId,
                        match.value.currentBowlerName
                    )
                )
                val deliveries = deliveryDocuments.activeInningsDeliveries(current)
                val scorecardDeliveries = deliveryDocuments.activeMatchDeliveries()
                match.value = current.copy(
                    currentPartnership = deliveries.calculateCurrentPartnership(current),
                    lastWicket = deliveries.lastWicketSummary(),
                    lastFiveOvers = deliveries.lastFiveOversSummary(),
                    currentBowling = deliveries.currentBowlingFigures(current),
                    lastSixBalls = deliveries.lastSixBallOutcomes(),
                    scorecardBatting = scorecardDeliveries.scorecardBattingRows(current, playerNames),
                    scorecardBowling = scorecardDeliveries.scorecardBowlingRows(current, playerNames),
                    battedTeamIds = current.battedTeamIds + scorecardDeliveries.mapNotNull { it["battingTeamId"] as? String }
                )
            }
            deliveriesListener = firestore.collection("matches").document(matchId)
                .collection("deliveries")
                .addSnapshotListener { snapshot, _ ->
                    deliveryDocuments = snapshot?.documents.orEmpty()
                    refreshPartnership()
                }
            var tournamentLookupStartedFor: String? = null
            matchListener = firestore.collection("matches").document(matchId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                    match.value = snapshot.toMatchDetail(match.value)
                    val teamIds = listOf(match.value.leftTeamId, match.value.rightTeamId)
                        .filter(String::isNotBlank)
                    val lookupsToStart = teamIds.filter { teamId ->
                        playerNameLookups.add(teamId).also { isNew ->
                            if (isNew) pendingPlayerNameLookups.add(teamId)
                        }
                    }
                    match.value = match.value.copy(playerNamesLoading = pendingPlayerNameLookups.isNotEmpty())
                    refreshPartnership()

                    lookupsToStart.forEach { teamId ->
                        lifecycleScope.launch {
                            val members = runCatching {
                                val response = functions.getHttpsCallable("getTeamMemberProfiles")
                                    .call(mapOf("teamId" to teamId)).await().data as? Map<*, *>
                                (response?.get("members") as? List<*>).orEmpty()
                                    .mapNotNull { it as? Map<*, *> }
                            }.onFailure { error ->
                                Log.w("ScorecardActivity", "Could not load roster profiles for $teamId", error)
                            }.getOrDefault(emptyList())
                            if (!isFinishing && !isDestroyed) {
                                members.mapNotNull { it["userId"] as? String }
                                    .forEach { userId ->
                                        resolvablePlayerIds[userId] = userId
                                        resolvablePlayerIds["$teamId::$userId"] = userId
                                    }
                                members.forEach { member ->
                                    val userId = member["userId"] as? String ?: return@forEach
                                    val profilePhotoUrl = (member["profilePhotoUrl"] as? String)
                                        ?.trim()
                                        ?.takeIf(String::isNotBlank) ?: return@forEach
                                    playerProfilePhotoUrls[userId] = profilePhotoUrl
                                    playerProfilePhotoUrls["$teamId::$userId"] = profilePhotoUrl
                                    val name = (member["displayName"] as? String).normalizePlayerProfileName()
                                    if (name.isNotBlank()) rosterPhotoUrlsByName[name] = profilePhotoUrl
                                }
                                val names = members.mapNotNull { member ->
                                    val userId = member["userId"] as? String ?: return@mapNotNull null
                                    val name = (member["displayName"] as? String)?.trim()
                                        ?.takeIf(String::isNotBlank) ?: return@mapNotNull null
                                    listOf(userId, "$teamId::$userId").map { it to name }
                                }.flatten().toMap()
                                if (names.isNotEmpty()) {
                                    playerNames.putAll(names)
                                }
                                members.forEach { member ->
                                    val userId = member["userId"] as? String ?: return@forEach
                                    val name = (member["displayName"] as? String).normalizePlayerProfileName()
                                    if (name.isNotBlank()) rosterUserIdsByName[name] = userId
                                }
                                pendingPlayerNameLookups.remove(teamId)
                                match.value = match.value.copy(playerNamesLoading = pendingPlayerNameLookups.isNotEmpty())
                                refreshPartnership()
                            }
                        }
                    }

                    val tournamentId = snapshot.getString("tournamentId")
                        ?.takeIf(String::isNotBlank)
                    if (tournamentId != null && tournamentLookupStartedFor != tournamentId) {
                        tournamentLookupStartedFor = tournamentId
                        lifecycleScope.launch {
                            val name = runCatching {
                                firestore.collection("tournaments").document(tournamentId)
                                    .get().await().getString("name")
                            }.getOrNull()?.takeIf(String::isNotBlank)
                            if (name != null && !isFinishing && !isDestroyed) {
                                match.value = match.value.copy(league = name)
                            }
                        }
                    }
                }
        }
    }

    override fun onDestroy() {
        matchListener?.remove()
        matchListener = null
        deliveriesListener?.remove()
        deliveriesListener = null
        super.onDestroy()
    }

    private fun shareScorecard(match: MatchDetail) {
        if (match.matchId.isBlank()) return
        val scorecardUrl = Uri.Builder()
            .scheme("https")
            .authority("sportsxtreme-95fbb.web.app")
            .appendPath("scorecard")
            .appendQueryParameter("matchId", match.matchId)
            .build()
            .toString()
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "${match.leftName} vs ${match.rightName} scorecard on SportsXtreme:\n$scorecardUrl")
        }
        startActivity(Intent.createChooser(shareIntent, "Share scorecard"))
    }

    private fun resolveMatchId(source: Intent?): String =
        source?.getStringExtra(EXTRA_MATCH_ID)?.takeIf(String::isNotBlank)
            ?: source?.data?.getQueryParameter("matchId").orEmpty()

    private fun DocumentSnapshot.toMatchDetail(previous: MatchDetail): MatchDetail {
        val liveScore = get("liveScore") as? Map<*, *>
        val teamA = get("teamA") as? Map<*, *>
        val teamB = get("teamB") as? Map<*, *>
        val teamAId = (teamA?.get("teamId") as? String)
            ?: (liveScore?.get("teamAId") as? String).orEmpty()
        val teamBId = (teamB?.get("teamId") as? String)
            ?: (liveScore?.get("teamBId") as? String).orEmpty()
        val teamAName = teamA?.displayName("teamAShortName", "teamAName", liveScore)
            ?: previous.leftName
        val teamBName = teamB?.displayName("teamBShortName", "teamBName", liveScore)
            ?: previous.rightName
        val innings = (get("innings") as? List<*>)?.filterIsInstance<Map<*, *>>().orEmpty()
        val currentBattingTeamId = (liveScore?.get("battingTeamId") as? String)
            ?: (innings.lastOrNull()?.get("battingTeamId") as? String)

        var leftScore = innings.scoreFor(teamAId) ?: getString("teamAScore") ?: previous.leftScore
        var leftOvers = innings.oversFor(teamAId) ?: getString("teamAOvers") ?: previous.leftOvers
        var rightScore = innings.scoreFor(teamBId) ?: getString("teamBScore") ?: previous.rightScore
        var rightOvers = innings.oversFor(teamBId) ?: getString("teamBOvers") ?: previous.rightOvers

        val liveRuns = liveScore?.number("score")
        val liveWickets = liveScore?.number("wickets")
        val liveOvers = liveScore?.get("overs")?.toString()?.takeIf(String::isNotBlank)
        val strikerName = liveScore?.get("strikerName") as? String
        val currentBatters = if (liveScore == null) previous.currentBatters else listOfNotNull(
            strikerName?.takeIf(String::isNotBlank)?.let {
                CurrentBatter(
                    id = liveScore?.get("strikerPlayerId") as? String,
                    name = it,
                    runs = liveScore?.number("strikerRuns") ?: 0,
                    balls = liveScore?.number("strikerBalls") ?: 0,
                    fours = liveScore?.number("strikerFours") ?: 0,
                    sixes = liveScore?.number("strikerSixes") ?: 0,
                    isStriker = true
                )
            },
            (liveScore?.get("non" +
                    "StrikerName") as? String)
                ?.takeIf(String::isNotBlank)
                ?.takeIf { it != strikerName }
                ?.let {
                    CurrentBatter(
                        id = liveScore?.get("nonStrikerPlayerId") as? String,
                        name = it,
                        runs = liveScore?.number("nonStrikerRuns") ?: 0,
                        balls = liveScore?.number("nonStrikerBalls") ?: 0,
                        fours = liveScore?.number("nonStrikerFours") ?: 0,
                        sixes = liveScore?.number("nonStrikerSixes") ?: 0,
                        isStriker = false
                    )
                }
        )
        if (liveRuns != null && currentBattingTeamId == teamAId) {
            leftScore = "$liveRuns/${liveWickets ?: 0}"
            leftOvers = liveOvers ?: leftOvers
        } else if (liveRuns != null && currentBattingTeamId == teamBId) {
            rightScore = "$liveRuns/${liveWickets ?: 0}"
            rightOvers = liveOvers ?: rightOvers
        }

        val activeInnings = innings.lastOrNull()
        val activeInningsNumber = activeInnings?.number("number")
        val targetValue = liveScore?.number("target") ?: activeInnings?.number("target")
        val isFirstInnings = when {
            activeInningsNumber != null -> activeInningsNumber <= 1
            innings.size >= 2 -> false
            else -> targetValue == null
        }
        val ballsBowled = liveOvers?.let(::ballsFromOvers)
            ?: activeInnings?.number("legalBalls")
        val scheduledOvers = (get("overs") as? Number)?.toInt()
            ?: (get("overs") as? String)?.toIntOrNull()
        val crr = liveScore?.decimal("currentRunRate")
            ?: activeInnings?.let { inning ->
                val balls = inning.number("legalBalls") ?: 0
                val runs = inning.number("score") ?: 0
                if (balls > 0) runs * 6.0 / balls else 0.0
            }
        val toss = get("toss") as? Map<*, *>
        val tossWinnerId = toss?.get("winnerTeamId") as? String
        val tossDecision = (toss?.get("decision") as? String)?.uppercase(Locale.US)
        val tossWinnerName = when (tossWinnerId) {
            teamAId -> teamAName
            teamBId -> teamBName
            else -> null
        }
        val tossText = when {
            tossWinnerName.isNullOrBlank() -> ""
            tossDecision == "BAT" -> "$tossWinnerName won the toss and elected to bat"
            tossDecision == "FIELD" -> "$tossWinnerName won the toss and elected to field"
            else -> ""
        }
        val target = if (isFirstInnings) "—" else targetValue?.toString() ?: "—"
        val requiredRunRate = if (isFirstInnings) "—" else {
            liveScore?.decimal("requiredRunRate")?.formatRate()
                ?: activeInnings?.let { inning ->
                    val totalBalls = scheduledOvers?.times(6)
                    val remainingBalls = totalBalls?.minus(inning.number("legalBalls") ?: 0) ?: 0
                    if (targetValue != null && remainingBalls > 0) {
                        ((targetValue - (liveRuns ?: inning.number("score") ?: 0)).coerceAtLeast(0) * 6.0 / remainingBalls).formatRate()
                    } else "—"
                } ?: "—"
        }
        val isFriendlyMatch = getString("matchType")?.equals("FRIENDLY", ignoreCase = true) == true

        return previous.copy(
            league = if (isFriendlyMatch) getString("title")?.takeIf(String::isNotBlank) ?: "Friendly Match"
                else getString("tournamentName")?.takeIf(String::isNotBlank)
                    ?: (liveScore?.get("tournamentName") as? String)?.takeIf(String::isNotBlank)
                    ?: previous.league,
            round = if (isFriendlyMatch) "" else (getString("roundName")
                ?: getString("selectedStage") ?: previous.round).orEmpty(),
            leftName = teamAName,
            leftTeamId = teamAId,
            leftScore = leftScore,
            leftOvers = leftOvers,
            rightName = teamBName,
            rightTeamId = teamBId,
            rightScore = rightScore,
            rightOvers = rightOvers,
            venue = getString("venue")?.takeIf(String::isNotBlank).orEmpty(),
            target = target,
            rrr = requiredRunRate,
            currentRunRate = crr?.formatRate() ?: previous.currentRunRate,
            tossInfo = tossText,
            isFirstInnings = isFirstInnings,
            battingTeamIsLeft = when (currentBattingTeamId) {
                teamAId -> true
                teamBId -> false
                else -> null
            },
            ballsBowled = ballsBowled,
            scheduledBalls = scheduledOvers?.times(6),
            currentBatters = currentBatters,
            currentInningsId = activeInnings?.get("inningsId") as? String ?: activeInnings?.get("id") as? String,
            currentBowlerId = activeInnings?.get("currentBowlerId") as? String,
            currentBowlerName = (liveScore?.get("bowlerName") as? String).orEmpty(),
            battedTeamIds = innings.mapNotNull { it["battingTeamId"] as? String }.toSet(),
            scorecardViews = getLong("scorecardViews") ?: 0L
        )
    }

    private fun Map<*, *>?.number(key: String): Int? = when (val value = this?.get(key)) {
        is Number -> value.toInt()
        is String -> value.toIntOrNull()
        else -> null
    }

    private fun Map<*, *>?.decimal(key: String): Double? = when (val value = this?.get(key)) {
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull()
        else -> null
    }

    private fun Map<*, *>?.displayName(shortNameKey: String, nameKey: String, liveScore: Map<*, *>?): String? =
        (this?.get("shortName") as? String)?.takeIf(String::isNotBlank)
            ?: (this?.get("name") as? String)?.takeIf(String::isNotBlank)
            ?: (liveScore?.get(shortNameKey) as? String)?.takeIf(String::isNotBlank)
            ?: (liveScore?.get(nameKey) as? String)?.takeIf(String::isNotBlank)

    private fun List<Map<*, *>>.scoreFor(teamId: String): String? =
        lastOrNull { it["battingTeamId"] == teamId }?.let { inning ->
            val runs = inning.number("score") ?: return@let null
            "$runs/${inning.number("wickets") ?: 0}"
        }

    private fun List<Map<*, *>>.oversFor(teamId: String): String? =
        lastOrNull { it["battingTeamId"] == teamId }?.let { inning ->
            val legalBalls = inning.number("legalBalls")
            if (legalBalls != null) "${legalBalls / 6}.${legalBalls % 6}"
            else inning["overs"]?.toString()?.takeIf(String::isNotBlank)
        }

    private fun Double.formatRate(): String = String.format(Locale.US, "%.2f", this)

    private fun ballsFromOvers(overs: String): Int? {
        val parts = overs.split('.')
        val completedOvers = parts.firstOrNull()?.toIntOrNull() ?: return null
        val ballsInOver = parts.getOrNull(1)?.toIntOrNull() ?: 0
        if (ballsInOver !in 0..5) return null
        return completedOvers * 6 + ballsInOver
    }

    private fun readMatch(): MatchDetail {
        return MatchDetail(
            league = intent.getStringExtra(EXTRA_LEAGUE)?.takeIf(String::isNotBlank) ?: "MATCH SCORECARD",
            matchId = resolveMatchId(intent),
            round = intent.getStringExtra(EXTRA_ROUND)?.takeIf(String::isNotBlank).orEmpty(),
            leftName = intent.getStringExtra(EXTRA_LEFT_NAME)?.takeIf(String::isNotBlank) ?: "TEAM A",
            leftTeamId = "",
            leftScore = intent.getStringExtra(EXTRA_LEFT_SCORE) ?: "—",
            leftOvers = intent.getStringExtra(EXTRA_LEFT_OVERS).orEmpty(),
            rightName = intent.getStringExtra(EXTRA_RIGHT_NAME)?.takeIf(String::isNotBlank) ?: "TEAM B",
            rightTeamId = "",
            rightScore = intent.getStringExtra(EXTRA_RIGHT_SCORE) ?: "—",
            rightOvers = intent.getStringExtra(EXTRA_RIGHT_OVERS).orEmpty(),
            target = intent.getStringExtra(EXTRA_TARGET) ?: "—",
            rrr = intent.getStringExtra(EXTRA_RRR) ?: "—",
            currentRunRate = "—",
            tossInfo = "",
            isFirstInnings = true,
            battingTeamIsLeft = null,
            ballsBowled = null,
            scheduledBalls = null,
            currentBatters = emptyList(),
            currentInningsId = null,
            currentBowlerId = null,
            currentBowlerName = "",
            currentPartnership = CurrentPartnership(),
            currentBowling = null,
            lastWicket = "No wicket this innings",
            lastFiveOvers = "0 runs, 0 wickets",
            scorecardViews = 0L,
            win = intent.getStringExtra(EXTRA_WIN).orEmpty(),
            note = intent.getStringExtra(EXTRA_NOTE).orEmpty(),
            venue = ""
        )
    }
}

private data class MatchDetail(
    val league: String,
    val matchId: String,
    val round: String,
    val leftName: String,
    val leftTeamId: String,
    val leftScore: String,
    val leftOvers: String,
    val rightName: String,
    val rightTeamId: String,
    val rightScore: String,
    val rightOvers: String,
    val venue: String,
    val target: String,
    val rrr: String,
    val currentRunRate: String,
    val tossInfo: String,
    val isFirstInnings: Boolean,
    val battingTeamIsLeft: Boolean?,
    val ballsBowled: Int?,
    val scheduledBalls: Int?,
    val currentBatters: List<CurrentBatter>,
    val currentInningsId: String?,
    val currentBowlerId: String?,
    val currentBowlerName: String,
    val playerNamesLoading: Boolean = false,
    val currentPartnership: CurrentPartnership = CurrentPartnership(),
    val currentBowling: CurrentBowlingFigures? = null,
    val lastWicket: String = "No wicket this innings",
    val lastFiveOvers: String = "0 runs, 0 wickets",
    val lastSixBalls: List<BallOutcome> = emptyList(),
    val battedTeamIds: Set<String> = emptySet(),
    val scorecardBatting: Map<String, List<ScorecardBattingRow>> = emptyMap(),
    val scorecardBowling: Map<String, List<ScorecardBowlingRow>> = emptyMap(),
    val scorecardViews: Long = 0L,
    val win: String,
    val note: String
)

private data class CurrentBatter(
    val id: String?,
    val name: String,
    val runs: Int,
    val balls: Int,
    val fours: Int,
    val sixes: Int,
    val isStriker: Boolean
) {
    val strikeRate: String
        get() = if (balls == 0) "0.0" else String.format(Locale.US, "%.1f", runs * 100.0 / balls)
}

private data class CurrentPartnership(
    val runs: Int = 0,
    val balls: Int = 0,
    val firstBatterRuns: Int = 0,
    val secondBatterRuns: Int = 0,
    val firstBatterBalls: Int = 0,
    val secondBatterBalls: Int = 0
) {
    val runRate: String
        get() = if (balls == 0) "0.00" else String.format(Locale.US, "%.2f", runs * 6.0 / balls)
}

private data class CurrentBowlingFigures(
    val name: String,
    val overs: String,
    val maidens: Int,
    val runs: Int,
    val wickets: Int,
    val economy: String
)

private data class BallOutcome(val label: String, val kind: String)
private data class ScorecardBattingRow(
    val playerId: String,
    val name: String,
    val runs: Int,
    val balls: Int,
    val fours: Int,
    val sixes: Int,
    val strikeRate: String,
    val active: Boolean
)
private data class ScorecardBowlingRow(val playerId: String, val name: String, val overs: String, val runs: Int, val wickets: Int, val economy: String)

private fun Map<String, String>.resolvePlayerName(playerId: String?, candidate: String?): String {
    playerId?.let { this[it]?.takeIf(String::isNotBlank) }?.let { return it }
    val value = candidate?.trim().orEmpty()
    val looksLikeId = value.isNotBlank() && (
        value == playerId || "::" in value || value.matches(Regex("[A-Za-z0-9_-]{20,}"))
    )
    if (value.isNotBlank() && !looksLikeId) return value
    return "Player"
}

private fun String?.normalizePlayerProfileName(): String = this
    ?.trim()
    ?.lowercase()
    ?.replace(Regex("\\s+"), " ")
    .orEmpty()

private fun List<DocumentSnapshot>.activeMatchDeliveries(): List<Map<String, Any?>> {
    val all = mapNotNull { it.data }.sortedWith(compareBy<Map<String, Any?>> { (it["sequenceNumber"] as? Number)?.toLong() ?: 0L })
    val reversedIds = all.mapNotNull { it["reversedEventId"] as? String }.toSet()
    return all.filter { it["eventType"] != "REVERSAL" && (it["deliveryId"] as? String) !in reversedIds }
}

private fun List<Map<String, Any?>>.scorecardBattingRows(
    match: MatchDetail,
    playerNames: Map<String, String>
): Map<String, List<ScorecardBattingRow>> =
    groupBy { it["battingTeamId"] as? String ?: return@groupBy "" }
        .filterKeys(String::isNotBlank)
        .mapValues { (_, inningsDeliveries) ->
            inningsDeliveries.groupBy { it["batsmanId"] as? String ?: (it["batsmanName"] as? String ?: "Unknown") }
                .map { (playerId, balls) ->
                    val runs = balls.sumOf { (it["runs"] as? Number)?.toInt() ?: 0 }
                    val faced = balls.count { it["extraType"] != "WIDE" }
                    val knownName = match.currentBatters.firstOrNull { it.id == playerId }?.name
                    val batter = match.currentBatters.firstOrNull { it.id == playerId }
                    val deliveryName = balls.firstNotNullOfOrNull { it["batsmanName"] as? String }
                    val resolvedName = playerNames.resolvePlayerName(playerId, knownName ?: deliveryName)
                    ScorecardBattingRow(
                        playerId = playerId,
                        name = resolvedName + if (batter?.isStriker == true) "*" else "",
                        runs = runs,
                        balls = faced,
                        fours = balls.count { (it["runs"] as? Number)?.toInt() == 4 },
                        sixes = balls.count { (it["runs"] as? Number)?.toInt() == 6 },
                        strikeRate = if (faced == 0) "0.0" else String.format(Locale.US, "%.1f", runs * 100.0 / faced),
                        active = batter?.isStriker == true
                    )
                }.sortedByDescending { it.balls }
        }

private fun List<Map<String, Any?>>.scorecardBowlingRows(
    match: MatchDetail,
    playerNames: Map<String, String>
): Map<String, List<ScorecardBowlingRow>> =
    groupBy { it["bowlingTeamId"] as? String ?: return@groupBy "" }
        .filterKeys(String::isNotBlank)
        .mapValues { (_, inningsDeliveries) ->
            inningsDeliveries.groupBy { it["bowlerId"] as? String ?: (it["bowlerName"] as? String ?: "Unknown") }
                .map { (playerId, balls) ->
                    val legalBalls = balls.count { it["isLegalDelivery"] as? Boolean ?: true }
                    val conceded = balls.sumOf { ball ->
                        val batRuns = (ball["runs"] as? Number)?.toInt() ?: 0
                        val extras = ball["extraBreakdown"] as? List<*> ?: emptyList<Any>()
                        batRuns + extras.filterIsInstance<Map<*, *>>()
                            .filter { it["type"] == "WIDE" || it["type"] == "NO_BALL" }
                            .sumOf { (it["runs"] as? Number)?.toInt() ?: 0 }
                    }
                    val wickets = balls.count { ball ->
                        val type = (ball["dismissalType"] as? String).orEmpty()
                        (ball["dismissedPlayerId"] as? String).orEmpty().isNotBlank() &&
                            type !in setOf("", "NONE", "RUN_OUT", "RETIRED_OUT", "RETIRED_HURT", "OBSTRUCTING_THE_FIELD")
                    }
                    val economy = if (legalBalls == 0) 0.0 else conceded * 6.0 / legalBalls
                    val deliveryName = balls.firstNotNullOfOrNull { it["bowlerName"] as? String }
                    val knownName = if (playerId == match.currentBowlerId) match.currentBowlerName else deliveryName
                    ScorecardBowlingRow(
                        playerId = playerId,
                        name = playerNames.resolvePlayerName(playerId, knownName),
                        overs = "${legalBalls / 6}.${legalBalls % 6}",
                        runs = conceded,
                        wickets = wickets,
                        economy = String.format(Locale.US, "%.1f", economy)
                    )
                }.sortedByDescending { it.overs }
        }

private fun List<Map<String, Any?>>.lastSixBallOutcomes(): List<BallOutcome> = takeLast(6).map { delivery ->
    val runs = (delivery["runs"] as? Number)?.toInt() ?: 0
    val extras = (delivery["extras"] as? Number)?.toInt() ?: 0
    val extraType = (delivery["extraType"] as? String).orEmpty()
    val dismissalType = (delivery["dismissalType"] as? String).orEmpty()
    val isWicket = (delivery["dismissedPlayerId"] as? String).orEmpty().isNotBlank() &&
        dismissalType.isNotBlank() && dismissalType != "NONE" && dismissalType != "RETIRED_HURT"
    when {
        isWicket -> BallOutcome("W", "W")
        extraType == "WIDE" -> BallOutcome("Wd", "EXTRA")
        extraType == "NO_BALL" -> BallOutcome("Nb", "EXTRA")
        runs + extras == 0 -> BallOutcome(".", ".")
        else -> BallOutcome((runs + extras).toString(), if (runs + extras == 4) "4" else if (runs + extras == 6) "6" else "RUN")
    }
}

private fun List<DocumentSnapshot>.activeInningsDeliveries(match: MatchDetail): List<Map<String, Any?>> {
    val inningsId = match.currentInningsId ?: return emptyList()
    val inningsDeliveries = mapNotNull { document ->
        val data = document.data ?: return@mapNotNull null
        if (data["inningsId"] != inningsId) return@mapNotNull null
        data
    }.sortedWith(compareBy<Map<String, Any?>> { (it["sequenceNumber"] as? Number)?.toLong() ?: 0L })
    val reversedDeliveryIds = inningsDeliveries.mapNotNull { it["reversedEventId"] as? String }.toSet()
    val activeDeliveries = inningsDeliveries.filter {
        it["eventType"] != "REVERSAL" && (it["deliveryId"] as? String) !in reversedDeliveryIds
    }
    return activeDeliveries
}

private fun List<Map<String, Any?>>.calculateCurrentPartnership(match: MatchDetail): CurrentPartnership {
    val lastWicketIndex = indexOfLast { delivery ->
        val dismissal = (delivery["dismissedPlayerId"] as? String).orEmpty()
        val type = (delivery["dismissalType"] as? String).orEmpty()
        dismissal.isNotBlank() && type.isNotBlank() && type != "NONE" && type != "RETIRED_HURT"
    }
    val partnershipDeliveries = drop(lastWicketIndex + 1)
    fun number(delivery: Map<String, Any?>, key: String): Int = (delivery[key] as? Number)?.toInt() ?: 0
    val firstBatterId = match.currentBatters.getOrNull(0)?.id
    val secondBatterId = match.currentBatters.getOrNull(1)?.id
    return CurrentPartnership(
        runs = partnershipDeliveries.sumOf { number(it, "runs") + number(it, "extras") },
        balls = partnershipDeliveries.count { it["isLegalDelivery"] as? Boolean ?: true },
        firstBatterRuns = partnershipDeliveries.filter { it["batsmanId"] == firstBatterId }.sumOf { number(it, "runs") },
        secondBatterRuns = partnershipDeliveries.filter { it["batsmanId"] == secondBatterId }.sumOf { number(it, "runs") },
        firstBatterBalls = partnershipDeliveries.count { it["batsmanId"] == firstBatterId && it["extraType"] != "WIDE" },
        secondBatterBalls = partnershipDeliveries.count { it["batsmanId"] == secondBatterId && it["extraType"] != "WIDE" }
    )
}

private fun List<Map<String, Any?>>.lastWicketSummary(): String {
    val wicketIndex = indexOfLast { delivery ->
        val dismissedPlayer = (delivery["dismissedPlayerId"] as? String).orEmpty()
        val dismissalType = (delivery["dismissalType"] as? String).orEmpty()
        dismissedPlayer.isNotBlank() && dismissalType.isNotBlank() &&
            dismissalType != "NONE" && dismissalType != "RETIRED_HURT"
    }
    if (wicketIndex < 0) return "No wicket this innings"
    fun number(delivery: Map<String, Any?>, key: String): Int = (delivery[key] as? Number)?.toInt() ?: 0
    val wicketDelivery = this[wicketIndex]
    val runsAtFall = take(wicketIndex + 1).sumOf { number(it, "runs") + number(it, "extras") }
    val wicketsAtFall = take(wicketIndex + 1).count { delivery ->
        val dismissalType = (delivery["dismissalType"] as? String).orEmpty()
        (delivery["dismissedPlayerId"] as? String).orEmpty().isNotBlank() &&
            dismissalType.isNotBlank() && dismissalType != "NONE" && dismissalType != "RETIRED_HURT"
    }
    val dismissalType = (wicketDelivery["dismissalType"] as? String)
        ?.replace('_', ' ')
        ?.lowercase(Locale.US)
        ?.replaceFirstChar { it.uppercase(Locale.US) }
        .orEmpty()
    return "$runsAtFall/$wicketsAtFall · $dismissalType"
}

private fun List<Map<String, Any?>>.lastFiveOversSummary(): String {
    if (isEmpty()) return "0 runs, 0 wickets"
    val legalIndices = indices.filter { this[it]["isLegalDelivery"] as? Boolean ?: true }
    val startIndex = legalIndices.getOrNull((legalIndices.size - 30).coerceAtLeast(0)) ?: 0
    val recentDeliveries = drop(startIndex)
    val runs = recentDeliveries.sumOf {
        ((it["runs"] as? Number)?.toInt() ?: 0) + ((it["extras"] as? Number)?.toInt() ?: 0)
    }
    val wickets = recentDeliveries.count { delivery ->
        val dismissalType = (delivery["dismissalType"] as? String).orEmpty()
        (delivery["dismissedPlayerId"] as? String).orEmpty().isNotBlank() &&
            dismissalType.isNotBlank() && dismissalType != "NONE" && dismissalType != "RETIRED_HURT"
    }
    val legalBalls = recentDeliveries.count { it["isLegalDelivery"] as? Boolean ?: true }
    return "$runs runs, $wickets wickets ($legalBalls balls)"
}

private fun List<Map<String, Any?>>.currentBowlingFigures(match: MatchDetail): CurrentBowlingFigures? {
    val bowlerId = match.currentBowlerId ?: lastOrNull()?.get("bowlerId") as? String ?: return null
    val deliveries = filter { it["bowlerId"] == bowlerId }
    if (deliveries.isEmpty()) return null
    fun number(delivery: Map<String, Any?>, key: String): Int = (delivery[key] as? Number)?.toInt() ?: 0
    fun extrasOfType(delivery: Map<String, Any?>, type: String): Int {
        val breakdown = delivery["extraBreakdown"] as? List<*> ?: return if (delivery["extraType"] == type) number(delivery, "extras") else 0
        return breakdown.filterIsInstance<Map<*, *>>()
            .filter { it["type"] == type }
            .sumOf { (it["runs"] as? Number)?.toInt() ?: 0 }
    }
    fun runsConceded(delivery: Map<String, Any?>): Int =
        number(delivery, "runs") + extrasOfType(delivery, "WIDE") + extrasOfType(delivery, "NO_BALL")
    val legalBalls = deliveries.count { it["isLegalDelivery"] as? Boolean ?: true }
    val runs = deliveries.sumOf(::runsConceded)
    val wickets = deliveries.count { delivery ->
        val dismissalType = (delivery["dismissalType"] as? String).orEmpty()
        (delivery["dismissedPlayerId"] as? String).orEmpty().isNotBlank() &&
            dismissalType !in setOf("", "NONE", "RUN_OUT", "RETIRED_OUT", "RETIRED_HURT", "OBSTRUCTING_THE_FIELD")
    }
    val maidens = deliveries.groupBy { it["over"] }.values.count { over ->
        over.count { it["isLegalDelivery"] as? Boolean ?: true } == 6 && over.sumOf(::runsConceded) == 0
    }
    val economy = if (legalBalls == 0) 0.0 else runs * 6.0 / legalBalls
    return CurrentBowlingFigures(
        name = match.currentBowlerName.ifBlank { "Bowler" },
        overs = "${legalBalls / 6}.${legalBalls % 6}",
        maidens = maidens,
        runs = runs,
        wickets = wickets,
        economy = String.format(Locale.US, "%.2f", economy)
    )
}

private val Accent = Color(0xFFC1FF00)
private val ElectricBlue = Color(0xFF007FFF)
private val CyanLine = Color(0xFF00D2FF)
private val ScreenBg = Color(0xFF010509)
private val Surface = Color(0xFF060C11)
private val Panel = Color(0xFF071016)
private val SoftText = Color(0xFF84938F)

private enum class DetailIcon { BACK, SEARCH, BELL, MESSAGE, SHIELD, BOLT, DOTS }
private enum class MetricIcon { SPEED, TARGET, TREND, TROPHY }

@Composable
private fun ScorecardScreen(match: MatchDetail, onBack: () -> Unit, onShare: () -> Unit, onOpenPlayer: (String, String) -> Unit) {
    var selectedTab by remember { mutableIntStateOf(1) }
    val tabs = listOf("INFO", "LIVE", "SCORECARD", "SUMMARY", "COMMENTARY")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF061019), ScreenBg, Color(0xFF031006))
                )
            )
    ) {
        Column(Modifier.fillMaxSize()) {
            ScorecardTopBar(match = match, onBack = onBack, onShare = onShare)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(selectedTab) {
                        var dragTotal = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { dragTotal = 0f },
                            onHorizontalDrag = { _, dragAmount -> dragTotal += dragAmount },
                            onDragEnd = {
                                when {
                                    dragTotal < -90f && selectedTab < tabs.lastIndex -> selectedTab += 1
                                    dragTotal > 90f && selectedTab > 0 -> selectedTab -= 1
                                }
                            }
                        )
                    }
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MatchDetailsStrip(match)
                MatchHero(match)
                ScorecardTabs(
                    tabs = tabs,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val direction = if (targetState > initialState) {
                            AnimatedContentTransitionScope.SlideDirection.Left
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.Right
                        }
                        slideIntoContainer(direction, animationSpec = tween(320)) + fadeIn(tween(180)) togetherWith
                            slideOutOfContainer(direction, animationSpec = tween(320)) + fadeOut(tween(180))
                    },
                    label = "scorecardSwipeTransition"
                ) { tab ->
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        when (tab) {
                            0 -> InfoTab(match)
                            1 -> LiveTab(match)
                            2 -> ScorecardTab(match, onOpenPlayer)
                            3 -> SummaryTab(match)
                            else -> CommentaryTab(match)
                        }
                    }
                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun ScorecardTopBar(
    match: MatchDetail,
    onBack: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Surface)
            .padding(start = 10.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF02070B))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            DetailIconView(DetailIcon.BACK, Color.White, Modifier.size(20.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, end = 8.dp)
        ) {
            Text(
                text = match.league,
                color = Color.White,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (match.round.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = match.round,
                    color = SoftText,
                    fontSize = 7.2.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(enabled = match.matchId.isNotBlank(), onClick = onShare),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_share),
                contentDescription = "Share scorecard",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ScorecardTabs(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(16.dp, RoundedCornerShape(28.dp), clip = false)
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.055f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
            .horizontalScroll(rememberScrollState())
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            TabButton(
                label = label,
                selected = selectedTab == index,
                modifier = Modifier,
                onClick = { onTabSelected(index) }
            )
        }
    }
}

@Composable
private fun TabButton(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val glow by animateFloatAsState(targetValue = if (selected) 1f else 0f, label = "tabGlow")
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(23.dp))
            .background(
                if (selected) {
                    Brush.horizontalGradient(listOf(Accent.copy(alpha = 0.24f), ElectricBlue.copy(alpha = 0.12f)))
                } else {
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                }
            )
            .border(
                1.dp,
                if (selected) Accent.copy(alpha = 0.28f + glow * 0.32f) else Color.Transparent,
                RoundedCornerShape(23.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            color = if (selected) Accent else Color(0xFFB7C1BD),
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .width(if (selected) 18.dp else 0.dp)
                .height(4.dp)
                .shadow(if (selected) 9.dp else 0.dp, RoundedCornerShape(2.dp), clip = false)
                .clip(RoundedCornerShape(2.dp))
                .background(if (selected) Accent else Color.Transparent)
        )
    }
}

@Composable
private fun MatchDetailsStrip(match: MatchDetail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 5.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = buildString {
                    append("${match.leftName} vs ${match.rightName}")
                    if (match.round.isNotBlank()) append(", ${match.round}")
                    append(" - Live Score")
                },
                color = Color.White,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                lineHeight = 18.sp,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            ViewsPill(match.scorecardViews.toString())
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            DetailMeta("League", match.league, Modifier.weight(1f))
            if (match.venue.isNotBlank()) {
                HeaderDot()
                DetailMeta("Venue", match.venue, Modifier.weight(1.22f))
            }
        }
    }
}

@Composable
private fun DetailMeta(label: String, value: String, modifier: Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("$label: ", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
        Text(value, color = Color.White.copy(alpha = 0.7f), fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun HeaderDot() {
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .size(4.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.24f))
    )
}

@Composable
private fun ViewsPill(value: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.065f))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EyeIcon(Color.White, Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text(value, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun MatchHero(match: MatchDetail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(30.dp, RoundedCornerShape(20.dp), clip = false)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Accent.copy(alpha = 0.14f),
                        Color(0xFF07121A).copy(alpha = 0.96f),
                        ElectricBlue.copy(alpha = 0.16f),
                        Color(0xFF050B10).copy(alpha = 0.98f)
                    )
                )
            )
            .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiveBadge()
            Spacer(Modifier.weight(1f))
            Text(
                text = match.league,
                color = Color(0xFFE8F0ED),
                fontSize = 8.4.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            CompactTeam(match.leftName, match.leftScore, match.leftOvers, DetailIcon.SHIELD, Modifier.weight(1f))
            CompactTeam(match.rightName, match.rightScore, match.rightOvers, DetailIcon.BOLT, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricPill(MetricIcon.SPEED, "CRR", match.currentRunRate, Modifier.weight(1f))
            MetricPill(MetricIcon.TARGET, "TARGET", match.target, Modifier.weight(1f))
            MetricPill(MetricIcon.TREND, "RRR", match.rrr, Modifier.weight(1f))
            MetricPill(MetricIcon.TROPHY, "WIN", match.win, Modifier.weight(1.1f))
        }
        Spacer(Modifier.height(6.dp))
        if (match.tossInfo.isNotBlank()) {
            Text(match.tossInfo, color = Color(0xFFB8C6C1), fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun CompactTeam(name: String, score: String, detail: String, icon: DetailIcon, modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .shadow(13.dp, RoundedCornerShape(14.dp), clip = false)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            DetailIconView(icon, Color(0xFFE2E9E6), Modifier.size(18.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(name, color = SoftText, fontSize = 8.sp, fontWeight = FontWeight.Black)
        Text(score, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
        Text("($detail)", color = Color(0xFF8B9692), fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MetricPill(icon: MetricIcon, label: String, value: String, modifier: Modifier) {
    Row(
        modifier = modifier
            .height(32.dp)
            .shadow(9.dp, RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.075f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(horizontal = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MetricIconView(icon, Accent, Modifier.size(12.dp))
        Spacer(Modifier.width(4.dp))
        Column {
            Text(label, color = SoftText, fontSize = 5.5.sp, fontWeight = FontWeight.Black)
            Text(value, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun InfoTab(match: MatchDetail) {
    SectionPanel("MATCH INFO") {
        if (match.venue.isNotBlank()) InfoRow("Venue", match.venue)
        if (match.tossInfo.isNotBlank()) InfoRow("Toss", match.tossInfo)
        InfoRow("Target", match.target)
        InfoRow("Required rate", match.rrr)
        InfoRow("Win projection", match.win)
    }
    TwoColumnStats("CURRENT SNAPSHOT", listOf("Partnership" to "${match.currentPartnership.runs} (${match.currentPartnership.balls})", "Run rate" to match.currentRunRate, "Last 5 overs" to match.lastFiveOvers, "Momentum" to match.leftName))
}

@Composable
private fun LiveTab(match: MatchDetail) {
    LiveScorePanel(match)
    LastSixBalls(match)
    CurrentBattingCard(match.currentBatters, match.playerNamesLoading)
    CurrentBowlingCard(match)
    PartnershipCard(match)
    LiveKeyStats(match)
    WinPredictorPanel(match)
}

@Composable
private fun LiveScorePanel(match: MatchDetail) {
    SectionPanel("LIVE SCORE") {
        val battingIsLeft = match.battingTeamIsLeft ?: true
        val battingName = if (battingIsLeft) match.leftName else match.rightName
        val battingScore = if (battingIsLeft) match.leftScore else match.rightScore
        val battingOvers = if (battingIsLeft) match.leftOvers else match.rightOvers
        val oppositionName = if (battingIsLeft) match.rightName else match.leftName
        val oppositionScore = if (battingIsLeft) match.rightScore else match.leftScore
        val oppositionOvers = if (battingIsLeft) match.rightOvers else match.leftOvers
        val currentRuns = battingScore.substringBefore('/').toIntOrNull()
        val target = match.target.toIntOrNull()
        val runsNeeded = if (target != null && currentRuns != null) (target - currentRuns).coerceAtLeast(0) else null
        val ballsRemaining = if (match.scheduledBalls != null && match.ballsBowled != null) {
            (match.scheduledBalls - match.ballsBowled).coerceAtLeast(0)
        } else null

        if (!match.isFirstInnings && oppositionScore != "—") {
            Text(
                text = "1ST INNINGS  ·  $oppositionName  $oppositionScore${oppositionOvers.takeIf(String::isNotBlank)?.let { " ($it ov)" }.orEmpty()}",
                color = SoftText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(9.dp))
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(battingName, color = SoftText, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(battingScore, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                    Spacer(Modifier.width(7.dp))
                    Text("(${battingOvers.ifBlank { "0.0" }} ov)", color = SoftText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 3.dp))
                }
            }
            Text(if (match.isFirstInnings) "1ST INNINGS" else "2ND INNINGS", color = Accent, fontSize = 8.sp, fontWeight = FontWeight.Black)
        }

        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LiveRateChip("CRR", match.currentRunRate)
            if (!match.isFirstInnings && target != null) {
                Spacer(Modifier.width(7.dp))
                LiveRateChip("REQ", match.rrr)
                Spacer(Modifier.width(7.dp))
                LiveRateChip("TARGET", match.target)
            }
        }

        if (!match.isFirstInnings && runsNeeded != null && ballsRemaining != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (runsNeeded == 0) "Target reached" else "$battingName need $runsNeeded runs from $ballsRemaining balls",
                color = if (runsNeeded == 0) Accent else Color(0xFFFF4D5E),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LiveRateChip(label: String, value: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = SoftText, fontSize = 7.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.width(4.dp))
        Text(value, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun LiveKeyStats(match: MatchDetail) {
    SectionPanel("KEY STATS") {
        LiveStatLine("Partnership", "${match.currentPartnership.runs} (${match.currentPartnership.balls})")
        ThinRule()
        LiveStatLine("Last wicket", match.lastWicket)
        ThinRule()
        LiveStatLine("Last 5 overs", match.lastFiveOvers)
        ThinRule()
        if (match.tossInfo.isNotBlank()) LiveStatLine("Toss", match.tossInfo)
    }
}

@Composable
private fun LiveStatLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = SoftText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.width(96.dp)
        )
        Text(
            text = value,
            color = Color(0xFFD7E2DF),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 14.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun WinPredictorPanel(match: MatchDetail) {
    SectionPanel("MATCH PULSE") {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(match.leftName, color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Text(match.win, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Text(match.rightName, color = ElectricBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(8.dp))
        WinPredictor(match)
    }
}

@Composable
private fun ScorecardTab(match: MatchDetail, onOpenPlayer: (String, String) -> Unit) {
    var selectedTeamTab by remember { mutableIntStateOf(0) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.055f)),
        horizontalArrangement = Arrangement.Center
    ) {
        val leftSelected = selectedTeamTab == 0
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (leftSelected) Accent.copy(alpha = 0.15f) else Color.Transparent)
                .clickable { selectedTeamTab = 0 }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(match.leftName, color = if (leftSelected) Accent else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }
        
        val rightSelected = selectedTeamTab == 1
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (rightSelected) Accent.copy(alpha = 0.15f) else Color.Transparent)
                .clickable { selectedTeamTab = 1 }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(match.rightName, color = if (rightSelected) Accent else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }
    }
    
    Spacer(Modifier.height(14.dp))

    val isLeftTeam = selectedTeamTab == 0
    val battingName = if (isLeftTeam) match.leftName else match.rightName
    val bowlingName = if (isLeftTeam) match.rightName else match.leftName
    val battingTeamId = if (isLeftTeam) match.leftTeamId else match.rightTeamId
    val bowlingTeamId = if (isLeftTeam) match.rightTeamId else match.leftTeamId
    val displayedScore = if (isLeftTeam) match.leftScore else match.rightScore
    val displayedOvers = if (isLeftTeam) match.leftOvers else match.rightOvers
    val hasBatted = battingTeamId.isNotBlank() && battingTeamId in match.battedTeamIds ||
        displayedScore != "—" && displayedOvers.isNotBlank()
    if (!hasBatted) {
        SectionPanel("$battingName BATTING") {
            Text(
                "This team has not started batting.",
                color = SoftText,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp, horizontal = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    } else {
        SectionPanel("$battingName BATTING") {
            PlayerTableHeader("BATTER", listOf("R", "B", "4S", "6S", "SR"))
            ThinRule()
            val battingRows = match.scorecardBatting[battingTeamId].orEmpty()
            if (battingRows.isEmpty()) {
                Text("No batting figures recorded yet.", color = SoftText, fontSize = 11.sp, modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp))
            } else {
                battingRows.forEachIndexed { index, row ->
                    if (index > 0) ThinRule()
                    BatterTableRow(
                        playerId = row.playerId,
                        name = row.name,
                        runs = row.runs.toString(),
                        balls = row.balls.toString(),
                        fours = row.fours.toString(),
                        sixes = row.sixes.toString(),
                        sr = row.strikeRate,
                        active = row.active,
                        nameLoading = match.playerNamesLoading && row.name == "Player",
                        onOpenPlayer = onOpenPlayer
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        SectionPanel("$bowlingName BOWLING") {
            TableHeader("BOWLER", "O", "R", "W")
            ThinRule()
            val bowlingRows = match.scorecardBowling[bowlingTeamId].orEmpty()
            if (bowlingRows.isEmpty()) {
                Text("No bowling figures recorded yet.", color = SoftText, fontSize = 11.sp, modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp))
            } else {
                bowlingRows.forEachIndexed { index, row ->
                    if (index > 0) ThinRule()
                    ScoreLine(row.playerId, row.name, row.overs, row.runs.toString(), row.wickets.toString(), if (index == 0) CyanLine else Color.White, match.playerNamesLoading && row.name == "Player") { playerId, name -> onOpenPlayer(playerId, name) }
                }
            }
        }
    }
}

@Composable
private fun SummaryTab(match: MatchDetail) {
    LastSixBalls(match)
    CurrentBattingCard(match.currentBatters, match.playerNamesLoading)
    PartnershipCard(match)
    CurrentBowlingCard(match)
    RunWormPanel(match)
    MatchInsights(match)
    FallOfWicketsPremium()
}

@Composable
private fun CommentaryTab(match: MatchDetail) {
    SectionPanel("LIVE COMMENTARY") {
        CommentaryLine("18.4", "Four through cover. ${match.leftName} keep the chase alive.", Accent)
        CommentaryLine("18.3", "Short ball pulled to deep square for two.", Color.White)
        CommentaryLine("18.2", "Dot ball. Clever slower one outside off.", Color.White)
        CommentaryLine("18.1", "Single clipped behind square.", Color.White)
    }
}

@Composable
private fun SectionPanel(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(18.dp), clip = false)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.085f),
                        Color(0xFF151B20).copy(alpha = 0.92f),
                        Color(0xFF0B1116).copy(alpha = 0.96f)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.105f), RoundedCornerShape(18.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.07f), Accent.copy(alpha = 0.035f))))
                .padding(horizontal = 16.dp, vertical = 13.dp)
        ) {
            Text(title, color = SoftText, fontSize = 10.8.sp, fontWeight = FontWeight.Black)
        }
        Column(Modifier.padding(horizontal = 16.dp, vertical = 15.dp)) {
            content()
        }
    }
}

@Composable
private fun TwoColumnStats(title: String, stats: List<Pair<String, String>>) {
    SectionPanel(title) {
        stats.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { stat ->
                    StatTile(stat.first, stat.second, Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun LastSixBalls(match: MatchDetail) {
    SectionPanel("LAST 6 BALLS") {
        if (match.lastSixBalls.isEmpty()) {
            Text("Ball outcomes will appear when live scoring begins.", color = SoftText, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp))
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                match.lastSixBalls.forEachIndexed { index, outcome ->
                    BallBubble(outcome = outcome, isLatest = index == match.lastSixBalls.lastIndex)
                }
                Spacer(Modifier.weight(1f))
                val ballsBowled = match.ballsBowled
                val overLabel = ballsBowled?.let { "${it / 6}.${it % 6} OV" } ?: "— OV"
                Text(overLabel, color = SoftText, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun BallBubble(outcome: BallOutcome, isLatest: Boolean) {
    val ball = outcome.label
    val scale by animateFloatAsState(
        targetValue = if (isLatest) 1.08f else 1f,
        animationSpec = tween(420),
        label = "ballEntry"
    )
    val colors = when (outcome.kind) {
        "6" -> listOf(Accent, Color(0xFF7BFF2B))
        "4" -> listOf(Color(0xFF8EEA42), Color(0xFF16B85E))
        "W" -> listOf(Color(0xFFFF5F6D), Color(0xFF99001E))
        "." -> listOf(Color(0xFF30383D), Color(0xFF11171B))
        else -> listOf(Color(0xFF40484E), Color(0xFF1A2227))
    }
    val textColor = if (outcome.kind == "6" || outcome.kind == "4") ScreenBg else Color.White
    Box(
        modifier = Modifier
            .size(40.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(12.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(Brush.linearGradient(colors))
            .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(ball, color = textColor, fontSize = if (ball.length > 1) 10.sp else 14.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun CurrentBattingCard(batters: List<CurrentBatter>, namesLoading: Boolean) {
    SectionPanel("CURRENT BATTING") {
        PlayerTableHeader("BATTER", listOf("R", "B", "4S", "6S", "SR"))
        ThinRule()
        if (batters.isEmpty()) {
            Text("Batting figures will appear when live scoring begins.", color = SoftText, fontSize = 11.sp, modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp))
        } else {
            batters.forEachIndexed { index, batter ->
                if (index > 0) ThinRule()
                BatterTableRow(
                    playerId = batter.id.orEmpty(),
                    name = batter.name + if (batter.isStriker) "*" else "",
                    runs = batter.runs.toString(),
                    balls = batter.balls.toString(),
                    fours = batter.fours.toString(),
                    sixes = batter.sixes.toString(),
                    sr = batter.strikeRate,
                    active = batter.isStriker,
                    nameLoading = namesLoading && batter.name == "Player",
                    onOpenPlayer = { _, _ -> }
                )
            }
        }
    }
}

@Composable
private fun PlayerTableHeader(first: String, columns: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.055f))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(first, color = SoftText.copy(alpha = 0.75f), fontSize = 8.4.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1.9f))
        columns.forEach { label ->
            Text(label, color = SoftText.copy(alpha = 0.75f), fontSize = 8.4.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End, modifier = Modifier.weight(0.62f))
        }
    }
}

@Composable
private fun BatterTableRow(playerId: String, name: String, runs: String, balls: String, fours: String, sixes: String, sr: String, active: Boolean, nameLoading: Boolean = false, onOpenPlayer: (String, String) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.985f else 1f, label = "batterRowPress")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(14.dp))
            .background(if (active) Accent.copy(alpha = 0.105f) else Color.Transparent)
            .clickable(interactionSource = interactionSource, indication = null, onClick = { if (playerId.isNotBlank()) onOpenPlayer(playerId, name.removeSuffix("*")) })
            .padding(horizontal = 10.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (nameLoading) PlayerNameSkeleton(Modifier.weight(1.9f)) else Text(
            name, color = if (active) Accent else Color(0xFFE4ECE8), fontSize = 14.sp,
            fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic,
            modifier = Modifier.weight(1.9f).clickable(enabled = playerId.isNotBlank()) { onOpenPlayer(playerId, name.removeSuffix("*")) }, maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        listOf(runs, balls, fours, sixes, sr).forEachIndexed { index, value ->
            Text(
                value,
                color = if (index == 0) Accent else Color(0xFFD9E0DD),
                fontSize = if (index == 4) 11.sp else 12.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(0.62f)
            )
        }
    }
}

@Composable
private fun PartnershipCard(match: MatchDetail) {
    val partnership = match.currentPartnership
    val firstBatter = match.currentBatters.getOrNull(0)
    val secondBatter = match.currentBatters.getOrNull(1)
    val batterRuns = partnership.firstBatterRuns + partnership.secondBatterRuns
    val firstShare = if (batterRuns == 0) 0.5f else partnership.firstBatterRuns.toFloat() / batterRuns
    val firstPercent = if (batterRuns == 0) 0 else partnership.firstBatterRuns * 100 / batterRuns
    val secondPercent = if (batterRuns == 0) 0 else 100 - firstPercent
    SectionPanel("CURRENT PARTNERSHIP") {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(partnership.runs.toString(), color = Accent, fontSize = 31.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                Text("RUNS", color = SoftText, fontSize = 8.5.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(10.dp))
            Text("(${partnership.balls} BALLS)", color = Color.White.copy(alpha = 0.72f), fontSize = 10.5.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Text("RR ${partnership.runRate}", color = SoftText, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(16.dp))
        ContributionBar(leftFraction = firstShare)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth()) {
            Text("${firstBatter?.name ?: "—"} $firstPercent%", color = Accent, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${secondBatter?.name ?: "—"} $secondPercent%", color = Color(0xFFD7E2DF), fontSize = 9.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.height(14.dp))
        PartnershipRate(partnership.runRate)
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth()) {
            PartnershipPlayer(firstBatter?.name ?: "—", "${partnership.firstBatterRuns} (${partnership.firstBatterBalls})", Modifier.weight(1f), alignEnd = false)
            PartnershipPlayer(secondBatter?.name ?: "—", "${partnership.secondBatterRuns} (${partnership.secondBatterBalls})", Modifier.weight(1f), alignEnd = true)
        }
    }
}

@Composable
private fun PartnershipPlayer(name: String, score: String, modifier: Modifier, alignEnd: Boolean) {
    Column(modifier, horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(name, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
        Text(score, color = SoftText, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun ContributionBar(leftFraction: Float) {
    val animated by animateFloatAsState(targetValue = leftFraction, animationSpec = tween(760), label = "partnershipContribution")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .shadow(8.dp, RoundedCornerShape(7.dp), clip = false)
            .clip(RoundedCornerShape(7.dp))
            .background(Color(0xFF232A2F))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .height(12.dp)
                .background(Brush.horizontalGradient(listOf(Accent, Color(0xFFA3D800))))
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .background(Color.White.copy(alpha = 0.24f))
        )
    }
}

@Composable
private fun PartnershipRate(rate: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF02080D).copy(alpha = 0.72f))
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("RATE VISUAL", color = SoftText, fontSize = 7.5.sp, fontWeight = FontWeight.Black)
            Text("$rate RPO", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
        }
        repeat(8) { index ->
            val height = (16 + index * 4).dp
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(7.dp)
                    .height(height)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (index > 4) Accent else Color.White.copy(alpha = 0.22f))
            )
        }
    }
}

@Composable
private fun CurrentBowlingCard(match: MatchDetail) {
    SectionPanel("CURRENT BOWLING") {
        PlayerTableHeader("BOWLER", listOf("O", "M", "R", "W", "ECN"))
        ThinRule()
        val bowler = match.currentBowling
        if (bowler == null) {
            Text("Bowling figures will appear when live scoring begins.", color = SoftText, fontSize = 11.sp, modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp))
        } else {
            BowlerTableRow(
                bowler.name,
                bowler.overs,
                bowler.maidens.toString(),
                bowler.runs.toString(),
                bowler.wickets.toString(),
                bowler.economy,
                nameLoading = match.playerNamesLoading && bowler.name == "Player"
            )
        }
    }
}

@Composable
private fun BowlerTableRow(name: String, overs: String, maidens: String, runs: String, wickets: String, economy: String, nameLoading: Boolean = false) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.985f else 1f, label = "bowlerRowPress")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(14.dp))
            .background(if (wickets.toIntOrNull() ?: 0 >= 2) ElectricBlue.copy(alpha = 0.105f) else Color.Transparent)
            .clickable(interactionSource = interactionSource, indication = null, onClick = {})
            .padding(horizontal = 10.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (nameLoading) PlayerNameSkeleton(Modifier.weight(1.9f)) else Text(name, color = if (wickets.toIntOrNull() ?: 0 >= 2) CyanLine else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, modifier = Modifier.weight(1.9f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        listOf(overs, maidens, runs, wickets, economy).forEachIndexed { index, value ->
            Text(value, color = if (index == 3) Accent else Color(0xFFD9E0DD), fontSize = if (index == 4) 11.sp else 12.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End, modifier = Modifier.weight(0.62f))
        }
    }
}

@Composable
private fun ThinRule() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.055f))
    )
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF02080D))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(3.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(label.uppercase(), color = SoftText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun WormPanel() {
    SectionPanel("RUN WORM") {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(124.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFF02070B))
                .padding(8.dp)
        ) {
            val w = size.width
            val h = size.height
            repeat(4) { i ->
                val y = h * (0.2f + i * 0.19f)
                drawLine(Color.White.copy(alpha = 0.07f), Offset(0f, y), Offset(w, y), 1.2f)
            }
            val first = Path().apply {
                moveTo(w * 0.04f, h * 0.82f)
                lineTo(w * 0.22f, h * 0.73f)
                lineTo(w * 0.43f, h * 0.57f)
                lineTo(w * 0.64f, h * 0.39f)
                lineTo(w * 0.95f, h * 0.18f)
            }
            val second = Path().apply {
                moveTo(w * 0.04f, h * 0.88f)
                lineTo(w * 0.24f, h * 0.77f)
                lineTo(w * 0.45f, h * 0.65f)
                lineTo(w * 0.68f, h * 0.48f)
                lineTo(w * 0.95f, h * 0.31f)
            }
            drawPath(second, ElectricBlue, style = Stroke(3.4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(first, Accent, style = Stroke(3.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

@Composable
private fun RunWormPanel(match: MatchDetail) {
    SectionPanel("RUN WORM") {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            LegendLine(match.leftName, Accent, dashed = false)
            Spacer(Modifier.width(14.dp))
            LegendLine(match.rightName, Color(0xFFB4B5B7), dashed = true)
        }
        Spacer(Modifier.height(10.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(194.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Accent.copy(alpha = 0.075f), Color(0xFF02070B), Color(0xFF02070B))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.055f), RoundedCornerShape(5.dp))
                .padding(12.dp)
        ) {
            val w = size.width
            val h = size.height
            repeat(6) { i ->
                val y = h * (0.12f + i * 0.145f)
                drawLine(Color.White.copy(alpha = 0.05f), Offset(w * 0.02f, y), Offset(w * 0.98f, y), 1.05f)
            }
            repeat(5) { i ->
                val x = w * (0.08f + i * 0.22f)
                drawLine(Color.White.copy(alpha = 0.032f), Offset(x, h * 0.1f), Offset(x, h * 0.9f), 1f)
            }
            val nsc = Path().apply {
                moveTo(w * 0.04f, h * 0.88f)
                cubicTo(w * 0.14f, h * 0.82f, w * 0.25f, h * 0.75f, w * 0.35f, h * 0.68f)
                cubicTo(w * 0.48f, h * 0.58f, w * 0.57f, h * 0.48f, w * 0.68f, h * 0.4f)
                cubicTo(w * 0.79f, h * 0.31f, w * 0.88f, h * 0.22f, w * 0.96f, h * 0.12f)
            }
            val vcr = Path().apply {
                moveTo(w * 0.04f, h * 0.9f)
                cubicTo(w * 0.17f, h * 0.86f, w * 0.29f, h * 0.8f, w * 0.41f, h * 0.7f)
                cubicTo(w * 0.53f, h * 0.61f, w * 0.64f, h * 0.56f, w * 0.76f, h * 0.47f)
                cubicTo(w * 0.86f, h * 0.39f, w * 0.92f, h * 0.33f, w * 0.96f, h * 0.25f)
            }
            drawLine(Color.White.copy(alpha = 0.08f), Offset(w * 0.02f, h * 0.92f), Offset(w * 0.98f, h * 0.92f), 1.2f)
            drawLine(Color.White.copy(alpha = 0.08f), Offset(w * 0.02f, h * 0.1f), Offset(w * 0.02f, h * 0.92f), 1.2f)
            drawPath(vcr, Color(0xFFB4B5B7), style = Stroke(2.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(nsc, Accent, style = Stroke(3.3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth()) {
            Text("0 OVER", color = SoftText, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Text("10", color = SoftText, fontSize = 8.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            Text("20", color = SoftText, fontSize = 8.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun LegendLine(label: String, color: Color, dashed: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(if (dashed) 2 else 1) {
                Box(
                    Modifier
                        .width(if (dashed) 5.dp else 12.dp)
                        .height(2.dp)
                        .background(color)
                )
            }
        }
        Spacer(Modifier.width(4.dp))
        Text(label, color = SoftText, fontSize = 8.5.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun MatchInsights(match: MatchDetail) {
    SectionPanel("MATCH INSIGHTS") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            InsightMetric("Projected", "168", Modifier.weight(1f))
            InsightMetric("CRR", "7.61", Modifier.weight(1f))
            InsightMetric("RRR", match.rrr, Modifier.weight(1f))
        }
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            InsightMetric("Best Bowler", "JH 2/22", Modifier.weight(1f))
            InsightMetric("High P'ship", "52", Modifier.weight(1f))
        }
        Spacer(Modifier.height(9.dp))
        WinPredictor(match)
    }
}

@Composable
private fun InsightMetric(label: String, value: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF02080D))
            .border(1.dp, Color.White.copy(alpha = 0.055f), RoundedCornerShape(4.dp))
            .padding(7.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(label.uppercase(), color = SoftText, fontSize = 7.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun WinPredictor(match: MatchDetail) {
    val animated by animateFloatAsState(targetValue = 0.61f, label = "winPredictor")
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("${match.leftName} 61%", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.weight(1f))
        Text("${match.rightName} 39%", color = ElectricBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
    Spacer(Modifier.height(5.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFF111A20))
    ) {
        Box(Modifier.fillMaxWidth(animated).height(10.dp).background(Accent))
        Box(Modifier.weight(1f).height(10.dp).background(ElectricBlue))
    }
}

@Composable
private fun FallOfWicketsPremium() {
    SectionPanel("FALL OF WICKETS") {
        listOf(
            Triple("1-28", "L. Shaw, 3.2", false),
            Triple("2-45", "S. Smith, 6.4", false),
            Triple("3-82", "K. Williamson, 12.1", false),
            Triple("4-135", "T. Head, 16.2", true)
        ).forEach { wicket ->
            PremiumWicketRow(score = wicket.first, detail = wicket.second, latest = wicket.third)
        }
    }
}

@Composable
private fun PremiumWicketRow(score: String, detail: String, latest: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (latest) 11.dp else 9.dp)
                .clip(CircleShape)
                .background(if (latest) Accent else Color(0xFF52605D))
        )
        Spacer(Modifier.width(10.dp))
        Text(
            if (latest) "Last Wicket: $score ($detail)" else "$score ($detail)",
            color = if (latest) Accent else Color.White,
            fontSize = 11.2.sp,
            fontWeight = if (latest) FontWeight.Black else FontWeight.Bold,
            fontStyle = if (latest) FontStyle.Italic else FontStyle.Normal,
            modifier = Modifier.weight(1f)
        )
        if (latest) {
            Text(
                "LIVE",
                color = ScreenBg,
                fontSize = 7.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(Accent)
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun FallOfWicketsTimeline(match: MatchDetail) {
    SectionPanel("FALL OF WICKETS") {
        listOf(
            "24/1" to "3.2 ov",
            "68/2" to "8.5 ov",
            "111/3" to "14.1 ov",
            match.leftScore to "17.0 ov"
        ).forEachIndexed { index, wicket ->
            WicketTimelineRow(score = wicket.first, over = wicket.second, latest = index == 3)
        }
    }
}

@Composable
private fun WicketTimelineRow(score: String, over: String, latest: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (latest) 9.dp else 7.dp)
                .clip(CircleShape)
                .background(if (latest) Accent else Color(0xFF52605D))
        )
        Spacer(Modifier.width(8.dp))
        Text(score, color = if (latest) Accent else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.width(6.dp))
        Text("•", color = SoftText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(6.dp))
        Text(over, color = SoftText, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        if (latest) {
            Spacer(Modifier.weight(1f))
            Text("LATEST", color = ScreenBg, fontSize = 5.8.sp, fontWeight = FontWeight.Black, modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(Accent).padding(horizontal = 5.dp, vertical = 2.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label.uppercase(), color = SoftText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.3f))
    }
}

@Composable
private fun TableHeader(first: String, second: String, third: String, fourth: String) {
    Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(first, color = SoftText, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.9f))
        listOf(second, third, fourth).forEach {
            Text(it, color = SoftText, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.7f))
        }
    }
}

@Composable
private fun ScoreLine(playerId: String, name: String, a: String, b: String, c: String, color: Color, nameLoading: Boolean = false, onOpenPlayer: (String, String) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        if (nameLoading) PlayerNameSkeleton(Modifier.weight(1.9f))
        else Text(name, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.9f).clickable(enabled = playerId.isNotBlank()) { onOpenPlayer(playerId, name) })
        listOf(a, b, c).forEach {
            Text(it, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.7f))
        }
    }
}

@Composable
private fun PlayerNameSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "playerNameSkeleton")
    val progress by transition.animateFloat(
        initialValue = -110f,
        targetValue = 150f,
        animationSpec = infiniteRepeatable(animation = tween(950), repeatMode = RepeatMode.Restart),
        label = "playerNameShimmer"
    )
    Box(
        modifier = modifier
            .padding(end = 16.dp)
            .height(13.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.08f)),
                    start = Offset(progress, 0f),
                    end = Offset(progress + 110f, 20f)
                )
            )
    )
}

@Composable
private fun CommentaryLine(over: String, text: String, color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(over, color = color, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(46.dp))
        Text(text, color = Color(0xFFD7E2DF), fontSize = 13.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun LiveBadge() {
    val infinite = rememberInfiniteTransition(label = "livePulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(820), repeatMode = RepeatMode.Reverse),
        label = "livePulseAlpha"
    )
    val halo by animateDpAsState(targetValue = (7 + pulse * 5).dp, label = "livePulseHalo")
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Accent.copy(alpha = 0.12f))
            .border(1.dp, Accent.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(halo)
                    .clip(CircleShape)
                    .background(Accent.copy(alpha = 0.18f * pulse))
            )
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Accent)
            )
        }
        Spacer(Modifier.width(5.dp))
        Text("LIVE", color = Accent, fontSize = 8.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 6.2.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.55f), RoundedCornerShape(3.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    )
}

@Composable
private fun MetricIconView(icon: MetricIcon, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val s = minOf(w, h)
        val stroke = Stroke(width = s * 0.11f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (icon) {
            MetricIcon.SPEED -> {
                drawArc(tint, 205f, 130f, false, style = stroke)
                drawLine(tint, Offset(w * 0.5f, h * 0.55f), Offset(w * 0.74f, h * 0.34f), stroke.width, StrokeCap.Round)
                drawCircle(tint, s * 0.06f, Offset(w * 0.5f, h * 0.55f))
            }
            MetricIcon.TARGET -> {
                drawCircle(tint, s * 0.38f, Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawCircle(tint, s * 0.2f, Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawCircle(tint, s * 0.06f, Offset(w * 0.5f, h * 0.5f))
            }
            MetricIcon.TREND -> {
                val path = Path().apply {
                    moveTo(w * 0.14f, h * 0.72f)
                    lineTo(w * 0.38f, h * 0.5f)
                    lineTo(w * 0.55f, h * 0.58f)
                    lineTo(w * 0.84f, h * 0.26f)
                }
                drawPath(path, tint, style = stroke)
                drawLine(tint, Offset(w * 0.7f, h * 0.26f), Offset(w * 0.84f, h * 0.26f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.84f, h * 0.26f), Offset(w * 0.84f, h * 0.42f), stroke.width, StrokeCap.Round)
            }
            MetricIcon.TROPHY -> {
                drawRoundRect(tint, Offset(w * 0.3f, h * 0.18f), androidx.compose.ui.geometry.Size(w * 0.4f, h * 0.38f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.08f), style = stroke)
                drawLine(tint, Offset(w * 0.5f, h * 0.56f), Offset(w * 0.5f, h * 0.78f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.33f, h * 0.82f), Offset(w * 0.67f, h * 0.82f), stroke.width, StrokeCap.Round)
                drawArc(tint, 90f, 120f, false, topLeft = Offset(w * 0.08f, h * 0.22f), size = androidx.compose.ui.geometry.Size(w * 0.34f, h * 0.32f), style = stroke)
                drawArc(tint, -30f, 120f, false, topLeft = Offset(w * 0.58f, h * 0.22f), size = androidx.compose.ui.geometry.Size(w * 0.34f, h * 0.32f), style = stroke)
            }
        }
    }
}

@Composable
private fun EyeIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val s = minOf(w, h)
        val stroke = Stroke(width = s * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val eye = Path().apply {
            moveTo(w * 0.08f, h * 0.5f)
            cubicTo(w * 0.24f, h * 0.24f, w * 0.42f, h * 0.18f, w * 0.5f, h * 0.18f)
            cubicTo(w * 0.58f, h * 0.18f, w * 0.76f, h * 0.24f, w * 0.92f, h * 0.5f)
            cubicTo(w * 0.76f, h * 0.76f, w * 0.58f, h * 0.82f, w * 0.5f, h * 0.82f)
            cubicTo(w * 0.42f, h * 0.82f, w * 0.24f, h * 0.76f, w * 0.08f, h * 0.5f)
            close()
        }
        drawPath(eye, tint, style = stroke)
        drawOval(
            color = tint,
            topLeft = Offset(w * 0.39f, h * 0.35f),
            size = Size(w * 0.22f, h * 0.3f)
        )
    }
}

@Composable
private fun IconTile(icon: DetailIcon, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFF02070B))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(9.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        DetailIconView(icon, Color.White, Modifier.size(22.dp))
    }
}

@Composable
private fun DetailIconView(icon: DetailIcon, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val s = minOf(w, h)
        val stroke = Stroke(width = s * 0.085f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (icon) {
            DetailIcon.BACK -> {
                drawLine(tint, Offset(w * 0.27f, h * 0.5f), Offset(w * 0.76f, h * 0.5f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.27f, h * 0.5f), Offset(w * 0.48f, h * 0.3f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.27f, h * 0.5f), Offset(w * 0.48f, h * 0.7f), stroke.width, StrokeCap.Round)
            }
            DetailIcon.SEARCH -> {
                drawCircle(tint, s * 0.2f, Offset(w * 0.45f, h * 0.43f), style = stroke)
                drawLine(tint, Offset(w * 0.6f, h * 0.58f), Offset(w * 0.78f, h * 0.76f), stroke.width, StrokeCap.Round)
            }
            DetailIcon.BELL -> {
                val path = Path().apply {
                    moveTo(w * 0.32f, h * 0.58f)
                    cubicTo(w * 0.34f, h * 0.36f, w * 0.38f, h * 0.25f, w * 0.5f, h * 0.25f)
                    cubicTo(w * 0.62f, h * 0.25f, w * 0.66f, h * 0.36f, w * 0.68f, h * 0.58f)
                    lineTo(w * 0.76f, h * 0.7f)
                    lineTo(w * 0.24f, h * 0.7f)
                    close()
                }
                drawPath(path, tint, style = stroke)
                drawLine(tint, Offset(w * 0.44f, h * 0.8f), Offset(w * 0.56f, h * 0.8f), stroke.width, StrokeCap.Round)
            }
            DetailIcon.MESSAGE -> {
                drawRoundRect(tint, Offset(w * 0.22f, h * 0.28f), androidx.compose.ui.geometry.Size(w * 0.56f, h * 0.38f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.08f), style = stroke)
                val path = Path().apply {
                    moveTo(w * 0.4f, h * 0.66f)
                    lineTo(w * 0.32f, h * 0.8f)
                    lineTo(w * 0.52f, h * 0.66f)
                }
                drawPath(path, tint, style = stroke)
            }
            DetailIcon.SHIELD -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.16f)
                    lineTo(w * 0.77f, h * 0.3f)
                    lineTo(w * 0.7f, h * 0.66f)
                    lineTo(w * 0.5f, h * 0.85f)
                    lineTo(w * 0.3f, h * 0.66f)
                    lineTo(w * 0.23f, h * 0.3f)
                    close()
                }
                drawPath(path, tint)
            }
            DetailIcon.BOLT -> {
                val path = Path().apply {
                    moveTo(w * 0.58f, h * 0.12f)
                    lineTo(w * 0.3f, h * 0.56f)
                    lineTo(w * 0.52f, h * 0.56f)
                    lineTo(w * 0.42f, h * 0.88f)
                    lineTo(w * 0.72f, h * 0.42f)
                    lineTo(w * 0.5f, h * 0.42f)
                    close()
                }
                drawPath(path, tint)
            }
            DetailIcon.DOTS -> {
                drawCircle(tint, s * 0.055f, Offset(w * 0.3f, h * 0.5f))
                drawCircle(tint, s * 0.055f, Offset(w * 0.5f, h * 0.5f))
                drawCircle(tint, s * 0.055f, Offset(w * 0.7f, h * 0.5f))
            }
        }
    }
}
