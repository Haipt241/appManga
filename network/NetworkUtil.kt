package com.bigberry.comicvn.network

import android.content.Context
import android.util.Log
import com.bigberry.comicvn.R
import com.bigberry.comicvn.data.preference.PreferencesHelper
import com.bigberry.comicvn.util.UserUtil
import com.bigberry.comicvn.util.toast
import kotlinx.android.synthetic.main.pref_account_login.view.*
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import uy.kohesive.injekt.injectLazy
import it.gmariotti.changelibs.library.Util.isConnected
import android.net.NetworkInfo
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager



/**
 * Created by ThangBK on 7/20/17.
 */
object NetworkUtil {

    /**
     * Network service.
     */
    val network: NetworkHelper by injectLazy()

    /**
     * Headers used for requests.
     */
    val headers: Headers by lazy { UserUtil.headersBuilder().build() }

    /**
     * Default network client for doing requests.
     */
    val client: OkHttpClient
        get() = network.client

    fun send(request: Request): Observable<Response>
    {
        return client.newCall(request)
                .asObservable()
                .subscribeOn(Schedulers.io())
    }

    fun isOnline(context: Context): Boolean {

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        //should check null because in airplane mode it will be null
        return netInfo != null && netInfo.isConnected
    }
}