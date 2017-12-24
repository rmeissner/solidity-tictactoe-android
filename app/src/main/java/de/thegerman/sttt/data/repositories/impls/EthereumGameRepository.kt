package de.thegerman.sttt.data.repositories.impls

import de.thegerman.sttt.BuildConfig
import de.thegerman.sttt.TicTacToe
import de.thegerman.sttt.data.apis.EthereumJsonRpcApi
import de.thegerman.sttt.data.apis.models.JsonRpcRequest
import de.thegerman.sttt.data.apis.models.TransactionCallParams
import de.thegerman.sttt.data.db.GameDao
import de.thegerman.sttt.data.models.GameInfo
import de.thegerman.sttt.data.repositories.GameRepository
import io.reactivex.Observable
import pm.gnosis.model.Solidity
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EthereumGameRepository @Inject constructor(
        private val rpcApi: EthereumJsonRpcApi
) : GameRepository {
    override fun loadGameDetails(gameId: BigInteger): Observable<GameInfo> =
            Observable.fromCallable {
                TicTacToe.GetGameInfo.encode(Solidity.UInt256(gameId))
            }.flatMap {
                rpcApi.post(TransactionCallParams(to = BuildConfig.GAME_ADDRESS, data = it).callRequest(1))
                        .map {
                            val data = TicTacToe.GetGameInfo.decode(it.checkedResult()).param0.value.toString(2).padStart(64, '0')
                            val lastMove = (data.substring(0, 32).toLongOrNull(2) ?: 0L) * 1000L
                            val state = data.substring(62, 64).toIntOrNull(2) ?: 0
                            val currentPlayer = data.substring(60, 62).toIntOrNull(2) ?: 0
                            val fieldData = data.substring(38, 56)
                            val field = ArrayList<Int>(9)
                            (8 downTo 0).mapTo(field) {
                                fieldData.substring(it * 2, it * 2 + 2).toIntOrNull(2) ?: 0
                            }
                            GameInfo(field, currentPlayer, lastMove, state)
                        }
            }
}