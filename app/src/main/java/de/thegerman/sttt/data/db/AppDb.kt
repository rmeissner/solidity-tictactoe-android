package de.thegerman.sttt.data.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import de.thegerman.sttt.data.db.models.GameDb

@Database(entities = [
    GameDb::class
], version = 1)
@TypeConverters(BigIntegerConverter::class)
abstract class AppDb : RoomDatabase() {
    companion object {
        const val DB_NAME = "app-db"
    }

    abstract fun gameDao(): GameDao
}
