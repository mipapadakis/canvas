package com.mipapadakis.canvas.ui

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.toBitmap
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.model.CvImage
import kotlin.collections.ArrayList
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt


private const val POINTER_DOWN_DELAY = 10L

/** Custom ImageView which represents the canvas. It handles canvas changes and touches. */
class CanvasImageView(context: Context?) : AppCompatImageView(context!!), MyTouchListener.MultiTouchListener{
    private val touchTolerance = 0.01f//ViewConfiguration.get(context).scaledTouchSlop //If the finger has moved less than the touchTolerance distance, don't draw.
    private lateinit var params: RelativeLayout.LayoutParams
    private lateinit var cvImage: CvImage
    private val paint = CanvasViewModel.paint
    private val history = ArrayList<Action>()
    private var firstTime = true
    private var historyIndex = 0
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

    //Cached coordinates:
    private var firstPoint = Point()//Coords of position at on1PointerDown(), for MODE_DRAW
    private var prevPoint = Point() //Coords of last position of pointer, for MODE_DRAW
    private var currentPath = Path()
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap

    companion object {
        private const val X = 0
        private const val Y = 1
        private const val MIN_TOUCH_DISTANCE = 10F
        private const val SCALE_ANIMATION_DURATION: Long = 200
        private const val MIN_SCALE = 0.3 //Determines how much the user can zoom out
        //const val CANVAS_PADDING = 100
        private const val MODE_NONE = 0
        private const val MODE_DRAW = 1
        private const val MODE_PINCH = 2

        const val ACTION_DRAW = 1
        const val ACTION_SCALE = 2
        const val ACTION_ROTATE = 3
        const val ACTION_FLIP_VERTICALLY = 4
        const val ACTION_FLIP_HORIZONTALLY = 5
        const val ACTION_CROP = 6
    }

    init { setOnTouchListener(MyTouchListener(this)) }

    //First called in CanvasActivity.onAttachedToWindow()
    fun onAttachedToWindowInitializer(width: Int, height: Int){
        startingWidth = width
        startingHeight = height
        setPositionToCenter()
        extraBitmap = drawable.toBitmap()
        addActionToHistory(ACTION_DRAW)
        //cvImage = CvImage(drawable.toBitmap())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(startingWidth, startingHeight, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(Color.WHITE)
        //if(firstTime){ firstTime = false }
    }

    override fun onDraw(canvas: Canvas) {
        // Draw the bitmap that has the saved path.
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)

        /**if(mode == MODE_DRAW) {
            try {
                cvImage.layers[0].drawPath(Canvas(drawable.toBitmap()), currentPath)
            }catch(e: java.lang.IllegalStateException){
                //Must pass a mutable bitmap to the Canvas constructor.
                val workingBitmap: Bitmap = Bitmap.createBitmap(drawable.toBitmap())
                val mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true)
                cvImage.layers[0].drawPath(Canvas(mutableBitmap), currentPath)
            }
            //canvas.save()
            //val coords = mapScreenCoordsToBitmapCoords(0f,0f)
            //canvas.translate(coords.x, coords.y)
            //cvImage.layers[0].drawPath(Canvas(drawable.toBitmap()), currentPath)
            //canvas.drawBitmap(cvImage.layers[0].bitmap, 0f, 0f, null)
            //drawable.draw(canvas)
            //canvas.restore()
        }*/
    }

    override fun on1PointerTap(event: MotionEvent) { Log.i("CanvasTouchListener", "on1PointerTap") }
    override fun on2PointerTap(event: MotionEvent) { Log.i("CanvasTouchListener", "on2PointerTap") }

    //TODO: if setting of 3-finger screenshot is enabled, and user taps with three fingers
    // aligned horizontally, onCancel is called. Fix?
    override fun on3PointerTap(event: MotionEvent) {
        Log.i("CanvasTouchListener", "on3PointerTap")
        resetScale()
        setPositionToCenter()
        restoreRotation()
    }

    override fun on1PointerDoubleTap(event: MotionEvent) { Log.i("CanvasTouchListener", "on1PointerDoubleTap") }

    override fun on2PointerDoubleTap(event: MotionEvent) {
        Log.i("CanvasTouchListener", "on2PointerDoubleTap")
        scaleToFitScreen()
        setPositionToCenter()
        restoreRotation()
    }

    override fun on3PointerDoubleTap(event: MotionEvent) { Log.i("CanvasTouchListener", "on3PointerDoubleTap") }

    override fun on1PointerLongPress(event: MotionEvent) {
        Log.i("CanvasTouchListener", "on1PointerLongPress")
        //TODO: change brush size? the longer pressed the larger the size
    }

    override fun on2PointerLongPress(event: MotionEvent) { Log.i("CanvasTouchListener", "on2PointerLongPress") }
    override fun on3PointerLongPress(event: MotionEvent) { Log.i("CanvasTouchListener", "on3PointerLongPress") }

    override fun on1PointerDown(event: MotionEvent) {
        //if (::backupBitmap.isInitialized && !backupBitmap.isRecycled()) backupBitmap.recycle()
        //backupBitmap = Bitmap.createBitmap(drawable.toBitmap())

        firstPoint = Point(event)
        prevPoint = Point(event)
        currentPath.reset()
        currentPath.moveTo(event.x, event.y)
        setModeDraw()

        /** When user touches with two pointers, avoid drawing with the first touch.
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
        }.start()*/
    }

    override fun on2PointerDown(event: MotionEvent) {
        params = layoutParams as RelativeLayout.LayoutParams
        dx = event.rawX - params.leftMargin
        dy = event.rawY - params.topMargin
        setModeDraw()
        oldDist = touchDistance(event)
        if (oldDist > MIN_TOUCH_DISTANCE) {
            /**TODO: Attempting to zoom -> erase anything drawn in on1PointerDown:
            if(mode== MODE_DRAW && firstPoint.equalTo(Point(event))) setImageBitmap(backupBitmap)*/
            setModeZoom()

//            val centerOfPointers = arrayOf((event.getX(0)+event.getX(1))/2, (event.getY(0)+event.getY(1))/2) //TODO! debug this
//            pivotX = centerOfPointers[X]
//            pivotY = centerOfPointers[Y]
        }
        d = touchRotation(event)
    }

    override fun on3PointerDown(event: MotionEvent) { setModeNone() }

    override fun on1PointerUp(event: MotionEvent) {
        if(mode== MODE_DRAW){
            currentPath.quadTo(prevPoint.x, prevPoint.y, (prevPoint.x + event.x) / 2, (prevPoint.y + event.y) / 2) //https://developer.android.com/codelabs/advanced-android-kotlin-training-canvas#5
            extraCanvas.drawPath(currentPath, paint)
            //Create a dot
            currentPath.reset()
            currentPath.moveTo(event.x, event.y)
            currentPath.lineTo(event.x, event.y+1)
            extraCanvas.drawPath(currentPath, paint)
            invalidate()
            addActionToHistory(ACTION_DRAW)
            currentPath.reset()
            setModeNone()
        }
        currentPath.reset()
        setModeNone()
    }

    override fun on2PointerUp(event: MotionEvent) {
        setModeNone()
        //TODO: When double tapping, on2PointerUp() is called twice. Must avoid adding two actions to history.
    }

    override fun on3PointerUp(event: MotionEvent) {
        oldDist = touchDistance(event) // After third pointer is up, continue the zoom-mode using the remaining pointers
        setModeNone()
    }

    //https://github.com/lau1944/Zoom-Drag-Rotate-ImageView/blob/branch/rotateimageview/src/main/java/com/easystudio/rotateimageview/RotateZoomImageView.java
    override fun onPointerMove(event: MotionEvent) {
        if (mode == MODE_DRAW && event.pointerCount == 1) {
            if(Math.abs(event.x-prevPoint.x) >= touchTolerance || Math.abs(event.y-prevPoint.y) >= touchTolerance) {
                currentPath.quadTo(prevPoint.x, prevPoint.y, (prevPoint.x + event.x) / 2, (prevPoint.y + event.y) / 2) //https://developer.android.com/codelabs/advanced-android-kotlin-training-canvas#5
                extraCanvas.drawPath(currentPath, paint)
            }
            invalidate()
            // else, pointer remains roughly at the same position => avoid drawing.
        }
        else if (mode == MODE_PINCH && event.pointerCount == 2) {

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
            params.leftMargin = (event.rawX-dx).toInt() //event.rawX-dx+scaleDiff
            params.topMargin =  (event.rawY-dy).toInt() ///event.rawY-dy+scaleDiff
            params.rightMargin = params.leftMargin + 5 * params.width
            params.bottomMargin = params.topMargin + 10 * params.height
            layoutParams = params
        }
        prevPoint = Point(event)
    }

    override fun onCancelTouch(event: MotionEvent?) {
        if(event?.pointerCount==3)
            Log.i("CanvasTouchListener", "onCancel due to 3-finger screenshot") //todo: show this as a toast?
        else Log.i("CanvasTouchListener", "onCancelTouch")
    }

    fun setModeNone(){
        mode = MODE_NONE
        currentPath = Path()
        //firstPoint.clear() //todo?
        prevPoint.clear()
    }

    fun setModeDraw(){
        mode = MODE_DRAW
    }

    fun setModeZoom(){
        mode = MODE_PINCH
        currentPath = Path()
        //firstPoint.clear()
        prevPoint.clear()
    }

    private fun setPositionToCenter(){
        val center = DeviceDimensions.getCenter(context!!)
        params = layoutParams as RelativeLayout.LayoutParams
        params.leftMargin = center.x - startingWidth / 2
        params.topMargin = center.y - startingHeight / 2
        params.rightMargin = 0
        params.bottomMargin = 0
        layoutParams = params
    }

    private fun restoreRotation(){
        animate().rotationBy(restoreAngle % 360).setDuration(SCALE_ANIMATION_DURATION).setInterpolator(LinearInterpolator()).start()
        restoreAngle = 0f
    }

    private fun resetScale(){
        scaleDiff = 1F
        animate().scaleY(1F).duration = SCALE_ANIMATION_DURATION
        animate().scaleX(1F).duration = SCALE_ANIMATION_DURATION
    }

    private fun scaleToFitScreen(){
        val scaleRatioX = DeviceDimensions.getWidth(context).toFloat()/width.toFloat()
        val scaleRatioY = DeviceDimensions.getHeight(context).toFloat()/height.toFloat()
        val scaleToFit = min(scaleRatioX, scaleRatioY)
        scaleDiff = scaleToFit
        animate().scaleX(scaleToFit).duration = SCALE_ANIMATION_DURATION
        animate().scaleY(scaleToFit).duration = SCALE_ANIMATION_DURATION
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

    private fun getCanvasCenter() = Point((params.rightMargin + params.leftMargin)/2, (params.topMargin + params.bottomMargin)/2)

//    fun mapScreenCoordsToBitmapCoords(e: MotionEvent): Point {
//        // Get the coordinates of the touch point x, y
//        val x = e.x
//        val y = e.y
//        val dst = FloatArray(2)
//        // Get the matrix of ImageView
//        val imageMatrix: Matrix = getImageMatrix()
//        // Create an inverse matrix
//        val inverseMatrix = Matrix()
//        // Inverse, the inverse matrix is assigned
//        imageMatrix.invert(inverseMatrix)
//        // Get the value of the target point dst through the inverse matrix mapping
//        inverseMatrix.mapPoints(dst, floatArrayOf(x, y))
//        // Return the position on the Bitmap
//        return Point(dst[X], dst[Y])
//    }

    private fun addActionToHistory(actionType: Int){

        val currentAction = Action(actionType,
            Bitmap.createBitmap(extraBitmap),
            startingWidth,
            startingHeight,
            false,
            false)

        if(historyIndex<history.lastIndex) { //Redo:
            history[++historyIndex] = currentAction
            while(historyIndex != history.lastIndex){
                history.removeLast()
            }
        }
        else{
            history.add(currentAction)
            historyIndex = history.lastIndex
        }

        showHistory("addActionToHistory")
    }

    private fun showHistory(msg: String){
        val s = StringBuilder("$msg\n")
        for(i in 0 until history.size){
            s.append("\n• history[$i] = ")
            when (history[i].actionType){
                ACTION_DRAW ->  s.append("ACTION_DRAW")
                ACTION_SCALE ->  s.append("ACTION_SCALE")
                ACTION_ROTATE -> s.append("ACTION_ROTATE")
                ACTION_FLIP_VERTICALLY ->  s.append("ACTION_FLIP_VERTICALLY")
                ACTION_FLIP_HORIZONTALLY ->  s.append("ACTION_FLIP_HORIZONTALLY")
                ACTION_CROP ->  s.append("ACTION_CROP")
                else -> {s.append("ERROR")}
            }
            if(i==historyIndex) s.append(" -> active")
        }
        s.append("\nhistoryIndex = $historyIndex")
        Log.i("CanvasHistory", s.toString())
    }

    fun undo(): Boolean{
        if(history.size==1 || historyIndex==0) return false
        history[--historyIndex].makeAction()
        showHistory("undo")
        return true
    }

    fun redo(): Boolean{
        if(historyIndex==history.lastIndex) return false
        history[++historyIndex].makeAction()
        showHistory("redo")
        return true
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
        fun isEqualTo(p: Point) = x==p.x && y==p.y
    }

    // History = List<Action>
    inner class Action(val actionType: Int,
                       bitmap: Bitmap,
                       val cropWidth: Int,
                       val cropHeight: Int,
                       val flippedHorizontally: Boolean,
                       val flippedVertically: Boolean){
        var bitmap: Bitmap

        init {
            this.bitmap = Bitmap.createBitmap(bitmap)
        }

        /** Example:
         *  val actionDraw = Action(Action.ACTION_DRAW)
         *  actionDraw.addBitmap(bmp)
         *  history.add(actionDraw)
         * */

        fun setBmp(bmp: Bitmap){ bitmap = Bitmap.createBitmap(bmp) }

        fun makeAction(){
            //ACTION_DRAW
            extraCanvas.drawBitmap(bitmap, 0f, 0f, null)
            invalidate()

            //ACTION_FLIP_VERTICALLY
            //TODO

            //ACTION_FLIP_HORIZONTALLY
            //TODO

            //ACTION_CROP
            //TODO
        }
    }
}

