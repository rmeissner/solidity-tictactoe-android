package de.thegerman.sttt.data.repositories

import de.thegerman.sttt.data.models.PendingAction
import de.thegerman.sttt.data.models.Game
import de.thegerman.sttt.data.models.GameInfo
import io.reactivex.Flowable
import io.reactivex.Observable
import pm.gnosis.models.Wei
import java.math.BigInteger

interface GameRepository {
    fun loadGameDetails(gameId: BigInteger): Observable<GameInfo>
    fun estimateJoinGame(gameId: BigInteger): Observable<Wei>
    fun joinGame(gameId: BigInteger): Observable<String>
    fun estimateCreateGame(): Observable<Wei>
    fun createGame(): Observable<String>
    fun estimateMakeMoveGame(gameId: BigInteger, fieldNo: Int): Observable<Wei>
    fun makeMove(gameId: BigInteger, fieldNo: Int): Observable<String>
    fun observeGames(): Flowable<List<Game>>
    fun observeDeployStatus(transactionHash: String): Observable<String>
    fun observeInteractionStatus(transactionHash: String): Observable<Boolean>
    fun observePendingActions(gameId: BigInteger): Flowable<List<PendingAction>>
    fun observeGameAccountBalance(): Observable<Wei>
}