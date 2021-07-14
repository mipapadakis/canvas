package com.mipapadakis.canvas.model.layer.shape

import android.graphics.Bitmap
/** This represents a shape drawn by the user.
 * @property bitmap: the bitmap of the shape.
 * @property shapeType: An int representing this shape's type. (e.g. CanvasViewModel.SHAPE_SQUARE)
 * */
class CvShape(val shapeType: Int, var bitmap: Bitmap) {}