package com.bigberry.comicvn.ui.reader

import com.bigberry.comicvn.data.database.models.Chapter
import com.bigberry.comicvn.data.database.models.Manga

class ReaderEvent(val manga: Manga, val chapter: Chapter)
