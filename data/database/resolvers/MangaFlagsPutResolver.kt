package com.bigberry.comicvn.data.database.resolvers

import android.content.ContentValues
import com.pushtorefresh.storio.sqlite.StorIOSQLite
import com.pushtorefresh.storio.sqlite.operations.put.PutResolver
import com.pushtorefresh.storio.sqlite.operations.put.PutResult
import com.pushtorefresh.storio.sqlite.queries.UpdateQuery
import com.bigberry.comicvn.data.database.inTransactionReturn
import com.bigberry.comicvn.data.database.models.Manga
import com.bigberry.comicvn.data.database.tables.MangaTable

class MangaFlagsPutResolver : PutResolver<Manga>() {

    override fun performPut(db: StorIOSQLite, manga: Manga) = db.inTransactionReturn {
        val updateQuery = mapToUpdateQuery(manga)
        val contentValues = mapToContentValues(manga)

        val numberOfRowsUpdated = db.lowLevel().update(updateQuery, contentValues)
        PutResult.newUpdateResult(numberOfRowsUpdated, updateQuery.table())
    }

    fun mapToUpdateQuery(manga: Manga) = UpdateQuery.builder()
            .table(MangaTable.TABLE)
            .where("${MangaTable.COL_ID} = ?")
            .whereArgs(manga.id)
            .build()

    fun mapToContentValues(manga: Manga) = ContentValues(1).apply {
        put(MangaTable.COL_CHAPTER_FLAGS, manga.chapter_flags)
    }

}
