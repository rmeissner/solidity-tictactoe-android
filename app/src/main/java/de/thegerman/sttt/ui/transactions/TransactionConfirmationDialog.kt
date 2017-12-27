package de.thegerman.sttt.ui.transactions

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.clicks
import de.thegerman.sttt.R
import de.thegerman.sttt.StttApplication
import de.thegerman.sttt.di.components.DaggerViewComponent
import de.thegerman.sttt.di.modules.ViewModule
import de.thegerman.sttt.ui.transactions.TransactionConfirmationContract.Action.*
import de.thegerman.sttt.utils.displayString
import de.thegerman.sttt.utils.subscribeForResult
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.layout_confirm_transaction.*
import pm.gnosis.heimdall.common.utils.snackbar
import pm.gnosis.heimdall.common.utils.toast
import pm.gnosis.models.Wei
import pm.gnosis.utils.asDecimalString
import pm.gnosis.utils.hexAsBigIntegerOrNull
import java.math.BigInteger
import javax.inject.Inject

class TransactionConfirmationDialog : BottomSheetDialogFragment() {

    @Inject
    lateinit var viewModel: TransactionConfirmationContract

    private lateinit var gameId: BigInteger
    private lateinit var action: TransactionConfirmationContract.Action

    private val disposables = CompositeDisposable()
    private var hasEstimate = false

    init {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.TransactionConfirmationDialog)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerViewComponent.builder()
                .viewModule(ViewModule(context!!))
                .appComponent(StttApplication[context!!].component)
                .build().inject(this)
        gameId = arguments?.getString(KEY_GAME_ID)?.hexAsBigIntegerOrNull() ?: run {
            dismiss()
            return
        }
        action = when (arguments?.getInt(KEY_TYPE)) {
            TYPE_JOIN -> Join()
            TYPE_MOVE -> arguments?.getInt(KEY_MOVE_FIELD)?.let { MakeMove(it) }
            TYPE_CANCEL -> Cancel()
            TYPE_KICK-> Kick()
            else -> null
        } ?: run {
            dismiss()
            return
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.layout_confirm_transaction, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val action = this.action
        layout_confirm_transaction_description.text = when (action) {
            is Join -> getString(R.string.description_join, gameId.asDecimalString())
            is MakeMove -> getString(R.string.description_make_move, fieldName(action.field))
            is Cancel -> getString(R.string.description_cancel, gameId.asDecimalString())
            is Kick -> getString(R.string.description_kick, gameId.asDecimalString())
        }
    }

    private fun fieldName(field: Int): String {
        return getString(when (field) {
            0 -> R.string.top_left
            1 -> R.string.top_center
            2 -> R.string.top_right
            3 -> R.string.center_left
            4 -> R.string.center
            5 -> R.string.center_right
            6 -> R.string.bottom_left
            7 -> R.string.bottom_center
            8 -> R.string.bottom_right
            else -> R.string.unknown
        })
    }

    override fun onStart() {
        super.onStart()
        disposables += layout_confirm_transaction_estimate_button.clicks()
                .startWith(Unit)
                .filter { !hasEstimate }
                .map { gameId to action }
                .doOnNext {
                    layout_confirm_transaction_estimate.text = getString(R.string.costs_loading)
                    buttonsEnabled(false)
                }
                .compose(viewModel.estimateTransformer())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { buttonsEnabled(true) }
                .subscribeForResult(::updateEstimate, ::estimateError)
        disposables += layout_confirm_transaction_confirm_button.clicks()
                .filter { hasEstimate }
                .map { gameId to action }
                .doOnNext { buttonsEnabled(false) }
                .compose(viewModel.confirmTransformer())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { buttonsEnabled(true) }
                .subscribeForResult({
                    dismiss()
                }, {
                    snackbar(layout_confirm_transaction_estimate_button, R.string.error_unknown)
                })
    }

    private fun buttonsEnabled(enabled: Boolean) {
        layout_confirm_transaction_confirm_button.isEnabled = enabled
        layout_confirm_transaction_estimate_button.isEnabled = enabled
    }

    private fun updateEstimate(estimate: Wei) {
        hasEstimate = true
        layout_confirm_transaction_estimate.text = getString(R.string.costs_estimate, estimate.displayString(context!!))
        layout_confirm_transaction_confirm_button.visibility = View.VISIBLE
        layout_confirm_transaction_estimate_button.visibility = View.GONE
    }

    private fun estimateError(throwable: Throwable) {
        hasEstimate = false
        layout_confirm_transaction_estimate.text = getString(R.string.costs_error)
        layout_confirm_transaction_confirm_button.visibility = View.GONE
        layout_confirm_transaction_estimate_button.visibility = View.VISIBLE
    }

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }

    companion object {
        private const val KEY_GAME_ID = "argument.string.game_id"
        private const val KEY_MOVE_FIELD = "argument.int.move_field"
        private const val KEY_TYPE = "argument.int.type"
        private const val TYPE_JOIN = 0
        private const val TYPE_MOVE = 1
        private const val TYPE_CANCEL = 2
        private const val TYPE_KICK = 3

        private fun createBundle(gameId: BigInteger, type: Int) =
                Bundle().apply {
                    putString(KEY_GAME_ID, gameId.toString(16))
                    putInt(KEY_TYPE, type)
                }

        fun confirmJoin(gameId: BigInteger) = TransactionConfirmationDialog().apply {
            arguments = createBundle(gameId, TYPE_JOIN)
        }

        fun confirmCancel(gameId: BigInteger) = TransactionConfirmationDialog().apply {
            arguments = createBundle(gameId, TYPE_CANCEL)
        }

        fun confirmKick(gameId: BigInteger) = TransactionConfirmationDialog().apply {
            arguments = createBundle(gameId, TYPE_KICK)
        }

        fun confirmMove(gameId: BigInteger, field: Int) = TransactionConfirmationDialog().apply {
            arguments = createBundle(gameId, TYPE_MOVE).apply {
                putInt(KEY_MOVE_FIELD, field)
            }
        }
    }
}