package com.bigberry.comicvn.ui.download

import android.os.Bundle
import com.bigberry.comicvn.data.download.DownloadManager
import com.bigberry.comicvn.data.download.model.Download
import com.bigberry.comicvn.data.download.model.DownloadQueue
import com.bigberry.comicvn.ui.base.presenter.BasePresenter
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import uy.kohesive.injekt.injectLazy
import java.util.*

/**
 * Presenter of [DownloadController].
 */
class DownloadPresenter : BasePresenter<DownloadController>() {

    /**
     * Download manager.
     */
    val downloadManager: DownloadManager by injectLazy()

    /**
     * Property to get the queue from the download manager.
     */
    val downloadQueue: DownloadQueue
        get() = downloadManager.queue

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)

        downloadQueue.getUpdatedObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .map { ArrayList(it) }
                .subscribeLatestCache(DownloadController::onNextDownloads, { view, error ->
                    Timber.e(error)
                })
    }

    fun getDownloadStatusObservable(): Observable<Download> {
        return downloadQueue.getStatusObservable()
                .startWith(downloadQueue.getActiveDownloads())
    }

    fun getDownloadProgressObservable(): Observable<Download> {
        return downloadQueue.getProgressObservable()
                .onBackpressureBuffer()
    }

    /**
     * Pauses the download queue.
     */
    fun pauseDownloads() {
        downloadManager.pauseDownloads()
    }

    /**
     * Clears the download queue.
     */
    fun clearQueue() {
        downloadManager.clearQueue()
    }

}