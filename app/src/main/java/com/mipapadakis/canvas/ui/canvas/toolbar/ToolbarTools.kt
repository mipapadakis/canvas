package com.mipapadakis.canvas.ui.canvas.toolbar

import android.view.View
import android.widget.PopupMenu
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.ui.util.ShowTipDialog
import com.mipapadakis.canvas.ui.canvas.CanvasViews

class ToolbarTools(canvasViews: CanvasViews) {

    init{
        canvasViews.toolbarToolBtn.setImageResource(canvasViews.canvasViewModel.tool)
        canvasViews.toolbarToolBtn.setOnClickListener {
            //If Bottom Toolbar is not visible, show it. Else, show the menu.
            if(!canvasViews.bottomToolbarIsVisible() || wrongPropertiesShown(canvasViews)){
                showCurrentToolProperties(canvasViews)
                return@setOnClickListener
            }
            // Manage which properties are shown in the Bottom Toolbar
            val toolsMenu = PopupMenu(canvasViews.context, canvasViews.toolbarToolBtn)
            toolsMenu.menuInflater.inflate(R.menu.tools, toolsMenu.menu)
            toolsMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.tool_brush ->
                        canvasViews.canvasViewModel.tool = CanvasViewModel.TOOL_BRUSH
                    R.id.tool_eraser ->
                        canvasViews.canvasViewModel.tool = CanvasViewModel.TOOL_ERASER
                    R.id.tool_bucket ->
                        canvasViews.canvasViewModel.tool = CanvasViewModel.TOOL_BUCKET
                    R.id.tool_eyedropper ->
                        canvasViews.canvasViewModel.tool = CanvasViewModel.TOOL_EYEDROPPER
                    R.id.tool_select ->
                        canvasViews.canvasViewModel.tool = CanvasViewModel.TOOL_SELECT
                    R.id.tool_shape ->
                        canvasViews.canvasViewModel.tool = CanvasViewModel.TOOL_SHAPE
                    R.id.tool_text ->
                        canvasViews.canvasViewModel.tool = CanvasViewModel.TOOL_TEXT
                    else -> {}
                }
                showCurrentToolProperties(canvasViews)
                updateCurrentToolIconOfToolbar(canvasViews)
                true
            }
            toolsMenu.show()
        }
        ShowTipDialog(canvasViews.toolbarToolBtn, R.drawable.brush_outlined)
    }

    private fun wrongPropertiesShown(canvasViews: CanvasViews): Boolean{
        if(canvasViews.layersLayout.visibility == View.VISIBLE) return true
        if(canvasViews.transformLayout.visibility == View.VISIBLE) return true
        return when (canvasViews.canvasViewModel.tool){
            CanvasViewModel.TOOL_BRUSH -> canvasViews.toolBrushLayout.visibility != View.VISIBLE
            CanvasViewModel.TOOL_ERASER -> canvasViews.toolEraserLayout.visibility != View.VISIBLE
            CanvasViewModel.TOOL_BUCKET -> canvasViews.toolBucketLayout.visibility != View.VISIBLE
            CanvasViewModel.TOOL_EYEDROPPER -> !canvasViews.bottomToolbarIsVisible()
            CanvasViewModel.TOOL_SELECT -> canvasViews.toolSelectLayout.visibility != View.VISIBLE
            CanvasViewModel.TOOL_SHAPE -> canvasViews.toolShapeLayout.visibility != View.VISIBLE
            CanvasViewModel.TOOL_TEXT -> canvasViews.toolTextLayout.visibility != View.VISIBLE
            else -> false
        }
    }

    companion object{
        fun showCurrentToolProperties(canvasViews: CanvasViews){
            canvasViews.hideProperties()
            when (canvasViews.canvasViewModel.tool){
                CanvasViewModel.TOOL_BRUSH -> {
                    canvasViews.showBottomToolbar()
                    canvasViews.toolBrushLayout.visibility = View.VISIBLE
                }
                CanvasViewModel.TOOL_ERASER -> {
                    canvasViews.showBottomToolbar()
                    canvasViews.toolEraserLayout.visibility = View.VISIBLE
                }
                CanvasViewModel.TOOL_BUCKET -> {
                    canvasViews.showBottomToolbar()
                    canvasViews.toolBucketLayout.visibility = View.VISIBLE
                }
                CanvasViewModel.TOOL_EYEDROPPER ->  canvasViews.hideBottomToolbar()
                CanvasViewModel.TOOL_SELECT -> {
                    canvasViews.showBottomToolbar()
                    canvasViews.toolSelectLayout.visibility = View.VISIBLE
                }
                CanvasViewModel.TOOL_SHAPE -> {
                    canvasViews.showBottomToolbar()
                    canvasViews.toolShapeLayout.visibility = View.VISIBLE
                }
                CanvasViewModel.TOOL_TEXT -> {
                    canvasViews.showBottomToolbar()
                    canvasViews.toolTextLayout.visibility = View.VISIBLE
                }
            }
        }

        fun updateCurrentToolIconOfToolbar(canvasViews: CanvasViews) {
            when (canvasViews.canvasViewModel.tool) {
                CanvasViewModel.TOOL_BRUSH -> canvasViews.toolbarToolBtn.setImageResource(R.drawable.brush_outlined)
                CanvasViewModel.TOOL_ERASER -> canvasViews.toolbarToolBtn.setImageResource(R.drawable.eraser_outlined)
                CanvasViewModel.TOOL_BUCKET -> canvasViews.toolbarToolBtn.setImageResource(R.drawable.bucket_outlined)
                CanvasViewModel.TOOL_EYEDROPPER -> canvasViews.toolbarToolBtn.setImageResource(R.drawable.eyedropper_outlined)
                CanvasViewModel.TOOL_SELECT -> canvasViews.toolbarToolBtn.setImageResource(R.drawable.select_rectangular)
                CanvasViewModel.TOOL_SHAPE -> canvasViews.toolbarToolBtn.setImageResource(canvasViews.canvasViewModel.shapeType)
                CanvasViewModel.TOOL_TEXT -> canvasViews.toolbarToolBtn.setImageResource(R.drawable.text_outlined)
            }
        }
    }
}