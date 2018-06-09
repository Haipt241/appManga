package com.bigberry.comicvn

import android.app.Application
import com.google.gson.Gson
import com.bigberry.comicvn.data.cache.ChapterCache
import com.bigberry.comicvn.data.cache.CoverCache
import com.bigberry.comicvn.data.database.DatabaseHelper
import com.bigberry.comicvn.data.download.DownloadManager
import com.bigberry.comicvn.data.preference.PreferencesHelper
import com.bigberry.comicvn.data.track.TrackManager
import com.bigberry.comicvn.network.NetworkHelper
import com.bigberry.comicvn.source.SourceManager
import uy.kohesive.injekt.api.InjektModule
import uy.kohesive.injekt.api.InjektRegistrar
import uy.kohesive.injekt.api.addSingletonFactory

class AppModule(val app: Application) : InjektModule {

    override fun InjektRegistrar.registerInjectables() {

            addSingletonFactory { PreferencesHelper(app) }

            addSingletonFactory { DatabaseHelper(app) }

            addSingletonFactory { ChapterCache(app) }

            addSingletonFactory { CoverCache(app) }

            addSingletonFactory { NetworkHelper(app) }

            addSingletonFactory { SourceManager(app) }

            addSingletonFactory { DownloadManager(app) }

            addSingletonFactory { TrackManager(app) }

            addSingletonFactory { Gson() }

    }

}