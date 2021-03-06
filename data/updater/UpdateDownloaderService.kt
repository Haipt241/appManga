package com.bigberry.comicvn.data.updater

import android.app.IntentService
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.bigberry.comicvn.BuildConfig
import com.bigberry.comicvn.network.GET
import com.bigberry.comicvn.network.NetworkHelper
import com.bigberry.comicvn.network.ProgressListener
import com.bigberry.comicvn.network.newCallWithProgress
import com.bigberry.comicvn.util.registerLocalReceiver
import com.bigberry.comicvn.util.saveTo
import com.bigberry.comicvn.util.sendLocalBroadcastSync
import com.bigberry.comicvn.util.unregisterLocalReceiver
import timber.log.Timber
import uy.kohesive.injekt.injectLazy
import java.io.File

class UpdateDownloaderService : IntentService(UpdateDownloaderService::class.java.name) {
    /**
     * Network helper
     */
    private val network: NetworkHelper by injectLazy()

    /**
     * Local [BroadcastReceiver] that runs on UI thread
     */
    private val updaterNotificationReceiver = UpdateDownloaderReceiver(this)


    override fun onCreate() {
        super.onCreate()
        // Register receiver
        registerLocalReceiver(updaterNotificationReceiver, IntentFilter(INTENT_FILTER_NAME))
    }

    override fun onDestroy() {
        // Unregister receiver
        unregisterLocalReceiver(updaterNotificationReceiver)
        super.onDestroy()
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return

        val url = intent.getStringExtra(EXTRA_DOWNLOAD_URL) ?: return
        downloadApk(url)
    }

    /**
     * Called to start downloading apk of new update
     *
     * @param url url location of file
     */
    fun downloadApk(url: String) {
        // Show notification download starting.
        sendInitialBroadcast()

        val progressListener = object : ProgressListener {

            // Progress of the download
            var savedProgress = 0

            // Keep track of the last notification sent to avoid posting too many.
            var lastTick = 0L

            override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
                val progress = (100 * bytesRead / contentLength).toInt()
                val currentTime = System.currentTimeMillis()
                if (progress > savedProgress && currentTime - 200 > lastTick) {
                    savedProgress = progress
                    lastTick = currentTime
                    sendProgressBroadcast(progress)
                }
            }
        }

        try {
            // Download the new update.
            val response = network.client.newCallWithProgress(GET(url), progressListener).execute()

            // File where the apk will be saved.
            val apkFile = File(externalCacheDir, "update.apk")

            if (response.isSuccessful) {
                response.body()!!.source().saveTo(apkFile)
            } else {
                response.close()
                throw Exception("Unsuccessful response")
            }
            sendInstallBroadcast(apkFile.absolutePath)
        } catch (error: Exception) {
            Timber.e(error)
            sendErrorBroadcast(url)
        }
    }

    /**
     * Show notification download starting.
     */
    private fun sendInitialBroadcast() {
        val intent = Intent(INTENT_FILTER_NAME).apply {
            putExtra(UpdateDownloaderReceiver.EXTRA_ACTION, UpdateDownloaderReceiver.NOTIFICATION_UPDATER_INITIAL)
        }
        sendLocalBroadcastSync(intent)
    }

    /**
     * Show notification progress changed
     *
     * @param progress progress of download
     */
    private fun sendProgressBroadcast(progress: Int) {
        val intent = Intent(INTENT_FILTER_NAME).apply {
            putExtra(UpdateDownloaderReceiver.EXTRA_ACTION, UpdateDownloaderReceiver.NOTIFICATION_UPDATER_PROGRESS)
            putExtra(UpdateDownloaderReceiver.EXTRA_PROGRESS, progress)
        }
        sendLocalBroadcastSync(intent)
    }

    /**
     * Show install notification.
     *
     * @param path location of file
     */
    private fun sendInstallBroadcast(path: String){
        val intent = Intent(INTENT_FILTER_NAME).apply {
            putExtra(UpdateDownloaderReceiver.EXTRA_ACTION, UpdateDownloaderReceiver.NOTIFICATION_UPDATER_INSTALL)
            putExtra(UpdateDownloaderReceiver.EXTRA_APK_PATH, path)
        }
        sendLocalBroadcastSync(intent)
    }

    /**
     * Show error notification.
     *
     * @param url url of file
     */
    private fun sendErrorBroadcast(url: String){
        val intent = Intent(INTENT_FILTER_NAME).apply {
            putExtra(UpdateDownloaderReceiver.EXTRA_ACTION, UpdateDownloaderReceiver.NOTIFICATION_UPDATER_ERROR)
            putExtra(UpdateDownloaderReceiver.EXTRA_APK_URL, url)
        }
        sendLocalBroadcastSync(intent)
    }

    companion object {
        /**
         * Name of Local BroadCastReceiver.
         */
        private val INTENT_FILTER_NAME = UpdateDownloaderService::class.java.name

        /**
         * Download url.
         */
        internal const val EXTRA_DOWNLOAD_URL = "${com.bigberry.comicvn.BuildConfig.APPLICATION_ID}.UpdateDownloaderService.DOWNLOAD_URL"

        /**
         * Downloads a new update and let the user install the new version from a notification.
         * @param context the application context.
         * @param url the url to the new update.
         */
        fun downloadUpdate(context: Context, url: String) {
            val intent = Intent(context, UpdateDownloaderService::class.java).apply {
                putExtra(EXTRA_DOWNLOAD_URL, url)
            }
            context.startService(intent)
        }

        /**
         * Returns [PendingIntent] that starts a service which downloads the apk specified in url.
         *
         * @param url the url to the new update.
         * @return [PendingIntent]
         */
        internal fun downloadApkPendingService(context: Context, url: String): PendingIntent {
            val intent = Intent(context, UpdateDownloaderService::class.java).apply {
                putExtra(EXTRA_DOWNLOAD_URL, url)
            }
            return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}


