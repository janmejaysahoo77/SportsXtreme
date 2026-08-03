package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.InningsScorecard
import com.example.sportsxtreme.domain.repository.ScoringRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveScorecardUseCase @Inject constructor(
    private val repository: ScoringRepository
) {
    operator fun invoke(matchId: String, inningsId: String): Flow<Resource<InningsScorecard>> =
        repository.observeScorecard(matchId, inningsId)
}
