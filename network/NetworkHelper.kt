package com.bigberry.comicvn.network

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory

class NetworkHelper(context: Context) {

    private val cacheDir = File(context.cacheDir, "network_cache")

    private val cacheSize = 5L * 1024 * 1024 // 5 MiB

    private val cookieManager = PersistentCookieJar(context)

    private val allHostTrust = arrayOf<HostTrustManager>(object : HostTrustManager(){})

    fun getSSLContext(): SSLSocketFactory
    {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, allHostTrust, null)

        return sslContext.socketFactory
    }

    val client = OkHttpClient.Builder()
            .cookieJar(cookieManager)
            .cache(Cache(cacheDir, cacheSize))
            .sslSocketFactory(getSSLContext(), allHostTrust[0])
            .hostnameVerifier(HostVerify())
            .build()

    val forceCacheClient = client.newBuilder()
            .addNetworkInterceptor { chain ->
                val originalResponse = chain.proceed(chain.request())
                originalResponse.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "max-age=600")
                        .build()
            }
            .build()

    val cloudflareClient = client.newBuilder()
            .addInterceptor(CloudflareInterceptor())
            .build()

    val cookies: PersistentCookieStore
        get() = cookieManager.store

}
