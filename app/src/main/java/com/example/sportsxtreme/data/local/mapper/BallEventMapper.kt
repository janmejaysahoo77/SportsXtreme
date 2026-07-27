package com.example.sportsxtreme.data.local.mapper

import com.example.sportsxtreme.data.local.entity.BallEventEntity
import com.example.sportsxtreme.domain.model.BallEvent
import com.example.sportsxtreme.domain.model.BallEventType
import com.example.sportsxtreme.domain.model.Dismissal
import com.example.sportsxtreme.domain.model.DismissalType
import com.example.sportsxtreme.domain.model.ExtraRun
import com.example.sportsxtreme.domain.model.ExtraType
import com.example.sportsxtreme.domain.model.SyncState
import org.json.JSONArray
import org.json.JSONObject

fun BallEvent.toEntity(): BallEventEntity {
    val primaryExtra = extras.firstOrNull() ?: ExtraRun(ExtraType.NONE, 0)
    val additionalExtras = JSONArray().apply {
        extras.drop(1).forEach { extra ->
            put(JSONObject().put("type", extra.type.name).put("runs", extra.runs))
        }
    }
    return BallEventEntity(
        ballId = ballId,
        matchId = matchId,
        inningsId = inningsId,
        inningsNumber = inningsNumber,
        sequenceNumber = sequenceNumber,
        overNumber = overNumber,
        ballNumber = ballNumber,
        legalBallNumber = legalBallNumber,
        battingTeamId = battingTeamId,
        bowlingTeamId = bowlingTeamId,
        strikerId = strikerId,
        nonStrikerId = nonStrikerId,
        bowlerId = bowlerId,
        runs = runsOffBat,
        extraType = primaryExtra.type.name,
        extraRuns = primaryExtra.runs,
        additionalExtrasJson = additionalExtras.toString(),
        dismissalType = dismissal?.type?.name ?: DismissalType.NONE.name,
        dismissedPlayerId = dismissal?.dismissedPlayerId,
        dismissalAssistPlayerIds = dismissal?.assistedByPlayerIds?.joinToString("|").orEmpty(),
        isLegalDelivery = isLegalDelivery,
        eventType = eventType.name,
        reversedEventId = reversedEventId,
        comment = comment,
        recordedByUserId = recordedByUserId,
        timestamp = timestampEpochMs,
        previousEventId = previousEventId,
        metadataJson = JSONObject(metadata).toString(),
        syncState = syncState.name
    )
}

fun BallEventEntity.toDomain(): BallEvent {
    val primaryExtra = ExtraType.valueOf(extraType).takeUnless { it == ExtraType.NONE }?.let {
        ExtraRun(it, extraRuns)
    }
    val additionalExtras = JSONArray(additionalExtrasJson).let { json ->
        (0 until json.length()).map { index ->
            json.getJSONObject(index).let { extra ->
                ExtraRun(ExtraType.valueOf(extra.getString("type")), extra.getInt("runs"))
            }
        }
    }
    val dismissalType = DismissalType.valueOf(dismissalType)
    return BallEvent(
        ballId = ballId,
        matchId = matchId,
        inningsId = inningsId,
        inningsNumber = inningsNumber,
        sequenceNumber = sequenceNumber,
        overNumber = overNumber,
        ballNumber = ballNumber,
        legalBallNumber = legalBallNumber,
        battingTeamId = battingTeamId,
        bowlingTeamId = bowlingTeamId,
        strikerId = strikerId,
        nonStrikerId = nonStrikerId,
        bowlerId = bowlerId,
        runsOffBat = runs,
        extras = listOfNotNull(primaryExtra) + additionalExtras,
        dismissal = dismissalType.takeUnless { it == DismissalType.NONE }?.let { type ->
            Dismissal(
                type = type,
                dismissedPlayerId = requireNotNull(dismissedPlayerId),
                assistedByPlayerIds = dismissalAssistPlayerIds.split("|").filter { it.isNotBlank() }
            )
        },
        isLegalDelivery = isLegalDelivery,
        eventType = BallEventType.valueOf(eventType),
        reversedEventId = reversedEventId,
        comment = comment,
        recordedByUserId = recordedByUserId,
        timestampEpochMs = timestamp,
        syncState = SyncState.valueOf(syncState),
        previousEventId = previousEventId,
        metadata = JSONObject(metadataJson).let { json ->
            json.keys().asSequence().associateWith(json::getString)
        }
    )
}
