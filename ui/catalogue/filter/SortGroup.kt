package com.bigberry.comicvn.ui.catalogue.filter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractExpandableHeaderItem
import eu.davidea.flexibleadapter.items.ISectionable
import com.bigberry.comicvn.R
import com.bigberry.comicvn.source.model.Filter
import com.bigberry.comicvn.util.setVectorCompat

class SortGroup(val filter: Filter.Sort) : AbstractExpandableHeaderItem<SortGroup.Holder, ISectionable<*, *>>() {

    // Use an id instead of the layout res to allow to reuse the layout.
    override fun getLayoutRes(): Int {
        return com.bigberry.comicvn.R.id.catalogue_filter_sort_group
    }

    override fun createViewHolder(adapter: FlexibleAdapter<*>, inflater: LayoutInflater, parent: ViewGroup): Holder {
        return Holder(inflater.inflate(com.bigberry.comicvn.R.layout.navigation_view_group, parent, false), adapter)
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<*>, holder: Holder, position: Int, payloads: List<Any?>?) {
        holder.title.text = filter.name

        holder.icon.setVectorCompat(if (isExpanded)
            com.bigberry.comicvn.R.drawable.ic_expand_more_white_24dp
        else
            com.bigberry.comicvn.R.drawable.ic_chevron_right_white_24dp)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is SortGroup) {
            return filter == other.filter
        }
        return false
    }

    override fun hashCode(): Int {
        return filter.hashCode()
    }

    class Holder(view: View, adapter: FlexibleAdapter<*>) : GroupItem.Holder(view, adapter)
}