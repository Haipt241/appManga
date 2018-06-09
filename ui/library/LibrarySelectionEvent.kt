package com.bigberry.comicvn.ui.library

import com.bigberry.comicvn.data.database.models.Manga

sealed class LibrarySelectionEvent {

    class Selected(val manga: Manga) : LibrarySelectionEvent()
    class Unselected(val manga: Manga) : LibrarySelectionEvent()
    class Cleared() : LibrarySelectionEvent()
}