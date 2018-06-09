package com.bigberry.comicvn.ui.latest_updates

import com.bigberry.comicvn.source.CatalogueSource
import com.bigberry.comicvn.source.model.MangasPage
import com.bigberry.comicvn.ui.catalogue.Pager
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * LatestUpdatesPager inherited from the general Pager.
 */
class LatestUpdatesPager(val source: CatalogueSource): Pager() {

    override fun requestNext(): Observable<MangasPage> {
        return source.fetchLatestUpdates(currentPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { onPageReceived(it) }
    }

}
