package com.example.sportsxtreme.domain.repository

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.CreatedMatchInvite
import com.example.sportsxtreme.domain.model.ClaimedMatchInvite
import com.example.sportsxtreme.domain.model.TeamSide

interface MatchInviteRepository {
    suspend fun createOrGetOpenInvite(matchId: String, teamSlot: TeamSide): Resource<CreatedMatchInvite>
    suspend fun claimInvite(rawToken: String): Resource<ClaimedMatchInvite>
}
