package com.example.sportsxtreme.data.repository

import androidx.room.withTransaction
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.data.local.dao.PlayerDao
import com.example.sportsxtreme.data.local.dao.TeamDao
import com.example.sportsxtreme.data.local.database.SportsXtremeDatabase
import com.example.sportsxtreme.data.local.entity.PlayerEntity
import com.example.sportsxtreme.data.local.entity.TeamEntity
import com.example.sportsxtreme.data.local.mapper.toDomain
import com.example.sportsxtreme.data.local.mapper.toEntity
import com.example.sportsxtreme.domain.model.Team
import com.example.sportsxtreme.domain.model.TeamType
import com.example.sportsxtreme.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamRepositoryImpl @Inject constructor(
    private val database: SportsXtremeDatabase,
    private val teamDao: TeamDao,
    private val playerDao: PlayerDao
) : TeamRepository {
    override suspend fun saveTeam(team: Team): Resource<Team> = runCatching {
        database.withTransaction {
            teamDao.insertTeam(team.toEntity())
            playerDao.insertPlayers(team.players.map { it.toEntity() })
        }
        Resource.Success(team)
    }.getOrElse { Resource.Error(it.message ?: "Unable to save team") }

    override suspend fun getTeam(teamId: String): Resource<Team> = runCatching {
        val team = requireNotNull(teamDao.getTeam(teamId)) { "Team not found" }
        Resource.Success(team.toDomain(playerDao.getPlayers(teamId)))
    }.getOrElse { Resource.Error(it.message ?: "Unable to load team") }

    override fun observeFriendlyTestTeams(): Flow<Resource<Pair<Team, Team>>> = flow {
        runCatching { database.withTransaction { ensureDummyTeamsExist() } }
            .onFailure { emit(Resource.Error(it.message ?: "Failed to init dummy teams")) }
        emitAll(
            teamDao.observeFriendlyTestTeams().map { teams ->
                runCatching {
                    val teamA = requireNotNull(teams.firstOrNull { it.isTeamA }) { "Team A not found" }
                    val teamB = requireNotNull(teams.firstOrNull { !it.isTeamA }) { "Team B not found" }
                    Resource.Success(
                        teamA.toDomain(playerDao.getPlayers(teamA.teamId)) to
                            teamB.toDomain(playerDao.getPlayers(teamB.teamId))
                    )
                }.getOrElse { Resource.Error(it.message ?: "Unable to load friendly teams") }
            }.catch { e -> emit(Resource.Error(e.message ?: "Unknown error")) }
        )
    }

    override fun observeTeam(teamId: String): Flow<Resource<Team>> = teamDao.observeTeam(teamId).map { team ->
        if (team == null) {
            Resource.Error("Team not found")
        } else {
            runCatching { Resource.Success(team.toDomain(playerDao.getPlayers(teamId))) }
                .getOrElse { Resource.Error(it.message ?: "Unable to observe team") }
        }
    }

    override suspend fun ensureDummyTeamsExist() {
        if (teamDao.observeFriendlyTestTeams().first().size >= 2) return

        val now = System.currentTimeMillis()
        val teamA = TeamEntity(
            teamId = "dummy_team_a",
            teamName = "Team A",
            shortName = "A",
            isTeamA = true,
            type = TeamType.FRIENDLY_TEST.name,
            createdAtEpochMs = now,
            updatedAtEpochMs = now
        )
        val teamB = TeamEntity(
            teamId = "dummy_team_b",
            teamName = "Team B",
            shortName = "B",
            isTeamA = false,
            type = TeamType.FRIENDLY_TEST.name,
            createdAtEpochMs = now,
            updatedAtEpochMs = now
        )
        teamDao.insertTeams(listOf(teamA, teamB))
        playerDao.insertPlayers(dummyPlayers(teamA.teamId, "dA") + dummyPlayers(teamB.teamId, "dB"))
    }

    private fun dummyPlayers(teamId: String, prefix: String): List<PlayerEntity> = (1..15).map { number ->
        PlayerEntity(
            playerId = "$prefix$number",
            teamId = teamId,
            playerName = "Player $number",
            battingOrder = number,
            role = "UNKNOWN"
        )
    }
}
