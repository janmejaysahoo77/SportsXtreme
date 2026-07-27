package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.BallEvent
import com.example.sportsxtreme.domain.model.MatchState
import com.example.sportsxtreme.domain.repository.ScoringRepository

class RecordBallUseCase(private val repository: ScoringRepository) {
    suspend operator fun invoke(event: BallEvent): Resource<MatchState> {
        return repository.recordBall(event)
    }
}
