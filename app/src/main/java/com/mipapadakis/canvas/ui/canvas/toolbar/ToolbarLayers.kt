package com.mipapadakis.canvas.ui.canvas.toolbar

import android.annotation.SuppressLint
import android.view.View
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.ui.util.ShowTipDialog
import com.mipapadakis.canvas.ui.canvas.CanvasViews

@SuppressLint("NotifyDataSetChanged")
class ToolbarLayers(canvasViews: CanvasViews) {
    init{
        canvasViews.toolbarLayerBtn.setOnClickListener {
            canvasViews.hideProperties()
            canvasViews.layerRecyclerView.adapter?.notifyDataSetChanged()
            canvasViews.layersLayout.visibility = View.VISIBLE
            canvasViews.showBottomToolbar()
        }
        ShowTipDialog(canvasViews.toolbarLayerBtn, R.drawable.layers_outlined)
    }
}