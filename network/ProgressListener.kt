package com.bigberry.comicvn.network

interface ProgressListener {
    fun update(bytesRead: Long, contentLength: Long, done: Boolean)
}