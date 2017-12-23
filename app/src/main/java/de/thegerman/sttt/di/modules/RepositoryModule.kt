package de.thegerman.sttt.di.modules

import dagger.Binds
import dagger.Module
import de.thegerman.sttt.data.repositories.GameRepository
import de.thegerman.sttt.data.repositories.impls.EthereumGameRepository
import javax.inject.Singleton

@Module
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun binds(repo: EthereumGameRepository): GameRepository
}