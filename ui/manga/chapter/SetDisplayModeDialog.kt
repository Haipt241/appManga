package com.bigberry.comicvn.ui.manga.chapter

import android.app.Dialog
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.bluelinelabs.conductor.Controller
import com.bigberry.comicvn.R
import com.bigberry.comicvn.data.database.models.Manga
import com.bigberry.comicvn.ui.base.controller.DialogController

class SetDisplayModeDialog<T>(bundle: Bundle? = null) : DialogController(bundle)
        where T : Controller, T : SetDisplayModeDialog.Listener {

    private val selectedIndex = args.getInt("selected", -1)

    constructor(target: T, selectedIndex: Int = -1) : this(Bundle().apply {
        putInt("selected", selectedIndex)
    }) {
        targetController = target
    }

    override fun onCreateDialog(savedViewState: Bundle?): Dialog {
        val activity = activity!!
        val ids = intArrayOf(Manga.DISPLAY_NAME, Manga.DISPLAY_NUMBER)
        val choices = intArrayOf(R.string.show_title, com.bigberry.comicvn.R.string.show_chapter_number)
                .map { activity.getString(it) }

        return MaterialDialog.Builder(activity)
                .title(com.bigberry.comicvn.R.string.action_display_mode)
                .items(choices)
                .itemsIds(ids)
                .itemsCallbackSingleChoice(selectedIndex) { _, itemView, _, _ ->
                    (targetController as? Listener)?.setDisplayMode(itemView.id)
                    true
                }
                .build()
    }

    interface Listener {
        fun setDisplayMode(id: Int)
    }

}