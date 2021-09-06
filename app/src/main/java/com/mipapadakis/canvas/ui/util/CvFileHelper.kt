package com.mipapadakis.canvas.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.model.CvImage
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory
import android.net.Uri

import android.util.Log
import kotlin.collections.ArrayList

import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.model.layer.CvLayer
import kotlin.math.roundToInt

class CvFileHelper(val context: Context) {
    private val resources = context.resources
    private val cacheDir = context.cacheDir
    private var toast = Toast(context)

    private fun saveCvImageAsCanvasFile(cvImage: CvImage, fileName: String){
        try {
            val fos: FileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            val os = ObjectOutputStream(fos)
            os.writeObject(cvImage.toSerializable())
            os.close()
            fos.close()
        }
        catch (e: Exception){
            e.printStackTrace()
            showToast("Error")
        }
    }
    private fun saveCvImageAsCanvasFile(cvImage: CvImage) = saveCvImageAsCanvasFile(cvImage, cvImage.getFilenameWithExtension(context))
    private fun loadCvImageFromCanvasFile(fileName: String?): CvImage?{
        var cvImageInput: CvImage? = null
        try {
            val fis: FileInputStream = context.openFileInput(fileName)
            val ois = ObjectInputStream(fis)
            cvImageInput = (ois.readObject() as CvImage.SerializableCvImage).deserialize()
            ois.close()
            fis.close()
        }
        catch (e: Exception){
            e.printStackTrace()
            showToast("Error")
        }
        return cvImageInput
    }

    private fun saveCvImageAsPngFile(cvImage: CvImage, fileName: String){
        try {
            val fos: FileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            if(!cvImage.getTotalImage(false).compress(Bitmap.CompressFormat.PNG, 100, fos))
                Log.v("CanvasDebug", "couldnt compress and write to the FileOutputStream")
            else Log.v("CanvasDebug", "Successfully compressed and wrote to the FileOutputStream")
            fos.close()
        } catch(e: Exception){
            e.printStackTrace()
            showToast("Error")
        }
    }
    private fun saveCvImageAsPngFile(cvImage: CvImage) = saveCvImageAsPngFile(cvImage, cvImage.getFilenameWithExtension(context))
    private fun loadCvImageFromPngFile(fileName: String?): CvImage?{
        var deserializedCvImage: CvImage? = null
        val byteArray: ByteArray
        try {
            val fis: FileInputStream = context.openFileInput(fileName)
            byteArray = ByteArray(fis.available())
            fis.read(byteArray)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                .copy(Bitmap.Config.ARGB_8888, true)
            deserializedCvImage = CvImage(resources, File(fileName!!).nameWithoutExtension, bitmap)
            deserializedCvImage.fileType = CanvasViewModel.FILETYPE_PNG
            fis.close()

        } catch(e: Exception){
            e.printStackTrace()
            showToast("Error")
        }
        return deserializedCvImage
    }

    private fun saveCvImageAsJpegFile(cvImage: CvImage, fileName: String){
        try {
            val fos: FileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            cvImage.getTotalImage(false).compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
        } catch(e: Exception){
            e.printStackTrace()
            showToast("Error")
        }
    }
    private fun saveCvImageAsJpegFile(cvImage: CvImage) = saveCvImageAsJpegFile(cvImage, cvImage.getFilenameWithExtension(context))
    private fun loadCvImageFromJpegFile(fileName: String?): CvImage?{
        var deserializedCvImage: CvImage? = null
        val byteArray: ByteArray
        try {
            val fis: FileInputStream = context.openFileInput(fileName)
            byteArray = ByteArray(fis.available())
            fis.read(byteArray)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                .copy(Bitmap.Config.ARGB_8888, true)
            deserializedCvImage = CvImage(resources, File(fileName!!).nameWithoutExtension, bitmap)
            deserializedCvImage.fileType = CanvasViewModel.FILETYPE_JPEG
            fis.close()

        } catch(e: Exception){
            e.printStackTrace()
            showToast("Error")
        }
        return deserializedCvImage
    }

    fun saveCvImage(cvImage: CvImage): Boolean{
        when (cvImage.fileType) {
            CanvasViewModel.FILETYPE_CANVAS -> saveCvImageAsCanvasFile(cvImage)
            CanvasViewModel.FILETYPE_PNG -> saveCvImageAsPngFile(cvImage)
            CanvasViewModel.FILETYPE_JPEG -> saveCvImageAsJpegFile(cvImage)
            else -> return false
        }
        return true
    }
    fun loadCvImage(fileName: String?): CvImage?{
        var cvImageInput: CvImage? = null
        when (getFileTypeFromFileName(fileName?:"")) {
            CanvasViewModel.FILETYPE_CANVAS -> cvImageInput = loadCvImageFromCanvasFile(fileName)
            CanvasViewModel.FILETYPE_PNG -> cvImageInput = loadCvImageFromPngFile(fileName)
            CanvasViewModel.FILETYPE_JPEG -> cvImageInput = loadCvImageFromJpegFile(fileName)
        }
        return cvImageInput
    }
    fun loadExternalCvImage(uri: Uri?): CvImage?{
        if(uri==null) return null
        var cvImage: CvImage? = null
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val ois = ObjectInputStream(inputStream)
            cvImage = (ois.readObject() as CvImage.SerializableCvImage).deserialize()
            ois.close()
            inputStream?.close()
        }
        catch (e: Exception){ //Try png/jpeg
            e.printStackTrace()
            val byteArray: ByteArray
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                byteArray = ByteArray(inputStream!!.available())
                inputStream.read(byteArray)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    .copy(Bitmap.Config.ARGB_8888, true)
                cvImage = CvImage(resources, "", bitmap)
                cvImage.fileType = CanvasViewModel.FILETYPE_PNG
                inputStream.close()

            } catch(e: Exception){
                e.printStackTrace()
                showToast("Error")
            }
        }
        return cvImage
    }
    fun loadAndSetCvImage(cvImage: CvImage, fileName: String?){
        val deserializedCvImage = loadCvImage(fileName)
        if(deserializedCvImage!=null) cvImage.setCvImage(deserializedCvImage)
    }

    fun deleteCvImage(fileName: String){
        val file = File(fileName.withPath()).absoluteFile
        if (file.exists()) {
            if (file.delete()) showToast("File deleted") else showToast("File couldn't be deleted")
        }
        else Log.v("CanvasInfo", "File \"$fileName\" does not exist!")
    }
    fun getInfo(fileName: String): String{
        val sb = StringBuffer("")
        val file = File(fileName.withPath()).absoluteFile
        if(!file.exists()) return sb.append("Error retrieving file info!").toString()
        sb.append("• Name: ${file.name}")
        sb.append("\n\n• File Type: ${if(file.extension=="cv") "Canvas" else if(file.extension=="png") "PNG" else "JPEG"}")
        sb.append("\n\n• Path:\n${file.canonicalPath}")
        sb.append("\n\n• Modified: ${getFormattedDateAndTime(file.lastModified())}")
        sb.append("\n\n• Size: ${getFormattedFileSize(file.length())}")
        return sb.toString()
    }

    fun getAllCvImages(): ArrayList<CvImage> {
        val cvImageList = ArrayList<CvImage>()
        val fileNames = ArrayList<String>()
        try{ //try to order the files based on time of last modification
            val files = ArrayList<File>()
            for(fn in context.fileList()) files.add(File(fn.withPath()))
            files.sortBy { it.lastModified() }
            for(f in files) fileNames.add(0, f.name) //Place the most recent first
        }
        catch (e: Exception){
            for(fn in context.fileList()) fileNames.add(fn)
        }
        var loadedImage: CvImage?
        for(f in fileNames){
            loadedImage = loadCvImage(f)
            if(loadedImage!=null) cvImageList.add(loadedImage)
        }
        return cvImageList
    }

    fun fileNameAlreadyExists(name: String): Boolean{
        val files = context.fileList()
        if(files.isEmpty()) return false
        for(f in files) if(name == f) return true
        return false
    }

    private fun getFileTypeFromFileName(fileName: String): Int{
        val extension = File(fileName).extension
        if(extension==context.getString(R.string.file_extension_canvas))
            return CanvasViewModel.FILETYPE_CANVAS
        if(extension==context.getString(R.string.file_extension_png))
            return CanvasViewModel.FILETYPE_PNG
        if(extension==context.getString(R.string.file_extension_jpeg))
            return CanvasViewModel.FILETYPE_JPEG
        return -1
    }

    private fun String?.withPath(): String{
        return context.filesDir.path+"/"+this
    }

    @Suppress("DEPRECATION")
    private fun getFormattedDateAndTime(milliseconds: Long?): String{ //If milliseconds==null, return current date
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            resources.configuration.locales.get(0)
        else resources.configuration.locale
        val sdf = SimpleDateFormat("dd/MM/yyyy, HH:mm", locale)
        if(milliseconds==null) return sdf.format(Date())
        return sdf.format(Date(milliseconds))
    }

    private fun getFormattedFileSize(bytes: Long): String{
        val kiloBytes = bytes/1024.toDouble()
        val megaBytes = kiloBytes/1024
        val gigaBytes = megaBytes/1024
        if(gigaBytes>1) return "${(gigaBytes * 100).roundToInt()/100} GB"
        if(megaBytes>1) return "${(megaBytes * 100).roundToInt()/100} MB"
        if(kiloBytes>1) return "${(kiloBytes * 100).roundToInt()/100} KB"
        return "$bytes B"
    }

    @Suppress("SameParameterValue")
    private fun showToast(text: String){
        if(text.length>30) toast.duration = Toast.LENGTH_LONG
        else toast.duration = Toast.LENGTH_SHORT
        toast.cancel()
        toast = Toast.makeText(context, text, toast.duration)
        toast.show()
    }

    companion object{
        fun getFilesDirPath(context: Context) = context.filesDir.path+"/"
        fun getFilesDirPath(context: Context, fileNameWithExtension: String?) =
            context.filesDir.path+"/"+fileNameWithExtension
    }
}

















