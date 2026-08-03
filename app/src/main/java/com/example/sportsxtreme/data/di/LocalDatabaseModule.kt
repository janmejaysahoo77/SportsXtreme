package com.example.sportsxtreme.data.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.example.sportsxtreme.data.local.dao.BallEventDao
import com.example.sportsxtreme.data.local.dao.BattingDao
import com.example.sportsxtreme.data.local.dao.BowlingDao
import com.example.sportsxtreme.data.local.dao.InningsDao
import com.example.sportsxtreme.data.local.dao.MatchDao
import com.example.sportsxtreme.data.local.dao.MatchSummaryDao
import com.example.sportsxtreme.data.local.dao.PlayerDao
import com.example.sportsxtreme.data.local.dao.SyncQueueDao
import com.example.sportsxtreme.data.local.dao.TeamDao
import com.example.sportsxtreme.data.local.database.SportsXtremeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SportsXtremeDatabase =
        Room.databaseBuilder(context, SportsXtremeDatabase::class.java, "sports_xtreme.db")
            .addMigrations(
                SportsXtremeDatabase.MIGRATION_1_2,
                SportsXtremeDatabase.MIGRATION_2_3,
                SportsXtremeDatabase.MIGRATION_3_4,
                SportsXtremeDatabase.MIGRATION_4_5
            )
            .build()

    @Provides fun provideMatchDao(database: SportsXtremeDatabase): MatchDao = database.matchDao()
    @Provides fun provideTeamDao(database: SportsXtremeDatabase): TeamDao = database.teamDao()
    @Provides fun providePlayerDao(database: SportsXtremeDatabase): PlayerDao = database.playerDao()
    @Provides fun provideInningsDao(database: SportsXtremeDatabase): InningsDao = database.inningsDao()
    @Provides fun provideBallEventDao(database: SportsXtremeDatabase): BallEventDao = database.ballEventDao()
    @Provides fun provideBattingDao(database: SportsXtremeDatabase): BattingDao = database.battingDao()
    @Provides fun provideBowlingDao(database: SportsXtremeDatabase): BowlingDao = database.bowlingDao()
    @Provides fun provideMatchSummaryDao(database: SportsXtremeDatabase): MatchSummaryDao = database.matchSummaryDao()
    @Provides fun provideSyncQueueDao(database: SportsXtremeDatabase): SyncQueueDao = database.syncQueueDao()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)
}
