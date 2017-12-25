package de.thegerman.sttt.ui.account.unlock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding2.view.clicks
import de.thegerman.sttt.R
import de.thegerman.sttt.di.components.AppComponent
import de.thegerman.sttt.di.components.DaggerViewComponent
import de.thegerman.sttt.di.modules.ViewModule
import de.thegerman.sttt.ui.base.InjectedActivity
import de.thegerman.sttt.utils.subscribeForResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.layout_unlock.*
import pm.gnosis.heimdall.common.utils.snackbar
import timber.log.Timber
import javax.inject.Inject

class UnlockActivity : InjectedActivity() {

    @Inject
    lateinit var viewModel: UnlockContract

    override fun inject(appComponent: AppComponent) {
        DaggerViewComponent.builder()
                .appComponent(appComponent)
                .viewModule(ViewModule(this))
                .build().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        skipSecurityCheck()
        super.onCreate(savedInstanceState)
        supportActionBar?.title = getString(R.string.unlock_screen_title)
        setContentView(R.layout.layout_unlock)
        if (intent?.getBooleanExtra(EXTRA_CLOSE_APP, false) == true) {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        disposables += viewModel.loadState()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::handleState, Timber::e)

        disposables += layout_unlock_unlock_button.clicks()
                .map { layout_unlock_password_input.text.toString() to layout_unlock_repeat_input.text.toString() }
                .compose(viewModel.unlockTransformer())
                .subscribeForResult(::handleState, {
                    Timber.e(it)
                    snackbar(layout_unlock_password_input, getString(R.string.invalid_credentials))
                })
    }

    private fun handleState(state: UnlockContract.State) {
        when (state) {
            UnlockContract.State.CLEAN -> {
                layout_unlock_repeat_input.visibility = View.VISIBLE
                layout_unlock_password_input.visibility = View.VISIBLE
                layout_unlock_unlock_button.visibility = View.VISIBLE
            }
            UnlockContract.State.LOCKED -> {
                layout_unlock_repeat_input.visibility = View.GONE
                layout_unlock_password_input.visibility = View.VISIBLE
                layout_unlock_unlock_button.visibility = View.VISIBLE
            }
            UnlockContract.State.UNLOCKED -> finish()
        }
    }

    companion object {
        private const val EXTRA_CLOSE_APP = "extra.boolean.close_app"

        fun createIntent(context: Context) =
                Intent(context, UnlockActivity::class.java)

        private fun createIntentToCloseApp(context: Context): Intent {
            val intent = Intent(context, UnlockActivity::class.java)
            intent.putExtra(EXTRA_CLOSE_APP, true)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            return intent
        }
    }
}