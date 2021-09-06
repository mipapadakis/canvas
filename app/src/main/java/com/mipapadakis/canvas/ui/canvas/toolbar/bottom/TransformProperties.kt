package com.mipapadakis.canvas.ui.canvas.toolbar.bottom

import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.ui.util.ShowTipDialog
import com.mipapadakis.canvas.ui.canvas.CanvasViews


class TransformProperties(canvasViews: CanvasViews){
    private var transformFlipHorizontallyBtn = canvasViews.transformLayout.findViewById<ImageButton>(R.id.property_transform_flip_horizontally_btn)
    private var transformFlipVerticallyBtn = canvasViews.transformLayout.findViewById<ImageButton>(R.id.property_transform_flip_vertically_btn)
    private var transformCropBtn = canvasViews.transformLayout.findViewById<TextView>(R.id.property_transform_crop_btn)

    init {
        transformFlipHorizontallyBtn.setOnClickListener {
            canvasViews.canvasIV.flipHorizontally()
        }
        transformFlipVerticallyBtn.setOnClickListener {
            canvasViews.canvasIV.flipVertically()
        }
        transformCropBtn.setOnClickListener {
            Toast.makeText(canvasViews.transformLayout.context, "Not implemented yet", Toast.LENGTH_SHORT).show()
        }
        ShowTipDialog(transformFlipHorizontallyBtn, R.drawable.flip_horizontally_outlined)
        ShowTipDialog(transformFlipVerticallyBtn, R.drawable.flip_vertically_outlined)
    }
}