package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.Tournament
import com.sportsxtreme.app.domain.repository.TournamentRepository

class CreateTournamentUseCase(private val repository: TournamentRepository) {
    suspend operator fun invoke(tournament: Tournament): Resource<Tournament> {
        return repository.createTournament(tournament)
    }
}
