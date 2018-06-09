package com.bigberry.comicvn.ui.category

import android.app.Dialog
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.bluelinelabs.conductor.Controller
import com.bigberry.comicvn.R
import com.bigberry.comicvn.ui.base.controller.DialogController

/**
 * Dialog to create a new category for the library.
 */
class CategoryCreateDialog<T>(bundle: Bundle? = null) : DialogController(bundle)
        where T : Controller, T : CategoryCreateDialog.Listener {

    /**
     * Name of the new category. Value updated with each input from the user.
     */
    private var currentName = ""

    constructor(target: T) : this() {
        targetController = target
    }

    /**
     * Called when creating the dialog for this controller.
     *
     * @param savedViewState The saved state of this dialog.
     * @return a new dialog instance.
     */
    override fun onCreateDialog(savedViewState: Bundle?): Dialog {
        return MaterialDialog.Builder(activity!!)
                .title(com.bigberry.comicvn.R.string.action_add_category)
                .negativeText(android.R.string.cancel)
                .alwaysCallInputCallback()
                .input(resources?.getString(com.bigberry.comicvn.R.string.name), currentName, false, { _, input ->
                    currentName = input.toString()
                })
                .onPositive { _, _ -> (targetController as? Listener)?.createCategory(currentName) }
                .build()
    }

    interface Listener {
        fun createCategory(name: String)
    }

}