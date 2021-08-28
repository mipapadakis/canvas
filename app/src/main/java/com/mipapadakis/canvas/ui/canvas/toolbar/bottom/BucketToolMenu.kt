package com.mipapadakis.canvas.ui.canvas.toolbar.bottom

import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import androidx.lifecycle.LifecycleOwner
import com.mipapadakis.canvas.CanvasActivityData
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.tools.ColorValues
import com.mipapadakis.canvas.tools.ShowTipDialog
import com.mipapadakis.canvas.ui.canvas.toolbar.bottom.editors.ColorEditorHelper

class BucketToolMenu(owner: LifecycleOwner, bucketLayout: View){
    private var toolBucketColorBtn = bucketLayout.findViewById<ImageButton>(R.id.property_bucket_color_btn)
    private var toolBucketColorEditor = bucketLayout.findViewById<LinearLayout>(R.id.property_bucket_color_editor)

    init {
        ColorEditorHelper(owner, toolBucketColorEditor){ toolBucketColorEditor.visibility = View.GONE }
        toolBucketColorBtn.setOnClickListener {
            toolBucketColorEditor.visibility = View.GONE
            toolBucketColorEditor.visibility = View.VISIBLE
            doWhenTheViewIsVisible(toolBucketColorEditor){
                CanvasActivityData.newColorHue = ColorValues.colorOnlyHue(CanvasActivityData.paint.color)
                CanvasActivityData.setTemporaryColor(CanvasActivityData.paint.color)
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