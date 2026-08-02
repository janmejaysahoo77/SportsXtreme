package com.example.sportsxtreme.data.di

import com.example.sportsxtreme.domain.repository.MatchRepository
import com.example.sportsxtreme.domain.repository.ScoringRepository
import com.example.sportsxtreme.domain.repository.TeamRepository
import com.example.sportsxtreme.domain.usecase.CreateMatchUseCase
import com.example.sportsxtreme.domain.usecase.FinishInningsUseCase
import com.example.sportsxtreme.domain.usecase.FinishMatchUseCase
import com.example.sportsxtreme.domain.usecase.MatchUseCases
import com.example.sportsxtreme.domain.usecase.ObserveMatchStateUseCase
import com.example.sportsxtreme.domain.usecase.ObserveActiveMatchUseCase
import com.example.sportsxtreme.domain.usecase.ObserveMatchUseCase
import com.example.sportsxtreme.domain.usecase.RecordBallUseCase
import com.example.sportsxtreme.domain.usecase.SaveTossUseCase
import com.example.sportsxtreme.domain.usecase.SelectOpeningPlayersUseCase
import com.example.sportsxtreme.domain.usecase.SelectPlayingXIUseCase
import com.example.sportsxtreme.domain.usecase.StartMatchUseCase
import com.example.sportsxtreme.domain.usecase.UndoBallUseCase
import com.example.sportsxtreme.domain.usecase.UpdateMatchSettingsUseCase
import com.example.sportsxtreme.domain.usecase.UpdateMatchDetailsUseCase
import com.example.sportsxtreme.domain.usecase.UpdateMatchTeamsUseCase
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
        scoringRepository: ScoringRepository,
        teamRepository: TeamRepository
    ): MatchUseCases {
        return MatchUseCases(
            createMatch = CreateMatchUseCase(matchRepository),
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
