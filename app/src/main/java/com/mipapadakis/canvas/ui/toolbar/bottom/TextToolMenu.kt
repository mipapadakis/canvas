package com.mipapadakis.canvas.ui.toolbar.bottom

import android.graphics.Typeface
import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import androidx.lifecycle.LifecycleOwner
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.tools.ColorValues
import com.mipapadakis.canvas.ui.toolbar.bottom.editors.ColorEditorHelper
import com.mipapadakis.canvas.ui.toolbar.bottom.editors.SizeEditorHelper
import android.text.method.ScrollingMovementMethod
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener

class TextToolMenu(owner: LifecycleOwner, textLayout: View){
    private var toolTextColorBtn = textLayout.findViewById<ImageButton>(R.id.property_text_color_btn)
    private var toolTextColorEditor = textLayout.findViewById<LinearLayout>(R.id.property_text_color_editor)
    private var toolTextSizeBtn = textLayout.findViewById<TextView>(R.id.property_text_size_btn)
    private var toolTextSizeEditor = textLayout.findViewById<LinearLayout>(R.id.property_text_size_editor)
    private var toolTextFontBtn = textLayout.findViewById<TextView>(R.id.property_text_font_btn)
    private var toolTextFontEditor = textLayout.findViewById<HorizontalScrollView>(R.id.property_text_font_editor)
    private var toolTextBoldBtn = textLayout.findViewById<ImageButton>(R.id.property_text_bold_btn)
    private var toolTextItalicsBtn = textLayout.findViewById<ImageButton>(R.id.property_text_italics_btn)
    private var editorTextEditText = textLayout.findViewById<EditText>(R.id.property_text_text_editor) //[0,100]
    private var editorSizeSeekbar = toolTextSizeEditor.findViewById<SeekBar>(R.id.property_size_seekbar) //[0,100] property_text_editor
    private var editorFont1 = textLayout.findViewById<TextView>(R.id.font_1)
    private var editorFont2 = textLayout.findViewById<TextView>(R.id.font_2)
    private var editorFont3 = textLayout.findViewById<TextView>(R.id.font_3)
    private var editorFont4 = textLayout.findViewById<TextView>(R.id.font_4)
    private var editorFont5 = textLayout.findViewById<TextView>(R.id.font_5)
    private var editorFont6 = textLayout.findViewById<TextView>(R.id.font_6)
    private var editorFont7 = textLayout.findViewById<TextView>(R.id.font_7)
    private var editorFont8 = textLayout.findViewById<TextView>(R.id.font_8)

    init {
        ColorEditorHelper(owner, toolTextColorEditor){ hideAllEditors() }
        if(CanvasViewModel.textPaint.typeface==null)
            CanvasViewModel.textPaint.typeface = ResourcesCompat.getFont(textLayout.context, R.font.roboto_regular)
        toolTextSizeBtn.text = CanvasViewModel.textPaint.textSize.toInt().toString()
        toolTextFontBtn.movementMethod = ScrollingMovementMethod() //In case text-width > maxWidth
        if(CanvasViewModel.textPaint.typeface.isBold) toolTextBoldBtn.setImageResource(R.drawable.bold)
        else toolTextBoldBtn.setImageResource(R.drawable.bold_no_outline)
        if(CanvasViewModel.textPaint.typeface.isItalic) toolTextItalicsBtn.setImageResource(R.drawable.italic)
        else toolTextItalicsBtn.setImageResource(R.drawable.italic_no_outline)
        editorTextEditText.setText(CanvasViewModel.textToolText)

        toolTextColorBtn.setOnClickListener {
            hideAllEditors()
            toolTextColorEditor.visibility = View.VISIBLE
            doWhenTheViewIsVisible(toolTextColorEditor){
                CanvasViewModel.newColorHue = ColorValues.colorOnlyHue(CanvasViewModel.textPaint.color)
                CanvasViewModel.setTemporaryColor(CanvasViewModel.textPaint.color)
            }
        }
        toolTextSizeBtn.setOnClickListener {
            hideAllEditors()
            toolTextSizeEditor.visibility = View.VISIBLE
            toolTextSizeBtn.text = CanvasViewModel.textPaint.textSize.toInt().toString()
            editorSizeSeekbar.progress = CanvasViewModel.textPaint.textSize.toInt()
            doWhenTheViewIsVisible(toolTextSizeBtn){
                SizeEditorHelper.updateSeekbar(editorSizeSeekbar)
            }
        }
        toolTextFontBtn.setOnClickListener {
            hideAllEditors()
            toolTextFontEditor.visibility = View.VISIBLE
            CanvasViewModel
        }
        toolTextBoldBtn.setOnClickListener {
            when {
                CanvasViewModel.textPaint.typeface.isBold -> {
                    if(CanvasViewModel.textPaint.typeface.isItalic)
                        CanvasViewModel.textPaint.typeface = Typeface.create(CanvasViewModel.textPaint.typeface, Typeface.ITALIC)
                    else
                        CanvasViewModel.textPaint.typeface = Typeface.create(CanvasViewModel.textPaint.typeface, Typeface.NORMAL)
                    toolTextBoldBtn.setImageResource(R.drawable.bold_no_outline)
                }
                CanvasViewModel.textPaint.typeface.isItalic -> {
                    CanvasViewModel.textPaint.typeface = Typeface.create(CanvasViewModel.textPaint.typeface, Typeface.BOLD_ITALIC)
                    toolTextBoldBtn.setImageResource(R.drawable.bold)
                }
                else -> {
                    CanvasViewModel.textPaint.typeface = Typeface.create(CanvasViewModel.textPaint.typeface, Typeface.BOLD)
                    toolTextBoldBtn.setImageResource(R.drawable.bold)
                }
            }
        }
        toolTextItalicsBtn.setOnClickListener {
            when {
                CanvasViewModel.textPaint.typeface.isItalic -> {
                    if(CanvasViewModel.textPaint.typeface.isBold)
                        CanvasViewModel.textPaint.typeface = Typeface.create(CanvasViewModel.textPaint.typeface, Typeface.BOLD)
                    else
                        CanvasViewModel.textPaint.typeface = Typeface.create(CanvasViewModel.textPaint.typeface, Typeface.NORMAL)
                    toolTextItalicsBtn.setImageResource(R.drawable.italic_no_outline)
                }
                CanvasViewModel.textPaint.typeface.isBold -> {
                    CanvasViewModel.textPaint.typeface = Typeface.create(CanvasViewModel.textPaint.typeface, Typeface.BOLD_ITALIC)
                    toolTextItalicsBtn.setImageResource(R.drawable.italic)
                }
                else -> {
                    CanvasViewModel.textPaint.typeface = Typeface.create(CanvasViewModel.textPaint.typeface, Typeface.ITALIC)
                    toolTextItalicsBtn.setImageResource(R.drawable.italic)
                }
            }
        }

        ////////////////////////////////////////SIZE EDITOR/////////////////////////////////////////
        editorSizeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(!fromUser) return //TODO what if the progress (the font size) is too small?
                toolTextSizeBtn.text = progress.toString()
                CanvasViewModel.textPaint.textSize = progress.toFloat()
            }
        })

        ////////////////////////////////////////TEXT EDITOR/////////////////////////////////////////
        editorTextEditText.addTextChangedListener {
            CanvasViewModel.textToolText = it.toString()
        }

        ////////////////////////////////////////FONT EDITOR/////////////////////////////////////////
        setFontButtonListener(editorFont1)
        setFontButtonListener(editorFont2)
        setFontButtonListener(editorFont3)
        setFontButtonListener(editorFont4)
        setFontButtonListener(editorFont5)
        setFontButtonListener(editorFont6)
        setFontButtonListener(editorFont7)
        setFontButtonListener(editorFont8)
    }

    private fun setFontButtonListener(fontBtn: TextView){
        fontBtn.setOnClickListener {
            CanvasViewModel.textPaint.typeface = Typeface.create(fontBtn.typeface,
                if(CanvasViewModel.textPaint.typeface.isBold && CanvasViewModel.textPaint.typeface.isItalic) Typeface.BOLD_ITALIC
                else if (CanvasViewModel.textPaint.typeface.isBold ) Typeface.BOLD
                else if (CanvasViewModel.textPaint.typeface.isItalic ) Typeface.ITALIC
                else Typeface.NORMAL)
            toolTextFontBtn.text = fontBtn.text.toString()

//            doWhenTheViewIsVisible(canvasImageView) {
//                val randomX = Random.nextInt(0, canvasImageView.width/2).toFloat()
//                val randomY = Random.nextInt(0, canvasImageView.height).toFloat()
//                canvasImageView.writeText("Example Text", randomX, randomY)
//            }
        }
    }

    fun hideAllEditors(){
        toolTextColorEditor.visibility = View.GONE
        toolTextSizeEditor.visibility = View.GONE
        toolTextFontEditor.visibility = View.GONE
    }

    private fun doWhenTheViewIsVisible(view: View, function: () -> Unit) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {//https://stackoverflow.com/a/15578844/11535380
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                function()
            }
        })
    }
}