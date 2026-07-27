package com.example.sportsxtreme.domain.model

data class Team(
    val id: String,
    val name: String,
    val shortName: String,
    val type: TeamType,
    val players: List<Player>,
    val ownerUserId: String? = null,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

enum class TeamType {
    FRIENDLY_TEST,
    USER_CREATED,
    TOURNAMENT
}
