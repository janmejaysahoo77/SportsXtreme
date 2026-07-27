package com.example.sportsxtreme.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sportsxtreme.data.local.entity.PlayerEntity
import com.example.sportsxtreme.data.local.entity.PlayingXIEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<PlayerEntity>)

    @Query("SELECT * FROM players WHERE playerId = :playerId")
    suspend fun getPlayer(playerId: String): PlayerEntity?

    @Query("SELECT * FROM players WHERE teamId = :teamId ORDER BY battingOrder, playerName")
    suspend fun getPlayers(teamId: String): List<PlayerEntity>

    @Query("SELECT * FROM players WHERE teamId = :teamId ORDER BY battingOrder, playerName")
    fun observePlayers(teamId: String): Flow<List<PlayerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayingXI(players: List<PlayingXIEntity>)

    @Query("DELETE FROM playing_xi WHERE matchId = :matchId AND teamId = :teamId")
    suspend fun clearPlayingXI(matchId: String, teamId: String)

    @Query("SELECT * FROM playing_xi WHERE matchId = :matchId AND teamId = :teamId ORDER BY battingOrder")
    suspend fun getPlayingXI(matchId: String, teamId: String): List<PlayingXIEntity>

    @Query("SELECT * FROM playing_xi WHERE matchId = :matchId AND teamId = :teamId ORDER BY battingOrder")
    fun observePlayingXI(matchId: String, teamId: String): Flow<List<PlayingXIEntity>>
}
