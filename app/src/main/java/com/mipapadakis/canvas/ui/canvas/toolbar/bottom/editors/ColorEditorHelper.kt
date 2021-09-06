package com.mipapadakis.canvas.ui.canvas.toolbar.bottom.editors

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.text.Editable
import android.widget.*
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.R
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LifecycleOwner
import com.mipapadakis.canvas.ui.util.ColorValues

private enum class UserChanged {NOTHING, RGB_SEEKBAR, BRIGHTNESS_SEEKBAR, OPACITY_SEEKBAR, EDIT_TEXT_A, EDIT_TEXT_RGB}
private enum class Change{COMMITTED, UNCOMMITTED}
const val SEEKBAR_RGB_MAX = 1536 //6*256
//const val SEEKBAR_BRIGHTNESS_MAX = 199
//const val SEEKBAR_OPACITY_MAX = 255
//const val SEEKBAR_SIZE_MAX = 100
private const val Red = 0
private const val Green = 1
private const val Blue = 2

class ColorEditorHelper(owner: LifecycleOwner, val canvasViewModel: CanvasViewModel, toolColorEditor: LinearLayout, val hideAllEditors: () -> Unit){
    private var change = Change.COMMITTED
    private var state = UserChanged.NOTHING
    private var editorTemporaryColorImageView = toolColorEditor.findViewById<ImageView>(R.id.color_editor_temporary_color)
    private var editorColorRGBSeekBar = toolColorEditor.findViewById<SeekBar>(R.id.color_editor_rgb_seekbar)//[0,1536]
    private var editorColorAlpha = toolColorEditor.findViewById<EditText>(R.id.argb_alpha)
    private var editorColorRed = toolColorEditor.findViewById<EditText>(R.id.argb_red)
    private var editorColorGreen = toolColorEditor.findViewById<EditText>(R.id.argb_green)
    private var editorColorBlue = toolColorEditor.findViewById<EditText>(R.id.argb_blue)
    private var editorColorBrightnessSeekbar = toolColorEditor.findViewById<SeekBar>(R.id.color_editor_brightness_seekbar) //[0,200]
    private var editorColorOpacitySeekbar = toolColorEditor.findViewById<SeekBar>(R.id.color_editor_opacity_seekbar) //[0,255]

    init {
        canvasViewModel.newColorHue = ColorValues.colorOnlyHue(canvasViewModel.getColor())
        canvasViewModel.setTemporaryColor(canvasViewModel.getColor())
        hideAllEditors()

        canvasViewModel.colorEditorTempColor.observe(owner, {
            beginChange() //Avoid unwanted loops
            editorTemporaryColorImageView.setBackgroundColor(canvasViewModel.newColor)
            if(state!= UserChanged.BRIGHTNESS_SEEKBAR && state!= UserChanged.OPACITY_SEEKBAR && state!= UserChanged.EDIT_TEXT_A){ //Hue has changed
                initializeColorRGBSeekbar(editorColorRGBSeekBar)
                updateColorBrightnessSeekbar(editorColorBrightnessSeekbar, canvasViewModel.newColorHue)
                updateColorOpacitySeekbar(editorColorOpacitySeekbar, canvasViewModel.newColorHue)
            }
            if(state != UserChanged.RGB_SEEKBAR)
                editorColorRGBSeekBar.progress = getProgressOfRGBSeekbarFromColor(canvasViewModel.newColorHue)
            if(state != UserChanged.BRIGHTNESS_SEEKBAR)
                editorColorBrightnessSeekbar.progress = getProgressOfBrightnessSeekbarFromColor(canvasViewModel.newColor)
            if(state != UserChanged.OPACITY_SEEKBAR)
                editorColorOpacitySeekbar.progress = getProgressOfOpacitySeekbarFromColor(canvasViewModel.newColor)
            if(state != UserChanged.EDIT_TEXT_A){
                editorColorAlpha.setText(Color.alpha(canvasViewModel.newColor).toString())
                moveCursorToEndOfEditText(editorColorAlpha)
            }
            if(state != UserChanged.EDIT_TEXT_RGB){
                editorColorRed.setText(Color.red(canvasViewModel.newColor).toString())
                editorColorGreen.setText(Color.green(canvasViewModel.newColor).toString())
                editorColorBlue.setText(Color.blue(canvasViewModel.newColor).toString())
                moveCursorToEndOfEditText(editorColorRed)
                moveCursorToEndOfEditText(editorColorGreen)
                moveCursorToEndOfEditText(editorColorBlue)
            }
            state = UserChanged.NOTHING
            commitChange()
        })
        setupEditors(canvasViewModel)
    }

    private fun setupEditors(canvasViewModel: CanvasViewModel) {
        //ImageView for previewing the new color
        editorTemporaryColorImageView.setOnClickListener {
            canvasViewModel.paint.alpha = Color.alpha(canvasViewModel.newColor)
            canvasViewModel.setPaintColor(canvasViewModel.newColor)
            hideAllEditors()
        }

        //ColorRgbSeekbar [0,1536]
        editorColorRGBSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(!fromUser || isUnderChange()) return
                state = UserChanged.RGB_SEEKBAR
                val color = getColorFromRGBSeekbarProgress(progress)
                canvasViewModel.newColorHue = ColorValues.colorOnlyHue(color)
                canvasViewModel.setTemporaryColor(color)
            }
        })

        //ColorBrightnessSeekbar [0,100]
        editorColorBrightnessSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(!fromUser || isUnderChange()) return
                state = UserChanged.BRIGHTNESS_SEEKBAR
                val color = getColorFromBrightnessSeekbarProgress(progress)
                canvasViewModel.setTemporaryColor(color)
            }
        })

        //ColorOpacitySeekbar [0,255]
        editorColorOpacitySeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(!fromUser || isUnderChange()) return
                state = UserChanged.OPACITY_SEEKBAR
                val color = getColorFromOpacitySeekbarProgress(progress)
                canvasViewModel.setTemporaryColor(color)
            }
        })

        editorColorAlpha.addTextChangedListener {
            state = UserChanged.EDIT_TEXT_A
            if(!isUnderChange()) canvasViewModel.setTemporaryColor(getFixedNewColorFromEditTexts())
        }
        editorColorRed.addTextChangedListener {
            state = UserChanged.EDIT_TEXT_RGB
            if(!isUnderChange()) {
                val color = getFixedNewColorFromEditTexts()
                canvasViewModel.newColorHue = ColorValues.colorOnlyHue(color)
                canvasViewModel.setTemporaryColor(color)
            }
        }
        editorColorGreen.addTextChangedListener {
            state = UserChanged.EDIT_TEXT_RGB
            if(!isUnderChange()) {
                val color = getFixedNewColorFromEditTexts()
                canvasViewModel.newColorHue = ColorValues.colorOnlyHue(color)
                canvasViewModel.setTemporaryColor(color)
            }
        }
        editorColorBlue.addTextChangedListener {
            state = UserChanged.EDIT_TEXT_RGB
            if(!isUnderChange()) {
                val color = getFixedNewColorFromEditTexts()
                canvasViewModel.newColorHue = ColorValues.colorOnlyHue(color)
                canvasViewModel.setTemporaryColor(color)
            }
        }
    }

    private fun getFixedNewColorFromEditTexts(): Int{
        val argb = arrayOf(
            if(!editorColorAlpha.text.isDigits()) 0 else editorColorAlpha.text.toString().toInt(),
            if(!editorColorRed.text.isDigits()) 0 else editorColorRed.text.toString().toInt(),
            if(!editorColorGreen.text.isDigits()) 0 else editorColorGreen.text.toString().toInt(),
            if(!editorColorBlue.text.isDigits()) 0 else editorColorBlue.text.toString().toInt())
        return Color.argb(
            if(argb[0]<0) 0 else if(argb[0]>255) 255 else argb[0],
            if(argb[1]<0) 0 else if(argb[1]>255) 255 else argb[1],
            if(argb[2]<0) 0 else if(argb[2]>255) 255 else argb[2],
            if(argb[3]<0) 0 else if(argb[3]>255) 255 else argb[3])
    }

    private fun Editable?.isDigits(): Boolean{
        try{this.toString().toInt()}
        catch(e: NumberFormatException){return false}
        return true //!(this != null && this.toString().isNotEmpty() && this.toString().isDigitsOnly())
    }

    private fun moveCursorToEndOfEditText(editText: EditText){
        if(editText.text!=null && editText.text.isNotEmpty())
            editText.setSelection(editText.text.length)
    }

    private fun beginChange(){change = Change.UNCOMMITTED
    }
    private fun commitChange() {change = Change.COMMITTED
    }
    private fun isUnderChange() = change == Change.UNCOMMITTED

    private fun updateColorBrightnessSeekbar(seekbar: SeekBar, color: Int) {
        if(seekbar.width<=0 || seekbar.height<=0) return
        if (canvasViewModel.brightnessBitmap == null ||
            canvasViewModel.brightnessBitmap?.width != seekbar.width ||
            canvasViewModel.brightnessBitmap?.height != seekbar.height) {
            initializeBrightnessBitmap(seekbar.width, seekbar.height)
        }
        //Seekbar progressDrawable: @param color, with brightnessBitmap drawn on top of it.
        val bmp = Bitmap.createBitmap(seekbar.width, seekbar.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val colorWithFullAlpha = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color))
        canvas.drawColor(colorWithFullAlpha)
        canvas.drawBitmap(canvasViewModel.brightnessBitmap!!, 0f, 0f, null)
        seekbar.progressDrawable = BitmapDrawable(seekbar.resources, bmp)
    }

    private fun updateColorOpacitySeekbar(seekbar: SeekBar, color: Int) {
        if(seekbar.width<=0 || seekbar.height<=0) return
        if (canvasViewModel.opacityBitmapForColorEditor == null ||
            canvasViewModel.opacityBitmapForColorEditor?.width != seekbar.width ||
            canvasViewModel.opacityBitmapForColorEditor?.height != seekbar.height) {
            initializeOpacityBitmap(seekbar.resources, seekbar.width, seekbar.height)
        }
        //Seekbar progressDrawable: @param color, with opacityBitmap drawn on top of it.
        val bmp = Bitmap.createBitmap(seekbar.width, seekbar.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val colorWithFullAlpha = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color))
        canvas.drawColor(colorWithFullAlpha)
        canvas.drawBitmap(canvasViewModel.opacityBitmapForColorEditor!!, 0f, 0f, null)
        seekbar.progressDrawable = BitmapDrawable(seekbar.resources, bmp)
    }

    fun getColorFromRGBSeekbarProgress(progress: Int): Int {
        if (canvasViewModel.allColors ==null) initializeAllColorsArray()
        val selectedColorRgb = canvasViewModel.allColors!![progress]
        return Color.argb(Color.alpha(canvasViewModel.newColor), selectedColorRgb[Red], selectedColorRgb[Green], selectedColorRgb[Blue])
    }

    fun getColorFromBrightnessSeekbarProgress(progress: Int): Int {
        val hsbColor = ColorValues.colorToHsbArray(canvasViewModel.newColorHue)
        if(progress>99) hsbColor[2] = (199 - progress.toFloat())/100
        else hsbColor[1] = progress.toFloat()/100
        val rgbColor = Color.HSVToColor(hsbColor)
        return Color.argb(
            Color.alpha(canvasViewModel.newColor),
            Color.red(rgbColor),
            Color.green(rgbColor),
            Color.blue(rgbColor))
    }

    fun getColorFromOpacitySeekbarProgress(progress: Int): Int {
        val rgb = ColorValues.colorToRgbArray(canvasViewModel.newColor)
        return Color.argb(progress, rgb[0], rgb[1], rgb[2])
    }

    private fun getProgressOfRGBSeekbarFromColor(hueColor: Int): Int{
        if (canvasViewModel.allColors ==null) initializeAllColorsArray()
        for(i in canvasViewModel.allColors!!.indices)
            if(canvasViewModel.allColors!![i].contentEquals(ColorValues.colorToRgbArray(hueColor))) return i
        return 0 //something went wrong
    }

    private fun getProgressOfBrightnessSeekbarFromColor(color: Int): Int{
        val hsbColor = ColorValues.colorToHsbArray(color)
        if(hsbColor[1]<hsbColor[2]) return (hsbColor[1]*100).toInt()
        return 99+((1-hsbColor[2])*100).toInt()
    }

    private fun getProgressOfOpacitySeekbarFromColor(color: Int) = Color.alpha(color)

    private fun initializeColorRGBSeekbar(seekbar: SeekBar){
        if(seekbar.width<=0 || seekbar.height<=0) return
        if (canvasViewModel.allColors == null) initializeAllColorsArray()
        if (canvasViewModel.colorTableBitmap == null ||
            canvasViewModel.colorTableBitmap?.width != seekbar.width ||
            canvasViewModel.colorTableBitmap?.height != seekbar.height) {
            val bmp = Bitmap.createBitmap(SEEKBAR_RGB_MAX, 1, Bitmap.Config.ARGB_8888)
            val pixelArray = IntArray(SEEKBAR_RGB_MAX * 1)
            bmp.getPixels(pixelArray, 0, SEEKBAR_RGB_MAX, 0, 0, SEEKBAR_RGB_MAX, 1)
            for (i in 0 until SEEKBAR_RGB_MAX) {
                pixelArray[i] = Color.rgb(canvasViewModel.allColors!![i][Red], canvasViewModel.allColors!![i][Green], canvasViewModel.allColors!![i][Blue])
            }
            bmp.setPixels(pixelArray, 0, SEEKBAR_RGB_MAX, 0, 0, SEEKBAR_RGB_MAX, 1)
            canvasViewModel.colorTableBitmap = Bitmap.createScaledBitmap(bmp, seekbar.width, seekbar.height, false)
            //Seekbar progressDrawable: has the colorTableBitmap drawn on top of it.
        }
        seekbar.progressDrawable = BitmapDrawable(seekbar.resources, canvasViewModel.colorTableBitmap)
    }

    private fun initializeAllColorsArray() {
        canvasViewModel.allColors = Array(SEEKBAR_RGB_MAX +1){Array(3){0}}
        for(i in 0 until 256){
            canvasViewModel.allColors!![i][Red] = 255
            canvasViewModel.allColors!![i][Green] = i //0 up to 255
        }
        var counter = 255
        for(i in 256 until 2*256){
            canvasViewModel.allColors!![i][Red] = counter-- //255 down to 0
            canvasViewModel.allColors!![i][Green] = 255
        }
        counter=0
        for(i in 2*256 until 3*256){
            canvasViewModel.allColors!![i][Green] = 255
            canvasViewModel.allColors!![i][Blue] = counter++ //0 up to 255
        }
        counter = 255
        for(i in 3*256 until 4*256){
            canvasViewModel.allColors!![i][Green] = counter--
            canvasViewModel.allColors!![i][Blue] = 255
        }
        counter = 0
        for(i in 4*256 until 5*256){
            canvasViewModel.allColors!![i][Red] = counter++
            canvasViewModel.allColors!![i][Blue] = 255
        }
        counter = 256
        for(i in 5*256 until 6*256+1){
            canvasViewModel.allColors!![i][Red] = 255
            canvasViewModel.allColors!![i][Blue] = counter--
        }
    }

    private fun initializeBrightnessBitmap(width: Int, height: Int) {
        if(width<=0 || height<=0) return
        val bmp = Bitmap.createBitmap(width, 1, Bitmap.Config.ARGB_8888)
        val step1 = 255/(width/2).toDouble()
        val step2 = 255/(width/2-1).toDouble()
        var opacity = 255
        var rgb = 255

        val pixelArray = IntArray(width * 1)
        bmp.getPixels(pixelArray, 0, width, 0, 0, width, 1)
        for (i in 0 until width) {
            pixelArray[i] = Color.argb(opacity, rgb, rgb, rgb)
            rgb = 255*(width-i)/width
            opacity = if(i<width/2) (255 - step1*i).toInt()
            else (step2*(i-width/2)).toInt()
        }
        bmp.setPixels(pixelArray, 0, width, 0, 0, width, 1)
        canvasViewModel.brightnessBitmap = Bitmap.createScaledBitmap(bmp, width, height, false)
    }

    private fun initializeOpacityBitmap(resources: Resources, width: Int, height: Int) {
        if(width<=0 || height<=0) return
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pngBackgroundPattern = BitmapFactory.decodeResource(resources, R.drawable.png_background_pattern)
        val paint = Paint().apply { shader = BitmapShader( pngBackgroundPattern, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT) }
        val canvas = Canvas(bmp)
        val step = 255/width.toDouble()
        for (i in 0 until width) {
            canvas.drawRect(i.toFloat(), 0f, i+1f, height.toFloat(), paint)
            paint.alpha = 255 - (i*step).toInt() //255*(width-i)/width
        }
        canvasViewModel.opacityBitmapForColorEditor = Bitmap.createScaledBitmap(bmp, width, height, false)
    }
}