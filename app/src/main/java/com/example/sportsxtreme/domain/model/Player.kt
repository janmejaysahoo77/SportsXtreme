package com.example.sportsxtreme.domain.model

data class Player(
    val id: String,
    val teamId: String,
    val displayName: String,
    val jerseyNumber: Int? = null,
    val linkedUserId: String? = null,
    val role: PlayerRole = PlayerRole.UNKNOWN,
    val isGuestPlayer: Boolean = true
)

enum class PlayerRole {
    BATTER,
    BOWLER,
    ALL_ROUNDER,
    WICKET_KEEPER,
    UNKNOWN
}
