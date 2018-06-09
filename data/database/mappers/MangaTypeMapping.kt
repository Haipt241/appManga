package com.bigberry.comicvn.data.database.mappers

import android.content.ContentValues
import android.database.Cursor
import com.pushtorefresh.storio.sqlite.SQLiteTypeMapping
import com.pushtorefresh.storio.sqlite.operations.delete.DefaultDeleteResolver
import com.pushtorefresh.storio.sqlite.operations.get.DefaultGetResolver
import com.pushtorefresh.storio.sqlite.operations.put.DefaultPutResolver
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio.sqlite.queries.InsertQuery
import com.pushtorefresh.storio.sqlite.queries.UpdateQuery
import com.bigberry.comicvn.data.database.models.Manga
import com.bigberry.comicvn.data.database.models.MangaImpl
import com.bigberry.comicvn.data.database.tables.MangaTable.COL_MANGA_ID_SERVER
import com.bigberry.comicvn.data.database.tables.MangaTable.COL_ARTIST
import com.bigberry.comicvn.data.database.tables.MangaTable.COL_AUTHOR
import com.bigberry.comicvn.data.database.tables.MangaTable.COL_CHAPTER_FLAGS
import com.bigberry.comicvn.data.database.tables.MangaTable.COL_DESCRIPTION
import com.bigberry.comicvn.data.database.tables.MangaTable.COL_FAVORITE
import com.bigberry.comicvn.data.database.tables.MangaTable.COL_GENRE
import com.bigberry.comicvn.data.database.tables.MangaTable.COL_ID
import com.bigberry.comicvn.data.database.tables.MangaTable.COL_INITIALIZED
import com.bigberry.comicvn.data.database.tables.MangaTable.COL_LAST_UPDATE
import com.bigberry.comicvn.data.database.tables.MangaTable.COL_SOURCE
import com.bigberry.comicvn.data.database.tables.MangaTable.COL_STATUS
import com.bigberry.comicvn.data.database.tables.MangaTable.COL_THUMBNAIL_URL
import com.bigberry.comicvn.data.database.tables.MangaTable.COL_TITLE
import com.bigberry.comicvn.data.database.tables.MangaTable.COL_URL
import com.bigberry.comicvn.data.database.tables.MangaTable.COL_VIEWER
import com.bigberry.comicvn.data.database.tables.MangaTable.TABLE

class MangaTypeMapping : SQLiteTypeMapping<Manga>(
        MangaPutResolver(),
        MangaGetResolver(),
        MangaDeleteResolver()
)

class MangaPutResolver : DefaultPutResolver<Manga>() {

    override fun mapToInsertQuery(obj: Manga) = InsertQuery.builder()
            .table(TABLE)
            .build()

    override fun mapToUpdateQuery(obj: Manga) = UpdateQuery.builder()
            .table(TABLE)
            .where("$COL_ID = ?")
            .whereArgs(obj.id)
            .build()

    override fun mapToContentValues(obj: Manga) = ContentValues(16).apply {
        put(COL_ID, obj.id)
        put(COL_MANGA_ID_SERVER, obj.mangaId)
        put(COL_SOURCE, obj.source)
        put(COL_URL, obj.url)
        put(COL_ARTIST, obj.artist)
        put(COL_AUTHOR, obj.author)
        put(COL_DESCRIPTION, obj.description)
        put(COL_GENRE, obj.genre)
        put(COL_TITLE, obj.title)
        put(COL_STATUS, obj.status)
        put(COL_THUMBNAIL_URL, obj.thumbnail_url)
        put(COL_FAVORITE, obj.favorite)
        put(COL_LAST_UPDATE, obj.last_update)
        put(COL_INITIALIZED, obj.initialized)
        put(COL_VIEWER, obj.viewer)
        put(COL_CHAPTER_FLAGS, obj.chapter_flags)
    }
}

open class MangaGetResolver : DefaultGetResolver<Manga>() {

    override fun mapFromCursor(cursor: Cursor): Manga = MangaImpl().apply {
        id = cursor.getLong(cursor.getColumnIndex(COL_ID))
        mangaId = cursor.getLong(cursor.getColumnIndex(COL_MANGA_ID_SERVER))
        source = cursor.getLong(cursor.getColumnIndex(COL_SOURCE))
        url = cursor.getString(cursor.getColumnIndex(COL_URL))
        artist = cursor.getString(cursor.getColumnIndex(COL_ARTIST))
        author = cursor.getString(cursor.getColumnIndex(COL_AUTHOR))
        description = cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION))
        genre = cursor.getString(cursor.getColumnIndex(COL_GENRE))
        title = cursor.getString(cursor.getColumnIndex(COL_TITLE))
        status = cursor.getInt(cursor.getColumnIndex(COL_STATUS))
        thumbnail_url = cursor.getString(cursor.getColumnIndex(COL_THUMBNAIL_URL))
        favorite = cursor.getInt(cursor.getColumnIndex(COL_FAVORITE)) == 1
        last_update = cursor.getLong(cursor.getColumnIndex(COL_LAST_UPDATE))
        initialized = cursor.getInt(cursor.getColumnIndex(COL_INITIALIZED)) == 1
        viewer = cursor.getInt(cursor.getColumnIndex(COL_VIEWER))
        chapter_flags = cursor.getInt(cursor.getColumnIndex(COL_CHAPTER_FLAGS))
    }
}

class MangaDeleteResolver : DefaultDeleteResolver<Manga>() {

    override fun mapToDeleteQuery(obj: Manga) = DeleteQuery.builder()
            .table(TABLE)
            .where("$COL_ID = ?")
            .whereArgs(obj.id)
            .build()
}