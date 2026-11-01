package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.Match
import com.sportsxtreme.app.domain.model.PlayingXI
import com.sportsxtreme.app.domain.model.TeamSide
import com.sportsxtreme.app.domain.repository.MatchRepository

class SelectPlayingXIUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(matchId: String, side: TeamSide, playingXI: PlayingXI): Resource<Match> {
        return repository.selectPlayingXI(matchId, side, playingXI)
    }
}
