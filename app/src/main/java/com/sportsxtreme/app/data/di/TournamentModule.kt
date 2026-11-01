package com.sportsxtreme.app.data.di

import com.sportsxtreme.app.data.repository.TournamentRepositoryImpl
import com.sportsxtreme.app.domain.repository.TournamentRepository
import com.sportsxtreme.app.domain.usecase.CreateTournamentUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TournamentModule {

    @Binds
    @Singleton
    abstract fun bindTournamentRepository(
        tournamentRepositoryImpl: TournamentRepositoryImpl
    ): TournamentRepository
}

@Module
@InstallIn(SingletonComponent::class)
object TournamentUseCaseModule {

    @Provides
    @Singleton
    fun provideCreateTournamentUseCase(
        repository: TournamentRepository
    ): CreateTournamentUseCase {
        return CreateTournamentUseCase(repository)
    }
}
