package com.example.sportsxtreme.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Upsert
import androidx.room.Query
import com.example.sportsxtreme.data.local.entity.BattingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BattingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun replaceAll(scorecards: List<BattingEntity>)

    @Upsert
    suspend fun upsert(scorecard: BattingEntity)

    @Query("DELETE FROM batting_scorecards WHERE matchId = :matchId AND inningsId = :inningsId")
    suspend fun deleteForInnings(matchId: String, inningsId: String)

    @Query("SELECT * FROM batting_scorecards WHERE matchId = :matchId AND inningsId = :inningsId ORDER BY status = 'NOT_BATTED', runs DESC, balls ASC")
    fun observeScorecard(matchId: String, inningsId: String): Flow<List<BattingEntity>>
}
