package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.CreatedMatchInvite
import com.sportsxtreme.app.domain.model.TeamSide
import com.sportsxtreme.app.domain.repository.MatchInviteRepository

class CreateMatchInviteUseCase(private val repository: MatchInviteRepository) {
    suspend operator fun invoke(matchId: String, teamSlot: TeamSide): Resource<CreatedMatchInvite> =
        repository.createOrGetOpenInvite(matchId, teamSlot)
}
