package de.thegerman.sttt.ui.base

import android.os.Bundle
import android.support.v4.app.Fragment
import de.thegerman.sttt.StttApplication
import de.thegerman.sttt.di.components.AppComponent
import io.reactivex.disposables.CompositeDisposable

abstract class InjectedFragment : Fragment() {

    protected val disposables = CompositeDisposable()

    abstract fun inject(appComponent: AppComponent)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject(StttApplication[context!!].component)
    }

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }
}