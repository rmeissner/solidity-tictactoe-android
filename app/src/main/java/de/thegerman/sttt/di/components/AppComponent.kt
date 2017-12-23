package de.thegerman.sttt.di.components

import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import dagger.Component
import de.thegerman.sttt.data.repositories.GameRepository
import de.thegerman.sttt.di.annotations.ApplicationContext
import de.thegerman.sttt.di.modules.*
import javax.inject.Singleton

@Singleton
@Component(modules = [
    CoreModule::class,
    InterceptorsModule::class,
    NetworkingModule::class,
    RepositoryModule::class,
    StorageModule::class,
    ViewModelFactoryModule::class
])
interface AppComponent {

    @ApplicationContext
    fun context(): Context

    fun viewModelFactory(): ViewModelProvider.Factory

    fun gameRepository(): GameRepository
}
