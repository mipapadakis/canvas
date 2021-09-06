package com.mipapadakis.canvas

import android.annotation.SuppressLint
import android.graphics.*
import android.net.Uri
import android.os.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.mipapadakis.canvas.ui.canvas.CanvasViews
import com.mipapadakis.canvas.ui.create_canvas.CreateCanvasFragment

private const val IMPORT_IMAGE_INTENT_KEY = CreateCanvasFragment.IMPORT_IMAGE_INTENT_KEY
private const val IMPORT_CV_IMAGE_INTENT_KEY = CreateCanvasFragment.IMPORT_CV_IMAGE_INTENT_KEY
private const val DIMENSION_WIDTH_INTENT_KEY = CreateCanvasFragment.DIMENSION_WIDTH_INTENT_KEY
private const val DIMENSION_HEIGHT_INTENT_KEY = CreateCanvasFragment.DIMENSION_HEIGHT_INTENT_KEY

@SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
class CanvasActivity : AppCompatActivity() {
    private lateinit var canvasViews: CanvasViews
    private lateinit var canvasViewModel: CanvasViewModel
    private lateinit var toast: Toast
    private var canvasWidth = 540
    private var canvasHeight = 984
    private var safeToExit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canvas)
        canvasViewModel = ViewModelProvider(this).get(CanvasViewModel::class.java)
        toast = Toast(this)
        createCanvasViews()

        //Receive intent from MainActivity:
        when {
            intent==null -> showToast("Error!")
            //Imported image from CreateCanvasFragment
            intent.getStringExtra(IMPORT_IMAGE_INTENT_KEY)!=null -> {
                val uri = intent.getStringExtra(IMPORT_IMAGE_INTENT_KEY)
                //Initialize canvasWidth and canvasHeight:
                if(uri!=null) getImageDimensionsFromUri(Uri.parse(uri))
                canvasViews.createCanvasImageViewFromUri(canvasWidth, canvasHeight, Uri.parse(uri))
            }
            //Imported image from GalleryFragment or MainActivity(intent.ACTION_VIEW)
            intent.getStringExtra(IMPORT_CV_IMAGE_INTENT_KEY)!=null -> {
                val cvImage = CanvasViewModel.importedCvImage
                if(cvImage != null){
                    canvasWidth = cvImage.width
                    canvasHeight = cvImage.height
                    canvasViews.createCanvasImageViewFromCvImage(canvasWidth, canvasHeight)
                }
                else canvasViews.createCanvasImageviewFromDimensions(canvasWidth, canvasHeight)
            }
            else -> { //Image dimensions from CreateCanvas fragment
                canvasWidth = intent.getIntExtra(DIMENSION_WIDTH_INTENT_KEY, canvasWidth)
                canvasHeight = intent.getIntExtra(DIMENSION_HEIGHT_INTENT_KEY, canvasHeight)
                if(canvasWidth<CreateCanvasFragment.MIN_WIDTH) canvasWidth=CreateCanvasFragment.MIN_WIDTH
                if(canvasHeight<CreateCanvasFragment.MIN_HEIGHT) canvasHeight=CreateCanvasFragment.MIN_HEIGHT
                canvasViews.createCanvasImageviewFromDimensions(canvasWidth, canvasHeight)
            }
        }
    }

    private fun getImageDimensionsFromUri(uri: Uri) {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(contentResolver.openInputStream(uri), null, options)
        canvasWidth = options.outWidth
        canvasHeight = options.outHeight
    }

    private fun createCanvasViews(){
        canvasViews = CanvasViews(
            context = this,
            owner = this,
            canvasViewModel = canvasViewModel,
            toast = toast,
            layoutCanvas = findViewById(R.id.canvas_layout),
            toolbarOuterCardView = findViewById(R.id.toolbar_outer_card),
            //toolbarInnerCardView = findViewById(R.id.toolbar_inner_card),
            toolbarColorIndicator = findViewById(R.id.toolbar_color_indicator),
            toolbarPngBackgroundImageView = findViewById(R.id.toolbar_png_background_imageview),
            toolbarMoveImageView = findViewById(R.id.toolbar_move),
            toolbarButtonLayout = findViewById(R.id.toolbar_buttons),
            toolbarUndoBtn = findViewById(R.id.toolbar_button_undo),
            toolbarRedoBtn = findViewById(R.id.toolbar_button_redo),
            toolbarLayerBtn = findViewById(R.id.toolbar_button_layers),
            toolbarToolBtn = findViewById(R.id.toolbar_button_tool),
            toolbarOptionsBtn = findViewById(R.id.toolbar_button_options),
            bottomToolbarOuterCardView = findViewById(R.id.bottom_toolbar_outer_card),
            //bottomToolbarInnerCardView = findViewById(R.id.bottom_toolbar_inner_card),
            bottomToolbarColorIndicator = findViewById(R.id.bottom_toolbar_color_indicator),
            bottomToolbarCloseBtn = findViewById(R.id.bottom_toolbar_close_btn),
            toolBrushLayout = findViewById(R.id.tool_brush_properties),
            toolEraserLayout = findViewById(R.id.tool_eraser_properties),
            toolBucketLayout = findViewById(R.id.tool_bucket_properties),
            toolSelectLayout = findViewById(R.id.tool_select_properties),
            toolShapeLayout = findViewById(R.id.tool_shape_properties),
            toolTextLayout = findViewById(R.id.tool_text_properties),
            layersLayout = findViewById(R.id.canvas_layers_properties),
            transformLayout = findViewById(R.id.canvas_transform_properties),
            goBack = ::goBack
        )
    }

    private fun goBack(){
        safeToExit = true
        onBackPressed()
    }

    @Suppress("SameParameterValue")
    private fun showToast(text: String){
        if(text.length>30) toast.duration = Toast.LENGTH_LONG
        else toast.duration = Toast.LENGTH_SHORT
        toast.cancel()
        toast = Toast.makeText(this, text, toast.duration)
        toast.show()
    }

    override fun onBackPressed() {
        if(safeToExit) {
            //canvasViewModel.resetAttributes() //todo?
            super.onBackPressed()
        }
        else canvasViews.showSaveCanvasDialog(true)
    }
}
