package com.bigberry.comicvn.widget.preference

import android.os.Bundle
import android.view.View
import com.bigberry.comicvn.R
import com.bigberry.comicvn.source.Source
import com.bigberry.comicvn.source.SourceManager
import com.bigberry.comicvn.source.online.LoginSource
import com.bigberry.comicvn.util.toast
import kotlinx.android.synthetic.main.pref_account_login.view.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class SourceLoginDialog(bundle: Bundle? = null) : LoginDialogPreference(bundle) {

    private val source = Injekt.get<SourceManager>().get(args.getLong("key")) as LoginSource

    constructor(source: Source) : this(Bundle().apply { putLong("key", source.id) })

    override fun setCredentialsOnView(view: View) = with(view) {
        dialog_title.text = context.getString(R.string.login_title, "Comic Viet")
        username.setText(preferences.username())
        password.setText(preferences.password())
    }

    override fun checkLogin() {
        requestSubscription?.unsubscribe()

        v?.apply {
            if (username.text.isEmpty() || password.text.isEmpty())
                return

            login.progress = 1

            requestSubscription = source.login(username.text.toString(), password.text.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ logged ->
                        if (logged) {
                            preferences.setCredentials(username.text.toString(), password.text.toString())

                            dialog?.dismiss()
                            context.toast(R.string.login_success)
                        } else {
                            preferences.setSourceCredentials(source, "", "")
                            login.progress = -1
                        }
                    }, { error ->
                        login.progress = -1
                        login.setText(R.string.unknown_error)
                        error.message?.let { context.toast(it) }
                    })
        }
    }

    override fun onDialogClosed() {
        super.onDialogClosed()
        (targetController as? Listener)?.loginDialogClosed(source)
    }

    interface Listener {
        fun loginDialogClosed(source: LoginSource)
    }

}
