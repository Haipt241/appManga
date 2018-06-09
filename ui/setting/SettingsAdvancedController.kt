package com.bigberry.comicvn.ui.setting

import android.app.Dialog
import android.os.Bundle
import android.support.v7.preference.PreferenceScreen
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bigberry.comicvn.R
import com.bigberry.comicvn.data.cache.ChapterCache
import com.bigberry.comicvn.data.database.DatabaseHelper
import com.bigberry.comicvn.data.library.LibraryUpdateService
import com.bigberry.comicvn.data.library.LibraryUpdateService.Target
import com.bigberry.comicvn.network.NetworkHelper
import com.bigberry.comicvn.ui.base.controller.DialogController
import com.bigberry.comicvn.ui.library.LibraryController
import com.bigberry.comicvn.ui.main.MainActivity
import com.bigberry.comicvn.util.toast
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import uy.kohesive.injekt.injectLazy

class SettingsAdvancedController : SettingsController() {

    private val network: NetworkHelper by injectLazy()

    private val chapterCache: ChapterCache by injectLazy()

    private val db: DatabaseHelper by injectLazy()

    override fun setupPreferenceScreen(screen: PreferenceScreen) = with(screen) {
        titleRes = com.bigberry.comicvn.R.string.pref_category_advanced

        preference {
            key = CLEAR_CACHE_KEY
            titleRes = com.bigberry.comicvn.R.string.pref_clear_chapter_cache
            summary = context.getString(com.bigberry.comicvn.R.string.used_cache, chapterCache.readableSize)

            onClick { clearChapterCache() }
        }
        preference {
            titleRes = com.bigberry.comicvn.R.string.pref_clear_cookies

            onClick {
                network.cookies.removeAll()
                activity?.toast(com.bigberry.comicvn.R.string.cookies_cleared)
            }
        }
        preference {
            titleRes = com.bigberry.comicvn.R.string.pref_clear_database
            summaryRes = com.bigberry.comicvn.R.string.pref_clear_database_summary

            onClick {
                val ctrl = ClearDatabaseDialogController()
                ctrl.targetController = this@SettingsAdvancedController
                ctrl.showDialog(router)
            }
        }
        preference {
            titleRes = com.bigberry.comicvn.R.string.pref_refresh_library_metadata
            summaryRes = com.bigberry.comicvn.R.string.pref_refresh_library_metadata_summary

            onClick { LibraryUpdateService.start(context, target = Target.DETAILS) }
        }
        /*preference {
            titleRes = com.bigberry.comicvn.R.string.pref_refresh_library_tracking
            summaryRes = com.bigberry.comicvn.R.string.pref_refresh_library_tracking_summary

            onClick { LibraryUpdateService.start(context, target = Target.TRACKING) }
        }*/
    }

    private fun clearChapterCache() {
        if (activity == null) return
        val files = chapterCache.cacheDir?.listFiles() ?: return

        var deletedFiles = 0

        val ctrl = DeletingFilesDialogController()
        ctrl.total = files.size
        ctrl.showDialog(router)

        Observable.defer { Observable.from(files) }
                .doOnNext { file ->
                    if (chapterCache.removeFileFromCache(file.name)) {
                        deletedFiles++
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    ctrl.setProgress(deletedFiles)
                }, {
                    activity?.toast(com.bigberry.comicvn.R.string.cache_delete_error)
                }, {
                    ctrl.finish()
                    activity?.toast(resources?.getString(com.bigberry.comicvn.R.string.cache_deleted, deletedFiles))
                    findPreference(CLEAR_CACHE_KEY)?.summary =
                            resources?.getString(com.bigberry.comicvn.R.string.used_cache, chapterCache.readableSize)
                })
    }

    class DeletingFilesDialogController : DialogController() {

        var total = 0

        private var materialDialog: MaterialDialog? = null

        override fun onCreateDialog(savedViewState: Bundle?): Dialog {
            return MaterialDialog.Builder(activity!!)
                    .title(com.bigberry.comicvn.R.string.deleting)
                    .progress(false, total, true)
                    .cancelable(false)
                    .build()
                    .also { materialDialog = it }
        }

        override fun onDestroyView(view: View) {
            super.onDestroyView(view)
            materialDialog = null
        }

        override fun onRestoreInstanceState(savedInstanceState: Bundle) {
            super.onRestoreInstanceState(savedInstanceState)
            finish()
        }

        fun setProgress(deletedFiles: Int) {
            materialDialog?.setProgress(deletedFiles)
        }

        fun finish() {
            router.popController(this)
        }
    }

    class ClearDatabaseDialogController : DialogController() {
        override fun onCreateDialog(savedViewState: Bundle?): Dialog {
            return MaterialDialog.Builder(activity!!)
                    .content(com.bigberry.comicvn.R.string.clear_database_confirmation)
                    .positiveText(android.R.string.yes)
                    .negativeText(android.R.string.no)
                    .onPositive { _, _ ->
                        (targetController as? SettingsAdvancedController)?.clearDatabase()
                    }
                    .build()
        }
    }

    private fun clearDatabase() {
        // Avoid weird behavior by going back to the library.
        val newBackstack = listOf(RouterTransaction.with(LibraryController())) +
                router.backstack.drop(1)

        router.setBackstack(newBackstack, FadeChangeHandler())

        // Set navi menu select Library
        if (activity is MainActivity) {
            (activity as MainActivity).setSelectedDrawerItem(R.id.nav_drawer_library)
        }

        db.deleteMangasNotInLibrary().executeAsBlocking()
        db.deleteHistoryNoLastRead().executeAsBlocking()
        activity?.toast(com.bigberry.comicvn.R.string.clear_database_completed)
    }

    private companion object {
        const val CLEAR_CACHE_KEY = "pref_clear_cache_key"
    }
}
