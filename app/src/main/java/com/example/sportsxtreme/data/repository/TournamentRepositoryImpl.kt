package com.example.sportsxtreme.data.repository

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Tournament
import com.example.sportsxtreme.domain.repository.TournamentRepository

import javax.inject.Inject
import com.example.sportsxtreme.data.remote.firestore.FirebaseFirestoreTournamentDataSource

class TournamentRepositoryImpl @Inject constructor(
    private val dataSource: FirebaseFirestoreTournamentDataSource
) : TournamentRepository {
    override suspend fun createTournament(tournament: Tournament): Resource<Tournament> {
        return dataSource.createTournament(tournament)
    }
}
