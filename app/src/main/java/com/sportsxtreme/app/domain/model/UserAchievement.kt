package com.sportsxtreme.app.domain.model

data class UserAchievement(
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val type: String,
    val iconName: String,
    val earnedAtMillis: Long = 0L,
    val matchId: String? = null,
    val tournamentId: String? = null
)
