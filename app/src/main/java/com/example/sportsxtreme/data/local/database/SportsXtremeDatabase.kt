package com.example.sportsxtreme.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = true
)
abstract class SportsXtremeDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun teamDao(): TeamDao
    abstract fun playerDao(): PlayerDao
    abstract fun inningsDao(): InningsDao
    abstract fun ballEventDao(): BallEventDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS ball_events_new (
                        ballId TEXT NOT NULL,
                        matchId TEXT NOT NULL,
                        inningsId TEXT NOT NULL,
                        inningsNumber INTEGER NOT NULL,
                        sequenceNumber INTEGER NOT NULL,
                        overNumber INTEGER NOT NULL,
                        ballNumber INTEGER NOT NULL,
                        legalBallNumber INTEGER NOT NULL,
                        battingTeamId TEXT NOT NULL,
                        bowlingTeamId TEXT NOT NULL,
                        strikerId TEXT NOT NULL,
                        nonStrikerId TEXT NOT NULL,
                        bowlerId TEXT NOT NULL,
                        runs INTEGER NOT NULL,
                        extraType TEXT NOT NULL,
                        extraRuns INTEGER NOT NULL,
                        additionalExtrasJson TEXT NOT NULL,
                        dismissalType TEXT NOT NULL,
                        dismissedPlayerId TEXT,
                        dismissalAssistPlayerIds TEXT NOT NULL,
                        isLegalDelivery INTEGER NOT NULL,
                        eventType TEXT NOT NULL,
                        reversedEventId TEXT,
                        comment TEXT,
                        recordedByUserId TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        previousEventId TEXT,
                        metadataJson TEXT NOT NULL,
                        syncState TEXT NOT NULL,
                        PRIMARY KEY(ballId)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO ball_events_new (
                        ballId, matchId, inningsId, inningsNumber, sequenceNumber, overNumber,
                        ballNumber, legalBallNumber, battingTeamId, bowlingTeamId, strikerId,
                        nonStrikerId, bowlerId, runs, extraType, extraRuns, additionalExtrasJson,
                        dismissalType, dismissedPlayerId, dismissalAssistPlayerIds, isLegalDelivery,
                        eventType, reversedEventId, comment, recordedByUserId, timestamp,
                        previousEventId, metadataJson, syncState
                    )
                    SELECT
                        ballId, matchId, inningsId, inningsNumber, sequenceNumber, overNumber,
                        ballNumber, legalBallNumber, battingTeamId, bowlingTeamId, strikerId,
                        nonStrikerId, bowlerId, runs,
                        CASE
                            WHEN wides > 0 THEN 'WIDE'
                            WHEN noBalls > 0 THEN 'NO_BALL'
                            WHEN byes > 0 THEN 'BYE'
                            WHEN legByes > 0 THEN 'LEG_BYE'
                            WHEN penaltyRuns > 0 THEN 'PENALTY'
                            ELSE 'NONE'
                        END,
                        CASE
                            WHEN wides > 0 THEN wides
                            WHEN noBalls > 0 THEN noBalls
                            WHEN byes > 0 THEN byes
                            WHEN legByes > 0 THEN legByes
                            ELSE penaltyRuns
                        END,
                        '[]',
                        CASE
                            WHEN wicketType IS NULL THEN 'NONE'
                            WHEN wicketType = 'RETIRED' THEN 'RETIRED_OUT'
                            ELSE wicketType
                        END,
                        wicketPlayerId, wicketAssistPlayerIds, isLegalDelivery,
                        'DELIVERY', NULL, NULL, recordedByUserId, timestamp,
                        previousEventId, metadataJson,
                        CASE WHEN synced = 1 THEN 'SYNCED' ELSE 'PENDING' END
                    FROM ball_events
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE ball_events")
                db.execSQL("ALTER TABLE ball_events_new RENAME TO ball_events")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ball_events_matchId ON ball_events(matchId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ball_events_inningsId ON ball_events(inningsId)")
            }
        }
    }
}
