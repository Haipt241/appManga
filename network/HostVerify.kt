package com.bigberry.comicvn.network

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

/**
 * Created by ThangBK on 5/22/17.
 */
class HostVerify: HostnameVerifier
{
    override fun verify(hostname: String?, session: SSLSession?): Boolean {
//        return hostname.equals(Constants.HOST_NAME_TRUYENTRANHVIET, true)
        return true
    }

}