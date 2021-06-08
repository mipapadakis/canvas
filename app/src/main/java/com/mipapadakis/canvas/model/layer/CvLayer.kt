package com.mipapadakis.canvas.model.layer

import android.graphics.*
import com.mipapadakis.canvas.CanvasViewModel


class CvLayer(var bitmap: Bitmap){
    private val initialWidth: Int = bitmap.width
    private val initialHeight: Int = bitmap.height
    private val paint = CanvasViewModel.paint
    //var canvas: Canvas
    var visible = true

    init {
//        val workingBitmap: Bitmap = Bitmap.createBitmap(bitmap)
//        val mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true)
//        canvas = Canvas(mutableBitmap)
    }

    fun setVisible(){ visible = true } //TODO
    fun setInvisible(){ visible = false } //TODO
    fun isVisible() = visible //TODO

    fun clearCanvas(){
        bitmap = Bitmap.createBitmap(initialWidth, initialHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
    }

    fun drawPath(canvas: Canvas, path: Path?){
        //canvas.drawColor( Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        canvas.drawPath(path ?: Path(), paint)
    }

    fun drawDot(canvas: Canvas, x: Float, y: Float){
        canvas.drawPoint(x,y,paint)
    }

    fun drawRect(canvas: Canvas){ //TODO
        val x1 = 100F
        val y1 = 100F
        val x2 = 300F
        val y2 = 300F

        //canvas.drawColor( Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        canvas.drawRoundRect(RectF(x1, y1, x2, y2), 20F, 20F, CanvasViewModel.paint)
    }
}

//fun drawFreeHand(path: Path?): Bitmap{
//    //Create a new image bitmap and attach a brand new canvas to it
//    val tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//    val tempCanvas = Canvas(tempBitmap)
//
//    //Draw the image bitmap into the canvas
//    tempCanvas.drawBitmap(tempBitmap, 0f, 0f, paint)
//    tempCanvas.drawPath(path?: Path(), paint) //TODO debug? (if path==null -> ?)
//    return tempBitmap
//}
//
//fun drawRect(paint: Paint): Bitmap{ //TODO
//    val myRectPaint = paint
//    val x1 = 100F
//    val y1 = 100F
//    val x2 = 300F
//    val y2 = 300F
//
//    //Create a new image bitmap and attach a brand new canvas to it
//    val tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//    val tempCanvas = Canvas(tempBitmap)
//
//    //Draw the image bitmap into the canvas
//    tempCanvas.drawBitmap(tempBitmap, 0F, 0F, myRectPaint)
//
//    //Draw everything else you want into the canvas, in this example a rectangle with rounded edges
//    tempCanvas.drawRoundRect(RectF(x1, y1, x2, y2), 20F, 20F, myRectPaint)
//
//    return tempBitmap
//}