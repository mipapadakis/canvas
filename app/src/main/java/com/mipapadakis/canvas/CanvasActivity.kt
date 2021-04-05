package com.mipapadakis.canvas

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mipapadakis.canvas.ui.canvas.CanvasImageView
import com.mipapadakis.canvas.ui.canvas.CreateCanvasFragment
import com.mipapadakis.canvas.ui.canvas.DeviceDimensions


private const val IMPORT_IMAGE_INTENT_KEY = CreateCanvasFragment.IMPORT_IMAGE_INTENT_KEY
private const val DIMENSION_WIDTH_INTENT_KEY = CreateCanvasFragment.DIMENSION_WIDTH_INTENT_KEY
private const val DIMENSION_HEIGHT_INTENT_KEY = CreateCanvasFragment.DIMENSION_HEIGHT_INTENT_KEY
private const val WIDTH = CreateCanvasFragment.WIDTH
private const val HEIGHT = CreateCanvasFragment.HEIGHT

class CanvasActivity : AppCompatActivity() {
    private lateinit var layoutCanvas: RelativeLayout
    private lateinit var canvasIV: CanvasImageView
    private var devicePixelWidth: Int = 0
    private var devicePixelHeight: Int = 0
    private var canvasWidth = 540
    private var canvasHeight = 984
    private lateinit var toast: Toast

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canvas)
        toast = Toast(this)
        devicePixelWidth = DeviceDimensions.getWidth(this)
        devicePixelHeight = DeviceDimensions.getHeight(this)
        layoutCanvas = findViewById(R.id.canvas_layout)
        canvasIV = CanvasImageView(applicationContext)

        //Receive intent from MainActivity:
        when {
            intent==null -> showToast("No intent!")
            intent.getStringExtra(IMPORT_IMAGE_INTENT_KEY)!=null -> {
                val uri = intent.getStringExtra(IMPORT_IMAGE_INTENT_KEY)
                val layoutParamsCanvas = RelativeLayout.LayoutParams(devicePixelWidth, devicePixelHeight)
                layoutParamsCanvas.addRule(RelativeLayout.BELOW)
                canvasIV.setImageURI(Uri.parse(uri))
                canvasIV.layoutParams = layoutParamsCanvas
                layoutCanvas.addView(canvasIV)
                //TODO find canvasWidth and canvasHeight
            }
            else -> {
                canvasWidth= intent.getIntExtra(DIMENSION_WIDTH_INTENT_KEY, 540)
                canvasHeight= intent.getIntExtra(DIMENSION_HEIGHT_INTENT_KEY, 984)
                val layoutParamsCanvas = RelativeLayout.LayoutParams(canvasWidth, canvasHeight)
                layoutParamsCanvas.addRule(RelativeLayout.BELOW)
                canvasIV.layoutParams = layoutParamsCanvas

                val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                canvasIV.setImageBitmap(bitmap)

                layoutCanvas.addView(canvasIV)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        canvasIV.onAttachedToWindowInitializer()

    }

    @Suppress("SameParameterValue")
    private fun showToast(text: String){
        if(text.length>30) toast.duration = Toast.LENGTH_LONG
        else toast.duration = Toast.LENGTH_SHORT
        toast.cancel()
        toast = Toast.makeText(this, text, toast.duration)
        toast.show()
    }
}