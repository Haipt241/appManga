package com.bigberry.comicvn.ui.main

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import com.afollestad.materialdialogs.MaterialDialog
import com.bigberry.comicvn.BuildConfig
import com.bigberry.comicvn.R
import com.bigberry.comicvn.ui.base.controller.DialogController
import it.gmariotti.changelibs.library.view.ChangeLogRecyclerView

class ChangelogDialogController : DialogController() {

    override fun onCreateDialog(savedState: Bundle?): Dialog {
        val activity = activity!!
        val view = WhatsNewRecyclerView(activity)
        return MaterialDialog.Builder(activity)
                .title(R.string.app_changelog)
                .customView(view, false)
                .positiveText(android.R.string.yes)
                .build()
    }

    class WhatsNewRecyclerView(context: Context) : ChangeLogRecyclerView(context) {
        override fun initAttrs(attrs: AttributeSet?, defStyle: Int) {
            mRowLayoutId = R.layout.changelog_row_layout
            mRowHeaderLayoutId = R.layout.changelog_header_layout
            mChangeLogFileResourceId = if (com.bigberry.comicvn.BuildConfig.DEBUG) R.raw.changelog_debug else R.raw.changelog_release
        }
    }
}