package com.example.sportsxtreme.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.sportsxtreme.data.local.dao.BallEventDao
import com.example.sportsxtreme.data.local.dao.BattingDao
import com.example.sportsxtreme.data.local.dao.BowlingDao
import com.example.sportsxtreme.data.local.dao.InningsDao
import com.example.sportsxtreme.data.local.dao.LiveMatchDao
import com.example.sportsxtreme.data.local.dao.MatchDao
import com.example.sportsxtreme.data.local.dao.MatchSummaryDao
import com.example.sportsxtreme.data.local.dao.PlayerDao
import com.example.sportsxtreme.data.local.dao.SyncQueueDao
import com.example.sportsxtreme.data.local.dao.TeamDao
import com.example.sportsxtreme.data.local.entity.BallEventEntity
import com.example.sportsxtreme.data.local.entity.BattingEntity
import com.example.sportsxtreme.data.local.entity.BowlingEntity
import com.example.sportsxtreme.data.local.entity.InningsEntity
import com.example.sportsxtreme.data.local.entity.LiveMatchEntity
import com.example.sportsxtreme.data.local.entity.MatchEntity
import com.example.sportsxtreme.data.local.entity.MatchSummaryEntity
import com.example.sportsxtreme.data.local.entity.PlayerEntity
import com.example.sportsxtreme.data.local.entity.PlayingXIEntity
import com.example.sportsxtreme.data.local.entity.SyncQueueEntity
import com.example.sportsxtreme.data.local.entity.TeamEntity

@Database(
    entities = [
        MatchEntity::class,
        TeamEntity::class,
        PlayerEntity::class,
        PlayingXIEntity::class,
        InningsEntity::class,
        BallEventEntity::class,
        BattingEntity::class,
        BowlingEntity::class,
        MatchSummaryEntity::class,
        SyncQueueEntity::class,
        LiveMatchEntity::class
    ],
    version = 7,
    exportSchema = true
)
abstract class SportsXtremeDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun teamDao(): TeamDao
    abstract fun playerDao(): PlayerDao
    abstract fun inningsDao(): InningsDao
    abstract fun ballEventDao(): BallEventDao
    abstract fun battingDao(): BattingDao
    abstract fun bowlingDao(): BowlingDao
    abstract fun matchSummaryDao(): MatchSummaryDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun liveMatchDao(): LiveMatchDao

    companion object {
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS live_matches (
                        matchId TEXT NOT NULL,
                        tournamentName TEXT NOT NULL,
                        teamAName TEXT NOT NULL,
                        teamBName TEXT NOT NULL,
                        teamAShortName TEXT NOT NULL,
                        teamBShortName TEXT NOT NULL,
                        status TEXT NOT NULL,
                        score INTEGER NOT NULL,
                        wickets INTEGER NOT NULL,
                        overs TEXT NOT NULL,
                        currentRunRate REAL NOT NULL,
                        requiredRunRate REAL,
                        target INTEGER,
                        strikerName TEXT,
                        strikerRuns INTEGER NOT NULL,
                        strikerBalls INTEGER NOT NULL,
                        nonStrikerName TEXT,
                        bowlerName TEXT,
                        bowlerOvers TEXT NOT NULL,
                        bowlerRuns INTEGER NOT NULL,
                        bowlerWickets INTEGER NOT NULL,
                        matchStatusNote TEXT,
                        updatedAtEpochMs INTEGER NOT NULL,
                        PRIMARY KEY(matchId)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS batting_scorecards (
                        matchId TEXT NOT NULL,
                        inningsId TEXT NOT NULL,
                        inningsNumber INTEGER NOT NULL,
                        playerId TEXT NOT NULL,
                        runs INTEGER NOT NULL,
                        balls INTEGER NOT NULL,
                        fours INTEGER NOT NULL,
                        sixes INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        dismissalType TEXT,
                        dismissedByBowlerId TEXT,
                        dismissedByFielderId TEXT,
                        updatedAtEpochMs INTEGER NOT NULL,
                        PRIMARY KEY(matchId, inningsId, playerId)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_batting_scorecards_matchId_inningsId ON batting_scorecards(matchId, inningsId)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS bowling_scorecards (
                        matchId TEXT NOT NULL,
                        inningsId TEXT NOT NULL,
                        inningsNumber INTEGER NOT NULL,
                        playerId TEXT NOT NULL,
                        legalBalls INTEGER NOT NULL,
                        maidens INTEGER NOT NULL,
                        runsConceded INTEGER NOT NULL,
                        wickets INTEGER NOT NULL,
                        wides INTEGER NOT NULL,
                        noBalls INTEGER NOT NULL,
                        updatedAtEpochMs INTEGER NOT NULL,
                        PRIMARY KEY(matchId, inningsId, playerId)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_bowling_scorecards_matchId_inningsId ON bowling_scorecards(matchId, inningsId)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS match_summaries (
                        matchId TEXT NOT NULL,
                        inningsId TEXT NOT NULL,
                        inningsNumber INTEGER NOT NULL,
                        totalScore INTEGER NOT NULL,
                        wickets INTEGER NOT NULL,
                        legalBalls INTEGER NOT NULL,
                        target INTEGER,
                        strikerId TEXT,
                        nonStrikerId TEXT,
                        bowlerId TEXT,
                        updatedAtEpochMs INTEGER NOT NULL,
                        PRIMARY KEY(matchId, inningsId)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_match_summaries_matchId ON match_summaries(matchId)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sync_queue (
                        operationId TEXT NOT NULL,
                        matchId TEXT NOT NULL,
                        entityType TEXT NOT NULL,
                        entityId TEXT NOT NULL,
                        operationType TEXT NOT NULL,
                        state TEXT NOT NULL,
                        attemptCount INTEGER NOT NULL,
                        nextAttemptAtEpochMs INTEGER NOT NULL,
                        lastError TEXT,
                        createdAtEpochMs INTEGER NOT NULL,
                        updatedAtEpochMs INTEGER NOT NULL,
                        PRIMARY KEY(operationId)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_queue_state_nextAttemptAtEpochMs ON sync_queue(state, nextAttemptAtEpochMs)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_sync_queue_entityType_entityId ON sync_queue(entityType, entityId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_queue_matchId ON sync_queue(matchId)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE matches ADD COLUMN matchDateEpochMs INTEGER")
                db.execSQL("ALTER TABLE matches ADD COLUMN matchTime TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE matches ADD COLUMN format TEXT")
                db.execSQL("ALTER TABLE matches ADD COLUMN ballType TEXT")
            }
        }

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

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE live_matches ADD COLUMN teamAId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE live_matches ADD COLUMN teamBId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE live_matches ADD COLUMN battingTeamId TEXT DEFAULT NULL")
            }
        }
    }
}
