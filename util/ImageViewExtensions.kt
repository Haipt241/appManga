package com.bigberry.comicvn.util

import android.support.annotation.DrawableRes
import android.support.graphics.drawable.VectorDrawableCompat
import android.widget.ImageView

/**
 * Set a vector on a [ImageView].
 *
 * @param drawable id of drawable resource
 */
fun ImageView.setVectorCompat(@DrawableRes drawable: Int, tint: Int? = null) {
    val vector = VectorDrawableCompat.create(resources, drawable, context.theme)
    if (tint != null) {
        vector?.mutate()
        vector?.setTint(tint)
    }
    setImageDrawable(vector)
}