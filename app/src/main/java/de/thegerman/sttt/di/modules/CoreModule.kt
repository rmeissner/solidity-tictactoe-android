package de.thegerman.sttt.di.modules

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import de.thegerman.sttt.di.annotations.ApplicationContext
import javax.inject.Singleton

@Module
class CoreModule(private val application: Application) {

    @Provides
    @Singleton
    @ApplicationContext
    fun providesContext(): Context = application

    @Provides
    @Singleton
    fun providesApplication(): Application = application
}