package com.bigberry.comicvn.data.database.queries

import com.pushtorefresh.storio.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio.sqlite.queries.Query
import com.bigberry.comicvn.data.database.DbProvider
import com.bigberry.comicvn.data.database.models.Manga
import com.bigberry.comicvn.data.database.models.Track
import com.bigberry.comicvn.data.database.tables.TrackTable
import com.bigberry.comicvn.data.track.TrackService

interface TrackQueries : DbProvider {

    fun getTracks(manga: Manga) = db.get()
            .listOfObjects(Track::class.java)
            .withQuery(Query.builder()
                    .table(TrackTable.TABLE)
                    .where("${TrackTable.COL_MANGA_ID} = ?")
                    .whereArgs(manga.id)
                    .build())
            .prepare()

    fun insertTrack(track: Track) = db.put().`object`(track).prepare()

    fun insertTracks(tracks: List<Track>) = db.put().objects(tracks).prepare()

    fun deleteTrackForManga(manga: Manga, sync: TrackService) = db.delete()
            .byQuery(DeleteQuery.builder()
                    .table(TrackTable.TABLE)
                    .where("${TrackTable.COL_MANGA_ID} = ? AND ${TrackTable.COL_SYNC_ID} = ?")
                    .whereArgs(manga.id, sync.id)
                    .build())
            .prepare()

}