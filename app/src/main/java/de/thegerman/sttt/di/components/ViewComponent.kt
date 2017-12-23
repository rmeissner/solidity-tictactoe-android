package de.thegerman.sttt.di.components

import dagger.Component
import de.thegerman.sttt.di.annotations.ForView
import de.thegerman.sttt.di.modules.ViewModule

@ForView
@Component(
        dependencies = [AppComponent::class],
        modules = [ViewModule::class]
)
interface ViewComponent {
}
