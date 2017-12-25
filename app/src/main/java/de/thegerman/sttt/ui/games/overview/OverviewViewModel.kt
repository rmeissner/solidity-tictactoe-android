package de.thegerman.sttt.ui.games.overview

import io.reactivex.Single
import pm.gnosis.heimdall.accounts.base.repositories.AccountsRepository
import javax.inject.Inject

class OverviewViewModel @Inject constructor(
        private val accountsRepository: AccountsRepository
) : OverviewContract() {
    override fun isAccountSetup(): Single<Boolean> =
            accountsRepository.loadActiveAccount().map { true }
                    .onErrorReturn { false }
}