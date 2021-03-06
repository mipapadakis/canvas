package com.mipapadakis.canvas.ui.canvas.toolbar.bottom

import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.ui.canvas.CanvasViews
import com.mipapadakis.canvas.ui.util.ShowTipDialog
import com.mipapadakis.canvas.ui.canvas.toolbar.bottom.editors.EraserOpacityEditorHelper
import com.mipapadakis.canvas.ui.canvas.toolbar.bottom.editors.SizeEditorHelper


class EraserProperties(canvasViews: CanvasViews){
    private var toolEraserSizeBtn = canvasViews.toolEraserLayout.findViewById<TextView>(R.id.property_eraser_size_btn)
    private var toolEraserSizeEditor = canvasViews.toolEraserLayout.findViewById<LinearLayout>(R.id.property_eraser_size_editor)
    private var editorSizeSeekbar = toolEraserSizeEditor.findViewById<SeekBar>(R.id.size_editor_seekbar) //[0,100]
    private var toolEraserOpacityBtn = canvasViews.toolEraserLayout.findViewById<ImageButton>(R.id.property_eraser_opacity_btn)
    private var toolEraserOpacityValue = canvasViews.toolEraserLayout.findViewById<TextView>(R.id.property_eraser_opacity_value)
    private var toolEraserOpacityEditor = canvasViews.toolEraserLayout.findViewById<LinearLayout>(R.id.property_eraser_opacity_editor)
    private var editorOpacitySeekbar = toolEraserOpacityEditor.findViewById<SeekBar>(R.id.opacity_editor_seekbar) //[0,255]

    init {
        val currentSize = canvasViews.canvasViewModel.eraserPaint.strokeWidth.toInt().toString() + "px"
        toolEraserSizeBtn.text = currentSize
        toolEraserOpacityValue.text = canvasViews.canvasViewModel.eraserPaint.alpha.toString()

        toolEraserSizeBtn.setOnClickListener {
            toolEraserOpacityEditor.visibility = View.GONE
            toolEraserSizeEditor.visibility = View.VISIBLE
            val newSize = canvasViews.canvasViewModel.eraserPaint.strokeWidth.toInt().toString() + "px"
            toolEraserSizeBtn.text = newSize
            editorSizeSeekbar.progress = canvasViews.canvasViewModel.eraserPaint.strokeWidth.toInt()
            doWhenTheViewIsVisible(toolEraserSizeBtn){
                SizeEditorHelper.updateSeekbar(editorSizeSeekbar)
            }
        }

        toolEraserOpacityBtn.setOnClickListener { onOpacityButtonClick(canvasViews) }
        toolEraserOpacityValue.setOnClickListener { onOpacityButtonClick(canvasViews) }

        ////////////////////////////////////////SIZE EDITOR/////////////////////////////////////////
        editorSizeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(!fromUser) return
                val newSize = progress.toString() + "px"
                toolEraserSizeBtn.text = newSize
                canvasViews.canvasViewModel.eraserPaint.strokeWidth = progress.toFloat()
            }
        })
        //////////////////////////////////////OPACITY EDITOR////////////////////////////////////////
        editorOpacitySeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(!fromUser) return
                toolEraserOpacityValue.text = progress.toString()
                canvasViews.canvasViewModel.eraserPaint.alpha = progress
            }
        })
        ShowTipDialog(toolEraserSizeBtn, R.drawable.eraser_outlined)
        ShowTipDialog(toolEraserOpacityBtn, R.drawable.opacity_outlined)
    }

    private fun onOpacityButtonClick(canvasViews: CanvasViews){
        toolEraserOpacityEditor.visibility = View.VISIBLE
        toolEraserSizeEditor.visibility = View.GONE
        editorOpacitySeekbar.progress = canvasViews.canvasViewModel.eraserPaint.alpha
        doWhenTheViewIsVisible(toolEraserOpacityBtn){
            EraserOpacityEditorHelper.updateSeekbar(canvasViews.canvasViewModel, editorOpacitySeekbar)
        }
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