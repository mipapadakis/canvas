package com.mipapadakis.canvas.ui.canvas.toolbar.bottom

import android.graphics.Paint
import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.ui.canvas.CanvasViews
import com.mipapadakis.canvas.ui.util.ColorValues
import com.mipapadakis.canvas.ui.util.ShowTipDialog
import com.mipapadakis.canvas.ui.canvas.toolbar.bottom.editors.ColorEditorHelper
import com.mipapadakis.canvas.ui.canvas.toolbar.bottom.editors.SizeEditorHelper


class ShapeProperties(canvasViews: CanvasViews){
    private var toolShapeColorBtn = canvasViews.toolShapeLayout.findViewById<ImageButton>(R.id.property_shape_color_btn)
    private var toolShapeColorEditor = canvasViews.toolShapeLayout.findViewById<LinearLayout>(R.id.property_shape_color_editor)
    private var toolShapeTypeBtn = canvasViews.toolShapeLayout.findViewById<ImageButton>(R.id.property_shape_type_btn)
    private var toolShapeTypeEditor = canvasViews.toolShapeLayout.findViewById<HorizontalScrollView>(R.id.property_shape_type_editor)
    private var toolShapeSizeBtn = canvasViews.toolShapeLayout.findViewById<TextView>(R.id.property_shape_size_btn)
    private var toolShapeSizeEditor = canvasViews.toolShapeLayout.findViewById<LinearLayout>(R.id.property_shape_size_editor)
    private var toolShapeFillBtn = canvasViews.toolShapeLayout.findViewById<ImageButton>(R.id.property_shape_fill_btn)
    private var editorSizeSeekbar = toolShapeSizeEditor.findViewById<SeekBar>(R.id.size_editor_seekbar) //[0,100]
    private var editorTypeLine = canvasViews.toolShapeLayout.findViewById<ImageButton>(R.id.shape_type_line)
    private var editorTypeSquare = canvasViews.toolShapeLayout.findViewById<ImageButton>(R.id.shape_type_square)
    private var editorTypeRectangle = canvasViews.toolShapeLayout.findViewById<ImageButton>(R.id.shape_type_rectangle)
    private var editorTypeCircle = canvasViews.toolShapeLayout.findViewById<ImageButton>(R.id.shape_type_circle)
    private var editorTypeOval = canvasViews.toolShapeLayout.findViewById<ImageButton>(R.id.shape_type_oval)
    private var editorTypePolygon = canvasViews.toolShapeLayout.findViewById<ImageButton>(R.id.shape_type_polygon)
//    private var editorTypeTriangle = shapeLayout.findViewById<ImageButton>(R.id.shape_type_triangle) //TODO
//    private var editorTypeArrow = shapeLayout.findViewById<ImageButton>(R.id.shape_type_arrow) //TODO
//    private var editorTypeCallout = shapeLayout.findViewById<ImageButton>(R.id.shape_type_callout) //TODO

    init {
        ColorEditorHelper(canvasViews.owner, canvasViews.canvasViewModel, toolShapeColorEditor){ hideAllEditors() }
        val currentSize = canvasViews.canvasViewModel.shapePaint.strokeWidth.toInt().toString() + "px"
        toolShapeSizeBtn.text = currentSize
        if(canvasViews.canvasViewModel.shapePaint.style == Paint.Style.FILL) toolShapeFillBtn.setImageResource(R.drawable.stroke_type_fill)
        else toolShapeFillBtn.setImageResource(R.drawable.stroke_type_stroke)
        toolShapeTypeBtn.setImageResource(canvasViews.canvasViewModel.shapeType)

        toolShapeColorBtn.setOnClickListener {
            hideAllEditors()
            toolShapeColorEditor.visibility = View.VISIBLE
            doWhenTheViewIsVisible(toolShapeColorEditor){
                canvasViews.canvasViewModel.newColorHue = ColorValues.colorOnlyHue(canvasViews.canvasViewModel.getColor())
                canvasViews.canvasViewModel.setTemporaryColor(canvasViews.canvasViewModel.getColor())
            }
        }
        toolShapeSizeBtn.setOnClickListener {
            hideAllEditors()
            toolShapeSizeEditor.visibility = View.VISIBLE
            val newSize = canvasViews.canvasViewModel.shapePaint.strokeWidth.toInt().toString() + "px"
            toolShapeSizeBtn.text = newSize
            editorSizeSeekbar.progress = canvasViews.canvasViewModel.shapePaint.strokeWidth.toInt()
            doWhenTheViewIsVisible(toolShapeSizeBtn){
                SizeEditorHelper.updateSeekbar(editorSizeSeekbar)
            }
        }
        toolShapeFillBtn.setOnClickListener {
            if(canvasViews.canvasViewModel.shapePaint.style == Paint.Style.FILL){
                canvasViews.canvasViewModel.shapePaint.style = Paint.Style.STROKE
                toolShapeFillBtn.setImageResource(R.drawable.stroke_type_stroke)
            }
            else{
                canvasViews.canvasViewModel.shapePaint.style = Paint.Style.FILL
                toolShapeFillBtn.setImageResource(R.drawable.stroke_type_fill)
            }
        }
        toolShapeTypeBtn.setOnClickListener {
            hideAllEditors()
            toolShapeTypeEditor.visibility = View.VISIBLE
        }

        ////////////////////////////////////////SIZE EDITOR/////////////////////////////////////////
        editorSizeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(!fromUser) return
                val newSize = progress.toString() + "px"
                toolShapeSizeBtn.text = newSize
                canvasViews.canvasViewModel.shapePaint.strokeWidth = progress.toFloat()
            }
        })


        ////////////////////////////////////////TYPE EDITOR/////////////////////////////////////////
        editorTypeLine.setOnClickListener {
            canvasViews.canvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_LINE
            toolShapeTypeBtn.setImageResource(canvasViews.canvasViewModel.shapeType)
            canvasViews.toolbarToolBtn.setImageResource(canvasViews.canvasViewModel.shapeType)
        }
        editorTypeSquare.setOnClickListener {
            canvasViews.canvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_SQUARE
            toolShapeTypeBtn.setImageResource(canvasViews.canvasViewModel.shapeType)
            canvasViews.toolbarToolBtn.setImageResource(canvasViews.canvasViewModel.shapeType)
        }
        editorTypeRectangle.setOnClickListener {
            canvasViews.canvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_RECTANGLE
            toolShapeTypeBtn.setImageResource(canvasViews.canvasViewModel.shapeType)
            canvasViews.toolbarToolBtn.setImageResource(canvasViews.canvasViewModel.shapeType)
        }
        editorTypeCircle.setOnClickListener {
            canvasViews.canvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_CIRCLE
            toolShapeTypeBtn.setImageResource(canvasViews.canvasViewModel.shapeType)
            canvasViews.toolbarToolBtn.setImageResource(canvasViews.canvasViewModel.shapeType)
        }
        editorTypeOval.setOnClickListener {
            canvasViews.canvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_OVAL
            toolShapeTypeBtn.setImageResource(canvasViews.canvasViewModel.shapeType)
            canvasViews.toolbarToolBtn.setImageResource(canvasViews.canvasViewModel.shapeType)
        }
        editorTypePolygon.setOnClickListener {
            canvasViews.canvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_POLYGON
            toolShapeTypeBtn.setImageResource(canvasViews.canvasViewModel.shapeType)
            canvasViews.toolbarToolBtn.setImageResource(canvasViews.canvasViewModel.shapeType)
        }
        setTipDialogs()
    }

    private fun setTipDialogs() {
        ShowTipDialog(toolShapeColorBtn, R.drawable.color_palette_outlined)
        ShowTipDialog(toolShapeSizeBtn, R.drawable.check_box_empty_outlined)
        ShowTipDialog(toolShapeTypeBtn, R.drawable.check_box_empty_outlined)
        ShowTipDialog(toolShapeFillBtn, R.drawable.stroke_type_fill)
    }

    fun hideAllEditors(){
        toolShapeColorEditor.visibility = View.GONE
        toolShapeSizeEditor.visibility = View.GONE
        toolShapeTypeEditor.visibility = View.GONE
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