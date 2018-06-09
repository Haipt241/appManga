package com.bigberry.comicvn.ui.manga.track

import com.bigberry.comicvn.data.database.models.Track
import com.bigberry.comicvn.data.track.TrackService

data class TrackItem(val track: Track?, val service: TrackService)
