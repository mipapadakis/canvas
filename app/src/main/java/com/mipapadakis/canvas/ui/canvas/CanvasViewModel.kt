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
    var lastDpiUnitUsed = UNIT_MM
    var change = COMMITTED
    var unitPixels = arrayListOf(1000,1000)
    var unitMillimeters = arrayListOf(1.0, 1.0)
    var unitInches = arrayListOf(1/25.4, 1/25.4)
    var dpi = 350

    private val _importImagePreview = MutableLiveData<String>().apply { value = "" }
    val importImagePreview: LiveData<String> = _importImagePreview
    fun setImportImagePreview(uri: String){ _importImagePreview.value = uri }

    private val _customUnit = MutableLiveData<Int>().apply { value = UNIT_PIXEL }
    val customUnit: LiveData<Int> = _customUnit
    fun setCustomUnit(unit: Int){  _customUnit.value = unit }
}




















