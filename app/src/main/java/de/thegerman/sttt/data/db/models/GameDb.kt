package de.thegerman.sttt.data.db.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.math.BigInteger

@Entity(tableName = GameDb.TABLE_NAME)
data class GameDb(
        @PrimaryKey
        @ColumnInfo(name = COL_ID)
        var id: BigInteger,

        @ColumnInfo(name = COL_JOINED_AT)
        var joinedAt: Long
) {
    companion object {
        const val TABLE_NAME = "ttt_games"
        const val COL_ID = "id"
        const val COL_JOINED_AT = "joined_at"
    }
}