package com.mipapadakis.canvas.ui.canvas.toolbar.bottom

import android.graphics.Paint
import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import androidx.lifecycle.LifecycleOwner
import com.mipapadakis.canvas.CanvasActivityData
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.tools.ColorValues
import com.mipapadakis.canvas.tools.ShowTipDialog
import com.mipapadakis.canvas.ui.canvas.toolbar.bottom.editors.ColorEditorHelper
import com.mipapadakis.canvas.ui.canvas.toolbar.bottom.editors.SizeEditorHelper


class ShapeToolMenu(owner: LifecycleOwner, shapeLayout: View, val updateToolbarIcon: () -> Unit){
    private var toolShapeColorBtn = shapeLayout.findViewById<ImageButton>(R.id.property_shape_color_btn)
    private var toolShapeColorEditor = shapeLayout.findViewById<LinearLayout>(R.id.property_shape_color_editor)
    private var toolShapeTypeBtn = shapeLayout.findViewById<ImageButton>(R.id.property_shape_type_btn)
    private var toolShapeTypeEditor = shapeLayout.findViewById<HorizontalScrollView>(R.id.property_shape_type_editor)
    private var toolShapeSizeBtn = shapeLayout.findViewById<TextView>(R.id.property_shape_size_btn)
    private var toolShapeSizeEditor = shapeLayout.findViewById<LinearLayout>(R.id.property_shape_size_editor)
    private var toolShapeFillBtn = shapeLayout.findViewById<ImageButton>(R.id.property_shape_fill_btn)
    private var editorSizeSeekbar = toolShapeSizeEditor.findViewById<SeekBar>(R.id.size_editor_seekbar) //[0,100]
    private var editorTypeLine = shapeLayout.findViewById<ImageButton>(R.id.shape_type_line)
    private var editorTypeSquare = shapeLayout.findViewById<ImageButton>(R.id.shape_type_square)
    private var editorTypeRectangle = shapeLayout.findViewById<ImageButton>(R.id.shape_type_rectangle)
    private var editorTypeCircle = shapeLayout.findViewById<ImageButton>(R.id.shape_type_circle)
    private var editorTypeOval = shapeLayout.findViewById<ImageButton>(R.id.shape_type_oval)
    private var editorTypePolygon = shapeLayout.findViewById<ImageButton>(R.id.shape_type_polygon)
//    private var editorTypeTriangle = shapeLayout.findViewById<ImageButton>(R.id.shape_type_triangle) //TODO
//    private var editorTypeArrow = shapeLayout.findViewById<ImageButton>(R.id.shape_type_arrow) //TODO
//    private var editorTypeCallout = shapeLayout.findViewById<ImageButton>(R.id.shape_type_callout) //TODO

    init {
        ColorEditorHelper(owner, toolShapeColorEditor){ hideAllEditors() }
        val currentSize = CanvasActivityData.shapePaint.strokeWidth.toInt().toString() + "px"
        toolShapeSizeBtn.text = currentSize
        if(CanvasActivityData.shapePaint.style == Paint.Style.FILL) toolShapeFillBtn.setImageResource(R.drawable.stroke_type_fill)
        else toolShapeFillBtn.setImageResource(R.drawable.stroke_type_stroke)
        toolShapeTypeBtn.setImageResource(CanvasActivityData.shapeType)

        toolShapeColorBtn.setOnClickListener {
            hideAllEditors()
            toolShapeColorEditor.visibility = View.VISIBLE
            doWhenTheViewIsVisible(toolShapeColorEditor){
                CanvasActivityData.newColorHue = ColorValues.colorOnlyHue(CanvasActivityData.paint.color)
                CanvasActivityData.setTemporaryColor(CanvasActivityData.paint.color)
            }
        }
        toolShapeSizeBtn.setOnClickListener {
            hideAllEditors()
            toolShapeSizeEditor.visibility = View.VISIBLE
            val newSize = CanvasActivityData.shapePaint.strokeWidth.toInt().toString() + "px"
            toolShapeSizeBtn.text = newSize
            editorSizeSeekbar.progress = CanvasActivityData.shapePaint.strokeWidth.toInt()
            doWhenTheViewIsVisible(toolShapeSizeBtn){
                SizeEditorHelper.updateSeekbar(editorSizeSeekbar)
            }
        }
        toolShapeFillBtn.setOnClickListener {
            if(CanvasActivityData.shapePaint.style == Paint.Style.FILL){
                CanvasActivityData.shapePaint.style = Paint.Style.STROKE
                toolShapeFillBtn.setImageResource(R.drawable.stroke_type_stroke)
            }
            else{
                CanvasActivityData.shapePaint.style = Paint.Style.FILL
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
                CanvasActivityData.shapePaint.strokeWidth = progress.toFloat()
            }
        })


        ////////////////////////////////////////TYPE EDITOR/////////////////////////////////////////
        editorTypeLine.setOnClickListener {
            CanvasActivityData.shapeType = CanvasActivityData.SHAPE_TYPE_LINE
            toolShapeTypeBtn.setImageResource(CanvasActivityData.shapeType)
            updateToolbarIcon()
        }
        editorTypeSquare.setOnClickListener {
            CanvasActivityData.shapeType = CanvasActivityData.SHAPE_TYPE_SQUARE
            toolShapeTypeBtn.setImageResource(CanvasActivityData.shapeType)
            updateToolbarIcon()
        }
        editorTypeRectangle.setOnClickListener {
            CanvasActivityData.shapeType = CanvasActivityData.SHAPE_TYPE_RECTANGLE
            toolShapeTypeBtn.setImageResource(CanvasActivityData.shapeType)
            updateToolbarIcon()
        }
        editorTypeCircle.setOnClickListener {
            CanvasActivityData.shapeType = CanvasActivityData.SHAPE_TYPE_CIRCLE
            toolShapeTypeBtn.setImageResource(CanvasActivityData.shapeType)
            updateToolbarIcon()
        }
        editorTypeOval.setOnClickListener {
            CanvasActivityData.shapeType = CanvasActivityData.SHAPE_TYPE_OVAL
            toolShapeTypeBtn.setImageResource(CanvasActivityData.shapeType)
            updateToolbarIcon()
        }
        editorTypePolygon.setOnClickListener {
            CanvasActivityData.shapeType = CanvasActivityData.SHAPE_TYPE_POLYGON
            toolShapeTypeBtn.setImageResource(CanvasActivityData.shapeType)
            updateToolbarIcon()
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