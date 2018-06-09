package com.bigberry.comicvn.ui.manga.track

import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import com.afollestad.materialdialogs.MaterialDialog
import com.bluelinelabs.conductor.Controller
import com.bigberry.comicvn.R
import com.bigberry.comicvn.data.database.models.Track
import com.bigberry.comicvn.data.track.TrackManager
import com.bigberry.comicvn.ui.base.controller.DialogController
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class SetTrackScoreDialog<T> : DialogController
        where T : Controller, T : SetTrackScoreDialog.Listener {

    private val item: TrackItem

    constructor(target: T, item: TrackItem) : super(Bundle().apply {
        putSerializable(KEY_ITEM_TRACK, item.track)
    }) {
        targetController = target
        this.item = item
    }

    @Suppress("unused")
    constructor(bundle: Bundle) : super(bundle) {
        val track = bundle.getSerializable(KEY_ITEM_TRACK) as Track
        val service = Injekt.get<TrackManager>().getService(track.sync_id)!!
        item = TrackItem(track, service)
    }

    override fun onCreateDialog(savedViewState: Bundle?): Dialog {
        val item = item

        val dialog = MaterialDialog.Builder(activity!!)
                .title(com.bigberry.comicvn.R.string.score)
                .customView(com.bigberry.comicvn.R.layout.track_score_dialog, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive { dialog, _ ->
                    val view = dialog.customView
                    if (view != null) {
                        // Remove focus to update selected number
                        val np = view.findViewById(com.bigberry.comicvn.R.id.score_picker) as NumberPicker
                        np.clearFocus()

                        (targetController as? Listener)?.setScore(item, np.value)
                    }
                }
                .show()

        val view = dialog.customView
        if (view != null) {
            val np = view.findViewById(com.bigberry.comicvn.R.id.score_picker) as NumberPicker
            val scores = item.service.getScoreList().toTypedArray()
            np.maxValue = scores.size - 1
            np.displayedValues = scores

            // Set initial value
            val displayedScore = item.service.displayScore(item.track!!)
            if (displayedScore != "-") {
                val index = scores.indexOf(displayedScore)
                np.value = if (index != -1) index else 0
            }
        }

        return dialog
    }

    interface Listener {
        fun setScore(item: TrackItem, score: Int)
    }

    private companion object {
        const val KEY_ITEM_TRACK = "SetTrackScoreDialog.item.track"
    }

}