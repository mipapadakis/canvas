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
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.ui.create_canvas.CreateCanvasFragment

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
        val itemView: View = LayoutInflater.from(parent.context).inflate( R.layout.gallery_card_view, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val cvImage: CvImage = imageList[position]
        holder.image.setImageBitmap(cvImage.getTotalImage(false))
        holder.titleTV.text = cvImage.getFilenameWithExtension(holder.titleTV.context)
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
            notifyDataSetChanged()
        }
        holder.image.setOnClickListener {
            //TODO fix problem: ripple effect is cancelled (because of this clickListener override)
            CanvasViewModel.cvImage = CvImage(cvImage)
            launchCanvasActivity(holder.image.context)
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