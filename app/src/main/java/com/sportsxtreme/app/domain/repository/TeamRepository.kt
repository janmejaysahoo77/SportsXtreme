package com.sportsxtreme.app.domain.repository

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.Team
import kotlinx.coroutines.flow.Flow

interface TeamRepository {
    suspend fun saveTeam(team: Team): Resource<Team>
    suspend fun getTeam(teamId: String): Resource<Team>
    fun observeFriendlyTestTeams(): Flow<Resource<Pair<Team, Team>>>
    suspend fun ensureDummyTeamsExist()
    fun observeTeam(teamId: String): Flow<Resource<Team>>
}
