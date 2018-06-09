package com.bigberry.comicvn.data.database.resolvers

import android.content.ContentValues
import com.pushtorefresh.storio.sqlite.StorIOSQLite
import com.pushtorefresh.storio.sqlite.operations.put.PutResolver
import com.pushtorefresh.storio.sqlite.operations.put.PutResult
import com.pushtorefresh.storio.sqlite.queries.UpdateQuery
import com.bigberry.comicvn.data.database.inTransactionReturn
import com.bigberry.comicvn.data.database.models.Chapter
import com.bigberry.comicvn.data.database.tables.ChapterTable

class ChapterSourceOrderPutResolver : PutResolver<Chapter>() {

    override fun performPut(db: StorIOSQLite, chapter: Chapter) = db.inTransactionReturn {
        val updateQuery = mapToUpdateQuery(chapter)
        val contentValues = mapToContentValues(chapter)

        val numberOfRowsUpdated = db.lowLevel().update(updateQuery, contentValues)
        PutResult.newUpdateResult(numberOfRowsUpdated, updateQuery.table())
    }

    fun mapToUpdateQuery(chapter: Chapter) = UpdateQuery.builder()
            .table(ChapterTable.TABLE)
            .where("${ChapterTable.COL_URL} = ? AND ${ChapterTable.COL_MANGA_ID} = ?")
            .whereArgs(chapter.url, chapter.manga_id)
            .build()

    fun mapToContentValues(chapter: Chapter) = ContentValues(1).apply {
        put(ChapterTable.COL_SOURCE_ORDER, chapter.source_order)
    }

}