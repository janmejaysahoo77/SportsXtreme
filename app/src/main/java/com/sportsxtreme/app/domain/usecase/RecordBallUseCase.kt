package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.BallEvent
import com.sportsxtreme.app.domain.model.MatchState
import com.sportsxtreme.app.domain.repository.ScoringRepository
import javax.inject.Inject

class RecordBallUseCase @Inject constructor(private val repository: ScoringRepository) {
    suspend operator fun invoke(event: BallEvent): Resource<MatchState> {
        return repository.recordBall(event)
    }
}
