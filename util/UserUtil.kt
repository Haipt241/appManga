package com.bigberry.comicvn.util

import android.Manifest
import android.content.Context
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.telephony.TelephonyManager
import android.util.Log
import com.bigberry.comicvn.BuildConfig
import com.bigberry.comicvn.Constants
import com.bigberry.comicvn.data.preference.PreferencesHelper
import com.bigberry.comicvn.network.NetworkHelper
import com.bigberry.comicvn.network.NetworkUtil
import com.bigberry.comicvn.network.POST
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import okhttp3.*
import org.json.JSONObject
import uy.kohesive.injekt.injectLazy

/**
 * Created by ThangBK on 7/20/17.
 */

object UserUtil {

    val preferences: PreferencesHelper by injectLazy()

    /**
     * Headers builder for requests. Implementations can override this method for custom headers.
     */
    fun headersBuilder() = Headers.Builder().apply {
        add("User-Agent", "Android " + Build.MODEL + "/" + Build.VERSION.CODENAME)
    }

    fun getInfoRequest(context: Context): Request
    {
        var imei = "no"
        var phone = "no"
        var email = "no"

        if (context.hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            try {
                imei = telephonyManager.getDeviceId()
            }
            catch(e: Exception)
            {
                Answers.getInstance().logCustom(CustomEvent("Get phone error"));
            }
            try {
                phone = telephonyManager.getLine1Number();
            }
            catch(e: Exception)
            {
                Answers.getInstance().logCustom(CustomEvent("Get imei error"));
            }
        }

        if (context.hasPermission(Manifest.permission.GET_ACCOUNTS)) {

            try {
                email = UserEmailFetcher.getEmail(context)
            }
            catch(e: Exception)
            {
                Answers.getInstance().logCustom(CustomEvent("Get email error"));
            }
        }

        val jsonObject = JSONObject()
        jsonObject.put("email", email)
        jsonObject.put("imei", imei)

        val optionObject = JSONObject()
        optionObject.put("phone", phone)
        optionObject.put("os", "Android")
        optionObject.put("os_version", Build.VERSION.SDK_INT)
        optionObject.put("version", BuildConfig.VERSION_NAME)
        optionObject.put("version_code", BuildConfig.VERSION_CODE)

        jsonObject.put("options", optionObject.toString())

        val JSON = MediaType.parse("application/json; charset=utf-8")
        val body = RequestBody.create(JSON, jsonObject.toString())
        return POST("https://"+Constants.HOST_NAME_TRUYENTRANHVIET+":8888/api/user/register", body = body, headers = headersBuilder().build())
//        return POST("http://192.168.1.7:8100/api/user/register", body = body, headers = headersBuilder().build())
    }

    fun sendUserInfo(context: Context)
    {
        NetworkUtil.send(getInfoRequest(context))
                .subscribe({ response ->
                    val json = response.body()!!.string()
                    val jsonObject = JSONObject(json)
                    val code = jsonObject["code"]
                    if (code == 200 || code == 102 || code == 101) {
                        preferences.setSendUserInfo(true)
                    }
                }, {
                    error ->
                    Log.d("USER_INFO", error.message)
                })

    }



}