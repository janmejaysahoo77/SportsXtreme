package com.example.sportsxtreme.data.local.dao

import androidx.room.Dao
import androidx.room.Upsert
import androidx.room.Query
import com.example.sportsxtreme.data.local.entity.MatchSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchSummaryDao {
    @Upsert
    suspend fun upsert(summary: MatchSummaryEntity)

    @Query("DELETE FROM match_summaries WHERE matchId = :matchId AND inningsId = :inningsId")
    suspend fun deleteForInnings(matchId: String, inningsId: String)

    @Query("SELECT * FROM match_summaries WHERE matchId = :matchId AND inningsId = :inningsId")
    fun observeSummary(matchId: String, inningsId: String): Flow<MatchSummaryEntity?>
}
