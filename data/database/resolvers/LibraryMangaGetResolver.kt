package com.bigberry.comicvn.data.database.resolvers

import android.database.Cursor
import com.bigberry.comicvn.data.database.mappers.MangaGetResolver
import com.bigberry.comicvn.data.database.models.Manga
import com.bigberry.comicvn.data.database.tables.MangaTable

class LibraryMangaGetResolver : MangaGetResolver() {

    companion object {
        val INSTANCE = LibraryMangaGetResolver()
    }

    override fun mapFromCursor(cursor: Cursor): Manga {
        val manga = super.mapFromCursor(cursor)

        val unreadColumn = cursor.getColumnIndex(MangaTable.COL_UNREAD)
        manga.unread = cursor.getInt(unreadColumn)

        val categoryColumn = cursor.getColumnIndex(MangaTable.COL_CATEGORY)
        manga.category = cursor.getInt(categoryColumn)

        return manga
    }

}
