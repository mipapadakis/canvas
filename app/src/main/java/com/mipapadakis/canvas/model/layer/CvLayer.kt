package com.mipapadakis.canvas.model.layer

import android.graphics.*
import java.io.ByteArrayOutputStream
import java.io.Serializable


/** This represents a layer of the canvas, also containing its own shapes.
 * @property bitmap: the layer's bitmap.
 * @property title: the layer's title, unique among the rest of the cvImage's layers.
 * */
class CvLayer(var title: String, private var bitmap: Bitmap){
    private var opacity = 255
    val width = bitmap.width
    val height = bitmap.height
    var selected = false
    var visible = true

    constructor(title: String, cvLayer: CvLayer): this(title, Bitmap.createBitmap(cvLayer.bitmap)){
        opacity = cvLayer.opacity
        selected = cvLayer.selected
        visible = cvLayer.isVisible()
    }
    fun isVisible() = visible
    fun isSelected() = selected

    fun getOpacity() = opacity
    fun setOpacity(newOpacity: Int) {opacity = newOpacity}
    fun getBitmap() = bitmap
    fun getBitmapWithOpacity(): Bitmap{
        //Create temporary bitmap with full opacity
        val tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        tempCanvas.drawBitmap(getBitmap(), 0f, 0f, Paint().apply { alpha = opacity })
        return tempBitmap
    }

    fun clearCanvas(){
        Canvas(bitmap).drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    }

    fun toSerializable(): SerializableCvLayer {
        return SerializableCvLayer(this)
    }

    class SerializableCvLayer(cvLayer: CvLayer): Serializable {
        private val opacity = cvLayer.getOpacity()
        private val byteArray: ByteArray
        val title = cvLayer.title

        init {
            val byteStream = ByteArrayOutputStream()
            cvLayer.getBitmap().compress(Bitmap.CompressFormat.PNG, 0, byteStream) //If you compress() to a PNG, the quality value is ignored.
            byteArray = byteStream.toByteArray()
        }

        fun deserialize(): CvLayer {
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size).copy(Bitmap.Config.ARGB_8888, true)
            val deserializedLayer = CvLayer(title, bitmap)
            deserializedLayer.setOpacity(opacity)
            return deserializedLayer
        }
    }
}

