package com.sportsxtreme.app.domain.scoring

import com.sportsxtreme.app.domain.model.BallEvent
import com.sportsxtreme.app.domain.model.MatchState

fun interface MatchStateReducer {
    fun reduce(currentState: MatchState, event: BallEvent): MatchState
}
