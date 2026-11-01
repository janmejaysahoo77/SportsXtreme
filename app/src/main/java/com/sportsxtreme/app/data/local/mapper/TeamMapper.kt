package com.sportsxtreme.app.data.local.mapper

import com.sportsxtreme.app.data.local.entity.PlayerEntity
import com.sportsxtreme.app.data.local.entity.TeamEntity
import com.sportsxtreme.app.domain.model.Team
import com.sportsxtreme.app.domain.model.TeamType

fun Team.toEntity(isTeamA: Boolean = false): TeamEntity = TeamEntity(
    teamId = id,
    teamName = name,
    shortName = shortName,
    isTeamA = isTeamA,
    type = type.name,
    ownerUserId = ownerUserId,
    createdAtEpochMs = createdAtEpochMs,
    updatedAtEpochMs = updatedAtEpochMs
)

fun TeamEntity.toDomain(players: List<PlayerEntity>): Team = Team(
    id = teamId,
    name = teamName,
    shortName = shortName,
    type = TeamType.valueOf(type),
    players = players.map { it.toDomain() },
    ownerUserId = ownerUserId,
    createdAtEpochMs = createdAtEpochMs,
    updatedAtEpochMs = updatedAtEpochMs
)
