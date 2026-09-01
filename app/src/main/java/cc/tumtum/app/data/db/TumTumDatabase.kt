package cc.tumtum.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Persistência local primeiro (§2). O feed social vem depois do backend. */
@Database(
    entities = [EventEntity::class, NightEntity::class, SampleEntity::class, MomentEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class TumTumDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun nightDao(): NightDao

    companion object {
        fun build(context: Context): TumTumDatabase =
            Room.databaseBuilder(context, TumTumDatabase::class.java, "tumtum.db").build()
    }
}
