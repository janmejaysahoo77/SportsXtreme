package com.example.sportsxtreme.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sportsxtreme.data.local.entity.BallEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BallEventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBall(ball: BallEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBall(ball: BallEventEntity)

    @Query("SELECT * FROM ball_events WHERE matchId = :matchId AND inningsId = :inningsId ORDER BY sequenceNumber, timestamp, ballId")
    suspend fun getBallEvents(matchId: String, inningsId: String): List<BallEventEntity>

    @Query("SELECT * FROM ball_events WHERE ballId = :ballId")
    suspend fun getBallEvent(ballId: String): BallEventEntity?

    @Query("UPDATE ball_events SET syncState = :syncState WHERE ballId = :ballId")
    suspend fun updateSyncState(ballId: String, syncState: String)

    @Query(
        """
        SELECT * FROM ball_events AS delivery
        WHERE delivery.matchId = :matchId
          AND delivery.eventType = 'DELIVERY'
          AND NOT EXISTS (
              SELECT 1 FROM ball_events AS reversal
              WHERE reversal.eventType = 'REVERSAL'
                AND reversal.reversedEventId = delivery.ballId
          )
        ORDER BY delivery.sequenceNumber DESC, delivery.timestamp DESC, delivery.ballId DESC
        LIMIT 1
        """
    )
    suspend fun getLatestActiveDelivery(matchId: String): BallEventEntity?

    @Query("SELECT * FROM ball_events WHERE matchId = :matchId AND inningsId = :inningsId ORDER BY sequenceNumber")
    fun observeBallEvents(matchId: String, inningsId: String): Flow<List<BallEventEntity>>

}
