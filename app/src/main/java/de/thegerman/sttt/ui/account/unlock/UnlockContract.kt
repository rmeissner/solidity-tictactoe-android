package de.thegerman.sttt.ui.account.unlock

import android.arch.lifecycle.ViewModel
import de.thegerman.sttt.utils.Result
import io.reactivex.ObservableTransformer
import io.reactivex.Single

abstract class UnlockContract: ViewModel() {
    abstract fun loadState(): Single<State>

    abstract fun unlockTransformer(): ObservableTransformer<Pair<String, String>, Result<State>>

    enum class State {
        CLEAN,
        LOCKED,
        UNLOCKED
    }
}