package de.thegerman.sttt.di.components

import dagger.Component
import de.thegerman.sttt.di.modules.ViewModule
import de.thegerman.sttt.ui.account.setup.AccountSetupActivity
import de.thegerman.sttt.ui.account.unlock.UnlockActivity
import de.thegerman.sttt.ui.games.add.AddGameActivity
import de.thegerman.sttt.ui.games.add.create.CreateGameFragment
import de.thegerman.sttt.ui.games.details.DetailsActivity
import de.thegerman.sttt.ui.games.overview.OverviewActivity
import de.thegerman.sttt.ui.transactions.TransactionConfirmationDialog
import pm.gnosis.heimdall.common.di.ForView

@ForView
@Component(
        dependencies = [AppComponent::class],
        modules = [ViewModule::class]
)
interface ViewComponent {
    /*
        Dialogs
     */

    fun inject(dialog: TransactionConfirmationDialog)
    /*
        Fragments
     */

    fun inject(fragment: CreateGameFragment)

    /*
        Activities
     */

    fun inject(activity: AccountSetupActivity)
    fun inject(activity: AddGameActivity)
    fun inject(activity: DetailsActivity)
    fun inject(activity: OverviewActivity)
    fun inject(activity: UnlockActivity)
}
