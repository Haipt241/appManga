package com.bigberry.comicvn.ui.catalogue.filter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.viewholders.FlexibleViewHolder
import com.bigberry.comicvn.R
import com.bigberry.comicvn.source.model.Filter
import com.bigberry.comicvn.widget.IgnoreFirstSpinnerListener

open class SelectItem(val filter: Filter.Select<*>) : AbstractFlexibleItem<SelectItem.Holder>() {

    override fun getLayoutRes(): Int {
        return com.bigberry.comicvn.R.layout.navigation_view_spinner
    }

    override fun createViewHolder(adapter: FlexibleAdapter<*>, inflater: LayoutInflater, parent: ViewGroup): Holder {
        return Holder(inflater.inflate(layoutRes, parent, false), adapter)
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<*>, holder: Holder, position: Int, payloads: List<Any?>?) {
        holder.text.text = filter.name + ": "

        val spinner = holder.spinner
        spinner.prompt = filter.name
        spinner.adapter = ArrayAdapter<Any>(holder.itemView.context,
                android.R.layout.simple_spinner_item, filter.values).apply {
            setDropDownViewResource(com.bigberry.comicvn.R.layout.common_spinner_item)
        }
        spinner.onItemSelectedListener = IgnoreFirstSpinnerListener { position ->
            filter.state = position
        }
        spinner.setSelection(filter.state)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is SelectItem) {
            return filter == other.filter
        }
        return false
    }

    override fun hashCode(): Int {
        return filter.hashCode()
    }

    class Holder(view: View, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(view, adapter) {

        val text = itemView.findViewById(com.bigberry.comicvn.R.id.nav_view_item_text) as TextView
        val spinner = itemView.findViewById(com.bigberry.comicvn.R.id.nav_view_item) as Spinner
    }
}