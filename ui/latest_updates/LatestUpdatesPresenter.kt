package com.bigberry.comicvn.ui.latest_updates

import com.bigberry.comicvn.source.CatalogueSource
import com.bigberry.comicvn.source.Source
import com.bigberry.comicvn.source.model.FilterList
import com.bigberry.comicvn.ui.catalogue.CataloguePresenter
import com.bigberry.comicvn.ui.catalogue.Pager

/**
 * Presenter of [LatestUpdatesController]. Inherit CataloguePresenter.
 */
class LatestUpdatesPresenter : CataloguePresenter() {

    override fun createPager(query: String, filters: FilterList): Pager {
        return LatestUpdatesPager(source)
    }

    override fun getEnabledSources(): List<CatalogueSource> {
        return super.getEnabledSources().filter { it.supportsLatest }
    }

    override fun isValidSource(source: Source?): Boolean {
        return super.isValidSource(source) && (source as CatalogueSource).supportsLatest
    }

}