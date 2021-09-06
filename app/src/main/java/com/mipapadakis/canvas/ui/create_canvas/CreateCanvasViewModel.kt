package com.mipapadakis.canvas.ui.create_canvas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class CreateCanvasViewModel : ViewModel() {
    val UNIT_PIXEL = 0
    val UNIT_MM = 1
    val UNIT_INCH = 2
    val UNCOMMITTED = 100
    val COMMITTED = 200
    var lastDpiUnitUsed = UNIT_MM
    var change = COMMITTED
    var unitPixels = arrayListOf(1000,1000)
    var unitMillimeters = arrayListOf(250.0, 250.0)
    var unitInches = arrayListOf(unitMillimeters[0]/25.4, unitMillimeters[1]/25.4)
    var dpi = 350

    private val _importImagePreview = MutableLiveData<String>().apply { value = "" }
    val importImagePreview: LiveData<String> = _importImagePreview
    fun setImportImagePreview(uri: String){ _importImagePreview.value = uri }

    private val _customUnit = MutableLiveData<Int>().apply { value = UNIT_PIXEL }
    val customUnit: LiveData<Int> = _customUnit
    fun setCustomUnit(unit: Int){  _customUnit.value = unit }
}




















