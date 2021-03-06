package com.bigberry.comicvn.ui.reader.viewer.pager

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.bigberry.comicvn.R
import com.bigberry.comicvn.source.model.Page
import com.bigberry.comicvn.util.inflate
import com.bigberry.comicvn.widget.ViewPagerAdapter

/**
 * Adapter of pages for a ViewPager.
 */
class PagerReaderAdapter(private val reader: PagerReader) : ViewPagerAdapter() {

    /**
     * Pages stored in the adapter.
     */
    var pages: List<Page> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun createView(container: ViewGroup, position: Int): View {
        val view = container.inflate(com.bigberry.comicvn.R.layout.reader_pager_item) as PageView
        view.initialize(reader, pages[position])
        return view
    }

    /**
     * Returns the number of pages.
     */
    override fun getCount(): Int {
        return pages.size
    }

    override fun getItemPosition(obj: Any): Int {
        val view = obj as PageView
        return if (view.page in pages) {
            PagerAdapter.POSITION_UNCHANGED
        } else {
            PagerAdapter.POSITION_NONE
        }
    }

}
