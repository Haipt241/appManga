package com.bigberry.comicvn.ui.library

import android.app.Dialog
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.bluelinelabs.conductor.Controller
import com.bigberry.comicvn.R
import com.bigberry.comicvn.data.database.models.Manga
import com.bigberry.comicvn.ui.base.controller.DialogController
import com.bigberry.comicvn.widget.DialogCheckboxView

class DeleteLibraryMangasDialog<T>(bundle: Bundle? = null) :
        DialogController(bundle) where T : Controller, T: DeleteLibraryMangasDialog.Listener {

    private var mangas = emptyList<Manga>()

    constructor(target: T, mangas: List<Manga>) : this() {
        this.mangas = mangas
        targetController = target
    }

    override fun onCreateDialog(savedViewState: Bundle?): Dialog {
        val view = DialogCheckboxView(activity!!).apply {
            setDescription(com.bigberry.comicvn.R.string.confirm_delete_manga)
            setOptionDescription(com.bigberry.comicvn.R.string.also_delete_chapters)
        }

        return MaterialDialog.Builder(activity!!)
                .title(com.bigberry.comicvn.R.string.action_remove)
                .customView(view, true)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive { _, _ ->
                    val deleteChapters = view.isChecked()
                    (targetController as? Listener)?.deleteMangasFromLibrary(mangas, deleteChapters)
                }
                .build()
    }

    interface Listener {
        fun deleteMangasFromLibrary(mangas: List<Manga>, deleteChapters: Boolean)
    }
}