package de.thegerman.sttt.data.db.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.math.BigInteger

@Entity(tableName = PendingInteactionDb.TABLE_NAME)
data class PendingInteactionDb(
        @PrimaryKey
        @ColumnInfo(name = COL_TX_HASH)
        var transactionHash: BigInteger,

        @ColumnInfo(name = COL_GAME_ID)
        var gameId: BigInteger,

        @ColumnInfo(name = COL_PUBLISHED_AT)
        var publishedAt: Long,

        @ColumnInfo(name = COL_ACTION)
        var action: String
) {
    companion object {
        const val TABLE_NAME = "ttt_pending_interaction"
        const val COL_TX_HASH = "transactionHash"
        const val COL_GAME_ID = "gameId"
        const val COL_PUBLISHED_AT = "publishedAt"
        const val COL_ACTION = "action"

        const val ACTION_JOIN = "J"
        const val ACTION_MAKE_MOVE = "M"
        const val ACTION_CANCEL = "C"
        const val ACTION_KICK = "K"
        fun join(txHash: BigInteger, gameId: BigInteger) = PendingInteactionDb(txHash, gameId, System.currentTimeMillis(), ACTION_JOIN)
        fun makeMove(txHash: BigInteger, gameId: BigInteger, field: Int) = PendingInteactionDb(txHash, gameId, System.currentTimeMillis(), "$ACTION_MAKE_MOVE$field")
        fun cancel(txHash: BigInteger, gameId: BigInteger) = PendingInteactionDb(txHash, gameId, System.currentTimeMillis(), ACTION_CANCEL)
        fun kick(txHash: BigInteger, gameId: BigInteger) = PendingInteactionDb(txHash, gameId, System.currentTimeMillis(), ACTION_KICK)
    }
}