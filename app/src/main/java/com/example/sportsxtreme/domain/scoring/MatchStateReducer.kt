package com.example.sportsxtreme.domain.scoring

import com.example.sportsxtreme.domain.model.BallEvent
import com.example.sportsxtreme.domain.model.MatchState

fun interface MatchStateReducer {
    fun reduce(currentState: MatchState, event: BallEvent): MatchState
}
