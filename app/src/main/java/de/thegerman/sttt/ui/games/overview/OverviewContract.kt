package de.thegerman.sttt.ui.games.overview

import android.arch.lifecycle.ViewModel
import de.thegerman.sttt.data.models.Game
import de.thegerman.sttt.ui.base.Adapter
import de.thegerman.sttt.utils.Result
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

abstract class OverviewContract : ViewModel() {
    abstract fun isAccountSetup(): Single<Boolean>
    abstract fun observeDeployedStatus(transactionHash: String): Observable<String>
    abstract fun observeGames(): Flowable<Result<Adapter.Data<Game>>>
}