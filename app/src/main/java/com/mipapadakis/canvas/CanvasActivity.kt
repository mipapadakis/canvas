package com.mipapadakis.canvas

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mipapadakis.canvas.ui.canvas.CreateCanvasFragment
import com.mipapadakis.canvas.ui.canvas.DeviceDimensions


private const val IMPORT_IMAGE_INTENT_KEY = CreateCanvasFragment.IMPORT_IMAGE_INTENT_KEY
private const val DIMENSION_WIDTH_INTENT_KEY = CreateCanvasFragment.DIMENSION_WIDTH_INTENT_KEY
private const val DIMENSION_HEIGHT_INTENT_KEY = CreateCanvasFragment.DIMENSION_HEIGHT_INTENT_KEY
private const val WIDTH = CreateCanvasFragment.WIDTH
private const val HEIGHT = CreateCanvasFragment.HEIGHT

class CanvasActivity : AppCompatActivity() {
    private var devicePixelWidth: Int = 0
    private var devicePixelHeight: Int = 0
    private lateinit var toast: Toast
    private var canvasWidth = 540
    private var canvasHeight = 984

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canvas)
        devicePixelWidth = DeviceDimensions.getWidth(this)
        devicePixelHeight = DeviceDimensions.getHeight(this)
        //showToast("Device dimensions: $devicePixelWidth x $devicePixelHeight")
        toast = Toast(this)

        //Receive intent from MainActivity:
        when {
            intent==null -> showToast("No intent!")
            intent.getStringExtra(IMPORT_IMAGE_INTENT_KEY)!=null -> {
                val uri = intent.getStringExtra(IMPORT_IMAGE_INTENT_KEY)
                findViewById<ImageView>(R.id.canvas_image).setImageURI(Uri.parse(uri))

                //TODO find canvasWidth and canvasHeight
            }
            else -> {
                canvasWidth= intent.getIntExtra(DIMENSION_WIDTH_INTENT_KEY, 540)
                canvasHeight= intent.getIntExtra(DIMENSION_HEIGHT_INTENT_KEY, 984)
                showToast("width = $canvasWidth, height = $canvasHeight")
            }
        }
    }

    private fun showToast(text: String){
        if(text.length>30) toast.duration = Toast.LENGTH_LONG
        else toast.duration = Toast.LENGTH_SHORT
        toast.cancel()
        toast = Toast.makeText(this, text, toast.duration)
        toast.show()
    }
}