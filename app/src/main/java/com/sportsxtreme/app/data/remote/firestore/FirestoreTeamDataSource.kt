package com.sportsxtreme.app.data.remote.firestore

import com.sportsxtreme.app.domain.model.Team
import kotlinx.coroutines.flow.Flow

interface FirestoreTeamDataSource {
    suspend fun saveTeam(team: Team): Team
    suspend fun getTeam(teamId: String): Team
    fun observeFriendlyTestTeams(): Flow<Pair<Team, Team>>
    fun observeTeam(teamId: String): Flow<Team>
}
