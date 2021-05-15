package com.mipapadakis.canvas.model

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import com.mipapadakis.canvas.model.layer.CvLayer
import com.mipapadakis.canvas.model.shape.CvShape

/**
 * @property bitmap: contains the image created by the user, consisted of all the visible layers.
 * @param width: The width of the image. It can be changed by the user from the global settings.
 * @param height: The height of the image. It can be changed by the user from the global settings.
 * @property title: A user-generated title for the image file
 * @property layers: Contains all the layers of this CvImage. Their hierarchy is represented by
 * their position in the list, with the top layer located at position 0.
 * @property shapes: Contains a list of all the shapes created by the user in this session.
 * */

class CvImage(var width: Int, var height: Int) {
    var bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    var title = "image"
    var layers: ArrayList<CvLayer> = ArrayList()
    var shapes: ArrayList<CvShape> = ArrayList()

    constructor(bitmap: Bitmap) : this(bitmap.width, bitmap.height) {
        this.bitmap = bitmap
    }

    constructor(cvImage: CvImage): this(cvImage.width, cvImage.height){
        //TODO if needed
    }

    init {
        //there is only one layer, which contains the current bitmap of this CvImage.
        layers.add(CvLayer(bitmap))
    }

    fun newLayer(bmp: Bitmap?){
        if(bmp==null) layers.add(0, CvLayer(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)))
        else layers.add(0, CvLayer(bmp))
    }

    fun drawFreeHand(){

    }

    fun drawRect(resources: Resources): BitmapDrawable{
        val myRectPaint = Paint()
        myRectPaint.setARGB (255, 0, 0, 0)
        val x1 = 100F
        val y1 = 100F
        val x2 = 300F
        val y2 = 300F

        //Create a new image bitmap and attach a brand new canvas to it
        val tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)

        //Draw the image bitmap into the canvas
        tempCanvas.drawBitmap(bitmap, 0F, 0F, myRectPaint)

        //Draw everything else you want into the canvas, in this example a rectangle with rounded edges
        tempCanvas.drawRoundRect(RectF(x1, y1, x2, y2), 20F, 20F, myRectPaint)

        return BitmapDrawable(resources, tempBitmap)
    }
}