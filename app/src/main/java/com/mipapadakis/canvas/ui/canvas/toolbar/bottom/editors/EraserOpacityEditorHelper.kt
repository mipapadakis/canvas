package com.mipapadakis.canvas.ui.canvas.toolbar.bottom.editors

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.widget.SeekBar
import com.mipapadakis.canvas.R
import android.graphics.Bitmap
import com.mipapadakis.canvas.CanvasViewModel


class EraserOpacityEditorHelper {
    companion object {
        fun updateSeekbar(canvasViewModel: CanvasViewModel, seekbar: SeekBar) {
            if(seekbar.width<=0 || seekbar.height<=0) return
            if (canvasViewModel.opacityBitmapForOpacityEditor == null ||
                canvasViewModel.opacityBitmapForOpacityEditor?.width != seekbar.width ||
                canvasViewModel.opacityBitmapForOpacityEditor?.height != seekbar.height) {
                initializeOpacityBitmap(canvasViewModel, seekbar.resources, seekbar.width, seekbar.height)
            }
            //Seekbar progressDrawable: @param color, with opacityBitmap drawn on top of it.
            val bmp = Bitmap.createBitmap(seekbar.width, seekbar.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            val colorWithFullAlpha = Color.argb(255, Color.red(canvasViewModel.getColor()), Color.green(canvasViewModel.getColor()), Color.blue(canvasViewModel.getColor()))
            canvas.drawColor(colorWithFullAlpha)
            canvas.drawBitmap(canvasViewModel.opacityBitmapForOpacityEditor!!, 0f, 0f, null)
            seekbar.progressDrawable = BitmapDrawable(seekbar.resources, bmp)
        }

        private fun initializeOpacityBitmap(canvasViewModel: CanvasViewModel, resources: Resources, width: Int, height: Int) {
            if(width<=0 || height<=0) return
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val pngBackgroundPattern = BitmapFactory.decodeResource(resources, R.drawable.png_background_pattern)
            val paint = Paint().apply {
                alpha = 0
                shader = BitmapShader( pngBackgroundPattern, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT) }
            val canvas = Canvas(bmp)
            val step = 255/width.toDouble()
            for (i in 0 until width) {
                canvas.drawRect(i.toFloat(), 0f, i+1f, height.toFloat(), paint)
                paint.alpha = ((width-i)*step).toInt()
            }
            canvasViewModel.opacityBitmapForOpacityEditor = Bitmap.createScaledBitmap(bmp, width, height, false)
        }
    }
}