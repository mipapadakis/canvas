package com.mipapadakis.canvas

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.mipapadakis.canvas.ui.*
import com.mipapadakis.canvas.ui.create_canvas.CreateCanvasFragment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


private const val IMPORT_IMAGE_INTENT_KEY = CreateCanvasFragment.IMPORT_IMAGE_INTENT_KEY
private const val DIMENSION_WIDTH_INTENT_KEY = CreateCanvasFragment.DIMENSION_WIDTH_INTENT_KEY
private const val DIMENSION_HEIGHT_INTENT_KEY = CreateCanvasFragment.DIMENSION_HEIGHT_INTENT_KEY
private const val WIDTH = CreateCanvasFragment.WIDTH
private const val HEIGHT = CreateCanvasFragment.HEIGHT

private const val FULL_ALPHA = 1f
private const val MEDIUM_ALPHA = 0.6f
private const val LOW_ALPHA = 0.3f

@SuppressLint("ClickableViewAccessibility")
class CanvasActivity : AppCompatActivity() {
    private var devicePixelWidth: Int = 0
    private var devicePixelHeight: Int = 0
    private var canvasWidth = 540
    private var canvasHeight = 984
    private lateinit var toast: Toast
    private var outRect = Rect()
    private var location = IntArray(2)

    //Views
    private lateinit var layoutCanvas: RelativeLayout
    private lateinit var canvasIV: CanvasImageView
    //private lateinit var drawingView: DrawingView
    private lateinit var toolbarOuterCardView: CardView
    private lateinit var toolbarInnerCardView: CardView
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
        //canvasViewModel = ViewModelProvider(this).get(CanvasViewModel::class.java)
        val layoutParamsCanvas = RelativeLayout.LayoutParams(devicePixelWidth, devicePixelHeight)
        layoutParamsCanvas.addRule(RelativeLayout.BELOW)
        layoutCanvas = findViewById(R.id.canvas_layout)
        canvasIV = CanvasImageView(this)
//        drawingView = DrawingView(this)
//        drawingView.setBrushSize(10f)


        //Receive intent from MainActivity:
        when {
            intent==null -> showToast("Error!")
            intent.getStringExtra(IMPORT_IMAGE_INTENT_KEY)!=null -> {
                val uri = intent.getStringExtra(IMPORT_IMAGE_INTENT_KEY)
                //Initialize canvasWidth and canvasHeight:
                if(uri!=null) getImageDimensionsFromUri(Uri.parse(uri))
                showToast("canvasWidth=$canvasWidth, canvasHeight=$canvasHeight")
                canvasIV.layoutParams = layoutParamsCanvas
                canvasIV.setImageURI(Uri.parse(uri))
                layoutCanvas.addView(canvasIV)
            }
            else -> {
                canvasWidth= intent.getIntExtra(DIMENSION_WIDTH_INTENT_KEY, 540)
                canvasHeight= intent.getIntExtra(DIMENSION_HEIGHT_INTENT_KEY, 984)
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
            override fun on2PointerDoubleTap(event: MotionEvent) {
                Log.i("CanvasTouchListener", "Background on2PointerDoubleTap")
                canvasIV.on2PointerDoubleTap(event)
            }
            override fun on1PointerLongPress(event: MotionEvent) {
                Log.i("CanvasTouchListener", "Background on1PointerLongPress")
                //TODO: set canvasIV center to event position
            }
        }))
        setToolbar()
    }

    /** Create a temp cv file. */
    private fun createCvFile(fileName: String?): File {
        @Suppress("DEPRECATION") val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                resources.configuration.locales.get(0)
            else resources.configuration.locale

        //val folder = getDir(currentDate, MODE_PRIVATE) //Create directory
        //https://developer.android.com/training/data-storage/app-specific#internal-create-cache
        //Create temp file in cacheDir
        return if(fileName==null || fileName.length<=3){
            val sdf = SimpleDateFormat("dd/M/yyyy, hh:mm", locale)
            val currentDate = sdf.format(Date())
            File.createTempFile("Temp file ($currentDate)", ".cv", cacheDir)
        } else File.createTempFile(fileName, ".cv", cacheDir) //Create temp file in cacheDir
        //TODO: If user sets a name for this project, rename this file accordingly.
        // if this is an existing project, don't create a new directory. Rather, must open the project's dir.
    }

    private fun getImageDimensionsFromUri(uri: Uri) {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(contentResolver.openInputStream(uri), null, options)
        canvasWidth = options.outWidth
        canvasHeight = options.outHeight
    }

    //Called as soon as canvasIV has been created.
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //Pass dimensions because canvas' width and height attributes are 0 at this point
        canvasIV.onAttachedToWindowInitializer(canvasWidth, canvasHeight)
    }

    private fun setToolbar() {
        toolbarOuterCardView = findViewById(R.id.toolbar_outer_card)
        toolbarInnerCardView = findViewById(R.id.toolbar_inner_card) //TODO: Its background color is the same as the brush color.
        toolbarVisibilityImageView = findViewById(R.id.toolbar_visibility) //TODO: OnClick, hide/show the toolbarButtonLayout. OnLongPress, drag the toolbar.
        toolbarButtonLayout = findViewById(R.id.toolbar_buttons)
        toolbarUndoBtn = findViewById(R.id.toolbar_button_undo) //TODO action stack.
        toolbarRedoBtn = findViewById(R.id.toolbar_button_redo)
        toolbarPaletteBtn = findViewById(R.id.toolbar_button_palette) //TODO menu of colors (onColorPick, change background color of toolbarInnerCardView)
        toolbarToolBtn = findViewById(R.id.toolbar_button_tool) //TODO menu & options for each tool
        toolbarOptionsBtn = findViewById(R.id.toolbar_button_options) //TODO menu. Contains canvas global options.
        setupToolbarMenus()
        var timer: CountDownTimer? = null
        var longPressed = false
        var dX = 0f
        var dY = 0f
        toolbarVisibilityImageView.setOnTouchListener { v, event ->
            val drawableOff = getDrawableFromId(R.drawable.baseline_visibility_off_black_36)
            val drawableOn = getDrawableFromId(R.drawable.baseline_visibility_black_36)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    longPressed = false
                    dX = -1f
                    dY = -1f
                    val longPressDelay = ViewConfiguration.getLongPressTimeout().toLong()
                    timer = object : CountDownTimer(longPressDelay, longPressDelay) {
                        override fun onTick(millisUntilFinished: Long) {}
                        override fun onFinish() {
                            longPressed = true
                            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            if (Build.VERSION.SDK_INT >= 26) {
                                vibrator.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator.vibrate(70)
                            }
                            dX = toolbarOuterCardView.x - event.rawX
                            dY = toolbarOuterCardView.y - event.rawY
                            toolbarOuterCardView.alpha = MEDIUM_ALPHA
                        }
                    }
                    timer?.start()
                }
                MotionEvent.ACTION_UP -> {
                    timer?.cancel()
                    if (!longPressed && inViewInBounds(v, event.rawX.toInt(), event.rawY.toInt())) {
                        if (toolbarButtonLayout.visibility == View.GONE) {
                            toolbarVisibilityImageView.setImageDrawable(drawableOff)
                            toolbarButtonLayout.visibility = View.VISIBLE
                            //toolbarOuterCardView.animate().x(v.x+toolbarButtonLayout.x/2).setDuration(0).start() TODO
                        } else {
                            toolbarVisibilityImageView.setImageDrawable(drawableOn)
                            toolbarButtonLayout.visibility = View.GONE
                        }
                    } else toolbarOuterCardView.alpha = FULL_ALPHA
                }
                MotionEvent.ACTION_CANCEL -> {
                    toolbarOuterCardView.alpha = FULL_ALPHA
                }
                MotionEvent.ACTION_MOVE -> {
                    timer?.cancel()
                    val navBarHeight = DeviceDimensions.getSoftKeyBarSize(this) + 10 // for Redmi 9: 220 pixels
                    val newY = when {
                        event.rawY + dY>layoutCanvas.bottom-navBarHeight-> layoutCanvas.bottom.toFloat()-navBarHeight //TODO debug
                        event.rawY + dY<layoutCanvas.top -> layoutCanvas.top.toFloat()
                        else -> event.rawY + dY
                    }
                    if (dX != -1f && dY != -1f)
                            toolbarOuterCardView.animate()
                            //.x(event.rawX + dX)
                            .y(newY)
                            .setDuration(0)
                            .start()
                }
            }
            true
        }
        toolbarInnerCardView.setCardBackgroundColor(CanvasColor.getColorFromId(this, CanvasPreferences.startingColorId))
        CanvasViewModel.paint.color = CanvasColor.getColorFromId(this, CanvasPreferences.startingColorId)

        /*TODO:
        toolbarOuterCardView = findViewById(R.id.toolbar_outer_card)
        toolbarInnerCardView = findViewById(R.id.toolbar_inner_card) //Its background color is the same as the brush color.
        toolbarVisibilityImageView = findViewById(R.id.toolbar_visibility) //OnClick, hide/show the toolbarButtonLayout. OnLongPress, drag the toolbar.
        toolbarButtonLayout = findViewById(R.id.toolbar_buttons)
        toolbarUndoBtn = findViewById(R.id.toolbar_button_undo) //TODO action stack.
        toolbarRedoBtn = findViewById(R.id.toolbar_button_redo)
        toolbarPaletteBtn = findViewById(R.id.toolbar_button_palette) //TODO palette menu
        toolbarToolBtn = findViewById(R.id.toolbar_button_tool) //TODO menu & options for each tool
        toolbarOptionsBtn = findViewById(R.id.toolbar_button_options) //TODO menu. Contains canvas global options.*/
    }

    private fun inViewInBounds(view: View, x: Int, y: Int): Boolean {
        view.getDrawingRect(outRect)
        view.getLocationOnScreen(location)
        outRect.offset(location[0], location[1])
        return outRect.contains(x, y)
    }

    private fun setupToolbarMenus() {
        //val v: View = findViewById(R.id.button2)
        toolbarPaletteBtn.setOnClickListener {
            val paletteMenu = PopupMenu(this, toolbarPaletteBtn)
            paletteMenu.menuInflater.inflate(R.menu.palette, paletteMenu.menu)
            paletteMenu.setOnMenuItemClickListener {
                Toast.makeText(applicationContext, it.title, Toast.LENGTH_SHORT).show()
                when (it.itemId) {
                    R.id.color_black -> {
                        CanvasViewModel.paint.color = CanvasColor.getColorFromId(this, R.color.black)
                    }
                    R.id.color_red -> {
                        CanvasViewModel.paint.color = CanvasColor.getColorFromId(this, R.color.red)
                    }
                    R.id.color_green -> {
                        CanvasViewModel.paint.color = CanvasColor.getColorFromId(this, R.color.green)
                    }
                    R.id.color_blue -> {
                        CanvasViewModel.paint.color = CanvasColor.getColorFromId(this, R.color.blue)
                    }
                    R.id.color_yellow -> {
                        CanvasViewModel.paint.color = CanvasColor.getColorFromId(this, R.color.yellow)
                    }
                    R.id.color_purple -> {
                        CanvasViewModel.paint.color = CanvasColor.getColorFromId(this, R.color.purple)
                    }
                    else -> {} //TODO palette
                }
                toolbarInnerCardView.setCardBackgroundColor(CanvasViewModel.paint.color)
                true
            }
            paletteMenu.show()
        }
        toolbarToolBtn.setOnClickListener {
            val toolsMenu = PopupMenu(this, toolbarToolBtn)
            toolsMenu.menuInflater.inflate(R.menu.tools, toolsMenu.menu)
            toolsMenu.setOnMenuItemClickListener {
                Toast.makeText(applicationContext, it.title, Toast.LENGTH_SHORT).show()
                when (it.itemId) {
                    R.id.tool_brush -> {
                    }
                    R.id.tool_bucket -> {
                    }
                    R.id.tool_eraser -> {
                    }
                    R.id.tool_eyedropper -> {
                    }
                    else -> {
                    }
                }
                true
            }
            toolsMenu.show()
        }

        toolbarOptionsBtn.setOnClickListener {
            //TODO specific menu for each tool
        }
    }

    fun getColorFromId(id: Int) = CanvasColor.getColorFromId(this, id)
    fun getDrawableFromId(id: Int) = ContextCompat.getDrawable(this, id)

    @Suppress("SameParameterValue")
    private fun showToast(text: String){
        if(text.length>30) toast.duration = Toast.LENGTH_LONG
        else toast.duration = Toast.LENGTH_SHORT
        toast.cancel()
        toast = Toast.makeText(this, text, toast.duration)
        toast.show()
    }
}