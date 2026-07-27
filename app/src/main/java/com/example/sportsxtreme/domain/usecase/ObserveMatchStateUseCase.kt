package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.MatchState
import com.example.sportsxtreme.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow

class ObserveMatchStateUseCase(private val repository: MatchRepository) {
    operator fun invoke(matchId: String): Flow<Resource<MatchState>> {
        return repository.observeMatchState(matchId)
    }
}
