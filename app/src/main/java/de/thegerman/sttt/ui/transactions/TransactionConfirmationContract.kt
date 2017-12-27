package de.thegerman.sttt.ui.transactions

import android.arch.lifecycle.ViewModel
import de.thegerman.sttt.utils.Result
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import pm.gnosis.models.Wei
import java.math.BigInteger

abstract class TransactionConfirmationContract : ViewModel() {
    abstract fun estimateTransformer(): ObservableTransformer<Pair<BigInteger, Action>, Result<Wei>>
    abstract fun confirmTransformer(): ObservableTransformer<Pair<BigInteger, Action>, Result<String>>

    sealed class Action {
        class Join : Action()
        class Cancel : Action()
        class Kick : Action()
        data class MakeMove(val field: Int) : Action()
    }
}
