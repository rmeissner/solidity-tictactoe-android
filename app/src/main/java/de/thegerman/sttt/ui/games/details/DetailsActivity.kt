package de.thegerman.sttt.ui.games.details

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.text.format.DateUtils
import android.view.View
import android.widget.ImageView
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import com.jakewharton.rxbinding2.view.clicks
import de.thegerman.sttt.R
import de.thegerman.sttt.data.models.JoinPendingAction
import de.thegerman.sttt.data.models.MakeMovePendingAction
import de.thegerman.sttt.di.components.AppComponent
import de.thegerman.sttt.di.components.DaggerViewComponent
import de.thegerman.sttt.di.modules.ViewModule
import de.thegerman.sttt.ui.account.setup.AccountSetupActivity
import de.thegerman.sttt.ui.base.InjectedActivity
import de.thegerman.sttt.utils.subscribeForResult
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.layout_game_details.*
import pm.gnosis.heimdall.common.utils.snackbar
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
        registerToolbar(layout_game_details_toolbar)
        layout_game_details_toolbar.title = getString(R.string.game_x, gameIndex?.asDecimalString())
        viewModel.setGameId(gameIndex!!)
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
                .compose(viewModel.joinTransformer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeForResult({
                    layout_game_details_join_button.visibility = View.GONE
                }, {
                    snackbar(layout_game_details_join_button, R.string.error_unknown)
                    Timber.e(it)
                })
        disposables += makeMove(
                layout_game_details_field_0.clicks().map { 0 },
                layout_game_details_field_1.clicks().map { 1 },
                layout_game_details_field_2.clicks().map { 2 },
                layout_game_details_field_3.clicks().map { 3 },
                layout_game_details_field_4.clicks().map { 4 },
                layout_game_details_field_5.clicks().map { 5 },
                layout_game_details_field_6.clicks().map { 6 },
                layout_game_details_field_7.clicks().map { 7 },
                layout_game_details_field_8.clicks().map { 8 }
        )
    }

    private fun makeMove(vararg inputs: Observable<Int>) =
            Observable.merge(mutableListOf(*inputs))
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { toggleFields(false) }
                    .compose(viewModel.makeMoveTransformer())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeForResult({
                        snackbar(layout_game_details_join_button, getString(R.string.move_published))
                    }, {
                        toggleFields(true)
                        snackbar(layout_game_details_join_button, R.string.error_unknown)
                        Timber.e(it)
                    })


    private fun onGameDetailsError(throwable: Throwable) {
        if (!hasGameInfo) {
            layout_game_details_state.text = getString(R.string.error_game_info)
            layout_game_details_last_move.text = null
        }
        snackbar(layout_game_details_state, getString(R.string.error_game_info_will_retry))
    }

    private fun onGameDetails(data: DetailsContract.GameData) {
        val details = data.info
        hasGameInfo = true
        layout_game_details_pending_actions.text = getString(R.string.x_pending_actions, data.pendingActions.size.toString())
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
        val isJoining = data.pendingActions.any { it is JoinPendingAction }
        layout_game_details_last_move.text = when (details.state) {
            0 -> {
                details.playerIndex?.let {
                    layout_game_details_setup_button.visibility = View.GONE
                    layout_game_details_join_button.visibility = if (it == 1 || isJoining) View.GONE else View.VISIBLE
                } ?: run {
                    layout_game_details_setup_button.visibility = View.VISIBLE
                    layout_game_details_join_button.visibility = View.GONE
                }
                getString(if (isJoining) R.string.joining_game else R.string.game_not_started)
            }
            else -> {
                layout_game_details_setup_button.visibility = View.GONE
                layout_game_details_join_button.visibility = View.GONE
                getString(R.string.last_move_at, DateUtils.formatDateTime(this, details.lastMoveAt, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME))
            }
        }
        details.playerIndex?.let {
            val pendingMoves = arrayListOf(-1, -1, -1, -1, -1, -1, -1, -1, -1)
            data.pendingActions.forEach {
                when (it) {
                    is MakeMovePendingAction -> {
                        pendingMoves[it.fieldNo] = details.playerIndex
                    }
                }
            }
            updateFields(pendingMoves, ContextCompat.getColor(this, R.color.pending_move))
        }
        updateFields(details.fields)
        toggleFields(details.playerIndex == details.currentPlayer)
    }

    private fun toggleFields(enabled: Boolean) {
        layout_game_details_field_0.isEnabled = enabled
        layout_game_details_field_1.isEnabled = enabled
        layout_game_details_field_2.isEnabled = enabled
        layout_game_details_field_3.isEnabled = enabled
        layout_game_details_field_4.isEnabled = enabled
        layout_game_details_field_5.isEnabled = enabled
        layout_game_details_field_6.isEnabled = enabled
        layout_game_details_field_7.isEnabled = enabled
        layout_game_details_field_8.isEnabled = enabled
    }

    private fun updateFields(fields: List<Int>, @ColorInt tint: Int = ContextCompat.getColor(this, R.color.move)) {
        setField(layout_game_details_field_0, fields, 0, tint)
        setField(layout_game_details_field_1, fields, 1, tint)
        setField(layout_game_details_field_2, fields, 2, tint)
        setField(layout_game_details_field_3, fields, 3, tint)
        setField(layout_game_details_field_4, fields, 4, tint)
        setField(layout_game_details_field_5, fields, 5, tint)
        setField(layout_game_details_field_6, fields, 6, tint)
        setField(layout_game_details_field_7, fields, 7, tint)
        setField(layout_game_details_field_8, fields, 8, tint)
    }

    private fun setField(field: ImageView, fields: List<Int>, index: Int, @ColorInt tint: Int) =
            field.apply {
                fieldDrawable(fields.getOrNull(index) ?: 0)?.let {
                    setImageDrawable(it)
                    setColorFilter(tint, PorterDuff.Mode.SRC_IN)
                }
            }

    private fun fieldDrawable(playerIndex: Int): Drawable? =
            when (playerIndex) {
                -1 -> ContextCompat.getDrawable(this, android.R.color.transparent)
                1 -> ContextCompat.getDrawable(this, R.drawable.ic_player_1)
                2 -> ContextCompat.getDrawable(this, R.drawable.ic_player_2)
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