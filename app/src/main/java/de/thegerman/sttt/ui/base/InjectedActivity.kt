package de.thegerman.sttt.ui.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import de.thegerman.sttt.R
import de.thegerman.sttt.StttApplication
import de.thegerman.sttt.di.components.AppComponent
import de.thegerman.sttt.ui.account.unlock.UnlockActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import pm.gnosis.heimdall.security.EncryptionManager
import timber.log.Timber
import javax.inject.Inject

abstract class InjectedActivity : AppCompatActivity() {

    @Inject
    lateinit var encryptionManager: EncryptionManager

    protected val disposables = CompositeDisposable()

    private var performSecurityCheck = true

    abstract fun inject(appComponent: AppComponent)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject(StttApplication[this].component)
    }

    override fun onStart() {
        super.onStart()
        if (performSecurityCheck) {
            disposables += encryptionManager.unlocked()
                    // We block the ui thread here to avoid exposing the ui before the app is unlocked
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(::checkSecurity, ::handleCheckError)
        }
    }

    private fun checkSecurity(unlocked: Boolean) {
        if (!unlocked) {
            startActivity(UnlockActivity.createIntent(this))
        }
    }

    protected fun registerToolbar(toolbar: Toolbar) {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun handleCheckError(throwable: Throwable) {
        Timber.d(throwable)
        // Show blocker screen. No auth -> no app usage
    }

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }

    protected fun skipSecurityCheck() {
        performSecurityCheck = false
    }
}