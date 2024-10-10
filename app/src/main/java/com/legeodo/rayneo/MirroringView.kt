package com.rayneo.arsdk.android.ui.wiget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

/**
 * Mirror View, 소스 매핑
 */
open class MirroringView : View {
    private var mSource: View? = null
    private var isMirroring = false

    private var interval = 35L

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attr: AttributeSet?) : super(context, attr) {}
    constructor(context: Context?, attr: AttributeSet?, defStyle: Int) :
            super(context, attr,defStyle) { }

    fun setSource(source: View) {
        mSource = source
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isMirroring) {
            mSource?.apply {
                val scrollX = this.scrollX
                val scrollY = this.scrollY
                canvas.translate(-scrollX.toFloat(), -scrollY.toFloat())
                draw(canvas)
                canvas.translate(scrollX.toFloat(), scrollY.toFloat())
            }
//            mSource?.draw(canvas)
            postInvalidateDelayed(interval)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isMirroring = false
    }

    fun startMirroring() {
        isMirroring = true
        postInvalidateDelayed(interval)
    }

    fun stopMirroring() {
        isMirroring = false
    }
}