package com.bigberry.comicvn.ui.main

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.graphics.drawable.DrawerArrowDrawable
import android.view.ViewGroup
import com.bluelinelabs.conductor.*
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bigberry.comicvn.Migrations
import com.bigberry.comicvn.R
import com.bigberry.comicvn.data.preference.PreferencesHelper
import com.bigberry.comicvn.data.track.TrackManager
import com.bigberry.comicvn.network.*
import com.bigberry.comicvn.ui.base.activity.BaseActivity
import com.bigberry.comicvn.ui.base.controller.DialogController
import com.bigberry.comicvn.ui.base.controller.NoToolbarElevationController
import com.bigberry.comicvn.ui.base.controller.SecondaryDrawerController
import com.bigberry.comicvn.ui.base.controller.TabbedController
import com.bigberry.comicvn.ui.catalogue.CatalogueController
import com.bigberry.comicvn.ui.download.DownloadController
import com.bigberry.comicvn.ui.latest_updates.LatestUpdatesController
import com.bigberry.comicvn.ui.library.LibraryController
import com.bigberry.comicvn.ui.manga.MangaController
import com.bigberry.comicvn.ui.recent_updates.RecentChaptersController
import com.bigberry.comicvn.ui.recently_read.RecentlyReadController
import com.bigberry.comicvn.ui.setting.SettingsMainController
import com.bigberry.comicvn.util.UserUtil
import com.bigberry.comicvn.util.asJsoup
import kotlinx.android.synthetic.main.main_activity.*
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Response
import rx.Observable
import uy.kohesive.injekt.injectLazy
import java.net.URI

class MainActivity : BaseActivity(), AccountLoginSource {

    private val trackManager: TrackManager by injectLazy()

    private lateinit var router: Router

    val preferences: PreferencesHelper by injectLazy()

    private var drawerArrow: DrawerArrowDrawable? = null

    private var secondaryDrawer: ViewGroup? = null

    private val startScreenId by lazy {
        val screen = preferences.startScreen()
        when (screen) {
            1 -> R.id.nav_drawer_library
            2 -> R.id.nav_drawer_recently_read
            3 -> R.id.nav_drawer_recent_updates
            4 -> R.id.nav_drawer_catalogues
            else -> R.id.nav_drawer_catalogues
        }
    }

    lateinit var tabAnimator: TabsAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(when (preferences.theme()) {
            2 -> R.style.Theme_PTH_Dark
            3 -> R.style.Theme_PTH_Amoled
            else -> R.style.Theme_PTH
        })
        super.onCreate(savedInstanceState)

        // Do not let the launcher create a new activity http://stackoverflow.com/questions/16283079
        if (!isTaskRoot) {
            finish()
            return
        }

        setContentView(R.layout.main_activity)

        setSupportActionBar(toolbar)

        drawerArrow = DrawerArrowDrawable(this)
        drawerArrow?.color = Color.WHITE
        toolbar.navigationIcon = drawerArrow

        tabAnimator = TabsAnimator(tabs)

        // Set behavior of Navigation drawer
        nav_view.setNavigationItemSelectedListener { item ->
            val id = item.itemId

            val currentRoot = router.backstack.firstOrNull()
            if (currentRoot?.tag()?.toIntOrNull() != id) {
                when (id) {
                    R.id.nav_drawer_library -> setRoot(LibraryController(), id)
                    R.id.nav_drawer_recent_updates -> setRoot(RecentChaptersController(), id)
                    R.id.nav_drawer_recently_read -> setRoot(RecentlyReadController(), id)
                    R.id.nav_drawer_catalogues -> setRoot(CatalogueController(), id)
                    R.id.nav_drawer_latest_updates -> setRoot(LatestUpdatesController(), id)
                    R.id.nav_drawer_downloads -> {
                        router.pushController(RouterTransaction.with(DownloadController())
                                .pushChangeHandler(FadeChangeHandler())
                                .popChangeHandler(FadeChangeHandler()))
                    }
                    R.id.nav_drawer_settings ->
                        router.pushController(RouterTransaction.with(SettingsMainController())
                                .pushChangeHandler(FadeChangeHandler())
                                .popChangeHandler(FadeChangeHandler()))
                }
            }
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        val headerView = nav_view.getHeaderView(0)
        val btn_account = headerView.findViewById(R.id.btn_account)
        val btn_setting = headerView.findViewById(R.id.btn_setting)

        btn_account.setOnClickListener {
//            toast(com.bigberry.comicvn.R.string.account_login_coming_soon)
            val dialog = AccountLoginDialog()
//            dialog.targetController = this@MainActivity
            dialog.showDialog(router)
        }

        btn_setting.setOnClickListener {
            router.pushController(RouterTransaction.with(SettingsMainController())
                    .pushChangeHandler(FadeChangeHandler())
                    .popChangeHandler(FadeChangeHandler()))
        }

        val container = findViewById(R.id.controller_container) as ViewGroup

        router = Conductor.attachRouter(this, container, savedInstanceState)
        if (!router.hasRootController()) {
            // Set start screen
            if (!handleIntentAction(intent)) {
                setSelectedDrawerItem(startScreenId)
            }
        }

        toolbar.setNavigationOnClickListener {
            if (router.backstackSize == 1) {
                drawer.openDrawer(GravityCompat.START)
            } else {
                onBackPressed()
            }
        }

        router.addChangeListener(object : ControllerChangeHandler.ControllerChangeListener {
            override fun onChangeStarted(to: Controller?, from: Controller?, isPush: Boolean,
                                         container: ViewGroup, handler: ControllerChangeHandler) {

                syncActivityViewWithController(to, from)
            }

            override fun onChangeCompleted(to: Controller?, from: Controller?, isPush: Boolean,
                                           container: ViewGroup, handler: ControllerChangeHandler) {

            }

        })

        syncActivityViewWithController(router.backstack.lastOrNull()?.controller())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE/*, Manifest.permission.READ_PHONE_STATE, Manifest.permission.GET_ACCOUNTS*/), 500)
        }

        if (savedInstanceState == null) {
            // Show changelog if needed
            if (Migrations.upgrade(preferences)) {
                ChangelogDialogController().showDialog(router)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 500) {
            var grantedPhoneState = false
            var grantedEmail = false
            val indexPhoneState = permissions.indexOf(Manifest.permission.READ_PHONE_STATE)
            if (indexPhoneState > -1) {
                if (grantResults[indexPhoneState] == PackageManager.PERMISSION_GRANTED) {
                    grantedPhoneState = true
                }
            }

            val indexEmail = permissions.indexOf(Manifest.permission.GET_ACCOUNTS)
            if (indexEmail > -1) {
                if (grantResults[indexEmail] == PackageManager.PERMISSION_GRANTED) {
                    grantedEmail = true
                }
            }

            if (grantedPhoneState || grantedEmail) {
                if (NetworkUtil.isOnline(this)) {
                    if (!preferences.isSendUserInfo())
                        UserUtil.sendUserInfo(this)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        if (!handleIntentAction(intent)) {
            super.onNewIntent(intent)
        }
    }

    private fun handleIntentAction(intent: Intent): Boolean {
        when (intent.action) {
            SHORTCUT_LIBRARY -> setSelectedDrawerItem(R.id.nav_drawer_library)
            SHORTCUT_RECENTLY_UPDATED -> setSelectedDrawerItem(R.id.nav_drawer_recent_updates)
            SHORTCUT_RECENTLY_READ -> setSelectedDrawerItem(R.id.nav_drawer_recently_read)
            SHORTCUT_CATALOGUES -> setSelectedDrawerItem(R.id.nav_drawer_catalogues)
            SHORTCUT_MANGA -> router.setRoot(RouterTransaction.with(MangaController(intent.extras)))
            SHORTCUT_DOWNLOADS -> {
                if (router.backstack.none { it.controller() is DownloadController }) {
                    setSelectedDrawerItem(R.id.nav_drawer_downloads)
                }
            }
            else -> return false
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        nav_view?.setNavigationItemSelectedListener(null)
        toolbar?.setNavigationOnClickListener(null)
    }

    override fun onBackPressed() {
        val backstackSize = router.backstackSize
        if (drawer.isDrawerOpen(GravityCompat.START) || drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawers()
        } else if (backstackSize == 1 && router.getControllerWithTag("$startScreenId") == null) {
            setSelectedDrawerItem(startScreenId)
        } else if (backstackSize == 1 || !router.handleBack()) {
            super.onBackPressed()
        }
    }

    fun setSelectedDrawerItem(itemId: Int) {
        if (!isFinishing) {
            nav_view.setCheckedItem(itemId)
            nav_view.menu.performIdentifierAction(itemId, 0)
        }
    }

    private fun setRoot(controller: Controller, id: Int) {
        router.setRoot(RouterTransaction.with(controller)
                .popChangeHandler(FadeChangeHandler())
                .pushChangeHandler(FadeChangeHandler())
                .tag(id.toString()))
    }

    private fun syncActivityViewWithController(to: Controller?, from: Controller? = null) {
        if (from is DialogController || to is DialogController) {
            return
        }

        val showHamburger = router.backstackSize == 1
        if (showHamburger) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }

        ObjectAnimator.ofFloat(drawerArrow, "progress", if (showHamburger) 0f else 1f).start()

        if (from is TabbedController) {
            from.cleanupTabs(tabs)
        }
        if (to is TabbedController) {
            tabAnimator.expand()
            to.configureTabs(tabs)
        } else {
            tabAnimator.collapse()
            tabs.setupWithViewPager(null)
        }

        if (from is SecondaryDrawerController) {
            if (secondaryDrawer != null) {
                from.cleanupSecondaryDrawer(drawer)
                drawer.removeView(secondaryDrawer)
                secondaryDrawer = null
            }
        }
        if (to is SecondaryDrawerController) {
            secondaryDrawer = to.createSecondaryDrawer(drawer)?.also { drawer.addView(it) }
        }

        if (to is NoToolbarElevationController) {
            appbar.disableElevation()
        } else {
            appbar.enableElevation()
        }
    }

    companion object {
        // Shortcut actions
        const val SHORTCUT_LIBRARY = "com.bigberry.comicvn.SHOW_LIBRARY"
        const val SHORTCUT_RECENTLY_UPDATED = "com.bigberry.comicvn.SHOW_RECENTLY_UPDATED"
        const val SHORTCUT_RECENTLY_READ = "com.bigberry.comicvn.SHOW_RECENTLY_READ"
        const val SHORTCUT_CATALOGUES = "com.bigberry.comicvn.SHOW_CATALOGUES"
        const val SHORTCUT_DOWNLOADS = "com.bigberry.comicvn.SHOW_DOWNLOADS"
        const val SHORTCUT_MANGA = "com.bigberry.comicvn.SHOW_MANGA"
    }

    /**
     * Network service.
     */
    protected val network: NetworkHelper by injectLazy()

    /**
     * Default network client for doing requests.
     */
    open val client: OkHttpClient
        get() = network.client
    /**
     * Implement AccountLoginSource
     */
    override fun login(username: String, password: String) =
            client.newCall(GET("/forums/index.php?app=core&module=global&section=login"))
                    .asObservable()
                    .flatMap { doLogin(it, username, password) }
                    .map { isAuthenticationSuccessful(it) }

    private fun doLogin(response: Response, username: String, password: String): Observable<Response> {
        val doc = response.asJsoup()
        val form = doc.select("#login").first()
        val url = form.attr("action")
        val authKey = form.select("input[name=auth_key]").first()

        val payload = FormBody.Builder().apply {
            add(authKey.attr("name"), authKey.attr("value"))
            add("ips_username", username)
            add("ips_password", password)
            add("invisible", "1")
            add("rememberMe", "1")
        }.build()

        return client.newCall(POST(url, body = payload)).asObservable()
    }

    override fun isAuthenticationSuccessful(response: Response) =
            response.priorResponse() != null && response.priorResponse()!!.code() == 302

    override fun isLogged(): Boolean {
        return network.cookies.get(URI("")).any { it.name() == "pass_hash" }
    }
}