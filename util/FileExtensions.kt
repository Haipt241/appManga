package com.bigberry.comicvn.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import com.bigberry.comicvn.BuildConfig
import java.io.File

/**
 * Returns the uri of a file
 *
 * @param context context of application
 */
fun File.getUriCompat(context: Context): Uri {
    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        FileProvider.getUriForFile(context, com.bigberry.comicvn.BuildConfig.APPLICATION_ID + ".provider", this)
    else Uri.fromFile(this)
    return uri
}

