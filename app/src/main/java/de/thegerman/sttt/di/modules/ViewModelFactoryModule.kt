package de.thegerman.sttt.di.modules

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.thegerman.sttt.di.ViewModelFactory
import de.thegerman.sttt.di.annotations.ViewModelKey
import de.thegerman.sttt.ui.account.setup.AccountSetupActivity
import de.thegerman.sttt.ui.account.setup.AccountSetupContract
import de.thegerman.sttt.ui.account.setup.AccountSetupViewModel
import de.thegerman.sttt.ui.account.unlock.UnlockContract
import de.thegerman.sttt.ui.account.unlock.UnlockViewModel
import de.thegerman.sttt.ui.games.add.create.CreateGameContract
import de.thegerman.sttt.ui.games.add.create.CreateGameViewModel
import de.thegerman.sttt.ui.games.details.DetailsContract
import de.thegerman.sttt.ui.games.details.DetailsViewModel
import de.thegerman.sttt.ui.games.overview.OverviewContract
import de.thegerman.sttt.ui.games.overview.OverviewViewModel
import de.thegerman.sttt.ui.transactions.TransactionConfirmViewModel
import de.thegerman.sttt.ui.transactions.TransactionConfirmationContract
import javax.inject.Singleton

@Module
abstract class ViewModelFactoryModule {
    @Binds
    @IntoMap
    @ViewModelKey(AccountSetupContract::class)
    abstract fun bindsAccountSetupContract(viewModel: AccountSetupViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateGameContract::class)
    abstract fun bindsCreateGameContract(viewModel: CreateGameViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DetailsContract::class)
    abstract fun bindsDetailsContract(viewModel: DetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OverviewContract::class)
    abstract fun bindsOverviewContract(viewModel: OverviewViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TransactionConfirmationContract::class)
    abstract fun bindsTransactionConfirmationContract(viewModel: TransactionConfirmViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UnlockContract::class)
    abstract fun bindsUnlockContract(viewModel: UnlockViewModel): ViewModel

    @Binds
    @Singleton
    abstract fun bindsViewModelFactory(viewModel: ViewModelFactory): ViewModelProvider.Factory
}
