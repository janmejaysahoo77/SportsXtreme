package com.example.sportsxtreme.data.repository

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Tournament
import com.example.sportsxtreme.domain.repository.TournamentRepository

class TournamentRepositoryImpl : TournamentRepository {
    override suspend fun createTournament(tournament: Tournament): Resource<Tournament> {
        return Resource.Success(tournament)
    }
}
