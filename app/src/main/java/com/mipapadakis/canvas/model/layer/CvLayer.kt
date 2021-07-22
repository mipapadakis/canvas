package com.mipapadakis.canvas.model.layer

import android.content.res.Resources
import android.graphics.*
import com.mipapadakis.canvas.model.CvImage
import com.mipapadakis.canvas.model.layer.shape.CvShape

/** This represents a layer of the canvas, also containing its own shapes.
 * @property bitmap: the layer's bitmap.
 * @property shapes: Contains a list of all the shapes created by the user on this layer.
 * */
class CvLayer(var bitmap: Bitmap){
    var shapes: ArrayList<CvShape> = ArrayList()
    val width = bitmap.width
    val height = bitmap.height
    var visible = true

    constructor(cvLayer: CvLayer): this(Bitmap.createBitmap(cvLayer.bitmap)){
        shapes = ArrayList(cvLayer.shapes)
    }
    fun setVisible(){ visible = true } //TODO
    fun setInvisible(){ visible = false } //TODO
    fun isVisible() = visible //TODO

    fun clearCanvas(){
        bitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    }
    fun newShape(shapeType: Int, bitmap: Bitmap){
        shapes.add(0, CvShape(shapeType, bitmap))
    }

}