package com.example.sportsxtreme.data.di

import com.example.sportsxtreme.data.repository.TournamentRepositoryImpl
import com.example.sportsxtreme.domain.repository.TournamentRepository
import com.example.sportsxtreme.domain.usecase.CreateTournamentUseCase
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
