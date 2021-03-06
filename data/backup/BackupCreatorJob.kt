package com.bigberry.comicvn.data.backup

import android.net.Uri
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.bigberry.comicvn.data.preference.PreferencesHelper
import com.bigberry.comicvn.data.preference.getOrDefault
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.io.File

class BackupCreatorJob : Job() {

    override fun onRunJob(params: Params): Result {
        val preferences = Injekt.get<PreferencesHelper>()
        val uri = Uri.fromFile(File(preferences.backupsDirectory().getOrDefault()))
        val flags = BackupCreateService.BACKUP_ALL
        BackupCreateService.makeBackup(context, uri, flags, true)
        return Result.SUCCESS
    }

    companion object {
        const val TAG = "BackupCreator"

        fun setupTask(prefInterval: Int? = null) {
            val preferences = Injekt.get<PreferencesHelper>()
            val interval = prefInterval ?: preferences.backupInterval().getOrDefault()
            if (interval > 0) {
                JobRequest.Builder(TAG)
                        .setPeriodic(interval * 60 * 60 * 1000L, 10 * 60 * 1000)
                        .setPersisted(true)
                        .setUpdateCurrent(true)
                        .build()
                        .schedule()
            }
        }

        fun cancelTask() {
            JobManager.instance().cancelAllForTag(TAG)
        }
    }
}