package com.example.sportsxtreme.domain.model

data class Tournament(
    val id: String = "",
    val type: String = "Tournament", // Tournament or Series
    val name: String = "",
    val city: String = "",
    val ground: String = "",
    val organizerName: String = "",
    val phone: String = "",
    val email: String = "",
    val startDate: String = "",
    val dateToBeAnnounced: Boolean = false,
    val ballType: String = "Tennis",
    val matchForm: String = "Limited Overs",
    val lookingForTeams: Boolean = true,
    val hostUid: String = "",
    /** Epoch time recorded when the tournament is registered, used for newest-first lists. */
    val createdAtEpochMs: Long = 0L,
    val requirements: TournamentRequirements = TournamentRequirements()
)

data class TournamentRequirements(
    val location: String = "",
    val entryFee: String = "",
    val numberOfTeams: String = "",
    val expectedEndDate: String = "",
    val matchDuration: String = "",
    val prizeType: String = "Cash",
    val trophyIncluded: Boolean = false,
    val prizePool: String = "",
    val runnerUpPrize: String = "",
    val tournamentFormat: String = "Knockout",
    val notes: String = "",
    val invitePreviousPlayers: Boolean = true,
    val needsOfficials: Boolean = false
)
