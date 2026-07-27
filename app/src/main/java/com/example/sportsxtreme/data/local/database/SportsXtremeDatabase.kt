package com.example.sportsxtreme.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sportsxtreme.data.local.dao.BallEventDao
import com.example.sportsxtreme.data.local.dao.InningsDao
import com.example.sportsxtreme.data.local.dao.MatchDao
import com.example.sportsxtreme.data.local.dao.PlayerDao
import com.example.sportsxtreme.data.local.dao.TeamDao
import com.example.sportsxtreme.data.local.entity.BallEventEntity
import com.example.sportsxtreme.data.local.entity.InningsEntity
import com.example.sportsxtreme.data.local.entity.MatchEntity
import com.example.sportsxtreme.data.local.entity.PlayerEntity
import com.example.sportsxtreme.data.local.entity.PlayingXIEntity
import com.example.sportsxtreme.data.local.entity.TeamEntity

@Database(
    entities = [
        MatchEntity::class,
        TeamEntity::class,
        PlayerEntity::class,
        PlayingXIEntity::class,
        InningsEntity::class,
        BallEventEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class SportsXtremeDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun teamDao(): TeamDao
    abstract fun playerDao(): PlayerDao
    abstract fun inningsDao(): InningsDao
    abstract fun ballEventDao(): BallEventDao
}
