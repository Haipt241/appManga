package com.bigberry.comicvn.data.track

import android.content.Context
import com.bigberry.comicvn.data.track.anilist.Anilist
import com.bigberry.comicvn.data.track.comicviet.ComicViet
import com.bigberry.comicvn.data.track.kitsu.Kitsu
import com.bigberry.comicvn.data.track.myanimelist.Myanimelist

class TrackManager(private val context: Context) {

    companion object {
        const val MYANIMELIST = 1
        const val ANILIST = 2
        const val KITSU = 3
        const val COMICVIET = 4
    }

    val myAnimeList = Myanimelist(context, MYANIMELIST)

    val aniList = Anilist(context, ANILIST)

    val kitsu = Kitsu(context, KITSU)

    val comicViet = ComicViet(context, COMICVIET)

    val services = listOf(myAnimeList, aniList, kitsu, comicViet)

    fun getService(id: Int) = services.find { it.id == id }

    fun hasLoggedServices() = services.any { it.isLogged }

}
