package com.mipapadakis.canvas.ui.toolbar.bottom

import android.view.View
import android.widget.ImageButton
import com.mipapadakis.canvas.R

import com.mipapadakis.canvas.ui.CanvasImageView


class TransformToolMenu(transformLayout: View, val canvasImageView: CanvasImageView){
    private var toolTransformFlipHorizontallyBtn = transformLayout.findViewById<ImageButton>(R.id.property_transform_flip_horizontally_btn)
    private var toolTransformFlipVerticallyBtn = transformLayout.findViewById<ImageButton>(R.id.property_transform_flip_vertically_btn)
    //private var toolTransformCropBtn = transformLayout.findViewById<TextView?>(R.id.property_transform_crop_btn) TODO

    init {
        toolTransformFlipHorizontallyBtn.setOnClickListener {
            canvasImageView.flipHorizontally()
        }
        toolTransformFlipVerticallyBtn.setOnClickListener {
            canvasImageView.flipVertically()
        }
    }
}