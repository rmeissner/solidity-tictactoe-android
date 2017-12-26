package de.thegerman.sttt.ui.games.overview

import android.arch.lifecycle.ViewModel
import de.thegerman.sttt.data.models.Game
import de.thegerman.sttt.ui.base.Adapter
import de.thegerman.sttt.utils.Result
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import pm.gnosis.models.Wei

abstract class OverviewContract : ViewModel() {
    abstract fun isAccountSetup(): Single<Boolean>
    abstract fun observeDeployedStatus(transactionHash: String): Observable<String>
    abstract fun observeGames(): Flowable<Result<Adapter.Data<Game>>>
    abstract fun shareAccountTransformer(): ObservableTransformer<Unit, Result<String>>
    abstract fun observeAccountBalance(): Observable<Wei>
}