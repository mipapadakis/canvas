package com.mipapadakis.canvas.model

import android.content.res.Resources
import android.graphics.*
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.model.layer.CvLayer
import java.util.*

/**This represents the canvas' list of layers that the user has created.*/

class CvImage(var title: String, var width: Int, var height: Int): ArrayList<CvLayer>() {
    var layerNameIndex = 0

    constructor(width: Int, height: Int): this("image", width, height)
    constructor(title: String, bmp: Bitmap): this(title, bmp.width, bmp.height){
        addLayer(0, bmp)
    }
    constructor(bmp: Bitmap): this("image", bmp.width, bmp.height){
        addLayer(0, bmp)
    }
    constructor(cvImage: CvImage): this(cvImage.title, cvImage.width, cvImage.height){
        for(layer in cvImage) add(CvLayer(layer.title, layer))
    }

    fun setCvImage(cvImage: CvImage){
        title = cvImage.title
        width = cvImage.width
        height = cvImage.height
        clear()
        for(layer in cvImage) {
            add(CvLayer(layer.title, layer))
            last().setOpacityPercentage(layer.getOpacityPercentage())
        }
        //addAll(cvImage)
    }
    fun layerCount() = size
    fun getPngGridLayer() = last()
    /** Create a new layer.*/
    fun newLayer(){ newLayer(Color.TRANSPARENT) }
    fun newLayer(backgroundColor: Int){
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        if(backgroundColor!=Color.TRANSPARENT){
            val canvas = Canvas(bitmap)
            canvas.drawColor(backgroundColor)
        }
        add(0, CvLayer(getUniqueLayerName(), bitmap))
    }
    /** Add an existing layer.*/
    fun addLayer(index: Int, bmp: Bitmap){ add(index, CvLayer(getUniqueLayerName(), bmp)) }
    fun addLayer(index: Int, cvLayer: CvLayer){ add(index, cvLayer) }
    /** Remove a layer.*/
    fun removeLayer(layer: CvLayer){ remove(layer) }
    fun removeLayer(layerIndex: Int){ removeAt(layerIndex) }
    /** Set top layer.*/
    fun setTopLayer(layer: CvLayer){
        removeLayer(layer)
        add(0, layer)
    }
    fun getTopLayer() = get(0)

    fun getUniqueLayerName(): String {return "Layer ${CanvasViewModel.cvImage.layerNameIndex++}"}

    fun swapLayers(fromPosition: Int, toPosition: Int){
        Collections.swap(this, fromPosition, toPosition)
    }

    // This layer is supposed to be in the background and must always remain at the end of the list!
    fun addPngGridLayer(resources: Resources){
        add(CvLayer(getUniqueLayerName(), createBackgroundBitmap(width, height, resources)))
    }

    /**Merges all layers into one bitmap.
     * @param withPngGrid: if set to true, use pngGrid as a background to represent transparency.*/
    fun getTotalImage(withPngGrid: Boolean): Bitmap{
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        for(i in (if(withPngGrid) layerCount()-1 else layerCount()-2) downTo 0) {
            if(get(i).isVisible()) canvas.drawBitmap(get(i).getBitmapWithOpacity(), 0f, 0f, null)
        }
        return bmp
    }

    private fun copyBitmap(bmp: Bitmap): Bitmap{
        return bmp.copy(bmp.config, true)
    }

    companion object{
        fun createBackgroundBitmap(width: Int, height: Int, resources: Resources): Bitmap{
            val backgroundBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isAntiAlias = false
                color = 0xFFFFFFFF.toInt()
                alpha = 255
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND //BUTT
                strokeWidth = 20F
                style = Paint.Style.FILL_AND_STROKE
            }
            val pngBackgroundPattern = BitmapFactory.decodeResource(resources, R.drawable.png_background_pattern)
            backgroundPaint.shader = BitmapShader( pngBackgroundPattern, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            val backgroundCanvas = Canvas(backgroundBitmap)
            backgroundCanvas.drawRect(0f, 0f, backgroundBitmap.width-1f, backgroundBitmap.height-1f, backgroundPaint)
            return backgroundBitmap
        }
    }
}