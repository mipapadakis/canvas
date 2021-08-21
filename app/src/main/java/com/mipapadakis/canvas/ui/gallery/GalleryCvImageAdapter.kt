package com.mipapadakis.canvas.ui.gallery

import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.InputFilter
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.model.CvImage
import com.mipapadakis.canvas.tools.CvFileHelper
import android.content.DialogInterface
import androidx.core.content.ContextCompat


class GalleryCvImageAdapter(liveDataToObserve: LiveData<ArrayList<CvImage>>, lifecycleOwner: LifecycleOwner): RecyclerView.Adapter<ItemViewHolder>() {
    private lateinit var imageList: ArrayList<CvImage>

    init {
        liveDataToObserve.observe(lifecycleOwner){
            imageList = it
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate( R.layout.gallery_card_view, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val cvImage: CvImage = imageList[position]
        holder.image.setImageBitmap(cvImage.getTotalImage(false))
        holder.titleTV.text = cvImage.title
        holder.button1.setOnClickListener{
            AlertDialog.Builder(holder.button1.context)
                .setTitle("File Information")
                .setMessage(CvFileHelper(holder.button1.context).getInfo(cvImage.getFilenameWithExtension(holder.button1.context)))
                .setNegativeButton("Close", null)
                .setIcon(ContextCompat.getDrawable(holder.button1.context, R.drawable.baseline_info_black_36))
                .setCancelable(true)
                .show()
        }
        holder.button2.setOnClickListener {
            //TODO share as PNG
        }
        holder.button3.setOnClickListener {
            val fileHelper = CvFileHelper(holder.button3.context)
            fileHelper.deleteCvImage(cvImage.getFilenameWithExtension(holder.button3.context))
            imageList.removeAt(position)
            notifyItemRemoved(position)
        }
        holder.image.setOnClickListener {
            //todo intent to canvas
        }
    }

    override fun getItemCount() = imageList.size

}

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    private var outerCardView: CardView
    var image: ImageView
    var titleTV: TextView
    var button1: ImageButton
    var button2: ImageButton
    var button3: ImageButton

    init {
        super.itemView
        outerCardView = itemView.findViewById(R.id.card)
        image = itemView.findViewById(R.id.image_view)
        titleTV = itemView.findViewById(R.id.title)
        titleTV.movementMethod = ScrollingMovementMethod() //In case text-width > maxWidth
        button1 = itemView.findViewById(R.id.button_1)
        button2 = itemView.findViewById(R.id.button_2)
        button3 = itemView.findViewById(R.id.button_3)
    }
}