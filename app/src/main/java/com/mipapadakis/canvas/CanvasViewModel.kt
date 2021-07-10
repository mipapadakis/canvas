package com.mipapadakis.canvas

import android.graphics.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


/** Store here the current tool and its options.*/
class CanvasViewModel: ViewModel() {
    companion object{
        //enum class toolCode { TOOL_BRUSH, TOOL_ERASER, TOOL_BUCKET, TOOL_EYEDROPPER, TOOL_SELECT, TOOL_SHAPE, TOOL_TEXT}
        const val TOOL_BRUSH = R.drawable.baseline_brush_black_36
        const val TOOL_ERASER = R.drawable.eraser
        const val TOOL_BUCKET = R.drawable.baseline_format_color_fill_black_36
        const val TOOL_EYEDROPPER = R.drawable.baseline_colorize_black_36
        const val TOOL_SELECT = R.drawable.select_rectangular
        const val TOOL_SHAPE = R.drawable.baseline_check_box_outline_blank_black_36
        const val TOOL_TEXT = R.drawable.baseline_title_black_36

        const val SHAPE_TYPE_LINE = R.drawable.baseline_show_chart_black_36
        const val SHAPE_TYPE_SQUARE = R.drawable.baseline_check_box_outline_blank_black_36
        const val SHAPE_TYPE_RECTANGLE = R.drawable.baseline_crop_16_9_black_36
        const val SHAPE_TYPE_CIRCLE = R.drawable.baseline_panorama_fish_eye_black_36
        const val SHAPE_TYPE_OVAL = R.drawable.oval
        const val SHAPE_TYPE_POLYGON = R.drawable.baseline_star_outline_black_36
        const val SHAPE_TYPE_TRIANGLE = R.drawable.baseline_change_history_black_36
        const val SHAPE_TYPE_ARROW = R.drawable.baseline_east_black_36
        const val SHAPE_TYPE_CALLOUT = R.drawable.baseline_chat_bubble_outline_black_36

        const val SELECT_TYPE_RECTANGULAR = R.drawable.select_rectangular
        const val SELECT_TYPE_OVAL = R.drawable.select_elliptical
        const val SELECT_TYPE_LASSO = R.drawable.select_lasso
        const val SELECT_TYPE_MAGIC_WAND = 10 //TODO

        const val SELECT_METHOD_NEW = R.drawable.new_selection
        const val SELECT_METHOD_UNION = R.drawable.selection_union
        const val SELECT_METHOD_INTERSECTION = R.drawable.select_intersection
        const val SELECT_METHOD_SUBTRACTION = R.drawable.select_subtraction

//        const val PAINT_MIN_SIZE = 1f
//        const val PAINT_MAX_SIZE = 100f

        ///////////////////////////////////////////Tools////////////////////////////////////////////
        var tool = TOOL_BRUSH

        ////////////////////////////////////////Properties://///////////////////////////////////////

        //Brush
        val paint = Paint().apply {
            isAntiAlias = true
            color = CanvasPreferences.startingColorId
            alpha = 255
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND //BUTT
            strokeWidth = 20F
            style = Paint.Style.STROKE
            //pathEffect = DashPathEffect(floatArrayOf(10f,5f), 3f) //CornerPathEffect(10F)
            isDither = true
        }

        //Eraser
        val eraserPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isAntiAlias = false
            color = Color.TRANSPARENT
            alpha = 255
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND //BUTT
            strokeWidth = 20F
            style = Paint.Style.STROKE
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
            //pathEffect = DashPathEffect(floatArrayOf(10f,5f), 3f) //CornerPathEffect(10F)
        }

        //Bucket
        var bucketOpacity = 100//%

        //Select
        var selectType = SELECT_TYPE_RECTANGULAR
        var selectMethod = SELECT_METHOD_NEW

        //Shape
        var shapeType = SHAPE_TYPE_LINE
        val shapePaint= Paint().apply {
            isAntiAlias = true
            color = paint.color
            alpha = 255
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND //BUTT
            strokeWidth = 20F
            style = Paint.Style.FILL
            pathEffect = DashPathEffect(floatArrayOf(10f,5f), 3f) //null, CornerPathEffect(10f)
            //pathEffect = CornerPathEffect(10F)
            isDither = true
        }

        //Text
        var textFont = 0 //TODO
        var textFontSize = 12

        fun setBrushAndShapeColor(color: Int){
            paint.color = color
            shapePaint.color = color
        }
    }

    //var colorID = CanvasPreferences.startingColorId

//    fun setColor(color: Int){  this.color = color }
//    fun getColor() = color

    //val brushSize = CanvasPreferences.startingBrushSize

//    private val _colorId = MutableLiveData<Int>().apply { value = R.color.black }
//    val colorId: LiveData<Int> = _colorId
//    fun setColor(id: Int){  _colorId.value = id }
//    fun getCanvasColor() = CanvasColor(colorId.value ?: 0)
}