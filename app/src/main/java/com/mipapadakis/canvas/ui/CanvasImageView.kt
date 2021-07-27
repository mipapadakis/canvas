package com.mipapadakis.canvas.ui

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.toBitmap
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.model.CvImage
import com.mipapadakis.canvas.tools.DeviceDimensions
import java.lang.StringBuilder
import java.util.*
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt


private const val POINTER_DOWN_DELAY = 10L

/** Custom ImageView which represents the canvas. It handles canvas changes and touches in order to draw on the canvas. */
class CanvasImageView(context: Context?, val notifyDataSetChanged: () -> Unit) : AppCompatImageView(context!!), MyTouchListener.MultiTouchListener{
    private val touchTolerance = 0.01f//ViewConfiguration.get(context).scaledTouchSlop //If the finger has moved less than the touchTolerance distance, don't draw.
    private lateinit var params: RelativeLayout.LayoutParams
    private lateinit var cvImage: CvImage
    private val paint = CanvasViewModel.paint
    private val eraserPaint = CanvasViewModel.eraserPaint
    private val history = CanvasViewModel.history
    private var historyIndex = CanvasViewModel.historyIndex
    //private var firstTime = true
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
    private lateinit var firstBitmap: Bitmap //Caches the extraBitmap while the user is drawing
    lateinit var foregroundBitmap: Bitmap //This has all the user-created drawings on it.
    lateinit var foregroundCanvas: Canvas
    private lateinit var visibleBitmap: Bitmap //Merges the background and foreground bitmaps together
    private lateinit var visibleCanvas: Canvas

    companion object {
        private const val X = 0
        private const val Y = 1
        private const val MIN_TOUCH_DISTANCE = 10F
        private const val SCALE_ANIMATION_DURATION: Long = 200
        private const val MIN_SCALE = 0.3 //Determines how much the user can zoom out
        private const val MODE_NONE = 0
        private const val MODE_DRAW = 1
        private const val MODE_PINCH = 2

        const val ACTION_INITIALIZE = 0
        const val ACTION_DRAW = 1
        const val ACTION_ERASE = 2
        const val ACTION_FLIP_VERTICALLY = 3
        const val ACTION_FLIP_HORIZONTALLY = 4
        const val ACTION_CROP = 5
        const val ACTION_LAYER_MOVE = 6
        const val ACTION_LAYER_ADD = 7
        const val ACTION_LAYER_MERGE = 8
        const val ACTION_LAYER_DUPLICATE = 9
        const val ACTION_LAYER_CLEAR = 10
        const val ACTION_LAYER_DELETE = 11
        const val ACTION_LAYER_OPACITY = 12
    }

    init { setOnTouchListener(MyTouchListener(this)) }

    //First called in CanvasActivity.onAttachedToWindow()
    fun onAttachedToWindowInitializer(width: Int, height: Int){
        CanvasViewModel.cvImage = CvImage(width, height)
        cvImage = CanvasViewModel.cvImage
        cvImage.addPngGridLayer(resources)

        //Add a layer containing the starting canvas (either imported image or solid-color canvas)
        val startingBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
        val startingCanvas = Canvas(startingBitmap)
        startingCanvas.drawBitmap(drawable.toBitmap())
        cvImage.addLayer(0, startingBitmap)

        startingWidth = width
        startingHeight = height
        setPositionToCenter()
        visibleBitmap = cvImage.getTotalImage(true)
        visibleCanvas = Canvas(visibleBitmap)
        foregroundBitmap = cvImage[0].getBitmap()
        foregroundCanvas = Canvas(foregroundBitmap)
        invalidate()
        addActionToHistory(ACTION_INITIALIZE)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        //if(firstTime){ firstTime = false }
    }

    override fun onDraw(canvas: Canvas) {
        //Make sure that we draw on the layer cvImage[0], in case the user changed the front layer
        foregroundBitmap = cvImage[0].getBitmap()
        foregroundCanvas = Canvas(foregroundBitmap) //TODO Avoid object allocations during draw/layout operations (preallocate and reuse instead)
        //Combine the background with the foreground into the visibleBitmap:
        visibleBitmap = cvImage.getTotalImage(true)
        //visibleCanvas.drawBitmap(foregroundBitmap)
        canvas.drawBitmap(visibleBitmap)
        notifyDataSetChanged() //so that LayerListAdapter reflects the changes in the layers
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
        if(firstPoint.isEmpty() || CanvasViewModel.shapeType != CanvasViewModel.SHAPE_TYPE_POLYGON)
            firstPoint = Point(event)
        firstBitmap = Bitmap.createBitmap(foregroundBitmap)
        prevPoint = Point(event)
        currentPath.reset()
        currentPath.moveTo(event.x, event.y)
        if(CanvasViewModel.tool == CanvasViewModel.TOOL_EYEDROPPER) draw(event, false)
        setModeDraw()
    }
    override fun on2PointerDown(event: MotionEvent) {
        params = layoutParams as RelativeLayout.LayoutParams
        dx = event.rawX - params.leftMargin
        dy = event.rawY - params.topMargin
        setModeDraw()
        oldDist = touchDistance(event)
        if (oldDist > MIN_TOUCH_DISTANCE) setModePinch()
        d = touchRotation(event)
    }
    override fun on3PointerDown(event: MotionEvent) { setModeNone() }
    override fun on1PointerUp(event: MotionEvent) {
        if(mode== MODE_DRAW){
            draw(event, true)
            //Save Action to history:
            when(CanvasViewModel.tool){
                CanvasViewModel.TOOL_ERASER -> addActionToHistory(ACTION_ERASE)
                CanvasViewModel.TOOL_EYEDROPPER -> {} //Don't include eyedropper actions to history
                else -> addActionToHistory(ACTION_DRAW)
            }
        }
        currentPath.reset()
        setModeNone()
    }
    override fun on2PointerUp(event: MotionEvent) { setModeNone() }
    override fun on3PointerUp(event: MotionEvent) {
        oldDist = touchDistance(event) // After third pointer is up, continue the zoom-mode using the remaining pointers
        setModeNone()
    }
    //https://github.com/lau1944/Zoom-Drag-Rotate-ImageView/blob/branch/rotateimageview/src/main/java/com/easystudio/rotateimageview/RotateZoomImageView.java
    override fun onPointerMove(event: MotionEvent) {
        if (mode == MODE_DRAW && event.pointerCount == 1) {
            if(Math.abs(event.x-prevPoint.x) >= touchTolerance || Math.abs(event.y-prevPoint.y) >= touchTolerance)
                draw(event, false)
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

    /**@param pointerUp: When set to false, the user is in the process of drawing.
     * When set to true, the user has lifted their finger, therefore their drawing must be
     * permanently drawn on the canvas.*/
    private fun draw(event: MotionEvent, pointerUp: Boolean) {
        foregroundBitmap = cvImage[0].getBitmap()
        foregroundCanvas = Canvas(foregroundBitmap)
        when (CanvasViewModel.tool) {
            CanvasViewModel.TOOL_BRUSH -> {
                clearForeground()
                foregroundCanvas.drawBitmap(firstBitmap)
                //https://developer.android.com/codelabs/advanced-android-kotlin-training-canvas#5
                currentPath.quadTo(prevPoint.x, prevPoint.y, (prevPoint.x + event.x) / 2, (prevPoint.y + event.y) / 2)
                foregroundCanvas.drawPath(currentPath, paint)
            }
            CanvasViewModel.TOOL_ERASER -> {
                clearForeground()
                foregroundCanvas.drawBitmap(firstBitmap)
                currentPath.quadTo(prevPoint.x, prevPoint.y, (prevPoint.x + event.x) / 2, (prevPoint.y + event.y) / 2)
                foregroundCanvas.drawPath(currentPath, eraserPaint)
            }
            CanvasViewModel.TOOL_BUCKET -> {
                val x = event.x.toInt()
                val y = event.y.toInt()
                val oldColor = foregroundBitmap.getPixel(x,y)
                /** The floodFill Aglorithm consumes too many resources resulting to a crash in
                 * many cases. To avoid this, use the floodFill_array() method which has no
                 * recursion, and uses an array instead of accessing the pixels one by one.*/
                //TODO: It is still pretty slow in cases of large areas to flood-fill.
                bucketFloodFill(foregroundBitmap, x, y, oldColor, CanvasViewModel.bucketPaint.color)
                foregroundCanvas.drawBitmap(foregroundBitmap)
            }
            CanvasViewModel.TOOL_EYEDROPPER -> {
                val xInBounds = if(event.x.toInt()<0) 0
                else if(event.x.toInt()>=foregroundBitmap.width) foregroundBitmap.width-1
                else event.x.toInt()
                val yInBounds = if(event.y.toInt()<0) 0
                else if(event.y.toInt()>=foregroundBitmap.height) foregroundBitmap.height-1
                else event.y.toInt()
                CanvasViewModel.setPaintColor(cvImage.getTotalImage(true).getPixel(xInBounds, yInBounds))
            }
            CanvasViewModel.TOOL_SHAPE -> {
                //TODO: make them resizable?
                clearForeground()
                foregroundCanvas.drawBitmap(firstBitmap)
                when(CanvasViewModel.shapeType){
                    CanvasViewModel.SHAPE_TYPE_LINE -> {
                        foregroundCanvas.drawLine(firstPoint.x, firstPoint.y, event.x, event.y, CanvasViewModel.shapePaint)
                    }
                    CanvasViewModel.SHAPE_TYPE_RECTANGLE -> {
                        foregroundCanvas.drawRect(firstPoint.x, firstPoint.y, event.x, event.y, CanvasViewModel.shapePaint)
                    }
                    CanvasViewModel.SHAPE_TYPE_SQUARE -> {
                        foregroundCanvas.drawRect(getSquareCoords(firstPoint, Point(event)), CanvasViewModel.shapePaint)
                    }
                    CanvasViewModel.SHAPE_TYPE_OVAL -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            foregroundCanvas.drawOval(firstPoint.x, firstPoint.y, event.x, event.y, CanvasViewModel.shapePaint)
                        }
                    }
                    CanvasViewModel.SHAPE_TYPE_CIRCLE -> {
                        foregroundCanvas.drawOval(getSquareCoords(firstPoint, Point(event)), CanvasViewModel.shapePaint)
                    }
                    CanvasViewModel.SHAPE_TYPE_POLYGON -> {
                        foregroundCanvas.drawLine(firstPoint.x, firstPoint.y, event.x, event.y, CanvasViewModel.shapePaint)
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
        invalidate()
    }

    /** Returns a RectF containing the coordinates of a square, created by combining two points.*/
    private fun getSquareCoords(pointA: Point, pointB: Point): RectF{
        val side = min(abs(pointA.x - pointB.x), abs(pointA.y - pointB.y))
        return if(pointB.x >= pointA.x && pointB.y >= pointA.y)
            RectF(pointA.x, pointA.y,pointA.x + side, pointA.y + side)
        else if(pointB.x < pointA.x && pointB.y >= pointA.y)
            RectF(pointA.x - side, pointA.y, pointA.x, pointA.y + side)
        else if(pointB.x >= pointA.x && pointB.y < pointA.y)
            RectF(pointA.x, pointA.y - side,pointA.x + side, pointA.y)
        else RectF(pointA.x - side, pointA.y - side,pointA.x, pointA.y)
    }

    //https://stackoverflow.com/a/23032962/11535380
    //The FLOOD-FILL algorithm, with no recursion for better performance:
    private fun bucketFloodFill(bmp: Bitmap, x: Int, y: Int, oldColor: Int, newColor: Int) {
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
        if(getForegroundLayer().isVisible()) mode = MODE_DRAW else setModeNone()
    }

    fun setModePinch(){
        mode = MODE_PINCH
        currentPath = Path()
        firstPoint.clear()
        prevPoint.clear()
    }

    private fun setPositionToCenter(){
        val center = DeviceDimensions.getCenter(context)
        params = layoutParams as RelativeLayout.LayoutParams
        params.leftMargin = center.x - startingWidth / 2
        params.topMargin = center.y - startingHeight / 2
        params.rightMargin = params.leftMargin + 5 * params.width //0
        params.bottomMargin = params.topMargin + 10 * params.height //0
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

    private fun clearForeground(){
        foregroundCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    }

    fun flipVertically(){
        val matrix = Matrix()
        matrix.postScale(1f, -1f, width / 2f,height / 2f)
        val flippedBmp = Bitmap.createBitmap(foregroundBitmap, 0, 0, width, height, matrix, true)
        clearForeground()
        foregroundCanvas.drawBitmap(flippedBmp)
        invalidate()
        addActionToHistory(ACTION_FLIP_VERTICALLY)
    }
    fun flipHorizontally(){
        val matrix = Matrix()
        matrix.postScale(-1f, 1f, width / 2f,height / 2f)
        val flippedBmp = Bitmap.createBitmap(foregroundBitmap, 0, 0, width, height, matrix, true)
        clearForeground()
        foregroundCanvas.drawBitmap(flippedBmp)
        invalidate()
        addActionToHistory(ACTION_FLIP_HORIZONTALLY)
    }

    private fun getForegroundLayer() = cvImage[0]

    fun undo(): Boolean{
        if(history.size==1 || historyIndex==0) return false
        history[--historyIndex].makeAction()
        showUndoHistory("UNDO")
        return true
    }

    fun redo(): Boolean{
        if(historyIndex==history.lastIndex) return false
        history[++historyIndex].makeAction()
        showUndoHistory("REDO")
        return true
    }

    fun addActionToHistory(actionType: Int){
        val currentAction = Action(actionType, CvImage(cvImage))
        if(historyIndex<history.lastIndex) {
            history[++historyIndex] = currentAction
            while(historyIndex != history.lastIndex) history.removeLast()
        }
        else{
            history.add(currentAction)
            historyIndex = history.lastIndex
        }
        showUndoHistory("ACTION ADDED")
    }

    fun showUndoHistory(msg: String){
        return
        val str = StringBuilder("$msg:")
        var i: Int
        for(action in history){
            i = history.indexOf(action)
            if(historyIndex==i) str.append("\n--> $i) ") else str.append("\n    $i) ")
            str.append("${when(action.actionType){
                ACTION_INITIALIZE -> "ACTION_INITIALIZE"
                ACTION_DRAW -> "ACTION_DRAW"
                ACTION_ERASE -> "ACTION_ERASE"
                ACTION_FLIP_VERTICALLY -> "ACTION_FLIP_VERTICALLY"
                ACTION_FLIP_HORIZONTALLY -> "ACTION_FLIP_HORIZONTALLY"
                ACTION_CROP -> "ACTION_CROP"
                ACTION_LAYER_ADD -> "ACTION_LAYER_ADD"
                ACTION_LAYER_MERGE -> "ACTION_LAYER_MERGE"
                ACTION_LAYER_DUPLICATE -> "ACTION_LAYER_DUPLICATE"
                ACTION_LAYER_CLEAR -> "ACTION_LAYER_CLEAR"
                ACTION_LAYER_DELETE -> "ACTION_LAYER_DELETE"
                else -> "Other"
            }}")
        }
        Log.d("CanvasUndoHistory", str.toString())
    }

    inner class Action( val actionType: Int, val actionCvImage: CvImage){

        fun makeAction(){
//            when(actionType){
//                ACTION_INITIALIZE -> {}
//                ACTION_DRAW -> {}
//                ACTION_ERASE -> {}
//                ACTION_FLIP_VERTICALLY -> {}
//                ACTION_FLIP_HORIZONTALLY -> {}
//                ACTION_CROP -> {}
//                ACTION_LAYER_ADD -> {}
//                ACTION_LAYER_MERGE -> {}
//                ACTION_LAYER_DUPLICATE -> {}
//                ACTION_LAYER_CLEAR -> {}
//                ACTION_LAYER_DELETE -> {}
//                else -> {}
//            }
            cvImage.setCvImage(actionCvImage)
            invalidate()
        }
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
        fun isEqualTo(p: Point) = (x==p.x && y==p.y)
        fun isEmpty() = (x == -1f && y == -1f)
    }
}

