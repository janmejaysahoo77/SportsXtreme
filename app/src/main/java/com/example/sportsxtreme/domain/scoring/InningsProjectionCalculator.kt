package com.example.sportsxtreme.domain.scoring

import com.example.sportsxtreme.domain.model.BallEvent
import com.example.sportsxtreme.domain.model.BallEventType
import com.example.sportsxtreme.domain.model.DismissalType
import com.example.sportsxtreme.domain.model.ExtraType

data class InningsProjection(
    val batting: List<BattingProjection>,
    val bowling: List<BowlingProjection>,
    val totalScore: Int,
    val wickets: Int,
    val legalBalls: Int,
    val strikerId: String?,
    val nonStrikerId: String?,
    val bowlerId: String?
)

data class BattingProjection(
    val playerId: String,
    val runs: Int,
    val balls: Int,
    val fours: Int,
    val sixes: Int,
    val status: String,
    val dismissalType: String?,
    val dismissedByBowlerId: String?,
    val dismissedByFielderId: String?
)

data class BowlingProjection(
    val playerId: String,
    val legalBalls: Int,
    val maidens: Int,
    val runsConceded: Int,
    val wickets: Int,
    val wides: Int,
    val noBalls: Int
)

object InningsProjectionCalculator {
    fun calculate(events: List<BallEvent>): InningsProjection {
        val reversedEventIds = events
            .filter { it.eventType == BallEventType.REVERSAL }
            .mapNotNull { it.reversedEventId }
            .toSet()
        val deliveries = events
            .asSequence()
            .filter { it.eventType == BallEventType.DELIVERY && it.ballId !in reversedEventIds }
            .sortedWith(compareBy<BallEvent> { it.sequenceNumber }.thenBy { it.timestampEpochMs }.thenBy { it.ballId })
            .toList()

        val batting = deliveries
            .flatMap { listOf(it.strikerId, it.nonStrikerId) }
            .distinct()
            .map { playerId -> playerId.toBattingProjection(deliveries) }
        val bowling = deliveries
            .groupBy { it.bowlerId }
            .map { (playerId, bowlerDeliveries) -> playerId.toBowlingProjection(bowlerDeliveries) }
        var currentBatters: Pair<String?, String?>? = null
        deliveries.forEachIndexed { index: Int, delivery: BallEvent ->
            val striker = currentBatters?.first ?: delivery.strikerId
            val nonStriker = currentBatters?.second ?: delivery.nonStrikerId
            val dismissal = delivery.dismissal
            currentBatters = if (dismissal?.dismissedPlayerId == striker ||
                dismissal?.dismissedPlayerId == nonStriker
            ) {
                (if (dismissal?.dismissedPlayerId == striker) null else striker) to
                    (if (dismissal?.dismissedPlayerId == nonStriker) null else nonStriker)
            } else {
                val swapsStrike = delivery.totalRuns % 2 != 0
                val endsOver = delivery.isLegalDelivery &&
                    deliveries.take(index).count { event -> event.isLegalDelivery } % 6 == 5
                if (swapsStrike.xor(endsOver)) nonStriker to striker else striker to nonStriker
            }
        }
        val lastDelivery = deliveries.lastOrNull()

        return InningsProjection(
            batting = batting,
            bowling = bowling,
            totalScore = deliveries.sumOf { it.totalRuns },
            wickets = deliveries.count { it.dismissal?.type?.countsAsWicket() == true },
            legalBalls = deliveries.count { it.isLegalDelivery },
            strikerId = currentBatters?.first,
            nonStrikerId = currentBatters?.second,
            bowlerId = lastDelivery?.bowlerId
        )
    }

    private fun String.toBattingProjection(deliveries: List<BallEvent>): BattingProjection {
        val facedDeliveries = deliveries.filter { it.strikerId == this }
        val dismissalDelivery = deliveries.lastOrNull { it.dismissal?.dismissedPlayerId == this }
        val dismissal = dismissalDelivery?.dismissal
        val activePlayers = deliveries.lastOrNull()?.let { setOf(it.strikerId, it.nonStrikerId) }.orEmpty()
        val status = when {
            dismissal?.type == DismissalType.RETIRED_HURT -> "RETIRED_HURT"
            dismissal != null -> "OUT"
            this in activePlayers -> "NOT_OUT"
            else -> "YET_TO_BAT"
        }

        return BattingProjection(
            playerId = this,
            runs = facedDeliveries.sumOf { it.runsOffBat },
            balls = facedDeliveries.count { delivery -> delivery.extras.none { it.type == ExtraType.WIDE } },
            fours = facedDeliveries.count { it.runsOffBat == 4 },
            sixes = facedDeliveries.count { it.runsOffBat == 6 },
            status = status,
            dismissalType = dismissal?.type?.name,
            dismissedByBowlerId = dismissal?.takeIf { it.type.creditsBowler() }?.let { dismissalDelivery?.bowlerId },
            dismissedByFielderId = dismissal?.assistedByPlayerIds?.firstOrNull()
        )
    }

    private fun String.toBowlingProjection(deliveries: List<BallEvent>): BowlingProjection {
        val overGroups = deliveries.groupBy { it.overNumber }
        return BowlingProjection(
            playerId = this,
            legalBalls = deliveries.count { it.isLegalDelivery },
            maidens = overGroups.values.count { overDeliveries ->
                overDeliveries.count { it.isLegalDelivery } == 6 &&
                    overDeliveries.sumOf { it.runsConcededByBowler() } == 0
            },
            runsConceded = deliveries.sumOf { it.runsConcededByBowler() },
            wickets = deliveries.count { it.dismissal?.type?.creditsBowler() == true },
            wides = deliveries.sumOf { delivery -> delivery.extras.filter { it.type == ExtraType.WIDE }.sumOf { it.runs } },
            noBalls = deliveries.sumOf { delivery -> delivery.extras.filter { it.type == ExtraType.NO_BALL }.sumOf { it.runs } }
        )
    }

    private fun BallEvent.runsConcededByBowler(): Int =
        runsOffBat + extras
            .filter { it.type == ExtraType.WIDE || it.type == ExtraType.NO_BALL }
            .sumOf { it.runs }

    private fun DismissalType.countsAsWicket(): Boolean = this != DismissalType.RETIRED_HURT

    private fun DismissalType.creditsBowler(): Boolean = this !in setOf(
        DismissalType.RUN_OUT,
        DismissalType.RETIRED_OUT,
        DismissalType.RETIRED_HURT,
        DismissalType.OBSTRUCTING_THE_FIELD
    )
}
