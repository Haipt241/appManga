package com.bigberry.comicvn.ui.recently_read

import android.app.Dialog
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.bluelinelabs.conductor.Controller
import com.bigberry.comicvn.R
import com.bigberry.comicvn.data.database.models.History
import com.bigberry.comicvn.data.database.models.Manga
import com.bigberry.comicvn.ui.base.controller.DialogController
import com.bigberry.comicvn.widget.DialogCheckboxView

class RemoveHistoryDialog<T>(bundle: Bundle? = null) : DialogController(bundle)
        where T : Controller, T: RemoveHistoryDialog.Listener {

    private var manga: Manga? = null

    private var history: History? = null

    constructor(target: T, manga: Manga, history: History) : this() {
        this.manga = manga
        this.history = history
        targetController = target
    }

    override fun onCreateDialog(savedViewState: Bundle?): Dialog {
        val activity = activity!!

        // Create custom view
        val dialogCheckboxView = DialogCheckboxView(activity).apply {
            setDescription(com.bigberry.comicvn.R.string.dialog_with_checkbox_remove_description)
            setOptionDescription(com.bigberry.comicvn.R.string.dialog_with_checkbox_reset)
        }

        return MaterialDialog.Builder(activity)
                .title(com.bigberry.comicvn.R.string.action_remove)
                .customView(dialogCheckboxView, true)
                .positiveText(com.bigberry.comicvn.R.string.action_remove)
                .negativeText(android.R.string.cancel)
                .onPositive { _, _ -> onPositive(dialogCheckboxView.isChecked()) }
                .build()
    }

    private fun onPositive(checked: Boolean) {
        val target = targetController as? Listener ?: return
        val manga = manga ?: return
        val history = history ?: return

        target.removeHistory(manga, history, checked)
    }

    interface Listener {
        fun removeHistory(manga: Manga, history: History, all: Boolean)
    }

}