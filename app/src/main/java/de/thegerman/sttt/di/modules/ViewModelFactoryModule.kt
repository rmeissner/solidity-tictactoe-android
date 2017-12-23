package de.thegerman.sttt.di.modules

import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import de.thegerman.sttt.di.ViewModelFactory
import javax.inject.Singleton

@Module
abstract class ViewModelFactoryModule {

    @Binds
    @Singleton
    abstract fun bindsViewModelFactory(viewModel: ViewModelFactory): ViewModelProvider.Factory
}
