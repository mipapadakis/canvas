package com.mipapadakis.canvas.ui.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.toIcon
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.model.CvImage
import java.util.*

class CvImageAdapter(liveDataToObserve: LiveData<List<CvImage>>, lifecycleOwner: LifecycleOwner): RecyclerView.Adapter<ItemViewHolder>() {
    private lateinit var imageList: List<CvImage>

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
        holder.image.setImageBitmap(cvImage.layers[0].bitmap) //TODO
        holder.titleTV.text = cvImage.title
        holder.button1.text = "Button 1" //TODO
        holder.button2.text = "Button 2" //TODO
    }

    override fun getItemCount() = imageList.size
}

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    private var outerCardView: CardView
    var image: ImageView
    var titleTV: TextView
    var button1: MaterialButton
    var button2: MaterialButton

    init {
        super.itemView
        outerCardView = itemView.findViewById(R.id.card)
        image = itemView.findViewById(R.id.image_view)
        titleTV = itemView.findViewById(R.id.title)
        button1 = itemView.findViewById(R.id.button_1)
        button2 = itemView.findViewById(R.id.button_2)
    }
}