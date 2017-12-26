package de.thegerman.sttt.data.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import de.thegerman.sttt.data.db.models.GameDb
import de.thegerman.sttt.data.db.models.PendingGameDb
import io.reactivex.Flowable
import java.math.BigInteger

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(game: GameDb)

    @Query("SELECT * FROM ${GameDb.TABLE_NAME}")
    fun observeJoinedGames(): Flowable<List<GameDb>>

    @Query("SELECT * FROM ${PendingGameDb.TABLE_NAME}")
    fun observePendingGames(): Flowable<List<PendingGameDb>>

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(game: PendingGameDb)

    @Query("DELETE FROM ${PendingGameDb.TABLE_NAME} WHERE ${PendingGameDb.COL_TX_HASH} = :hash")
    fun removePendingGame(hash: BigInteger)

}