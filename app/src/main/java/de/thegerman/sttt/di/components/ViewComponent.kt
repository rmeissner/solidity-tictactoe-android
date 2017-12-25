package de.thegerman.sttt.di.components

import dagger.Component
import de.thegerman.sttt.di.modules.ViewModule
import de.thegerman.sttt.ui.account.setup.AccountSetupActivity
import de.thegerman.sttt.ui.account.unlock.UnlockActivity
import de.thegerman.sttt.ui.games.details.DetailsActivity
import de.thegerman.sttt.ui.games.overview.OverviewActivity
import pm.gnosis.heimdall.common.di.ForView

@ForView
@Component(
        dependencies = [AppComponent::class],
        modules = [ViewModule::class]
)
interface ViewComponent {
    /*
        Activities
     */

    fun inject(activity: AccountSetupActivity)
    fun inject(activity: DetailsActivity)
    fun inject(activity: OverviewActivity)
    fun inject(activity: UnlockActivity)
}
