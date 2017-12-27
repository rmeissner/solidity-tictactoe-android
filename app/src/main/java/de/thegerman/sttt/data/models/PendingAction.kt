package de.thegerman.sttt.data.models

import java.math.BigInteger

sealed class PendingAction(val hash: BigInteger)
data class JoinPendingAction(private val _hash: BigInteger) : PendingAction(_hash)
data class MakeMovePendingAction(private val _hash: BigInteger, val fieldNo: Int): PendingAction(_hash)
data class CancelPendingAction(private val _hash: BigInteger): PendingAction(_hash)
data class KickPendingAction(private val _hash: BigInteger): PendingAction(_hash)