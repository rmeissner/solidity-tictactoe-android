package de.thegerman.sttt.di.modules

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.thegerman.sttt.di.ViewModelFactory
import de.thegerman.sttt.di.annotations.ViewModelKey
import de.thegerman.sttt.ui.overview.OverviewContract
import de.thegerman.sttt.ui.overview.OverviewViewModel
import javax.inject.Singleton

@Module
abstract class ViewModelFactoryModule {
    @Binds
    @IntoMap
    @ViewModelKey(OverviewContract::class)
    abstract fun bindsOverviewContract(viewModel: OverviewViewModel): ViewModel

    @Binds
    @Singleton
    abstract fun bindsViewModelFactory(viewModel: ViewModelFactory): ViewModelProvider.Factory
}
