package com.mipapadakis.canvas.ui.canvas.toolbar

import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.ui.util.ShowTipDialog
import com.mipapadakis.canvas.ui.canvas.CanvasViews

class ToolbarUndo(canvasViews: CanvasViews) {
    init {
        canvasViews.toolbarUndoBtn.setOnClickListener {
            if(!canvasViews.canvasIV.undo())
                canvasViews.showToast("can't undo")
        }
        ShowTipDialog(canvasViews.toolbarUndoBtn, R.drawable.undo_outlined)
    }
}