package com.mipapadakis.canvas.model.layer

import android.graphics.*
import com.mipapadakis.canvas.CanvasViewModel

class CvLayer(var bitmap: Bitmap){
    val width: Int = bitmap.width
    val height: Int = bitmap.height
    var visible = true

    fun setVisible(){ visible=true } //TODO
    fun setInvisible(){ visible = false } //TODO

    fun clearCanvas(){
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
    }

    fun drawFreeHand(path: Path?){
        val tempCanvas = Canvas(bitmap)

        //Draw the image bitmap into the canvas
        tempCanvas.drawBitmap(bitmap, 0f, 0f, CanvasViewModel.paint)
        tempCanvas.drawPath(path?: Path(), CanvasViewModel.paint) //TODO debug? (if path==null -> ?)
    }

    fun drawRect(){ //TODO
        val x1 = 100F
        val y1 = 100F
        val x2 = 300F
        val y2 = 300F

        //Create a new image bitmap and attach a brand new canvas to it
        val tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        tempCanvas.drawColor(Color.WHITE)

        //Draw the image bitmap into the canvas
        tempCanvas.drawBitmap(bitmap, 0f, 0f, CanvasViewModel.paint)
        tempCanvas.drawRoundRect(RectF(x1, y1, x2, y2), 20F, 20F, CanvasViewModel.paint)

        bitmap = tempBitmap
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