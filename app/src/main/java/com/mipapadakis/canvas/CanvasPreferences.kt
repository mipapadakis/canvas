package com.mipapadakis.canvas

import android.content.Context
import android.content.SharedPreferences
import com.mipapadakis.canvas.ui.CanvasColor

const val TMP_PREFERENCES_KEY = "TEMP_PREFERENCES"

/** Temporarily store here the user's settings of the current project.
 * TODO: When user chooses to save this project, save these preferences inside its .cv file?
 * */
class CanvasPreferences {

    companion object{
        /** Create shared-preferences for this project. */
        fun initializeProjectPreferences(context: Context){
            val sharedPreferences = context.getSharedPreferences(TMP_PREFERENCES_KEY, Context.MODE_PRIVATE)
            val preferencesEditor = sharedPreferences.edit()

            if(sharedPreferences.getBoolean("firstTimeProjectModified",true)){
                //Create default preferences for this project:
                putDefaultPreferences(preferencesEditor)
                preferencesEditor.putBoolean("firstTimeProjectModified", false) //Next times the app runs, "firstTimeProjectModified" will be set to false.
                preferencesEditor.apply()
            }
        }

        private fun putDefaultPreferences(preferencesEditor: SharedPreferences.Editor) {
            preferencesEditor.putInt(BRUSH_COLOR_KEY, BRUSH_COLOR_DEFAULT)
            preferencesEditor.putInt(BRUSH_SIZE_KEY, BRUSH_SIZE_DEFAULT)
        }

        fun restoreDefaultSettings(context: Context) {
            val sharedPreferences = context.getSharedPreferences(TMP_PREFERENCES_KEY, Context.MODE_PRIVATE)
            val preferencesEditor = sharedPreferences.edit()
            putDefaultPreferences(preferencesEditor)
        }
    }
}

//Settings (Default values, Keys):

//////////////////////////////////BRUSH_COLOR//////////////////////////////////
private const val BRUSH_COLOR_DEFAULT = R.color.black
private const val BRUSH_COLOR_KEY = "brush_color"

//////////////////////////////////BRUSH_SIZE//////////////////////////////////
private const val BRUSH_SIZE_DEFAULT = CanvasViewModel.SIZE_NORMAL
private const val BRUSH_SIZE_KEY = "brush_size"