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
        holder.image.setImageBitmap(cvImage.bitmap)
        holder.titleTV.text = cvImage.title
        holder.button1.text = "Button 1"
        holder.button2.text = "Button 2"
    }

    override fun getItemCount() = imageList.size

//    fun onItemMove(fromPosition: Int?, toPosition: Int?): Boolean {
//        fromPosition?.let {
//            toPosition?.let {
//                if (fromPosition < toPosition) {
//                    for (i in fromPosition until toPosition)
//                        Collections.swap(imageList, i, i + 1)
//                } else {
//                    for (i in fromPosition downTo toPosition+1)
//                        Collections.swap(imageList, i, i - 1)
//                }
//                notifyItemMoved(fromPosition, toPosition)
//                return true
//            }
//        }
//        return false
//    }
}

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)/*, ItemTouchHelperViewHolder*/ {
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

//    override fun onItemSelected() {
//        outerCardView.alpha = 0.5F
//    }
//
//    override fun onItemDropped() {
//        outerCardView.alpha = 1.0F
//        //TODO Update list numbers after item drop?
//    }
}

//interface ItemTouchHelperViewHolder {
//    fun onItemSelected()
//    fun onItemDropped()
//}