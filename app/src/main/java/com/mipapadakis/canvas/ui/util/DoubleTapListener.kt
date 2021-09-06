package com.mipapadakis.canvas.ui.util

import android.os.SystemClock
import android.view.View

private val DEFAULT_DOUBLE_TAP_TIMEOUT = CanvasTouchListener.DOUBLE_TAP_TIMEOUT

abstract class DoubleTapListener : View.OnClickListener {
    private var doubleTapTimeout = DEFAULT_DOUBLE_TAP_TIMEOUT
    private var lastClickTimestamp = 0L

    override fun onClick(v: View?) {
        onSingleTap()
        if (SystemClock.elapsedRealtime() - lastClickTimestamp < doubleTapTimeout) onDoubleTap()
        lastClickTimestamp = SystemClock.elapsedRealtime()
    }

    abstract fun onSingleTap()
    abstract fun onDoubleTap()
}