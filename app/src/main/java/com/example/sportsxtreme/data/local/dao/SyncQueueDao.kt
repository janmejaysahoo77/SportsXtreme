package com.example.sportsxtreme.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sportsxtreme.data.local.entity.SyncQueueEntity

@Dao
interface SyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun enqueue(operation: SyncQueueEntity): Long

    @Query("SELECT * FROM sync_queue WHERE state IN ('PENDING', 'FAILED') AND nextAttemptAtEpochMs <= :nowEpochMs ORDER BY createdAtEpochMs LIMIT :limit")
    suspend fun getReadyOperations(nowEpochMs: Long, limit: Int): List<SyncQueueEntity>

    @Update
    suspend fun update(operation: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE operationId = :operationId")
    suspend fun delete(operationId: String)

    @Query("DELETE FROM sync_queue WHERE entityType = :entityType AND entityId = :entityId")
    suspend fun deleteByEntity(entityType: String, entityId: String)
}
