package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.repository.MatchRepository

class StartMatchUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(match: Match): Resource<Match> {
        return repository.startMatch(match)
    }
}
