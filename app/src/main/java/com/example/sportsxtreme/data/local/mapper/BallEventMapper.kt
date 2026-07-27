package com.example.sportsxtreme.data.local.mapper

import com.example.sportsxtreme.data.local.entity.BallEventEntity
import com.example.sportsxtreme.domain.model.BallEvent
import com.example.sportsxtreme.domain.model.Extras
import com.example.sportsxtreme.domain.model.Wicket
import com.example.sportsxtreme.domain.model.WicketType
import org.json.JSONObject

fun BallEvent.toEntity(inningsNumber: Int): BallEventEntity = BallEventEntity(
    ballId = id,
    matchId = matchId,
    inningsId = inningsId,
    inningsNumber = inningsNumber,
    sequenceNumber = sequenceNumber,
    overNumber = overNumber,
    ballNumber = ballNumberInOver,
    legalBallNumber = legalBallNumber,
    battingTeamId = battingTeamId,
    bowlingTeamId = bowlingTeamId,
    strikerId = strikerId,
    nonStrikerId = nonStrikerId,
    bowlerId = bowlerId,
    runs = runsOffBat,
    wides = extras.wides,
    noBalls = extras.noBalls,
    byes = extras.byes,
    legByes = extras.legByes,
    penaltyRuns = extras.penalty,
    wicketPlayerId = wicket?.dismissedPlayerId,
    wicketType = wicket?.type?.name,
    wicketAssistPlayerIds = wicket?.assistedByPlayerIds?.joinToString("|").orEmpty(),
    isLegalDelivery = isLegalDelivery,
    recordedByUserId = recordedByUserId,
    timestamp = recordedAtEpochMs,
    previousEventId = previousEventId,
    metadataJson = JSONObject(metadata).toString()
)

fun BallEventEntity.toDomain(): BallEvent = BallEvent(
    id = ballId,
    matchId = matchId,
    inningsId = inningsId,
    sequenceNumber = sequenceNumber,
    overNumber = overNumber,
    ballNumberInOver = ballNumber,
    legalBallNumber = legalBallNumber,
    battingTeamId = battingTeamId,
    bowlingTeamId = bowlingTeamId,
    strikerId = strikerId,
    nonStrikerId = nonStrikerId,
    bowlerId = bowlerId,
    runsOffBat = runs,
    extras = Extras(wides, noBalls, byes, legByes, penaltyRuns),
    wicket = wicketPlayerId?.let { playerId ->
        Wicket(playerId, WicketType.valueOf(requireNotNull(wicketType)), wicketAssistPlayerIds.split("|").filter { it.isNotBlank() })
    },
    isLegalDelivery = isLegalDelivery,
    recordedByUserId = recordedByUserId,
    recordedAtEpochMs = timestamp,
    previousEventId = previousEventId,
    metadata = JSONObject(metadataJson).let { json ->
        json.keys().asSequence().associateWith(json::getString)
    }
)
