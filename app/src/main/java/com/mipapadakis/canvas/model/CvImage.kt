package com.mipapadakis.canvas.model

import android.content.res.Resources
import android.graphics.*
import com.mipapadakis.canvas.CanvasPreferences
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.model.layer.CvLayer
import java.util.*
import kotlin.collections.ArrayList

/** This represents the user's painting, containing all its properties (title, layers)
 * @property background: represents the background of our canvas, with the png_background_pattern.png drawn on it.
 * @property title: A user-generated title for the image file.
 * @property layers: Contains all the layers of this CvImage. Their hierarchy is represented by
 * their position in the list, with the top layer located at position 0.
 * */

class CvImage(var title: String, val width: Int, val height: Int, resources: Resources): ArrayList<CvLayer>() {
    private val pngGridBitmap = createBackgroundBitmap(width, height, resources)

    constructor(width: Int, height: Int, resources: Resources):
            this("image", width, height, resources)
    constructor(title: String, bmp: Bitmap, resources: Resources): this(title, bmp.width, bmp.height, resources){
        addLayer(0, bmp)
    }
    constructor(bmp: Bitmap, resources: Resources): this("image", bmp.width, bmp.height, resources){
        addLayer(0, bmp)
    }

    init {
        //First add the background layer, which contains the pngGridBitmap.
        add(CvLayer(pngGridBitmap)) // This layer must always remain at the end of the list!
    }

    fun layerCount() = size
    fun getPngGridLayer() = last()
    /** Create a new layer.*/
    fun newLayer(){ add(0, CvLayer(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888))) }
    fun newLayer(width:Int, height: Int, backgroundColor: Int){
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        if(backgroundColor!=Color.TRANSPARENT){
            val canvas = Canvas(bitmap)
            canvas.drawColor(backgroundColor)
        }
        add(0, CvLayer(bitmap))
    }
    /** Add an existing layer.*/
    fun addLayer(index: Int, bmp: Bitmap){ add(index, CvLayer(bmp)) }
    fun addLayer(index: Int, cvLayer: CvLayer){ add(index, cvLayer) }
    /** Remove a layer.*/
    fun removeLayer(layer: CvLayer){ remove(layer) }
    fun removeLayer(layerIndex: Int){ removeAt(layerIndex) }
    /** Set top layer.*/
    fun setTopLayer(layer: CvLayer){
        remove(layer)
        add(0, layer)
    }
    fun setTopLayer(layerIndex: Int){
        val layer = CvLayer(get(layerIndex))
        removeAt(layerIndex)
        add(0, layer)
    }
    fun getTopLayer() = get(0)

    fun swapLayers(fromPosition: Int, toPosition: Int){
        Collections.swap(this, fromPosition, toPosition)
    }

    fun addStartingColorLayer(): CvLayer{ //Add a layer containing a white canvas to start drawing.
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(CanvasPreferences.startingCanvasColor)
        addLayer(0, CvLayer(bmp))
        return get(0)
    }

    /**Merges all layers into a total bitmap.
     * @param withPngGrid: if set to true, use pngGrid as a background to represent transparency.*/
    fun getTotalImage(withPngGrid: Boolean): Bitmap{
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        for(i in (if(withPngGrid) layerCount()-1 else layerCount()-2) downTo 0)
            canvas.drawBitmap(get(i).bitmap, 0f, 0f, null)
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