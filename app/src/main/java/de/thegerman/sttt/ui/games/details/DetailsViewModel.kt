package de.thegerman.sttt.ui.games.details

import de.thegerman.sttt.data.models.PendingAction
import de.thegerman.sttt.data.models.GameInfo
import de.thegerman.sttt.data.repositories.GameRepository
import de.thegerman.sttt.utils.Result
import de.thegerman.sttt.utils.mapToResult
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import java.math.BigInteger
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class DetailsViewModel @Inject constructor(
        private val gameRepository: GameRepository
) : DetailsContract() {

    private val gameOver = AtomicBoolean(false)
    private var gameId: BigInteger? = null

    override fun setGameId(id: BigInteger) {
        gameId = id
    }

    override fun gameDetailsTransformer() = ObservableTransformer<Unit, Result<GameData>> {
        Observable.combineLatest(it.startWith(Unit).switchMap {
            gameRepository.loadGameDetails(gameId!!)
                    .doOnNext { gameOver.set(it.state > 1) }
                    .repeatWhen { flatMapForRepeat(it) }
        }, gameRepository.observePendingActions(gameId!!).toObservable(),
                BiFunction { info: GameInfo, actions: List<PendingAction> ->
                    GameData(info, actions)
                })
                .mapToResult()

    }

    private fun <T> flatMapForRepeat(it: Observable<T>, delayS: Long = 1): Observable<T> =
            it.flatMap {
                if (gameOver.get()) Observable.empty<T>()
                else Observable.just(it).delay(delayS, TimeUnit.SECONDS)
            }
}