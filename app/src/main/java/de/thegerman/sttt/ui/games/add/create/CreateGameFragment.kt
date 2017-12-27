package de.thegerman.sttt.ui.games.add.create

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.clicks
import de.thegerman.sttt.R
import de.thegerman.sttt.di.components.AppComponent
import de.thegerman.sttt.di.components.DaggerViewComponent
import de.thegerman.sttt.di.modules.ViewModule
import de.thegerman.sttt.ui.base.InjectedFragment
import de.thegerman.sttt.ui.games.overview.OverviewActivity
import de.thegerman.sttt.utils.displayString
import de.thegerman.sttt.utils.subscribeForResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.layout_create_game.*
import pm.gnosis.heimdall.common.utils.snackbar
import timber.log.Timber
import javax.inject.Inject

class CreateGameFragment : InjectedFragment() {

    @Inject
    lateinit var viewModel: CreateGameContract

    override fun inject(appComponent: AppComponent) {
        DaggerViewComponent.builder()
                .viewModule(ViewModule(activity!!))
                .appComponent(appComponent)
                .build().inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.layout_create_game, container, false)

    override fun onStart() {
        super.onStart()
        layout_create_game_create_button.isEnabled = false
        disposables += layout_create_game_estimate.clicks().startWith(Unit)
                .doOnNext { layout_create_game_estimate.text = getString(R.string.costs_loading) }
                .compose(viewModel.estimateTransformer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeForResult({
                    layout_create_game_estimate.text = getString(R.string.costs_estimate, it.displayString(context!!))
                    layout_create_game_create_button.isEnabled = true
                }, {
                    layout_create_game_estimate.text = getString(R.string.costs_error_retry)
                    Timber.e(it)
                })
        disposables += layout_create_game_create_button.clicks()
                .doOnNext { layout_create_game_create_button.isEnabled = false }
                .compose(viewModel.createTransformer())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { layout_create_game_create_button.isEnabled = true }
                .subscribeForResult({
                    startActivity(OverviewActivity.createIntent(context!!).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                }, {
                    snackbar(view!!, R.string.error_unknown)
                    Timber.e(it)
                })
    }

    companion object {
        fun createInstance() = CreateGameFragment()
    }
}