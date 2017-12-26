package de.thegerman.sttt.data.db.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.math.BigInteger

@Entity(tableName = PendingGameDb.TABLE_NAME)
data class PendingGameDb(
        @PrimaryKey
        @ColumnInfo(name = COL_TX_HASH)
        var transactionHash: BigInteger,

        @ColumnInfo(name = COL_PUBLISHED_AT)
        var publishedAt: Long
) {
    companion object {
        const val TABLE_NAME = "ttt_pending_games"
        const val COL_TX_HASH = "transactionHash"
        const val COL_PUBLISHED_AT = "publishedAt"
    }
}