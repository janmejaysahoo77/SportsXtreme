package com.example.sportsxtreme.data.repository

import androidx.room.withTransaction
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.data.local.dao.InningsDao
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
import com.example.sportsxtreme.domain.model.InningsStatus
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.model.MatchState
import com.example.sportsxtreme.domain.model.MatchStatus
import com.example.sportsxtreme.domain.model.MatchTeam
import com.example.sportsxtreme.domain.model.Overs
import com.example.sportsxtreme.domain.model.PlayingXI
import com.example.sportsxtreme.domain.model.TeamSide
import com.example.sportsxtreme.domain.model.Toss
import com.example.sportsxtreme.domain.model.TossDecision
import com.example.sportsxtreme.domain.model.TeamType
import com.example.sportsxtreme.domain.repository.CreateMatchRequest
import com.example.sportsxtreme.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepositoryImpl @Inject constructor(
    private val database: SportsXtremeDatabase,
    private val matchDao: MatchDao,
    private val teamDao: TeamDao,
    private val playerDao: PlayerDao,
    private val inningsDao: InningsDao
) : MatchRepository {
    override suspend fun createMatch(request: CreateMatchRequest): Resource<Match> = runCatching {
        val matchId = UUID.randomUUID().toString()
        database.withTransaction {
            ensureMatchTeam(request.teamA)
            ensureMatchTeam(request.teamB)
            matchDao.insertMatch(request.toEntity(matchId))
        }
        Resource.Success(loadMatch(matchId))
    }.getOrElse { Resource.Error(it.message ?: "Unable to create match") }

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
        Resource.Success(loadMatch(matchId))
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
        Resource.Success(loadMatch(matchId))
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
        Resource.Success(matchStateFor(matchId))
    }.getOrElse { Resource.Error(it.message ?: "Unable to finish innings") }

    override suspend fun finishMatch(matchId: String): Resource<Match> = runCatching {
        database.withTransaction {
            val match = requireMatch(matchId)
            matchDao.updateMatch(match.copy(status = MatchStatus.COMPLETED.name, updatedAtEpochMs = System.currentTimeMillis()))
        }
        Resource.Success(loadMatch(matchId))
    }.getOrElse { Resource.Error(it.message ?: "Unable to finish match") }

    override fun observeMatch(matchId: String): Flow<Resource<Match>> = matchDao.observeMatch(matchId).map { match ->
        if (match == null) Resource.Error("Match not found")
        else runCatching { Resource.Success(loadMatch(matchId)) }
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
        return MatchState(
            matchId = matchId,
            inningsId = latestInnings?.inningsId,
            matchStatus = MatchStatus.valueOf(match.status),
            battingTeam = battingTeam,
            bowlingTeam = bowlingTeam,
            striker = match.strikerId?.let { playerDao.getPlayer(it)?.toDomain() },
            nonStriker = match.nonStrikerId?.let { playerDao.getPlayer(it)?.toDomain() },
            bowler = match.currentBowlerId?.let { playerDao.getPlayer(it)?.toDomain() },
            score = latestInnings?.score ?: 0,
            wickets = latestInnings?.wickets ?: 0,
            legalBalls = legalBalls,
            overs = Overs(legalBalls / 6, legalBalls % 6),
            currentOverEvents = emptyList(),
            target = latestInnings?.target,
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
