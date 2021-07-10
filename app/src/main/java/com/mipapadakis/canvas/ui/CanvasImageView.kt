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
import com.mipapadakis.canvas.tools.DeviceDimensions
import java.util.*
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
    private val eraserPaint = CanvasViewModel.eraserPaint

    private val history = ArrayList<Action>()
    //private var firstTime = true
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

    //Cached coordinates/bitmaps:
    private var firstPoint = Point()//Coords of position at on1PointerDown(), for MODE_DRAW
    private var prevPoint = Point() //Coords of last position of pointer, for MODE_DRAW
    private var currentPath = Path()
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private lateinit var firstBitmap: Bitmap

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

        extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(startingWidth, startingHeight, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(Color.WHITE)
        //if(firstTime){ firstTime = false }
    }

    override fun onDraw(canvas: Canvas) {
        // Draw the bitmap that has the saved path.
        canvas.drawBitmap(extraBitmap)
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

        if(firstPoint.isEmpty() || CanvasViewModel.shapeType != CanvasViewModel.SHAPE_TYPE_POLYGON)
            firstPoint = Point(event)
        firstBitmap = Bitmap.createBitmap(extraBitmap)
        prevPoint = Point(event)
        currentPath.reset()
        currentPath.moveTo(event.x, event.y)
        setModeDraw()
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
            draw(event, true) //Draw in case onPointerMove() has been called
            drawDot(event) //Draw dot in case of single tap
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
    }

    override fun on3PointerUp(event: MotionEvent) {
        oldDist = touchDistance(event) // After third pointer is up, continue the zoom-mode using the remaining pointers
        setModeNone()
    }

    //https://github.com/lau1944/Zoom-Drag-Rotate-ImageView/blob/branch/rotateimageview/src/main/java/com/easystudio/rotateimageview/RotateZoomImageView.java
    override fun onPointerMove(event: MotionEvent) {
        if (mode == MODE_DRAW && event.pointerCount == 1) {
            if(Math.abs(event.x-prevPoint.x) >= touchTolerance || Math.abs(event.y-prevPoint.y) >= touchTolerance) {
                draw(event, false)
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

    /**@param pointerUp: used while the Shape tool is selected. When set to false, the user is in the
     * process of creating a shape. When set to true, the user has lifted their finger, therefore
     * the shape must be permanently drawn on the canvas.*/
    private fun draw(event: MotionEvent, pointerUp: Boolean) {
        when (CanvasViewModel.tool) {
            CanvasViewModel.TOOL_BRUSH -> {
                currentPath.quadTo(prevPoint.x, prevPoint.y, (prevPoint.x + event.x) / 2, (prevPoint.y + event.y) / 2) //https://developer.android.com/codelabs/advanced-android-kotlin-training-canvas#5
                extraCanvas.drawPath(currentPath, paint)
            }
            CanvasViewModel.TOOL_ERASER -> {
                currentPath.quadTo(prevPoint.x, prevPoint.y, (prevPoint.x + event.x) / 2, (prevPoint.y + event.y) / 2) //https://developer.android.com/codelabs/advanced-android-kotlin-training-canvas#5
                extraCanvas.drawPath(currentPath, eraserPaint)
            }
            CanvasViewModel.TOOL_BUCKET -> {
                val x = event.x.toInt()
                val y = event.y.toInt()
                val oldColor = extraBitmap.getPixel(x,y)
                /** The floodFill Aglorithm consumes too many resources resulting to a crash in
                 * many cases. To avoid this, use the floodFill_array() method which has no
                 * recursion, and uses an array instead of accessing the pixels one by one.*/
                //TODO: It is still pretty slow in cases of large areas to flood-fill.
                bucketFloodFill(extraBitmap, x, y, oldColor, CanvasViewModel.paint.color)
                extraCanvas.drawBitmap(extraBitmap)
            }
            CanvasViewModel.TOOL_EYEDROPPER -> {
                CanvasViewModel.setBrushAndShapeColor(drawable.toBitmap().getPixel(event.x.toInt(), event.y.toInt()))
            }
            CanvasViewModel.TOOL_SHAPE -> {
                //TODO: make them resizable
                if(!pointerUp) extraCanvas.drawBitmap(firstBitmap)
                when(CanvasViewModel.shapeType){
                    CanvasViewModel.SHAPE_TYPE_LINE -> {
                        extraCanvas.drawLine(firstPoint.x, firstPoint.y, event.x, event.y, CanvasViewModel.shapePaint)
                    }
                    CanvasViewModel.SHAPE_TYPE_RECTANGLE -> {
                        extraCanvas.drawRect(firstPoint.x, firstPoint.y, event.x, event.y, CanvasViewModel.shapePaint)
                    }
                    CanvasViewModel.SHAPE_TYPE_SQUARE -> {} //TODO
                    CanvasViewModel.SHAPE_TYPE_OVAL -> {
                        extraCanvas.drawOval(firstPoint.x, firstPoint.y, event.x, event.y, CanvasViewModel.shapePaint)
                    }
                    CanvasViewModel.SHAPE_TYPE_CIRCLE -> {} //TODO
                    CanvasViewModel.SHAPE_TYPE_POLYGON -> {
                        extraCanvas.drawLine(firstPoint.x, firstPoint.y, event.x, event.y, CanvasViewModel.shapePaint)
                        if(pointerUp) firstPoint = Point(event)
                    }
                    CanvasViewModel.SHAPE_TYPE_TRIANGLE -> {} //TODO
                    CanvasViewModel.SHAPE_TYPE_ARROW -> {} //TODO
                    CanvasViewModel.SHAPE_TYPE_CALLOUT -> {} //TODO
                }
                if (pointerUp) firstBitmap.recycle()
            }
            CanvasViewModel.TOOL_SELECT -> {}
        }
    }

    private fun drawDot(event: MotionEvent){
        val dot = Path()
        dot.moveTo(event.x, event.y)
        dot.lineTo(event.x, event.y+1)
        when (CanvasViewModel.tool) {
            CanvasViewModel.TOOL_BRUSH -> extraCanvas.drawPath(dot, paint)
            CanvasViewModel.TOOL_ERASER -> extraCanvas.drawPath(dot, eraserPaint)
        }
    }

    //https://stackoverflow.com/a/23032962/11535380
    //The FLOOD-FILL algorithm, with no recursion for better performance:
    private fun bucketFloodFill(bmp: Bitmap, x: Int, y: Int, oldColor: Int, newColor: Int ) {
        if (oldColor == newColor) return
        val pixelArray = IntArray(width * height)
        val q: Queue<Array<Int>> = LinkedList()
        val width = bmp.width
        val height = bmp.height

        bmp.getPixels(pixelArray, 0, width, 0, 0, width, height)
        q.add(arrayOf(x,y))
        while (q.size > 0) {
            val node = q.poll() ?: continue // Returns and removes the element at the front the queue.
            if (pixelArray[(width * node[Y] + node[X]).toInt()] != oldColor) continue
            val e = arrayOf(node[X] + 1, node[Y])
            while (node[X] >= 0 && pixelArray[(width * node[Y] + node[X]).toInt()] == oldColor) {
                pixelArray[(width * node[Y] + node[X]).toInt()] = newColor // set pixel color
                if (node[Y] > 0 && pixelArray[(width * (node[Y] - 1) + node[X]).toInt()] == oldColor)
                    q.add(arrayOf( node[X], node[Y] - 1))
                if (node[Y] < height-1 && pixelArray[(width * (node[Y] + 1) + node[X]).toInt()] == oldColor)
                    q.add(arrayOf(node[X], node[Y] + 1))
                node[X]--
            }
            while (e[X] < width && pixelArray[(width * e[Y] + e[X]).toInt()] == oldColor) {
                pixelArray[(width * e[Y] + e[X]).toInt()] = newColor // setPixel
                if (e[Y] > 0 && pixelArray[(width * (e[Y] - 1) + e[X]).toInt()] == oldColor)
                    q.add(arrayOf(e[X], e[Y] - 1))
                if (e[Y] < height - 1 && pixelArray[(width * (e[Y] + 1) + e[X]).toInt()] == oldColor)
                    q.add(arrayOf(e[X], e[Y] + 1))
                e[X]++
            }
        }
        bmp.setPixels(pixelArray, 0, width, 0, 0, width, height)
    }

    /** The FLOOD FILL algorithm (Basic 4 Way Recursive Method).
     * This method has low performance if there are many pixels to fill, resulting in a crash.
     * private fun floodFill(x: Int, y: Int, oldColor: Int){
        if(x !in 0 until extraBitmap.width || y !in 0 until extraBitmap.height) return
        if(extraBitmap.getPixel(x, y) != oldColor) return
        extraBitmap.setPixel(x, y, paint.color)
        floodFill( x+1, y, oldColor) //East
        floodFill( x-1, y, oldColor) //West
        floodFill( x, y+1, oldColor) //North
        floodFill( x, y-1, oldColor) //South
    }*/

    fun setModeNone(){
        mode = MODE_NONE
        currentPath = Path()
        if(CanvasViewModel.shapeType != CanvasViewModel.SHAPE_TYPE_POLYGON) firstPoint.clear()
        prevPoint.clear()
    }

    fun setModeDraw(){
        mode = MODE_DRAW
    }

    fun setModeZoom(){
        mode = MODE_PINCH
        currentPath = Path()
        firstPoint.clear()
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

    private fun Canvas.drawBitmap(bmp: Bitmap){ this.drawBitmap(bmp, 0f, 0f, null) }

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
            startingHeight)

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
    }

    fun undo(): Boolean{
        if(history.size==1 || historyIndex==0) return false
        history[--historyIndex].makeAction()
        return true
    }

    fun redo(): Boolean{
        if(historyIndex==history.lastIndex) return false
        history[++historyIndex].makeAction()
        return true
    }

    fun flipVertically(){
        val matrix = Matrix()
        matrix.postScale(1f, -1f, width / 2f,height / 2f)
        val flippedBmp = Bitmap.createBitmap(extraBitmap, 0, 0, width, height, matrix, true)
        extraCanvas.drawBitmap(flippedBmp)
        invalidate()
        addActionToHistory(ACTION_FLIP_VERTICALLY)
    }
    fun flipHorizontally(){
        val matrix = Matrix()
        matrix.postScale(-1f, 1f, width / 2f,height / 2f)
        val flippedBmp = Bitmap.createBitmap(extraBitmap, 0, 0, width, height, matrix, true)
        extraCanvas.drawBitmap(flippedBmp)
        invalidate()
        addActionToHistory(ACTION_FLIP_HORIZONTALLY)
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
        fun isEmpty() = (x == -1f && y == -1f)
    }

    // History = List<Action>
    inner class Action(val actionType: Int,
                       bitmap: Bitmap,
                       val cropWidth: Int,
                       val cropHeight: Int){
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
            extraCanvas.drawBitmap(bitmap)
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

