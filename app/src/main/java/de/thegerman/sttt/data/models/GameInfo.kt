package de.thegerman.sttt.data.models

data class GameInfo(val field: List<Int>, val currentPlayer: Int, val lastMoveAt: Long, val state: Int, val playerIndex: Int? = null)