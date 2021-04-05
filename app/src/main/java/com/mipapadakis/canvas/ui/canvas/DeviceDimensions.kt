package com.mipapadakis.canvas.ui.canvas

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.util.TypedValue


class DeviceDimensions {
    companion object{
        fun getWidth(context: Context): Int{
            val displayMetrics = context.resources.displayMetrics
            return displayMetrics.widthPixels
        }
        fun getHeight(context: Context): Int{
            val displayMetrics = context.resources.displayMetrics
            return displayMetrics.heightPixels
        }
        fun getCenter(context: Context): Point {
            val point = Point()
            point.x = getWidth(context)/2
            point.y = getHeight(context)/2
            return point
        }
        fun dpToPixels(context: Context, dp: Float): Float{
            val r: Resources = context.resources
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.displayMetrics)
        }
        fun pixelsToDp(context: Context, px: Float): Float{
            val r = context.resources
            val metrics = r.displayMetrics
            return px / (metrics.densityDpi / 160f)
        }
    }
}