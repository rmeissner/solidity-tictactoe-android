package de.thegerman.sttt.ui.games.add.create

import android.arch.lifecycle.ViewModel
import de.thegerman.sttt.utils.Result
import io.reactivex.ObservableTransformer
import pm.gnosis.models.Wei

abstract class CreateGameContract: ViewModel() {
    abstract fun createTransformer(): ObservableTransformer<Unit, Result<String>>
    abstract fun estimateTransformer(): ObservableTransformer<Unit, Result<Wei>>
}