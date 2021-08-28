package com.mipapadakis.canvas.ui.canvas.toolbar.bottom

import android.graphics.Typeface
import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import androidx.lifecycle.LifecycleOwner
import com.mipapadakis.canvas.CanvasActivityData
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.tools.ColorValues
import com.mipapadakis.canvas.ui.canvas.toolbar.bottom.editors.ColorEditorHelper
import com.mipapadakis.canvas.ui.canvas.toolbar.bottom.editors.SizeEditorHelper
import android.text.method.ScrollingMovementMethod
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import com.mipapadakis.canvas.tools.ShowTipDialog

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
    private var editorSizeSeekbar = toolTextSizeEditor.findViewById<SeekBar>(R.id.size_editor_seekbar) //[0,100] property_text_editor
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
        if(CanvasActivityData.textPaint.typeface==null)
            CanvasActivityData.textPaint.typeface = ResourcesCompat.getFont(textLayout.context, R.font.roboto_regular)
        toolTextSizeBtn.text = CanvasActivityData.textPaint.textSize.toInt().toString()
        toolTextFontBtn.movementMethod = ScrollingMovementMethod() //In case text-width > maxWidth
        if(CanvasActivityData.textPaint.typeface.isBold) toolTextBoldBtn.setImageResource(R.drawable.bold_outlined)
        else toolTextBoldBtn.setImageResource(R.drawable.bold)
        if(CanvasActivityData.textPaint.typeface.isItalic) toolTextItalicsBtn.setImageResource(R.drawable.italic_outlined)
        else toolTextItalicsBtn.setImageResource(R.drawable.italic)
        editorTextEditText.setText(CanvasActivityData.textToolText)

        toolTextColorBtn.setOnClickListener {
            hideAllEditors()
            toolTextColorEditor.visibility = View.VISIBLE
            doWhenTheViewIsVisible(toolTextColorEditor){
                CanvasActivityData.newColorHue = ColorValues.colorOnlyHue(CanvasActivityData.textPaint.color)
                CanvasActivityData.setTemporaryColor(CanvasActivityData.textPaint.color)
            }
        }
        toolTextSizeBtn.setOnClickListener {
            hideAllEditors()
            toolTextSizeEditor.visibility = View.VISIBLE
            toolTextSizeBtn.text = CanvasActivityData.textPaint.textSize.toInt().toString()
            editorSizeSeekbar.progress = CanvasActivityData.textPaint.textSize.toInt()
            doWhenTheViewIsVisible(toolTextSizeBtn){
                SizeEditorHelper.updateSeekbar(editorSizeSeekbar)
            }
        }
        toolTextFontBtn.setOnClickListener {
            hideAllEditors()
            toolTextFontEditor.visibility = View.VISIBLE
            CanvasActivityData
        }
        toolTextBoldBtn.setOnClickListener {
            when {
                CanvasActivityData.textPaint.typeface.isBold -> {
                    if(CanvasActivityData.textPaint.typeface.isItalic)
                        CanvasActivityData.textPaint.typeface = Typeface.create(CanvasActivityData.textPaint.typeface, Typeface.ITALIC)
                    else
                        CanvasActivityData.textPaint.typeface = Typeface.create(CanvasActivityData.textPaint.typeface, Typeface.NORMAL)
                    toolTextBoldBtn.setImageResource(R.drawable.bold)
                }
                CanvasActivityData.textPaint.typeface.isItalic -> {
                    CanvasActivityData.textPaint.typeface = Typeface.create(CanvasActivityData.textPaint.typeface, Typeface.BOLD_ITALIC)
                    toolTextBoldBtn.setImageResource(R.drawable.bold_outlined)
                }
                else -> {
                    CanvasActivityData.textPaint.typeface = Typeface.create(CanvasActivityData.textPaint.typeface, Typeface.BOLD)
                    toolTextBoldBtn.setImageResource(R.drawable.bold_outlined)
                }
            }
        }
        toolTextItalicsBtn.setOnClickListener {
            when {
                CanvasActivityData.textPaint.typeface.isItalic -> {
                    if(CanvasActivityData.textPaint.typeface.isBold)
                        CanvasActivityData.textPaint.typeface = Typeface.create(CanvasActivityData.textPaint.typeface, Typeface.BOLD)
                    else
                        CanvasActivityData.textPaint.typeface = Typeface.create(CanvasActivityData.textPaint.typeface, Typeface.NORMAL)
                    toolTextItalicsBtn.setImageResource(R.drawable.italic)
                }
                CanvasActivityData.textPaint.typeface.isBold -> {
                    CanvasActivityData.textPaint.typeface = Typeface.create(CanvasActivityData.textPaint.typeface, Typeface.BOLD_ITALIC)
                    toolTextItalicsBtn.setImageResource(R.drawable.italic_outlined)
                }
                else -> {
                    CanvasActivityData.textPaint.typeface = Typeface.create(CanvasActivityData.textPaint.typeface, Typeface.ITALIC)
                    toolTextItalicsBtn.setImageResource(R.drawable.italic_outlined)
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
                CanvasActivityData.textPaint.textSize = progress.toFloat()
            }
        })

        ////////////////////////////////////////TEXT EDITOR/////////////////////////////////////////
        editorTextEditText.addTextChangedListener {
            CanvasActivityData.textToolText = it.toString()
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

        setTipDialogs()
    }

    private fun setTipDialogs() {
        ShowTipDialog(toolTextColorBtn, R.drawable.color_palette_outlined)
        ShowTipDialog(toolTextSizeBtn, R.drawable.text_outlined)
        ShowTipDialog(toolTextFontBtn, R.drawable.text_outlined)
        ShowTipDialog(toolTextBoldBtn, R.drawable.bold_outlined)
        ShowTipDialog(toolTextItalicsBtn, R.drawable.italic_outlined)
    }

    private fun setFontButtonListener(fontBtn: TextView){
        fontBtn.setOnClickListener {
            CanvasActivityData.textPaint.typeface = Typeface.create(fontBtn.typeface,
                if(CanvasActivityData.textPaint.typeface.isBold && CanvasActivityData.textPaint.typeface.isItalic) Typeface.BOLD_ITALIC
                else if (CanvasActivityData.textPaint.typeface.isBold ) Typeface.BOLD
                else if (CanvasActivityData.textPaint.typeface.isItalic ) Typeface.ITALIC
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