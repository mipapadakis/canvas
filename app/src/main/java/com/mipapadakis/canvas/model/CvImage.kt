package com.mipapadakis.canvas.model

import android.graphics.*
import com.mipapadakis.canvas.model.layer.CvLayer
import java.util.*
import kotlin.collections.ArrayList

/** This represents the user's painting, containing all its properties (title, layers)
 * @property background: represents the background of our canvas, with the png_background_pattern.png drawn on it.
 * @property title: A user-generated title for the image file.
 * @property layers: Contains all the layers of this CvImage. Their hierarchy is represented by
 * their position in the list, with the top layer located at position 0.
 * */

open class CvImage(var title: String, background: Bitmap) {
    val layers: ArrayList<CvLayer> = ArrayList()

    constructor(background: Bitmap): this("image", background)

    init {
        //there is only one layer, which contains the background of this CvImage.
        layers.add(CvLayer(background))
    }

    /** Create a new layer.*/
    fun newLayer(bmp: Bitmap){ layers.add(0, CvLayer(bmp)) }
    fun newLayer(width:Int, height: Int, backgroundColor: Int){
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        if(backgroundColor!=Color.TRANSPARENT){
            val canvas = Canvas(bitmap)
            canvas.drawColor(backgroundColor)
        }
        layers.add(0, CvLayer(bitmap))
    }
    /** Remove a layer.*/
    fun removeLayer(layer: CvLayer){ layers.remove(layer) }
    fun removeLayer(layerIndex: Int){ layers.removeAt(layerIndex) }
    /** Set top layer.*/
    fun setTopLayer(layer: CvLayer){
        layers.remove(layer)
        layers.add(0, layer)
    }
    fun setTopLayer(layerIndex: Int){
        val layer = CvLayer(layers[layerIndex])
        layers.removeAt(layerIndex)
        layers.add(0, layer)
    }

    private fun copyBitmap(bmp: Bitmap): Bitmap{
        return bmp.copy(bmp.config, true)
    }
}