package com.bigberry.comicvn.ui.catalogue.filter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractExpandableHeaderItem
import eu.davidea.flexibleadapter.items.ISectionable
import eu.davidea.viewholders.ExpandableViewHolder
import com.bigberry.comicvn.R
import com.bigberry.comicvn.source.model.Filter
import com.bigberry.comicvn.util.setVectorCompat

class GroupItem(val filter: Filter.Group<*>) : AbstractExpandableHeaderItem<GroupItem.Holder, ISectionable<*, *>>() {

    override fun getLayoutRes(): Int {
        return com.bigberry.comicvn.R.layout.navigation_view_group
    }

    override fun createViewHolder(adapter: FlexibleAdapter<*>, inflater: LayoutInflater, parent: ViewGroup): Holder {
        return Holder(inflater.inflate(layoutRes, parent, false), adapter)
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
        if (other is GroupItem) {
            return filter == other.filter
        }
        return false
    }

    override fun hashCode(): Int {
        return filter.hashCode()
    }

    open class Holder(view: View, adapter: FlexibleAdapter<*>) : ExpandableViewHolder(view, adapter, true) {

        val title = itemView.findViewById(com.bigberry.comicvn.R.id.title) as TextView
        val icon = itemView.findViewById(com.bigberry.comicvn.R.id.expand_icon) as ImageView

        override fun shouldNotifyParentOnClick(): Boolean {
            return true
        }
    }
}