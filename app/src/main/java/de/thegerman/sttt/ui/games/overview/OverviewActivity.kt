package de.thegerman.sttt.ui.games.overview

import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding2.view.clicks
import de.thegerman.sttt.R
import de.thegerman.sttt.di.components.AppComponent
import de.thegerman.sttt.di.components.DaggerViewComponent
import de.thegerman.sttt.di.modules.ViewModule
import de.thegerman.sttt.ui.account.setup.AccountSetupActivity
import de.thegerman.sttt.ui.base.InjectedActivity
import de.thegerman.sttt.ui.games.details.DetailsActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.layout_game_overview.*
import pm.gnosis.utils.nullOnThrow
import timber.log.Timber
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
                .subscribe({
                    nullOnThrow { BigInteger(layout_game_overview_game_id_input.text.toString()) }?.let {
                        startActivity(DetailsActivity.createIntent(this, it))
                    }
                }, Timber::e)
        disposables += viewModel.isAccountSetup()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    layout_game_overview_setup_account_button.visibility = if (it) View.GONE else View.VISIBLE
                }, Timber::e)
        disposables += layout_game_overview_setup_account_button.clicks()
                .subscribe({
                    startActivity(AccountSetupActivity.createIntent(this))
                }, Timber::e)
    }
}