package com.example.sportsxtreme.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sportsxtreme.data.local.entity.MatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity)

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("SELECT * FROM matches WHERE matchId = :matchId")
    suspend fun getMatch(matchId: String): MatchEntity?

    @Query("SELECT * FROM matches WHERE matchId = :matchId")
    fun observeMatch(matchId: String): Flow<MatchEntity?>

    @Query("DELETE FROM matches WHERE matchId = :matchId")
    suspend fun deleteMatch(matchId: String)

    @Query("SELECT * FROM matches WHERE status NOT IN ('COMPLETED', 'ABANDONED') ORDER BY updatedAtEpochMs DESC LIMIT 1")
    fun observeActiveMatch(): Flow<MatchEntity?>

    @Query("SELECT * FROM matches WHERE status IN ('LIVE', 'IN_PROGRESS', 'INNINGS_BREAK', 'PAUSED') ORDER BY updatedAtEpochMs DESC LIMIT 1")
    fun observeLatestScoringMatch(): Flow<MatchEntity?>
}
