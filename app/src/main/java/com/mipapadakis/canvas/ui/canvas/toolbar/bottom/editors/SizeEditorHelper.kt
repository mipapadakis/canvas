package com.mipapadakis.canvas.ui.canvas.toolbar.bottom.editors

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.widget.SeekBar
import com.mipapadakis.canvas.R
import android.graphics.Bitmap


class SizeEditorHelper {
    companion object {
        private lateinit var increasingWidthBitmap: Bitmap //transparent -> progressbar increasing in height -> black

        fun updateSeekbar(seekbar: SeekBar) {
            if(seekbar.width<=0 || seekbar.height<=0) return
            if (!Companion::increasingWidthBitmap.isInitialized)  initializeIncreasingWidthBitmap(seekbar)
            seekbar.progressDrawable = BitmapDrawable(seekbar.resources, increasingWidthBitmap)
        }

        private fun initializeIncreasingWidthBitmap(seekbar: SeekBar){
            if(seekbar.width<=0 || seekbar.height<=0) return
            val bmp = BitmapFactory.decodeResource(seekbar.resources, R.drawable.increasing_width_progress)
            increasingWidthBitmap = Bitmap.createScaledBitmap(bmp, seekbar.width, seekbar.height, false)
        }
    }
}