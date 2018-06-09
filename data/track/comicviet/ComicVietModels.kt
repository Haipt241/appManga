package com.bigberry.comicvn.data.track.comicviet

import android.support.annotation.CallSuper
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.bigberry.comicvn.data.database.models.Track
import com.bigberry.comicvn.data.track.TrackManager

open class ComicVietManga(obj: JsonObject) {
    val id by obj.byInt
    val canonicalTitle by obj["attributes"].byString
    val chapterCount = obj["attributes"].obj.get("chapterCount").nullInt
    val type = obj["attributes"].obj.get("mangaType").nullString

    @CallSuper
    open fun toTrack() = Track.create(TrackManager.KITSU).apply {
        remote_id = this@ComicVietManga.id
        title = canonicalTitle
        total_chapters = chapterCount ?: 0
    }
}

class ComicVietLibManga(obj: JsonObject, manga: JsonObject) : ComicVietManga(manga) {
    val remoteId by obj.byInt("id")
    val status by obj["attributes"].byString
    val ratingTwenty = obj["attributes"].obj.get("ratingTwenty").nullString
    val progress by obj["attributes"].byInt

    override fun toTrack() = super.toTrack().apply {
        remote_id = remoteId
        status = toTrackStatus()
        score = ratingTwenty?.let { it.toInt() / 2f } ?: 0f
        last_chapter_read = progress
    }

    private fun toTrackStatus() = when (status) {
        "current" -> ComicViet.READING
        "completed" -> ComicViet.COMPLETED
        "on_hold" -> ComicViet.ON_HOLD
        "dropped" -> ComicViet.DROPPED
        "planned" -> ComicViet.PLAN_TO_READ
        else -> throw Exception("Unknown status")
    }

}

fun Track.toComicVietStatus() = when (status) {
    ComicViet.READING -> "current"
    ComicViet.COMPLETED -> "completed"
    ComicViet.ON_HOLD -> "on_hold"
    ComicViet.DROPPED -> "dropped"
    ComicViet.PLAN_TO_READ -> "planned"
    else -> throw Exception("Unknown status")
}

fun Track.toComicVietScore(): String? {
    return if (score > 0) (score * 2).toInt().toString() else null
}
