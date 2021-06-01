package com.mipapadakis.canvas.model

import android.graphics.*
import com.mipapadakis.canvas.model.layer.CvLayer
import com.mipapadakis.canvas.model.shape.CvShape

/** This represents the user's painting, containing all its properties (title, layers, shapes)
 * @property background: contains the background of our canvas, aka the first layer added in the list.
 * @property width: The width of the image. It can be changed by the user from the global settings.
 * @property height: The height of the image. It can be changed by the user from the global settings.
 * @property title: A user-generated title for the image file
 * @property layers: Contains all the layers of this CvImage. Their hierarchy is represented by
 * their position in the list, with the top layer located at position 0.
 * @property shapes: Contains a list of all the shapes created by the user in this session.
 * */

open class CvImage(var title: String, background: Bitmap) {
    val layers: ArrayList<CvLayer> = ArrayList()
    var shapes: ArrayList<CvShape> = ArrayList()

    constructor(background: Bitmap): this("image", background)

    init {
        //there is only one layer, which contains the background of this CvImage.
        layers.add(CvLayer(background))
    }

    /** Create a new layer.*/
    fun newLayer(bmp: Bitmap){ layers.add(0, CvLayer(bmp)) }
    fun newEmptyLayer(width:Int, height: Int){
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        layers.add(0, CvLayer(bitmap))
    }

    private fun copyBitmap(bmp: Bitmap): Bitmap{
        return bmp.copy(bmp.config, true)
    }


}