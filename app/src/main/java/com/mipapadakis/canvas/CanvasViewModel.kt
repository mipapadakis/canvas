package com.mipapadakis.canvas

import android.graphics.CornerPathEffect
import android.graphics.Paint
import androidx.lifecycle.ViewModel
import com.mipapadakis.canvas.ui.CanvasColor


/** Store here the current tool and its options.*/
class CanvasViewModel: ViewModel() {
    companion object{
        //enum class toolCode { TOOL_BRUSH, TOOL_ERASER, TOOL_BUCKET, TOOL_EYEDROPPER, TOOL_SELECT, TOOL_SHAPE, TOOL_TEXT}
        const val TOOL_BRUSH = 0
        const val TOOL_ERASER = 1
        const val TOOL_BUCKET = 2
        const val TOOL_EYEDROPPER = 3
        const val TOOL_SELECT = 4
        const val TOOL_SHAPE = 5
        const val TOOL_TEXT = 6
        const val SHAPE_LINE = 10
        const val SHAPE_SQUARE = 11
        const val SHAPE_RECTANGLE = 12
        const val SHAPE_CIRCLE = 13
        const val SHAPE_OVAL = 14
        const val SHAPE_POLYGON = 15
        const val SHAPE_TRIANGLE = 16
        const val SHAPE_ARROW = 17
        const val SHAPE_CALLOUT = 18
        const val PAINT_MIN_SIZE = 1f
        const val PAINT_MAX_SIZE = 100f

        //        var colorID = CanvasPreferences.startingColorId
//        val brushSize = CanvasPreferences.startingBrushSize
        val paint = Paint().apply {
            isAntiAlias = true
            color = CanvasPreferences.startingColorId
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 20F
            style = Paint.Style.STROKE
            pathEffect = CornerPathEffect(10F)
            isDither = true
        }
        var tool = TOOL_BRUSH
        var shape = SHAPE_LINE
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