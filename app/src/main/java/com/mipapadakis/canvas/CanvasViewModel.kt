package com.mipapadakis.canvas

import android.content.res.Resources
import android.graphics.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mipapadakis.canvas.model.CvImage
import com.mipapadakis.canvas.ui.util.ColorValues
import com.mipapadakis.canvas.ui.canvas.CanvasImageView
import java.util.*

private const val PENCIL_WIDTH = 1f
private const val PATTERN_MINIMUM_DIMENSION = 100

class CanvasViewModel: ViewModel() {
    private val _toolbarColor = MutableLiveData<Int>().apply { value = 0 }
    val toolbarColor: LiveData<Int> = _toolbarColor

    //Color Editor
    private val _colorEditorTempColor = MutableLiveData<Int>()
    val colorEditorTempColor: LiveData<Int> = _colorEditorTempColor

    ////////////////////////////////////////Properties://///////////////////////////////////////
    var cvImage = CvImage(0,0) //TODO observer
    var history = ArrayList<CanvasImageView.Action>() //TODO if history.size too large, remove some of the initial actions //TODO observer
    var historyIndex = 0
    //val bottomToolbarState = BottomToolbarState.TOOL //TODO observer
    //val toolbarPosition = 0 // Represents position of toolbar relative to screen height.  TODO observer
    //val toolbarsVisible = true // Represents whether the toolbars are shown or hidden.  TODO observer
    //val canvasState: scale, rotation and position of canvas

    var newColor = CanvasPreferences.startingColor
    var newColorHue = ColorValues.colorOnlyHue(CanvasPreferences.startingColor)
    var textToolText = ""
    var allColors: Array<Array<Int>>? = null //Colors of rainbow (in rgb values)
    var colorTableBitmap: Bitmap? = null //Rainbow
    var brightnessBitmap: Bitmap? = null //white -> transparent -> black
    var opacityBitmapForColorEditor: Bitmap? = null //png_grid -> transparent
    var opacityBitmapForOpacityEditor: Bitmap? = null //png_grid -> transparent

    ///////////////////////////////////////////Tools////////////////////////////////////////////
    var tool = TOOL_BRUSH //TODO observer
    //Brush
    var brushType = BRUSH_TYPE_BRUSH //TODO observer
    private var lastStrokeWidth = 20f //In case we switch to pencil -> store here the last width
    val paint = Paint().apply {
        isAntiAlias = true
        color = CanvasPreferences.startingColor
        alpha = 255
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND //BUTT
        strokeWidth = 20f
        style = Paint.Style.STROKE
        isDither = true
    }
    //Eraser
    val eraserPaint = Paint().apply {
        isAntiAlias = false
        color = Color.TRANSPARENT
        alpha = 255
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND //BUTT
        strokeWidth = 20F
        style = Paint.Style.STROKE
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }
    //Bucket
    val bucketPaint = Paint().apply {
        color = CanvasPreferences.startingColor
        alpha = 255
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND //BUTT
        style = Paint.Style.STROKE
        isDither = true
    }
    //Select
    ////var selectType = SELECT_TYPE_RECTANGULAR
    ////var selectMethod = SELECT_METHOD_NEW
    //Shape
    var shapeType = SHAPE_TYPE_LINE //TODO observer
    val shapePaint= Paint().apply {
        isAntiAlias = true
        color = paint.color
        alpha = 255
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND //BUTT
        strokeWidth = 20F
        style = Paint.Style.FILL //STROKE //FILL
        pathEffect = DashPathEffect(floatArrayOf(10f,5f), 3f) //null, CornerPathEffect(10f)
        //pathEffect = CornerPathEffect(10F)
        isDither = true
    }
    //Text
    val textPaint = Paint().apply {
        isAntiAlias = true
        color = CanvasPreferences.startingColor
        alpha = 255
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND //BUTT
        strokeWidth = 10f
        textSize = 60f
        style = Paint.Style.FILL
        isDither = true
    }

    //////////////////////////////////////////Methods://////////////////////////////////////////
    fun getColor() = _toolbarColor.value ?: CanvasPreferences.startingColor
    fun setPaintColor(color: Int){
        paint.color = color
        bucketPaint.color = color
        shapePaint.color = color
        textPaint.color = color
        _toolbarColor.value = color
    }
    fun setTemporaryColor(color: Int){ //While in Color Editor
        newColor = color
        //newColorHue = ColorValues.colorOnlyHue(color)
        _colorEditorTempColor.value = color
    }
    fun setBrushSize(resources: Resources, size: Float){
        lastStrokeWidth = size
        if(brushType == BRUSH_TYPE_BRUSH) paint.strokeWidth = size
        else if(brushType!= BRUSH_TYPE_PENCIL){
            setBrushType(resources, brushType) //update pattern size
        }
    }
    fun getBrushSize(): Float{
        return if(brushType== BRUSH_TYPE_PENCIL) PENCIL_WIDTH else lastStrokeWidth
    }
    fun setBrushOrPencilType(type: Int){
        brushType = type
        when(type){
            BRUSH_TYPE_BRUSH ->{
                paint.apply {
                    isAntiAlias = true
                    strokeJoin = Paint.Join.ROUND //The outer edges of a join meet in a circular arc.
                    strokeCap = Paint.Cap.ROUND //The stroke projects out as a semicircle, with the center at the end of the path.
                    style = Paint.Style.STROKE
                    strokeWidth = lastStrokeWidth
                    shader = null
                    isDither = true
                }
            }
            BRUSH_TYPE_PENCIL ->{
                lastStrokeWidth = paint.strokeWidth
                paint.apply {
                    isAntiAlias = false
                    strokeJoin = Paint.Join.MITER //The outer edges of a join meet at a sharp angle
                    //strokeJoin = Paint.Join.BEVEL //The outer edges of a join meet with a straight line
                    strokeCap = Paint.Cap.BUTT //The stroke ends with the path, and does not project beyond it.
                    //strokeCap = Paint.Cap.SQUARE //The stroke projects out as a square, with the center at the end of the path.
                    style = Paint.Style.STROKE
                    strokeWidth = PENCIL_WIDTH
                    shader = null
                    isDither = true
                }
            }
        }
    }
    fun setBrushType(resources: Resources, type: Int){
        brushType = type
        when(type){
            BRUSH_TYPE_BRUSH -> setBrushOrPencilType(brushType)
            BRUSH_TYPE_PENCIL -> setBrushOrPencilType(brushType)
            else -> {
                val pattern = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(resources, type),
                    lastStrokeWidth.toInt() + PATTERN_MINIMUM_DIMENSION,
                    lastStrokeWidth.toInt()+ PATTERN_MINIMUM_DIMENSION, false)

                paint.apply {
                    isAntiAlias = true
                    strokeJoin = Paint.Join.ROUND
                    strokeCap = Paint.Cap.ROUND
                    style = Paint.Style.STROKE
                    shader = BitmapShader( pattern, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
                    strokeWidth = 50f
                    isDither = true
                }
            }
        }
    }

    companion object{
        const val FILETYPE_CANVAS = 0
        const val FILETYPE_PNG = 1
        const val FILETYPE_JPEG = 2

        enum class BottomToolbarState { HIDDEN, TOOL, LAYER_LIST, TRANSFORM, SAVE, SAVE_ON_EXIT}

        const val TOOL_BRUSH = R.drawable.brush_outlined
        const val TOOL_ERASER = R.drawable.eraser_outlined
        const val TOOL_BUCKET = R.drawable.bucket_outlined
        const val TOOL_EYEDROPPER = R.drawable.eyedropper_outlined
        const val TOOL_SELECT = R.drawable.select_rectangular
        const val TOOL_SHAPE = R.drawable.check_box_empty_outlined
        const val TOOL_TEXT = R.drawable.text_outlined

        const val BRUSH_TYPE_BRUSH = R.drawable.brush_outlined
        const val BRUSH_TYPE_PENCIL = R.drawable.pencil_outlined
        const val BRUSH_TYPE_PATTERN_1 = R.drawable.logo
        //Icons found at https://uxwing.com/ //TODO make my own
        const val BRUSH_TYPE_PATTERN_2 = R.drawable.pattern_2
        const val BRUSH_TYPE_PATTERN_3 = R.drawable.pattern_3
        const val BRUSH_TYPE_PATTERN_4 = R.drawable.pattern_4
        const val BRUSH_TYPE_PATTERN_5 = R.drawable.pattern_5

        const val SHAPE_TYPE_LINE = R.drawable.line_outlined
        const val SHAPE_TYPE_SQUARE = R.drawable.check_box_empty_outlined
        const val SHAPE_TYPE_RECTANGLE = R.drawable.rectangle_outlined
        const val SHAPE_TYPE_CIRCLE = R.drawable.circle_outlined
        const val SHAPE_TYPE_OVAL = R.drawable.oval
        const val SHAPE_TYPE_POLYGON = R.drawable.star_outlined
        const val SHAPE_TYPE_TRIANGLE = R.drawable.triangle_outlined
        const val SHAPE_TYPE_ARROW = R.drawable.arrow_outlined
        const val SHAPE_TYPE_CALLOUT = R.drawable.callout_outlined

//        const val SELECT_TYPE_RECTANGULAR = R.drawable.select_rectangular
//        const val SELECT_TYPE_OVAL = R.drawable.select_elliptical
//        const val SELECT_TYPE_LASSO = R.drawable.select_lasso_outlined
//        const val SELECT_TYPE_MAGIC_WAND = 10 //TODO
//
//        const val SELECT_METHOD_NEW = R.drawable.new_selection
//        const val SELECT_METHOD_UNION = R.drawable.selection_union
//        const val SELECT_METHOD_INTERSECTION = R.drawable.select_intersection
//        const val SELECT_METHOD_SUBTRACTION = R.drawable.select_subtraction

        var importedCvImage: CvImage? = null
    }
}