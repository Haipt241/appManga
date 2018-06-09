package com.bigberry.comicvn.ui.setting

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.support.v7.preference.PreferenceScreen
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.bigberry.comicvn.R
import com.bigberry.comicvn.data.database.DatabaseHelper
import com.bigberry.comicvn.data.library.LibraryUpdateJob
import com.bigberry.comicvn.data.preference.PreferencesHelper
import com.bigberry.comicvn.data.preference.getOrDefault
import com.bigberry.comicvn.ui.base.controller.DialogController
import kotlinx.android.synthetic.main.pref_library_columns.view.*
import rx.Observable
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import com.bigberry.comicvn.data.preference.PreferenceKeys as Keys

class SettingsGeneralController : SettingsController() {

    private val db: DatabaseHelper = Injekt.get()

    override fun setupPreferenceScreen(screen: PreferenceScreen) = with(screen) {
        titleRes = com.bigberry.comicvn.R.string.pref_category_general

        /*
        listPreference {
            key = Keys.lang
            titleRes = R.string.pref_language
            entryValues = arrayOf("", "bg", "en", "es", "fr", "it", "lv", "nl", "pt", "pt-BR", "ru",
                    "vi")
            entries = entryValues.map { value ->
                val locale = LocaleHelper.getLocaleFromString(value.toString())
                locale?.getDisplayName(locale)?.capitalize() ?:
                        context.getString(R.string.system_default)
            }.toTypedArray()
            defaultValue = ""
            summary = "%s"

            onChange { newValue ->
                val activity = activity ?: return@onChange false
                val app = activity.application
                LocaleHelper.changeLocale(newValue.toString())
                LocaleHelper.updateConfiguration(app, app.resources.configuration)
                activity.recreate()
                true
            }
        }

        intListPreference {
            key = Keys.theme
            titleRes = R.string.pref_theme
            entriesRes = arrayOf(R.string.light_theme, R.string.dark_theme, R.string.amoled_theme)
            entryValues = arrayOf("1", "2", "3")
            defaultValue = "1"
            summary = "%s"

            onChange {
                activity?.recreate()
                true
            }
        }
        */
        preference {
            titleRes = com.bigberry.comicvn.R.string.pref_library_columns
            onClick {
                LibraryColumnsDialog().showDialog(router)
            }

            fun getColumnValue(value: Int): String {
                return if (value == 0)
                    context.getString(com.bigberry.comicvn.R.string.default_columns)
                else
                    value.toString()
            }

            Observable.combineLatest(
                    preferences.portraitColumns().asObservable(),
                    preferences.landscapeColumns().asObservable(),
                    { portraitCols, landscapeCols -> Pair(portraitCols, landscapeCols) })
                    .subscribeUntilDestroy { (portraitCols, landscapeCols) ->
                        val portrait = getColumnValue(portraitCols)
                        val landscape = getColumnValue(landscapeCols)
                        summary = "${context.getString(com.bigberry.comicvn.R.string.portrait)}: $portrait, " +
                                "${context.getString(com.bigberry.comicvn.R.string.landscape)}: $landscape"
                    }
        }
        intListPreference {
            key = Keys.startScreen
            titleRes = R.string.pref_start_screen
            entriesRes = arrayOf(R.string.label_library, R.string.label_recent_manga,
                    R.string.label_recent_updates, R.string.label_catalogues)
            entryValues = arrayOf("1", "2", "3", "4")
            defaultValue = "4"
            summary = "%s"
        }
        intListPreference {
            key = Keys.libraryUpdateInterval
            titleRes = com.bigberry.comicvn.R.string.pref_library_update_interval
            entriesRes = arrayOf(com.bigberry.comicvn.R.string.update_never, com.bigberry.comicvn.R.string.update_1hour,
                    com.bigberry.comicvn.R.string.update_2hour, com.bigberry.comicvn.R.string.update_3hour, com.bigberry.comicvn.R.string.update_6hour,
                    com.bigberry.comicvn.R.string.update_12hour, com.bigberry.comicvn.R.string.update_24hour, com.bigberry.comicvn.R.string.update_48hour)
            entryValues = arrayOf("0", "1", "2", "3", "6", "12", "24", "48")
            defaultValue = "12"
            summary = "%s"

            onChange { newValue ->
                // Always cancel the previous task, it seems that sometimes they are not updated.
                LibraryUpdateJob.cancelTask()

                val interval = (newValue as String).toInt()
                if (interval > 0) {
                    LibraryUpdateJob.setupTask(interval)
                }
                true
            }
        }
        multiSelectListPreference {
            key = Keys.libraryUpdateRestriction
            titleRes = com.bigberry.comicvn.R.string.pref_library_update_restriction
            entriesRes = arrayOf(com.bigberry.comicvn.R.string.wifi, com.bigberry.comicvn.R.string.charging)
            entryValues = arrayOf("wifi", "ac")
            summaryRes = com.bigberry.comicvn.R.string.pref_library_update_restriction_summary

            preferences.libraryUpdateInterval().asObservable()
                    .subscribeUntilDestroy { isVisible = it > 0 }

            onChange {
                // Post to event looper to allow the preference to be updated.
                Handler().post { LibraryUpdateJob.setupTask() }
                true
            }
        }
        switchPreference {
            key = Keys.updateOnlyNonCompleted
            titleRes = com.bigberry.comicvn.R.string.pref_update_only_non_completed
            defaultValue = false
        }

        val dbCategories = db.getCategories().executeAsBlocking()

        multiSelectListPreference {
            key = Keys.libraryUpdateCategories
            titleRes = com.bigberry.comicvn.R.string.pref_library_update_categories
            entries = dbCategories.map { it.name }.toTypedArray()
            entryValues = dbCategories.map { it.id.toString() }.toTypedArray()

            preferences.libraryUpdateCategories().asObservable()
                    .subscribeUntilDestroy {
                        val selectedCategories = it
                                .mapNotNull { id -> dbCategories.find { it.id == id.toInt() } }
                                .sortedBy { it.order }

                        summary = if (selectedCategories.isEmpty())
                            context.getString(com.bigberry.comicvn.R.string.all)
                        else
                            selectedCategories.joinToString { it.name }
                    }
        }
        intListPreference {
            key = Keys.defaultCategory
            titleRes = com.bigberry.comicvn.R.string.default_category

            val selectedCategory = dbCategories.find { it.id == preferences.defaultCategory() }
            entries = arrayOf(context.getString(com.bigberry.comicvn.R.string.default_category_summary)) +
                    dbCategories.map { it.name }.toTypedArray()
            entryValues = arrayOf("-1") + dbCategories.map { it.id.toString() }.toTypedArray()
            defaultValue = "-1"
            summary = selectedCategory?.name ?: context.getString(com.bigberry.comicvn.R.string.default_category_summary)

            onChange { newValue ->
                summary = dbCategories.find {
                    it.id == (newValue as String).toInt()
                }?.name ?: context.getString(com.bigberry.comicvn.R.string.default_category_summary)
                true
            }
        }
    }

    class LibraryColumnsDialog : DialogController() {

        private val preferences: PreferencesHelper = Injekt.get()

        private var portrait = preferences.portraitColumns().getOrDefault()
        private var landscape = preferences.landscapeColumns().getOrDefault()

        override fun onCreateDialog(savedViewState: Bundle?): Dialog {
            val dialog = MaterialDialog.Builder(activity!!)
                    .title(com.bigberry.comicvn.R.string.pref_library_columns)
                    .customView(com.bigberry.comicvn.R.layout.pref_library_columns, false)
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
                    .onPositive { _, _ ->
                        preferences.portraitColumns().set(portrait)
                        preferences.landscapeColumns().set(landscape)
                    }
                    .build()

            onViewCreated(dialog.view)
            return dialog
        }

        fun onViewCreated(view: View) {
            with(view.portrait_columns) {
                displayedValues = arrayOf(context.getString(com.bigberry.comicvn.R.string.default_columns)) +
                        IntRange(1, 10).map(Int::toString)
                value = portrait

                setOnValueChangedListener { _, _, newValue ->
                    portrait = newValue
                }
            }
            with(view.landscape_columns) {
                displayedValues = arrayOf(context.getString(com.bigberry.comicvn.R.string.default_columns)) +
                        IntRange(1, 10).map(Int::toString)
                value = landscape

                setOnValueChangedListener { _, _, newValue ->
                    landscape = newValue
                }
            }
        }

    }

}