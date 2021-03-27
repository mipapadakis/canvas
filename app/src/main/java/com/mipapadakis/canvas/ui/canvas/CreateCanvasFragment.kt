package com.mipapadakis.canvas.ui.canvas

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


private const val WIDTH = 0
private const val HEIGHT = 1
private const val CODE_IMAGE_PICK = 1000
private const val CODE_PERMISSION = 1001


class CreateCanvasFragment : Fragment() {
    private lateinit var canvasViewModel: CanvasViewModel
    private lateinit var interfaceMainActivity: InterfaceMainActivity
    private lateinit var scrollToBottomLayout: FrameLayout
    private lateinit var pixelLayout: LinearLayout
    private lateinit var dpiLayout: LinearLayout
    private lateinit var pixelBtn: Button
    private lateinit var mmBtn: Button
    private lateinit var inchBtn: Button
    private var importedImagePreview: ImageView? = null

    companion object {
        const val IMPORT_IMAGE_INTENT_KEY = "image_uri"
        enum class CanvasDefaultSize(val width: Int, val height: Int) {
            SD_SIZE(540, 984),
            HD_SIZE(1080, 1968),
            DEFAULT_1_1(768, 768),
            DEFAULT_3_4(768, 1024),
            DEFAULT_9_16(720, 1280),
            A4(1240, 1754),
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        interfaceMainActivity.hideFab() //TODO: Doesn't work after a lifecycle event!
//        interfaceMainActivity.showFab()
//        interfaceMainActivity.setFabListener {
//            val str = "Random Number: " + (Math.random()*100).toInt()
//            showToast("Fab pressed at Canvas fragment!")
//            canvasViewModel.setText(str)
//        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        canvasViewModel = ViewModelProvider(this).get(CanvasViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_create_canvas, container, false)
        importedImagePreview = root.findViewById(R.id.import_image_view)

//        Observable example:
//        val textView: TextView = root.findViewById(R.id.text_canvas)
//        canvasViewModel.text.observe(viewLifecycleOwner, {
//            textView.text = it })
        initializeList(root)
        return root
    }

    private fun initializeList(root: View) { //}: ArrayList<Int>{
        val importDetails = root.findViewById<TextView>(R.id.import_details)
        importDetails.text = resources.getString(R.string.create_canvas_import_details)

        drawRectangle(root.findViewById(R.id.sd_image_view), CanvasDefaultSize.SD_SIZE.width, CanvasDefaultSize.SD_SIZE.height, true)
        drawRectangle(root.findViewById(R.id.hd_image_view), CanvasDefaultSize.HD_SIZE.width, CanvasDefaultSize.HD_SIZE.height, true)
        drawRectangle(root.findViewById(R.id.default_1_1_image_view), CanvasDefaultSize.DEFAULT_1_1.width, CanvasDefaultSize.DEFAULT_1_1.height, true)
        drawRectangle(root.findViewById(R.id.default_3_4_image_view), CanvasDefaultSize.DEFAULT_3_4.width, CanvasDefaultSize.DEFAULT_3_4.height, true)
        drawRectangle(root.findViewById(R.id.default_9_16_image_view), CanvasDefaultSize.DEFAULT_9_16.width, CanvasDefaultSize.DEFAULT_9_16.height, true)
        drawRectangle(root.findViewById(R.id.A4_image_view), CanvasDefaultSize.A4.width, CanvasDefaultSize.A4.height, true)

        root.findViewById<MaterialCardView>(R.id.import_card).setOnClickListener {
            if(importedImagePreview==null) importedImagePreview = root.findViewById(R.id.import_image_view)
            if(canvasViewModel.importImagePreview.value?.isEmpty() == true) pickImageFromGallery()
            else if(canvasViewModel.importImagePreview.value!=null) {
                val intent = Intent(context, CanvasActivity::class.java)
                intent.putExtra(IMPORT_IMAGE_INTENT_KEY, canvasViewModel.importImagePreview.value)
                startActivity(intent)
            }
        }
        canvasViewModel.importImagePreview.observe(viewLifecycleOwner, {
            if (it.isEmpty()) {
                importedImagePreview?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.baseline_collections_black_24))
                importDetails.text = resources.getString(R.string.create_canvas_import_details)
            } else {
                importedImagePreview?.setImageURI(Uri.parse(it))
                importDetails.text = resources.getString(R.string.create_canvas_import_details_2)
            }
        })
        importedImagePreview?.setOnClickListener {
            if(canvasViewModel.importImagePreview.value?.isNotEmpty() == true){
                canvasViewModel.setImportImagePreview("")
                pickImageFromGallery()
            }
        }

        root.findViewById<MaterialCardView>(R.id.sd_card).setOnClickListener {
            showToast("${CanvasDefaultSize.SD_SIZE.width}, ${CanvasDefaultSize.SD_SIZE.height}")}
        root.findViewById<MaterialCardView>(R.id.hd_card).setOnClickListener {
            showToast("${CanvasDefaultSize.HD_SIZE.width}, ${CanvasDefaultSize.HD_SIZE.height}") }
        root.findViewById<MaterialCardView>(R.id.default_1_1_card).setOnClickListener {
            showToast("${CanvasDefaultSize.DEFAULT_1_1.width}, ${CanvasDefaultSize.DEFAULT_1_1.height}") }
        root.findViewById<MaterialCardView>(R.id.default_3_4_card).setOnClickListener {
            showToast("${CanvasDefaultSize.DEFAULT_3_4.width}, ${CanvasDefaultSize.DEFAULT_3_4.height}") }
        root.findViewById<MaterialCardView>(R.id.default_9_16_card).setOnClickListener {
            showToast("${CanvasDefaultSize.DEFAULT_9_16.width}, ${CanvasDefaultSize.DEFAULT_9_16.height}") }
        root.findViewById<MaterialCardView>(R.id.A4_card).setOnClickListener {
            showToast("${CanvasDefaultSize.A4.width}, ${CanvasDefaultSize.A4.height}") }
        root.findViewById<MaterialCardView>(R.id.custom_card).setOnClickListener {
            when {
                canvasViewModel.customUnit.value == canvasViewModel.UNIT_PIXEL ->
                    showToast("${canvasViewModel.unitPixels[WIDTH]}, ${canvasViewModel.unitPixels[HEIGHT]}")
                dpiInputIsValidInPixels() ->
                    showToast(inchToPixels(canvasViewModel.unitInches[WIDTH], canvasViewModel.unitInches[HEIGHT], canvasViewModel.dpi).toString())
                else -> showToast("Wrong Input!\n Pixel dimensions must be between 1 and 4096.")
            }
        }

        ///////////////////////////////////////Custom Size//////////////////////////////////////////
        scrollToBottomLayout = root.findViewById(R.id.scroll_to_bottom_layout)
        pixelLayout = root.findViewById(R.id.custom_pixel_layout)
        dpiLayout = root.findViewById(R.id.custom_dpi_layout)
        pixelBtn = root.findViewById(R.id.custom_pixel_button)
        mmBtn = root.findViewById(R.id.custom_mm_button)
        inchBtn = root.findViewById(R.id.custom_inch_button)
        updateCustomSizeImageView(root.findViewById(R.id.custom_image_view))

        //Following views must not trigger the card listener when clicked => override clickListener:
        pixelLayout.setOnClickListener {}
        dpiLayout.setOnClickListener {}
        root.findViewById<LinearLayout>(R.id.custom_button_toggle_group).setOnClickListener {}

        canvasViewModel.customUnit.observe(viewLifecycleOwner, {
            when (it) {
                canvasViewModel.UNIT_PIXEL -> setPixelUnit()
                canvasViewModel.UNIT_MM -> setMmUnit(root)
                else -> setInchUnit(root)
            }
        })

        pixelBtn.setOnClickListener {
            canvasViewModel.setCustomUnit(canvasViewModel.UNIT_PIXEL)
            updateCustomSizeImageView(root.findViewById(R.id.custom_image_view))}
        mmBtn.setOnClickListener { canvasViewModel.setCustomUnit(canvasViewModel.UNIT_MM)}
        inchBtn.setOnClickListener { canvasViewModel.setCustomUnit(canvasViewModel.UNIT_INCH)}
        setPixelLayoutListeners(root)
        setDpiLayoutListeners(root)
        setKeyboardVisibilityListener(root)
    }

    private fun pickImageFromGallery(){
        //check runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if ( checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                //show popup to request runtime permission
                requestPermissions(permissions, CODE_PERMISSION)
                return
            }
            //else: permission already granted
        }
        //else: system OS is < Marshmallow
        val intent = Intent(Intent.ACTION_PICK) //Intent to pick image
        intent.type = "image/*"
        startActivityForResult(intent, CODE_IMAGE_PICK)
    }

    private fun setPixelUnit(){
        val deactivatedButtonColor = ContextCompat.getColor(requireContext(), R.color.gray_1)
        pixelBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.yellow_2))
        mmBtn.setBackgroundColor(deactivatedButtonColor)
        inchBtn.setBackgroundColor(deactivatedButtonColor)
        pixelLayout.visibility = View.VISIBLE
        dpiLayout.visibility = View.GONE
    }
    private fun setMmUnit(root: View){
        val deactivatedButtonColor = ContextCompat.getColor(requireContext(), R.color.gray_1)
        //If dpiWidth and dpiHeight values were previously inches, convert them to mm
        beginChange()
        root.findViewById<EditText>(R.id.custom_dpi_input_width).setText(
                canvasViewModel.unitMillimeters[WIDTH].toString())
        root.findViewById<EditText>(R.id.custom_dpi_input_height).setText(
                canvasViewModel.unitMillimeters[HEIGHT].toString())
        commitChange()
        root.findViewById<TextView>(R.id.custom_dpi_width_unit).text = resources.getString(R.string.unit_millimeters)
        root.findViewById<TextView>(R.id.custom_dpi_height_unit).text = resources.getString(R.string.unit_millimeters)
        pixelBtn.setBackgroundColor(deactivatedButtonColor)
        mmBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.yellow_2))
        inchBtn.setBackgroundColor(deactivatedButtonColor)
        pixelLayout.visibility = View.GONE
        dpiLayout.visibility = View.VISIBLE
        canvasViewModel.lastDpiUnitUsed = canvasViewModel.UNIT_MM
        updateCustomSizeImageView(root.findViewById(R.id.custom_image_view))
        scrollToBottom()
    }
    private fun setInchUnit(root: View){
        val deactivatedButtonColor = ContextCompat.getColor(requireContext(), R.color.gray_1)
        //If dpiWidth and dpiHeight values were previously millimeters, convert them to inches
        beginChange()
        root.findViewById<EditText>(R.id.custom_dpi_input_width).setText(
                canvasViewModel.unitInches[WIDTH].toString())
        root.findViewById<EditText>(R.id.custom_dpi_input_height).setText(
                canvasViewModel.unitInches[HEIGHT].toString())
        commitChange()
        root.findViewById<TextView>(R.id.custom_dpi_width_unit).text = resources.getString(R.string.unit_inches)
        root.findViewById<TextView>(R.id.custom_dpi_height_unit).text = resources.getString(R.string.unit_inches)
        pixelBtn.setBackgroundColor(deactivatedButtonColor)
        mmBtn.setBackgroundColor(deactivatedButtonColor)
        inchBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.yellow_2))
        pixelLayout.visibility = View.GONE
        dpiLayout.visibility = View.VISIBLE
        canvasViewModel.lastDpiUnitUsed = canvasViewModel.UNIT_INCH
        updateCustomSizeImageView(root.findViewById(R.id.custom_image_view))
        scrollToBottom()
    }

    @SuppressLint("SetTextI18n")
    private fun setPixelLayoutListeners(root: View){
        val widthEditText = root.findViewById<EditText>(R.id.custom_pixel_input_width)
        val heightEditText = root.findViewById<EditText>(R.id.custom_pixel_input_height)
        val widthSlider = root.findViewById<Slider>(R.id.custom_pixel_slider_width)
        val heightSlider = root.findViewById<Slider>(R.id.custom_pixel_slider_height)

        widthSlider.addOnChangeListener { _, value, _ ->
            if(!isUnderChange()) {
                widthEditText.setText(value.toInt().toString())
                updateCustomSizeImageView(root.findViewById(R.id.custom_image_view))
                moveCursorToEndOfEditText(widthEditText)
            }
        }
        heightSlider.addOnChangeListener { _, value, _ ->
            if(!isUnderChange()) {
                heightEditText.setText(value.toInt().toString())
                updateCustomSizeImageView(root.findViewById(R.id.custom_image_view))
                moveCursorToEndOfEditText(heightEditText)
            }
        }

        widthEditText.addTextChangedListener {
            val input = if(it == null || it.isEmpty() || !it.toString().isDigitsOnly() || it.toString().toInt()<=0){
                if(!it.toString().isDigitsOnly()) canvasViewModel.unitPixels[WIDTH]
                else 1
            } else if(it.toString().toInt() > 4096){
                4096
            } else it.toString().toInt()
            canvasViewModel.unitPixels[WIDTH] = input
            beginChange()
            widthSlider.value = input.toFloat()
            commitChange()
            updateCustomSizeImageView(root.findViewById(R.id.custom_image_view))
        }
        heightEditText.addTextChangedListener {
            val input = if(it == null || it.isEmpty() || !it.toString().isDigitsOnly() || it.toString().toInt()<=0){
                if(!it.toString().isDigitsOnly()) canvasViewModel.unitPixels[HEIGHT]
                else 1
            } else if(it.toString().toInt() > 4096){
                4096
            } else it.toString().toInt()
            canvasViewModel.unitPixels[HEIGHT] = input
            beginChange()
            heightSlider.value = input.toFloat()
            commitChange()
            updateCustomSizeImageView(root.findViewById(R.id.custom_image_view))
        }
    }

    private fun setDpiLayoutListeners(root: View){
        val widthEditText = root.findViewById<EditText>(R.id.custom_dpi_input_width)
        val heightEditText = root.findViewById<EditText>(R.id.custom_dpi_input_height)
        val dpiEditText = root.findViewById<EditText>(R.id.custom_dpi_input)
        val inPixelTextView = root.findViewById<TextView>(R.id.custom_dpi_in_pixels)

        widthEditText.addTextChangedListener{
            if(canvasViewModel.customUnit.value==canvasViewModel.UNIT_INCH){
                val input = if(it == null || it.isEmpty() || !isDouble(it.toString())){
                    if(!isDouble(it.toString())) canvasViewModel.unitInches[WIDTH]
                    else 0.0
                } else it.toString().toDouble()
                canvasViewModel.unitInches[WIDTH] = input
                if(!isUnderChange()) canvasViewModel.unitMillimeters[WIDTH] = inchToMm(input)
            }
            else{
                val input = if(it == null || it.isEmpty() || !isDouble(it.toString())){
                    if(!isDouble(it.toString())) canvasViewModel.unitMillimeters[WIDTH]
                    else 0.0
                } else it.toString().toDouble()
                canvasViewModel.unitMillimeters[WIDTH] = input
                if(!isUnderChange()) canvasViewModel.unitInches[WIDTH] = mmToInch(input)
            }
            updateInPixelsTextView(inPixelTextView)
            updateCustomSizeImageView(root.findViewById(R.id.custom_image_view))
        }
        heightEditText.addTextChangedListener {
            if(canvasViewModel.customUnit.value==canvasViewModel.UNIT_INCH){
                val input = if(it == null || it.isEmpty() || !isDouble(it.toString())){
                    if(!isDouble(it.toString())) canvasViewModel.unitInches[HEIGHT]
                    else 0.0
                } else it.toString().toDouble()
                canvasViewModel.unitInches[HEIGHT] = input
                if(!isUnderChange()) canvasViewModel.unitMillimeters[HEIGHT] = inchToMm(input)
            }
            else{
                val input = if(it == null || it.isEmpty() || !isDouble(it.toString())){
                    if(!isDouble(it.toString())) canvasViewModel.unitMillimeters[HEIGHT]
                    else 0.0
                } else it.toString().toDouble()
                canvasViewModel.unitMillimeters[HEIGHT] = input
                if(!isUnderChange()) canvasViewModel.unitInches[HEIGHT] = mmToInch(input)
//                if(abs(canvasViewModel.unitInches[WIDTH] - mmToInch(input)) >=0.009)
//                    canvasViewModel.unitMillimeters[HEIGHT] = inchToMm(input)
            }
            updateInPixelsTextView(inPixelTextView)
            updateCustomSizeImageView(root.findViewById(R.id.custom_image_view))
        }
        dpiEditText.addTextChangedListener {
            val input = if(it == null || it.isEmpty() || !it.toString().isDigitsOnly()){
                if(!it.toString().isDigitsOnly()) canvasViewModel.dpi
                else 0
            } else it.toString().toInt()
            canvasViewModel.dpi = input
            updateInPixelsTextView(inPixelTextView)
            updateCustomSizeImageView(root.findViewById(R.id.custom_image_view))
        }
    }

    //Returns null if pixels outside (0,4096]
    private fun inchToPixels(width: Double, height: Double, dpi: Int): ArrayList<Int>?{
        val pixels: ArrayList<Int> = arrayListOf((width * dpi).toInt(), (height * dpi).toInt())
        if(pixels[WIDTH]<=0 || pixels[WIDTH]>4096 || pixels[HEIGHT]<=0 || pixels[HEIGHT]>4096)
            return null
        return pixels
    }
    //Returns null if pixels outside (0,4096]
    private fun mmToPixels(width: Double, height: Double, dpi: Int)
    = inchToPixels(mmToInch(width), mmToInch(height), dpi)
    private fun mmToInch(mm: Double): Double = mm/25.4
    private fun inchToMm(inch: Double): Double = inch*25.4
    private fun isDouble(str: String) = (str.isDigitsOnly() || str.contains("."))
    private fun dpiInputIsValidInPixels(): Boolean{
        val inPixels = if(canvasViewModel.customUnit.value == canvasViewModel.UNIT_MM){
            mmToPixels(canvasViewModel.unitMillimeters[WIDTH],
                    canvasViewModel.unitMillimeters[HEIGHT], canvasViewModel.dpi)
        } else{
            inchToPixels(canvasViewModel.unitInches[WIDTH],
                    canvasViewModel.unitInches[HEIGHT], canvasViewModel.dpi)
        }
        return inPixels != null
    }
    private fun updateInPixelsTextView(inPixelTextView: TextView){
        val inPixels = if(canvasViewModel.customUnit.value == canvasViewModel.UNIT_MM){
            mmToPixels(canvasViewModel.unitMillimeters[WIDTH],
                    canvasViewModel.unitMillimeters[HEIGHT], canvasViewModel.dpi)
        } else{
            inchToPixels(canvasViewModel.unitInches[WIDTH],
                    canvasViewModel.unitInches[HEIGHT], canvasViewModel.dpi)
        }
        //Update inPixelTextView:
        if(inPixels!=null) {
            inPixelTextView.text = String.format(
                    resources.getString(R.string.create_canvas_custom_dpi_in_pixels),
                    inPixels[WIDTH].toString(),
                    inPixels[HEIGHT].toString())
        }else {
            inPixelTextView.text = getString(R.string.error)
        }
    }

    private fun updateCustomSizeImageView(customImageView: ImageView){
        when (canvasViewModel.customUnit.value) {
            canvasViewModel.UNIT_PIXEL ->
                drawRectangle(customImageView, canvasViewModel.unitPixels[WIDTH], canvasViewModel.unitPixels[HEIGHT], false)
            canvasViewModel.UNIT_INCH -> {
                val inPixels = inchToPixels(canvasViewModel.unitInches[WIDTH],
                        canvasViewModel.unitInches[HEIGHT],
                        canvasViewModel.dpi)
                        ?: arrayListOf(0,0)
                drawRectangle(customImageView, inPixels[WIDTH], inPixels[HEIGHT], false)
            }
            else -> {
                val inPixels = mmToPixels(canvasViewModel.unitMillimeters[WIDTH],
                        canvasViewModel.unitMillimeters[HEIGHT],
                        canvasViewModel.dpi)
                        ?: arrayListOf(0,0)
                drawRectangle(customImageView, inPixels[WIDTH], inPixels[HEIGHT], false)
            }
        }
    }

    //By https://stackoverflow.com/users/4233197/hiren-patel
    private fun setKeyboardVisibilityListener(root: View) {
        val parentView = (activity?.findViewById<View>(android.R.id.content)
                as ViewGroup).getChildAt(0)
        parentView.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
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
                    root.findViewById<EditText>(R.id.custom_pixel_input_width).clearFocus()
                    root.findViewById<EditText>(R.id.custom_pixel_input_height).clearFocus()
                    root.findViewById<EditText>(R.id.custom_dpi_input_height).clearFocus()
                    root.findViewById<EditText>(R.id.custom_dpi_input_width).clearFocus()
                    root.findViewById<EditText>(R.id.custom_dpi_input).clearFocus()
                }
            }
        })
    }

    private fun drawRectangle(imageView: ImageView, width: Int, height: Int, matchBounds: Boolean){
        val bitmap = Bitmap.createBitmap(72, 72, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT)
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5F
        paint.color = Color.BLACK
        paint.isAntiAlias = true
        if(width>0 && height>0) {
            val scaledWidth = (width * canvas.width / 4096)
            val scaledHeight = (height * canvas.height / 4096)
            val centerOfCanvas = Point(canvas.width / 2, canvas.height / 2)
            val left: Int
            val top: Int
            val right: Int
            val bottom: Int

            if(matchBounds){ //Stretch the rectangle to match the bounds.
                val stretchedWidth: Int
                val stretchedHeight: Int
                if(width>=height){
                    stretchedWidth = canvas.width
                    stretchedHeight = scaledHeight*(stretchedWidth/scaledWidth)
                }
                else{
                    stretchedHeight = canvas.height
                    stretchedWidth = scaledWidth*(stretchedHeight/scaledHeight)
                }
                left = centerOfCanvas.x - stretchedWidth / 2 + 1
                top = centerOfCanvas.y - stretchedHeight / 2 + 1
                right = centerOfCanvas.x + stretchedWidth / 2 - 1
                bottom = centerOfCanvas.y + stretchedHeight / 2 - 1
            }
            else{ //Don't stretch the rectangle: maintain relativity.
                left = centerOfCanvas.x - scaledWidth / 2 + 1
                top = centerOfCanvas.y - scaledHeight / 2 + 1
                right = centerOfCanvas.x + scaledWidth / 2 - 1
                bottom = centerOfCanvas.y + scaledHeight / 2 - 1
            }
            val rectangle = Rect(left, top, right, bottom)
            canvas.drawRect(rectangle, paint)
        }
        else canvas.drawRect(Rect(0,0,0,0), paint)
        imageView.setImageBitmap(bitmap)
    }

    private fun moveCursorToEndOfEditText(editText: EditText){
        if(editText.text!=null && editText.text.isNotEmpty())
            editText.setSelection(editText.text.length)
    }

    private fun scrollToBottom(){
        scrollToBottomLayout.requestFocus()
        scrollToBottomLayout.clearFocus()}
    private fun beginChange(){canvasViewModel.change = canvasViewModel.UNCOMMITTED}
    private fun commitChange() {canvasViewModel.change = canvasViewModel.COMMITTED}
    private fun isUnderChange() = canvasViewModel.change == canvasViewModel.UNCOMMITTED

    override fun onAttach(context: Context) {
        super.onAttach(context)
        interfaceMainActivity = activity as InterfaceMainActivity
    }

    private fun showToast(text: String){
        interfaceMainActivity.showToast(text)
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            CODE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission from popup granted
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, CODE_IMAGE_PICK)
                } else {
                    //permission from popup denied
                    showToast("Permission denied")
                }
            }
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == CODE_IMAGE_PICK){
            val uri = data?.data
            importedImagePreview?.setImageURI(uri)
            canvasViewModel.setImportImagePreview(uri.toString())
        }
        else canvasViewModel.setImportImagePreview("")
    }
}

//TODO bug: When i set dpi 100 and inch width & height to 40.96: when I switch to mm, pixels are different
