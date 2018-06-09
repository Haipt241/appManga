package com.bigberry.comicvn.ui.setting

import android.support.v7.preference.PreferenceScreen
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bigberry.comicvn.R
import com.bigberry.comicvn.util.getResourceColor

class SettingsMainController : SettingsController() {
    override fun setupPreferenceScreen(screen: PreferenceScreen) = with(screen) {
        titleRes = R.string.label_settings

        val tintColor = context.getResourceColor(R.attr.colorAccent)

        preference {
            iconRes = R.drawable.ic_tune_black_24dp
            iconTint = tintColor
            titleRes = R.string.pref_category_general
            onClick { navigateTo(SettingsGeneralController()) }
        }
        preference {
            iconRes = R.drawable.ic_chrome_reader_mode_black_24dp
            iconTint = tintColor
            titleRes = R.string.pref_category_reader
            onClick { navigateTo(SettingsReaderController()) }
        }
        preference {
            iconRes = R.drawable.ic_file_download_black_24dp
            iconTint = tintColor
            titleRes = R.string.pref_category_downloads
            onClick { navigateTo(SettingsDownloadController()) }
        }
//        preference {
//            iconRes = R.drawable.ic_language_black_24dp
//            iconTint = tintColor
//            titleRes = R.string.pref_category_sources
//            onClick { navigateTo(SettingsSourcesController()) }
//        }
        preference {
            iconRes = R.drawable.ic_backup_black_24dp
            iconTint = tintColor
            titleRes = R.string.backup
            onClick { navigateTo(SettingsBackupController()) }
        }
        preference {
            iconRes = R.drawable.ic_code_black_24dp
            iconTint = tintColor
            titleRes = R.string.pref_category_advanced
            onClick { navigateTo(SettingsAdvancedController()) }
        }
        preference {
            iconRes = R.drawable.ic_help_black_24dp
            iconTint = tintColor
            titleRes = R.string.pref_category_about
            onClick { navigateTo(SettingsAboutController()) }
        }
    }

    private fun navigateTo(controller: SettingsController) {
        router.pushController(RouterTransaction.with(controller)
                .pushChangeHandler(FadeChangeHandler())
                .popChangeHandler(FadeChangeHandler()))
    }
}