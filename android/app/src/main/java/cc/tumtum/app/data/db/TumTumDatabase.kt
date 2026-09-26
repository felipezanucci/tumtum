package cc.tumtum.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/** Persistência local primeiro (§2). O feed social vem depois do backend. */
@Database(
    entities = [
        EventEntity::class, NightEntity::class, SampleEntity::class, MomentEntity::class, MarkEntity::class,
        BleSampleEntity::class, RrIntervalEntity::class, MotionEntity::class, ConnectionEventEntity::class,
    ],
    version = 9,
    exportSchema = false,
)
abstract class TumTumDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun nightDao(): NightDao
    abstract fun captureDao(): CaptureDao
    abstract fun markDao(): MarkDao

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

        /** v2 → v3: a trava da revela (protocolo do dia 25). */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE nights ADD COLUMN revealAt INTEGER")
            }
        }

        /** v3 → v4: the night knows where it stands with the server (Etapa 2). Existing nights start PENDING and upload on the next start. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE nights ADD COLUMN serverSessionId TEXT")
                db.execSQL("ALTER TABLE nights ADD COLUMN uploadState TEXT NOT NULL DEFAULT 'PENDING'")
                db.execSQL("ALTER TABLE nights ADD COLUMN uploadError TEXT")
                db.execSQL("ALTER TABLE nights ADD COLUMN momentsSource TEXT NOT NULL DEFAULT 'LOCAL'")
                db.execSQL("ALTER TABLE moments ADD COLUMN label TEXT")
            }
        }

        /** v4 → v5: the event knows its server twin and its kind; marks get a table (Etapa 3). */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE events ADD COLUMN serverEventId TEXT")
                db.execSQL("ALTER TABLE events ADD COLUMN eventType TEXT NOT NULL DEFAULT 'concert'")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `marks` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `eventId` INTEGER NOT NULL, " +
                        "`at` INTEGER NOT NULL, `label` TEXT NOT NULL, `entryType` TEXT NOT NULL, " +
                        "`synced` INTEGER NOT NULL DEFAULT 0)",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_marks_eventId` ON `marks` (`eventId`)")
            }
        }

        /** v5 → v6: the night keeps the photo behind its last shared card (21/09). */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE nights ADD COLUMN photoPath TEXT")
            }
        }

        /** v6 → v7: a moment keeps the server's guesses at its name (22/09). */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE moments ADD COLUMN candidates TEXT")
            }
        }

        /**
         * v7 → v8: a night knows which account uploaded it (#58, 23/09).
         * Felipe signed in as another account and the app offered the other
         * account's night as his; the server refused and the app hid why.
         */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE nights ADD COLUMN ownerUserId TEXT")
            }
        }

        /**
         * v8 → v9 (LGPD remediation, 26/09): **a night goes to the server only
         * when the person asks.** Until now every night was uploaded on its
         * own at the end of the capture and retried on every start, while the
         * onboarding promised "Nada deixa seu aparelho sem você mandar".
         * `sendRequested` is that ask; `sentAt` is when it reached the server,
         * so the screen can say so. A night already on the server was sent by
         * the rules of its time and keeps going (its analysis may be pending);
         * a night never sent stays on the phone until its owner taps.
         */
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE nights ADD COLUMN sendRequested INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE nights ADD COLUMN sentAt INTEGER")
                db.execSQL("UPDATE nights SET sendRequested = 1 WHERE serverSessionId IS NOT NULL")
            }
        }

        private const val NAME = "tumtum.db"

        /**
         * Encrypted at rest since 26/09 (SQLCipher): heart-rate readings are
         * health data, and until then they sat in a plain SQLite file. The
         * key is this phone's own ([DatabaseKey]); an older, unencrypted
         * file is converted once, before Room opens it.
         */
        fun build(context: Context): TumTumDatabase {
            System.loadLibrary("sqlcipher")
            val (passphrase, keyIsNew) = DatabaseKey.passphrase(context)
            DatabaseKey.prepare(context, NAME, passphrase, keyIsNew)
            return Room.databaseBuilder(context, TumTumDatabase::class.java, NAME)
                .openHelperFactory(SupportOpenHelperFactory(passphrase.toByteArray(Charsets.UTF_8)))
                .addMigrations(
                    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7,
                    MIGRATION_7_8, MIGRATION_8_9,
                )
                .build()
        }
    }
}
