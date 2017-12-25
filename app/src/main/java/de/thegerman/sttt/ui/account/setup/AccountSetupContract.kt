package de.thegerman.sttt.ui.account.setup

import android.arch.lifecycle.ViewModel
import de.thegerman.sttt.utils.Result
import io.reactivex.ObservableTransformer

abstract class AccountSetupContract: ViewModel() {
    abstract fun generateMnemonicTransformer(): ObservableTransformer<Unit, Result<String>>
    abstract fun setupAccount(): ObservableTransformer<String, Result<Unit>>
}