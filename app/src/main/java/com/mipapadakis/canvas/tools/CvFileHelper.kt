package com.mipapadakis.canvas.tools

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

//TODO
class CvFileHelper(val context: Context) {
    private val resources = context.resources
    private val cacheDir = context.cacheDir
    private var toast = Toast(context)


    private fun createTempCvFile(fileName: String?): DocumentFile {
        //val folder = getDir(currentDate, MODE_PRIVATE) //Create directory
        //https://developer.android.com/training/data-storage/app-specific#internal-create-cache
        //Create temp file in cacheDir
        return if(fileName==null || fileName.length<=3){
            DocumentFile.fromFile(File.createTempFile("Temp file (${getCurrentDateAndTime()})", ".cv", cacheDir))
        } else DocumentFile.fromFile(File.createTempFile(fileName, ".cv", cacheDir)) //Create temp file in cacheDir
        //TODO: If user sets a name for this project, rename this file accordingly.
        // if this is an existing project, don't create a new directory. Rather, must open the project's dir.
    }

    @Suppress("DEPRECATION")
    private fun getCurrentDateAndTime(): String{
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            resources.configuration.locales.get(0)
        else resources.configuration.locale
        val sdf = SimpleDateFormat("dd/M/yyyy, hh:mm", locale)
        return sdf.format(Date())
    }

    @Suppress("SameParameterValue")
    private fun showToast(text: String){
        if(text.length>30) toast.duration = Toast.LENGTH_LONG
        else toast.duration = Toast.LENGTH_SHORT
        toast.cancel()
        toast = Toast.makeText(context, text, toast.duration)
        toast.show()
    }
}