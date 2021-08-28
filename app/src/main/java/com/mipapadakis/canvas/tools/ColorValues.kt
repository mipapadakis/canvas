package com.mipapadakis.canvas.tools

import android.graphics.Color

class ColorValues {
    companion object{
        fun colorToArgbArray(color: Int) = arrayOf(Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color))
        fun colorToRgbArray(color: Int) = arrayOf(Color.red(color), Color.green(color), Color.blue(color))
        fun colorToHsbArray(color: Int): FloatArray{
            val array = FloatArray(3)
            Color.RGBToHSV(getR(color), getG(color), getB(color), array)
            return array
        }
        fun colorOnlyHue(color: Int): Int{
            val hsbArray = colorToHsbArray(color)
            hsbArray[1] = 1f
            hsbArray[2] = 1f
            return Color.HSVToColor(hsbArray)
        }
        fun argbToColor(array: Array<Int>) = Color.argb(array[0], array[1], array[2], array[3])
        fun getA(color: Int) = Color.alpha(color)
        fun getR(color: Int) = Color.red(color)
        fun getG(color: Int) = Color.green(color)
        fun getB(color: Int) = Color.blue(color)
        fun getHue(color: Int) = colorToHsbArray(color)[0]        //Hue: [0...360]
        fun getSaturation(color: Int) = colorToHsbArray(color)[1] //Saturation: [0...1]
        fun getBrightness(color: Int) = colorToHsbArray(color)[2] //Brightness: [0...1]
    }
}