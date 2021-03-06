package com.bigberry.comicvn.ui.reader

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.bigberry.comicvn.R
import com.bigberry.comicvn.data.preference.PreferencesHelper
import com.bigberry.comicvn.data.preference.getOrDefault
import com.bigberry.comicvn.util.plusAssign
import com.bigberry.comicvn.widget.IgnoreFirstSpinnerListener
import kotlinx.android.synthetic.main.reader_settings_dialog.view.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import uy.kohesive.injekt.injectLazy
import java.util.concurrent.TimeUnit.MILLISECONDS

class ReaderSettingsDialog : DialogFragment() {

    private val preferences by injectLazy<PreferencesHelper>()

    private lateinit var subscriptions: CompositeSubscription

    override fun onCreateDialog(savedState: Bundle?): Dialog {
        val dialog = MaterialDialog.Builder(activity)
                .title(com.bigberry.comicvn.R.string.label_settings)
                .customView(com.bigberry.comicvn.R.layout.reader_settings_dialog, true)
                .positiveText(android.R.string.ok)
                .build()

        subscriptions = CompositeSubscription()
        onViewCreated(dialog.view, savedState)

        return dialog
    }

    override fun onViewCreated(view: View, savedState: Bundle?) = with(view) {
        viewer.onItemSelectedListener = IgnoreFirstSpinnerListener { position ->
            subscriptions += Observable.timer(250, MILLISECONDS, AndroidSchedulers.mainThread())
                    .subscribe {
                        (activity as ReaderActivity).presenter.updateMangaViewer(position)
                        activity.recreate()
                    }
        }
        viewer.setSelection((activity as ReaderActivity).presenter.manga.viewer, false)

        rotation_mode.onItemSelectedListener = IgnoreFirstSpinnerListener { position ->
            subscriptions += Observable.timer(250, MILLISECONDS)
                    .subscribe {
                        preferences.rotation().set(position + 1)
                    }
        }
        rotation_mode.setSelection(preferences.rotation().getOrDefault() - 1, false)

        scale_type.onItemSelectedListener = IgnoreFirstSpinnerListener { position ->
            preferences.imageScaleType().set(position + 1)
        }
        scale_type.setSelection(preferences.imageScaleType().getOrDefault() - 1, false)

        zoom_start.onItemSelectedListener = IgnoreFirstSpinnerListener { position ->
            preferences.zoomStart().set(position + 1)
        }
        zoom_start.setSelection(preferences.zoomStart().getOrDefault() - 1, false)

        image_decoder.onItemSelectedListener = IgnoreFirstSpinnerListener { position ->
            preferences.imageDecoder().set(position)
        }
        image_decoder.setSelection(preferences.imageDecoder().getOrDefault(), false)

        background_color.onItemSelectedListener = IgnoreFirstSpinnerListener { position ->
            preferences.readerTheme().set(position)
        }
        background_color.setSelection(preferences.readerTheme().getOrDefault(), false)

        show_page_number.isChecked = preferences.showPageNumber().getOrDefault()
        show_page_number.setOnCheckedChangeListener { v, isChecked ->
            preferences.showPageNumber().set(isChecked)
        }

        fullscreen.isChecked = preferences.fullscreen().getOrDefault()
        fullscreen.setOnCheckedChangeListener { v, isChecked ->
            preferences.fullscreen().set(isChecked)
        }

        crop_borders.isChecked = preferences.cropBorders().getOrDefault()
        crop_borders.setOnCheckedChangeListener { v, isChecked ->
            preferences.cropBorders().set(isChecked)
        }
    }

    override fun onDestroyView() {
        subscriptions.unsubscribe()
        super.onDestroyView()
    }

}