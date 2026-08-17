package com.example.sportsxtreme.domain.model

data class MatchInvite(
    val inviteId: String,
    val matchId: String,
    val teamSlot: TeamSide,
    val organiserId: String,
    val tokenHash: String,
    val status: MatchInviteStatus,
    val createdAtEpochMs: Long,
    val expiresAtEpochMs: Long,
    val claimedByUserId: String? = null,
    val claimedAtEpochMs: Long? = null
)

enum class MatchInviteStatus { OPEN, CLAIMED, EXPIRED, CANCELLED }

data class CreatedMatchInvite(
    val invite: MatchInvite,
    val invitationUrl: String
)

data class ClaimedMatchInvite(
    val matchId: String,
    val teamSlot: TeamSide
)
