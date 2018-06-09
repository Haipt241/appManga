package com.bigberry.comicvn.ui.setting

import android.graphics.drawable.Drawable
import android.support.v7.preference.PreferenceGroup
import android.support.v7.preference.PreferenceScreen
import com.bigberry.comicvn.R
import com.bigberry.comicvn.data.preference.getOrDefault
import com.bigberry.comicvn.source.SourceManager
import com.bigberry.comicvn.source.online.HttpSource
import com.bigberry.comicvn.source.online.LoginSource
import com.bigberry.comicvn.widget.preference.LoginCheckBoxPreference
import com.bigberry.comicvn.widget.preference.SourceLoginDialog
import com.bigberry.comicvn.widget.preference.SwitchPreferenceCategory
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.util.*

class SettingsSourcesController : SettingsController(),
        SourceLoginDialog.Listener {

    private val onlineSources by lazy { Injekt.get<SourceManager>().getOnlineSources() }

    override fun setupPreferenceScreen(screen: PreferenceScreen) = with(screen) {
        titleRes = R.string.pref_category_sources

        // Get the list of active language codes.
        val activeLangsCodes = preferences.enabledLanguages().getOrDefault()

        // Get a map of sources grouped by language.
        val sourcesByLang = onlineSources.groupByTo(TreeMap(), { it.lang })

        // Order first by active languages, then inactive ones
        val orderedLangs = sourcesByLang.keys.filter { it in activeLangsCodes } +
                sourcesByLang.keys.filterNot { it in activeLangsCodes }

        orderedLangs.forEach { lang ->
            val sources = sourcesByLang[lang].orEmpty().sortedBy { it.name }

            // Create a preference group and set initial state and change listener
            SwitchPreferenceCategory(context).apply {
                preferenceScreen.addPreference(this)
                title = Locale(lang).let { it.getDisplayLanguage(it).capitalize() }
                isPersistent = false
                if (lang in activeLangsCodes) {
                    setChecked(true)
                    addLanguageSources(this, sources)
                }

                onChange { newValue ->
                    val checked = newValue as Boolean
                    val current = preferences.enabledLanguages().getOrDefault()
                    if (!checked) {
                        preferences.enabledLanguages().set(current - lang)
                        removeAll()
                    } else {
                        preferences.enabledLanguages().set(current + lang)
                        addLanguageSources(this, sources)
                    }
                    true
                }
            }
        }
    }

    override fun setDivider(divider: Drawable?) {
        super.setDivider(null)
    }

    /**
     * Adds the source list for the given group (language).
     *
     * @param group the language category.
     */
    private fun addLanguageSources(group: PreferenceGroup, sources: List<HttpSource>) {
        val hiddenCatalogues = preferences.hiddenCatalogues().getOrDefault()

        sources.forEach { source ->
            val sourcePreference = LoginCheckBoxPreference(group.context, source).apply {
                val id = source.id.toString()
                title = source.name
                key = getSourceKey(source.id)
                isPersistent = false
                isChecked = id !in hiddenCatalogues

                onChange { newValue ->
                    val checked = newValue as Boolean
                    val current = preferences.hiddenCatalogues().getOrDefault()

                    preferences.hiddenCatalogues().set(if (checked)
                        current - id
                    else
                        current + id)

                    true
                }

                setOnLoginClickListener {
                    val dialog = SourceLoginDialog(source)
                    dialog.targetController = this@SettingsSourcesController
                    dialog.showDialog(router)
                }
            }

            group.addPreference(sourcePreference)
        }
    }

    override fun loginDialogClosed(source: LoginSource) {
        val pref = findPreference(getSourceKey(source.id)) as? LoginCheckBoxPreference
        pref?.notifyChanged()
    }

    private fun getSourceKey(sourceId: Long): String {
        return "source_$sourceId"
    }

}