package com.example.sportsxtreme.data.repository

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.data.local.dao.BallEventDao
import com.example.sportsxtreme.data.local.dao.InningsDao
import com.example.sportsxtreme.data.local.dao.MatchDao
import com.example.sportsxtreme.data.local.dao.PlayerDao
import com.example.sportsxtreme.data.local.dao.TeamDao
import com.example.sportsxtreme.data.local.mapper.toDomain
import com.example.sportsxtreme.data.local.mapper.toEntity
import com.example.sportsxtreme.data.local.mapper.toMatchTeam
import com.example.sportsxtreme.domain.model.BallEvent
import com.example.sportsxtreme.domain.model.MatchState
import com.example.sportsxtreme.domain.model.MatchStatus
import com.example.sportsxtreme.domain.model.Overs
import com.example.sportsxtreme.domain.model.TeamSide
import com.example.sportsxtreme.domain.repository.ScoringRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScoringRepositoryImpl @Inject constructor(
    private val matchDao: MatchDao,
    private val inningsDao: InningsDao,
    private val ballEventDao: BallEventDao,
    private val teamDao: TeamDao,
    private val playerDao: PlayerDao
) : ScoringRepository {
    override suspend fun recordBall(event: BallEvent): Resource<MatchState> = runCatching {
        val innings = requireNotNull(inningsDao.getInnings(event.inningsId)) { "Innings not found" }
        require(innings.matchId == event.matchId) { "Ball event does not belong to this innings" }
        ballEventDao.insertBall(event.toEntity(innings.number))
        Resource.Success(matchStateFor(event.matchId))
    }.getOrElse { Resource.Error(it.message ?: "Unable to save ball event") }

    override suspend fun undoLastBall(
        matchId: String,
        requestedByUserId: String,
        reason: String?
    ): Resource<MatchState> = runCatching {
        ballEventDao.deleteLastBall(matchId)
        Resource.Success(matchStateFor(matchId))
    }.getOrElse { Resource.Error(it.message ?: "Unable to undo ball event") }

    override fun observeBallEvents(matchId: String, inningsId: String): Flow<Resource<List<BallEvent>>> =
        ballEventDao.observeBallEvents(matchId, inningsId).map { events ->
            runCatching { Resource.Success(events.map { it.toDomain() }) }
                .getOrElse { Resource.Error(it.message ?: "Unable to observe ball events") }
        }

    private suspend fun matchStateFor(matchId: String): MatchState {
        val match = requireNotNull(matchDao.getMatch(matchId)) { "Match not found" }
        val innings = inningsDao.getLatestInnings(matchId)
        val legalBalls = innings?.legalBalls ?: 0
        return MatchState(
            matchId = matchId,
            inningsId = innings?.inningsId,
            matchStatus = MatchStatus.valueOf(match.status),
            battingTeam = match.battingTeamId?.let { teamId -> teamDao.getTeam(teamId)?.toMatchTeam(
                if (teamId == match.teamAId) TeamSide.TEAM_A else TeamSide.TEAM_B
            ) },
            bowlingTeam = match.bowlingTeamId?.let { teamId -> teamDao.getTeam(teamId)?.toMatchTeam(
                if (teamId == match.teamAId) TeamSide.TEAM_A else TeamSide.TEAM_B
            ) },
            striker = match.strikerId?.let { playerDao.getPlayer(it)?.toDomain() },
            nonStriker = match.nonStrikerId?.let { playerDao.getPlayer(it)?.toDomain() },
            bowler = match.currentBowlerId?.let { playerDao.getPlayer(it)?.toDomain() },
            score = innings?.score ?: 0,
            wickets = innings?.wickets ?: 0,
            legalBalls = legalBalls,
            overs = Overs(legalBalls / 6, legalBalls % 6),
            currentOverEvents = emptyList(),
            target = innings?.target,
            updatedAtEpochMs = match.updatedAtEpochMs
        )
    }
}
