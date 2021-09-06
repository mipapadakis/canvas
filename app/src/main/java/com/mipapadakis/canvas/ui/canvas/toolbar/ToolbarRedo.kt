package com.mipapadakis.canvas.ui.canvas.toolbar

import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.ui.util.ShowTipDialog
import com.mipapadakis.canvas.ui.canvas.CanvasViews

class ToolbarRedo(canvasViews: CanvasViews) {
    init{
        canvasViews.toolbarRedoBtn.setOnClickListener {
            if(!canvasViews.canvasIV.redo())
                canvasViews.showToast("can't redo")
        }
        ShowTipDialog(canvasViews.toolbarRedoBtn, R.drawable.redo_outlined)
    }
}