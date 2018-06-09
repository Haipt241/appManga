package com.bigberry.comicvn.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import com.bigberry.comicvn.data.preference.PreferencesHelper
import com.bigberry.comicvn.util.UserUtil
import uy.kohesive.injekt.injectLazy

/**
 * Created by ThangBK on 7/20/17.
 */
class NetworkReceiver: BroadcastReceiver()
{
    protected val preferences: PreferencesHelper by injectLazy()

    override fun onReceive(context: Context, intent: Intent)
    {
        if (NetworkUtil.isOnline(context)) {
            if (!preferences.isSendUserInfo())
                UserUtil.sendUserInfo(context)
        }
    }
}