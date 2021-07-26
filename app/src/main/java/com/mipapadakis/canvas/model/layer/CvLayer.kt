package com.mipapadakis.canvas.model.layer

import android.graphics.*
import com.mipapadakis.canvas.model.layer.shape.CvShape


/** This represents a layer of the canvas, also containing its own shapes.
 * @property bitmap: the layer's bitmap.
 * @property title: the layer's title, unique among the rest of the cvImage's layers.
 * @property shapes: Contains a list of all the shapes created by the user on this layer.
 * */
class CvLayer(var title: String, private var bitmap: Bitmap){
    var shapes: ArrayList<CvShape> = ArrayList()
    private var opacityPercent = 100
    val width = bitmap.width
    val height = bitmap.height
    var visible = true

    constructor(title: String, cvLayer: CvLayer): this(title, Bitmap.createBitmap(cvLayer.bitmap)){
        shapes = ArrayList(cvLayer.shapes)
        opacityPercent = cvLayer.opacityPercent
        visible = cvLayer.isVisible()
    }
    fun setVisible(){ visible = true }
    fun setInvisible(){ visible = false }
    fun isVisible() = visible

    private fun getOpacity() = opacityPercent*255/100
    fun getOpacityPercentage() = opacityPercent
    //fun setOpacity(newOpacity: Int){ opacity = newOpacity*100/255 }
    fun setOpacityPercentage(newOpacityPercentage: Int){
        opacityPercent = newOpacityPercentage
    }
    fun getBitmap() = bitmap
    fun getBitmapWithOpacity(): Bitmap{
        //Create temporary bitmap with full opacity
        val tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        tempCanvas.drawBitmap(bitmap, 0f, 0f, Paint().apply { alpha = getOpacity() })
        return tempBitmap
    }

    fun clearCanvas(){
        Canvas(bitmap).drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    }
    fun setBmp(bmp: Bitmap){
        val cv = Canvas(bitmap)
        cv.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        cv.drawBitmap(bmp, 0f, 0f, null)
    }
    fun newShape(shapeType: Int, bitmap: Bitmap){
        shapes.add(0, CvShape(shapeType, bitmap))
    }
}