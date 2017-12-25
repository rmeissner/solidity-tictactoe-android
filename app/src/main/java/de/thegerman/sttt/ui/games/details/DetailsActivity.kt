package de.thegerman.sttt.ui.games.details

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.format.DateUtils
import android.view.View
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import com.jakewharton.rxbinding2.view.clicks
import de.thegerman.sttt.R
import de.thegerman.sttt.data.models.GameInfo
import de.thegerman.sttt.di.components.AppComponent
import de.thegerman.sttt.di.components.DaggerViewComponent
import de.thegerman.sttt.di.modules.ViewModule
import de.thegerman.sttt.ui.account.setup.AccountSetupActivity
import de.thegerman.sttt.ui.base.InjectedActivity
import de.thegerman.sttt.utils.subscribeForResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.layout_game_details.*
import pm.gnosis.heimdall.common.utils.snackbar
import pm.gnosis.heimdall.common.utils.toast
import pm.gnosis.utils.asDecimalString
import pm.gnosis.utils.hexAsBigIntegerOrNull
import timber.log.Timber
import java.math.BigInteger
import javax.inject.Inject

class DetailsActivity : InjectedActivity() {

    @Inject
    lateinit var viewModel: DetailsContract

    private var gameIndex: BigInteger? = null
    private var hasGameInfo: Boolean = false

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
            return
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.game_x, gameIndex?.asDecimalString())
        viewModel.setGameId(gameIndex!!)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onStart() {
        super.onStart()
        layout_game_details_setup_button.visibility = View.GONE
        layout_game_details_join_button.visibility = View.GONE
        disposables += layout_game_details_refresh_layout.refreshes()
                .doOnSubscribe { layout_game_details_refresh_layout.isRefreshing = true }
                .compose(viewModel.gameDetailsTransformer())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { layout_game_details_refresh_layout.isRefreshing = false }
                .subscribeForResult(::onGameDetails, ::onGameDetailsError)
        disposables += layout_game_details_setup_button.clicks()
                .subscribe({
                    startActivity(AccountSetupActivity.createIntent(this))
                }, Timber::e)
        disposables += layout_game_details_join_button.clicks()
                .subscribe({
                    toast("Join game")
                }, Timber::e)
    }

    private fun onGameDetailsError(throwable: Throwable) {
        if (!hasGameInfo) {
            layout_game_details_state.text = getString(R.string.error_game_info)
            layout_game_details_last_move.text = null
        }
        snackbar(layout_game_details_state, getString(R.string.error_game_info_will_retry))
    }

    private fun onGameDetails(details: GameInfo) {
        hasGameInfo = true
        layout_game_details_player_info.text = when (details.playerIndex) {
            null -> {
                null
            }
            0 -> {
                getString(R.string.not_player_of_game)
            }
            else -> {
                getString(R.string.you_are_player_x, details.playerIndex.toString())
            }
        }
        layout_game_details_state.text = when (details.state) {
            0 -> getString(R.string.waiting_for_player)
            1 -> getString(R.string.player_x_turn, details.currentPlayer.toString())
            else -> when (details.currentPlayer) {
                1, 2 -> getString(R.string.player_x_won, details.currentPlayer.toString())
                else -> getString(R.string.game_over_tie)
            }
        }
        layout_game_details_last_move.text = when (details.state) {
            0 -> {
                details.playerIndex?.let {
                    layout_game_details_setup_button.visibility = View.GONE
                    layout_game_details_join_button.visibility = if (it == 1) View.GONE else View.VISIBLE
                } ?: run {
                    layout_game_details_setup_button.visibility = View.VISIBLE
                    layout_game_details_join_button.visibility = View.GONE
                }
                getString(R.string.game_not_started)
            }
            else -> {
                layout_game_details_setup_button.visibility = View.GONE
                layout_game_details_join_button.visibility = View.GONE
                getString(R.string.last_move_at, DateUtils.formatDateTime(this, details.lastMoveAt, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME))
            }
        }
        updateField(details.field)
    }

    private fun updateField(field: List<Int>) {
        layout_game_details_field_0.setImageDrawable(fieldDrawable(field.getOrNull(0) ?: 0))
        layout_game_details_field_1.setImageDrawable(fieldDrawable(field.getOrNull(1) ?: 0))
        layout_game_details_field_2.setImageDrawable(fieldDrawable(field.getOrNull(2) ?: 0))
        layout_game_details_field_3.setImageDrawable(fieldDrawable(field.getOrNull(3) ?: 0))
        layout_game_details_field_4.setImageDrawable(fieldDrawable(field.getOrNull(4) ?: 0))
        layout_game_details_field_5.setImageDrawable(fieldDrawable(field.getOrNull(5) ?: 0))
        layout_game_details_field_6.setImageDrawable(fieldDrawable(field.getOrNull(6) ?: 0))
        layout_game_details_field_7.setImageDrawable(fieldDrawable(field.getOrNull(7) ?: 0))
        layout_game_details_field_8.setImageDrawable(fieldDrawable(field.getOrNull(8) ?: 0))
    }

    private fun fieldDrawable(playerIndex: Int): Drawable? =
            when (playerIndex) {
                1 -> ContextCompat.getDrawable(this, R.drawable.ic_player_o)
                2 -> ContextCompat.getDrawable(this, R.drawable.ic_player_x)
                else -> null
            }

    companion object {
        private const val EXTRA_GAME_INDEX = "extra.string.game_index"

        fun createIntent(context: Context, gameIndex: BigInteger) =
                Intent(context, DetailsActivity::class.java).apply {
                    putExtra(EXTRA_GAME_INDEX, gameIndex.toString(16))
                }
    }
}