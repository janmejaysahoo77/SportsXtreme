package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.model.Toss
import com.example.sportsxtreme.domain.repository.MatchRepository

class SaveTossUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(matchId: String, toss: Toss): Resource<Match> {
        return repository.saveToss(matchId, toss)
    }
}
