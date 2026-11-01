package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.MatchState
import com.sportsxtreme.app.domain.repository.MatchRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveMatchStateUseCase @Inject constructor(private val repository: MatchRepository) {
    operator fun invoke(matchId: String): Flow<Resource<MatchState>> {
        return repository.observeMatchState(matchId)
    }
}
