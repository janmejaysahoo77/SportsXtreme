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

    @Query("SELECT * FROM matches WHERE status != 'COMPLETED' ORDER BY updatedAtEpochMs DESC LIMIT 1")
    fun observeActiveMatch(): Flow<MatchEntity?>
}
