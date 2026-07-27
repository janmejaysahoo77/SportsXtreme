package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.repository.CreateMatchRequest
import com.example.sportsxtreme.domain.repository.MatchRepository

class CreateMatchUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(request: CreateMatchRequest): Resource<Match> {
        return repository.createMatch(request)
    }
}
