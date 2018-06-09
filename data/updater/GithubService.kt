package com.bigberry.comicvn.data.updater

import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import rx.Observable

/**
 * Used to connect with the Github API.
 */
interface GithubService {

    companion object {
        fun create(): GithubService {
            val restAdapter = Retrofit.Builder()
                    .baseUrl("https://truyentranhviet.org:8888")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build()

            return restAdapter.create(GithubService::class.java)
        }
    }

    @GET("/api/user/checkupdate")
    fun getLatestVersion(): Observable<GithubRelease>

}