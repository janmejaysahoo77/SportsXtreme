package com.example.sportsxtreme.domain.usecase

data class MatchUseCases(
    val createMatch: CreateMatchUseCase,
    val createMatchInvite: CreateMatchInviteUseCase,
    val updateMatchSettings: UpdateMatchSettingsUseCase,
    val updateMatchDetails: UpdateMatchDetailsUseCase,
    val updateMatchTeams: UpdateMatchTeamsUseCase,
    val selectPlayingXI: SelectPlayingXIUseCase,
    val saveToss: SaveTossUseCase,
    val selectOpeningPlayers: SelectOpeningPlayersUseCase,
    val startMatch: StartMatchUseCase,
    val recordBall: RecordBallUseCase,
    val undoBall: UndoBallUseCase,
    val finishInnings: FinishInningsUseCase,
    val finishMatch: FinishMatchUseCase,
    val observeMatch: ObserveMatchUseCase,
    val observeActiveMatch: ObserveActiveMatchUseCase,
    val observeMatchState: ObserveMatchStateUseCase
)
