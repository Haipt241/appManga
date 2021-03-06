package com.bigberry.comicvn.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView
import com.bigberry.comicvn.R
import java.util.*


class PTSansTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
        TextView(context, attrs) {

    companion object {
        const val PTSANS_NARROW = 0
        const val PTSANS_NARROW_BOLD = 1

        // Map where typefaces are cached
        private val typefaces = HashMap<Int, Typeface>(2)
    }

    init {
        if (attrs != null) {
            val values = context.obtainStyledAttributes(attrs, R.styleable.PTSansTextView)

            val typeface = values.getInt(R.styleable.PTSansTextView_typeface, 0)

            setTypeface(typefaces.getOrPut(typeface) {
                Typeface.createFromAsset(context.assets, when (typeface) {
                    PTSANS_NARROW -> "fonts/PTSans-Narrow.ttf"
                    PTSANS_NARROW_BOLD -> "fonts/PTSans-NarrowBold.ttf"
                    else -> throw IllegalArgumentException("Font not found " + typeface)
                })
            })

            values.recycle()
        }
    }

    override fun draw(canvas: Canvas) {
        // Draw two times for a more visible shadow around the text
        super.draw(canvas)
        super.draw(canvas)
    }

}
