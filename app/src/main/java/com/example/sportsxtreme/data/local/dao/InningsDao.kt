package com.example.sportsxtreme.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sportsxtreme.data.local.entity.InningsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InningsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInnings(innings: InningsEntity)

    @Update
    suspend fun updateInnings(innings: InningsEntity)

    @Query("SELECT * FROM innings WHERE inningsId = :inningsId")
    suspend fun getInnings(inningsId: String): InningsEntity?

    @Query("SELECT * FROM innings WHERE matchId = :matchId ORDER BY number")
    suspend fun getInningsForMatch(matchId: String): List<InningsEntity>

    @Query("SELECT * FROM innings WHERE matchId = :matchId ORDER BY number")
    fun observeInnings(matchId: String): Flow<List<InningsEntity>>

    @Query("SELECT * FROM innings WHERE matchId = :matchId ORDER BY number DESC LIMIT 1")
    suspend fun getLatestInnings(matchId: String): InningsEntity?
}
