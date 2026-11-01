package com.sportsxtreme.app.data.local.mapper

import com.sportsxtreme.app.data.local.entity.PlayerEntity
import com.sportsxtreme.app.data.local.entity.PlayingXIEntity
import com.sportsxtreme.app.domain.model.Player
import com.sportsxtreme.app.domain.model.PlayerRole
import com.sportsxtreme.app.domain.model.PlayingXI

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
