package com.mipapadakis.canvas.ui.canvas.toolbar.bottom

import android.graphics.Typeface
import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.ui.util.ColorValues
import com.mipapadakis.canvas.ui.canvas.toolbar.bottom.editors.ColorEditorHelper
import com.mipapadakis.canvas.ui.canvas.toolbar.bottom.editors.SizeEditorHelper
import android.text.method.ScrollingMovementMethod
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import com.mipapadakis.canvas.ui.canvas.CanvasViews
import com.mipapadakis.canvas.ui.util.ShowTipDialog

class TextProperties(canvasViews: CanvasViews){
    private var toolTextColorBtn = canvasViews.toolTextLayout.findViewById<ImageButton>(R.id.property_text_color_btn)
    private var toolTextColorEditor = canvasViews.toolTextLayout.findViewById<LinearLayout>(R.id.property_text_color_editor)
    private var toolTextSizeBtn = canvasViews.toolTextLayout.findViewById<TextView>(R.id.property_text_size_btn)
    private var toolTextSizeEditor = canvasViews.toolTextLayout.findViewById<LinearLayout>(R.id.property_text_size_editor)
    private var toolTextFontBtn = canvasViews.toolTextLayout.findViewById<TextView>(R.id.property_text_font_btn)
    private var toolTextFontEditor = canvasViews.toolTextLayout.findViewById<HorizontalScrollView>(R.id.property_text_font_editor)
    private var toolTextBoldBtn = canvasViews.toolTextLayout.findViewById<ImageButton>(R.id.property_text_bold_btn)
    private var toolTextItalicsBtn = canvasViews.toolTextLayout.findViewById<ImageButton>(R.id.property_text_italics_btn)
    private var editorTextEditText = canvasViews.toolTextLayout.findViewById<EditText>(R.id.property_text_text_editor) //[0,100]
    private var editorSizeSeekbar = toolTextSizeEditor.findViewById<SeekBar>(R.id.size_editor_seekbar) //[0,100] property_text_editor
    private var editorFont1 = canvasViews.toolTextLayout.findViewById<TextView>(R.id.font_1)
    private var editorFont2 = canvasViews.toolTextLayout.findViewById<TextView>(R.id.font_2)
    private var editorFont3 = canvasViews.toolTextLayout.findViewById<TextView>(R.id.font_3)
    private var editorFont4 = canvasViews.toolTextLayout.findViewById<TextView>(R.id.font_4)
    private var editorFont5 = canvasViews.toolTextLayout.findViewById<TextView>(R.id.font_5)
    private var editorFont6 = canvasViews.toolTextLayout.findViewById<TextView>(R.id.font_6)
    private var editorFont7 = canvasViews.toolTextLayout.findViewById<TextView>(R.id.font_7)
    private var editorFont8 = canvasViews.toolTextLayout.findViewById<TextView>(R.id.font_8)

    init {
        ColorEditorHelper(canvasViews.owner, canvasViews.canvasViewModel, toolTextColorEditor){ hideAllEditors() }
        if(canvasViews.canvasViewModel.textPaint.typeface==null)
            canvasViews.canvasViewModel.textPaint.typeface = ResourcesCompat.getFont(canvasViews.toolTextLayout.context, R.font.roboto_regular)
        toolTextSizeBtn.text = canvasViews.canvasViewModel.textPaint.textSize.toInt().toString()
        toolTextFontBtn.movementMethod = ScrollingMovementMethod() //In case text-width > maxWidth
        if(canvasViews.canvasViewModel.textPaint.typeface.isBold) toolTextBoldBtn.setImageResource(R.drawable.bold_outlined)
        else toolTextBoldBtn.setImageResource(R.drawable.not_bold_outlined)
        if(canvasViews.canvasViewModel.textPaint.typeface.isItalic) toolTextItalicsBtn.setImageResource(R.drawable.italic_outlined)
        else toolTextItalicsBtn.setImageResource(R.drawable.not_italic_outlined)
        editorTextEditText.setText(canvasViews.canvasViewModel.textToolText)

        toolTextColorBtn.setOnClickListener {
            hideAllEditors()
            toolTextColorEditor.visibility = View.VISIBLE
            doWhenTheViewIsVisible(toolTextColorEditor){
                canvasViews.canvasViewModel.newColorHue = ColorValues.colorOnlyHue(canvasViews.canvasViewModel.getColor())
                canvasViews.canvasViewModel.setTemporaryColor(canvasViews.canvasViewModel.getColor())
            }
        }
        toolTextSizeBtn.setOnClickListener {
            hideAllEditors()
            toolTextSizeEditor.visibility = View.VISIBLE
            toolTextSizeBtn.text = canvasViews.canvasViewModel.textPaint.textSize.toInt().toString()
            editorSizeSeekbar.progress = canvasViews.canvasViewModel.textPaint.textSize.toInt()
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
                canvasViews.canvasViewModel.textPaint.typeface.isBold -> {
                    if(canvasViews.canvasViewModel.textPaint.typeface.isItalic)
                        canvasViews.canvasViewModel.textPaint.typeface = Typeface.create(canvasViews.canvasViewModel.textPaint.typeface, Typeface.ITALIC)
                    else
                        canvasViews.canvasViewModel.textPaint.typeface = Typeface.create(canvasViews.canvasViewModel.textPaint.typeface, Typeface.NORMAL)
                    toolTextBoldBtn.setImageResource(R.drawable.not_bold_outlined)
                }
                canvasViews.canvasViewModel.textPaint.typeface.isItalic -> {
                    canvasViews.canvasViewModel.textPaint.typeface = Typeface.create(canvasViews.canvasViewModel.textPaint.typeface, Typeface.BOLD_ITALIC)
                    toolTextBoldBtn.setImageResource(R.drawable.bold_outlined)
                }
                else -> {
                    canvasViews.canvasViewModel.textPaint.typeface = Typeface.create(canvasViews.canvasViewModel.textPaint.typeface, Typeface.BOLD)
                    toolTextBoldBtn.setImageResource(R.drawable.bold_outlined)
                }
            }
        }
        toolTextItalicsBtn.setOnClickListener {
            when {
                canvasViews.canvasViewModel.textPaint.typeface.isItalic -> {
                    if(canvasViews.canvasViewModel.textPaint.typeface.isBold)
                        canvasViews.canvasViewModel.textPaint.typeface = Typeface.create(canvasViews.canvasViewModel.textPaint.typeface, Typeface.BOLD)
                    else
                        canvasViews.canvasViewModel.textPaint.typeface = Typeface.create(canvasViews.canvasViewModel.textPaint.typeface, Typeface.NORMAL)
                    toolTextItalicsBtn.setImageResource(R.drawable.not_italic_outlined)
                }
                canvasViews.canvasViewModel.textPaint.typeface.isBold -> {
                    canvasViews.canvasViewModel.textPaint.typeface = Typeface.create(canvasViews.canvasViewModel.textPaint.typeface, Typeface.BOLD_ITALIC)
                    toolTextItalicsBtn.setImageResource(R.drawable.italic_outlined)
                }
                else -> {
                    canvasViews.canvasViewModel.textPaint.typeface = Typeface.create(canvasViews.canvasViewModel.textPaint.typeface, Typeface.ITALIC)
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
                canvasViews.canvasViewModel.textPaint.textSize = progress.toFloat()
            }
        })

        ////////////////////////////////////////TEXT EDITOR/////////////////////////////////////////
        editorTextEditText.addTextChangedListener {
            canvasViews.canvasViewModel.textToolText = it.toString()
        }

        ////////////////////////////////////////FONT EDITOR/////////////////////////////////////////
        setFontButtonListener(canvasViews, editorFont1)
        setFontButtonListener(canvasViews,editorFont2)
        setFontButtonListener(canvasViews,editorFont3)
        setFontButtonListener(canvasViews,editorFont4)
        setFontButtonListener(canvasViews,editorFont5)
        setFontButtonListener(canvasViews,editorFont6)
        setFontButtonListener(canvasViews,editorFont7)
        setFontButtonListener(canvasViews,editorFont8)

        setTipDialogs()
    }

    private fun setTipDialogs() {
        ShowTipDialog(toolTextColorBtn, R.drawable.color_palette_outlined)
        ShowTipDialog(toolTextSizeBtn, R.drawable.text_outlined)
        ShowTipDialog(toolTextFontBtn, R.drawable.text_outlined)
        ShowTipDialog(toolTextBoldBtn, R.drawable.bold_outlined)
        ShowTipDialog(toolTextItalicsBtn, R.drawable.italic_outlined)
    }

    private fun setFontButtonListener(canvasViews: CanvasViews, fontBtn: TextView){
        fontBtn.setOnClickListener {
            canvasViews.canvasViewModel.textPaint.typeface = Typeface.create(fontBtn.typeface,
                if(canvasViews.canvasViewModel.textPaint.typeface.isBold && canvasViews.canvasViewModel.textPaint.typeface.isItalic) Typeface.BOLD_ITALIC
                else if (canvasViews.canvasViewModel.textPaint.typeface.isBold ) Typeface.BOLD
                else if (canvasViews.canvasViewModel.textPaint.typeface.isItalic ) Typeface.ITALIC
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