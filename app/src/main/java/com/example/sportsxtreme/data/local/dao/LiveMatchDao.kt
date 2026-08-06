package com.example.sportsxtreme.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.sportsxtreme.data.local.entity.LiveMatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LiveMatchDao {
    @Upsert
    suspend fun upsertAll(matches: List<LiveMatchEntity>)

    @Upsert
    suspend fun upsert(match: LiveMatchEntity)

    @Query("DELETE FROM live_matches")
    suspend fun clearAll()

    @Query("SELECT * FROM live_matches ORDER BY updatedAtEpochMs DESC")
    fun observeAll(): Flow<List<LiveMatchEntity>>

    @Query("SELECT * FROM live_matches WHERE matchId = :matchId")
    suspend fun get(matchId: String): LiveMatchEntity?
}