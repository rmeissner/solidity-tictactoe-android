package de.thegerman.sttt.ui.details

import android.content.Intent
import android.os.Bundle
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import de.thegerman.sttt.R
import de.thegerman.sttt.di.components.AppComponent
import de.thegerman.sttt.di.components.DaggerViewComponent
import de.thegerman.sttt.di.modules.ViewModule
import de.thegerman.sttt.ui.base.InjectedActivity
import de.thegerman.sttt.utils.subscribeForResult
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.layout_game_details.*
import pm.gnosis.utils.hexAsBigIntegerOrNull
import timber.log.Timber
import java.math.BigInteger
import javax.inject.Inject

class DetailsActivity : InjectedActivity() {

    @Inject
    lateinit var viewModel: DetailsContract

    private var gameIndex: BigInteger? = null

    override fun inject(appComponent: AppComponent) {
        DaggerViewComponent.builder()
                .appComponent(appComponent)
                .viewModule(ViewModule(this))
                .build().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_game_details)
        gameIndex = intent.getStringExtra(EXTRA_GAME_INDEX).hexAsBigIntegerOrNull()
        if (gameIndex == null) {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        disposables += layout_game_details_refresh_layout.refreshes()
                .compose(viewModel.gameDetailsTransformer())
                .subscribeForResult(::onGameDetails, Timber::e)
    }

    private fun onGameDetails(details: DetailsContract.GameDetails) {
        
    }

    companion object {
        private const val EXTRA_GAME_INDEX = "extra.string.game_index"

        fun createIntent(gameIndex: BigInteger) =
                Intent().apply {
                    putExtra(EXTRA_GAME_INDEX, gameIndex.toString(16))
                }
    }
}