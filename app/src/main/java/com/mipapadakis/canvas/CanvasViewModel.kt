package com.mipapadakis.canvas

import android.graphics.Paint
import androidx.lifecycle.ViewModel
import com.mipapadakis.canvas.ui.CanvasColor


/** Store here the current tool and its options.*/
class CanvasViewModel: ViewModel() {
    companion object{
//        var colorID = CanvasPreferences.startingColorId
//        val brushSize = CanvasPreferences.startingBrushSize
        val paint = Paint().apply {
            isAntiAlias = true
            color = CanvasPreferences.startingColorId
            strokeWidth = 20F
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
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