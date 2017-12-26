package de.thegerman.sttt.ui.games.add

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import de.thegerman.sttt.R
import de.thegerman.sttt.di.components.AppComponent
import de.thegerman.sttt.di.components.DaggerViewComponent
import de.thegerman.sttt.di.modules.ViewModule
import de.thegerman.sttt.ui.base.FactoryPagerAdapter
import de.thegerman.sttt.ui.base.InjectedActivity
import de.thegerman.sttt.ui.games.add.create.CreateGameFragment
import de.thegerman.sttt.ui.games.add.open.OpenGameFragment
import kotlinx.android.synthetic.main.layout_add_game.*

class AddGameActivity : InjectedActivity() {

    private val items = listOf(R.string.tab_title_create, R.string.tab_title_open)

    override fun inject(appComponent: AppComponent) {
        DaggerViewComponent.builder()
                .viewModule(ViewModule(this))
                .appComponent(appComponent)
                .build().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_add_game)

        layout_add_game_view_pager.adapter = pagerAdapter()
        layout_add_game_pager_indicator.setupWithViewPager(layout_add_game_view_pager)
    }

    private fun positionToId(position: Int) = items.getOrElse(position, { -1 })

    private fun pagerAdapter() = FactoryPagerAdapter(supportFragmentManager, FactoryPagerAdapter.Factory(items.size, {
        when (positionToId(it)) {
            R.string.tab_title_create -> {
                CreateGameFragment.createInstance()
            }
            R.string.tab_title_open -> {
                OpenGameFragment.createInstance()
            }
            else -> throw IllegalStateException("Unhandled tab position")
        }
    }, {
        getString(items[it])
    }))

    companion object {
        fun createIntent(context: Context) = Intent(context, AddGameActivity::class.java)
    }
}