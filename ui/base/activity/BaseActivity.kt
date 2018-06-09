package com.bigberry.comicvn.ui.base.activity

import android.support.v7.app.AppCompatActivity
import com.bigberry.comicvn.util.LocaleHelper

abstract class BaseActivity : AppCompatActivity() {

    init {
        @Suppress("LeakingThis")
        LocaleHelper.updateConfiguration(this)
    }

}
