package com.mipapadakis.canvas.ui.canvas

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.card.MaterialCardView
import com.google.android.material.slider.Slider
import com.mipapadakis.canvas.InterfaceMainActivity
import com.mipapadakis.canvas.R


enum class CanvasSize(val width: Int, val height: Int) {
    IMPORT(0, 0),
    SD_SIZE(540, 984),
    HD_SIZE(1080, 1968),
    DEFAULT_1_1(768, 768),
    DEFAULT_3_4(768, 1024),
    DEFAULT_9_16(720, 1280),
    A4(1240, 1754),
    CUSTOM(1000, 1000)
}

class CreateCanvasFragment : Fragment() {
    private lateinit var canvasViewModel: CanvasViewModel
    private lateinit var interfaceMainActivity: InterfaceMainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        interfaceMainActivity.hideFab()
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

        val textView: TextView = root.findViewById(R.id.text_canvas)
        canvasViewModel.text.observe(viewLifecycleOwner, {
            textView.text = it
        })
        initializeList(root)
        return root
    }

    private fun initializeList(root: View) { //}: ArrayList<Int>{
//        val size = arrayListOf<MaterialCardView>(
//            root.findViewById(R.id.import_card),
//            root.findViewById(R.id.sd_card),
//            root.findViewById(R.id.hd_card),
//            root.findViewById(R.id.default_1_1_card),
//            root.findViewById(R.id.default_3_4_card),
//            root.findViewById(R.id.default_9_16_card),
//            root.findViewById(R.id.A4_card),
//            root.findViewById(R.id.custom_card))
        root.findViewById<MaterialCardView>(R.id.import_card).setOnClickListener {
            showToast("TODO: Import picture from device")}
        root.findViewById<MaterialCardView>(R.id.sd_card).setOnClickListener {
            showToast("${CanvasSize.SD_SIZE.width}, ${CanvasSize.SD_SIZE.height}") }
        root.findViewById<MaterialCardView>(R.id.hd_card).setOnClickListener {
            showToast("${CanvasSize.HD_SIZE.width}, ${CanvasSize.HD_SIZE.height}") }
        root.findViewById<MaterialCardView>(R.id.default_1_1_card).setOnClickListener {
            showToast("${CanvasSize.DEFAULT_1_1.width}, ${CanvasSize.DEFAULT_1_1.height}") }
        root.findViewById<MaterialCardView>(R.id.default_3_4_card).setOnClickListener {
            showToast("${CanvasSize.DEFAULT_3_4.width}, ${CanvasSize.DEFAULT_3_4.height}") }
        root.findViewById<MaterialCardView>(R.id.default_9_16_card).setOnClickListener {
            showToast("${CanvasSize.DEFAULT_9_16.width}, ${CanvasSize.DEFAULT_9_16.height}") }
        root.findViewById<MaterialCardView>(R.id.A4_card).setOnClickListener {
            showToast("${CanvasSize.A4.width}, ${CanvasSize.A4.height}") }
//        root.findViewById<LinearLayout>(R.id.custom_pixel_width_layout).setOnClickListener {
//            showToast("yo")
//        }
//        root.findViewById<EditText>(R.id.custom_pixel_input_width).setOnFocusChangeListener { view, b ->
//            showToast("b=$b")
//        }

        ///////////////////////////////////////Custom Size//////////////////////////////////////////
        val pixelLayout = root.findViewById<LinearLayout>(R.id.custom_pixel_layout)
        val dpiLayout = root.findViewById<LinearLayout>(R.id.custom_dpi_layout)
        val pixelBtn = root.findViewById<Button>(R.id.custom_pixel_button)
        val mmBtn = root.findViewById<Button>(R.id.custom_mm_button)
        val inchBtn = root.findViewById<Button>(R.id.custom_inch_button)

        //Following views must not trigger the card listener when clicked => override clickListener:
        pixelLayout.setOnClickListener {}
        dpiLayout.setOnClickListener {}
        root.findViewById<LinearLayout>(R.id.custom_button_toggle_group).setOnClickListener {}
        val activatedButtonColor = ContextCompat.getColor(requireContext(), R.color.yellow_2)
        val deactivatedButtonColor = ContextCompat.getColor(requireContext(), R.color.gray_1)

        pixelBtn.setOnClickListener {
            pixelBtn.setBackgroundColor(activatedButtonColor)
            mmBtn.setBackgroundColor(deactivatedButtonColor)
            inchBtn.setBackgroundColor(deactivatedButtonColor)
            pixelLayout.visibility = View.VISIBLE
            dpiLayout.visibility = View.GONE
        }
        mmBtn.setOnClickListener {
            pixelBtn.setBackgroundColor(deactivatedButtonColor)
            mmBtn.setBackgroundColor(activatedButtonColor)
            inchBtn.setBackgroundColor(deactivatedButtonColor)
            pixelLayout.visibility = View.GONE
            dpiLayout.visibility = View.VISIBLE
        }
        inchBtn.setOnClickListener {
            pixelBtn.setBackgroundColor(deactivatedButtonColor)
            mmBtn.setBackgroundColor(deactivatedButtonColor)
            inchBtn.setBackgroundColor(activatedButtonColor)
            pixelLayout.visibility = View.GONE
            dpiLayout.visibility = View.VISIBLE
        }


        setPixelLayoutListeners(root)
        setDpiLayout(root)
        setKeyboardVisibilityListener(root)
    }

    private fun setDpiLayout(root: View){
        //TODO
    }

    @SuppressLint("SetTextI18n")
    private fun setPixelLayoutListeners(root: View){
        val widthEditText = root.findViewById<EditText>(R.id.custom_pixel_input_width)
        val heightEditText = root.findViewById<EditText>(R.id.custom_pixel_input_height)
        val widthSlider = root.findViewById<Slider>(R.id.custom_pixel_slider_width)
        val heightSlider = root.findViewById<Slider>(R.id.custom_pixel_slider_height)
        widthSlider.addOnChangeListener { _, value, _ ->
            if(!widthEditText.hasFocus()) widthEditText.setText(value.toInt().toString())}
        heightSlider.addOnChangeListener { _, value, _ ->
            if(!heightEditText.hasFocus()) heightEditText.setText(value.toInt().toString())}
        widthEditText.addTextChangedListener {
            if(it==null || it.isEmpty() || it.toString().toInt()<=0) {
                showToast("Minimum of 1 pixel")
                widthEditText.setText("1")
                widthSlider.value = 1F
            }
            else if(it.toString().toInt()>4096){
                showToast("Maximum of 4096 pixels")
                widthEditText.setText("4096")
                widthSlider.value = 4096F
            }
            else widthSlider.value = it.toString().toFloat()
        }
        heightEditText.addTextChangedListener {
            if(it==null || it.isEmpty() || it.toString().toInt()<=0) {
                heightEditText.setText("1")
                heightSlider.value = 1F
            }
            else if(it.toString().toInt()>4096){
                heightEditText.setText("4096")
                heightSlider.value = 4096F
            }
            else heightSlider.value = it.toString().toFloat()
        }

        root.findViewById<MaterialCardView>(R.id.custom_card).setOnClickListener {
            //TODO Max 4096!
            showToast("${widthEditText.text}, ${heightEditText.text}") }
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
                    root.findViewById<EditText>(R.id.custom_dpi_height_input).clearFocus()
                    root.findViewById<EditText>(R.id.custom_dpi_input_width).clearFocus()
                    root.findViewById<EditText>(R.id.custom_dpi_input).clearFocus()
                }
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        interfaceMainActivity = activity as InterfaceMainActivity
    }

    private fun showToast(text: String){
        interfaceMainActivity.showToast(text)
    }
}