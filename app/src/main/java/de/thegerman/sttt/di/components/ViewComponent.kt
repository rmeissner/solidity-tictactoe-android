package de.thegerman.sttt.di.components

import dagger.Component
import de.thegerman.sttt.di.annotations.ForView
import de.thegerman.sttt.di.modules.ViewModule
import de.thegerman.sttt.ui.details.DetailsActivity
import de.thegerman.sttt.ui.overview.OverviewActivity

@ForView
@Component(
        dependencies = [AppComponent::class],
        modules = [ViewModule::class]
)
interface ViewComponent {
    /*
        Activities
     */

    fun inject(activity: DetailsActivity)
    fun inject(activity: OverviewActivity)
}
