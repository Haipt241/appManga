package com.bigberry.comicvn.data.updater

import com.bigberry.comicvn.BuildConfig
import rx.Observable

class GithubUpdateChecker() {

    private val service: GithubService = GithubService.create()

    /**
     * Returns observable containing release information
     */
    fun checkForUpdate(): Observable<GithubUpdateResult> {
        return service.getLatestVersion().map { release ->
            val newVersion = release.version.replace("[^\\d.]".toRegex(), "")

            // Check if latest version is different from current version
            if (newVersion != BuildConfig.VERSION_NAME) {
                GithubUpdateResult.NewUpdate(release)
            } else {
                GithubUpdateResult.NoNewUpdate()
            }
        }
    }
}