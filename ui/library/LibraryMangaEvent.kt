package com.bigberry.comicvn.ui.library

import com.bigberry.comicvn.data.database.models.Category

class LibraryMangaEvent(val mangas: Map<Int, List<LibraryItem>>) {

    fun getMangaForCategory(category: Category): List<LibraryItem>? {
        return mangas[category.id]
    }
}
