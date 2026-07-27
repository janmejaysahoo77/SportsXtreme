package com.example.sportsxtreme.data.local.mapper

import com.example.sportsxtreme.data.local.entity.InningsEntity
import com.example.sportsxtreme.domain.model.Innings
import com.example.sportsxtreme.domain.model.InningsStatus

fun Innings.toEntity(): InningsEntity = InningsEntity(
    inningsId = id,
    matchId = matchId,
    number = number,
    battingTeamId = battingTeamId,
    bowlingTeamId = bowlingTeamId,
    strikerId = strikerId,
    nonStrikerId = nonStrikerId,
    currentBowlerId = currentBowlerId,
    score = score,
    wickets = wickets,
    legalBalls = legalBalls,
    target = target,
    status = status.name,
    startedAtEpochMs = startedAtEpochMs,
    completedAtEpochMs = completedAtEpochMs
)

fun InningsEntity.toDomain(): Innings = Innings(
    id = inningsId,
    matchId = matchId,
    number = number,
    battingTeamId = battingTeamId,
    bowlingTeamId = bowlingTeamId,
    strikerId = strikerId,
    nonStrikerId = nonStrikerId,
    currentBowlerId = currentBowlerId,
    score = score,
    wickets = wickets,
    legalBalls = legalBalls,
    target = target,
    status = InningsStatus.valueOf(status),
    startedAtEpochMs = startedAtEpochMs,
    completedAtEpochMs = completedAtEpochMs
)
