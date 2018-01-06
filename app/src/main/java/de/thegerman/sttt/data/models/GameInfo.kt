package de.thegerman.sttt.data.models

data class GameInfo(
        val fields: List<Int>,
        val currentPlayer: Int,
        val lastMoveAt: Long,
        val maxMoveTime: Long,
        val state: Int,
        val playerIndex: Int? = null,
        val canPlayerBeKicked: Boolean
)