package com.mipapadakis.canvas

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.*
import android.text.InputFilter
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mipapadakis.canvas.tools.DeviceDimensions
import com.mipapadakis.canvas.ui.*
import com.mipapadakis.canvas.ui.create_canvas.CreateCanvasFragment
import com.mipapadakis.canvas.ui.toolbar.bottom.*
import com.mipapadakis.canvas.ui.toolbar.bottom.editors.LayerListAdapter
import java.util.*
import kotlin.math.abs


private const val IMPORT_IMAGE_INTENT_KEY = CreateCanvasFragment.IMPORT_IMAGE_INTENT_KEY
private const val DIMENSION_WIDTH_INTENT_KEY = CreateCanvasFragment.DIMENSION_WIDTH_INTENT_KEY
private const val DIMENSION_HEIGHT_INTENT_KEY = CreateCanvasFragment.DIMENSION_HEIGHT_INTENT_KEY
private const val WIDTH = CreateCanvasFragment.WIDTH
private const val HEIGHT = CreateCanvasFragment.HEIGHT

@SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
class CanvasActivity : AppCompatActivity() {
    private var devicePixelWidth: Int = 0
    private var devicePixelHeight: Int = 0
    private lateinit var toast: Toast
    private var canvasWidth = 540
    private var canvasHeight = 984
    private var safeToExit = false
    private var outRect = Rect()
    private var location = IntArray(2)

    ////////////////////////////////////////////Views///////////////////////////////////////////////x
    private lateinit var layoutCanvas: RelativeLayout
    private lateinit var canvasIV: CanvasImageView
    private lateinit var layerRecyclerView: RecyclerView
    //Toolbar
    private lateinit var toolbarOuterCardView: CardView
    private lateinit var toolbarInnerCardView: CardView
    private lateinit var toolbarMoveImageView: ImageView
    private lateinit var toolbarButtonLayout: LinearLayout
    private lateinit var toolbarUndoBtn: ImageButton
    private lateinit var toolbarRedoBtn: ImageButton
    private lateinit var toolbarLayerBtn: ImageButton
    private lateinit var toolbarToolBtn: ImageButton
    private lateinit var toolbarCanvasBtn: ImageButton
    private lateinit var bottomToolbarOuterCardView: CardView
    private lateinit var bottomToolbarInnerCardView: CardView
    //Properties
    private lateinit var properties: Array<View>
    private lateinit var toolBrushLayout: View
    private lateinit var toolEraserLayout: View
    private lateinit var toolBucketLayout: View
    private lateinit var toolSelectLayout: View
    private lateinit var toolShapeLayout: View
    private lateinit var toolTextLayout: View
    private lateinit var toolCanvasLayersLayout: View
    private lateinit var toolCanvasLayersAddBtn: ImageButton
    private lateinit var toolCanvasTransformLayout: View
//    private lateinit var toolCanvasSettingsBtn: AppCompatButton //TODO
//    private lateinit var toolCanvasSaveBtn: AppCompatButton //TODO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canvas)
        //hideSystemUI() //TODO?
        toast = Toast(this)
        devicePixelWidth = DeviceDimensions.getWidth(this)
        devicePixelHeight = DeviceDimensions.getHeight(this)
        //canvasViewModel = ViewModelProvider(this).get(CanvasViewModel::class.java)
        CanvasViewModel.resetAttributes()
        layoutCanvas = findViewById(R.id.canvas_layout)
        toolCanvasLayersLayout = findViewById(R.id.canvas_layers_properties)
        layerRecyclerView = toolCanvasLayersLayout.findViewById(R.id.property_layers_recycler_view)
        canvasIV = CanvasImageView(this){
            layerRecyclerView.adapter?.notifyDataSetChanged()
        }

        //Receive intent from MainActivity:
        when {
            intent==null -> showToast("Error!")
            intent.getStringExtra(IMPORT_IMAGE_INTENT_KEY)!=null -> {
                val uri = intent.getStringExtra(IMPORT_IMAGE_INTENT_KEY)
                //Initialize canvasWidth and canvasHeight:
                if(uri!=null) getImageDimensionsFromUri(Uri.parse(uri))
                //showToast("canvasWidth=$canvasWidth, canvasHeight=$canvasHeight")
                Log.i("CanvasDimensions", "canvasWidth=$canvasWidth, canvasHeight=$canvasHeight")
                val layoutParamsCanvas = RelativeLayout.LayoutParams(canvasWidth, canvasHeight)
                layoutParamsCanvas.addRule(RelativeLayout.BELOW)
                canvasIV.layoutParams = layoutParamsCanvas
                canvasIV.setImageURI(Uri.parse(uri))
                layoutCanvas.addView(canvasIV)
            }
            else -> {
                canvasWidth = intent.getIntExtra(DIMENSION_WIDTH_INTENT_KEY, canvasWidth)
                canvasHeight = intent.getIntExtra(DIMENSION_HEIGHT_INTENT_KEY, canvasHeight)
                val layoutParamsCanvas = RelativeLayout.LayoutParams(canvasWidth, canvasHeight)
                layoutParamsCanvas.addRule(RelativeLayout.BELOW)
                canvasIV.layoutParams = layoutParamsCanvas
                val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(CanvasPreferences.startingCanvasColor)
                canvasIV.setImageBitmap(bitmap)
                layoutCanvas.addView(canvasIV)
            }
        }

        //Handle background touches:
        layoutCanvas.setOnTouchListener(MyTouchListener(object : MyTouchListener.MultiTouchListener {
            override fun on1PointerTap(event: MotionEvent) { toggleToolbarVisibility() }
            override fun on2PointerDoubleTap(event: MotionEvent) {
                Log.i("CanvasTouchListener", "Background on2PointerDoubleTap")
                canvasIV.on2PointerDoubleTap(event)
            }
            override fun on1PointerLongPress(event: MotionEvent) { toggleToolbarVisibility() }
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

        //Create recyclerView with the list of layers.
        layerRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        layerRecyclerView.itemAnimator = DefaultItemAnimator()
        layerRecyclerView.adapter = LayerListAdapter(canvasIV, resources)
        getItemTouchHelper().attachToRecyclerView(layerRecyclerView)
//        layerRecyclerView.addOnItemTouchListener(
//            RecyclerViewTouchListener(this, layerRecyclerView, object : RecyclerViewTouchListener.ClickListener {
//                override fun onItemClick(view: View?, position: Int) {}
//                override fun onLongClick(view: View?, position: Int) {}
//                override fun onDoubleClick(view: View?, position: Int) {}
//                override fun onBackgroundClick() {}
//            })
//        )
    }

    private fun getItemTouchHelper(): ItemTouchHelper {
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT + ItemTouchHelper.RIGHT, 0) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                if(layerRecyclerView.adapter is LayerListAdapter)
                    (layerRecyclerView.adapter as LayerListAdapter).onItemMove( viewHolder.adapterPosition, target.adapterPosition )
                return true
            }
            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    if (viewHolder is LayerListAdapter.ItemTouchHelperViewHolder) {
                        (viewHolder as LayerListAdapter.ItemTouchHelperViewHolder).onItemSelected()
                    }
                }
                super.onSelectedChanged(viewHolder, actionState)
            }
            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder){
                super.clearView(recyclerView, viewHolder)
                if (viewHolder is LayerListAdapter.ItemTouchHelperViewHolder) {
                    (viewHolder as LayerListAdapter.ItemTouchHelperViewHolder).onItemDropped()
                }
            }
            override fun isLongPressDragEnabled(): Boolean { return true }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
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
        toolbarInnerCardView = findViewById(R.id.toolbar_inner_card)
        toolbarMoveImageView = findViewById(R.id.toolbar_move) //OnClick, hide/show the toolbarButtonLayout. OnLongPress, drag the toolbar.
        toolbarButtonLayout = findViewById(R.id.toolbar_buttons)
        toolbarUndoBtn = findViewById(R.id.toolbar_button_undo)
        toolbarRedoBtn = findViewById(R.id.toolbar_button_redo)
        toolbarLayerBtn = findViewById(R.id.toolbar_button_layers)
        toolbarToolBtn = findViewById(R.id.toolbar_button_tool) //TODO menu & options for each tool
        toolbarCanvasBtn = findViewById(R.id.toolbar_button_options) //TODO canvas menu. Contains canvas global options.
        bottomToolbarOuterCardView = findViewById(R.id.bottom_toolbar_outer_card)
        bottomToolbarInnerCardView = findViewById(R.id.bottom_toolbar_inner_card)
        //TODO menu of colors (onColorPick, change background color of toolbarInnerCardView)

        var timer: CountDownTimer? = null
        var longPressed = false
        var dX = 0f
        var dY = 0f
        toolbarMoveImageView.setOnTouchListener { v, event -> //TODO fix bounds
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
                            toolbarOuterCardView.alpha = CanvasPreferences.MEDIUM_ALPHA
                        }
                    }
                    timer?.start()
                }
                MotionEvent.ACTION_UP -> {
                    timer?.cancel()
                    if (!longPressed && inViewInBounds(v, event.rawX.toInt(), event.rawY.toInt())) {
                        toggleToolbarVisibility()
                    } else toolbarOuterCardView.alpha = CanvasPreferences.FULL_ALPHA
                }
                MotionEvent.ACTION_CANCEL -> {
                    toolbarOuterCardView.alpha = CanvasPreferences.FULL_ALPHA
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
        toolbarLayerBtn.setOnClickListener {
            hideProperties()
            layerRecyclerView.adapter?.notifyDataSetChanged()
            toolCanvasLayersLayout.visibility = View.VISIBLE
            showBottomToolbar()
        }
        addPopMenuTools()
        addPopMenuCanvas()

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
        toolEraserLayout = findViewById(R.id.tool_eraser_properties)
        toolBucketLayout = findViewById(R.id.tool_bucket_properties)
        toolSelectLayout = findViewById(R.id.tool_select_properties)
        toolShapeLayout = findViewById(R.id.tool_shape_properties)
        toolTextLayout = findViewById(R.id.tool_text_properties)
        toolCanvasLayersAddBtn = toolCanvasLayersLayout.findViewById(R.id.property_layers_add_btn)
        toolCanvasTransformLayout = findViewById(R.id.canvas_transform_properties)
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

        // setup Toolbar Menus

        //BRUSH
        BrushToolMenu(this, toolBrushLayout)

        //ERASER
        EraserToolMenu(toolEraserLayout)

        //BUCKET
        BucketToolMenu(this, toolBucketLayout)

        //SELECT
        SelectToolMenu() //TODO

        //SHAPE
        ShapeToolMenu(this, toolShapeLayout){
            //OnShapeChanged:
            toolbarToolBtn.setImageResource(CanvasViewModel.shapeType)
        }

        //TEXT
        TextToolMenu(this, toolTextLayout)

        //LAYERS
        toolCanvasLayersAddBtn.setOnClickListener {
            if(CanvasViewModel.cvImage.layerCount()>=15){
                showToast("Try merging some of the existing layers first to save up memory.")
                return@setOnClickListener
            }
            CanvasViewModel.cvImage.newLayer()
            canvasIV.invalidateLayers()
            canvasIV.addActionToHistory(CanvasImageView.ACTION_LAYER_ADD)
        }

        //TRANSFORM
        TransformToolMenu(toolCanvasTransformLayout, canvasIV)

        //SAVE?

        //SETTINGS?
    }

    private fun addPopMenuCanvas() {
        toolbarCanvasBtn.setOnClickListener {
            val canvasMenu = PopupMenu(this, toolbarCanvasBtn)
            canvasMenu.menuInflater.inflate(R.menu.canvas, canvasMenu.menu)
            canvasMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.canvas_transform-> {
                        hideProperties()
                        showBottomToolbar()
                        toolCanvasTransformLayout.visibility = View.VISIBLE
                    }
                    R.id.canvas_save -> {
                        hideProperties()
                        hideBottomToolbar()
                        showSaveCanvasDialog(false)
                    }
                    R.id.canvas_settings -> { //TODO
                        hideProperties()
                        hideBottomToolbar()
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
                        toolbarToolBtn.setImageResource(R.drawable.baseline_brush_black_48)
                    }
                    R.id.tool_eraser -> {
                        toolEraserLayout.visibility = View.VISIBLE
                        CanvasViewModel.tool = CanvasViewModel.TOOL_ERASER
                        toolbarToolBtn.setImageResource(R.drawable.eraser_bold)
                    }
                    R.id.tool_bucket -> {
                        toolBucketLayout.visibility = View.VISIBLE
                        CanvasViewModel.tool = CanvasViewModel.TOOL_BUCKET
                        toolbarToolBtn.setImageResource(R.drawable.baseline_format_color_fill_black_48)
                    }
                    R.id.tool_eyedropper -> {
                        hideBottomToolbar()
                        CanvasViewModel.tool = CanvasViewModel.TOOL_EYEDROPPER
                        toolbarToolBtn.setImageResource(R.drawable.baseline_colorize_black_48)
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
                        toolbarToolBtn.setImageResource(R.drawable.baseline_title_black_48)
                    }
                    else -> {}
                }
                avoidTopAndBottomToolbarOverlap()
                true
            }
            toolsMenu.show()
        }
    }


    private fun showSaveCanvasDialog(askBeforeExit: Boolean){
        val layoutInflaterAndroid = LayoutInflater.from(this)
        val view: View = layoutInflaterAndroid.inflate(R.layout.input_dialog, null)
        val alertDialogBuilderUserInput: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialogBuilderUserInput.setView(view)
        val inputTitle = view.findViewById<EditText>(R.id.input_title)
        val dialogTitle = view.findViewById<TextView>(R.id.dialog_title)
        val dialogSubtitle = view.findViewById<TextView>(R.id.dialog_subtitle)
        val fileTypeCanvas = view.findViewById<RadioButton>(R.id.dialog_fileType_canvas)
        val fileTypePng = view.findViewById<RadioButton>(R.id.dialog_fileType_png)
        val fileTypeJpeg = view.findViewById<RadioButton>(R.id.dialog_fileType_jpeg)
        val fileTypeBitmap = view.findViewById<RadioButton>(R.id.dialog_fileType_bitmap)

        val titleText = getString(R.string.save_canvas_title) + if(askBeforeExit) "?" else ""
        dialogTitle.text = titleText
        dialogSubtitle.text = getString(R.string.save_canvas_subtitle)
        inputTitle.setText(CanvasViewModel.cvImage.title)

        alertDialogBuilderUserInput
            .setIcon(R.drawable.baseline_save_black_48)
            //.setCancelable(false)
            .setPositiveButton(  "save" ) { _, _ -> }
            .setNegativeButton( if(askBeforeExit) "Exit without saving" else "cancel" ) { dialogBox, _ ->
                hideKeyboard(inputTitle)
                dialogBox.cancel()
                if(askBeforeExit){
                    safeToExit = true
                    onBackPressed()
                }
            }
        val alertDialog = alertDialogBuilderUserInput.create()
        alertDialog.show()
        alertDialog.setCanceledOnTouchOutside(false)

        //Filter out the special characters ?:"*|/\<>
        val filter = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                if (source!=null && ("?:.\"*|/\\<>").contains(source[i])) {
                    showToast(getString(R.string.wrong_filename_warning))
                    return@InputFilter source.subSequence(start,i)
                }
            }
            null
        }
        //Focus the title, open soft keyboard.
        inputTitle.filters = arrayOf(filter)
        inputTitle.focusAndShowKeyboard()
        //toggleKeyboard(inputTitle, true)

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
            if (TextUtils.isEmpty(inputTitle.text.toString())) {
                showToast("You need to enter a title!")
                return@OnClickListener
            }

            CanvasViewModel.cvImage.title = inputTitle.text.toString()
            when {
                fileTypeCanvas.isChecked -> CanvasViewModel.cvImage.fileType = CanvasViewModel.FILETYPE_CANVAS
                fileTypePng.isChecked -> CanvasViewModel.cvImage.fileType = CanvasViewModel.FILETYPE_PNG
                fileTypeJpeg.isChecked -> CanvasViewModel.cvImage.fileType = CanvasViewModel.FILETYPE_JPEG
                fileTypeBitmap.isChecked -> CanvasViewModel.cvImage.fileType = CanvasViewModel.FILETYPE_BITMAP
            }
            //Update undo history with the new title and filetype
            for(action in CanvasViewModel.history){
                action.actionCvImage.title = CanvasViewModel.cvImage.title
                action.actionCvImage.fileType = CanvasViewModel.cvImage.fileType
            }

            //Save Canvas:
            hideKeyboard(inputTitle)
            alertDialog.dismiss()
            saveCvImage()
            if(askBeforeExit){
                safeToExit = true
                onBackPressed()
            }
        })
        alertDialog.setOnDismissListener {
            hideKeyboard(inputTitle)
        }
        alertDialog.setOnCancelListener {
            hideKeyboard(inputTitle)
        }
    }

    private fun saveCvImage(){
        //TODO save .cv file (containing all data from the current cvImage)
        showToast("Saved As \"${CanvasViewModel.cvImage.getFilenameWithExtension(this)}\"")
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
    //https://developer.squareup.com/blog/showing-the-android-keyboard-reliably/
    fun View.focusAndShowKeyboard() {
        fun View.showTheKeyboardNow() {
            if (isFocused) {
                post {
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }
        requestFocus()
        if (hasWindowFocus()) {
            showTheKeyboardNow()
        } else {
            // We need to wait until the window gets focus.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                viewTreeObserver.addOnWindowFocusChangeListener(
                    object : ViewTreeObserver.OnWindowFocusChangeListener {
                        override fun onWindowFocusChanged(hasFocus: Boolean) {
                            // This notification will arrive just before the InputMethodManager gets set up.
                            if (hasFocus) {
                                this@focusAndShowKeyboard.showTheKeyboardNow()
                                // It’s very important to remove this listener once we are done.
                                viewTreeObserver.removeOnWindowFocusChangeListener(this)
                            }
                        }
                    })
            }
        }
    }

//    private fun toggleKeyboard(view: View, show: Boolean) {
//        val inputMethodManger: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//        if(show) inputMethodManger.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0) //inputMethodManger.showSoftInput(view, 0)//
//        else inputMethodManger.hideSoftInputFromWindow(view.windowToken, 0)
//    }

    private fun inViewInBounds(view: View, x: Int, y: Int): Boolean {
        view.getDrawingRect(outRect)
        view.getLocationOnScreen(location)
        outRect.offset(location[0], location[1])
        return outRect.contains(x, y)
    }

    private fun toggleToolbarVisibility(){
        if(toolbarButtonLayout.visibility == View.VISIBLE) hideToolbars()
        else showToolBars()
    }

    private fun showToolBars() {
        //val drawableOff = getDrawableFromId(R.drawable.baseline_visibility_off_black_24)
        toolbarMoveImageView.setImageResource(R.drawable.baseline_open_with_black_48)
        toolbarButtonLayout.visibility = View.VISIBLE
        if(hasVisibleProperties()) showBottomToolbar() else hideBottomToolbar()
    }

    private fun avoidTopAndBottomToolbarOverlap() {
        if (abs(bottomToolbarOuterCardView.y - toolbarOuterCardView.y) < bottomToolbarOuterCardView.height)
            toolbarOuterCardView.animate()
                .y(bottomToolbarOuterCardView.y - toolbarOuterCardView.height - toolbarOuterCardView.paddingBottom)
                .setDuration(0)
                .start()
    }

    private fun hideToolbars() {
        val drawableOn = getDrawableFromId(R.drawable.baseline_visibility_black_48)
        toolbarMoveImageView.setImageDrawable(drawableOn)
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
    private fun showBottomToolbar(){
        bottomToolbarOuterCardView.visibility = View.VISIBLE
        avoidTopAndBottomToolbarOverlap()
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

    override fun onBackPressed() {
        if(safeToExit) super.onBackPressed()
        else showSaveCanvasDialog(true)
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
}