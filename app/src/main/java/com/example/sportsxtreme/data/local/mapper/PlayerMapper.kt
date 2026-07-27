package com.example.sportsxtreme.data.local.mapper

import com.example.sportsxtreme.data.local.entity.PlayerEntity
import com.example.sportsxtreme.data.local.entity.PlayingXIEntity
import com.example.sportsxtreme.domain.model.Player
import com.example.sportsxtreme.domain.model.PlayerRole
import com.example.sportsxtreme.domain.model.PlayingXI

fun Player.toEntity(
    battingOrder: Int? = null,
    playingXI: Boolean = false
): PlayerEntity = PlayerEntity(
    playerId = id,
    teamId = teamId,
    playerName = displayName,
    battingOrder = battingOrder,
    playingXI = playingXI,
    jerseyNumber = jerseyNumber,
    linkedUserId = linkedUserId,
    role = role.name,
    isGuestPlayer = isGuestPlayer
)

fun PlayerEntity.toDomain(): Player = Player(
    id = playerId,
    teamId = teamId,
    displayName = playerName,
    jerseyNumber = jerseyNumber,
    linkedUserId = linkedUserId,
    role = PlayerRole.valueOf(role),
    isGuestPlayer = isGuestPlayer
)

fun PlayingXI.toEntities(matchId: String): List<PlayingXIEntity> = playerIds.mapIndexed { index, playerId ->
    PlayingXIEntity(
        matchId = matchId,
        teamId = teamId,
        side = side.name,
        playerId = playerId,
        battingOrder = index + 1,
        selectedByUserId = selectedByUserId,
        selectedAtEpochMs = selectedAtEpochMs
    )
}
