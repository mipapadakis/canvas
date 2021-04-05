package com.mipapadakis.canvas.ui.canvas

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt


/** Custom ImageView which represents the canvas */
class CanvasImageView(context: Context?) : AppCompatImageView(context!!) /*, OnTouchListener*/ {
    private lateinit var params: RelativeLayout.LayoutParams
    private var mode = MODE_NONE
    private var oldDist = 1f
    private var newRot = 0f
    private var d = 0f
    var restoreAngle = 0f
    var startingWidth = 0
    var startingHeight = 0
    var scalediff = 0f
    var angle = 0f
    var rawX = 0f
    var rawY = 0f
    var dx = 0f
    var dy = 0f
    companion object {
        private const val MIN_TOUCH_DISTANCE = 10F
        private const val MIN_SCALE = 0.3 //Determines how much the user can zoom out
        private const val MODE_NONE = 0
        private const val MODE_DRAG = 1
        private const val MODE_ZOOM = 2
    }

    init {
        setOnTouchListener(CanvasTouchListener(object : CanvasTouchListener.MultiTouchListener {
            override fun on1PointerTap(event: MotionEvent) {
                Log.i("CanvasTouchListener", "on1PointerTap")
            }

            override fun on2PointerTap(event: MotionEvent) {
                Log.i("CanvasTouchListener", "on2PointerTap")
            }

            override fun on3PointerTap(event: MotionEvent) {
                Log.i("CanvasTouchListener", "on3PointerTap")
                //Bring view to center of screen:
                val center = DeviceDimensions.getCenter(context!!)
                params = layoutParams as RelativeLayout.LayoutParams
                params.leftMargin = center.x - width / 2
                params.topMargin = center.y - height / 2
                params.rightMargin = params.leftMargin + 5 * params.width
                params.bottomMargin = params.topMargin + 10 * params.height
                layoutParams = params
                restoreRotation()
                resetScale()
            }

            override fun on1PointerDoubleTap(event: MotionEvent) {
                Log.i("CanvasTouchListener", "on1PointerDoubleTap")
            }

            override fun on2PointerDoubleTap(event: MotionEvent) {
                Log.i("CanvasTouchListener", "on2PointerDoubleTap")
                //Bring view to center of screen:
                val center = DeviceDimensions.getCenter(context!!)
                params = layoutParams as RelativeLayout.LayoutParams
                params.leftMargin = center.x - width / 2
                params.topMargin = center.y - height / 2
                params.rightMargin = params.leftMargin + 5 * params.width
                params.bottomMargin = params.topMargin + 10 * params.height
                layoutParams = params
                restoreRotation()
                scaleToFitScreen()
            }

            override fun on3PointerDoubleTap(event: MotionEvent) {
                Log.i("CanvasTouchListener", "on3PointerDoubleTap")
            }

            override fun on1PointerLongPress(event: MotionEvent) {
                Log.i("CanvasTouchListener", "on1PointerLongPress")
            }

            override fun on2PointerLongPress(event: MotionEvent) {
                Log.i("CanvasTouchListener", "on2PointerLongPress")
            }

            override fun on3PointerLongPress(event: MotionEvent) {
                Log.i("CanvasTouchListener", "on3PointerLongPress")
            }

            override fun on1PointerDown(event: MotionEvent) {}

            override fun on2PointerDown(event: MotionEvent) {
                params = layoutParams as RelativeLayout.LayoutParams
                startingWidth = params.width
                startingHeight = params.height
                dx = event.rawX - params.leftMargin
                dy = event.rawY - params.topMargin
                mode = MODE_DRAG
                oldDist = touchDistance(event)
                if (oldDist > MIN_TOUCH_DISTANCE) mode = MODE_ZOOM
                d = touchRotation(event)
            }

            override fun on3PointerDown(event: MotionEvent) {}

            override fun on1PointerUp(event: MotionEvent) {}

            override fun on2PointerUp(event: MotionEvent) {}

            override fun on3PointerUp(event: MotionEvent) {}

            override fun onPointerMove(event: MotionEvent) {
                //TODO: if the ImageView is dragged out of the screen, center it
                if (mode == MODE_DRAG) {
                    rawX = event.rawX
                    rawY = event.rawY
                    params.leftMargin = (rawX - dx).toInt()
                    params.topMargin = (rawY - dy).toInt()
                    params.rightMargin = 0
                    params.bottomMargin = 0
                    params.rightMargin = params.leftMargin + 5 * params.width
                    params.bottomMargin = params.topMargin + 10 * params.height
                    layoutParams = params
                } else if (mode == MODE_ZOOM && event.pointerCount == 2) {
                    newRot = touchRotation(event)
                    angle = newRot - d
                    rawX = event.rawX
                    rawY = event.rawY
                    val newDist = touchDistance(event)
                    if (newDist > MIN_TOUCH_DISTANCE) {
                        val scale = newDist / oldDist * scaleX
                        if (scale > MIN_SCALE) {
                            scalediff = scale
                            scaleX = scale
                            scaleY = scale
                        }
                    }
                    restoreAngle -= angle
                    animate().rotationBy(angle).setDuration(0).setInterpolator(LinearInterpolator()).start()
                    rawX = event.rawX
                    rawY = event.rawY
                    params.leftMargin = (rawX - dx + scalediff).toInt()
                    params.topMargin = (rawY - dy + scalediff).toInt()
                    params.rightMargin = 0
                    params.bottomMargin = 0
                    params.rightMargin = params.leftMargin + 5 * params.width
                    params.bottomMargin = params.topMargin + 10 * params.height
                    layoutParams = params
                }
                //Log.i("CanvasTouchListener", "onPointerMove")
            }

            override fun onCancelTouch() {
                Log.i("CanvasTouchListener", "onCancelTouch")
            }
        }))
    }

    //First called in CanvasActivity.onAttachedToWindow()
    fun onAttachedToWindowInitializer(){
//        params = layoutParams as RelativeLayout.LayoutParams
//        params.leftMargin = 0
//        params.topMargin = 0
//        params.rightMargin = 0
//        params.bottomMargin = 0
//        layoutParams = params
    }

    private fun restoreRotation(){
        animate().rotationBy(restoreAngle % 360).setDuration(200).setInterpolator(LinearInterpolator()).start()
        restoreAngle = 0f
    }

    private fun resetScale(){
        scalediff = 1F
        animate().scaleY(1F).duration = 200
        animate().scaleX(1F).duration = 200
    }
    private fun scaleToFitScreen(){
        val scaleRatioX = DeviceDimensions.getWidth(context).toFloat()/width.toFloat()
        val scaleRatioY = DeviceDimensions.getHeight(context).toFloat()/height.toFloat()
        val scaleToFit = min(scaleRatioX, scaleRatioY)
        scalediff = scaleToFit
        animate().scaleX(scaleToFit).duration = 200
        animate().scaleY(scaleToFit).duration = 200
    }

    // Returns distance between two touches
    private fun touchDistance(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    // Returns degrees of rotation of two touches
    private fun touchRotation(event: MotionEvent): Float {
        val dx = (event.getX(0) - event.getX(1)).toDouble()
        val dy = (event.getY(0) - event.getY(1)).toDouble()
        val radians = atan2(dy, dx)
        return Math.toDegrees(radians).toFloat()
    }

    /*
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val imageView = v as ImageView
        (imageView.drawable as BitmapDrawable).setAntiAlias(true)
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                params = imageView.layoutParams as RelativeLayout.LayoutParams
                startingWidth = params!!.width
                startingHeight = params!!.height
                dx = event.rawX - params!!.leftMargin
                dy = event.rawY - params!!.topMargin
                mode = MODE_DRAG
                oldDist = touchDistance(event)
                if (oldDist > MIN_TOUCH_DISTANCE) mode = MODE_ZOOM
                d = touchRotation(event)
            }
            MotionEvent.ACTION_UP -> {
            }
            MotionEvent.ACTION_POINTER_UP -> mode = MODE_NONE
            MotionEvent.ACTION_MOVE -> if (mode == MODE_DRAG) {
                rawX = event.rawX
                rawY = event.rawY
                params!!.leftMargin = (rawX - dx).toInt()
                params!!.topMargin = (rawY - dy).toInt()
                params!!.rightMargin = 0
                params!!.bottomMargin = 0
                params!!.rightMargin = params!!.leftMargin + 5 * params!!.width
                params!!.bottomMargin = params!!.topMargin + 10 * params!!.height
                imageView.layoutParams = params
            } else if (mode == MODE_ZOOM && event.pointerCount == 2) {
                newRot = touchRotation(event)
                val r = newRot - d
                angle = r
                rawX = event.rawX
                rawY = event.rawY
                val newDist = touchDistance(event)
                if (newDist > MIN_TOUCH_DISTANCE) {
                    val scale = newDist / oldDist * imageView.scaleX
                    if (scale > MIN_SCALE) {
                        scalediff = scale
                        imageView.scaleX = scale
                        imageView.scaleY = scale
                    }
                }
                imageView.animate().rotationBy(angle).setDuration(0).setInterpolator(LinearInterpolator()).start()
                rawX = event.rawX
                rawY = event.rawY
                params!!.leftMargin = (rawX - dx + scalediff).toInt()
                params!!.topMargin = (rawY - dy + scalediff).toInt()
                params!!.rightMargin = 0
                params!!.bottomMargin = 0
                params!!.rightMargin = params!!.leftMargin + 5 * params!!.width
                params!!.bottomMargin = params!!.topMargin + 10 * params!!.height
                imageView.layoutParams = params
            }
        }
        return true
    }*/
}
