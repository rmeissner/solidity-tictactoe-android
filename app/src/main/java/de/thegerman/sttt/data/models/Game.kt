package de.thegerman.sttt.data.models

import de.thegerman.sttt.data.db.models.GameDb
import de.thegerman.sttt.data.db.models.PendingGameDb
import java.math.BigInteger

sealed class Game
data class PendingGame(val transactionHash: BigInteger, val publishedAt: Long) : Game()

fun PendingGameDb.detach() = PendingGame(transactionHash, publishedAt)

data class JoinedGame(val gameId: BigInteger, val joinedAt: Long) : Game()

fun GameDb.detach() = JoinedGame(id, joinedAt)