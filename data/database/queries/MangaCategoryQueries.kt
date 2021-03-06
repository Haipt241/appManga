package com.bigberry.comicvn.data.database.queries

import com.pushtorefresh.storio.Queries
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery
import com.bigberry.comicvn.data.database.DbProvider
import com.bigberry.comicvn.data.database.inTransaction
import com.bigberry.comicvn.data.database.models.Manga
import com.bigberry.comicvn.data.database.models.MangaCategory
import com.bigberry.comicvn.data.database.tables.MangaCategoryTable

interface MangaCategoryQueries : DbProvider {

    fun insertMangaCategory(mangaCategory: MangaCategory) = db.put().`object`(mangaCategory).prepare()

    fun insertMangasCategories(mangasCategories: List<MangaCategory>) = db.put().objects(mangasCategories).prepare()

    fun deleteOldMangasCategories(mangas: List<Manga>) = db.delete()
            .byQuery(DeleteQuery.builder()
                    .table(MangaCategoryTable.TABLE)
                    .where("${MangaCategoryTable.COL_MANGA_ID} IN (${Queries.placeholders(mangas.size)})")
                    .whereArgs(*mangas.map { it.id }.toTypedArray())
                    .build())
            .prepare()

    fun setMangaCategories(mangasCategories: List<MangaCategory>, mangas: List<Manga>) {
        db.inTransaction {
            deleteOldMangasCategories(mangas).executeAsBlocking()
            insertMangasCategories(mangasCategories).executeAsBlocking()
        }
    }

}