package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.InningsScorecard
import com.sportsxtreme.app.domain.repository.ScoringRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveScorecardUseCase @Inject constructor(
    private val repository: ScoringRepository
) {
    operator fun invoke(matchId: String, inningsId: String): Flow<Resource<InningsScorecard>> =
        repository.observeScorecard(matchId, inningsId)
}
