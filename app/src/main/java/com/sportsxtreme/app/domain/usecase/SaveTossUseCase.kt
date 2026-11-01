package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.Match
import com.sportsxtreme.app.domain.model.Toss
import com.sportsxtreme.app.domain.repository.MatchRepository

class SaveTossUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(matchId: String, toss: Toss): Resource<Match> {
        return repository.saveToss(matchId, toss)
    }
}
