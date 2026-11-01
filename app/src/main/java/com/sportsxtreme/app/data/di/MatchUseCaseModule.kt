package com.sportsxtreme.app.data.di

import com.sportsxtreme.app.domain.repository.MatchRepository
import com.sportsxtreme.app.domain.repository.MatchInviteRepository
import com.sportsxtreme.app.domain.repository.ScoringRepository
import com.sportsxtreme.app.domain.repository.TeamRepository
import com.sportsxtreme.app.domain.usecase.CreateMatchUseCase
import com.sportsxtreme.app.domain.usecase.CreateMatchInviteUseCase
import com.sportsxtreme.app.domain.usecase.FinishInningsUseCase
import com.sportsxtreme.app.domain.usecase.FinishMatchUseCase
import com.sportsxtreme.app.domain.usecase.MatchUseCases
import com.sportsxtreme.app.domain.usecase.ObserveMatchStateUseCase
import com.sportsxtreme.app.domain.usecase.ObserveActiveMatchUseCase
import com.sportsxtreme.app.domain.usecase.ObserveMatchUseCase
import com.sportsxtreme.app.domain.usecase.RecordBallUseCase
import com.sportsxtreme.app.domain.usecase.SaveTossUseCase
import com.sportsxtreme.app.domain.usecase.SelectOpeningPlayersUseCase
import com.sportsxtreme.app.domain.usecase.SelectPlayingXIUseCase
import com.sportsxtreme.app.domain.usecase.StartMatchUseCase
import com.sportsxtreme.app.domain.usecase.UndoBallUseCase
import com.sportsxtreme.app.domain.usecase.UpdateMatchSettingsUseCase
import com.sportsxtreme.app.domain.usecase.UpdateMatchDetailsUseCase
import com.sportsxtreme.app.domain.usecase.UpdateMatchTeamsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object MatchUseCaseModule {
    @Provides
    fun provideMatchUseCases(
        matchRepository: MatchRepository,
        matchInviteRepository: MatchInviteRepository,
        scoringRepository: ScoringRepository,
        teamRepository: TeamRepository
    ): MatchUseCases {
        return MatchUseCases(
            createMatch = CreateMatchUseCase(matchRepository),
            createMatchInvite = CreateMatchInviteUseCase(matchInviteRepository),
            updateMatchSettings = UpdateMatchSettingsUseCase(matchRepository),
            updateMatchDetails = UpdateMatchDetailsUseCase(matchRepository),
            updateMatchTeams = UpdateMatchTeamsUseCase(matchRepository, teamRepository),
            selectPlayingXI = SelectPlayingXIUseCase(matchRepository),
            saveToss = SaveTossUseCase(matchRepository),
            selectOpeningPlayers = SelectOpeningPlayersUseCase(matchRepository),
            startMatch = StartMatchUseCase(matchRepository),
            recordBall = RecordBallUseCase(scoringRepository),
            undoBall = UndoBallUseCase(scoringRepository),
            finishInnings = FinishInningsUseCase(matchRepository),
            finishMatch = FinishMatchUseCase(matchRepository),
            observeMatch = ObserveMatchUseCase(matchRepository),
            observeActiveMatch = ObserveActiveMatchUseCase(matchRepository),
            observeMatchState = ObserveMatchStateUseCase(matchRepository)
        )
    }
}
