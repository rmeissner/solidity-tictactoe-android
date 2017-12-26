package de.thegerman.sttt.data.repositories.impls

import com.gojuno.koptional.None
import com.gojuno.koptional.toOptional
import de.thegerman.sttt.BuildConfig
import de.thegerman.sttt.TicTacToe
import de.thegerman.sttt.data.apis.BulkRequest
import de.thegerman.sttt.data.apis.BulkRequest.SubRequest
import de.thegerman.sttt.data.apis.EthereumJsonRpcApi
import de.thegerman.sttt.data.apis.bulk
import de.thegerman.sttt.data.apis.models.*
import de.thegerman.sttt.data.db.GameDao
import de.thegerman.sttt.data.db.models.GameDb
import de.thegerman.sttt.data.db.models.PendingGameDb
import de.thegerman.sttt.data.db.models.PendingInteactionDb
import de.thegerman.sttt.data.models.*
import de.thegerman.sttt.data.repositories.GameRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import pm.gnosis.heimdall.accounts.base.models.Account
import pm.gnosis.heimdall.accounts.base.repositories.AccountsRepository
import pm.gnosis.heimdall.data.remote.models.TransactionParameters
import pm.gnosis.model.Solidity
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import pm.gnosis.utils.*
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EthereumGameRepository @Inject constructor(
        private val accountsRepository: AccountsRepository,
        private val gameDao: GameDao,
        private val rpcApi: EthereumJsonRpcApi
) : GameRepository {
    override fun observeGames(): Flowable<List<Game>> =
            Flowable.combineLatest(
                    gameDao.observePendingGames(),
                    gameDao.observeJoinedGames(),
                    BiFunction { pending: List<PendingGameDb>, joined: List<GameDb> ->
                        pending.map { it.detach() } + joined.map { it.detach() }
                    }
            )

    override fun loadGameDetails(gameId: BigInteger): Observable<GameInfo> =
            accountsRepository.loadActiveAccount()
                    .map { it.toOptional() }
                    .onErrorReturnItem(None)
                    .flatMap { account ->
                        gameDao.loadJoinedGame(gameId).map { it.toOptional() }.onErrorReturnItem(None)
                                .map { account to it }
                    }
                    .flatMapObservable { (account, cachedGame) ->
                        val from = account.toNullable()?.address?.asEthereumAddressStringOrNull()
                        rpcApi.bulk(GameInfoRequest(
                                SubRequest(TransactionCallParams(to = BuildConfig.GAME_ADDRESS, data = TicTacToe.GetGameInfo.encode(Solidity.UInt256(gameId))).callRequest(1),
                                        { TicTacToe.GetGameInfo.decode(it.checkedResult()).param0.value.toString(2).padStart(64, '0') }),
                                from?.let {
                                    SubRequest(TransactionCallParams(from = it, to = BuildConfig.GAME_ADDRESS, data = TicTacToe.SenderPlayerIndex.encode(Solidity.UInt256(gameId))).callRequest(2),
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
                                    val localGame = cachedGame.toNullable()
                                    val remotePlayerIndex = it.playerIndex?.value
                                    var playerIndex = localGame?.playerIndex ?: remotePlayerIndex
                                    if (remotePlayerIndex != null && remotePlayerIndex > 0 && remotePlayerIndex != localGame?.playerIndex) {
                                        playerIndex = remotePlayerIndex
                                        gameDao.insert(GameDb(gameId, localGame?.joinedAt ?: System.currentTimeMillis(), remotePlayerIndex))
                                    }
                                    GameInfo(field, currentPlayer, lastMove, state, playerIndex)
                                }
                    }

    private fun estimate(paramsBuilder: (Account) -> TransactionCallParams) =
            accountsRepository.loadActiveAccount()
                    .flatMapObservable {
                        getTransactionParameters(paramsBuilder(it))
                    }
                    .map { Wei(it.gas * it.gasPrice) }

    private fun publish(paramsBuilder: (Account) -> TransactionCallParams) =
            accountsRepository.loadActiveAccount()
                    .flatMapObservable {
                        val params = paramsBuilder(it)
                        val value = params.value?.hexAsBigIntegerOrNull()?.let { Wei(it) }
                        getTransactionParameters(params)
                                .map { Transaction(params.to!!.hexAsBigInteger(), value = value, data = params.data, gas = it.gas, gasPrice = it.gasPrice, nonce = it.nonce) }
                    }
                    .flatMapSingle { accountsRepository.signTransaction(it) }
                    .flatMap {
                        rpcApi.post(JsonRpcRequest(method = "eth_sendRawTransaction", params = arrayListOf(it))).map { it.checkedResult() }
                    }

    private fun buildJoinParams(gameId: BigInteger, account: Account): TransactionCallParams {
        val from = account.address.asEthereumAddressString()
        val data = TicTacToe.Join.encode(Solidity.UInt256(gameId))
        return TransactionCallParams(from = from, to = BuildConfig.GAME_ADDRESS, data = data, value = ONE_ETHER_STRING)
    }

    override fun estimateJoinGame(gameId: BigInteger): Observable<Wei> =
            estimate { buildJoinParams(gameId, it) }

    override fun joinGame(gameId: BigInteger): Observable<String> =
            gameDao.pendingInteactionsCount(gameId)
                    .subscribeOn(Schedulers.io())
                    .flatMapObservable {
                        if (it > 0) Observable.error<String>(IllegalStateException())
                        else publish { buildJoinParams(gameId, it) }
                                .map {
                                    gameDao.insert(PendingInteactionDb.join(it.hexAsBigInteger(), gameId))
                                    gameDao.insert(GameDb(gameId, System.currentTimeMillis(), 0))
                                    it
                                }
                    }

    private fun buildCreateParams(account: Account): TransactionCallParams {
        val from = account.address.asEthereumAddressString()
        val data = TicTacToe.Create.encode()
        return TransactionCallParams(from = from, to = BuildConfig.GAME_ADDRESS, data = data, value = ONE_ETHER_STRING)
    }

    override fun estimateCreateGame(): Observable<Wei> =
            estimate { buildCreateParams(it) }

    override fun createGame(): Observable<String> =
            publish { buildCreateParams(it) }
                    .map {
                        gameDao.insert(PendingGameDb(it.hexAsBigInteger(), System.currentTimeMillis()))
                        it
                    }

    private fun buildMakeMoveParams(gameId: BigInteger, fieldNo: Int, account: Account): TransactionCallParams {
        val from = account.address.asEthereumAddressString()
        val data = TicTacToe.MakeMove.encode(Solidity.UInt256(gameId), Solidity.UInt8(BigInteger.valueOf(fieldNo.toLong())))
        return TransactionCallParams(from = from, to = BuildConfig.GAME_ADDRESS, data = data)
    }

    override fun estimateMakeMoveGame(gameId: BigInteger, fieldNo: Int): Observable<Wei> =
            estimate { buildMakeMoveParams(gameId, fieldNo, it) }

    override fun makeMove(gameId: BigInteger, fieldNo: Int): Observable<String> =
            gameDao.pendingInteactionsCount(gameId)
                    .subscribeOn(Schedulers.io())
                    .flatMapObservable {
                        if (it > 0) Observable.error<String>(IllegalStateException())
                        else publish { buildMakeMoveParams(gameId, fieldNo, it) }
                                .map {
                                    gameDao.insert(PendingInteactionDb.makeMove(it.hexAsBigInteger(), gameId, fieldNo))
                                    it
                                }
                    }

    private fun getTransactionParameters(transactionCallParams: TransactionCallParams): Observable<TransactionParameters> {
        val request = TransactionParametersRequest(
                SubRequest(JsonRpcRequest(id = 0, method = "eth_estimateGas", params = arrayListOf(transactionCallParams)), { it.checkedResult().hexAsBigInteger() }),
                SubRequest(JsonRpcRequest(id = 1, method = "eth_gasPrice"), { it.checkedResult().hexAsBigInteger() }),
                SubRequest(JsonRpcRequest(id = 2, method = "eth_getTransactionCount", params = arrayListOf(transactionCallParams.from!!.asEthereumAddressString(), DEFAULT_BLOCK_LATEST)), { it.checkedResult().hexAsBigInteger() })
        )
        return rpcApi.bulk(request).map {
            val adjustedGas = BigDecimal.valueOf(2)
                    .multiply(BigDecimal(it.estimatedGas.value)).setScale(0, BigDecimal.ROUND_UP).unscaledValue()
            TransactionParameters(adjustedGas, it.gasPrice.value!!, it.transactionCount.value!!)
        }
    }

    override fun observeDeployStatus(transactionHash: String): Observable<String> =
            rpcApi.receipt(JsonRpcRequest(method = "eth_getTransactionReceipt", params = arrayListOf(transactionHash)))
                    .flatMap {
                        it.checkedResult()?.logs?.forEach {
                            nullOnThrow { TicTacToe.Events.GameCreation.decode(it.topics, it.data) }?.let {
                                return@flatMap Observable.just(it.gameindex.value)
                            }
                        }
                        Observable.error<BigInteger>(IllegalStateException())
                    }
                    .retryWhen { it.delay(20, TimeUnit.SECONDS) }
                    .map {
                        gameDao.removePendingGame(transactionHash.hexAsBigInteger())
                        gameDao.insert(GameDb(it, System.currentTimeMillis(), 1))
                        it.asEthereumAddressString()
                    }

    override fun observeInteractionStatus(transactionHash: String): Observable<Boolean> =
            rpcApi.receipt(JsonRpcRequest(method = "eth_getTransactionReceipt", params = arrayListOf(transactionHash)))
                    .flatMap {
                        it.checkedResult()?.let {
                            it.status?.let { return@flatMap Observable.just(it) }
                        }
                        Observable.error<BigInteger>(IllegalStateException())
                    }
                    .retryWhen { it.delay(10, TimeUnit.SECONDS) }
                    .map {
                        gameDao.removePendingInteaction(transactionHash.hexAsBigInteger())
                        it == BigInteger.ONE
                    }

    override fun observePendingActions(gameId: BigInteger): Flowable<List<PendingAction>> =
            gameDao.observePendingInteactions(gameId)
                    .map {
                        it.mapNotNull { entry ->
                            when {
                                entry.action.startsWith(PendingInteactionDb.ACTION_JOIN) ->
                                    JoinPendingAction(entry.transactionHash)
                                entry.action.startsWith(PendingInteactionDb.ACTION_MAKE_MOVE) ->
                                    entry.action.substring(1).toIntOrNull()?.let { MakeMovePendingAction(entry.transactionHash, it) }
                                else -> null
                            }
                        }
                    }
                    .switchMap {
                        Flowable.merge(
                                it.map {
                                    observeInteractionStatus(it.hash.asTransactionHash())
                                            .toFlowable(BackpressureStrategy.DROP)
                                            .flatMap {
                                                Flowable.empty<List<PendingAction>>()
                                            }
                                }
                        ).startWith(Flowable.just(it))
                    }

    override fun observeGameAccountBalance(): Observable<Wei> =
            accountsRepository.loadActiveAccount()
                    .flatMapObservable {
                        rpcApi.post(
                                JsonRpcRequest(
                                        method = FUNCTION_GET_BALANCE,
                                        params = arrayListOf(it.address.asEthereumAddressString(), DEFAULT_BLOCK_LATEST)))
                                .map { Wei(it.checkedResult().hexAsBigInteger()) }
                                .repeatWhen {  it.delay(10, TimeUnit.SECONDS) }
                                .retryWhen { it.delay(20, TimeUnit.SECONDS) }
                    }

    private class TransactionParametersRequest(val estimatedGas: SubRequest<BigInteger>, val gasPrice: SubRequest<BigInteger>, val transactionCount: SubRequest<BigInteger>) :
            BulkRequest(estimatedGas, gasPrice, transactionCount)

    private class GameInfoRequest(val state: SubRequest<String>, val playerIndex: SubRequest<Int>?) : BulkRequest(state, playerIndex)

    private fun JsonRpcTransactionReceiptResult.checkedResult(): TransactionReceipt? {
        error?.let {
            throw ErrorResultException(it.message)
        }
        return result
    }

    companion object {
        const val DEFAULT_BLOCK_EARLIEST = "earliest"
        const val DEFAULT_BLOCK_LATEST = "latest"
        const val DEFAULT_BLOCK_PENDING = "pending"

        const val FUNCTION_GET_BALANCE = "eth_getBalance"

        val ONE_ETHER_STRING = "0x${BigInteger.TEN.pow(18).toString(16)}"
    }
}