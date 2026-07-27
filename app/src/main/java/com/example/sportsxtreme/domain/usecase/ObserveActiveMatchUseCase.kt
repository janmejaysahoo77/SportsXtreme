package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow

class ObserveActiveMatchUseCase(private val repository: MatchRepository) {
    operator fun invoke(): Flow<Resource<Match>> = repository.observeActiveMatch()
}
