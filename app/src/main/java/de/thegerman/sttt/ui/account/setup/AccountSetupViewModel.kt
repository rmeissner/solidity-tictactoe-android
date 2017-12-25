package de.thegerman.sttt.ui.account.setup

import de.thegerman.sttt.utils.Result
import de.thegerman.sttt.utils.mapToResult
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import pm.gnosis.heimdall.accounts.base.repositories.AccountsRepository
import javax.inject.Inject

class AccountSetupViewModel @Inject constructor(
        private val accountsRepository: AccountsRepository
) : AccountSetupContract() {
    override fun generateMnemonicTransformer(): ObservableTransformer<Unit, Result<String>> =
            ObservableTransformer {
                it.switchMapSingle {
                    accountsRepository.generateMnemonic()
                            .mapToResult()
                }
            }

    override fun setupAccount(): ObservableTransformer<String, Result<Unit>> =
            ObservableTransformer {
                it.switchMapSingle {
                    accountsRepository.validateMnemonic(it)
                            .subscribeOn(Schedulers.computation())
                            .flatMapCompletable { accountsRepository.saveAccountFromMnemonic(it) }
                            .andThen(accountsRepository.saveMnemonic(it))
                            .andThen(Single.just(Unit))
                            .mapToResult()
                }
            }
}