package com.bigberry.comicvn.ui.main

import com.bigberry.comicvn.source.model.Page
import com.bigberry.comicvn.source.model.SChapter
import com.bigberry.comicvn.source.model.SManga
import com.bigberry.comicvn.source.online.LoginSource
import okhttp3.Response
import rx.Observable

/**
 * Created by ThangBK on 7/19/17.
 */
interface AccountLoginSource
{
    fun isLogged(): Boolean

    fun login(username: String, password: String): Observable<Boolean>

    fun isAuthenticationSuccessful(response: Response): Boolean

}