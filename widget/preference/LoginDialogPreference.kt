package com.bigberry.comicvn.widget.preference

import android.app.Dialog
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.dd.processbutton.iml.ActionProcessButton
import com.bigberry.comicvn.R
import com.bigberry.comicvn.data.preference.PreferencesHelper
import com.bigberry.comicvn.ui.base.controller.DialogController
import com.bigberry.comicvn.widget.SimpleTextWatcher
import kotlinx.android.synthetic.main.pref_account_login.view.*
import rx.Subscription
import uy.kohesive.injekt.injectLazy

abstract class LoginDialogPreference(bundle: Bundle? = null) : DialogController(bundle) {

    var v: View? = null
        private set

    val preferences: PreferencesHelper by injectLazy()

    var requestSubscription: Subscription? = null

    override fun onCreateDialog(savedState: Bundle?): Dialog {
        val dialog = MaterialDialog.Builder(activity!!)
                .customView(R.layout.pref_account_login, false)
                .negativeText(android.R.string.cancel)
                .build()

        onViewCreated(dialog.view, savedState)

        return dialog
    }

    fun onViewCreated(view: View, savedState: Bundle?) {
        v = view.apply {
            show_password.setOnCheckedChangeListener { v, isChecked ->
                if (isChecked)
                    password.transformationMethod = null
                else
                    password.transformationMethod = PasswordTransformationMethod()
            }

            login.setMode(ActionProcessButton.Mode.ENDLESS)
            login.setOnClickListener { checkLogin() }

            setCredentialsOnView(this)

            show_password.isEnabled = password.text.isNullOrEmpty()

            password.addTextChangedListener(object : SimpleTextWatcher() {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.isEmpty()) {
                        show_password.isEnabled = true
                    }
                }
            })
        }

    }

    override fun onChangeStarted(handler: ControllerChangeHandler, type: ControllerChangeType) {
        super.onChangeStarted(handler, type)
        if (!type.isEnter) {
            onDialogClosed()
        }
    }

    open fun onDialogClosed() {
        requestSubscription?.unsubscribe()
    }

    protected abstract fun checkLogin()

    protected abstract fun setCredentialsOnView(view: View)

}
