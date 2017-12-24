package de.thegerman.sttt.ui.overview

import android.os.Bundle
import com.jakewharton.rxbinding2.view.clicks
import de.thegerman.sttt.R
import de.thegerman.sttt.di.components.AppComponent
import de.thegerman.sttt.di.components.DaggerViewComponent
import de.thegerman.sttt.di.modules.ViewModule
import de.thegerman.sttt.ui.base.InjectedActivity
import de.thegerman.sttt.ui.details.DetailsActivity
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.layout_game_overview.*
import pm.gnosis.utils.nullOnThrow
import java.math.BigInteger
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
        setContentView(R.layout.layout_game_overview)
    }

    override fun onStart() {
        super.onStart()
        disposables += layout_game_overview_open_button.clicks()
                .subscribe {
                    nullOnThrow { BigInteger(layout_game_overview_game_id_input.text.toString()) }?.let {
                        startActivity(DetailsActivity.createIntent(this, it))
                    }
                }
    }
}