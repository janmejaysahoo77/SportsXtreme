package com.example.sportsxtreme.domain.model

object FriendlyMatchFixtures {
    fun teamA(nowEpochMs: Long): Team {
        return Team(
            id = "friendly-team-a",
            name = "Team A",
            shortName = "A",
            type = TeamType.FRIENDLY_TEST,
            players = (1..15).map { index ->
                Player(
                    id = "dA$index",
                    teamId = "friendly-team-a",
                    displayName = "dA$index",
                    jerseyNumber = index
                )
            },
            createdAtEpochMs = nowEpochMs,
            updatedAtEpochMs = nowEpochMs
        )
    }

    fun teamB(nowEpochMs: Long): Team {
        return Team(
            id = "friendly-team-b",
            name = "Team B",
            shortName = "B",
            type = TeamType.FRIENDLY_TEST,
            players = (1..15).map { index ->
                Player(
                    id = "dB$index",
                    teamId = "friendly-team-b",
                    displayName = "dB$index",
                    jerseyNumber = index
                )
            },
            createdAtEpochMs = nowEpochMs,
            updatedAtEpochMs = nowEpochMs
        )
    }
}
