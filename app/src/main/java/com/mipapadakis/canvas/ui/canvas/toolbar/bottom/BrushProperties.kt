package com.mipapadakis.canvas.ui.canvas.toolbar.bottom

import android.view.View
import android.widget.*
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.R
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import com.mipapadakis.canvas.ui.canvas.CanvasViews
import com.mipapadakis.canvas.ui.util.ColorValues
import com.mipapadakis.canvas.ui.util.ShowTipDialog
import com.mipapadakis.canvas.ui.canvas.toolbar.bottom.editors.ColorEditorHelper
import com.mipapadakis.canvas.ui.canvas.toolbar.bottom.editors.SizeEditorHelper

class BrushProperties(canvasViews: CanvasViews){
    private val resources = canvasViews.toolBrushLayout.resources
    private var toolBrushColorBtn = canvasViews.toolBrushLayout.findViewById<ImageButton>(R.id.property_brush_color_btn)
    private var toolBrushColorEditor = canvasViews.toolBrushLayout.findViewById<LinearLayout>(R.id.property_brush_color_editor)
    private var toolBrushSizeBtn = canvasViews.toolBrushLayout.findViewById<TextView>(R.id.property_brush_size_btn)
    private var toolBrushSizeEditor = canvasViews.toolBrushLayout.findViewById<LinearLayout>(R.id.property_size_editor)
    private var toolBrushTypeBtn = canvasViews.toolBrushLayout.findViewById<ImageButton>(R.id.property_brush_type_btn)
    private var toolBrushTypeEditor = canvasViews.toolBrushLayout.findViewById<HorizontalScrollView>(R.id.property_brush_type_editor)

    private var editorSizeSeekbar = toolBrushSizeEditor.findViewById<SeekBar>(R.id.size_editor_seekbar) //[0,100]
    private var editorBrushTypeBrush = canvasViews.toolBrushLayout.findViewById<ImageButton>(R.id.brush_type_brush)
    private var editorBrushTypePencil = canvasViews.toolBrushLayout.findViewById<ImageButton>(R.id.brush_type_pencil)
    private var editorBrushTypePattern1 = canvasViews.toolBrushLayout.findViewById<FrameLayout>(R.id.brush_type_pattern_1)
    private var editorBrushTypePattern2 = canvasViews.toolBrushLayout.findViewById<FrameLayout>(R.id.brush_type_pattern_2)
    private var editorBrushTypePattern3 = canvasViews.toolBrushLayout.findViewById<FrameLayout>(R.id.brush_type_pattern_3)
    private var editorBrushTypePattern4 = canvasViews.toolBrushLayout.findViewById<FrameLayout>(R.id.brush_type_pattern_4)
    private var editorBrushTypePattern5 = canvasViews.toolBrushLayout.findViewById<FrameLayout>(R.id.brush_type_pattern_5)

    init {
        ColorEditorHelper(canvasViews.owner, canvasViews.canvasViewModel, toolBrushColorEditor){ hideAllEditors() }
        val currentSize = canvasViews.canvasViewModel.getBrushSize().toInt().toString() + "px"
        toolBrushSizeBtn.text = currentSize
        toolBrushTypeBtn.setImageResource(canvasViews.canvasViewModel.brushType)

        toolBrushColorBtn.setOnClickListener {
            hideAllEditors()
            toolBrushColorEditor.visibility = View.VISIBLE
            doWhenTheViewIsVisible(toolBrushColorEditor){
                canvasViews.canvasViewModel.newColorHue = ColorValues.colorOnlyHue(canvasViews.canvasViewModel.getColor())
                canvasViews.canvasViewModel.setTemporaryColor(canvasViews.canvasViewModel.getColor())
            }
        }
        toolBrushSizeBtn.setOnClickListener {
            hideAllEditors()
            toolBrushSizeEditor.visibility = View.VISIBLE
            val newSize = canvasViews.canvasViewModel.getBrushSize().toInt().toString() + "px"
            toolBrushSizeBtn.text = newSize
            editorSizeSeekbar.progress = canvasViews.canvasViewModel.getBrushSize().toInt()
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
                canvasViews.canvasViewModel.setBrushSize(resources, progress.toFloat())
            }
        })


        ////////////////////////////////////////TYPE EDITOR/////////////////////////////////////////
        editorBrushTypeBrush.setOnClickListener {
            canvasViews.canvasViewModel.setBrushOrPencilType(CanvasViewModel.BRUSH_TYPE_BRUSH)
            toolBrushTypeBtn.setImageResource(CanvasViewModel.BRUSH_TYPE_BRUSH)
            toolBrushSizeBtn.visibility = View.VISIBLE
        }
        editorBrushTypePencil.setOnClickListener {
            canvasViews.canvasViewModel.setBrushOrPencilType(CanvasViewModel.BRUSH_TYPE_PENCIL)
            toolBrushTypeBtn.setImageResource(CanvasViewModel.BRUSH_TYPE_PENCIL)
            toolBrushSizeBtn.visibility = View.GONE
        }
        editorBrushTypePattern1.setOnClickListener {
            canvasViews.canvasViewModel.setBrushType(resources, CanvasViewModel.BRUSH_TYPE_PATTERN_1)
            toolBrushTypeBtn.setImageResource(CanvasViewModel.BRUSH_TYPE_PATTERN_1)
            toolBrushSizeBtn.visibility = View.VISIBLE
        }
        editorBrushTypePattern2.setOnClickListener {
            canvasViews.canvasViewModel.setBrushType(resources, CanvasViewModel.BRUSH_TYPE_PATTERN_2)
            toolBrushTypeBtn.setImageResource(CanvasViewModel.BRUSH_TYPE_PATTERN_2)
            toolBrushSizeBtn.visibility = View.VISIBLE
        }
        editorBrushTypePattern3.setOnClickListener {
            canvasViews.canvasViewModel.setBrushType(resources, CanvasViewModel.BRUSH_TYPE_PATTERN_3)
            toolBrushTypeBtn.setImageResource(CanvasViewModel.BRUSH_TYPE_PATTERN_3)
            toolBrushSizeBtn.visibility = View.VISIBLE
        }
        editorBrushTypePattern4.setOnClickListener {
            canvasViews.canvasViewModel.setBrushType(resources, CanvasViewModel.BRUSH_TYPE_PATTERN_4)
            toolBrushTypeBtn.setImageResource(CanvasViewModel.BRUSH_TYPE_PATTERN_4)
            toolBrushSizeBtn.visibility = View.VISIBLE
        }
        editorBrushTypePattern5.setOnClickListener {
            canvasViews.canvasViewModel.setBrushType(resources, CanvasViewModel.BRUSH_TYPE_PATTERN_5)
            toolBrushTypeBtn.setImageResource(CanvasViewModel.BRUSH_TYPE_PATTERN_5)
            toolBrushSizeBtn.visibility = View.VISIBLE
        }
        setTipDialogs()
    }

    private fun setTipDialogs() {
        ShowTipDialog(toolBrushColorBtn, R.drawable.color_palette_outlined)
        ShowTipDialog(toolBrushSizeBtn, R.drawable.brush_outlined)
        ShowTipDialog(toolBrushTypeBtn, R.drawable.brush_outlined)
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