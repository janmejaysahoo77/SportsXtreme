package com.example.sportsxtreme.data.di

import com.example.sportsxtreme.data.repository.MatchRepositoryImpl
import com.example.sportsxtreme.data.repository.ScoringRepositoryImpl
import com.example.sportsxtreme.data.repository.TeamRepositoryImpl
import com.example.sportsxtreme.data.remote.firestore.FirebaseFirestoreScoringDataSource
import com.example.sportsxtreme.data.remote.firestore.FirestoreScoringDataSource
import com.example.sportsxtreme.domain.repository.MatchRepository
import com.example.sportsxtreme.domain.repository.ScoringRepository
import com.example.sportsxtreme.domain.repository.TeamRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MatchRepositoryModule {
    @Binds abstract fun bindMatchRepository(repository: MatchRepositoryImpl): MatchRepository
    @Binds abstract fun bindTeamRepository(repository: TeamRepositoryImpl): TeamRepository
    @Binds abstract fun bindScoringRepository(repository: ScoringRepositoryImpl): ScoringRepository
    @Binds abstract fun bindFirestoreScoringDataSource(dataSource: FirebaseFirestoreScoringDataSource): FirestoreScoringDataSource
}
