package com.example.sportsxtreme.data.repository

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Tournament
import com.example.sportsxtreme.domain.model.TournamentRequirements
import com.example.sportsxtreme.domain.repository.TournamentRepository

import javax.inject.Inject
import com.example.sportsxtreme.data.remote.firestore.FirebaseFirestoreTournamentDataSource

class TournamentRepositoryImpl @Inject constructor(
    private val dataSource: FirebaseFirestoreTournamentDataSource
) : TournamentRepository {
    override suspend fun createTournament(tournament: Tournament): Resource<Tournament> {
        return dataSource.createTournament(tournament)
    }

    override suspend fun updateTournamentRequirements(tournamentId: String, requirements: TournamentRequirements): Resource<Unit> =
        dataSource.updateTournamentRequirements(tournamentId, requirements)

    override suspend fun getTournament(tournamentId: String): Resource<Tournament> = dataSource.getTournament(tournamentId)
}
