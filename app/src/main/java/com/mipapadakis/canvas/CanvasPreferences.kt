package com.mipapadakis.canvas

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.view.View

const val KEY = "canvas_preferences"

/** Store here the user's settings.*/
class CanvasPreferences {
    companion object{
        const val FULL_ALPHA = 1f
        const val MEDIUM_ALPHA = 0.6f
        const val LOW_ALPHA = 0.3f

        var startingColorId = R.color.green
        var startingCanvasColor = Color.WHITE


        /** Create shared-preferences for this project. */
        fun initializeProjectPreferences(context: Context){
            val sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
            val preferencesEditor = sharedPreferences.edit()

            if(sharedPreferences.getBoolean("firstTimeProjectModified",true)){
                //Create default preferences for this project:
                putDefaultPreferences(preferencesEditor)
                preferencesEditor.putBoolean("firstTimeProjectModified", false) //Next times the app runs, "firstTimeProjectModified" will be set to false.
                preferencesEditor.apply()
            }
        }

        fun restoreDefaultSettings(context: Context) {
            val sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
            val preferencesEditor = sharedPreferences.edit()
            putDefaultPreferences(preferencesEditor)
        }

        private fun putDefaultPreferences(preferencesEditor: SharedPreferences.Editor) {
//            preferencesEditor.putInt(BRUSH_COLOR_KEY, BRUSH_COLOR_DEFAULT)
//            preferencesEditor.putFloat(BRUSH_SIZE_KEY, BRUSH_SIZE_DEFAULT)
//            preferencesEditor.putBoolean(SHOW_TUTORIAL_TIPS_KEY, SHOW_TUTORIAL_TIPS_DEFAULT)
        }

        /*/////////////////////////////////BRUSH_COLOR//////////////////////////////////
        fun getBrushColor(context: Context): Int {
            val sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
            return sharedPreferences.getInt(BRUSH_COLOR_KEY, BRUSH_COLOR_DEFAULT)
        }
        fun setBrushColor(context: Context, color: Int){
            val sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
            val preferencesEditor = sharedPreferences.edit()
            preferencesEditor.putInt(BRUSH_COLOR_KEY, color)
            preferencesEditor.apply()
        }

        //////////////////////////////////BRUSH_SIZE//////////////////////////////////
        fun getBrushSize(context: Context): Float {
            val sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
            return sharedPreferences.getFloat(BRUSH_SIZE_KEY, BRUSH_SIZE_DEFAULT)
        }
        fun setBrushSize(context: Context, size: Float){ //TODO in settings
            val sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
            val preferencesEditor = sharedPreferences.edit()
            preferencesEditor.putFloat(BRUSH_SIZE_KEY, size)
            preferencesEditor.apply()
        }

        ////////////////////////////////TUTORIAL_TIPS/////////////////////////////////
        fun getShowTutorialTips(context: Context): Boolean {
            val sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean(SHOW_TUTORIAL_TIPS_KEY, SHOW_TUTORIAL_TIPS_DEFAULT)
        }
        fun setShowTutorialTips(context: Context, show: Boolean){
            val sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
            val preferencesEditor = sharedPreferences.edit()
            preferencesEditor.putBoolean(SHOW_TUTORIAL_TIPS_KEY, show)
            preferencesEditor.apply()
        }*/

        fun hasShownCanvasTip(context: Context): Boolean{
            val sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean(SHOW_CANVAS_TIP_KEY, false)
        }
        fun setHasShownCanvasTip(context: Context){
            val sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
            val preferencesEditor = sharedPreferences.edit()
            preferencesEditor.putBoolean(SHOW_CANVAS_TIP_KEY, true)
            preferencesEditor.apply()
        }

        fun hasShownTutorialDialog(context: Context): Boolean{
            val sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean(SHOW_TUTORIAL_DIALOG_KEY, false)
        }
        fun setHasShownTutorialDialog(context: Context){
            val sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
            val preferencesEditor = sharedPreferences.edit()
            preferencesEditor.putBoolean(SHOW_TUTORIAL_DIALOG_KEY, true)
            preferencesEditor.apply()
        }
    }
}
//                       Settings (Default values, Keys):

////////////////////////////////////BRUSH_COLOR//////////////////////////////////
//private const val BRUSH_COLOR_DEFAULT = R.color.green
//private const val BRUSH_COLOR_KEY = "BRUSH_COLOR_KEY"
//
////////////////////////////////////BRUSH_SIZE//////////////////////////////////
//private const val BRUSH_SIZE_DEFAULT = 20f
//private const val BRUSH_SIZE_KEY = "BRUSH_SIZE_KEY"
//
////////////////////////////////TUTORIAL_TIPS/////////////////////////////////
private const val SHOW_TUTORIAL_DIALOG_KEY = "TUTORIAL_DIALOG"
private const val SHOW_CANVAS_TIP_KEY = "CANVAS_TIP"

