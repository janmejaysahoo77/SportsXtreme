package com.example.sportsxtreme.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sportsxtreme.data.local.entity.BallEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BallEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBall(ball: BallEventEntity)

    @Query("SELECT * FROM ball_events WHERE matchId = :matchId AND inningsId = :inningsId ORDER BY sequenceNumber")
    fun observeBallEvents(matchId: String, inningsId: String): Flow<List<BallEventEntity>>

    @Query("DELETE FROM ball_events WHERE ballId = (SELECT ballId FROM ball_events WHERE matchId = :matchId ORDER BY sequenceNumber DESC LIMIT 1)")
    suspend fun deleteLastBall(matchId: String): Int
}
