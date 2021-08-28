package com.mipapadakis.canvas.ui.canvas.toolbar.bottom

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.tools.ShowTipDialog
import com.mipapadakis.canvas.ui.canvas.CanvasImageView


class TransformToolMenu(transformLayout: View, private val canvasImageView: CanvasImageView){
    private var toolTransformFlipHorizontallyBtn = transformLayout.findViewById<ImageButton>(R.id.property_transform_flip_horizontally_btn)
    private var toolTransformFlipVerticallyBtn = transformLayout.findViewById<ImageButton>(R.id.property_transform_flip_vertically_btn)
    private var toolTransformCropBtn = transformLayout.findViewById<TextView>(R.id.property_transform_crop_btn)

    init {
        toolTransformFlipHorizontallyBtn.setOnClickListener {
            canvasImageView.flipHorizontally()
        }
        toolTransformFlipVerticallyBtn.setOnClickListener {
            canvasImageView.flipVertically()
        }
        toolTransformCropBtn.setOnClickListener {
            Toast.makeText(transformLayout.context, "Not implemented yet", Toast.LENGTH_SHORT).show()
        }
        ShowTipDialog(toolTransformFlipHorizontallyBtn, R.drawable.flip_horizontally_outlined)
        ShowTipDialog(toolTransformFlipVerticallyBtn, R.drawable.flip_vertically_outlined)
    }
}