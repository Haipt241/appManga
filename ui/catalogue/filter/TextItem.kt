package com.bigberry.comicvn.ui.catalogue.filter

import android.support.design.widget.TextInputLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.viewholders.FlexibleViewHolder
import com.bigberry.comicvn.R
import com.bigberry.comicvn.source.model.Filter
import com.bigberry.comicvn.widget.SimpleTextWatcher

open class TextItem(val filter: Filter.Text) : AbstractFlexibleItem<TextItem.Holder>() {

    override fun getLayoutRes(): Int {
        return com.bigberry.comicvn.R.layout.navigation_view_text
    }

    override fun createViewHolder(adapter: FlexibleAdapter<*>, inflater: LayoutInflater, parent: ViewGroup): Holder {
        return Holder(inflater.inflate(layoutRes, parent, false), adapter)
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<*>, holder: Holder, position: Int, payloads: List<Any?>?) {
        holder.wrapper.hint = filter.name
        holder.edit.setText(filter.state)
        holder.edit.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                filter.state = s.toString()
            }
        })
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is TextItem) {
            return filter == other.filter
        }
        return false
    }

    override fun hashCode(): Int {
        return filter.hashCode()
    }

    class Holder(view: View, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(view, adapter) {

        val wrapper  = itemView.findViewById(com.bigberry.comicvn.R.id.nav_view_item_wrapper) as TextInputLayout
        val edit = itemView.findViewById(com.bigberry.comicvn.R.id.nav_view_item) as EditText
    }
}