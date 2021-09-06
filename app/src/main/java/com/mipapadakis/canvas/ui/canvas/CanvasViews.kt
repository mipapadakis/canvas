package com.mipapadakis.canvas.ui.canvas

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.text.InputFilter
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.CanvasPreferences
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.ui.util.CanvasTouchListener
import com.mipapadakis.canvas.ui.util.CvFileHelper
import com.mipapadakis.canvas.ui.util.ShowTipDialog
import com.mipapadakis.canvas.ui.canvas.toolbar.*
import com.mipapadakis.canvas.ui.canvas.toolbar.bottom.*

@SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
/**TODO*/
class CanvasViews(
    val context: Context,
    val owner: LifecycleOwner,
    val canvasViewModel: CanvasViewModel,
    var toast: Toast,
    val layoutCanvas: RelativeLayout,
    val toolbarOuterCardView: CardView,
    /// toolbarInnerCardView: CardView,
    val toolbarPngBackgroundImageView: ImageView,
    val toolbarColorIndicator: ImageView,
    val toolbarMoveImageView: ImageView,
    val toolbarButtonLayout: LinearLayout,
    val toolbarUndoBtn: ImageButton,
    val toolbarRedoBtn: ImageButton,
    val toolbarLayerBtn: ImageButton,
    val toolbarToolBtn: ImageButton,
    val toolbarOptionsBtn: ImageButton,
    val bottomToolbarOuterCardView: CardView,
    /// val bottomToolbarInnerCardView: CardView,
    val bottomToolbarColorIndicator: ImageView,
    val bottomToolbarCloseBtn: ImageButton,
    val toolBrushLayout: View,
    val toolEraserLayout: View,
    val toolBucketLayout: View,
    val toolSelectLayout: View,
    val toolShapeLayout: View,
    val toolTextLayout: View,
    val layersLayout: View,
    val transformLayout: View,
    val goBack: () -> Unit) {
    val layerRecyclerView: RecyclerView = layersLayout.findViewById(R.id.property_layers_recycler_view)
    lateinit var canvasIV: CanvasImageView
    private var canvasWidth = 0
    private var canvasHeight = 0
    private val properties =
        arrayOf(
        toolBrushLayout,
        toolEraserLayout,
        toolBucketLayout,
        toolSelectLayout,
        toolShapeLayout,
        toolTextLayout,
        layersLayout,
        transformLayout)

    fun createCanvasImageViewFromUri(width: Int, height: Int, uri: Uri?){
        canvasWidth = width
        canvasHeight = height
        val layoutParamsCanvas = RelativeLayout.LayoutParams(width, height)
        layoutParamsCanvas.addRule(RelativeLayout.BELOW)
        canvasIV = CanvasImageView(context, canvasViewModel, true){
            layerRecyclerView.adapter?.notifyDataSetChanged()
        }
        canvasIV.layoutParams = layoutParamsCanvas
        canvasIV.setImageURI(uri)
        layoutCanvas.addView(canvasIV)
        onCreateCanvasImageView()
    }

    fun createCanvasImageViewFromCvImage(width: Int, height: Int){
        canvasWidth = width
        canvasHeight = height
        val layoutParamsCanvas = RelativeLayout.LayoutParams(width, height)
        layoutParamsCanvas.addRule(RelativeLayout.BELOW)
        canvasIV = CanvasImageView(context, canvasViewModel,false){
            layerRecyclerView.adapter?.notifyDataSetChanged()
        }
        canvasIV.layoutParams = layoutParamsCanvas
        layoutCanvas.addView(canvasIV)
        onCreateCanvasImageView()
    }

    fun createCanvasImageviewFromDimensions(width: Int, height: Int){
        canvasWidth = width
        canvasHeight = height
        val layoutParamsCanvas = RelativeLayout.LayoutParams(width, height)
        layoutParamsCanvas.addRule(RelativeLayout.BELOW)
        canvasIV = CanvasImageView(context, canvasViewModel,true){
            layerRecyclerView.adapter?.notifyDataSetChanged()
        }
        canvasIV.layoutParams = layoutParamsCanvas
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(CanvasPreferences.startingCanvasColor)
        canvasIV.setImageBitmap(bitmap)
        layoutCanvas.addView(canvasIV)
        onCreateCanvasImageView()
    }

    private fun onCreateCanvasImageView(){
        //Pass dimensions, because canvas' width and height attributes are 0 at this point
        canvasIV.onAttachedToWindowInitializer(canvasWidth, canvasHeight)

        //Handle background touches:
        layoutCanvas.setOnTouchListener(CanvasTouchListener(object : CanvasTouchListener.MultiTouchListener {
            override fun on1PointerTap(event: MotionEvent) { toggleToolbarVisibility() }
            override fun on2PointerDoubleTap(event: MotionEvent) {
                Log.i("CanvasTouchListener", "Background on2PointerDoubleTap")
                canvasIV.on2PointerDoubleTap(event)
            }
            override fun on1PointerLongPress(event: MotionEvent) {
                ShowTipDialog.showCanvasBackgroundTipDialog(context)
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

        canvasViewModel.setPaintColor(ContextCompat.getColor(context, R.color.green))
    }
    private fun setToolbar() {
        ToolbarMove(this)
        ToolbarUndo(this)
        ToolbarRedo(this)
        ToolbarTools( this)
        ToolbarLayers(this)
        ToolbarOptions(this)

        // CURRENT COLOR INDICATOR
        canvasViewModel.toolbarColor.observe(owner, {
            toolbarColorIndicator.setBackgroundColor(canvasViewModel.getColor())
            bottomToolbarColorIndicator.setBackgroundColor(canvasViewModel.getColor())
        })
    }

    private fun setBottomToolbar() {
        ToolbarTools.showCurrentToolProperties(this)
        ToolbarTools.updateCurrentToolIconOfToolbar(this)
        BrushProperties(this)
        EraserProperties(this)
        BucketProperties(this)
        // SelectProperties() //TODO
        ShapeProperties(this)
        TextProperties(this)
        LayerProperties(this)
        TransformProperties(this)

        //SETTINGS?

        //CLOSE BOTTOM TOOLBAR
        bottomToolbarCloseBtn.setOnClickListener { hideBottomToolbar() }
    }

    fun showSaveCanvasDialog(askBeforeExit: Boolean){
        val layoutInflaterAndroid = LayoutInflater.from(context)
        val view: View = layoutInflaterAndroid.inflate(R.layout.dialog_save, null)
        val alertDialogBuilderUserInput: AlertDialog.Builder = AlertDialog.Builder(context)
        alertDialogBuilderUserInput.setView(view)
        val inputTitle = view.findViewById<EditText>(R.id.input_dialog_input_title)
        val dialogTitle = view.findViewById<TextView>(R.id.input_dialog_title)
        val dialogSubtitle = view.findViewById<TextView>(R.id.input_dialog_subtitle)
        val fileNameExistsTV = view.findViewById<TextView>(R.id.input_dialog_file_name_exists)
        val typeCanvasRadioButton = view.findViewById<RadioButton>(R.id.input_dialog_filetype_canvas)
        val typePngRadioButton = view.findViewById<RadioButton>(R.id.input_dialog_filetype_png)
        val typeJpegRadioButton = view.findViewById<RadioButton>(R.id.input_dialog_filetype_jpeg)
        val titleText = getString(R.string.save_canvas_title) + if(askBeforeExit) "?" else ""
        dialogTitle.text = titleText
        dialogSubtitle.text = getString(R.string.save_canvas_subtitle)
        inputTitle.setText(canvasViewModel.cvImage.title)

        fun hideKeyboard(view: View) {
            val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
        fun updateDialogFileNameExistsTextView(fileName: String, saveButton: Button){
            if(CvFileHelper(context).fileNameAlreadyExists(
                    "${fileName}." + when {
                        typeCanvasRadioButton.isChecked -> getString(R.string.file_extension_canvas)
                        typePngRadioButton.isChecked -> getString(R.string.file_extension_png)
                        else -> getString(R.string.file_extension_jpeg)
                    }
                )) {
                fileNameExistsTV.visibility = View.VISIBLE
                saveButton.text = getString(R.string.overwrite)
            }
            else {
                fileNameExistsTV.visibility = View.GONE
                saveButton.text = getString(R.string.save)
            }
        }
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

        alertDialogBuilderUserInput
            .setIcon(R.drawable.save_outlined)
            .setCancelable(true)
            .setPositiveButton(  getString(R.string.save) ) { _, _ -> }
            .setNegativeButton( if(askBeforeExit) getString(R.string.exit_without_save) else getString(R.string.cancel) ) { dialogBox, _ ->
                hideKeyboard(inputTitle)
                dialogBox.cancel()
                if(askBeforeExit) goBack()
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

        //////////////////////Handle when FileNameExistsTextView is visible:////////////////////////
        inputTitle.addTextChangedListener {
            updateDialogFileNameExistsTextView(it?.toString()?:"",
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE))
        }
        typeCanvasRadioButton.setOnCheckedChangeListener { _, _ ->
            updateDialogFileNameExistsTextView(inputTitle?.text.toString(),
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE))
        }
        typePngRadioButton.setOnCheckedChangeListener { _, _ ->
            updateDialogFileNameExistsTextView(inputTitle?.text.toString(),
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE))
        }
        typeJpegRadioButton.setOnCheckedChangeListener { _, _ ->
            updateDialogFileNameExistsTextView(inputTitle?.text.toString(),
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE))
        }
        updateDialogFileNameExistsTextView(inputTitle?.text.toString(),
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE))
        ////////////////////////////////////////////////////////////////////////////////////////////

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
            if (TextUtils.isEmpty(inputTitle.text.toString())) {
                showToast("You need to enter a title!")
                return@OnClickListener
            }

            canvasViewModel.cvImage.title = inputTitle.text.toString()
            when {
                typeCanvasRadioButton.isChecked -> canvasViewModel.cvImage.fileType = CanvasViewModel.FILETYPE_CANVAS
                typePngRadioButton.isChecked -> canvasViewModel.cvImage.fileType = CanvasViewModel.FILETYPE_PNG
                typeJpegRadioButton.isChecked -> canvasViewModel.cvImage.fileType = CanvasViewModel.FILETYPE_JPEG
            }
            //Update undo history with the new title and filetype
            for(action in canvasViewModel.history){
                action.actionCvImage.title = canvasViewModel.cvImage.title
                action.actionCvImage.fileType = canvasViewModel.cvImage.fileType
            }

            //Save Canvas:
            hideKeyboard(inputTitle)
            alertDialog.dismiss()
            saveCvImage()
            if(askBeforeExit) goBack()
        })
        alertDialog.setOnDismissListener {
            hideKeyboard(inputTitle)
        }
        alertDialog.setOnCancelListener {
            hideKeyboard(inputTitle)
        }
    }

    private fun saveCvImage(){
        if(CvFileHelper(context).saveCvImage(canvasViewModel.cvImage)) {
            showToast("Saved As \"${canvasViewModel.cvImage.getFilenameWithExtension(context)}\"")
            //GalleryViewModel.setImages(CvFileHelper(context).getAllCvImages())
        }
    }

    fun toggleToolbarVisibility(){
        if(toolbarButtonLayout.visibility == View.VISIBLE) hideToolbars()
        else showToolBars()
    }
    private fun showToolBars() {
        toolbarMoveImageView.setImageResource(R.drawable.move_outlined)
        toolbarButtonLayout.visibility = View.VISIBLE
        toolbarPngBackgroundImageView.visibility = View.VISIBLE
        toolbarColorIndicator.visibility = View.VISIBLE
        if(hasVisibleProperties()) showBottomToolbar() else hideBottomToolbar()
    }
    private fun hideToolbars() {
        toolbarMoveImageView.setImageResource(R.drawable.visibility_outlined)
        toolbarButtonLayout.visibility = View.GONE
        toolbarPngBackgroundImageView.visibility = View.GONE
        toolbarColorIndicator.visibility = View.GONE
        hideBottomToolbar()
    }
    fun hideProperties(){
        for(p in properties){ if(p.visibility == View.VISIBLE) p.visibility = View.GONE}
    }
    private fun hasVisibleProperties(): Boolean{
        for(p in properties){ if(p.visibility == View.VISIBLE) return true }
        return false
    }
    fun hideBottomToolbar(){
        bottomToolbarOuterCardView.visibility = View.GONE
    }
    fun showBottomToolbar(){
        bottomToolbarOuterCardView.visibility = View.VISIBLE
        doWhenTheViewIsVisible(bottomToolbarOuterCardView){
            avoidTopAndBottomToolbarOverlap()
        }
    }
    fun bottomToolbarIsVisible() = bottomToolbarOuterCardView.visibility == View.VISIBLE
    private fun avoidTopAndBottomToolbarOverlap() {
        if (kotlin.math.abs(bottomToolbarOuterCardView.y - toolbarOuterCardView.y) < bottomToolbarOuterCardView.height)
            toolbarOuterCardView.animate()
                .y(bottomToolbarOuterCardView.y - toolbarOuterCardView.height - toolbarOuterCardView.paddingBottom)
                .setDuration(0)
                .start()
    }
    private fun doWhenTheViewIsVisible(view: View, function: () -> Unit) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                function()
            }
        })
    }

    private fun getString(id: Int) = context.getString(id)

    //private fun getColorFromId(id: Int) = ContextCompat.getColor(context, id)
    //private fun getDrawableFromId(id: Int) = ContextCompat.getDrawable(context, id)

    @Suppress("SameParameterValue")
    fun showToast(text: String){
        if(text.length>30) toast.duration = Toast.LENGTH_LONG
        else toast.duration = Toast.LENGTH_SHORT
        toast.cancel()
        toast = Toast.makeText(context, text, toast.duration)
        toast.show()
    }
}