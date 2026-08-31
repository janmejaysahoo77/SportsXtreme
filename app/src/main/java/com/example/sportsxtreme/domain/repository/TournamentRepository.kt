package com.example.sportsxtreme.domain.repository

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Tournament
import com.example.sportsxtreme.domain.model.TournamentRequirements
import kotlinx.coroutines.flow.Flow

interface TournamentRepository {
    suspend fun createTournament(tournament: Tournament): Resource<Tournament>
    suspend fun updateTournamentRequirements(tournamentId: String, requirements: TournamentRequirements): Resource<Unit>
    suspend fun getTournament(tournamentId: String): Resource<Tournament>
    fun observeHostTournaments(hostUid: String): Flow<Resource<List<Tournament>>>
}
