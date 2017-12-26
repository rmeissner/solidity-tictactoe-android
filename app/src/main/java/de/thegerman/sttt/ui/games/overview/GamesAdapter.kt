package de.thegerman.sttt.ui.games.overview

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.thegerman.sttt.BuildConfig
import de.thegerman.sttt.R
import de.thegerman.sttt.data.models.Game
import de.thegerman.sttt.data.models.JoinedGame
import de.thegerman.sttt.data.models.PendingGame
import de.thegerman.sttt.ui.base.LifecycleAdapter
import de.thegerman.sttt.ui.games.details.DetailsActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.layout_joined_game_item.view.*
import kotlinx.android.synthetic.main.layout_pending_game_item.view.*
import pm.gnosis.heimdall.common.di.ForView
import pm.gnosis.heimdall.common.di.ViewContext
import pm.gnosis.utils.asDecimalString
import pm.gnosis.utils.asTransactionHash
import timber.log.Timber
import javax.inject.Inject


@ForView
class GamesAdapter @Inject constructor(
        @ViewContext private val context: Context,
        private val viewModel: OverviewViewModel
) : LifecycleAdapter<Game, GamesAdapter.CastingViewHolder<out Game>>(context) {

    companion object {
        private const val TYPE_PENDING_GAME = 0
        private const val TYPE_JOINED_GAME = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CastingViewHolder<out Game> {
        return when (viewType) {
            TYPE_PENDING_GAME -> {
                PendingViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.layout_pending_game_item, parent, false))
            }
            TYPE_JOINED_GAME -> {
                JoinedViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.layout_joined_game_item, parent, false))
            }
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is PendingGame -> TYPE_PENDING_GAME
            is JoinedGame -> TYPE_JOINED_GAME
        }
    }


    inner abstract class CastingViewHolder<T : Game>(val type: Class<T>, itemView: View) : LifecycleAdapter.LifecycleViewHolder<Game>(itemView) {
        override final fun bind(data: Game, payloads: List<Any>?) {
            if (type.isInstance(data)) {
                castedBind(type.cast(data), payloads)
            }
        }

        abstract fun castedBind(data: T, payloads: List<Any>?)
    }

    inner class PendingViewHolder(itemView: View) : CastingViewHolder<PendingGame>(PendingGame::class.java, itemView) {

        private val disposables = CompositeDisposable()

        private var currentEntry: PendingGame? = null

        override fun castedBind(data: PendingGame, payloads: List<Any>?) {
            currentEntry = data
            itemView.layout_pending_game_item_hash.text = context.getString(R.string.game_x, data.transactionHash.asTransactionHash())
            itemView.layout_pending_game_item_published.text = context.getString(R.string.joined_at,
                    DateUtils.formatDateTime(context, data.publishedAt, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME))
            itemView.setOnClickListener {
                context.startActivity(Intent(Intent.ACTION_VIEW).apply { this.data = Uri.parse("${BuildConfig.ETHERSCAN_TX_URL}${data.transactionHash.asTransactionHash()}") })
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun start() {
            // Make sure no disposable are left over
            disposables.clear()
            val entry = currentEntry ?: return
            disposables += viewModel.observeDeployedStatus(entry.transactionHash.asTransactionHash())
                    .observeOn(AndroidSchedulers.mainThread())
                    // Empty function for now, we should adjust the design and
                    // maybe display a retry button on error
                    .subscribeBy(onError = Timber::e)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun stop() {
            disposables.clear()
        }

        override fun unbind() {
            stop()
            currentEntry = null
            super.unbind()
        }
    }

    inner class JoinedViewHolder(itemView: View) : CastingViewHolder<JoinedGame>(JoinedGame::class.java, itemView) {
        override fun castedBind(data: JoinedGame, payloads: List<Any>?) {
            itemView.layout_joined_game_item_name.text = context.getString(R.string.game_x, data.gameId.asDecimalString())
            itemView.layout_joined_game_item_joined.text = context.getString(R.string.joined_at,
                    DateUtils.formatDateTime(context, data.joinedAt, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME))
            itemView.setOnClickListener {
                context.startActivity(DetailsActivity.createIntent(context, data.gameId))
            }
        }
    }
}