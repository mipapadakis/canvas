package com.mipapadakis.canvas.ui.create_canvas

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.text.isDigitsOnly
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.card.MaterialCardView
import com.google.android.material.slider.Slider
import com.mipapadakis.canvas.CanvasActivity
import com.mipapadakis.canvas.InterfaceMainActivity
import com.mipapadakis.canvas.R
import java.lang.Exception

class CreateCanvasFragment : Fragment() {
    private lateinit var permissionRequestLauncher: ActivityResultLauncher<String>
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var interfaceOfMainActivity: InterfaceMainActivity
    private lateinit var createCanvasViewModel: CreateCanvasViewModel
    private lateinit var scrollToBottomLayout: FrameLayout
    private lateinit var pixelLayout: LinearLayout
    private lateinit var dpiLayout: LinearLayout
    private lateinit var pixelBtn: Button
    private lateinit var mmBtn: Button
    private lateinit var inchBtn: Button
    private var importedImagePreview: ImageView? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        createCanvasViewModel = ViewModelProvider(this).get(CreateCanvasViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_create_canvas, container, false)
        importedImagePreview = root.findViewById(R.id.fragment_create_canvas_card_import_icon)

        //Receive image from gallery
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                //https://stackoverflow.com/a/63654043/11535380
                val uri: Uri? = result.data?.data //TODO what to do when the file is .cv type
                importedImagePreview?.setImageURI(uri)
                createCanvasViewModel.setImportImagePreview(uri.toString())
            }
            else createCanvasViewModel.setImportImagePreview("")
        }
        //handle requested permission result
        permissionRequestLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if (it) { //permission from popup granted
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                resultLauncher.launch(intent) //instead of startActivityForResult(intent, CODE_IMAGE_PICK)
            } else {//permission from popup denied
                showToast("Permission denied")
            }
        }

        initializeList(root)
        return root
    }

    private fun initializeList(root: View) { //}: ArrayList<Int>{
        val importDetails = root.findViewById<TextView>(R.id.fragment_create_canvas_card_import_details)
        importDetails.text = resources.getString(R.string.create_canvas_import_details)

        drawRectangle(root.findViewById(R.id.fragment_create_canvas_card_sd_icon), CanvasDefaultSize.SD_SIZE.width, CanvasDefaultSize.SD_SIZE.height)
        drawRectangle(root.findViewById(R.id.fragment_create_canvas_card_hd_icon), CanvasDefaultSize.HD_SIZE.width, CanvasDefaultSize.HD_SIZE.height)
        drawRectangle(root.findViewById(R.id.fragment_create_canvas_card_1_1_icon), CanvasDefaultSize.DEFAULT_1_1.width, CanvasDefaultSize.DEFAULT_1_1.height)
        drawRectangle(root.findViewById(R.id.fragment_create_canvas_card_3_4_icon), CanvasDefaultSize.DEFAULT_3_4.width, CanvasDefaultSize.DEFAULT_3_4.height)
        drawRectangle(root.findViewById(R.id.fragment_create_canvas_card_9_16_icon), CanvasDefaultSize.DEFAULT_9_16.width, CanvasDefaultSize.DEFAULT_9_16.height)
        drawRectangle(root.findViewById(R.id.fragment_create_canvas_card_A4_icon), CanvasDefaultSize.A4.width, CanvasDefaultSize.A4.height)

        root.findViewById<MaterialCardView>(R.id.fragment_create_canvas_card_import).setOnClickListener {
            if(importedImagePreview==null) importedImagePreview = root.findViewById(R.id.fragment_create_canvas_card_import_icon)
            if(createCanvasViewModel.importImagePreview.value?.isEmpty() == true) pickImageFromGallery()
            else if(createCanvasViewModel.importImagePreview.value!=null) {
                val intent = Intent(context, CanvasActivity::class.java)
                intent.putExtra(IMPORT_IMAGE_INTENT_KEY, createCanvasViewModel.importImagePreview.value)
                startActivity(intent)
            }
        }
        createCanvasViewModel.importImagePreview.observe(viewLifecycleOwner, {
            if (it.isEmpty()) {
                importedImagePreview?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.collections_outlined))
                importDetails.text = resources.getString(R.string.create_canvas_import_details)
            } else {
                importedImagePreview?.setImageURI(Uri.parse(it))
                importDetails.text = resources.getString(R.string.create_canvas_import_details_2)
            }
        })
        importedImagePreview?.setOnClickListener {
            if(createCanvasViewModel.importImagePreview.value?.isNotEmpty() == true){
                createCanvasViewModel.setImportImagePreview("")
                pickImageFromGallery()
            }
        }

        root.findViewById<MaterialCardView>(R.id.fragment_create_canvas_card_sd).setOnClickListener {
            startCanvasIntent(CanvasDefaultSize.SD_SIZE.width, CanvasDefaultSize.SD_SIZE.height)}
        root.findViewById<MaterialCardView>(R.id.fragment_create_canvas_card_hd).setOnClickListener {
            startCanvasIntent(CanvasDefaultSize.HD_SIZE.width, CanvasDefaultSize.HD_SIZE.height)}
        root.findViewById<MaterialCardView>(R.id.fragment_create_canvas_card_1_1).setOnClickListener {
            startCanvasIntent(CanvasDefaultSize.DEFAULT_1_1.width, CanvasDefaultSize.DEFAULT_1_1.height) }
        root.findViewById<MaterialCardView>(R.id.fragment_create_canvas_card_3_4).setOnClickListener {
            startCanvasIntent(CanvasDefaultSize.DEFAULT_3_4.width, CanvasDefaultSize.DEFAULT_3_4.height) }
        root.findViewById<MaterialCardView>(R.id.fragment_create_canvas_card_9_16).setOnClickListener {
            startCanvasIntent(CanvasDefaultSize.DEFAULT_9_16.width, CanvasDefaultSize.DEFAULT_9_16.height) }
        root.findViewById<MaterialCardView>(R.id.fragment_create_canvas_card_A4).setOnClickListener {
            startCanvasIntent(CanvasDefaultSize.A4.width, CanvasDefaultSize.A4.height) }
        root.findViewById<MaterialCardView>(R.id.fragment_create_canvas_card_custom).setOnClickListener {
            when {
                createCanvasViewModel.customUnit.value == createCanvasViewModel.UNIT_PIXEL ->
                    startCanvasIntent(createCanvasViewModel.unitPixels[WIDTH], createCanvasViewModel.unitPixels[HEIGHT])
                dpiInputIsValidInPixels() -> {
                    val inPixels = inchToPixels(createCanvasViewModel.unitInches[WIDTH], createCanvasViewModel.unitInches[HEIGHT], createCanvasViewModel.dpi)!!
                    startCanvasIntent(inPixels[WIDTH], inPixels[HEIGHT])
                }
                else -> showToast("Wrong Input!\n Pixel dimensions must be between 1 and $MAX_WIDTH.")
            }
        }

        ///////////////////////////////////////Custom Size//////////////////////////////////////////
        scrollToBottomLayout = root.findViewById(R.id.fragment_create_canvas_scroll_to_bottom_layout)
        pixelLayout = root.findViewById(R.id.fragment_create_canvas_card_custom_pixel_layout)
        dpiLayout = root.findViewById(R.id.fragment_create_canvas_card_custom_dpi_layout)
        pixelBtn = root.findViewById(R.id.fragment_create_canvas_card_custom_pixel_button)
        mmBtn = root.findViewById(R.id.fragment_create_canvas_card_custom_mm_button)
        inchBtn = root.findViewById(R.id.fragment_create_canvas_card_custom_inch_button)
        updateCustomSizeImageView(root.findViewById(R.id.fragment_create_canvas_card_custom_icon))

        //Following views must not trigger the card listener when clicked => override clickListener:
        pixelLayout.setOnClickListener {}
        dpiLayout.setOnClickListener {}
        root.findViewById<LinearLayout>(R.id.fragment_create_canvas_card_custom_button_toggle_group).setOnClickListener {}

        createCanvasViewModel.customUnit.observe(viewLifecycleOwner, {
            when (it) {
                createCanvasViewModel.UNIT_PIXEL -> setPixelUnit()
                createCanvasViewModel.UNIT_MM -> setMmUnit(root)
                else -> setInchUnit(root)
            }
        })

        pixelBtn.setOnClickListener {
            createCanvasViewModel.setCustomUnit(createCanvasViewModel.UNIT_PIXEL)
            updateCustomSizeImageView(root.findViewById(R.id.fragment_create_canvas_card_custom_icon))}
        mmBtn.setOnClickListener { createCanvasViewModel.setCustomUnit(createCanvasViewModel.UNIT_MM)}
        inchBtn.setOnClickListener { createCanvasViewModel.setCustomUnit(createCanvasViewModel.UNIT_INCH)}
        setPixelLayoutListeners(root)
        setDpiLayoutListeners(root)
        setKeyboardVisibilityListener(root)
    }

    private fun startCanvasIntent(width: Int, height: Int){
        val intent = Intent(context, CanvasActivity::class.java)
        intent.putExtra(DIMENSION_WIDTH_INTENT_KEY, width)
        intent.putExtra(DIMENSION_HEIGHT_INTENT_KEY, height)
        startActivity(intent)
    }

    private fun pickImageFromGallery(){
        //check runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if ( checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                //val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                //show popup to request runtime permission
                permissionRequestLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE) //requestPermissions(permissions, CODE_PERMISSION)
                return
            }
            //else: permission already granted
        } //else: system OS is < Marshmallow
        val intent = Intent(Intent.ACTION_PICK) //Intent to pick image
        intent.type = "image/*"
        resultLauncher.launch(intent) //startActivityForResult(intent, CODE_IMAGE_PICK)
    }

    private fun setPixelUnit(){
        val deactivatedButtonColor = ContextCompat.getColor(requireContext(), R.color.primary1)
        pixelBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary2))
        mmBtn.setBackgroundColor(deactivatedButtonColor)
        inchBtn.setBackgroundColor(deactivatedButtonColor)
        pixelLayout.visibility = View.VISIBLE
        dpiLayout.visibility = View.GONE
    }
    private fun setMmUnit(root: View){
        val deactivatedButtonColor = ContextCompat.getColor(requireContext(), R.color.primary1)
        //If dpiWidth and dpiHeight values were previously inches, convert them to mm
        beginChange()
        root.findViewById<EditText>(R.id.fragment_create_canvas_card_custom_dpi_input_width).setText(
                createCanvasViewModel.unitMillimeters[WIDTH].toString())
        root.findViewById<EditText>(R.id.fragment_create_canvas_card_custom_dpi_input_height).setText(
                createCanvasViewModel.unitMillimeters[HEIGHT].toString())
        commitChange()
        root.findViewById<TextView>(R.id.fragment_create_canvas_card_custom_dpi_width_unit).text = resources.getString(R.string.unit_millimeters)
        root.findViewById<TextView>(R.id.fragment_create_canvas_card_custom_dpi_height_unit).text = resources.getString(R.string.unit_millimeters)
        pixelBtn.setBackgroundColor(deactivatedButtonColor)
        mmBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary2))
        inchBtn.setBackgroundColor(deactivatedButtonColor)
        pixelLayout.visibility = View.GONE
        dpiLayout.visibility = View.VISIBLE
        createCanvasViewModel.lastDpiUnitUsed = createCanvasViewModel.UNIT_MM
        updateCustomSizeImageView(root.findViewById(R.id.fragment_create_canvas_card_custom_icon))
        scrollToBottom()
    }
    private fun setInchUnit(root: View){
        val deactivatedButtonColor = ContextCompat.getColor(requireContext(), R.color.primary1)
        //If dpiWidth and dpiHeight values were previously millimeters, convert them to inches
        beginChange()
        root.findViewById<EditText>(R.id.fragment_create_canvas_card_custom_dpi_input_width).setText(
                createCanvasViewModel.unitInches[WIDTH].toString())
        root.findViewById<EditText>(R.id.fragment_create_canvas_card_custom_dpi_input_height).setText(
                createCanvasViewModel.unitInches[HEIGHT].toString())
        commitChange()
        root.findViewById<TextView>(R.id.fragment_create_canvas_card_custom_dpi_width_unit).text = resources.getString(R.string.unit_inches)
        root.findViewById<TextView>(R.id.fragment_create_canvas_card_custom_dpi_height_unit).text = resources.getString(R.string.unit_inches)
        pixelBtn.setBackgroundColor(deactivatedButtonColor)
        mmBtn.setBackgroundColor(deactivatedButtonColor)
        inchBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary2))
        pixelLayout.visibility = View.GONE
        dpiLayout.visibility = View.VISIBLE
        createCanvasViewModel.lastDpiUnitUsed = createCanvasViewModel.UNIT_INCH
        updateCustomSizeImageView(root.findViewById(R.id.fragment_create_canvas_card_custom_icon))
        scrollToBottom()
    }

    @SuppressLint("SetTextI18n")
    private fun setPixelLayoutListeners(root: View){
        val widthEditText = root.findViewById<EditText>(R.id.fragment_create_canvas_card_custom_pixel_input_width)
        val heightEditText = root.findViewById<EditText>(R.id.fragment_create_canvas_card_custom_pixel_input_height)
        val widthSlider = root.findViewById<Slider>(R.id.fragment_create_canvas_card_custom_pixel_slider_width)
        val heightSlider = root.findViewById<Slider>(R.id.fragment_create_canvas_card_custom_pixel_slider_height)

        widthSlider.addOnChangeListener { _, value, _ ->
            if(!isUnderChange()) {
                widthEditText.setText(value.toInt().toString())
                updateCustomSizeImageView(root.findViewById(R.id.fragment_create_canvas_card_custom_icon))
                moveCursorToEndOfEditText(widthEditText)
            }
        }
        heightSlider.addOnChangeListener { _, value, _ ->
            if(!isUnderChange()) {
                heightEditText.setText(value.toInt().toString())
                updateCustomSizeImageView(root.findViewById(R.id.fragment_create_canvas_card_custom_icon))
                moveCursorToEndOfEditText(heightEditText)
            }
        }

        widthEditText.addTextChangedListener {
            val input = if(it == null || it.isEmpty() || !it.toString().isDigitsOnly() || it.toString().toInt()<=0){
                if(!it.toString().isDigitsOnly()) createCanvasViewModel.unitPixels[WIDTH]
                else 1
            } else if(it.toString().toInt() > MAX_WIDTH){
                MAX_WIDTH
            } else it.toString().toInt()
            createCanvasViewModel.unitPixels[WIDTH] = input
            beginChange()
            widthSlider.value = input.toFloat()
            commitChange()
            updateCustomSizeImageView(root.findViewById(R.id.fragment_create_canvas_card_custom_icon))
        }
        heightEditText.addTextChangedListener {
            val input = if(it == null || it.isEmpty() || !it.toString().isDigitsOnly() || it.toString().toInt()<=0){
                if(!it.toString().isDigitsOnly()) createCanvasViewModel.unitPixels[HEIGHT]
                else 1
            } else if(it.toString().toInt() > MAX_HEIGHT){
                MAX_HEIGHT
            } else it.toString().toInt()
            createCanvasViewModel.unitPixels[HEIGHT] = input
            beginChange()
            heightSlider.value = input.toFloat()
            commitChange()
            updateCustomSizeImageView(root.findViewById(R.id.fragment_create_canvas_card_custom_icon))
        }
    }

    private fun setDpiLayoutListeners(root: View){
        val widthEditText = root.findViewById<EditText>(R.id.fragment_create_canvas_card_custom_dpi_input_width)
        val heightEditText = root.findViewById<EditText>(R.id.fragment_create_canvas_card_custom_dpi_input_height)
        val dpiEditText = root.findViewById<EditText>(R.id.fragment_create_canvas_card_custom_dpi_input)
        val inPixelTextView = root.findViewById<TextView>(R.id.fragment_create_canvas_card_custom_dpi_in_pixels)

        widthEditText.addTextChangedListener{
            if(createCanvasViewModel.customUnit.value==createCanvasViewModel.UNIT_INCH){
                val input = if(it == null || it.isEmpty() || !isDouble(it.toString())){
                    if(!isDouble(it.toString())) createCanvasViewModel.unitInches[WIDTH]
                    else 0.0
                } else it.toString().toDouble()
                createCanvasViewModel.unitInches[WIDTH] = input
                if(!isUnderChange()) createCanvasViewModel.unitMillimeters[WIDTH] = inchToMm(input)
            }
            else{
                val input = if(it == null || it.isEmpty() || !isDouble(it.toString())){
                    if(!isDouble(it.toString())) createCanvasViewModel.unitMillimeters[WIDTH]
                    else 0.0
                } else it.toString().toDouble()
                createCanvasViewModel.unitMillimeters[WIDTH] = input
                if(!isUnderChange()) createCanvasViewModel.unitInches[WIDTH] = mmToInch(input)
            }
            updateInPixelsTextView(inPixelTextView)
            updateCustomSizeImageView(root.findViewById(R.id.fragment_create_canvas_card_custom_icon))
        }
        heightEditText.addTextChangedListener {
            if(createCanvasViewModel.customUnit.value==createCanvasViewModel.UNIT_INCH){
                val input = if(it == null || it.isEmpty() || !isDouble(it.toString())){
                    if(!isDouble(it.toString())) createCanvasViewModel.unitInches[HEIGHT]
                    else 0.0
                } else it.toString().toDouble()
                createCanvasViewModel.unitInches[HEIGHT] = input
                if(!isUnderChange()) createCanvasViewModel.unitMillimeters[HEIGHT] = inchToMm(input)
            }
            else{
                val input = if(it == null || it.isEmpty() || !isDouble(it.toString())){
                    if(!isDouble(it.toString())) createCanvasViewModel.unitMillimeters[HEIGHT]
                    else 0.0
                } else it.toString().toDouble()
                createCanvasViewModel.unitMillimeters[HEIGHT] = input
                if(!isUnderChange()) createCanvasViewModel.unitInches[HEIGHT] = mmToInch(input)
//                if(abs(canvasViewModel.unitInches[WIDTH] - mmToInch(input)) >=0.009)
//                    canvasViewModel.unitMillimeters[HEIGHT] = inchToMm(input)
            }
            updateInPixelsTextView(inPixelTextView)
            updateCustomSizeImageView(root.findViewById(R.id.fragment_create_canvas_card_custom_icon))
        }
        dpiEditText.addTextChangedListener {
            val input = if(it == null || it.isEmpty() || !it.toString().isDigitsOnly()){
                if(!it.toString().isDigitsOnly()) createCanvasViewModel.dpi
                else 0
            } else it.toString().toInt()
            createCanvasViewModel.dpi = input
            updateInPixelsTextView(inPixelTextView)
            updateCustomSizeImageView(root.findViewById(R.id.fragment_create_canvas_card_custom_icon))
        }
    }

    //Returns null if pixels outside (0,2048]
    private fun inchToPixels(width: Double, height: Double, dpi: Int): ArrayList<Int>?{
        val pixels: ArrayList<Int> = arrayListOf((width * dpi).toInt(), (height * dpi).toInt())
        if(pixels[WIDTH]<=0 || pixels[WIDTH]> MAX_WIDTH || pixels[HEIGHT]<=0 || pixels[HEIGHT]> MAX_HEIGHT)
            return null
        return pixels
    }
    //Returns null if pixels outside (0,2048]
    private fun mmToPixels(width: Double, height: Double, dpi: Int)
    = inchToPixels(mmToInch(width), mmToInch(height), dpi)
    private fun mmToInch(mm: Double): Double = mm/25.4
    private fun inchToMm(inch: Double): Double = inch*25.4
    private fun isDouble(str: String) = (str.isDigitsOnly() || str.contains("."))
    private fun dpiInputIsValidInPixels(): Boolean{
        val inPixels = if(createCanvasViewModel.customUnit.value == createCanvasViewModel.UNIT_MM){
            mmToPixels(createCanvasViewModel.unitMillimeters[WIDTH],
                    createCanvasViewModel.unitMillimeters[HEIGHT], createCanvasViewModel.dpi)
        } else{
            inchToPixels(createCanvasViewModel.unitInches[WIDTH],
                    createCanvasViewModel.unitInches[HEIGHT], createCanvasViewModel.dpi)
        }
        return inPixels != null
    }
    private fun updateInPixelsTextView(inPixelTextView: TextView){
        val mmToPixels = mmToPixels(createCanvasViewModel.unitMillimeters[WIDTH],
                createCanvasViewModel.unitMillimeters[HEIGHT], createCanvasViewModel.dpi)
        val inchToPixels = inchToPixels(createCanvasViewModel.unitInches[WIDTH],
                createCanvasViewModel.unitInches[HEIGHT], createCanvasViewModel.dpi)

        // Note: Due to rounding errors, sometimes mmToPixels will be different from inchToPixels.
        // In that case, prioritize inchToPixels over mmToPixels (because dpi uses inches).
        val inPixels = if(createCanvasViewModel.customUnit.value == createCanvasViewModel.UNIT_INCH
                || (mmToPixels!=inchToPixels)) inchToPixels else mmToPixels

        //Update inPixelTextView:
        if(inPixels!=null) {
            inPixelTextView.text = String.format(
                    resources.getString(R.string.create_canvas_custom_dpi_in_pixels),
                    inPixels[WIDTH].toString(),
                    inPixels[HEIGHT].toString())
        } else inPixelTextView.text = getString(R.string.error)
    }

    private fun updateCustomSizeImageView(customImageView: ImageView){
        when (createCanvasViewModel.customUnit.value) {
            createCanvasViewModel.UNIT_PIXEL ->
                drawRectangle(customImageView, createCanvasViewModel.unitPixels[WIDTH], createCanvasViewModel.unitPixels[HEIGHT])
            createCanvasViewModel.UNIT_INCH -> {
                val inPixels = inchToPixels(createCanvasViewModel.unitInches[WIDTH],
                        createCanvasViewModel.unitInches[HEIGHT],
                        createCanvasViewModel.dpi)
                        ?: arrayListOf(0,0)
                drawRectangle(customImageView, inPixels[WIDTH], inPixels[HEIGHT])
            }
            else -> {
                val inPixels = mmToPixels(createCanvasViewModel.unitMillimeters[WIDTH],
                        createCanvasViewModel.unitMillimeters[HEIGHT],
                        createCanvasViewModel.dpi)
                        ?: arrayListOf(0,0)
                drawRectangle(customImageView, inPixels[WIDTH], inPixels[HEIGHT])
            }
        }
    }

    //By https://stackoverflow.com/users/4233197/hiren-patel
    private fun setKeyboardVisibilityListener(root: View) {
        try {
            val parentView = (activity?.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)
            parentView.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    private var alreadyOpen = false
                    private val defaultKeyboardHeightDP = 100
                    private val EstimatedKeyboardDP = defaultKeyboardHeightDP +
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) 48 else 0
                    private val rect: Rect = Rect()
                    override fun onGlobalLayout() {
                        val estimatedKeyboardHeight = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            EstimatedKeyboardDP.toFloat(),
                            parentView.resources.displayMetrics
                        ).toInt()
                        parentView.getWindowVisibleDisplayFrame(rect)
                        val heightDiff: Int = parentView.rootView.height - (rect.bottom - rect.top)
                        val isShown = heightDiff >= estimatedKeyboardHeight
                        if (isShown == alreadyOpen) {
                            //Log.i("Keyboard state", "Ignoring global layout change...")
                            return
                        }
                        alreadyOpen = isShown
                        if (!isShown) {
                            root.findViewById<EditText>(R.id.fragment_create_canvas_card_custom_pixel_input_width).clearFocus()
                            root.findViewById<EditText>(R.id.fragment_create_canvas_card_custom_pixel_input_height).clearFocus()
                            root.findViewById<EditText>(R.id.fragment_create_canvas_card_custom_dpi_input_height).clearFocus()
                            root.findViewById<EditText>(R.id.fragment_create_canvas_card_custom_dpi_input_width).clearFocus()
                            root.findViewById<EditText>(R.id.fragment_create_canvas_card_custom_dpi_input).clearFocus()
                        }
                    }
                })
        }
        catch (e: Exception){}
    }

    private fun drawRectangle(imageView: ImageView, width: Int, height: Int){
        val bitmap = Bitmap.createBitmap(72, 72, Bitmap.Config.ARGB_8888)
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5F
        paint.color = Color.BLACK
        paint.isAntiAlias = true
        if(width>0 && height>0) {
            val scaledWidth = (width * bitmap.width / MAX_WIDTH)
            val scaledHeight = (height * bitmap.height / MAX_HEIGHT)
            val centerOfCanvas = Point(bitmap.width / 2, bitmap.height / 2)
            val rectangle = Rect(
                centerOfCanvas.x - scaledWidth / 2 + 1,
                centerOfCanvas.y - scaledHeight / 2 + 1,
                centerOfCanvas.x + scaledWidth / 2 - 1,
                centerOfCanvas.y + scaledHeight / 2 - 1
            )
            Canvas(bitmap).drawRect(rectangle, paint)
        }
        else Canvas(bitmap).drawRect(Rect(0,0,0,0), paint)
        imageView.setImageBitmap(bitmap)
    }

    private fun moveCursorToEndOfEditText(editText: EditText){
        if(editText.text!=null && editText.text.isNotEmpty())
            editText.setSelection(editText.text.length)
    }

    private fun scrollToBottom(){
        scrollToBottomLayout.requestFocus()
        scrollToBottomLayout.clearFocus()}
    private fun beginChange(){createCanvasViewModel.change = createCanvasViewModel.UNCOMMITTED}
    private fun commitChange() {createCanvasViewModel.change = createCanvasViewModel.COMMITTED}
    private fun isUnderChange() = createCanvasViewModel.change == createCanvasViewModel.UNCOMMITTED

    override fun onAttach(context: Context) {
        super.onAttach(context)
        interfaceOfMainActivity = activity as InterfaceMainActivity
    }

    private fun showToast(text: String){
        interfaceOfMainActivity.showToast(text)
    }

    companion object {
        const val IMPORT_IMAGE_INTENT_KEY = "image_uri"
        const val IMPORT_CV_IMAGE_INTENT_KEY = "cv_image"
        const val DIMENSION_WIDTH_INTENT_KEY = "width_in_pixels"
        const val DIMENSION_HEIGHT_INTENT_KEY = "eight_in_pixels"
        const val MAX_WIDTH = 2048
        const val MAX_HEIGHT = 2048
        const val MIN_WIDTH = 10
        const val MIN_HEIGHT = 10
        const val WIDTH = 0
        const val HEIGHT = 1
        enum class CanvasDefaultSize(val width: Int, val height: Int) {
            SD_SIZE(540, 984),
            HD_SIZE(1080, 1968),
            DEFAULT_1_1(768, 768),
            DEFAULT_3_4(768, 1024),
            DEFAULT_9_16(720, 1280),
            A4(1240, 1754),
        }
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        when(requestCode){
//            CODE_PERMISSION -> {
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    //permission from popup granted
//                    val intent = Intent(Intent.ACTION_PICK)
//                    intent.type = "image/*"
//                    resultLauncher.launch(intent) //instead of startActivityForResult(intent, CODE_IMAGE_PICK)
//                } else {
//                    //permission from popup denied
//                    showToast("Permission denied")
//                }
//            }
//        }
//    }
}
