package com.bigberry.comicvn.ui.manga.chapter

import android.app.Dialog
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.bluelinelabs.conductor.Controller
import com.bigberry.comicvn.R
import com.bigberry.comicvn.ui.base.controller.DialogController

class DeleteChaptersDialog<T>(bundle: Bundle? = null) : DialogController(bundle)
        where T : Controller, T : DeleteChaptersDialog.Listener {

    constructor(target: T) : this() {
        targetController = target
    }

    override fun onCreateDialog(savedViewState: Bundle?): Dialog {
        return MaterialDialog.Builder(activity!!)
                .content(R.string.confirm_delete_chapters)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive { _, _ ->
                    (targetController as? Listener)?.deleteChapters()
                }
                .show()
    }

    interface Listener {
        fun deleteChapters()
    }

}