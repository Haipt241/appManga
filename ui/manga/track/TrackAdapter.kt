package com.bigberry.comicvn.ui.manga.track

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.bigberry.comicvn.R
import com.bigberry.comicvn.util.inflate

class TrackAdapter(controller: TrackController) : RecyclerView.Adapter<TrackHolder>() {

    var items = emptyList<TrackItem>()
        set(value) {
            if (field !== value) {
                field = value
                notifyDataSetChanged()
            }
        }

    val rowClickListener: OnRowClickListener = controller

    fun getItem(index: Int): TrackItem? {
        return items.getOrNull(index)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHolder {
        val view = parent.inflate(com.bigberry.comicvn.R.layout.track_item)
        return TrackHolder(view, this)
    }

    override fun onBindViewHolder(holder: TrackHolder, position: Int) {
        holder.bind(items[position])
    }

    interface OnRowClickListener {
        fun onTitleClick(position: Int)
        fun onStatusClick(position: Int)
        fun onChaptersClick(position: Int)
        fun onScoreClick(position: Int)
    }

}
