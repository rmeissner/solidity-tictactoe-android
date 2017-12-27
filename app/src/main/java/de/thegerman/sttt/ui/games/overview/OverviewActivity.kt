package de.thegerman.sttt.ui.games.overview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import com.jakewharton.rxbinding2.view.clicks
import de.thegerman.sttt.R
import de.thegerman.sttt.di.components.AppComponent
import de.thegerman.sttt.di.components.DaggerViewComponent
import de.thegerman.sttt.di.modules.ViewModule
import de.thegerman.sttt.ui.account.setup.AccountSetupActivity
import de.thegerman.sttt.ui.base.InjectedActivity
import de.thegerman.sttt.ui.games.add.AddGameActivity
import de.thegerman.sttt.ui.games.details.DetailsActivity
import de.thegerman.sttt.utils.displayString
import de.thegerman.sttt.utils.subscribeForResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.layout_game_overview.*
import pm.gnosis.heimdall.common.utils.copyToClipboard
import pm.gnosis.heimdall.common.utils.snackbar
import pm.gnosis.utils.nullOnThrow
import timber.log.Timber
import java.math.BigInteger
import javax.inject.Inject

class OverviewActivity : InjectedActivity() {

    @Inject
    lateinit var viewModel: OverviewContract

    @Inject
    lateinit var adapter: GamesAdapter

    override fun inject(appComponent: AppComponent) {
        DaggerViewComponent.builder()
                .appComponent(appComponent)
                .viewModule(ViewModule(this))
                .build().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_game_overview)
        layout_game_overview_list.adapter = adapter
        layout_game_overview_list.layoutManager = LinearLayoutManager(this)
        layout_game_overview_balance.setOnClickListener {
        }
    }

    override fun onStart() {
        super.onStart()
        disposables += viewModel.observeAccountBalance()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    layout_game_overview_balance.text = it.displayString(this)
                }, {
                    layout_game_overview_balance.text = "-"
                    Timber.e(it)
                })
        disposables += layout_game_overview_balance.clicks()
                .compose(viewModel.shareAccountTransformer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeForResult({
                    copyToClipboard(getString(R.string.clipboard_address_label), it, {
                        snackbar(layout_game_overview_balance, getString(R.string.copied_address_to_clipboard))
                    })
                }, {
                    snackbar(layout_game_overview_balance, getString(R.string.copy_address_to_clipboard_error))
                    Timber.e(it)
                })
        disposables += viewModel.observeGames()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeForResult({
                    adapter.updateData(it)
                    layout_game_overview_tutorial.visibility = if (it.entries.isEmpty()) View.VISIBLE else View.GONE
                }, Timber::e)
        disposables += layout_game_overview_create_game_button.clicks()
                .subscribe({
                    startActivity(AddGameActivity.createIntent(this))
                }, Timber::e)
        disposables += layout_game_overview_setup_account_button.clicks()
                .subscribe({
                    startActivity(AccountSetupActivity.createIntent(this))
                }, Timber::e)
        disposables += viewModel.isAccountSetup()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    layout_game_overview_setup_account_button.visibility = if (it) View.GONE else View.VISIBLE
                    layout_game_overview_create_game_button.visibility = if (it) View.VISIBLE else View.GONE
                }, Timber::e)
    }

    companion object {

        fun createIntent(context: Context) =
                Intent(context, OverviewActivity::class.java)
    }
}