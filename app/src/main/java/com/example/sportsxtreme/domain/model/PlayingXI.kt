package com.example.sportsxtreme.domain.model

data class PlayingXI(
    val teamId: String,
    val side: TeamSide,
    val playerIds: List<String>,
    val selectedByUserId: String,
    val selectedAtEpochMs: Long
)
