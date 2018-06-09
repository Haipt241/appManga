package com.bigberry.comicvn.ui.base.activity

import com.bigberry.comicvn.ui.base.presenter.BasePresenter
import com.bigberry.comicvn.util.LocaleHelper
import nucleus.view.NucleusAppCompatActivity

abstract class BaseRxActivity<P : BasePresenter<*>> : NucleusAppCompatActivity<P>() {

    init {
        @Suppress("LeakingThis")
        LocaleHelper.updateConfiguration(this)
    }

}
