package com.bigberry.comicvn.ui.manga.track

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bigberry.comicvn.R
import com.bigberry.comicvn.data.database.models.Track
import com.bigberry.comicvn.util.inflate
import kotlinx.android.synthetic.main.track_search_item.view.*
import java.util.*

class TrackSearchAdapter(context: Context)
: ArrayAdapter<Track>(context, com.bigberry.comicvn.R.layout.track_search_item, ArrayList<Track>()) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var v = view
        // Get the data item for this position
        val track = getItem(position)
        // Check if an existing view is being reused, otherwise inflate the view
        val holder: TrackSearchHolder // view lookup cache stored in tag
        if (v == null) {
            v = parent.inflate(com.bigberry.comicvn.R.layout.track_search_item)
            holder = TrackSearchHolder(v)
            v.tag = holder
        } else {
            holder = v.tag as TrackSearchHolder
        }
        holder.onSetValues(track)
        return v
    }

    fun setItems(syncs: List<Track>) {
        setNotifyOnChange(false)
        clear()
        addAll(syncs)
        notifyDataSetChanged()
    }

    class TrackSearchHolder(private val view: View) {

        fun onSetValues(track: Track) {
            view.track_search_title.text = track.title
        }
    }

}