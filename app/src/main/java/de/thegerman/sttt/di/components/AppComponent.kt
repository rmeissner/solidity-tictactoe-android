package de.thegerman.sttt.di.components

import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import dagger.Component
import de.thegerman.sttt.data.repositories.GameRepository
import de.thegerman.sttt.di.modules.*
import pm.gnosis.heimdall.accounts.base.repositories.AccountsRepository
import pm.gnosis.heimdall.accounts.di.AccountsBindingModule
import pm.gnosis.heimdall.accounts.di.AccountsModule
import pm.gnosis.heimdall.common.di.ApplicationContext
import pm.gnosis.heimdall.common.di.modules.CoreModule
import pm.gnosis.heimdall.security.EncryptionManager
import pm.gnosis.heimdall.security.di.SecurityBindingsModule
import pm.gnosis.mnemonic.di.Bip39BindingModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AccountsBindingModule::class,
    AccountsModule::class,
    Bip39BindingModule::class,
    CoreModule::class,
    InterceptorsModule::class,
    NetworkingModule::class,
    RepositoryModule::class,
    SecurityBindingsModule::class,
    StorageModule::class,
    ViewModelFactoryModule::class
])
interface AppComponent {

    @ApplicationContext
    fun context(): Context

    fun viewModelFactory(): ViewModelProvider.Factory

    fun encryptionManager(): EncryptionManager

    fun accountsRepository(): AccountsRepository
    fun gameRepository(): GameRepository
}
