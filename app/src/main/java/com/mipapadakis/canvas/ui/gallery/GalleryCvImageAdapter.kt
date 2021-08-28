package com.mipapadakis.canvas.ui.gallery

import android.annotation.SuppressLint
import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.model.CvImage
import com.mipapadakis.canvas.tools.CvFileHelper
import android.content.Intent
import androidx.core.content.ContextCompat
import com.mipapadakis.canvas.CanvasActivity
import com.mipapadakis.canvas.CanvasActivityData
import com.mipapadakis.canvas.ui.create_canvas.CreateCanvasFragment

import androidx.core.content.FileProvider
import java.io.File


@SuppressLint("NotifyDataSetChanged")
class GalleryCvImageAdapter(liveDataToObserve: LiveData<ArrayList<CvImage>>, lifecycleOwner: LifecycleOwner): RecyclerView.Adapter<ItemViewHolder>() {
    private lateinit var imageList: ArrayList<CvImage>

    init {
        liveDataToObserve.observe(lifecycleOwner){
            imageList = it
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate( R.layout.card_gallery, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val cvImage: CvImage = imageList[position]
        val context = holder.context ?: return

        holder.image.setImageBitmap(cvImage.getTotalImage(false))
        holder.titleTV.text = cvImage.getFilenameWithExtension(context)
        holder.button1.setOnClickListener{
            AlertDialog.Builder(context)
                .setTitle("File Information")
                .setMessage(CvFileHelper(context).getInfo(cvImage.getFilenameWithExtension(context)))
                .setNegativeButton("Close", null)
                .setIcon(ContextCompat.getDrawable(context, R.drawable.info_black_36))
                .setCancelable(true)
                .show()
        }
        holder.button2.setOnClickListener { //Share cvImage
            when (cvImage.fileType) {
                CanvasActivityData.FILETYPE_CANVAS -> { // Share as Canvas
                    val requestFile = File( CvFileHelper.getFilesDirPath( context, cvImage.getFilenameWithExtension(context)))
                    val uri = FileProvider.getUriForFile( context, "com.mipapadakis.canvas.fileprovider", requestFile)
                    val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        type = "*/*"
                        putExtra(Intent.EXTRA_STREAM, uri)
                    }
                    context.startActivity(Intent.createChooser(sharingIntent, "Share canvas file using"))
                }
                CanvasActivityData.FILETYPE_JPEG -> { // Share as Jpeg
                    val requestFile = File( CvFileHelper.getFilesDirPath( context, cvImage.getFilenameWithExtension(context)))
                    val uri = FileProvider.getUriForFile( context, "com.mipapadakis.canvas.fileprovider", requestFile)
                    val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        type = "image/jpeg"
                        putExtra(Intent.EXTRA_STREAM, uri)
                    }
                    context.startActivity(Intent.createChooser(sharingIntent, "Share jpeg using"))
                }
                else -> { //share as PNG
                    val requestFile = File( CvFileHelper.getFilesDirPath( context, cvImage.getFilenameWithExtension(context)))
                    val uri = FileProvider.getUriForFile( context, "com.mipapadakis.canvas.fileprovider", requestFile)
                    val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                    }
                    context.startActivity(Intent.createChooser(sharingIntent, "Share png using"))
                }
            }
        }
        holder.button3.setOnClickListener {
            val fileHelper = CvFileHelper(context)
            fileHelper.deleteCvImage(cvImage.getFilenameWithExtension(context))
            imageList.removeAt(position)
            notifyDataSetChanged()
        }
        holder.image.setOnClickListener {
            //TODO fix problem: ripple effect is cancelled (because of this clickListener override)
            CanvasActivityData.cvImage = CvImage(cvImage)
            launchCanvasActivity(context)
        }
    }

    private fun launchCanvasActivity(context: Context){
        val intent = Intent(context, CanvasActivity::class.java)
        intent.putExtra(CreateCanvasFragment.IMPORT_CV_IMAGE_INTENT_KEY, "CanvasViewModel.cvImage contains the required cvImage.")
        context.startActivity(intent)
    }

    override fun getItemCount() = imageList.size

}

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    private var outerCardView: CardView
    val context: Context? = itemView.context
    var image: ImageView
    var titleTV: TextView
    var button1: ImageButton
    var button2: ImageButton
    var button3: ImageButton

    init {
        super.itemView
        outerCardView = itemView.findViewById(R.id.card)
        image = itemView.findViewById(R.id.gallery_card_icon)
        titleTV = itemView.findViewById(R.id.gallery_card_title)
        titleTV.movementMethod = ScrollingMovementMethod() //In case text-width > maxWidth
        button1 = itemView.findViewById(R.id.gallery_card_button_1)
        button2 = itemView.findViewById(R.id.gallery_card_button_2)
        button3 = itemView.findViewById(R.id.gallery_card_button_3)
    }
}