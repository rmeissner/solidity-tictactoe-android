package de.thegerman.sttt.ui.games.add.open

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.clicks
import de.thegerman.sttt.R
import de.thegerman.sttt.di.components.AppComponent
import de.thegerman.sttt.ui.base.InjectedFragment
import de.thegerman.sttt.ui.games.details.DetailsActivity
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.layout_open_game.*
import pm.gnosis.utils.nullOnThrow
import timber.log.Timber
import java.math.BigInteger

class OpenGameFragment : InjectedFragment() {
    override fun inject(appComponent: AppComponent) {
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.layout_open_game, container, false)

    override fun onStart() {
        super.onStart()
        disposables += layout_open_game_open_button.clicks()
                .subscribe({
                    nullOnThrow { BigInteger(layout_open_game_game_id_input.text.toString()) }?.let {
                        activity?.finish()
                        startActivity(DetailsActivity.createIntent(activity!!, it))
                    }
                }, Timber::e)
    }

    companion object {
        fun createInstance() = OpenGameFragment()
    }
}