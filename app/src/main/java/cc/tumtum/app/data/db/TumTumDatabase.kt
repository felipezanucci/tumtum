package cc.tumtum.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Persistência local primeiro (§2). O feed social vem depois do backend. */
@Database(
    entities = [
        EventEntity::class, NightEntity::class, SampleEntity::class, MomentEntity::class,
        BleSampleEntity::class, RrIntervalEntity::class, MotionEntity::class, ConnectionEventEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class TumTumDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun nightDao(): NightDao
    abstract fun captureDao(): CaptureDao

    companion object {
        /** v1 (b5, só Health Connect) → v2 (captura BLE ao vivo). Nada é perdido. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE events ADD COLUMN clockOffsetStartMs INTEGER")
                db.execSQL("ALTER TABLE events ADD COLUMN clockOffsetEndMs INTEGER")
                db.execSQL("ALTER TABLE nights ADD COLUMN clockOffsetStartMs INTEGER")
                db.execSQL("ALTER TABLE nights ADD COLUMN clockOffsetEndMs INTEGER")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `ble_samples` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `eventId` INTEGER NOT NULL, " +
                        "`wallClockMs` INTEGER NOT NULL, `elapsedRealtimeMs` INTEGER NOT NULL, " +
                        "`bpm` INTEGER NOT NULL, `contactStatus` INTEGER NOT NULL)",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ble_samples_eventId` ON `ble_samples` (`eventId`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `rr_intervals` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `eventId` INTEGER NOT NULL, " +
                        "`wallClockMs` INTEGER NOT NULL, `elapsedRealtimeMs` INTEGER NOT NULL, `rrMs` REAL NOT NULL)",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_rr_intervals_eventId` ON `rr_intervals` (`eventId`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `motion` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `eventId` INTEGER NOT NULL, " +
                        "`wallClockMs` INTEGER NOT NULL, `elapsedRealtimeMs` INTEGER NOT NULL, " +
                        "`magMean` REAL NOT NULL, `magStd` REAL NOT NULL)",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_motion_eventId` ON `motion` (`eventId`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `connection_events` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `eventId` INTEGER NOT NULL, " +
                        "`wallClockMs` INTEGER NOT NULL, `elapsedRealtimeMs` INTEGER NOT NULL, " +
                        "`type` TEXT NOT NULL, `detail` TEXT NOT NULL, `rssi` INTEGER)",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_connection_events_eventId` ON `connection_events` (`eventId`)")
            }
        }

        fun build(context: Context): TumTumDatabase =
            Room.databaseBuilder(context, TumTumDatabase::class.java, "tumtum.db")
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
