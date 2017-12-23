package de.thegerman.sttt.ui.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import de.thegerman.sttt.StttApplication
import de.thegerman.sttt.di.components.AppComponent
import io.reactivex.disposables.CompositeDisposable

abstract class InjectedActivity : AppCompatActivity() {
    protected val disposables = CompositeDisposable()

    abstract fun inject(appComponent: AppComponent)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject(StttApplication[this].component)
    }

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }
}