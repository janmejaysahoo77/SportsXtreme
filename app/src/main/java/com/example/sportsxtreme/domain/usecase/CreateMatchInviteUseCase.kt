package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.CreatedMatchInvite
import com.example.sportsxtreme.domain.model.TeamSide
import com.example.sportsxtreme.domain.repository.MatchInviteRepository

class CreateMatchInviteUseCase(private val repository: MatchInviteRepository) {
    suspend operator fun invoke(matchId: String, teamSlot: TeamSide): Resource<CreatedMatchInvite> =
        repository.createOrGetOpenInvite(matchId, teamSlot)
}
