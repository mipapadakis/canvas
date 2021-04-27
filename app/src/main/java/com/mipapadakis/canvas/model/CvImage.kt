package com.mipapadakis.canvas.model

import android.graphics.Bitmap
import com.mipapadakis.canvas.model.layer.CvLayer
import com.mipapadakis.canvas.model.shape.CvShape

class CvImage(var bitmap: Bitmap, var title: String, val layers: List<CvLayer>, val shapes: List<CvShape>) {

}