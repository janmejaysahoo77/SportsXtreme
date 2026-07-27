package com.example.sportsxtreme.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sportsxtreme.data.local.entity.TeamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<TeamEntity>)

    @Query("SELECT * FROM teams WHERE teamId = :teamId")
    suspend fun getTeam(teamId: String): TeamEntity?

    @Query("SELECT * FROM teams WHERE teamId = :teamId")
    fun observeTeam(teamId: String): Flow<TeamEntity?>

    @Query("SELECT * FROM teams WHERE type = 'FRIENDLY_TEST' ORDER BY isTeamA DESC, teamName")
    suspend fun getFriendlyTestTeams(): List<TeamEntity>
}
