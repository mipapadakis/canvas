package com.mipapadakis.canvas.model.layer

import android.graphics.*


/** This represents a layer of the canvas, also containing its own shapes.
 * @property bitmap: the layer's bitmap.
 * @property title: the layer's title, unique among the rest of the cvImage's layers.
 * */
class CvLayer(var title: String, private var bitmap: Bitmap){
    private var opacityPercent = 100
    val width = bitmap.width
    val height = bitmap.height
    var selected = false
    var visible = true

    constructor(title: String, cvLayer: CvLayer): this(title, Bitmap.createBitmap(cvLayer.bitmap)){
        opacityPercent = cvLayer.opacityPercent
        selected = cvLayer.selected
        visible = cvLayer.isVisible()
    }
    fun setVisible(){ visible = true }
    fun setInvisible(){ visible = false }
    fun isVisible() = visible
    fun isSelected() = selected

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
        tempCanvas.drawBitmap(getBitmap(), 0f, 0f, Paint().apply { alpha = getOpacity() })
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
}