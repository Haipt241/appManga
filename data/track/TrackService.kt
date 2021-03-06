package com.bigberry.comicvn.data.track

import android.support.annotation.CallSuper
import android.support.annotation.DrawableRes
import com.bigberry.comicvn.data.database.models.Track
import com.bigberry.comicvn.data.preference.PreferencesHelper
import com.bigberry.comicvn.network.NetworkHelper
import okhttp3.OkHttpClient
import rx.Completable
import rx.Observable
import uy.kohesive.injekt.injectLazy

abstract class TrackService(val id: Int) {

    val preferences: PreferencesHelper by injectLazy()
    val networkService: NetworkHelper by injectLazy()

    open val client: OkHttpClient
        get() = networkService.client

    // Name of the manga sync service to display
    abstract val name: String

    @DrawableRes
    abstract fun getLogo(): Int

    abstract fun getLogoColor(): Int

    abstract fun getStatusList(): List<Int>

    abstract fun getStatus(status: Int): String

    abstract fun getScoreList(): List<String>

    open fun indexToScore(index: Int): Float {
        return index.toFloat()
    }

    abstract fun displayScore(track: Track): String

    abstract fun add(track: Track): Observable<Track>

    abstract fun update(track: Track): Observable<Track>

    abstract fun bind(track: Track): Observable<Track>

    abstract fun search(query: String): Observable<List<Track>>

    abstract fun refresh(track: Track): Observable<Track>

    abstract fun login(username: String, password: String): Completable

    @CallSuper
    open fun logout() {
        preferences.setTrackCredentials(this, "", "")
    }

    open val isLogged: Boolean
        get() = !getUsername().isEmpty() &&
                !getPassword().isEmpty()

    fun getUsername() = preferences.trackUsername(this)

    fun getPassword() = preferences.trackPassword(this)

    fun saveCredentials(username: String, password: String) {
        preferences.setTrackCredentials(this, username, password)
    }

}
