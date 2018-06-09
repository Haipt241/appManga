package com.bigberry.comicvn.ui.catalogue

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import com.bigberry.comicvn.R
import com.bigberry.comicvn.data.database.models.Manga
import com.bigberry.comicvn.util.inflate
import com.bigberry.comicvn.widget.AutofitRecyclerView
import eu.kanade.tachiyomi.ui.catalogue.CatalogueListHolder
import kotlinx.android.synthetic.main.catalogue_grid_item.view.*

class CatalogueItem(val manga: Manga) : AbstractFlexibleItem<CatalogueHolder>() {

    override fun getLayoutRes(): Int {
        return com.bigberry.comicvn.R.layout.catalogue_grid_item
    }

    override fun createViewHolder(adapter: FlexibleAdapter<*>,
                                  inflater: LayoutInflater,
                                  parent: ViewGroup): CatalogueHolder {

        if (parent is AutofitRecyclerView) {
            val view = parent.inflate(com.bigberry.comicvn.R.layout.catalogue_grid_item).apply {
                card.layoutParams = FrameLayout.LayoutParams(
                        MATCH_PARENT, parent.itemWidth / 3 * 4)
                gradient.layoutParams = FrameLayout.LayoutParams(
                        MATCH_PARENT, parent.itemWidth / 3 * 4 / 2, Gravity.BOTTOM)
            }
            return CatalogueGridHolder(view, adapter)
        } else {
            val view = parent.inflate(com.bigberry.comicvn.R.layout.catalogue_list_item)
            return CatalogueListHolder(view, adapter)
        }
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<*>,
                                holder: CatalogueHolder,
                                position: Int,
                                payloads: List<Any?>?) {

        holder.onSetValues(manga)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is CatalogueItem) {
            return manga.id!! == other.manga.id!!
        }
        return false
    }

    override fun hashCode(): Int {
        return manga.id!!.hashCode()
    }



}