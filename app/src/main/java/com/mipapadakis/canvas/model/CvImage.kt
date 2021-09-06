package com.mipapadakis.canvas.model

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.model.layer.CvLayer
import java.io.Serializable
import java.util.*

/**This represents the canvas' list of layers that the user has created.*/

class CvImage(var title: String, var width: Int, var height: Int): ArrayList<CvLayer>() {
    var fileType = CanvasViewModel.FILETYPE_CANVAS
    private var layerNameIndex = 0

    constructor(width: Int, height: Int): this("", width, height)
    constructor(resources: Resources, title: String, bmp: Bitmap): this(title, bmp.width, bmp.height){
        addPngGridLayer(resources)
        addLayer(0, bmp)
    }
    constructor(cvImage: CvImage): this(cvImage.title, cvImage.width, cvImage.height){
        for(layer in cvImage) add(CvLayer(layer.title, layer))
    }

    fun setCvImage(cvImage: CvImage){
        title = cvImage.title
        width = cvImage.width
        height = cvImage.height
        fileType = cvImage.fileType
        clear()
        for(layer in cvImage) add(CvLayer(layer.title, layer))
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
    fun addLayer(bmp: Bitmap){ add(CvLayer(getUniqueLayerName(), bmp)) }
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

    private fun getUniqueLayerName(): String {return "Layer ${layerNameIndex++}"}
    fun getFilenameWithExtension(context: Context): String{
        return "${title}.${getExtension(context)}"
    }
    private fun getExtension(context: Context): String{
        return context.getString(
            when (fileType) {
                CanvasViewModel.FILETYPE_CANVAS -> R.string.file_extension_canvas
                CanvasViewModel.FILETYPE_PNG -> R.string.file_extension_png
                else -> R.string.file_extension_jpeg
            }
        )
    }

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

    fun toSerializable(): SerializableCvImage {
        return SerializableCvImage(this)
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

    class SerializableCvImage(cvImage: CvImage): Serializable {
        private val layerList: List<CvLayer.SerializableCvLayer>
        val title = cvImage.title
        val width = cvImage.width
        val height = cvImage.height

        init {
            val arrayList = ArrayList<CvLayer.SerializableCvLayer>()
            for(l in cvImage){
                arrayList.add(l.toSerializable())
            }
            layerList = arrayList.toList()
        }

        fun deserialize(): CvImage{
            val deserializedCvImage = CvImage(title, width, height)
            for(dl in layerList) deserializedCvImage.add(dl.deserialize())
            return deserializedCvImage
        }

    }
}