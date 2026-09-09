package com.example.sportsxtreme.data.repository

import androidx.room.withTransaction
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.data.local.dao.InningsDao
import com.example.sportsxtreme.data.local.dao.BallEventDao
import com.example.sportsxtreme.data.local.dao.MatchDao
import com.example.sportsxtreme.data.local.dao.PlayerDao
import com.example.sportsxtreme.data.local.dao.TeamDao
import com.example.sportsxtreme.data.local.database.SportsXtremeDatabase
import com.example.sportsxtreme.data.local.entity.InningsEntity
import com.example.sportsxtreme.data.local.entity.PlayerEntity
import com.example.sportsxtreme.data.local.entity.TeamEntity
import com.example.sportsxtreme.data.local.mapper.toDomain
import com.example.sportsxtreme.data.local.mapper.toEntities
import com.example.sportsxtreme.data.local.mapper.toEntity
import com.example.sportsxtreme.data.local.mapper.toMatchTeam
import com.example.sportsxtreme.data.remote.firestore.FirebaseFirestoreMatchSyncDataSource
import com.example.sportsxtreme.data.remote.firestore.FirebaseFirestoreMatchClaimsDataSource
import com.example.sportsxtreme.data.remote.firestore.FirestoreScoringDataSource
import com.example.sportsxtreme.domain.model.InningsStatus
import com.example.sportsxtreme.domain.model.BallType
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.model.MatchFormat
import com.example.sportsxtreme.domain.model.MatchState
import com.example.sportsxtreme.domain.model.MatchStatus
import com.example.sportsxtreme.domain.model.MatchTeam
import com.example.sportsxtreme.domain.model.LiveScorePayload
import com.example.sportsxtreme.domain.model.Overs
import com.example.sportsxtreme.domain.model.PlayingXI
import com.example.sportsxtreme.domain.model.TeamSide
import com.example.sportsxtreme.domain.model.Toss
import com.example.sportsxtreme.domain.model.TossDecision
import com.example.sportsxtreme.domain.model.TeamType
import com.example.sportsxtreme.domain.repository.CreateMatchRequest
import com.example.sportsxtreme.domain.repository.MatchRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class MatchRepositoryImpl @Inject constructor(
    private val database: SportsXtremeDatabase,
    private val matchDao: MatchDao,
    private val teamDao: TeamDao,
    private val playerDao: PlayerDao,
    private val inningsDao: InningsDao,
    private val ballEventDao: BallEventDao,
    private val firestoreMatchSyncDataSource: FirebaseFirestoreMatchSyncDataSource,
    private val firestoreMatchClaimsDataSource: FirebaseFirestoreMatchClaimsDataSource,
    private val firestoreScoringDataSource: FirestoreScoringDataSource,
    private val firestore: FirebaseFirestore
) : MatchRepository {
    override suspend fun createMatch(request: CreateMatchRequest): Resource<Match> = runCatching {
        val matchId = UUID.randomUUID().toString()
        database.withTransaction {
            ensureMatchTeam(request.teamA)
            ensureMatchTeam(request.teamB)
            matchDao.insertMatch(request.toEntity(matchId))
        }
        Resource.Success(loadAndSyncMatch(matchId))
    }.getOrElse { Resource.Error(it.message ?: "Unable to create match") }

    override suspend fun updateMatchSettings(
        matchId: String,
        format: MatchFormat,
        ballType: BallType,
        overs: Int
    ): Resource<Match> = runCatching {
        database.withTransaction {
            val match = requireMatch(matchId)
            matchDao.updateMatch(
                match.copy(
                    format = format.name,
                    ballType = ballType.name,
                    overs = overs,
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
        }
        Resource.Success(loadAndSyncMatch(matchId))
    }.getOrElse { Resource.Error(it.message ?: "Unable to update match settings") }

    override suspend fun updateMatchDetails(
        matchId: String,
        venue: String,
        matchDateEpochMs: Long,
        matchTime: String
    ): Resource<Match> = runCatching {
        database.withTransaction {
            val match = requireMatch(matchId)
            matchDao.updateMatch(
                match.copy(
                    venue = venue,
                    matchDateEpochMs = matchDateEpochMs,
                    matchTime = matchTime,
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
        }
        Resource.Success(loadAndSyncMatch(matchId))
    }.getOrElse { Resource.Error(it.message ?: "Unable to update match details") }

    override suspend fun updateMatchTeams(
        matchId: String,
        teamAId: String,
        teamBId: String
    ): Resource<Match> = runCatching {
        require(teamAId != teamBId) { "Team A and Team B cannot be the same team" }
        // Team selection now comes from the user's Firestore teams. Hydrate those teams
        // locally before the existing match engine validates and starts the match.
        syncFirestoreTeamIfNeeded(teamAId)
        syncFirestoreTeamIfNeeded(teamBId)
        database.withTransaction {
            val match = requireMatch(matchId)
            val teamA = requireNotNull(teamDao.getTeam(teamAId)) { "Team A not found" }
            val teamB = requireNotNull(teamDao.getTeam(teamBId)) { "Team B not found" }
            matchDao.updateMatch(
                match.copy(
                    teamAId = teamAId,
                    teamBId = teamBId,
                    status = MatchStatus.TEAM_SELECTION.name,
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
        }
        Resource.Success(loadAndSyncMatch(matchId))
    }.getOrElse { Resource.Error(it.message ?: "Unable to update match teams") }

    private suspend fun syncFirestoreTeamIfNeeded(teamId: String) {
        val document = firestore.collection("teams").document(teamId).get().await()
        if (!document.exists()) {
            // Local fixture teams are valid, but a selected cloud team must exist remotely.
            require(teamDao.getTeam(teamId) != null) { "Selected team is no longer available" }
            return
        }
        val teamName = document.getString("teamName").orEmpty()
            .ifBlank { document.getString("name").orEmpty().ifBlank { teamId } }
        val members = buildList {
            (document.get("members") as? List<*>).orEmpty()
            .mapNotNull { it as? Map<*, *> }
            .forEach { member ->
                val userId = member["userId"] as? String ?: return@forEach
                val role = (member["playingRole"] as? String).orEmpty()
                    .uppercase().replace(' ', '_').let { value ->
                        if (value in setOf("BATTER", "BOWLER", "ALL_ROUNDER", "WICKET_KEEPER")) value else "UNKNOWN"
                    }
                val profile = runCatching { firestore.collection("users").document(userId).get().await() }.getOrNull()
                val displayName = (member["displayName"] as? String).orEmpty()
                    .ifBlank { (member["name"] as? String).orEmpty() }
                    .ifBlank { profile?.getString("name").orEmpty() }
                    .ifBlank { profile?.getString("displayName").orEmpty() }
                    .ifBlank { "Team member" }
                add(PlayerEntity(
                    // A user can legitimately be a member of both teams; player IDs must
                    // therefore be scoped to their team inside the local match engine.
                    playerId = "$teamId::$userId",
                    teamId = teamId,
                    playerName = displayName,
                    linkedUserId = userId,
                    role = role,
                    isGuestPlayer = false
                ))
            }
        }
        val now = System.currentTimeMillis()
        database.withTransaction {
            teamDao.insertTeam(
                TeamEntity(
                    teamId = teamId,
                    teamName = teamName,
                    shortName = document.getString("shortName").orEmpty().ifBlank { teamName.take(3).uppercase() },
                    type = TeamType.USER_CREATED.name,
                    ownerUserId = document.getString("ownerUserId"),
                    createdAtEpochMs = (document.get("createdAtEpochMs") as? Number)?.toLong() ?: now,
                    updatedAtEpochMs = now
                )
            )
            playerDao.deletePlayersForTeam(teamId)
            playerDao.insertPlayers(members)
        }
    }

    override suspend fun selectPlayingXI(
        matchId: String,
        side: TeamSide,
        playingXI: PlayingXI
    ): Resource<Match> = runCatching {
        database.withTransaction {
            val match = requireMatch(matchId)
            val expectedTeamId = if (side == TeamSide.TEAM_A) match.teamAId else match.teamBId
            require(playingXI.teamId == expectedTeamId) { "Playing XI does not belong to $side" }

            playerDao.clearPlayingXI(matchId, expectedTeamId)
            playerDao.insertPlayingXI(playingXI.copy(side = side).toEntities(matchId))
            val selectedOrder = playingXI.playerIds.withIndex().associate { it.value to it.index + 1 }
            playerDao.insertPlayers(playerDao.getPlayers(expectedTeamId).map { player ->
                player.copy(
                    playingXI = player.playerId in selectedOrder,
                    battingOrder = selectedOrder[player.playerId] ?: player.battingOrder
                )
            })
            matchDao.updateMatch(
                match.copy(
                    status = if (side == TeamSide.TEAM_A) {
                        MatchStatus.TEAM_A_PLAYING_XI_SELECTED.name
                    } else {
                        MatchStatus.TEAM_B_PLAYING_XI_SELECTED.name
                    },
                    updatedAtEpochMs = playingXI.selectedAtEpochMs
                )
            )
        }
        Resource.Success(loadAndSyncMatch(matchId))
    }.getOrElse { Resource.Error(it.message ?: "Unable to save playing XI") }

    override suspend fun saveToss(matchId: String, toss: Toss): Resource<Match> = runCatching {
        database.withTransaction {
            val match = requireMatch(matchId)
            require(toss.winnerTeamId == match.teamAId || toss.winnerTeamId == match.teamBId) {
                "Toss winner is not part of this match"
            }
            val otherTeamId = if (toss.winnerTeamId == match.teamAId) match.teamBId else match.teamAId
            val battingTeamId = if (toss.decision == TossDecision.BAT) toss.winnerTeamId else otherTeamId
            val bowlingTeamId = if (battingTeamId == match.teamAId) match.teamBId else match.teamAId
            matchDao.updateMatch(
                match.copy(
                    status = MatchStatus.TOSS_COMPLETED.name,
                    battingTeamId = battingTeamId,
                    bowlingTeamId = bowlingTeamId,
                    tossWinner = toss.winnerTeamId,
                    tossDecision = toss.decision.name,
                    tossCompletedBy = toss.completedByUserId,
                    tossCompletedAtEpochMs = toss.completedAtEpochMs,
                    updatedAtEpochMs = toss.completedAtEpochMs
                )
            )
        }
        Resource.Success(loadAndSyncMatch(matchId))
    }.getOrElse { Resource.Error(it.message ?: "Unable to save toss") }

    override suspend fun selectOpeningPlayers(
        matchId: String,
        strikerId: String,
        nonStrikerId: String,
        bowlerId: String
    ): Resource<MatchState> = runCatching {
        database.withTransaction {
            val match = requireMatch(matchId)
            require(strikerId != nonStrikerId) { "Opening batters must be different players" }
            val battingTeamId = requireNotNull(match.battingTeamId) { "Toss must be completed first" }
            val bowlingTeamId = requireNotNull(match.bowlingTeamId) { "Toss must be completed first" }
            val striker = requireNotNull(playerDao.getPlayer(strikerId)) { "Selected striker was not found" }
            val nonStriker = requireNotNull(playerDao.getPlayer(nonStrikerId)) { "Selected non-striker was not found" }
            val bowler = requireNotNull(playerDao.getPlayer(bowlerId)) { "Selected bowler was not found" }
            require(striker.teamId == battingTeamId) { "Striker must belong to the batting team" }
            require(nonStriker.teamId == battingTeamId) { "Non-striker must belong to the batting team" }
            require(bowler.teamId == bowlingTeamId) { "Bowler must belong to the bowling team" }
            val battingUserIds = setOfNotNull(striker.linkedUserId, nonStriker.linkedUserId)
            require(bowler.linkedUserId !in battingUserIds) {
                "A player can represent only one team in this match"
            }
            matchDao.updateMatch(
                match.copy(
                    status = MatchStatus.OPENERS_SELECTED.name,
                    strikerId = strikerId,
                    nonStrikerId = nonStrikerId,
                    currentBowlerId = bowlerId,
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
        }
        loadAndSyncMatch(matchId)
        Resource.Success(matchStateFor(matchId))
    }.getOrElse { Resource.Error(it.message ?: "Unable to save opening players") }

    override suspend fun startMatch(matchId: String): Resource<MatchState> = runCatching {
        database.withTransaction {
            val match = requireMatch(matchId)
            val battingTeamId = requireNotNull(match.battingTeamId) { "Toss must be completed first" }
            val bowlingTeamId = requireNotNull(match.bowlingTeamId) { "Toss must be completed first" }
            val now = System.currentTimeMillis()
            if (inningsDao.getLatestInnings(matchId) == null) {
                inningsDao.insertInnings(
                    InningsEntity(
                        inningsId = "${matchId}_innings_1",
                        matchId = matchId,
                        number = 1,
                        battingTeamId = battingTeamId,
                        bowlingTeamId = bowlingTeamId,
                        strikerId = match.strikerId,
                        nonStrikerId = match.nonStrikerId,
                        currentBowlerId = match.currentBowlerId,
                        status = InningsStatus.LIVE.name,
                        startedAtEpochMs = now
                    )
                )
            }
            matchDao.updateMatch(match.copy(status = MatchStatus.LIVE.name, updatedAtEpochMs = now))
        }
        val liveMatch = loadAndSyncMatch(matchId)
        firestoreScoringDataSource.syncLiveScore(matchId, liveMatch.toInitialLiveScore())
        Resource.Success(matchStateFor(matchId))
    }.getOrElse { Resource.Error(it.message ?: "Unable to start match") }

    override suspend fun finishInnings(matchId: String, inningsId: String): Resource<MatchState> = runCatching {
        database.withTransaction {
            val match = requireMatch(matchId)
            val innings = requireNotNull(inningsDao.getInnings(inningsId)) { "Innings not found" }
            require(innings.matchId == matchId) { "Innings does not belong to this match" }
            val now = System.currentTimeMillis()
            inningsDao.updateInnings(innings.copy(status = InningsStatus.COMPLETED.name, completedAtEpochMs = now))
            matchDao.updateMatch(match.copy(status = MatchStatus.INNINGS_BREAK.name, updatedAtEpochMs = now))
        }
        loadAndSyncMatch(matchId)
        Resource.Success(matchStateFor(matchId))
    }.getOrElse { Resource.Error(it.message ?: "Unable to finish innings") }

    override suspend fun finishMatch(matchId: String): Resource<Match> = runCatching {
        database.withTransaction {
            val match = requireMatch(matchId)
            matchDao.updateMatch(match.copy(status = MatchStatus.COMPLETED.name, updatedAtEpochMs = System.currentTimeMillis()))
        }
        Resource.Success(loadAndSyncMatch(matchId))
    }.getOrElse { Resource.Error(it.message ?: "Unable to finish match") }

    override fun observeMatch(matchId: String): Flow<Resource<Match>> = combine(matchDao.observeMatch(matchId), firestoreMatchClaimsDataSource.observe(matchId)) { match, claims ->
        if (match == null) Resource.Error("Match not found")
        else runCatching { Resource.Success(loadMatch(matchId).copy(teamAClaim = claims.teamA, teamBClaim = claims.teamB)) }
            .getOrElse { Resource.Error(it.message ?: "Unable to observe match") }
    }

    override fun observeActiveMatch(): Flow<Resource<Match>> = matchDao.observeActiveMatch().map { match ->
        if (match == null) Resource.Error("No unfinished match")
        else runCatching { Resource.Success(loadMatch(match.matchId)) }
            .getOrElse { Resource.Error(it.message ?: "Unable to observe active match") }
    }

    override fun observeMatchState(matchId: String): Flow<Resource<MatchState>> = matchDao.observeMatch(matchId).map { match ->
        if (match == null) Resource.Error("Match not found")
        else runCatching { Resource.Success(matchStateFor(matchId)) }
            .getOrElse { Resource.Error(it.message ?: "Unable to observe match state") }
    }

    private suspend fun requireMatch(matchId: String) = requireNotNull(matchDao.getMatch(matchId)) {
        "Match not found"
    }

    private suspend fun loadMatch(matchId: String): Match {
        val match = requireMatch(matchId)
        val teamA = requireNotNull(teamDao.getTeam(match.teamAId)) { "Team A not found" }
        val teamB = requireNotNull(teamDao.getTeam(match.teamBId)) { "Team B not found" }
        return match.toDomain(
            teamA = teamA,
            teamB = teamB,
            teamAXI = playerDao.getPlayingXI(matchId, match.teamAId),
            teamBXI = playerDao.getPlayingXI(matchId, match.teamBId),
            innings = inningsDao.getInningsForMatch(matchId).map { it.toDomain() }
        )
    }

    private suspend fun loadAndSyncMatch(matchId: String): Match {
        val match = loadMatch(matchId)
        firestoreMatchSyncDataSource.sync(match).getOrThrow()
        return match
    }

    private suspend fun Match.toInitialLiveScore(): LiveScorePayload {
        val currentInnings = innings.lastOrNull()
        val legalBalls = currentInnings?.legalBalls ?: 0
        val score = currentInnings?.score ?: 0
        val striker = currentInnings?.strikerId?.let { playerDao.getPlayer(it) }
        val nonStriker = currentInnings?.nonStrikerId?.let { playerDao.getPlayer(it) }
        val bowler = currentInnings?.currentBowlerId?.let { playerDao.getPlayer(it) }

        return LiveScorePayload(
            matchId = id,
            tournamentName = title,
            teamAId = teamA.teamId,
            teamBId = teamB.teamId,
            battingTeamId = currentInnings?.battingTeamId,
            teamAName = teamA.name,
            teamBName = teamB.name,
            teamAShortName = teamA.shortName,
            teamBShortName = teamB.shortName,
            status = status.name,
            score = score,
            wickets = currentInnings?.wickets ?: 0,
            overs = Overs(legalBalls / 6, legalBalls % 6).display,
            currentRunRate = if (legalBalls == 0) 0.0 else score * 6.0 / legalBalls,
            requiredRunRate = null,
            target = currentInnings?.target,
            strikerName = striker?.playerName,
            strikerRuns = 0,
            strikerBalls = 0,
            nonStrikerName = nonStriker?.playerName,
            bowlerName = bowler?.playerName,
            bowlerOvers = "0.0",
            bowlerRuns = 0,
            bowlerWickets = 0,
            matchStatusNote = "Powerplay",
            updatedAtEpochMs = updatedAtEpochMs
        )
    }

    private suspend fun matchStateFor(matchId: String): MatchState {
        val match = requireMatch(matchId)
        val latestInnings = inningsDao.getLatestInnings(matchId)
        val battingTeam = match.battingTeamId?.let { teamId -> teamDao.getTeam(teamId)?.toMatchTeam(
            if (teamId == match.teamAId) TeamSide.TEAM_A else TeamSide.TEAM_B
        ) }
        val bowlingTeam = match.bowlingTeamId?.let { teamId -> teamDao.getTeam(teamId)?.toMatchTeam(
            if (teamId == match.teamAId) TeamSide.TEAM_A else TeamSide.TEAM_B
        ) }
        val legalBalls = latestInnings?.legalBalls ?: 0
        val currentOverEvents = latestInnings?.let { innings ->
            val events = ballEventDao.getBallEvents(matchId, innings.inningsId).map { it.toDomain() }
            events.takeLastWhile { it.overNumber == events.lastOrNull()?.overNumber }
        }.orEmpty()
        return MatchState(
            matchId = matchId,
            inningsId = latestInnings?.inningsId,
            matchStatus = MatchStatus.valueOf(match.status),
            battingTeam = battingTeam,
            bowlingTeam = bowlingTeam,
            striker = latestInnings?.strikerId?.let { playerDao.getPlayer(it)?.toDomain() },
            nonStriker = latestInnings?.nonStrikerId?.let { playerDao.getPlayer(it)?.toDomain() },
            bowler = latestInnings?.currentBowlerId?.let { playerDao.getPlayer(it)?.toDomain() },
            score = latestInnings?.score ?: 0,
            wickets = latestInnings?.wickets ?: 0,
            legalBalls = legalBalls,
            overs = Overs(legalBalls / 6, legalBalls % 6),
            currentOverEvents = currentOverEvents,
            target = latestInnings?.target,
            currentInnings = latestInnings?.toDomain(),
            updatedAtEpochMs = match.updatedAtEpochMs
        )
    }

    private suspend fun ensureMatchTeam(team: MatchTeam) {
        if (teamDao.getTeam(team.teamId) != null) return

        val now = System.currentTimeMillis()
        val isTeamA = team.side == TeamSide.TEAM_A
        val prefix = if (isTeamA) "dA" else "dB"
        teamDao.insertTeam(
            TeamEntity(
                teamId = team.teamId,
                teamName = team.name,
                shortName = team.shortName,
                isTeamA = isTeamA,
                type = TeamType.FRIENDLY_TEST.name,
                createdAtEpochMs = now,
                updatedAtEpochMs = now
            )
        )
        playerDao.insertPlayers((1..15).map { number ->
            PlayerEntity(
                playerId = "$prefix$number",
                teamId = team.teamId,
                playerName = "Player $number",
                battingOrder = number,
                role = "UNKNOWN"
            )
        })
    }
}
