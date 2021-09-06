package com.mipapadakis.canvas.ui.canvas.toolbar.bottom

import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.ui.canvas.CanvasViews
import com.mipapadakis.canvas.ui.util.ColorValues
import com.mipapadakis.canvas.ui.util.ShowTipDialog
import com.mipapadakis.canvas.ui.canvas.toolbar.bottom.editors.ColorEditorHelper

class BucketProperties(canvasViews: CanvasViews){
    private var toolBucketColorBtn = canvasViews.toolBucketLayout.findViewById<ImageButton>(R.id.property_bucket_color_btn)
    private var toolBucketColorEditor = canvasViews.toolBucketLayout.findViewById<LinearLayout>(R.id.property_bucket_color_editor)

    init {
        ColorEditorHelper(canvasViews.owner, canvasViews.canvasViewModel, toolBucketColorEditor){ toolBucketColorEditor.visibility = View.GONE }
        toolBucketColorBtn.setOnClickListener {
            toolBucketColorEditor.visibility = View.VISIBLE
            doWhenTheViewIsVisible(toolBucketColorEditor){
                canvasViews.canvasViewModel.newColorHue = ColorValues.colorOnlyHue(canvasViews.canvasViewModel.getColor())
                canvasViews.canvasViewModel.setTemporaryColor(canvasViews.canvasViewModel.getColor())
            }
        }
        ShowTipDialog(toolBucketColorBtn, R.drawable.color_palette_outlined)
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