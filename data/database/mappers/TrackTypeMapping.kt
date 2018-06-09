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
import com.bigberry.comicvn.data.database.models.Track
import com.bigberry.comicvn.data.database.models.TrackImpl
import com.bigberry.comicvn.data.database.tables.TrackTable.COL_ID
import com.bigberry.comicvn.data.database.tables.TrackTable.COL_LAST_CHAPTER_READ
import com.bigberry.comicvn.data.database.tables.TrackTable.COL_MANGA_ID
import com.bigberry.comicvn.data.database.tables.TrackTable.COL_REMOTE_ID
import com.bigberry.comicvn.data.database.tables.TrackTable.COL_SCORE
import com.bigberry.comicvn.data.database.tables.TrackTable.COL_STATUS
import com.bigberry.comicvn.data.database.tables.TrackTable.COL_SYNC_ID
import com.bigberry.comicvn.data.database.tables.TrackTable.COL_TITLE
import com.bigberry.comicvn.data.database.tables.TrackTable.COL_TOTAL_CHAPTERS
import com.bigberry.comicvn.data.database.tables.TrackTable.TABLE

class TrackTypeMapping : SQLiteTypeMapping<Track>(
        TrackPutResolver(),
        TrackGetResolver(),
        TrackDeleteResolver()
)

class TrackPutResolver : DefaultPutResolver<Track>() {

    override fun mapToInsertQuery(obj: Track) = InsertQuery.builder()
            .table(TABLE)
            .build()

    override fun mapToUpdateQuery(obj: Track) = UpdateQuery.builder()
            .table(TABLE)
            .where("$COL_ID = ?")
            .whereArgs(obj.id)
            .build()

    override fun mapToContentValues(obj: Track) = ContentValues(9).apply {
        put(COL_ID, obj.id)
        put(COL_MANGA_ID, obj.manga_id)
        put(COL_SYNC_ID, obj.sync_id)
        put(COL_REMOTE_ID, obj.remote_id)
        put(COL_TITLE, obj.title)
        put(COL_LAST_CHAPTER_READ, obj.last_chapter_read)
        put(COL_TOTAL_CHAPTERS, obj.total_chapters)
        put(COL_STATUS, obj.status)
        put(COL_SCORE, obj.score)
    }
}

class TrackGetResolver : DefaultGetResolver<Track>() {

    override fun mapFromCursor(cursor: Cursor): Track = TrackImpl().apply {
        id = cursor.getLong(cursor.getColumnIndex(COL_ID))
        manga_id = cursor.getLong(cursor.getColumnIndex(COL_MANGA_ID))
        sync_id = cursor.getInt(cursor.getColumnIndex(COL_SYNC_ID))
        remote_id = cursor.getInt(cursor.getColumnIndex(COL_REMOTE_ID))
        title = cursor.getString(cursor.getColumnIndex(COL_TITLE))
        last_chapter_read = cursor.getInt(cursor.getColumnIndex(COL_LAST_CHAPTER_READ))
        total_chapters = cursor.getInt(cursor.getColumnIndex(COL_TOTAL_CHAPTERS))
        status = cursor.getInt(cursor.getColumnIndex(COL_STATUS))
        score = cursor.getFloat(cursor.getColumnIndex(COL_SCORE))
    }
}

class TrackDeleteResolver : DefaultDeleteResolver<Track>() {

    override fun mapToDeleteQuery(obj: Track) = DeleteQuery.builder()
            .table(TABLE)
            .where("$COL_ID = ?")
            .whereArgs(obj.id)
            .build()
}
