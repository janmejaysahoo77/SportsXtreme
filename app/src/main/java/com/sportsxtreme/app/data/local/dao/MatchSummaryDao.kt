package com.sportsxtreme.app.data.local.dao

import androidx.room.Dao
import androidx.room.Upsert
import androidx.room.Query
import com.sportsxtreme.app.data.local.entity.MatchSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchSummaryDao {
    @Upsert
    suspend fun upsert(summary: MatchSummaryEntity)

    @Query("DELETE FROM match_summaries WHERE matchId = :matchId AND inningsId = :inningsId")
    suspend fun deleteForInnings(matchId: String, inningsId: String)

    @Query("DELETE FROM match_summaries WHERE matchId = :matchId")
    suspend fun deleteForMatch(matchId: String)

    @Query("SELECT * FROM match_summaries WHERE matchId = :matchId AND inningsId = :inningsId")
    fun observeSummary(matchId: String, inningsId: String): Flow<MatchSummaryEntity?>
}
