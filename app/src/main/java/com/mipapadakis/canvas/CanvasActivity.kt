package com.mipapadakis.canvas

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.mipapadakis.canvas.ui.canvas.CanvasImageView
import com.mipapadakis.canvas.ui.canvas.CreateCanvasFragment
import com.mipapadakis.canvas.ui.canvas.DeviceDimensions
import com.mipapadakis.canvas.ui.canvas.MyTouchListener


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
    //private lateinit var toolbarOuterCardView: MaterialCardView
    private lateinit var toolbarInnerCardView: MaterialCardView
    private lateinit var toolbarVisibilityImageView: ImageView
    private lateinit var toolbarButtonLayout: LinearLayout
    private lateinit var toolbarUndoBtn: ImageButton
    private lateinit var toolbarRedoBtn: ImageButton
    private lateinit var toolbarPaletteBtn: ImageButton
    private lateinit var toolbarToolBtn: ImageButton
    private lateinit var toolbarOptionsBtn: ImageButton

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canvas)
        toast = Toast(this)
        devicePixelWidth = DeviceDimensions.getWidth(this)
        devicePixelHeight = DeviceDimensions.getHeight(this)
        layoutCanvas = findViewById(R.id.canvas_layout)
        canvasIV = CanvasImageView(applicationContext)

        //TODO
        //toolbarOuterCardView = findViewById(R.id.toolbar_outer_card)
        toolbarInnerCardView = findViewById(R.id.toolbar_inner_card) //TODO: Its background color is the same as the brush color.
        toolbarVisibilityImageView = findViewById(R.id.toolbar_visibility) //TODO: OnClick, hide/show the toolbarButtonLayout. OnLongPress, drag the toolbar.
        toolbarButtonLayout = findViewById(R.id.toolbar_buttons)
        toolbarUndoBtn = findViewById(R.id.toolbar_button_undo) //TODO action stack.
        toolbarRedoBtn = findViewById(R.id.toolbar_button_redo)
        toolbarPaletteBtn = findViewById(R.id.toolbar_button_palette) //TODO menu of colors (onColorPick, change background color of toolbarInnerCardView)
        toolbarToolBtn = findViewById(R.id.toolbar_button_tool) //TODO menu & options for each tool
        toolbarOptionsBtn = findViewById(R.id.toolbar_button_tool_options) //TODO menu. Contains canvas global options.

        //Receive intent from MainActivity:
        when {
            intent==null -> showToast("Error!")
            intent.getStringExtra(IMPORT_IMAGE_INTENT_KEY)!=null -> {
                val uri = intent.getStringExtra(IMPORT_IMAGE_INTENT_KEY)
                val layoutParamsCanvas = RelativeLayout.LayoutParams(devicePixelWidth, devicePixelHeight)
                layoutParamsCanvas.addRule(RelativeLayout.BELOW)
                canvasIV.setImageURI(Uri.parse(uri))
                canvasIV.layoutParams = layoutParamsCanvas
                layoutCanvas.addView(canvasIV)
                //Initialize canvasWidth and canvasHeight:
                if(uri!=null) getImageDimensionsFromUri(Uri.parse(uri))
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

        //Handle background touches:
        layoutCanvas.setOnTouchListener(MyTouchListener(object : MyTouchListener.MultiTouchListener {
            override fun on1PointerTap(event: MotionEvent) {
                Log.i("CanvasTouchListener", "Background on1PointerTap")
            }

            override fun on2PointerTap(event: MotionEvent) {
                Log.i("CanvasTouchListener", "Background on2PointerTap")
            }

            override fun on1PointerDoubleTap(event: MotionEvent) {
                Log.i("CanvasTouchListener", "Background on1PointerDoubleTap")
            }

            override fun on2PointerDoubleTap(event: MotionEvent) {
                Log.i("CanvasTouchListener", "Background on2PointerDoubleTap")
                canvasIV.on2PointerDoubleTap(event)
            }

            override fun on1PointerLongPress(event: MotionEvent) {
                Log.i("CanvasTouchListener", "Background on1PointerLongPress")
                //TODO: set canvasIV center to event position
            }

            override fun on2PointerLongPress(event: MotionEvent) {
                Log.i("CanvasTouchListener", "Background on2PointerLongPress")
            }

            override fun on1PointerDown(event: MotionEvent) {}

            override fun on2PointerDown(event: MotionEvent) {}

            override fun on1PointerUp(event: MotionEvent) {}

            override fun on2PointerUp(event: MotionEvent) {}

            override fun on3PointerUp(event: MotionEvent) {}

            override fun onPointerMove(event: MotionEvent) {}

            override fun onCancelTouch() {
                Log.i("CanvasTouchListener", "Background onCancelTouch")
            }
        }))
    }

    private fun getImageDimensionsFromUri(uri: Uri) {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream( contentResolver.openInputStream(uri),null, options)
        canvasWidth = options.outWidth
        canvasHeight = options.outHeight
    }

    //Called as soon as canvasIV has been created.
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //Pass dimensions because canva's width and height attributes are 0 at this point
        canvasIV.onAttachedToWindowInitializer(canvasWidth, canvasHeight)
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