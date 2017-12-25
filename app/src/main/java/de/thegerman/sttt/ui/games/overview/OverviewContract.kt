package de.thegerman.sttt.ui.games.overview

import android.arch.lifecycle.ViewModel
import io.reactivex.Single

abstract class OverviewContract : ViewModel() {
    abstract fun isAccountSetup(): Single<Boolean>
}