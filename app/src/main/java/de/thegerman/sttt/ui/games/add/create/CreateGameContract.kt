package de.thegerman.sttt.ui.games.add.create

import android.arch.lifecycle.ViewModel
import de.thegerman.sttt.utils.Result
import io.reactivex.ObservableTransformer

abstract class CreateGameContract: ViewModel() {
    abstract fun createTransformer(): ObservableTransformer<Unit, Result<String>>
}