package com.mipapadakis.canvas

import androidx.lifecycle.ViewModel
import com.mipapadakis.canvas.ui.CanvasColor
import com.mipapadakis.canvas.R

const val SIZE_TINY = 0
const val SIZE_SMALL = 1
const val SIZE_NORMAL = 2
const val SIZE_BIG = 3
const val SIZE_LARGE = 4

/** Store here the current tool and its options.*/
class CanvasViewModel: ViewModel() {
    var color = CanvasColor(R.color.black)
    val size = SIZE_NORMAL
}