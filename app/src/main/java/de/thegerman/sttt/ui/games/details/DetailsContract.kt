package de.thegerman.sttt.ui.games.details

import android.arch.lifecycle.ViewModel
import de.thegerman.sttt.data.models.PendingAction
import de.thegerman.sttt.data.models.GameInfo
import de.thegerman.sttt.utils.Result
import io.reactivex.ObservableTransformer
import java.math.BigInteger

abstract class DetailsContract: ViewModel() {
    abstract fun setGameId(id: BigInteger)
    abstract fun gameDetailsTransformer(): ObservableTransformer<Unit, Result<GameData>>

    data class GameData(val info: GameInfo, val pendingActions: List<PendingAction>)
}