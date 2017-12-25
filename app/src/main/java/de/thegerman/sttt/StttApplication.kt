package de.thegerman.sttt

import android.content.Context
import android.support.multidex.MultiDexApplication
import de.thegerman.sttt.di.components.AppComponent
import de.thegerman.sttt.di.components.DaggerAppComponent
import org.spongycastle.jce.provider.BouncyCastleProvider
import pm.gnosis.heimdall.common.di.modules.CoreModule
import timber.log.Timber
import java.security.Security

class StttApplication : MultiDexApplication() {
    val component: AppComponent = DaggerAppComponent.builder()
            .coreModule(CoreModule(this))
            .build()

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Security.insertProviderAt(BouncyCastleProvider(), 1)
    }

    companion object {
        operator fun get(context: Context): StttApplication {
            return context.applicationContext as StttApplication
        }
    }
}