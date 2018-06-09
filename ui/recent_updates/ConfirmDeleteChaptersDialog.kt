package com.bigberry.comicvn.ui.recent_updates

import android.app.Dialog
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.bluelinelabs.conductor.Controller
import com.bigberry.comicvn.R
import com.bigberry.comicvn.ui.base.controller.DialogController

class ConfirmDeleteChaptersDialog<T>(bundle: Bundle? = null) : DialogController(bundle)
        where T : Controller, T : ConfirmDeleteChaptersDialog.Listener {

    private var chaptersToDelete = emptyList<RecentChapterItem>()

    constructor(target: T, chaptersToDelete: List<RecentChapterItem>) : this() {
        this.chaptersToDelete = chaptersToDelete
        targetController = target
    }

    override fun onCreateDialog(savedViewState: Bundle?): Dialog {
        return MaterialDialog.Builder(activity!!)
                .content(com.bigberry.comicvn.R.string.confirm_delete_chapters)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive { _, _ ->
                    (targetController as? Listener)?.deleteChapters(chaptersToDelete)
                }
                .build()
    }

    interface Listener {
        fun deleteChapters(chaptersToDelete: List<RecentChapterItem>)
    }
}