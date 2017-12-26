package de.thegerman.sttt.data.db

import android.arch.persistence.room.*
import de.thegerman.sttt.data.db.models.GameDb
import de.thegerman.sttt.data.db.models.PendingGameDb
import de.thegerman.sttt.data.db.models.PendingInteactionDb
import de.thegerman.sttt.data.models.Game
import io.reactivex.Flowable
import io.reactivex.Single
import java.math.BigInteger

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(game: GameDb)

    @Query("SELECT * FROM ${GameDb.TABLE_NAME} ORDER BY ${GameDb.COL_JOINED_AT} DESC")
    fun observeJoinedGames(): Flowable<List<GameDb>>

    @Query("SELECT * FROM ${GameDb.TABLE_NAME} WHERE ${GameDb.COL_ID} = :id")
    fun loadJoinedGame(id: BigInteger): Single<GameDb>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(game: PendingGameDb)

    @Query("DELETE FROM ${PendingGameDb.TABLE_NAME} WHERE ${PendingGameDb.COL_TX_HASH} = :hash")
    fun removePendingGame(hash: BigInteger)

    @Query("SELECT * FROM ${PendingGameDb.TABLE_NAME} ORDER BY ${PendingGameDb.COL_PUBLISHED_AT} DESC")
    fun observePendingGames(): Flowable<List<PendingGameDb>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(game: PendingInteactionDb)

    @Query("DELETE FROM ${PendingInteactionDb.TABLE_NAME} WHERE ${PendingInteactionDb.COL_TX_HASH} = :hash")
    fun removePendingInteaction(hash: BigInteger)

    @Query("SELECT * FROM ${PendingInteactionDb.TABLE_NAME} WHERE ${PendingInteactionDb.COL_GAME_ID} = :gameId")
    fun observePendingInteactions(gameId: BigInteger): Flowable<List<PendingInteactionDb>>

    @Query("SELECT Count(*) FROM ${PendingInteactionDb.TABLE_NAME} WHERE ${PendingInteactionDb.COL_GAME_ID} = :gameId")
    fun pendingInteactionsCount(gameId: BigInteger): Single<Long>

}