package de.thegerman.sttt.ui.games.overview

import de.thegerman.sttt.data.models.Game
import de.thegerman.sttt.data.repositories.GameRepository
import de.thegerman.sttt.ui.base.Adapter
import de.thegerman.sttt.utils.Result
import de.thegerman.sttt.utils.mapToResult
import de.thegerman.sttt.utils.scanToAdapterData
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import pm.gnosis.heimdall.accounts.base.repositories.AccountsRepository
import pm.gnosis.models.Wei
import pm.gnosis.utils.asEthereumAddressString
import javax.inject.Inject

class OverviewViewModel @Inject constructor(
        private val accountsRepository: AccountsRepository,
        private val gamesRepository: GameRepository
) : OverviewContract() {
    override fun shareAccountTransformer() = ObservableTransformer<Unit, Result<String>> {
        it.switchMapSingle {
            accountsRepository.loadActiveAccount()
                    .map { it.address.asEthereumAddressString() }
                    .mapToResult()
        }
    }

    override fun observeAccountBalance() =
            gamesRepository.observeGameAccountBalance()

    override fun observeDeployedStatus(transactionHash: String): Observable<String> =
            gamesRepository.observeDeployStatus(transactionHash)

    override fun observeGames(): Flowable<Result<Adapter.Data<Game>>> =
            gamesRepository.observeGames()
                    .scanToAdapterData()
                    .mapToResult()

    override fun isAccountSetup(): Single<Boolean> =
            accountsRepository.loadActiveAccount().map { true }
                    .onErrorReturn { false }
}