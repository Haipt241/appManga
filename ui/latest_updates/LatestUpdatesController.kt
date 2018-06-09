package com.bigberry.comicvn.ui.latest_updates

import android.support.v4.widget.DrawerLayout
import android.view.Menu
import android.view.ViewGroup
import com.bigberry.comicvn.R
import com.bigberry.comicvn.ui.catalogue.CatalogueController
import com.bigberry.comicvn.ui.catalogue.CataloguePresenter

/**
 * Fragment that shows the manga from the catalogue. Inherit CatalogueFragment.
 */
class LatestUpdatesController : CatalogueController() {

    override fun createPresenter(): CataloguePresenter {
        return LatestUpdatesPresenter()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(com.bigberry.comicvn.R.id.action_search).isVisible = false
        menu.findItem(com.bigberry.comicvn.R.id.action_set_filter).isVisible = false
    }

    override fun createSecondaryDrawer(drawer: DrawerLayout): ViewGroup? {
        return null
    }

    override fun cleanupSecondaryDrawer(drawer: DrawerLayout) {

    }

}