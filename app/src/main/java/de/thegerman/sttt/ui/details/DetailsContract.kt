package de.thegerman.sttt.ui.details

import android.arch.lifecycle.ViewModel
import de.thegerman.sttt.utils.Result
import io.reactivex.ObservableTransformer

abstract class DetailsContract: ViewModel() {
    abstract fun gameDetailsTransformer(): ObservableTransformer<Unit, Result<GameDetails>>

    data class GameDetails(val state: Int)
}