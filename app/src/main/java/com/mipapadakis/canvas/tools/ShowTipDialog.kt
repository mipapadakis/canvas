package com.mipapadakis.canvas.tools

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.mipapadakis.canvas.CanvasPreferences
import com.mipapadakis.canvas.R

private const val CONTENT_DESCRIPTION_SEPARATOR = "/"

/**@param view: when long pressed, this view's tutorial will appear
 * @param iconId: contains the resource ID of an image for the tip dialog
 * */
class ShowTipDialog(view: View, private val iconId: Int){
    private val context = view.context
    private val title = view.contentDescription.split(CONTENT_DESCRIPTION_SEPARATOR)[0]
    private val subtitle = view.contentDescription.split(CONTENT_DESCRIPTION_SEPARATOR)[1]
    private val description = view.contentDescription.split(CONTENT_DESCRIPTION_SEPARATOR)[2]

    init{
        view.setOnLongClickListener {
            showTutorialDialog()
            true
        }
    }

    private fun showTutorialDialog(){
        val dialog = Dialog(context)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_tip)
        dialog.findViewById<ImageView>(R.id.tip_dialog_icon).setImageResource(iconId)
        dialog.findViewById<TextView>(R.id.tip_dialog_title).text = title
        if(subtitle.isBlank())
            dialog.findViewById<TextView>(R.id.tip_dialog_subtitle).visibility = View.GONE
        else dialog.findViewById<TextView>(R.id.tip_dialog_subtitle).text = subtitle
        dialog.findViewById<TextView>(R.id.tip_dialog_description).text = description
        dialog.findViewById<Button>(R.id.tip_dialog_close_button).setOnClickListener{
            dialog.dismiss()
        }
        dialog.show()
    }

    companion object{
        //This dialog will appear only the first time the app runs, letting the user know of some of its basic functions.
        fun showTutorialDialog(context: Context){
            if(CanvasPreferences.hasShownTutorialDialog(context)) return
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog_tip)
            dialog.findViewById<ImageView>(R.id.tip_dialog_icon).setImageResource(R.drawable.logo_outlined)
            dialog.findViewById<TextView>(R.id.tip_dialog_title).text = context.getString(R.string.tutorial_dialog_title)
            dialog.findViewById<TextView>(R.id.tip_dialog_subtitle).text = context.getString(R.string.tutorial_dialog_subtitle)
            dialog.findViewById<TextView>(R.id.tip_dialog_description).text = context.getString(R.string.tutorial_dialog_description)
            dialog.findViewById<TextView>(R.id.tip_dialog_appear_once_textview).visibility=View.VISIBLE
            val closeBtn =  dialog.findViewById<Button>(R.id.tip_dialog_close_button)
            closeBtn.text = context.getString(R.string.thanks)
            closeBtn.setOnClickListener{
                dialog.dismiss()
                CanvasPreferences.setHasShownTutorialDialog(context)
            }
            dialog.show()
        }
        fun showCanvasTipDialog(context: Context){
            if(CanvasPreferences.hasShownCanvasTip(context)) return
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialog_tip)
            dialog.findViewById<ImageView>(R.id.tip_dialog_icon).setImageResource(R.drawable.logo_outlined)
            dialog.findViewById<TextView>(R.id.tip_dialog_title).text = context.getString(R.string.canvas_tip_title)
            dialog.findViewById<TextView>(R.id.tip_dialog_subtitle).text = context.getString(R.string.canvas_tip_subtitle)
            dialog.findViewById<TextView>(R.id.tip_dialog_description).text = context.getString(R.string.canvas_tip_description)
            dialog.findViewById<TextView>(R.id.tip_dialog_appear_once_textview).visibility=View.VISIBLE
            val closeBtn =  dialog.findViewById<Button>(R.id.tip_dialog_close_button)
            closeBtn.text = context.getString(R.string.ok)
            closeBtn.setOnClickListener{
                dialog.dismiss()
            }
            dialog.show()
            CanvasPreferences.setHasShownCanvasTip(context)
        }

    }

}