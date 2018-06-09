package com.bigberry.comicvn.data.download.model

import com.bigberry.comicvn.data.database.models.Chapter
import com.bigberry.comicvn.data.database.models.Manga
import com.bigberry.comicvn.source.model.Page
import com.bigberry.comicvn.source.online.HttpSource
import rx.subjects.PublishSubject

class Download(val source: HttpSource, val manga: Manga, val chapter: Chapter) {

    var pages: List<Page>? = null

    @Volatile @Transient var totalProgress: Int = 0

    @Volatile @Transient var downloadedImages: Int = 0

    @Volatile @Transient var status: Int = 0
        set(status) {
            field = status
            statusSubject?.onNext(this)
        }

    @Transient private var statusSubject: PublishSubject<Download>? = null

    fun setStatusSubject(subject: PublishSubject<Download>?) {
        statusSubject = subject
    }

    companion object {

        const val NOT_DOWNLOADED = 0
        const val QUEUE = 1
        const val DOWNLOADING = 2
        const val DOWNLOADED = 3
        const val ERROR = 4
    }
}