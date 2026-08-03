package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.MatchState
import com.example.sportsxtreme.domain.repository.MatchRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveMatchStateUseCase @Inject constructor(private val repository: MatchRepository) {
    operator fun invoke(matchId: String): Flow<Resource<MatchState>> {
        return repository.observeMatchState(matchId)
    }
}
