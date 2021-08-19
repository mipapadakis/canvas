package com.mipapadakis.canvas.ui.toolbar.bottom

import android.graphics.Paint
import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import androidx.lifecycle.LifecycleOwner
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.tools.ColorValues
import com.mipapadakis.canvas.ui.toolbar.bottom.editors.ColorEditorHelper
import com.mipapadakis.canvas.ui.toolbar.bottom.editors.SizeEditorHelper


class ShapeToolMenu(owner: LifecycleOwner, shapeLayout: View, val updateToolbarIcon: () -> Unit){
    private var toolShapeColorBtn = shapeLayout.findViewById<ImageButton>(R.id.property_shape_color_btn)
    private var toolShapeColorEditor = shapeLayout.findViewById<LinearLayout>(R.id.property_shape_color_editor)
    private var toolShapeTypeBtn = shapeLayout.findViewById<ImageButton>(R.id.property_shape_type_btn)
    private var toolShapeTypeEditor = shapeLayout.findViewById<HorizontalScrollView>(R.id.property_shape_type_editor)
    private var toolShapeSizeBtn = shapeLayout.findViewById<TextView>(R.id.property_shape_size_btn)
    private var toolShapeSizeEditor = shapeLayout.findViewById<LinearLayout>(R.id.property_shape_size_editor)
    private var toolShapeFillBtn = shapeLayout.findViewById<ImageButton>(R.id.property_shape_fill_btn)
    private var editorSizeSeekbar = toolShapeSizeEditor.findViewById<SeekBar>(R.id.property_size_seekbar) //[0,100]
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
        val currentSize = CanvasViewModel.shapePaint.strokeWidth.toInt().toString() + "px"
        toolShapeSizeBtn.text = currentSize
        if(CanvasViewModel.shapePaint.style == Paint.Style.FILL) toolShapeFillBtn.setImageResource(R.drawable.stroke_type_fill)
        else toolShapeFillBtn.setImageResource(R.drawable.stroke_type_stroke)
        toolShapeTypeBtn.setImageResource(CanvasViewModel.shapeType)

        toolShapeColorBtn.setOnClickListener {
            hideAllEditors()
            toolShapeColorEditor.visibility = View.VISIBLE
            doWhenTheViewIsVisible(toolShapeColorEditor){
                CanvasViewModel.newColorHue = ColorValues.colorOnlyHue(CanvasViewModel.paint.color)
                CanvasViewModel.setTemporaryColor(CanvasViewModel.paint.color)
            }
        }
        toolShapeSizeBtn.setOnClickListener {
            hideAllEditors()
            toolShapeSizeEditor.visibility = View.VISIBLE
            val newSize = CanvasViewModel.shapePaint.strokeWidth.toInt().toString() + "px"
            toolShapeSizeBtn.text = newSize
            editorSizeSeekbar.progress = CanvasViewModel.shapePaint.strokeWidth.toInt()
            doWhenTheViewIsVisible(toolShapeSizeBtn){
                SizeEditorHelper.updateSeekbar(editorSizeSeekbar)
            }
        }
        toolShapeFillBtn.setOnClickListener {
            if(CanvasViewModel.shapePaint.style == Paint.Style.FILL){
                CanvasViewModel.shapePaint.style = Paint.Style.STROKE
                toolShapeFillBtn.setImageResource(R.drawable.stroke_type_stroke)
            }
            else{
                CanvasViewModel.shapePaint.style = Paint.Style.FILL
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
                CanvasViewModel.shapePaint.strokeWidth = progress.toFloat()
            }
        })


        ////////////////////////////////////////TYPE EDITOR/////////////////////////////////////////
        editorTypeLine.setOnClickListener {
            CanvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_LINE
            toolShapeTypeBtn.setImageResource(CanvasViewModel.shapeType)
            updateToolbarIcon()
        }
        editorTypeSquare.setOnClickListener {
            CanvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_SQUARE
            toolShapeTypeBtn.setImageResource(CanvasViewModel.shapeType)
            updateToolbarIcon()
        }
        editorTypeRectangle.setOnClickListener {
            CanvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_RECTANGLE
            toolShapeTypeBtn.setImageResource(CanvasViewModel.shapeType)
            updateToolbarIcon()
        }
        editorTypeCircle.setOnClickListener {
            CanvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_CIRCLE
            toolShapeTypeBtn.setImageResource(CanvasViewModel.shapeType)
            updateToolbarIcon()
        }
        editorTypeOval.setOnClickListener {
            CanvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_OVAL
            toolShapeTypeBtn.setImageResource(CanvasViewModel.shapeType)
            updateToolbarIcon()
        }
        editorTypePolygon.setOnClickListener {
            CanvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_POLYGON
            toolShapeTypeBtn.setImageResource(CanvasViewModel.shapeType)
            updateToolbarIcon()
        }
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