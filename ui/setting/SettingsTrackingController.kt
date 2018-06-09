package com.bigberry.comicvn.ui.setting

import android.app.Activity
import android.content.Intent
import android.support.customtabs.CustomTabsIntent
import android.support.v7.preference.PreferenceScreen
import com.bigberry.comicvn.R
import com.bigberry.comicvn.data.track.TrackManager
import com.bigberry.comicvn.data.track.TrackService
import com.bigberry.comicvn.data.track.anilist.AnilistApi
import com.bigberry.comicvn.util.getResourceColor
import com.bigberry.comicvn.widget.preference.LoginPreference
import com.bigberry.comicvn.widget.preference.TrackLoginDialog
import uy.kohesive.injekt.injectLazy
import com.bigberry.comicvn.data.preference.PreferenceKeys as Keys

class SettingsTrackingController : SettingsController(),
        TrackLoginDialog.Listener {

    private val trackManager: TrackManager by injectLazy()

    override fun setupPreferenceScreen(screen: PreferenceScreen) = with(screen) {
        titleRes = R.string.pref_category_tracking

        switchPreference {
            key = Keys.autoUpdateTrack
            titleRes = R.string.pref_auto_update_manga_sync
            defaultValue = true
        }
        switchPreference {
            key = Keys.askUpdateTrack
            titleRes = R.string.pref_ask_update_manga_sync
            defaultValue = false
        }.apply {
            dependency = Keys.autoUpdateTrack // the preference needs to be attached.
        }
        preferenceCategory {
            titleRes = R.string.services

            trackPreference(trackManager.myAnimeList) {
                onClick {
                    val dialog = TrackLoginDialog(trackManager.myAnimeList)
                    dialog.targetController = this@SettingsTrackingController
                    dialog.showDialog(router)
                }
            }
            trackPreference(trackManager.aniList) {
                onClick {
                    val tabsIntent = CustomTabsIntent.Builder()
                            .setToolbarColor(context.getResourceColor(R.attr.colorPrimary))
                            .build()
                    tabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    tabsIntent.launchUrl(activity, AnilistApi.authUrl())
                }
            }
            trackPreference(trackManager.kitsu) {
                onClick {
                    val dialog = TrackLoginDialog(trackManager.kitsu)
                    dialog.targetController = this@SettingsTrackingController
                    dialog.showDialog(router)
                }
            }
        }
    }

    inline fun PreferenceScreen.trackPreference(
            service: TrackService,
            block: (@DSL LoginPreference).() -> Unit
    ): LoginPreference {
        return initThenAdd(LoginPreference(context).apply {
            key = Keys.trackUsername(service.id)
            title = service.name
        }, block)
    }

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        // Manually refresh anilist holder
        updatePreference(trackManager.aniList.id)
    }

    private fun updatePreference(id: Int) {
        val pref = findPreference(Keys.trackUsername(id)) as? LoginPreference
        pref?.notifyChanged()
    }

    override fun trackDialogClosed(service: TrackService) {
        updatePreference(service.id)
    }

}