package com.mipapadakis.canvas.ui

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.toBitmap
import com.mipapadakis.canvas.model.CvImage
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt


/** Custom ImageView which represents the canvas. It handles canvas changes and touches. */
class CanvasImageView(context: Context?) : AppCompatImageView(context!!), MyTouchListener.MultiTouchListener{
    private lateinit var params: RelativeLayout.LayoutParams
    private lateinit var cvImage: CvImage
    private var mode = MODE_NONE
    private var oldDist = 1f
    private var newRot = 0f
    private var d = 0f
    var restoreAngle = 0f
    var startingWidth = 0
    var startingHeight = 0
    var scaleDiff = 0f
    var angle = 0f
    var rawX = 0f
    var rawY = 0f
    var dx = 0f
    var dy = 0f
    var drawPath: Path? = null

    companion object {
        private const val MIN_TOUCH_DISTANCE = 10F
        private const val MIN_SCALE = 0.3 //Determines how much the user can zoom out
        private const val MODE_NONE = 0
        private const val MODE_DRAG = 1
        private const val MODE_ZOOM = 2
    }

    init { setOnTouchListener(MyTouchListener(this)) }

    //First called in CanvasActivity.onAttachedToWindow()
    fun onAttachedToWindowInitializer(width: Int, height: Int){
        cvImage = CvImage(drawable.toBitmap())
        setImageBitmap(cvImage.layers[0].bitmap)
        drawPath = Path()
    }

    fun drawFreeHand(path: Path?){///////////////////////////////////////////////////////////////////////////
        if(mode== MODE_ZOOM) return
        cvImage.layers[0].drawFreeHand(path)
        setImageBitmap(cvImage.layers[0].bitmap)
    }

    private fun setPositionToCenter(){
        //val center = DeviceDimensions.getCenter(context!!)
        params = layoutParams as RelativeLayout.LayoutParams
        params.leftMargin = 0 //center.x - width / 2
        params.topMargin = 0 //center.y - height / 2
        params.rightMargin = 0
        params.bottomMargin = 0
        layoutParams = params
    }

    private fun restoreRotation(){
        animate().rotationBy(restoreAngle % 360).setDuration(200).setInterpolator(LinearInterpolator()).start()
        restoreAngle = 0f
    }

    private fun resetScale(){
        scaleDiff = 1F
        animate().scaleY(1F).duration = 200
        animate().scaleX(1F).duration = 200
    }
    private fun scaleToFitScreen(){
        val scaleRatioX = DeviceDimensions.getWidth(context).toFloat()/width.toFloat()
        val scaleRatioY = DeviceDimensions.getHeight(context).toFloat()/height.toFloat()
        val scaleToFit = min(scaleRatioX, scaleRatioY)
        scaleDiff = scaleToFit
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

    fun mapScreenCoordsToBitmapCoords(e: MotionEvent): FloatArray {
        // Get the coordinates of the touch point x, y
        val x = e.x
        val y = e.y
        // The coordinates of the target point
        val dst = FloatArray(2)
        // Get the matrix of ImageView
        val imageMatrix: Matrix = getImageMatrix()
        // Create an inverse matrix
        val inverseMatrix = Matrix()
        // Inverse, the inverse matrix is assigned
        imageMatrix.invert(inverseMatrix)
        // Get the value of the target point dst through the inverse matrix mapping
        inverseMatrix.mapPoints(dst, floatArrayOf(x, y))
        // Return the position on the Bitmap
        return dst
    }

    override fun on1PointerTap(event: MotionEvent) {
        Log.i("CanvasTouchListener", "on1PointerTap")
    }

    override fun on2PointerTap(event: MotionEvent) {
        Log.i("CanvasTouchListener", "on2PointerTap")
    }

    //TODO: if setting of 3-finger screenshot is enabled, and user taps with three fingers
    // aligned horizontally, onCancel is called. Fix?
    override fun on3PointerTap(event: MotionEvent) {
        Log.i("CanvasTouchListener", "on3PointerTap")
    }

    override fun on1PointerDoubleTap(event: MotionEvent) {
        Log.i("CanvasTouchListener", "on1PointerDoubleTap")
    }

    override fun on2PointerDoubleTap(event: MotionEvent) {
        Log.i("CanvasTouchListener", "on2PointerDoubleTap")
        //showStuff("BEFORE CHANGE:")
        setPositionToCenter()
        //showStuff("AFTER PARAM CHANGE:")
        restoreRotation()
        scaleToFitScreen()
        //resetScale()
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

    override fun on1PointerDown(event: MotionEvent) {
        ///////////////canvasPaint.setColor(paintColor)
        val yo = mapScreenCoordsToBitmapCoords(event)
        drawPath?.moveTo(yo[0], yo[1])//////////////////////////////////////////
        drawFreeHand(drawPath)
        invalidate()
    }

    override fun on2PointerDown(event: MotionEvent) {
        //TODO attempting to zoom -> erase anything drawn in on1PointerDown
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

    override fun on1PointerUp(event: MotionEvent) {
        val yo = mapScreenCoordsToBitmapCoords(event)
        drawPath?.lineTo(yo[0], yo[1])//////////////////////////////////////////
        ///////////////drawCanvas.drawPath(drawPath, drawPaint)
//        c!!.drawPath(drawPath!!, CanvasViewModel.paint)
        drawFreeHand(drawPath)
        drawPath?.reset()
        mode = MODE_NONE
        invalidate()
    }

    override fun on2PointerUp(event: MotionEvent) {}

    override fun on3PointerUp(event: MotionEvent) {}

    override fun onPointerMove(event: MotionEvent) {
        if(event.pointerCount==1){
            //drawCanvas.drawPath(drawPath, drawPaint)
            val yo = mapScreenCoordsToBitmapCoords(event)
            drawPath?.lineTo(yo[0], yo[1])//////////////////////////////////////////
            drawFreeHand(drawPath)
            invalidate()
        }

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
        }
        else if (mode == MODE_ZOOM && event.pointerCount == 2) {
            newRot = touchRotation(event)
            angle = newRot - d
            rawX = event.rawX
            rawY = event.rawY
            val newDist = touchDistance(event)
            if (newDist > MIN_TOUCH_DISTANCE) {
                val scale = newDist / oldDist * scaleX
                if (scale > MIN_SCALE) {
                    scaleDiff = scale
                    scaleX = scale
                    scaleY = scale
                }
            }
            restoreAngle -= angle
            animate().rotationBy(angle).setDuration(0).setInterpolator(LinearInterpolator()).start()
            rawX = event.rawX
            rawY = event.rawY
            params.leftMargin = (rawX - dx + scaleDiff).toInt()
            params.topMargin = (rawY - dy + scaleDiff).toInt()
            params.rightMargin = params.leftMargin + 5 * params.width
            params.bottomMargin = params.topMargin + 10 * params.height
            layoutParams = params
        }
    }

    override fun onCancelTouch() {
        Log.i("CanvasTouchListener", "onCancelTouch")
    }
}
