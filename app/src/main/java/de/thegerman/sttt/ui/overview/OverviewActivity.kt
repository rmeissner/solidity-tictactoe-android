package de.thegerman.sttt.ui.overview

import android.os.Bundle
import de.thegerman.sttt.di.components.AppComponent
import de.thegerman.sttt.di.components.DaggerViewComponent
import de.thegerman.sttt.di.modules.ViewModule
import de.thegerman.sttt.ui.base.InjectedActivity
import javax.inject.Inject

class OverviewActivity: InjectedActivity() {

    @Inject
    lateinit var viewModel: OverviewContract

    override fun inject(appComponent: AppComponent) {
        DaggerViewComponent.builder()
                .appComponent(appComponent)
                .viewModule(ViewModule(this))
                .build().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}