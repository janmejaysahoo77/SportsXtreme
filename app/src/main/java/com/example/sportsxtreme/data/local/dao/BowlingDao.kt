package com.example.sportsxtreme.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Upsert
import androidx.room.Query
import com.example.sportsxtreme.data.local.entity.BowlingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BowlingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun replaceAll(scorecards: List<BowlingEntity>)

    @Upsert
    suspend fun upsert(scorecard: BowlingEntity)

    @Query("DELETE FROM bowling_scorecards WHERE matchId = :matchId AND inningsId = :inningsId")
    suspend fun deleteForInnings(matchId: String, inningsId: String)

    @Query("SELECT * FROM bowling_scorecards WHERE matchId = :matchId AND inningsId = :inningsId ORDER BY legalBalls DESC, runsConceded ASC")
    fun observeScorecard(matchId: String, inningsId: String): Flow<List<BowlingEntity>>
}
