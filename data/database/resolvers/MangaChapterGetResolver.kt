package com.bigberry.comicvn.data.database.resolvers

import android.database.Cursor
import com.pushtorefresh.storio.sqlite.operations.get.DefaultGetResolver
import com.bigberry.comicvn.data.database.mappers.ChapterGetResolver
import com.bigberry.comicvn.data.database.mappers.MangaGetResolver
import com.bigberry.comicvn.data.database.models.MangaChapter

class MangaChapterGetResolver : DefaultGetResolver<MangaChapter>() {

    companion object {
        val INSTANCE = MangaChapterGetResolver()
    }

    private val mangaGetResolver = MangaGetResolver()

    private val chapterGetResolver = ChapterGetResolver()

    override fun mapFromCursor(cursor: Cursor): MangaChapter {
        val manga = mangaGetResolver.mapFromCursor(cursor)
        val chapter = chapterGetResolver.mapFromCursor(cursor)
        manga.id = chapter.manga_id
        manga.url = cursor.getString(cursor.getColumnIndex("mangaUrl"))

        return MangaChapter(manga, chapter)
    }

}
