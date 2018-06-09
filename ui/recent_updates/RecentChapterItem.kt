package com.bigberry.comicvn.ui.recent_updates

import android.view.LayoutInflater
import android.view.ViewGroup
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractSectionableItem
import com.bigberry.comicvn.R
import com.bigberry.comicvn.data.database.models.Chapter
import com.bigberry.comicvn.data.database.models.Manga
import com.bigberry.comicvn.data.download.model.Download

class RecentChapterItem(val chapter: Chapter, val manga: Manga, header: DateItem) :
        AbstractSectionableItem<RecentChapterHolder, DateItem>(header) {

    private var _status: Int = 0

    var status: Int
        get() = download?.status ?: _status
        set(value) { _status = value }

    @Transient var download: Download? = null

    val isDownloaded: Boolean
        get() = status == Download.DOWNLOADED

    override fun getLayoutRes(): Int {
        return com.bigberry.comicvn.R.layout.recent_chapters_item
    }

    override fun createViewHolder(adapter: FlexibleAdapter<*>,
                                  inflater: LayoutInflater,
                                  parent: ViewGroup): RecentChapterHolder {

        val view = inflater.inflate(layoutRes, parent, false)
        return RecentChapterHolder(view , adapter as RecentChaptersAdapter)
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<*>,
                                holder: RecentChapterHolder,
                                position: Int,
                                payloads: List<Any?>?) {

        holder.bind(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is RecentChapterItem) {
            return chapter.id!! == other.chapter.id!!
        }
        return false
    }

    override fun hashCode(): Int {
        return chapter.id!!.hashCode()
    }

}