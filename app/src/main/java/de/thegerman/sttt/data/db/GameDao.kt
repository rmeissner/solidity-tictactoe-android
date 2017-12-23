package de.thegerman.sttt.data.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import de.thegerman.sttt.data.db.models.GameDb

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(game: GameDb)

}