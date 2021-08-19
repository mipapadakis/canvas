package com.mipapadakis.canvas.ui.toolbar.bottom

import android.view.View
import android.widget.*
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.R
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.lifecycle.LifecycleOwner
import com.mipapadakis.canvas.tools.ColorValues
import com.mipapadakis.canvas.ui.toolbar.bottom.editors.ColorEditorHelper
import com.mipapadakis.canvas.ui.toolbar.bottom.editors.SizeEditorHelper

class BrushToolMenu(owner: LifecycleOwner, brushLayout: View){
    private var toolBrushColorBtn = brushLayout.findViewById<ImageButton>(R.id.property_brush_color_btn)
    private var toolBrushColorEditor = brushLayout.findViewById<LinearLayout>(R.id.property_brush_color_editor)
    private var toolBrushSizeBtn = brushLayout.findViewById<TextView>(R.id.property_brush_size_btn)
    private var toolBrushSizeEditor = brushLayout.findViewById<LinearLayout>(R.id.property_size_editor)
    private var toolBrushTypeBtn = brushLayout.findViewById<ImageButton>(R.id.property_brush_type_btn)
    private var toolBrushTypeEditor = brushLayout.findViewById<HorizontalScrollView>(R.id.property_brush_type_editor)

    private var editorSizeSeekbar = toolBrushSizeEditor.findViewById<SeekBar>(R.id.property_size_seekbar) //[0,100]
    private var editorBrushTypeBrush = brushLayout.findViewById<ImageButton>(R.id.brush_type_brush)
    //private var editorBrushTypePencil = brushLayout.findViewById<ImageButton>(R.id.brush_type_pencil)
    private var editorBrushTypePattern1 = brushLayout.findViewById<FrameLayout>(R.id.brush_type_pattern_1)
    private var editorBrushTypePattern2 = brushLayout.findViewById<FrameLayout>(R.id.brush_type_pattern_2)
    private var editorBrushTypePattern3 = brushLayout.findViewById<FrameLayout>(R.id.brush_type_pattern_3)
    private var editorBrushTypePattern4 = brushLayout.findViewById<FrameLayout>(R.id.brush_type_pattern_4)
    private var editorBrushTypePattern5 = brushLayout.findViewById<FrameLayout>(R.id.brush_type_pattern_5)

    init {
        ColorEditorHelper(owner, toolBrushColorEditor){ hideAllEditors() }
        val currentSize = CanvasViewModel.paint.strokeWidth.toInt().toString() + "px"
        toolBrushSizeBtn.text = currentSize
        toolBrushTypeBtn.setImageResource(CanvasViewModel.brushType)

        toolBrushColorBtn.setOnClickListener {
            hideAllEditors()
            toolBrushColorEditor.visibility = View.VISIBLE
            doWhenTheViewIsVisible(toolBrushColorEditor){
                CanvasViewModel.newColorHue = ColorValues.colorOnlyHue(CanvasViewModel.paint.color)
                CanvasViewModel.setTemporaryColor(CanvasViewModel.paint.color)
            }
        }
        toolBrushSizeBtn.setOnClickListener {
            hideAllEditors()
            toolBrushSizeEditor.visibility = View.VISIBLE
            val newSize = CanvasViewModel.paint.strokeWidth.toInt().toString() + "px"
            toolBrushSizeBtn.text = newSize
            editorSizeSeekbar.progress = CanvasViewModel.paint.strokeWidth.toInt()
            doWhenTheViewIsVisible(toolBrushSizeBtn){
                SizeEditorHelper.updateSeekbar(editorSizeSeekbar)
            }
        }
        toolBrushTypeBtn.setOnClickListener {
            hideAllEditors()
            toolBrushTypeEditor.visibility = View.VISIBLE
        }

        ////////////////////////////////////////SIZE EDITOR/////////////////////////////////////////
        editorSizeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(!fromUser) return
                val newSize = progress.toString() + "px"
                toolBrushSizeBtn.text = newSize
                CanvasViewModel.paint.strokeWidth = progress.toFloat()
                CanvasViewModel.updatePatternDrawingSize(brushLayout.resources)
            }
        })


        ////////////////////////////////////////TYPE EDITOR/////////////////////////////////////////
        editorBrushTypeBrush.setOnClickListener {
            CanvasViewModel.setBrushOrPencilType(CanvasViewModel.BRUSH_TYPE_BRUSH)
            toolBrushTypeBtn.setImageResource(CanvasViewModel.BRUSH_TYPE_BRUSH)
        }
        /*editorBrushTypePencil.setOnClickListener {
            CanvasViewModel.setBrushOrPencilType(CanvasViewModel.BRUSH_TYPE_PENCIL)
            toolBrushTypeBtn.setImageResource(CanvasViewModel.BRUSH_TYPE_PENCIL)
        }*/
        editorBrushTypePattern1.setOnClickListener {
            CanvasViewModel.setBrushType(brushLayout.resources, CanvasViewModel.BRUSH_TYPE_PATTERN_1)
            toolBrushTypeBtn.setImageResource(CanvasViewModel.BRUSH_TYPE_PATTERN_1)
        }
        editorBrushTypePattern2.setOnClickListener {
            CanvasViewModel.setBrushType(brushLayout.resources, CanvasViewModel.BRUSH_TYPE_PATTERN_2)
            toolBrushTypeBtn.setImageResource(CanvasViewModel.BRUSH_TYPE_PATTERN_2)
        }
        editorBrushTypePattern3.setOnClickListener {
            CanvasViewModel.setBrushType(brushLayout.resources, CanvasViewModel.BRUSH_TYPE_PATTERN_3)
            toolBrushTypeBtn.setImageResource(CanvasViewModel.BRUSH_TYPE_PATTERN_3)
        }
        editorBrushTypePattern4.setOnClickListener {
            CanvasViewModel.setBrushType(brushLayout.resources, CanvasViewModel.BRUSH_TYPE_PATTERN_4)
            toolBrushTypeBtn.setImageResource(CanvasViewModel.BRUSH_TYPE_PATTERN_4)
        }
        editorBrushTypePattern5.setOnClickListener {
            CanvasViewModel.setBrushType(brushLayout.resources, CanvasViewModel.BRUSH_TYPE_PATTERN_5)
            toolBrushTypeBtn.setImageResource(CanvasViewModel.BRUSH_TYPE_PATTERN_5)
        }
    }

    fun hideAllEditors(){
        toolBrushColorEditor.visibility = View.GONE
        toolBrushSizeEditor.visibility = View.GONE
        toolBrushTypeEditor.visibility = View.GONE
    }

    private fun doWhenTheViewIsVisible(view: View, function: () -> Unit) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {//https://stackoverflow.com/a/15578844/11535380
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                function()
            }
        })
    }
}