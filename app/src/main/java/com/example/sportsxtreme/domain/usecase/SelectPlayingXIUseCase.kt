package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.model.PlayingXI
import com.example.sportsxtreme.domain.model.TeamSide
import com.example.sportsxtreme.domain.repository.MatchRepository

class SelectPlayingXIUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(matchId: String, side: TeamSide, playingXI: PlayingXI): Resource<Match> {
        return repository.selectPlayingXI(matchId, side, playingXI)
    }
}
