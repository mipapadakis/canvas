package com.mipapadakis.canvas.ui.toolbar.menu

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.mipapadakis.canvas.CanvasPreferences
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.model.CvImage
import com.mipapadakis.canvas.model.layer.CvLayer
import com.mipapadakis.canvas.ui.CanvasImageView

class LayerListAdapter(val canvasIV: CanvasImageView, val resources: Resources): RecyclerView.Adapter<LayerListAdapter.ItemViewHolder>() {
    private var mRecyclerView: RecyclerView? = null
    private lateinit var toast: Toast

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mRecyclerView = recyclerView
        toast = Toast(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate( R.layout.layer_card_view, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val layer = CanvasViewModel.cvImage.value?.get(position) ?: return
        if(position == itemCount - 1) {
            holder.hide()
            return
        }
        holder.show()
        holder.layerImage.setImageBitmap(layer.bitmap)
        holder.deselect()
        holder.layerMenuButton
        holder.layerVisibilityButton
        holder.layerImage.setOnClickListener {
            if(holder.isSelected) holder.deselect() else holder.select()
        }
        holder.layerVisibilityButton.setOnClickListener {
            layer.visible = !layer.visible
            if(layer.isVisible()) holder.layerVisibilityButton.setImageResource(R.drawable.baseline_visibility_black_18)
            else holder.layerVisibilityButton.setImageResource(R.drawable.baseline_visibility_off_black_18)
        }
        addPopMenuLayerOptions(layer, holder.layerMenuButton)
        /*TODO
           Option 1: clicking on a layer sets it as "selected" (NOT bringing it to front). You can
           select multiple layers, and use the menu of one of them to "Merge". As for the  drag &
           reorder, long pressing a layer and dragging it to the start of the list will bring that
           layer to the front.
           Option 2: drag & reorder only using the *holder.layerReorderButton*. Long press on item will
           activate the multiple-select mode, for handling groups of layers. Also, while
           multiple-select mode is on, the "property_layers_check_all" button will appear.
        */
    }

    override fun getItemCount() = CanvasViewModel.cvImage.value?.size ?: 0

    private fun addPopMenuLayerOptions(layer: CvLayer, btn: ImageButton){
        btn.setOnClickListener {
            val paletteMenu = PopupMenu(btn.context, btn)
            paletteMenu.menuInflater.inflate(R.menu.layer_options, paletteMenu.menu)
            paletteMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.layer_option_to_front -> {
                        CanvasViewModel.cvImage.value?.setTopLayer(layer)
                        canvasIV.setForegroundLayer(0)
                        notifyDataSetChanged()
                    }
                    R.id.layer_option_merge -> {
                        val selectedLayers = getSelectedLayers()
                        if(selectedLayers.size<2) showToast("Select at least two layers to merge.")
                        else{
                            /*TODO merge layers (according to their position in the list),
                               notifyDataSetChanged, if any of them is at position 0 then canvasIV.setForegroundLayer(0)*/
                        }
                    }
                    R.id.layer_option_duplicate -> {
                        //TODO show message if more than one selected. Create duplicates of ALL selected layers
                    }
                    R.id.layer_option_opacity -> {
                        //TODO show numberPicker dialog, then set opacity to ALL selected layers
                    }
                    R.id.layer_option_clear -> {
                        //TODO set layer's bitmaps to transparent
                    }
                    R.id.layer_option_delete -> {
                        /*TODO delete layers. If any of them is at foreground, make foreground the
                           first non-selected layer. If that doesn't exist, create a new empty layer.*/
                    }
                    else -> {}
                }
                true
            }
            paletteMenu.show()
        }
    }

    private fun getViewHolder(position: Int): ItemViewHolder?{
        return mRecyclerView?.findViewHolderForAdapterPosition(position) as ItemViewHolder?
    }

    fun getSelectedLayers(): ArrayList<CvLayer>{
        return ArrayList() //TODO
    }

    private fun deselectAll(){
        val layers = CanvasViewModel.cvImage.value
        if(layers!=null) for(i in layers.indices) getViewHolder(i)?.deselect()
    }

    private fun Bitmap.withPngGrid(): Bitmap{
        val bmp = CvImage.createBackgroundBitmap(this.width, this.height, resources)
        val canvas = Canvas(bmp)
        canvas.drawBitmap(this, 0f, 0f, null)
        return bmp
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ItemTouchHelperViewHolder {
        var outerCardView: CardView
        //var innerCardView: CardView
        var layerImage: ImageView
        var layerVisibilityButton: ImageButton
        var layerMenuButton: ImageButton
        var layerReorderButton: ImageButton
        var isSelected = false

        init {
            super.itemView
            outerCardView = itemView.findViewById(R.id.layer_outer_card)
            //innerCardView = itemView.findViewById(R.id.layer_inner_card)
            layerImage = itemView.findViewById(R.id.layer_image)
            layerVisibilityButton = itemView.findViewById(R.id.layer_visibility)
            layerReorderButton = itemView.findViewById(R.id.layer_reorder)
            layerMenuButton = itemView.findViewById(R.id.layer_menu)
        }

        fun hide(){
            itemView.visibility = View.GONE
            itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
        }
        fun show(){
            itemView.visibility = View.VISIBLE
            itemView.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        fun select(){
            isSelected = true
            outerCardView.setCardBackgroundColor(Color.WHITE)
            layerMenuButton.visibility = View.VISIBLE
            layerVisibilityButton.visibility = View.VISIBLE
        }

        fun deselect(){
            isSelected = false
            outerCardView.setCardBackgroundColor(Color.TRANSPARENT)
            layerMenuButton.visibility = View.GONE
            layerVisibilityButton.visibility = View.GONE
        }

        override fun onItemSelected() { outerCardView.alpha = CanvasPreferences.MEDIUM_ALPHA }
        override fun onItemDropped() {
            outerCardView.alpha = CanvasPreferences.FULL_ALPHA
            canvasIV.setForegroundLayer(0) //In case the layer was dropped at the start of the list.
        }
    }

    fun onItemMove(fromPosition: Int?, toPosition: Int?): Boolean {
        fromPosition?.let {
            toPosition?.let {
                if (fromPosition < toPosition) {
                    for (i in fromPosition until toPosition) swapItems(i, i + 1)
                } else {
                    for (i in fromPosition downTo toPosition+1) swapItems(i, i - 1)
                }
                notifyItemMoved(fromPosition, toPosition)
                return true
            }
        }
        return false
    }

    private fun swapItems(fromPosition: Int, toPosition: Int){
        CanvasViewModel.cvImage.value?.swapLayers(fromPosition, toPosition)
        //TODO update canvas layers
    }

    interface ItemTouchHelperViewHolder {
        fun onItemSelected()
        fun onItemDropped()
    }

    @Suppress("SameParameterValue")
    private fun showToast(text: String){
        if(text.length>30) toast.duration = Toast.LENGTH_LONG
        else toast.duration = Toast.LENGTH_SHORT
        toast.cancel()
        val context = mRecyclerView?.context ?: return
        toast = Toast.makeText(context, text, toast.duration)
        toast.show()
    }
}
