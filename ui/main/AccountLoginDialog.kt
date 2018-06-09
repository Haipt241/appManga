package com.bigberry.comicvn.ui.main

import android.os.Bundle
import android.view.View
import com.bigberry.comicvn.R
import com.bigberry.comicvn.source.SourceManager
import com.bigberry.comicvn.source.online.LoginSource
import com.bigberry.comicvn.util.toast
import com.bigberry.comicvn.widget.preference.LoginDialogPreference
import kotlinx.android.synthetic.main.pref_account_login.view.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class AccountLoginDialog(bundle: Bundle? = null) : LoginDialogPreference(bundle) {

    private var loginListener: Listener? = null

    private var source: AccountLoginSource? = null

    constructor(listener: Listener) : this() {
        this.loginListener = listener
        this.source = listener as AccountLoginSource
    }

    override fun setCredentialsOnView(view: View) = with(view) {
        dialog_title.text = context.getString(R.string.login_title, source.toString())
        username.setText(preferences.username())
        password.setText(preferences.password())
    }

    override fun checkLogin() {
        requestSubscription?.unsubscribe()

        v?.apply {
            if (username.text.isEmpty() || password.text.isEmpty())
                return

            login.progress = 1
            if (source != null) {
                requestSubscription = source!!.login(username.text.toString(), password.text.toString())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ logged ->
                            if (logged) {
                                preferences.setCredentials(username.text.toString(), password.text.toString())

                                dialog?.dismiss()
                                context.toast(R.string.login_success)
                            } else {
                                preferences.setCredentials("", "")
                                login.progress = -1
                            }
                        }, { error ->
                            login.progress = -1
                            login.setText(R.string.unknown_error)
                            error.message?.let { context.toast(it) }
                        })
            }
        }
    }

    override fun onDialogClosed() {
        super.onDialogClosed()
        loginListener?.loginDialogClosed()
    }

    interface Listener {
        fun loginDialogClosed()
    }
}
