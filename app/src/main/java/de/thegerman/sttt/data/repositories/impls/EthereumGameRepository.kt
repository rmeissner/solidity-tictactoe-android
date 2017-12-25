package de.thegerman.sttt.data.repositories.impls

import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import de.thegerman.sttt.BuildConfig
import de.thegerman.sttt.TicTacToe
import de.thegerman.sttt.data.apis.BulkRequest
import de.thegerman.sttt.data.apis.EthereumJsonRpcApi
import de.thegerman.sttt.data.apis.bulk
import de.thegerman.sttt.data.apis.models.JsonRpcRequest
import de.thegerman.sttt.data.apis.models.TransactionCallParams
import de.thegerman.sttt.data.db.GameDao
import de.thegerman.sttt.data.models.GameInfo
import de.thegerman.sttt.data.repositories.GameRepository
import io.reactivex.Observable
import pm.gnosis.heimdall.accounts.base.repositories.AccountsRepository
import pm.gnosis.model.Solidity
import pm.gnosis.utils.asEthereumAddressStringOrNull
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EthereumGameRepository @Inject constructor(
        private val accountsRepository: AccountsRepository,
        private val rpcApi: EthereumJsonRpcApi
) : GameRepository {
    override fun loadGameDetails(gameId: BigInteger): Observable<GameInfo> =
            accountsRepository.loadActiveAccount()
                    .map { it.toOptional() }
                    .onErrorReturnItem(None)
                    .flatMapObservable { account ->
                        val from = account.toNullable()?.address?.asEthereumAddressStringOrNull()
                        rpcApi.bulk(GameInfoRequest(
                                BulkRequest.SubRequest(TransactionCallParams(to = BuildConfig.GAME_ADDRESS, data = TicTacToe.GetGameInfo.encode(Solidity.UInt256(gameId))).callRequest(1),
                                        { TicTacToe.GetGameInfo.decode(it.checkedResult()).param0.value.toString(2).padStart(64, '0') }),
                                from?.let {
                                    BulkRequest.SubRequest(TransactionCallParams(to = BuildConfig.GAME_ADDRESS, data = TicTacToe.SenderPlayerIndex.encode(Solidity.UInt256(gameId))).callRequest(2),
                                            { TicTacToe.SenderPlayerIndex.decode(it.checkedResult()).param0.value.toInt() })
                                }

                        ))
                                .map {
                                    val data = it.state.value!!
                                    val lastMove = (data.substring(0, 32).toLongOrNull(2) ?: 0L) * 1000L
                                    val state = data.substring(62, 64).toIntOrNull(2) ?: 0
                                    val currentPlayer = data.substring(60, 62).toIntOrNull(2) ?: 0
                                    val fieldData = data.substring(38, 56)
                                    val field = ArrayList<Int>(9)
                                    (8 downTo 0).mapTo(field) {
                                        fieldData.substring(it * 2, it * 2 + 2).toIntOrNull(2) ?: 0
                                    }
                                    GameInfo(field, currentPlayer, lastMove, state, it.playerIndex?.value)
                                }
                    }

    private class GameInfoRequest(val state: SubRequest<String>, val playerIndex: SubRequest<Int>?) : BulkRequest(state, playerIndex)
}