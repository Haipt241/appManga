package com.bigberry.comicvn.ui.setting

import android.app.Dialog
import android.os.Bundle
import android.support.v7.preference.PreferenceScreen
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.bigberry.comicvn.BuildConfig
import com.bigberry.comicvn.R
import com.bigberry.comicvn.data.updater.GithubUpdateChecker
import com.bigberry.comicvn.data.updater.GithubUpdateResult
import com.bigberry.comicvn.data.updater.UpdateCheckerJob
import com.bigberry.comicvn.data.updater.UpdateDownloaderService
import com.bigberry.comicvn.ui.base.controller.DialogController
import com.bigberry.comicvn.util.toast
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import com.bigberry.comicvn.data.preference.PreferenceKeys as Keys

class SettingsAboutController : SettingsController() {

    /**
     * Checks for new releases
     */
    private val updateChecker by lazy { GithubUpdateChecker() }

    /**
     * The subscribtion service of the obtained release object
     */
    private var releaseSubscription: Subscription? = null

    private val isUpdaterEnabled = !com.bigberry.comicvn.BuildConfig.DEBUG && com.bigberry.comicvn.BuildConfig.INCLUDE_UPDATER

    override fun setupPreferenceScreen(screen: PreferenceScreen) = with(screen) {
        titleRes = com.bigberry.comicvn.R.string.pref_category_about

        switchPreference {
            key = "acra.enable"
            titleRes = com.bigberry.comicvn.R.string.pref_enable_acra
            summaryRes = com.bigberry.comicvn.R.string.pref_acra_summary
            defaultValue = true
        }
        switchPreference {
            key = Keys.automaticUpdates
            titleRes = com.bigberry.comicvn.R.string.pref_enable_automatic_updates
            summaryRes = com.bigberry.comicvn.R.string.pref_enable_automatic_updates_summary
            defaultValue = false

            if (isUpdaterEnabled) {
                onChange { newValue ->
                    val checked = newValue as Boolean
                    if (checked) {
                        UpdateCheckerJob.setupTask()
                    } else {
                        UpdateCheckerJob.cancelTask()
                    }
                    true
                }
            } else {
                isVisible = false
            }
        }
        preference {
            titleRes = com.bigberry.comicvn.R.string.version
            summary = if (com.bigberry.comicvn.BuildConfig.DEBUG)
                "r" + com.bigberry.comicvn.BuildConfig.COMMIT_COUNT
            else
                com.bigberry.comicvn.BuildConfig.VERSION_NAME

            if (isUpdaterEnabled) {
                onClick { checkVersion() }
            }
        }
        preference {
            titleRes = com.bigberry.comicvn.R.string.build_time
            summary = getFormattedBuildTime()
        }
    }

    override fun onDestroyView(view: View) {
        super.onDestroyView(view)
        releaseSubscription?.unsubscribe()
        releaseSubscription = null
    }

    /**
     * Checks version and shows a user prompt if an update is available.
     */
    private fun checkVersion() {
        if (activity == null) return

        activity?.toast(com.bigberry.comicvn.R.string.update_check_look_for_updates)
        releaseSubscription?.unsubscribe()
        releaseSubscription = updateChecker.checkForUpdate()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    when (result) {
                        is GithubUpdateResult.NewUpdate -> {
                            val body = result.release.changeLog
                            val url = result.release.downloadLink

                            // Create confirmation window
                            NewUpdateDialogController(body, url).showDialog(router)
                        }
                        is GithubUpdateResult.NoNewUpdate -> {
                            activity?.toast(com.bigberry.comicvn.R.string.update_check_no_new_updates)
                        }
                    }
                }, { error ->
                    Timber.e(error)
                })
    }

    class NewUpdateDialogController(bundle: Bundle? = null) : DialogController(bundle) {

        constructor(body: String, url: String) : this(Bundle().apply {
            putString(BODY_KEY, body)
            putString(URL_KEY, url)
        })

        override fun onCreateDialog(savedViewState: Bundle?): Dialog {
            return MaterialDialog.Builder(activity!!)
                    .title(com.bigberry.comicvn.R.string.update_check_title)
                    .content(args.getString(BODY_KEY))
                    .positiveText(com.bigberry.comicvn.R.string.update_check_confirm)
                    .negativeText(com.bigberry.comicvn.R.string.update_check_ignore)
                    .onPositive { _, _ ->
                        val appContext = applicationContext
                        if (appContext != null) {
                            // Start download
                            val url = args.getString(URL_KEY)
                            UpdateDownloaderService.downloadUpdate(appContext, url)
                        }
                    }
                    .build()
        }

        private companion object {
            const val BODY_KEY = "NewUpdateDialogController.body"
            const val URL_KEY = "NewUpdateDialogController.key"
        }
    }

    private fun getFormattedBuildTime(): String {
        try {
            val inputDf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US)
            inputDf.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputDf.parse(com.bigberry.comicvn.BuildConfig.BUILD_TIME)

            val outputDf = DateFormat.getDateTimeInstance(
                    DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
            outputDf.timeZone = TimeZone.getDefault()

            return outputDf.format(date)
        } catch (e: ParseException) {
            return com.bigberry.comicvn.BuildConfig.BUILD_TIME
        }
    }
}
