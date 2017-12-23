package de.thegerman.sttt.di.components

import dagger.Component
import de.thegerman.sttt.di.modules.*

@Component(modules = [
    CoreModule::class,
    InterceptorsModule::class,
    NetworkingModule::class,
    StorageModule::class,
    ViewModelFactoryModule::class
])
interface AppComponent {
}
