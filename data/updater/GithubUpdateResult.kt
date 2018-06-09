package com.bigberry.comicvn.data.updater

sealed class GithubUpdateResult {

    class NewUpdate(val release: GithubRelease): GithubUpdateResult()
    class NoNewUpdate(): GithubUpdateResult()
}