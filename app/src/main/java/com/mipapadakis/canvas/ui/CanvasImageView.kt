package com.mipapadakis.canvas.ui

import android.content.Context
import android.graphics.*
import android.os.CountDownTimer
import android.util.Log
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.toBitmap
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.model.CvImage
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

private const val POINTER_DOWN_DELAY = 10L

/** Custom ImageView which represents the canvas. It handles canvas changes and touches. */
class CanvasImageView(context: Context?) : AppCompatImageView(context!!), MyTouchListener.MultiTouchListener{
    private val touchTolerance = 0.1f //ViewConfiguration.get(context).scaledTouchSlop //If the finger has moved less than the touchTolerance distance, don't draw.
    private lateinit var params: RelativeLayout.LayoutParams
    private val paint = CanvasViewModel.paint
    private lateinit var cvImage: CvImage
    private var currentPath = Path()
    private val allPaths = Path()
    private var startingHeight = 0
    private var startingWidth = 0
    private var restoreAngle = 0f
    private var mode = MODE_NONE
    private var scaleDiff = 0f
    private var oldDist = 1f
    private var newRot = 0f
    private var angle = 0f
    private var dx = 0f
    private var dy = 0f
    private var d = 0f

    //Cached paths (of the whole drawing)

    //Cached coordinates:
    private var firstPoint = Point()//Coords of position at on1PointerDown(), for MODE_DRAW
    private var prevPoint = Point() //Coords of last position of pointer, for MODE_DRAW

    companion object {
        private const val X = 0
        private const val Y = 1
        private const val MIN_TOUCH_DISTANCE = 10F
        private const val MIN_SCALE = 0.3 //Determines how much the user can zoom out
        private const val MODE_NONE = 0
        private const val MODE_DRAW = 1
        private const val MODE_ZOOM = 2
    }

    init { setOnTouchListener(MyTouchListener(this)) }

    //First called in CanvasActivity.onAttachedToWindow()
    fun onAttachedToWindowInitializer(width: Int, height: Int){
        startingWidth = width
        startingHeight = height

        cvImage = CvImage(drawable.toBitmap())
        setImageBitmap(cvImage.layers[0].bitmap)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        canvas.drawPath(allPaths, paint)
//        canvas.drawPath(currentPath, paint)

        if(mode == MODE_DRAW) {
            try {
                cvImage.layers[0].drawPath(Canvas(drawable.toBitmap()), currentPath)
            }catch(e: java.lang.IllegalStateException){
                //Must pass a mutable bitmap to the Canvas constructor.
                val workingBitmap: Bitmap = Bitmap.createBitmap(drawable.toBitmap())
                val mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true)
                cvImage.layers[0].drawPath(Canvas(mutableBitmap), currentPath)
            }
            //canvas!!.drawBitmap(cvImage.layers[0].bitmap, 0f, 0f, paint)
            setImageBitmap(cvImage.layers[0].bitmap)
        }

        /**canvas.save()
        canvas.translate(dx, dy)
        cvImage.layers[0].drawFreeHand(Canvas(drawable.toBitmap()), currentPath)
        canvas.drawBitmap(cvImage.layers[0].bitmap, 0f, 0f, null)
        drawable.draw(canvas)
        canvas.restore()*/

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

    fun mapScreenCoordsToBitmapCoords(e: MotionEvent): Point {
        // Get the coordinates of the touch point x, y
        val x = e.x
        val y = e.y
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
        return Point(dst[X], dst[Y])
    }
    fun mapScreenCoordsToBitmapCoords(x: Float, y: Float): Point {
        val dst = FloatArray(2)
        val imageMatrix: Matrix = getImageMatrix()
        val inverseMatrix = Matrix()
        imageMatrix.invert(inverseMatrix)
        inverseMatrix.mapPoints(dst, floatArrayOf(x, y))
        return Point(dst[X], dst[Y])
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
//        resetScale() TODO this doesn't work!
//        setPositionToCenter()
//        restoreRotation()
    }

    override fun on1PointerDoubleTap(event: MotionEvent) {
        Log.i("CanvasTouchListener", "on1PointerDoubleTap")
    }

    override fun on2PointerDoubleTap(event: MotionEvent) {
        Log.i("CanvasTouchListener", "on2PointerDoubleTap")
        setPositionToCenter()
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

    override fun on1PointerDown(event: MotionEvent) {
        firstPoint = Point(event)
        prevPoint = Point(event)
        currentPath.reset()
        val bitmapCoords = mapScreenCoordsToBitmapCoords(event)
        currentPath.moveTo(bitmapCoords.x, bitmapCoords.y)
        //currentPath.moveTo(event.x, event.y)
        setModeDraw()

        object : CountDownTimer(POINTER_DOWN_DELAY,POINTER_DOWN_DELAY) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish(){
                if(mode== MODE_DRAW && firstPoint.equalTo(Point(event))){
                    val currentBitmapCoords = mapScreenCoordsToBitmapCoords(event)
                    try {
                        cvImage.layers[0].drawDot(Canvas(drawable.toBitmap()), currentBitmapCoords.x, currentBitmapCoords.y)
                    }catch(e: java.lang.IllegalStateException){
                        //Must pass a mutable bitmap to the Canvas constructor.
                        val workingBitmap: Bitmap = Bitmap.createBitmap(drawable.toBitmap())
                        val mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true)
                        cvImage.layers[0].drawDot(Canvas(mutableBitmap), currentBitmapCoords.x, currentBitmapCoords.y)
                    }
                    //canvas!!.drawBitmap(cvImage.layers[0].bitmap, 0f, 0f, paint)
                    setImageBitmap(cvImage.layers[0].bitmap)
                }
            }
        }.start()

//        invalidate()
    }

    override fun on2PointerDown(event: MotionEvent) {
        //TODO attempting to zoom -> erase anything drawn in on1PointerDown
        params = layoutParams as RelativeLayout.LayoutParams
        dx = event.rawX - params.leftMargin
        dy = event.rawY - params.topMargin
        setModeDraw()
        oldDist = touchDistance(event)
        if (oldDist > MIN_TOUCH_DISTANCE) {
            setModeZoom()
            val centerOfPointers = arrayOf((event.getX(0)+event.getX(1))/2,
                (event.getY(0)+event.getY(1))/2)
            pivotX = centerOfPointers[X]
            pivotY = centerOfPointers[Y]
        }
        d = touchRotation(event)
    }

    override fun on3PointerDown(event: MotionEvent) {
        setModeNone()
    }

    override fun on1PointerUp(event: MotionEvent) {
//        val bitmapCoords = mapScreenCoordsToBitmapCoords(event)
//        currentPath.lineTo(bitmapCoords.x,bitmapCoords.y)
//        invalidate()
        //allPaths.addPath(currentPath)
        currentPath.reset()
        setModeNone()
    }

    override fun on2PointerUp(event: MotionEvent) {
        setModeNone()
    }

    override fun on3PointerUp(event: MotionEvent) {
        oldDist = touchDistance(event)
        setModeNone()
    }

    override fun onPointerMove(event: MotionEvent) {
        if (mode == MODE_DRAW && event.pointerCount == 1) {
            val touchToleranceOk = Math.abs(event.x-prevPoint.x) >= touchTolerance
                                && Math.abs(event.y-prevPoint.y) >= touchTolerance
            if(touchToleranceOk) {
                val currentBitmapCoords = mapScreenCoordsToBitmapCoords(event)
//                val prevBitmapCoords = mapScreenCoordsToBitmapCoords(prevPoint.x, prevPoint.y)
//                currentPath.quadTo(currentBitmapCoords.x, currentBitmapCoords.y,
//                    (prevBitmapCoords.x + currentBitmapCoords.x) / 2,
//                    (prevBitmapCoords.y + currentBitmapCoords.y) / 2)
//                currentPath.quadTo(event.x, event.y, (prevPoint.x + event.x) / 2, (prevPoint.y + event.y) / 2) //TODO search quadTo() https://developer.android.com/codelabs/advanced-android-kotlin-training-canvas#5
//                prevPoint = Point(event)
                currentPath.lineTo(currentBitmapCoords.x, currentBitmapCoords.y)
                invalidate()
            }
            // else, pointer remains roughly at the same position => avoid drawing.
        }
        else if (mode == MODE_ZOOM && event.pointerCount == 2) {
            newRot = touchRotation(event)
            angle = newRot - d
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
            params.leftMargin = (event.rawX - dx + scaleDiff).toInt()
            params.topMargin =  (event.rawY - dy + scaleDiff).toInt()
            params.rightMargin = params.leftMargin + 5 * params.width
            params.bottomMargin = params.topMargin + 10 * params.height
            layoutParams = params
        }
    }

    override fun onCancelTouch() {
        Log.i("CanvasTouchListener", "onCancelTouch")
    }

    fun setModeNone(){
        mode = MODE_NONE
        currentPath = Path()
        //firstPoint.clear()
        prevPoint.clear()
    }

    fun setModeDraw(){
        mode = MODE_DRAW
    }

    fun setModeZoom(){
        mode = MODE_ZOOM
        currentPath = Path()
        //firstPoint.clear()
        prevPoint.clear()
    }

    inner class Point(var x: Float, var y: Float){
        constructor(x: Int, y: Int): this(x.toFloat(), y.toFloat())
        constructor(event: MotionEvent): this(event.x, event.y)
        constructor(point: Point): this(point.x, point.y)
        constructor(): this(-1f, -1f)
        fun clear(){
            x=-1f
            y=-1f
        }
        fun equalTo(p: Point) = x==p.x && y==p.y
    }
}
