package de.thegerman.sttt.ui.account.setup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.jakewharton.rxbinding2.view.clicks
import de.thegerman.sttt.R
import de.thegerman.sttt.di.components.AppComponent
import de.thegerman.sttt.di.components.DaggerViewComponent
import de.thegerman.sttt.di.modules.ViewModule
import de.thegerman.sttt.ui.base.InjectedActivity
import de.thegerman.sttt.utils.subscribeForResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.layout_account_setup.*
import pm.gnosis.mnemonic.*
import timber.log.Timber
import javax.inject.Inject

class AccountSetupActivity : InjectedActivity() {

    @Inject
    lateinit var viewModel: AccountSetupContract

    override fun inject(appComponent: AppComponent) {
        DaggerViewComponent.builder()
                .appComponent(appComponent)
                .viewModule(ViewModule(this))
                .build().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_account_setup)
    }

    override fun onResume() {
        super.onResume()
        disposables += layout_account_setup_generate_button.clicks()
                .compose(viewModel.generateMnemonicTransformer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeForResult({
                    layout_account_setup_mnemoic_input.setText(it)
                }, Timber::e)
        disposables += layout_account_setup_finish_button.clicks()
                .map { layout_account_setup_mnemoic_input.text.toString() }
                .compose(viewModel.setupAccount())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeForResult({
                    finish()
                }, {
                    layout_account_setup_error.text = errorMessage(it)
                    Timber.e(it)
                })
    }

    private fun errorMessage(it: Throwable): String? =
            (it as? Bip39ValidationResult)?.let {
                when(it) {
                    is MnemonicNotInWordlist -> getString(R.string.error_mnemonic_wrong_word)
                    is EmptyMnemonic -> getString(R.string.error_mnemonic_empty)
                    is InvalidEntropy, is UnknownMnemonicError, is InvalidChecksum -> getString(R.string.error_mnemonic_unknown)
                }
            } ?: getString(R.string.error_unknown)

    companion object {
        fun createIntent(context: Context) =
                Intent(context, AccountSetupActivity::class.java)
    }
}