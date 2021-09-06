package com.mipapadakis.canvas.ui.canvas.toolbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.mipapadakis.canvas.CanvasPreferences
import com.mipapadakis.canvas.ui.util.DeviceDimensions
import com.mipapadakis.canvas.ui.canvas.CanvasViews

@SuppressLint("ClickableViewAccessibility")
class ToolbarMove(canvasViews: CanvasViews) {
    private val devicePixelHeight = DeviceDimensions.getHeight(canvasViews.toolbarMoveImageView.context)
    private val context = canvasViews.toolbarMoveImageView.context
    private var location = IntArray(2)
    private var outRect = Rect()

    init {
        var timer: CountDownTimer? = null
        var longPressed = false
        var dX = 0f
        var dY = 0f
        canvasViews.toolbarMoveImageView.setOnTouchListener { v, event -> //TODO fix bounds
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    longPressed = false
                    dX = -1f
                    dY = -1f
                    val longPressDelay = ViewConfiguration.getLongPressTimeout().toLong()
                    timer = object : CountDownTimer(longPressDelay, longPressDelay) {
                        override fun onTick(millisUntilFinished: Long) {}
                        override fun onFinish() {
                            longPressed = true
                            vibrate(70)
                            dX = canvasViews.toolbarOuterCardView.x - event.rawX
                            dY = canvasViews.toolbarOuterCardView.y - event.rawY
                            canvasViews.toolbarOuterCardView.alpha = CanvasPreferences.MEDIUM_ALPHA
                        }
                    }
                    timer?.start()
                }
                MotionEvent.ACTION_UP -> {
                    timer?.cancel()
                    if (!longPressed && inViewInBounds(v, event.rawX.toInt(), event.rawY.toInt())) {
                        canvasViews.toggleToolbarVisibility()
                    } else canvasViews.toolbarOuterCardView.alpha = CanvasPreferences.FULL_ALPHA
                }
                MotionEvent.ACTION_CANCEL -> {
                    canvasViews.toolbarOuterCardView.alpha = CanvasPreferences.FULL_ALPHA
                }
                MotionEvent.ACTION_MOVE -> {
                    timer?.cancel()
                    val upperBound = canvasViews.layoutCanvas.top
                    val lowerBound =
                        if(canvasViews.bottomToolbarOuterCardView.visibility== View.VISIBLE)
                            canvasViews.bottomToolbarOuterCardView.y - canvasViews.toolbarOuterCardView.height + canvasViews.toolbarOuterCardView.paddingBottom
                        else
                            (devicePixelHeight - canvasViews.toolbarOuterCardView.height).toFloat()
                    val newY = when {
                        event.rawY + dY > lowerBound -> lowerBound
                        event.rawY + dY < upperBound -> upperBound
                        else -> event.rawY + dY
                    }
                    if (dX != -1f && dY != -1f )
                        canvasViews.toolbarOuterCardView.animate()
                            //.x(event.rawX + dX)
                            .y(newY.toFloat())
                            .setDuration(0)
                            .start()
                }
            }
            true
        }
    }

    private fun inViewInBounds(view: View, x: Int, y: Int): Boolean {
        view.getDrawingRect(outRect)
        view.getLocationOnScreen(location)
        outRect.offset(location[0], location[1])
        return outRect.contains(x, y)
    }

    private fun vibrate(milliseconds: Long){
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(milliseconds)
        }
    }
}