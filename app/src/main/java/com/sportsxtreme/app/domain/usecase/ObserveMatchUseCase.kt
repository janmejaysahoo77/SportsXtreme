package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.Match
import com.sportsxtreme.app.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow

class ObserveMatchUseCase(private val repository: MatchRepository) {
    operator fun invoke(matchId: String): Flow<Resource<Match>> = repository.observeMatch(matchId)
}
