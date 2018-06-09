package com.bigberry.comicvn.data.database.tables

object MangaTable {

    const val TABLE = "mangas"

    const val COL_ID = "_id"

    /**
     * MangaId from Manga in server
     */
    const val COL_MANGA_ID_SERVER = "mangaid"

    const val COL_SOURCE = "source"

    const val COL_URL = "url"

    const val COL_ARTIST = "artist"

    const val COL_AUTHOR = "author"

    const val COL_DESCRIPTION = "description"

    const val COL_GENRE = "genre"

    const val COL_TITLE = "title"

    const val COL_STATUS = "status"

    const val COL_THUMBNAIL_URL = "thumbnail_url"

    const val COL_FAVORITE = "favorite"

    const val COL_LAST_UPDATE = "last_update"

    const val COL_INITIALIZED = "initialized"

    const val COL_VIEWER = "viewer"

    const val COL_CHAPTER_FLAGS = "chapter_flags"

    const val COL_UNREAD = "unread"

    const val COL_CATEGORY = "category"

    val createTableQuery: String
        get() = """CREATE TABLE $TABLE(
            $COL_ID INTEGER NOT NULL PRIMARY KEY,
            $COL_MANGA_ID_SERVER INTEGER NOT NULL,
            $COL_SOURCE INTEGER NOT NULL,
            $COL_URL TEXT NOT NULL,
            $COL_ARTIST TEXT,
            $COL_AUTHOR TEXT,
            $COL_DESCRIPTION TEXT,
            $COL_GENRE TEXT,
            $COL_TITLE TEXT NOT NULL,
            $COL_STATUS INTEGER NOT NULL,
            $COL_THUMBNAIL_URL TEXT,
            $COL_FAVORITE INTEGER NOT NULL,
            $COL_LAST_UPDATE LONG,
            $COL_INITIALIZED BOOLEAN NOT NULL,
            $COL_VIEWER INTEGER NOT NULL,
            $COL_CHAPTER_FLAGS INTEGER NOT NULL
            )"""

    val createUrlIndexQuery: String
        get() = "CREATE INDEX ${TABLE}_${COL_URL}_index ON $TABLE($COL_URL)"

    val createFavoriteIndexQuery: String
        get() = "CREATE INDEX ${TABLE}_${COL_FAVORITE}_index ON $TABLE($COL_FAVORITE)"
}
