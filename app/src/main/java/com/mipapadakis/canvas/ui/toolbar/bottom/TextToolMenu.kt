package com.mipapadakis.canvas.ui.toolbar.bottom

import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import androidx.lifecycle.LifecycleOwner
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.tools.ColorValues
import com.mipapadakis.canvas.ui.toolbar.bottom.editors.ColorEditorHelper
import com.mipapadakis.canvas.ui.toolbar.bottom.editors.SizeEditorHelper


class TextToolMenu(owner: LifecycleOwner, textLayout: View){
    private var toolTextColorBtn = textLayout.findViewById<ImageButton>(R.id.property_text_color_btn)
    private var toolTextColorEditor = textLayout.findViewById<LinearLayout>(R.id.property_text_color_editor)
    private var toolTextSizeBtn = textLayout.findViewById<TextView>(R.id.property_text_size_btn)
    private var toolTextSizeEditor = textLayout.findViewById<LinearLayout>(R.id.property_text_size_editor)
    private var toolTextFontBtn = textLayout.findViewById<ImageButton>(R.id.property_text_font_btn)
    private var toolTextFontEditor = textLayout.findViewById<LinearLayout>(R.id.property_text_font_editor)
    private var editorSizeSeekbar = toolTextSizeEditor.findViewById<SeekBar>(R.id.property_size_seekbar) //[0,100]

    init {
        ColorEditorHelper(owner, toolTextColorEditor){ hideAllEditors() }
        toolTextSizeBtn.text = CanvasViewModel.textPaint.strokeWidth.toInt().toString()

        toolTextColorBtn.setOnClickListener {
            hideAllEditors()
            toolTextColorEditor.visibility = View.VISIBLE
            doWhenTheViewIsVisible(toolTextColorEditor){
                CanvasViewModel.newColorHue = ColorValues.colorOnlyHue(CanvasViewModel.textPaint.color)
                CanvasViewModel.setTemporaryColor(CanvasViewModel.textPaint.color)
            }
        }
        toolTextSizeBtn.setOnClickListener {
            hideAllEditors()
            toolTextSizeEditor.visibility = View.VISIBLE
            val newSize = CanvasViewModel.paint.strokeWidth.toInt().toString() + "px"
            toolTextSizeBtn.text = newSize
            editorSizeSeekbar.progress = CanvasViewModel.paint.strokeWidth.toInt()
            doWhenTheViewIsVisible(toolTextSizeBtn){
                SizeEditorHelper.updateSeekbar(editorSizeSeekbar)
            }
        }
        toolTextFontBtn.setOnClickListener {
            hideAllEditors()
            toolTextFontEditor.visibility = View.VISIBLE
        }

        ////////////////////////////////////////SIZE EDITOR/////////////////////////////////////////
        editorSizeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(!fromUser) return //TODO what if the progress (the font size) is too small?
                toolTextSizeBtn.text = progress.toString()
                CanvasViewModel.textPaint.strokeWidth = progress.toFloat()
            }
        })


        ////////////////////////////////////////TYPE EDITOR/////////////////////////////////////////
        // TODO brush type
    }

    fun hideAllEditors(){
        toolTextColorEditor.visibility = View.GONE
        toolTextSizeEditor.visibility = View.GONE
        toolTextFontEditor.visibility = View.GONE
    }

    private fun doWhenTheViewIsVisible(view: View, function: () -> Unit) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {//https://stackoverflow.com/a/15578844/11535380
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                function()
            }
        })
    }
}