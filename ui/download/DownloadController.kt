package com.bigberry.comicvn.ui.download

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.bigberry.comicvn.R
import com.bigberry.comicvn.data.download.DownloadService
import com.bigberry.comicvn.data.download.model.Download
import com.bigberry.comicvn.source.model.Page
import com.bigberry.comicvn.ui.base.controller.NucleusController
import kotlinx.android.synthetic.main.download_controller.view.*
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Controller that shows the currently active downloads.
 * Uses R.layout.fragment_download_queue.
 */
class DownloadController : NucleusController<DownloadPresenter>() {

    /**
     * Adapter containing the active downloads.
     */
    private var adapter: DownloadAdapter? = null

    /**
     * Map of subscriptions for active downloads.
     */
    private val progressSubscriptions by lazy { HashMap<Download, Subscription>() }

    /**
     * Whether the download queue is running or not.
     */
    private var isRunning: Boolean = false

    init {
        setHasOptionsMenu(true)
    }

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(com.bigberry.comicvn.R.layout.download_controller, container, false)
    }

    override fun createPresenter(): DownloadPresenter {
        return DownloadPresenter()
    }

    override fun getTitle(): String? {
        return resources?.getString(com.bigberry.comicvn.R.string.label_download_queue)
    }

    override fun onViewCreated(view: View, savedViewState: Bundle?) {
        super.onViewCreated(view, savedViewState)

        // Check if download queue is empty and update information accordingly.
        setInformationView()

        // Initialize adapter.
        adapter = DownloadAdapter()
        with(view) {
            recycler.adapter = adapter

            // Set the layout manager for the recycler and fixed size.
            recycler.layoutManager = LinearLayoutManager(context)
            recycler.setHasFixedSize(true)
        }

        // Suscribe to changes
        DownloadService.runningRelay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeUntilDestroy { onQueueStatusChange(it) }

        presenter.getDownloadStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeUntilDestroy { onStatusChange(it) }

        presenter.getDownloadProgressObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeUntilDestroy { onUpdateDownloadedPages(it) }
    }

    override fun onDestroyView(view: View) {
        super.onDestroyView(view)
        for (subscription in progressSubscriptions.values) {
            subscription.unsubscribe()
        }
        progressSubscriptions.clear()
        adapter = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(com.bigberry.comicvn.R.menu.download_queue, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        // Set start button visibility.
        menu.findItem(com.bigberry.comicvn.R.id.start_queue).isVisible = !isRunning && !presenter.downloadQueue.isEmpty()

        // Set pause button visibility.
        menu.findItem(com.bigberry.comicvn.R.id.pause_queue).isVisible = isRunning

        // Set clear button visibility.
        menu.findItem(com.bigberry.comicvn.R.id.clear_queue).isVisible = !presenter.downloadQueue.isEmpty()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val context = applicationContext ?: return false
        when (item.itemId) {
            com.bigberry.comicvn.R.id.start_queue -> DownloadService.start(context)
            com.bigberry.comicvn.R.id.pause_queue -> {
                DownloadService.stop(context)
                presenter.pauseDownloads()
            }
            com.bigberry.comicvn.R.id.clear_queue -> {
                DownloadService.stop(context)
                presenter.clearQueue()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    /**
     * Called when the status of a download changes.
     *
     * @param download the download whose status has changed.
     */
    private fun onStatusChange(download: Download) {
        when (download.status) {
            Download.DOWNLOADING -> {
                observeProgress(download)
                // Initial update of the downloaded pages
                onUpdateDownloadedPages(download)
            }
            Download.DOWNLOADED -> {
                unsubscribeProgress(download)
                onUpdateProgress(download)
                onUpdateDownloadedPages(download)
            }
            Download.ERROR -> unsubscribeProgress(download)
        }
    }

    /**
     * Observe the progress of a download and notify the view.
     *
     * @param download the download to observe its progress.
     */
    private fun observeProgress(download: Download) {
        val subscription = Observable.interval(50, TimeUnit.MILLISECONDS)
                // Get the sum of percentages for all the pages.
                .flatMap {
                    Observable.from(download.pages)
                            .map(Page::progress)
                            .reduce { x, y -> x + y }
                }
                // Keep only the latest emission to avoid backpressure.
                .onBackpressureLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { progress ->
                    // Update the view only if the progress has changed.
                    if (download.totalProgress != progress) {
                        download.totalProgress = progress
                        onUpdateProgress(download)
                    }
                }

        // Avoid leaking subscriptions
        progressSubscriptions.remove(download)?.unsubscribe()

        progressSubscriptions.put(download, subscription)
    }

    /**
     * Unsubscribes the given download from the progress subscriptions.
     *
     * @param download the download to unsubscribe.
     */
    private fun unsubscribeProgress(download: Download) {
        progressSubscriptions.remove(download)?.unsubscribe()
    }

    /**
     * Called when the queue's status has changed. Updates the visibility of the buttons.
     *
     * @param running whether the queue is now running or not.
     */
    private fun onQueueStatusChange(running: Boolean) {
        isRunning = running
        activity?.invalidateOptionsMenu()

        // Check if download queue is empty and update information accordingly.
        setInformationView()
    }

    /**
     * Called from the presenter to assign the downloads for the adapter.
     *
     * @param downloads the downloads from the queue.
     */
    fun onNextDownloads(downloads: List<Download>) {
        activity?.invalidateOptionsMenu()
        setInformationView()
        adapter?.setItems(downloads)
    }

    /**
     * Called when the progress of a download changes.
     *
     * @param download the download whose progress has changed.
     */
    fun onUpdateProgress(download: Download) {
        getHolder(download)?.notifyProgress()
    }

    /**
     * Called when a page of a download is downloaded.
     *
     * @param download the download whose page has been downloaded.
     */
    fun onUpdateDownloadedPages(download: Download) {
        getHolder(download)?.notifyDownloadedPages()
    }

    /**
     * Returns the holder for the given download.
     *
     * @param download the download to find.
     * @return the holder of the download or null if it's not bound.
     */
    private fun getHolder(download: Download): DownloadHolder? {
        val recycler = view?.recycler ?: return null
        return recycler.findViewHolderForItemId(download.chapter.id!!) as? DownloadHolder
    }

    /**
     * Set information view when queue is empty
     */
    private fun setInformationView() {
        val emptyView = view?.empty_view ?: return
        if (presenter.downloadQueue.isEmpty()) {
            emptyView.show(com.bigberry.comicvn.R.drawable.ic_file_download_black_128dp,
                    com.bigberry.comicvn.R.string.information_no_downloads)
        } else {
            emptyView.hide()
        }
    }

}
