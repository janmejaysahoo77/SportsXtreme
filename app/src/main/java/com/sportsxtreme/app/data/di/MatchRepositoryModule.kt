package com.sportsxtreme.app.data.di

import com.sportsxtreme.app.data.repository.LiveMatchRepositoryImpl
import com.sportsxtreme.app.data.repository.MatchRepositoryImpl
import com.sportsxtreme.app.data.repository.MatchInviteRepositoryImpl
import com.sportsxtreme.app.data.repository.ScoringRepositoryImpl
import com.sportsxtreme.app.data.repository.TeamRepositoryImpl
import com.sportsxtreme.app.data.remote.firestore.FirebaseFirestoreLiveMatchDataSource
import com.sportsxtreme.app.data.remote.firestore.FirebaseFirestoreScoringDataSource
import com.sportsxtreme.app.data.remote.firestore.FirestoreLiveMatchDataSource
import com.sportsxtreme.app.data.remote.firestore.FirestoreScoringDataSource
import com.sportsxtreme.app.domain.repository.LiveMatchRepository
import com.sportsxtreme.app.domain.repository.MatchRepository
import com.sportsxtreme.app.domain.repository.MatchInviteRepository
import com.sportsxtreme.app.domain.repository.ScoringRepository
import com.sportsxtreme.app.domain.repository.TeamRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MatchRepositoryModule {
    @Binds abstract fun bindMatchRepository(repository: MatchRepositoryImpl): MatchRepository
    @Binds abstract fun bindMatchInviteRepository(repository: MatchInviteRepositoryImpl): MatchInviteRepository
    @Binds abstract fun bindTeamRepository(repository: TeamRepositoryImpl): TeamRepository
    @Binds abstract fun bindScoringRepository(repository: ScoringRepositoryImpl): ScoringRepository
    @Binds abstract fun bindFirestoreScoringDataSource(dataSource: FirebaseFirestoreScoringDataSource): FirestoreScoringDataSource
    @Binds abstract fun bindLiveMatchRepository(repository: LiveMatchRepositoryImpl): LiveMatchRepository
    @Binds abstract fun bindFirestoreLiveMatchDataSource(dataSource: FirebaseFirestoreLiveMatchDataSource): FirestoreLiveMatchDataSource
}
