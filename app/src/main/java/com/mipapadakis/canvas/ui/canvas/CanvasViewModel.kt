package com.mipapadakis.canvas.ui.canvas

import android.renderscript.Element.DataType
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class CanvasViewModel : ViewModel() {
    val UNIT_PIXEL = 0
    val UNIT_MM = 1
    val UNIT_INCH = 2
    val UNCOMMITTED = 100
    val COMMITTED = 200
    var lastDpiUnitUsed = UNIT_PIXEL
    var change = COMMITTED

    private val _text = MutableLiveData<String>().apply {
        value = "This is canvas Fragment"
    }
    val text: LiveData<String> = _text
    fun setText(text: String){
        _text.value = text
    }

    private val _customUnit = MutableLiveData<Int>().apply { value = UNIT_PIXEL }
    val customUnit: LiveData<Int> = _customUnit
    fun setCustomUnit(unit: Int){  _customUnit.value = unit }

    private val _pixelWidth = MutableLiveData<Int>().apply { value = 1000 }
    val pixelWidth: LiveData<Int> = _pixelWidth
    fun setPixelWidth(width: Int){ _pixelWidth.value = width }

    private val _pixelHeight = MutableLiveData<Int>().apply { value = 1000 }
    val pixelHeight: LiveData<Int> = _pixelHeight
    fun setPixelHeight(height: Int){ _pixelHeight.value = height }

    private val _dpiWidth = MutableLiveData<Double>().apply {  value = 1.00 }
    val dpiWidth: LiveData<Double> = _dpiWidth
    fun setDpiWidth(width: Double){ _dpiWidth.value = width }

    private val _dpiHeight = MutableLiveData<Double>().apply { value = 1.00 }
    val dpiHeight: LiveData<Double> = _dpiHeight
    fun setDpiHeight(height: Double){ _dpiHeight.value = height }

    private val _dpi = MutableLiveData<Int>().apply { value = 350 }
    val dpi: LiveData<Int> = _dpi
    fun setDpi(dpi: Int){ _dpi.value = dpi }
}