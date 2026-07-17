package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Tournament
import com.example.sportsxtreme.domain.repository.TournamentRepository

class CreateTournamentUseCase(private val repository: TournamentRepository) {
    suspend operator fun invoke(tournament: Tournament): Resource<Tournament> {
        return repository.createTournament(tournament)
    }
}
