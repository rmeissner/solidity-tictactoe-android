package de.thegerman.sttt.di.modules

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import dagger.Module
import dagger.Provides
import de.thegerman.sttt.ui.account.setup.AccountSetupContract
import de.thegerman.sttt.ui.account.unlock.UnlockContract
import de.thegerman.sttt.ui.games.add.create.CreateGameContract
import de.thegerman.sttt.ui.games.details.DetailsContract
import de.thegerman.sttt.ui.games.overview.OverviewContract
import de.thegerman.sttt.ui.transactions.TransactionConfirmationContract
import pm.gnosis.heimdall.common.di.ForView
import pm.gnosis.heimdall.common.di.ViewContext

@Module
class ViewModule(private val context: Context) {
    @Provides
    @ForView
    @ViewContext
    fun providesContext() = context

    @Provides
    @ForView
    fun providesLinearLayoutManager() = LinearLayoutManager(context)

    @Provides
    @ForView
    fun providesAccountSetupContract(provider: ViewModelProvider) = provider[AccountSetupContract::class.java]

    @Provides
    @ForView
    fun providesCreateGameContract(provider: ViewModelProvider) = provider[CreateGameContract::class.java]

    @Provides
    @ForView
    fun providesDetailsContract(provider: ViewModelProvider) = provider[DetailsContract::class.java]

    @Provides
    @ForView
    fun providesOverviewContract(provider: ViewModelProvider) = provider[OverviewContract::class.java]

    @Provides
    @ForView
    fun providesTransactionConfirmationContract(provider: ViewModelProvider) = provider[TransactionConfirmationContract::class.java]

    @Provides
    @ForView
    fun providesUnlockContract(provider: ViewModelProvider) = provider[UnlockContract::class.java]

    @Provides
    @ForView
    fun providesViewModelProvider(factory: ViewModelProvider.Factory): ViewModelProvider {
        return when (context) {
            is Fragment -> ViewModelProviders.of(context, factory)
            is FragmentActivity -> ViewModelProviders.of(context, factory)
            else -> throw IllegalArgumentException("Unsupported context $context")
        }
    }
}
