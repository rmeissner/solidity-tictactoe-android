package de.thegerman.sttt

import android.content.Context
import android.support.multidex.MultiDexApplication
import de.thegerman.sttt.di.components.AppComponent
import de.thegerman.sttt.di.components.DaggerAppComponent
import de.thegerman.sttt.di.modules.CoreModule

class StttApplication : MultiDexApplication() {
    val component: AppComponent = DaggerAppComponent.builder()
            .coreModule(CoreModule(this))
            .build()

    companion object {
        operator fun get(context: Context): StttApplication {
            return context.applicationContext as StttApplication
        }
    }
}