package com.mipapadakis.canvas.ui.canvas.toolbar

import android.view.View
import android.widget.*
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.ui.util.ShowTipDialog
import com.mipapadakis.canvas.ui.canvas.CanvasViews

class ToolbarOptions(canvasViews: CanvasViews) {

    init{
        canvasViews.toolbarOptionsBtn.setOnClickListener {
            //If Bottom Toolbar transform layout is not visible, show it. Else, show the menu.
            if(!canvasViews.bottomToolbarIsVisible() && canvasViews.transformLayout.visibility == View.VISIBLE){
                canvasViews.showBottomToolbar()
                return@setOnClickListener
            }
            val canvasMenu = PopupMenu(canvasViews.context, canvasViews.toolbarOptionsBtn)
            canvasMenu.menuInflater.inflate(R.menu.canvas_options, canvasMenu.menu)
            canvasMenu.setOnMenuItemClickListener {
                canvasViews.hideProperties()
                when (it.itemId) {
                    R.id.canvas_transform-> {
                        canvasViews.showBottomToolbar()
                        canvasViews.transformLayout.visibility = View.VISIBLE
                    }
                    R.id.canvas_save -> {
                        canvasViews.hideBottomToolbar()
                        canvasViews.showSaveCanvasDialog(false)
                    }
                    R.id.canvas_settings -> { //TODO
                        canvasViews.hideBottomToolbar()
                    }
                    else -> {}
                }
                true
            }
            canvasMenu.show()
        }
        ShowTipDialog(canvasViews.toolbarOptionsBtn, R.drawable.settings_outlined)
    }
}