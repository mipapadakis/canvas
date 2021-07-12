package com.mipapadakis.canvas

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mipapadakis.canvas.tools.DeviceDimensions
import com.mipapadakis.canvas.ui.*
import com.mipapadakis.canvas.ui.create_canvas.CreateCanvasFragment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


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
    private lateinit var canvasViewModel: CanvasViewModel
    private var devicePixelWidth: Int = 0
    private var devicePixelHeight: Int = 0
    private var canvasWidth = 540
    private var canvasHeight = 984
    private lateinit var toast: Toast
    private var outRect = Rect()
    private var location = IntArray(2)

    ////////////////////////////////////////////Views///////////////////////////////////////////////
    private lateinit var layoutCanvas: RelativeLayout
    private lateinit var canvasIV: CanvasImageView
    //Toolbar
    private lateinit var toolbarOuterCardView: CardView
    private lateinit var toolbarInnerCardView: CardView
    private lateinit var toolbarVisibilityImageView: ImageView
    private lateinit var toolbarButtonLayout: LinearLayout
    private lateinit var toolbarUndoBtn: ImageButton
    private lateinit var toolbarRedoBtn: ImageButton
    //private lateinit var toolbarPaletteBtn: ImageButton
    private lateinit var toolbarToolBtn: ImageButton
    private lateinit var toolbarCanvasBtn: ImageButton
    private lateinit var bottomToolbarOuterCardView: CardView
    private lateinit var bottomToolbarInnerCardView: CardView
    //Properties
    private lateinit var properties: Array<View>
    private lateinit var toolBrushLayout: LinearLayout
    private lateinit var toolBrushColorBtn: ImageButton
    private lateinit var toolBrushSizeBtn: AppCompatButton
    private lateinit var toolBrushTypeBtn: AppCompatButton
    private lateinit var toolBrushOpacityBtn: AppCompatButton
    private lateinit var toolEraserLayout: LinearLayout
    private lateinit var toolEraserSizeBtn: AppCompatButton
    private lateinit var toolEraserOpacityBtn: AppCompatButton
    private lateinit var toolBucketLayout: LinearLayout
    private lateinit var toolBucketColorBtn: ImageButton
    private lateinit var toolBucketOpacityBtn: AppCompatButton
    private lateinit var toolSelectLayout: LinearLayout
    private lateinit var toolSelectTypeBtn: AppCompatButton
    private lateinit var toolSelectMethodBtn: AppCompatButton
    private lateinit var toolShapeLayout: LinearLayout
    private lateinit var toolShapeColorBtn: ImageButton
    private lateinit var toolShapeTypeBtn: AppCompatButton
    private lateinit var toolShapeStrokeSizeBtn: AppCompatButton
    private lateinit var toolShapeStrokeTypeBtn: AppCompatButton
    private lateinit var toolShapeOpacityBtn: AppCompatButton
    private lateinit var toolTextLayout: LinearLayout
    private lateinit var toolTextFontBtn: AppCompatButton
    private lateinit var toolTextFontSizeBtn: AppCompatButton
    private lateinit var toolCanvasLayersLayout: LinearLayout
    private lateinit var toolCanvasLayersAddBtn: ImageButton
    private lateinit var toolCanvasLayersListBtn: ImageButton
    private lateinit var toolCanvasTransformLayout: LinearLayout
    private lateinit var toolCanvasTransformCropBtn: AppCompatButton
    private lateinit var toolCanvasTransformFlipBtn: AppCompatButton
    //TODO toolCanvasSaveLayout
    //TODO toolCanvasSettingsLayout



    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canvas)
        //hideSystemUI() //TODO 00000000000000000000000000000000000000000000000000000000000000000000
        toast = Toast(this)
        devicePixelWidth = DeviceDimensions.getWidth(this)
        devicePixelHeight = DeviceDimensions.getHeight(this)
        canvasViewModel = ViewModelProvider(this).get(CanvasViewModel::class.java)
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
                val layoutParamsCanvas = RelativeLayout.LayoutParams(canvasWidth, canvasHeight)
                layoutParamsCanvas.addRule(RelativeLayout.BELOW)
                canvasIV.layoutParams = layoutParamsCanvas
                canvasIV.setImageURI(Uri.parse(uri))
                layoutCanvas.addView(canvasIV)
            }
            else -> {
                canvasWidth = intent.getIntExtra(DIMENSION_WIDTH_INTENT_KEY, 540)
                canvasHeight = intent.getIntExtra(DIMENSION_HEIGHT_INTENT_KEY, 984)
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
        //TODO set pngBackground resource as background for canvasIV //////////////////////////////////////////////////

        //Handle background touches:
        layoutCanvas.setOnTouchListener(MyTouchListener(object : MyTouchListener.MultiTouchListener {
            //TODO
            override fun on1PointerTap(event: MotionEvent) { toggleToolbarVisibility() }
            override fun on2PointerDoubleTap(event: MotionEvent) {
                Log.i("CanvasTouchListener", "Background on2PointerDoubleTap")
                canvasIV.on2PointerDoubleTap(event)
            }
            override fun on1PointerLongPress(event: MotionEvent) {
                Log.i("CanvasTouchListener", "Background on1PointerLongPress")
                //TODO: set canvasIV center to event position
                toggleToolbarVisibility()
            }
            override fun on3PointerTap(event: MotionEvent) {
                Log.i("CanvasTouchListener", "Background on3PointerTap")
                canvasIV.on3PointerTap(event)
            }
            override fun onCancelTouch(event: MotionEvent?) {
                super.onCancelTouch(event)
                canvasIV.onCancelTouch(event)
            }
        }))
        setToolbar()
        setBottomToolbar()
        setupToolbarMenus()
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

    // https://stackoverflow.com/a/64828067/11535380
    @SuppressLint("InlinedApi")
    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
    }

    private fun setToolbar() {
        toolbarOuterCardView = findViewById(R.id.toolbar_outer_card)
        toolbarInnerCardView = findViewById(R.id.toolbar_inner_card) //TODO: Its background color is the same as the brush color.
        toolbarVisibilityImageView = findViewById(R.id.toolbar_visibility) //TODO: OnClick, hide/show the toolbarButtonLayout. OnLongPress, drag the toolbar.
        toolbarButtonLayout = findViewById(R.id.toolbar_buttons)
        toolbarUndoBtn = findViewById(R.id.toolbar_button_undo) //TODO action stack.
        toolbarRedoBtn = findViewById(R.id.toolbar_button_redo)
        //toolbarPaletteBtn = findViewById(R.id.toolbar_button_palette) //TODO menu of colors (onColorPick, change background color of toolbarInnerCardView)
        toolbarToolBtn = findViewById(R.id.toolbar_button_tool) //TODO menu & options for each tool
        toolbarCanvasBtn = findViewById(R.id.toolbar_button_options) //TODO menu. Contains canvas global options.
        bottomToolbarOuterCardView = findViewById(R.id.bottom_toolbar_outer_card)
        bottomToolbarInnerCardView = findViewById(R.id.bottom_toolbar_inner_card)

        var timer: CountDownTimer? = null
        var longPressed = false
        var dX = 0f
        var dY = 0f
        toolbarVisibilityImageView.setOnTouchListener { v, event ->

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
//                            val location = IntArray(2)
//                            toolbarVisibilityImageView.getLocationOnScreen(location)
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
                        toggleToolbarVisibility()
                    } else toolbarOuterCardView.alpha = FULL_ALPHA
                }
                MotionEvent.ACTION_CANCEL -> {
                    toolbarOuterCardView.alpha = FULL_ALPHA
                }
                MotionEvent.ACTION_MOVE -> {
                    timer?.cancel()
                    val upperBound = layoutCanvas.top
                    val lowerBound =
                        if(bottomToolbarOuterCardView.visibility==View.VISIBLE)
                            bottomToolbarOuterCardView.y - toolbarOuterCardView.height + toolbarOuterCardView.paddingBottom
                        else
                            (devicePixelHeight - toolbarOuterCardView.height).toFloat()
                    val newY = when {
                        event.rawY + dY > lowerBound -> lowerBound
                        event.rawY + dY < upperBound -> upperBound
                        else -> event.rawY + dY
                    }
                    if (dX != -1f && dY != -1f )
                        toolbarOuterCardView.animate()
                            //.x(event.rawX + dX)
                            .y(newY.toFloat())
                            .setDuration(0)
                            .start()
                }
            }
            true
        }
        toolbarUndoBtn.setOnClickListener { if(!canvasIV.undo()) showToast("can't undo") }
        toolbarRedoBtn.setOnClickListener { if(!canvasIV.redo()) showToast("can't redo") }

        CanvasViewModel.toolbarColor.observe(this, {
            toolbarInnerCardView.setCardBackgroundColor(CanvasViewModel.paint.color)
            bottomToolbarInnerCardView.setCardBackgroundColor(CanvasViewModel.paint.color)
        })
        //bottomToolbarInnerCardView.setCardBackgroundColor(CanvasColor.getColorFromId(this, CanvasPreferences.startingColorId))
        toolbarToolBtn.setImageResource(CanvasViewModel.tool)
        CanvasViewModel.setPaintColor(getColorFromId( CanvasPreferences.startingColorId))
    }

    private fun setBottomToolbar() {
        toolBrushLayout = findViewById(R.id.tool_brush_properties)
        toolBrushColorBtn = findViewById(R.id.property_brush_color)
        toolBrushSizeBtn = findViewById(R.id.property_brush_size)
        toolBrushTypeBtn = findViewById(R.id.property_brush_type)
        toolBrushOpacityBtn = findViewById(R.id.property_brush_opacity)
        toolEraserLayout = findViewById(R.id.tool_eraser_properties)
        toolEraserSizeBtn = findViewById(R.id.property_eraser_size)
        toolEraserOpacityBtn = findViewById(R.id.property_eraser_opacity)
        toolBucketLayout = findViewById(R.id.tool_bucket_properties)
        toolBucketColorBtn = findViewById(R.id.property_bucket_color)
        toolBucketOpacityBtn = findViewById(R.id.property_bucket_opacity)
        toolSelectLayout = findViewById(R.id.tool_select_properties)
        toolSelectTypeBtn = findViewById(R.id.property_select_type)
        toolSelectMethodBtn = findViewById(R.id.property_select_method)
        toolShapeLayout = findViewById(R.id.tool_shape_properties)
        toolShapeColorBtn = findViewById(R.id.property_shape_color)
        toolShapeTypeBtn = findViewById(R.id.property_shape_type)
        toolShapeStrokeSizeBtn = findViewById(R.id.property_shape_stroke_size)
        toolShapeStrokeTypeBtn = findViewById(R.id.property_shape_stroke_type)
        toolShapeOpacityBtn = findViewById(R.id.property_shape_opacity)
        toolTextLayout = findViewById(R.id.tool_text_properties)
        toolTextFontBtn = findViewById(R.id.property_text_font)
        toolTextFontSizeBtn = findViewById(R.id.property_text_font_size)
        toolCanvasLayersLayout = findViewById(R.id.canvas_layers_properties)
        toolCanvasLayersAddBtn = findViewById(R.id.property_layers_add)
        toolCanvasLayersListBtn = findViewById(R.id.property_layers_list)
        toolCanvasTransformLayout = findViewById(R.id.canvas_transform_properties)
        toolCanvasTransformCropBtn = findViewById(R.id.property_transform_crop)
        toolCanvasTransformFlipBtn = findViewById(R.id.property_transform_flip)
        properties = arrayOf(
            toolBrushLayout,
            toolEraserLayout,
            toolBucketLayout,
            toolSelectLayout,
            toolShapeLayout,
            toolTextLayout,
            toolCanvasLayersLayout,
            toolCanvasTransformLayout)
        hideProperties()
        if(CanvasViewModel.tool == CanvasViewModel.TOOL_BRUSH)
            toolBrushLayout.visibility = View.VISIBLE
    }

    private fun inViewInBounds(view: View, x: Int, y: Int): Boolean {
        view.getDrawingRect(outRect)
        view.getLocationOnScreen(location)
        outRect.offset(location[0], location[1])
        return outRect.contains(x, y)
    }

    private fun setupToolbarMenus() {
        addPopMenuTools()
        addPopMenuCanvas()

        /** PATTERN DRAWING:
        val pngBackgroundPattern = BitmapFactory.decodeResource(getResources(), R.drawable.png_background_pattern)
        val patternBmpShader = BitmapShader( pngBackgroundPattern, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        paint.shader = patternBmpShader
         * */

        //BRUSH
        addPopMenuColor(toolBrushColorBtn)
        toolBrushSizeBtn.setOnClickListener {
            numberPicker("Brush Size", "Choose number of pixels:", CanvasViewModel.paint.strokeWidth.toInt()){
                CanvasViewModel.paint.strokeWidth = it.toFloat()
            }
        }

        //TODO brush type
        toolBrushOpacityBtn.setOnClickListener {
            numberPicker("Brush Opacity", "Choose percentage:", CanvasViewModel.paint.alpha*100/255){
                CanvasViewModel.paint.alpha = it*255/100
            }
        }

        //ERASER
        toolEraserSizeBtn.setOnClickListener {
            numberPicker("Eraser Size", "Choose number of pixels:", CanvasViewModel.eraserPaint.strokeWidth.toInt()){
                CanvasViewModel.eraserPaint.strokeWidth = it.toFloat()
            }
        }
        toolEraserOpacityBtn.setOnClickListener {
            numberPicker("Eraser Opacity", "Choose percentage:", CanvasViewModel.eraserPaint.alpha*100/255){
                CanvasViewModel.eraserPaint.alpha = it*255/100
            }
        }

        //BUCKET
        addPopMenuColor(toolBucketColorBtn)
        toolBucketOpacityBtn.setOnClickListener {
            numberPicker("Bucket Opacity", "Choose percentage:", CanvasViewModel.bucketPaint.alpha*100/255){
                CanvasViewModel.bucketPaint.alpha = it*255/100
            }
        }

        //SELECT
        //TODO toolSelectTypeBtn
        //TODO toolSelectMethodBtn

        //SHAPE
        addPopMenuColor(toolShapeColorBtn)
        addPopMenuShapeType()
        toolShapeStrokeSizeBtn.setOnClickListener {
            numberPicker("Shape Stroke Size", "Choose number of pixels:", CanvasViewModel.shapePaint.strokeWidth.toInt()){
                CanvasViewModel.shapePaint.strokeWidth = it.toFloat()
            }
        }
        //TODO stroke type
        toolShapeOpacityBtn.setOnClickListener {
            numberPicker("Shape Opacity", "Choose percentage:", CanvasViewModel.shapePaint.alpha*100/255){
                CanvasViewModel.shapePaint.alpha = it*255/100
            }
        }

        //TEXT
        toolTextFontSizeBtn.setOnClickListener {
            numberPicker("Font Size", "Choose pixel height:", CanvasViewModel.textFontSize){
                CanvasViewModel.textFontSize = it
            }
        }

        //LAYERS
        //todo add/copy/delete new layer
        //todo choose layer

        //TRANSFORM
        addPopMenuTransformFlip()
        //todo crop

    }

    private fun addPopMenuColor(btn: ImageButton){
        btn.setOnClickListener {
            val paletteMenu = PopupMenu(this, btn)
            paletteMenu.menuInflater.inflate(R.menu.palette, paletteMenu.menu)
            paletteMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.color_black -> { CanvasViewModel.setPaintColor(getColorFromId(R.color.black)) }
                    R.id.color_red -> { CanvasViewModel.setPaintColor(getColorFromId( R.color.red)) }
                    R.id.color_green -> { CanvasViewModel.setPaintColor(getColorFromId( R.color.green)) }
                    R.id.color_blue -> { CanvasViewModel.setPaintColor(getColorFromId( R.color.blue)) }
                    R.id.color_yellow -> { CanvasViewModel.setPaintColor(getColorFromId( R.color.yellow)) }
                    R.id.color_purple -> { CanvasViewModel.setPaintColor(getColorFromId( R.color.purple)) }
                    else -> {} //TODO palette
                }
                toolbarInnerCardView.setCardBackgroundColor(CanvasViewModel.paint.color)
                bottomToolbarInnerCardView.setCardBackgroundColor(CanvasViewModel.paint.color)
                true
            }
            paletteMenu.show()
        }
    }

    private fun numberPicker(title: String, message: String, currentValue: Int, positive: (number: Int) -> Unit){
        val numberPicker = NumberPicker(this)
        numberPicker.minValue = 1
        numberPicker.maxValue = 100
        numberPicker.value = currentValue

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setView(numberPicker)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { _, _ ->
            positive(numberPicker.value)
        }
        builder.setNegativeButton( "CANCEL") { _, _ ->}
        builder.create()
        builder.show()
    }


    /** Don't need this, since I'm using the drawable IDs as key identifiers.
     *  For example, CanvasViewModel.SHAPE_TYPE_LINE == R.drawable.baseline_show_chart_black_36.
     * private fun getCurrentShapeId(): Int{
        return when (CanvasViewModel.shapeType) {
            CanvasViewModel.SHAPE_TYPE_LINE -> { R.drawable.baseline_show_chart_black_36 }
            CanvasViewModel.SHAPE_TYPE_SQUARE -> { R.drawable.baseline_check_box_outline_blank_black_36 }
            CanvasViewModel.SHAPE_TYPE_RECTANGLE -> { R.drawable.baseline_crop_16_9_black_36 }
            CanvasViewModel.SHAPE_TYPE_CIRCLE -> { R.drawable.baseline_panorama_fish_eye_black_36 }
            CanvasViewModel.SHAPE_TYPE_OVAL -> { R.drawable.oval }
            CanvasViewModel.SHAPE_TYPE_POLYGON -> { R.drawable.baseline_star_outline_black_36 }
            CanvasViewModel.SHAPE_TYPE_TRIANGLE -> { R.drawable.baseline_change_history_black_36 }
            CanvasViewModel.SHAPE_TYPE_ARROW -> { R.drawable.baseline_east_black_36 }
            CanvasViewModel.SHAPE_TYPE_CALLOUT -> { R.drawable.baseline_chat_bubble_outline_black_36 }
            else -> {R.drawable.baseline_show_chart_black_36}
        }
    }*/
    private fun addPopMenuShapeType(){
        toolShapeTypeBtn.setOnClickListener {
            val menu = PopupMenu(this, toolShapeTypeBtn)
            menu.menuInflater.inflate(R.menu.shapes, menu.menu)
            menu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.shape_line -> {CanvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_LINE}
                    R.id.shape_square -> { CanvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_SQUARE}
                    R.id.shape_rectangle -> { CanvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_RECTANGLE }
                    R.id.shape_circle -> { CanvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_CIRCLE }
                    R.id.shape_oval -> { CanvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_OVAL }
                    R.id.shape_polygon -> { CanvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_POLYGON }
                    R.id.shape_triangle -> { CanvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_TRIANGLE }
                    R.id.shape_arrow -> { CanvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_ARROW }
                    R.id.shape_callout -> { CanvasViewModel.shapeType = CanvasViewModel.SHAPE_TYPE_CALLOUT }
                    else -> {}
                }
                toolbarToolBtn.setImageResource(CanvasViewModel.shapeType)
                true
            }
            menu.show()
        }
    }
    private fun addPopMenuTransformFlip(){
        toolCanvasTransformFlipBtn.setOnClickListener {
            val menu = PopupMenu(this, toolCanvasTransformFlipBtn)
            menu.menuInflater.inflate(R.menu.transform_flip, menu.menu)
            menu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.flip_vertically -> { canvasIV.flipVertically() }
                    R.id.flip_horizontally-> { canvasIV.flipHorizontally() }
                }
                true
            }
            menu.show()
        }
    }

    private fun addPopMenuCanvas() {
        toolbarCanvasBtn.setOnClickListener {
            val canvasMenu = PopupMenu(this, toolbarCanvasBtn)
            canvasMenu.menuInflater.inflate(R.menu.canvas, canvasMenu.menu)
            canvasMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.canvas_layers -> {
                        hideProperties()
                        showBottomToolbar()
                        toolCanvasLayersLayout.visibility = View.VISIBLE
                    }
                    R.id.canvas_transform-> {
                        hideProperties()
                        showBottomToolbar()
                        toolCanvasTransformLayout.visibility = View.VISIBLE
                    }
                    R.id.canvas_save -> {
                        hideProperties()
                        showToast("Save!")
                    }
                    R.id.canvas_settings -> {
                        hideProperties()
                        showToast("Settings!")
                    }
                    else -> {
                        hideProperties()
                        hideBottomToolbar()
                    }
                }
                true
            }
            canvasMenu.show()
        }
    }

    private fun addPopMenuTools(){
        toolbarToolBtn.setOnClickListener {
            val toolsMenu = PopupMenu(this, toolbarToolBtn)
            toolsMenu.menuInflater.inflate(R.menu.tools, toolsMenu.menu)
            toolsMenu.setOnMenuItemClickListener {
                //Toast.makeText(applicationContext, it.title, Toast.LENGTH_SHORT).show()
                hideProperties()
                showBottomToolbar()
                when (it.itemId) {
                    R.id.tool_brush -> {
                        CanvasViewModel.tool = CanvasViewModel.TOOL_BRUSH
                        toolBrushLayout.visibility = View.VISIBLE
                        toolbarToolBtn.setImageResource(R.drawable.baseline_brush_black_36)
                    }
                    R.id.tool_eraser -> {
                        toolEraserLayout.visibility = View.VISIBLE
                        CanvasViewModel.tool = CanvasViewModel.TOOL_ERASER
                        toolbarToolBtn.setImageResource(R.drawable.eraser) //TODO
                    }
                    R.id.tool_bucket -> {
                        toolBucketLayout.visibility = View.VISIBLE
                        CanvasViewModel.tool = CanvasViewModel.TOOL_BUCKET
                        toolbarToolBtn.setImageResource(R.drawable.baseline_format_color_fill_black_36)
                    }
                    R.id.tool_eyedropper -> {
                        hideBottomToolbar()
                        CanvasViewModel.tool = CanvasViewModel.TOOL_EYEDROPPER
                        toolbarToolBtn.setImageResource(R.drawable.baseline_colorize_black_36)
                    }
                    R.id.tool_select -> {
                        toolSelectLayout.visibility = View.VISIBLE
                        CanvasViewModel.tool = CanvasViewModel.TOOL_SELECT
                        toolbarToolBtn.setImageResource(R.drawable.select_rectangular)
                    }
                    R.id.tool_shape -> {
                        toolShapeLayout.visibility = View.VISIBLE
                        CanvasViewModel.tool = CanvasViewModel.TOOL_SHAPE
                        toolbarToolBtn.setImageResource(CanvasViewModel.shapeType)
                    }
                    R.id.tool_text -> {
                        toolTextLayout.visibility = View.VISIBLE
                        CanvasViewModel.tool = CanvasViewModel.TOOL_TEXT
                        toolbarToolBtn.setImageResource(R.drawable.baseline_title_black_36)
                    }
                    else -> {}
                }
                true
            }
            toolsMenu.show()
        }
    }


    private fun toggleToolbarVisibility(){
        if(toolbarButtonLayout.visibility == View.VISIBLE) hideToolbars()
        else showToolBars()
    }

    private fun showToolBars() {
        //val drawableOff = getDrawableFromId(R.drawable.baseline_visibility_off_black_36)
        toolbarVisibilityImageView.setImageResource(R.drawable.baseline_visibility_off_black_36)
        toolbarButtonLayout.visibility = View.VISIBLE
        if(hasVisibleProperties()) showBottomToolbar()

        //If they overlap, move the toolbar above the bottom menu.
        if (abs(bottomToolbarOuterCardView.y - toolbarOuterCardView.y) < bottomToolbarOuterCardView.height)
            toolbarOuterCardView.animate()
                .y(bottomToolbarOuterCardView.y - toolbarOuterCardView.height - toolbarOuterCardView.paddingBottom)
                .setDuration(0)
                .start()
    }
    private fun hideToolbars() {
        val drawableOn = getDrawableFromId(R.drawable.baseline_visibility_black_36)
        toolbarVisibilityImageView.setImageDrawable(drawableOn)
        toolbarButtonLayout.visibility = View.GONE
        hideBottomToolbar()
    }
    private fun hideProperties(){
        for(p in properties){ if(p.visibility == View.VISIBLE) p.visibility = View.GONE}
    }
    private fun hasVisibleProperties(): Boolean{
        for(p in properties){ if(p.visibility == View.VISIBLE) return true }
        return false
    }
    private fun hideBottomToolbar(){ bottomToolbarOuterCardView.visibility = View.GONE }
    private fun showBottomToolbar(){ bottomToolbarOuterCardView.visibility = View.VISIBLE }

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