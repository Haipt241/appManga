package com.bigberry.comicvn.ui.reader

import com.bigberry.comicvn.data.database.models.Chapter
import com.bigberry.comicvn.source.model.Page

class ReaderChapter(c: Chapter) : Chapter by c {

    @Transient var pages: List<Page>? = null

    var isDownloaded: Boolean = false

    var requestedPage: Int = 0
}