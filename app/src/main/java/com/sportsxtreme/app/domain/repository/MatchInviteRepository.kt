package com.sportsxtreme.app.domain.repository

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.CreatedMatchInvite
import com.sportsxtreme.app.domain.model.ClaimedMatchInvite
import com.sportsxtreme.app.domain.model.TeamSide

interface MatchInviteRepository {
    suspend fun createOrGetOpenInvite(matchId: String, teamSlot: TeamSide): Resource<CreatedMatchInvite>
    suspend fun claimInvite(rawToken: String): Resource<ClaimedMatchInvite>
}
