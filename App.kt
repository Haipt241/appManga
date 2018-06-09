package com.bigberry.comicvn

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.net.NetworkInfo
import android.support.multidex.MultiDex
import android.support.v7.app.AppCompatActivity
import android.telephony.TelephonyManager
import com.evernote.android.job.JobManager
import com.bigberry.comicvn.data.backup.BackupCreatorJob
import com.bigberry.comicvn.data.library.LibraryUpdateJob
import com.bigberry.comicvn.data.preference.PreferencesHelper
import com.bigberry.comicvn.data.updater.UpdateCheckerJob
import com.bigberry.comicvn.network.NetworkUtil
import com.bigberry.comicvn.util.*
import org.acra.ACRA
import org.acra.annotation.ReportsCrashes
import timber.log.Timber
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.InjektScope
import uy.kohesive.injekt.registry.default.DefaultRegistrar
import com.crashlytics.android.Crashlytics
import com.github.pwittchen.reactivenetwork.library.Connectivity
import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork
import io.fabric.sdk.android.Fabric
import org.json.JSONObject
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import uy.kohesive.injekt.injectLazy

@ReportsCrashes(
        formUri = "https://truyentranhviet.org:8888/crash_report",
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.POST,
        buildConfigClass = com.bigberry.comicvn.BuildConfig::class,
        excludeMatchingSharedPreferencesKeys = arrayOf(".*username.*", ".*password.*", ".*token.*")
)
open class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Injekt = InjektScope(DefaultRegistrar())
        Injekt.importModule(AppModule(this))

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        setupAcra()
        setupJobManager()
        setupCrashlytics()

        subscriptions = CompositeSubscription()
        listenNetworkChanges()

        LocaleHelper.updateConfiguration(this, resources.configuration)
    }

    override fun onTerminate() {
        super.onTerminate()
        subscriptions.unsubscribe()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        if (BuildConfig.DEBUG) {
            MultiDex.install(this)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.updateConfiguration(this, newConfig, true)
    }

    protected open fun setupCrashlytics() {
        Fabric.with(this, Crashlytics())
        logUser()
    }

    private fun logUser() {
        // TODO: Use the current user's information
        // You can call any combination of these three methods

        if (hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            Crashlytics.setUserIdentifier(telephonyManager.getDeviceId())
            Crashlytics.setString("phone", telephonyManager.getLine1Number())
        }

        if (hasPermission(Manifest.permission.GET_ACCOUNTS)) {
            Crashlytics.setUserEmail(UserEmailFetcher.getEmail(this))
        }

    }

    protected open fun setupAcra() {
        ACRA.init(this)
    }

    protected open fun setupJobManager() {
        JobManager.create(this).addJobCreator { tag ->
            when (tag) {
                LibraryUpdateJob.TAG -> LibraryUpdateJob()
                UpdateCheckerJob.TAG -> UpdateCheckerJob()
                BackupCreatorJob.TAG -> BackupCreatorJob()
                else -> null
            }
        }
    }

    protected val preferences: PreferencesHelper by injectLazy()

    /**
     * Subscriptions to store while the service is running.
     */
    private lateinit var subscriptions: CompositeSubscription

    /**
     * Listens to network changes.
     *
     * @see onNetworkStateChanged
     */
    private fun listenNetworkChanges() {
        subscriptions += ReactiveNetwork.observeNetworkConnectivity(applicationContext)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ state ->
                    onNetworkStateChanged(state)
                }, { _ -> })
    }

    /**
     * Called when the network state changes.
     *
     * @param connectivity the new network state.
     */
    private fun onNetworkStateChanged(connectivity: Connectivity) {
        when (connectivity.state) {
            NetworkInfo.State.CONNECTED -> {
                val isSendUserInfo = preferences.isSendUserInfo()
                if (!isSendUserInfo)
                    UserUtil.sendUserInfo(this)
            }

            else -> { /* Do nothing */ }
        }
    }
}
