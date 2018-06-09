package com.bigberry.comicvn.ui.base.controller

import android.os.Bundle
import com.bigberry.comicvn.ui.base.presenter.NucleusConductorDelegate
import com.bigberry.comicvn.ui.base.presenter.NucleusConductorLifecycleListener
import nucleus.factory.PresenterFactory
import nucleus.presenter.Presenter

@Suppress("LeakingThis")
abstract class NucleusController<P : Presenter<*>>(val bundle: Bundle? = null) : RxController(),
        PresenterFactory<P> {

    private val delegate = NucleusConductorDelegate(this)

    val presenter: P
        get() = delegate.presenter

    init {
        addLifecycleListener(NucleusConductorLifecycleListener(delegate))
    }
}
