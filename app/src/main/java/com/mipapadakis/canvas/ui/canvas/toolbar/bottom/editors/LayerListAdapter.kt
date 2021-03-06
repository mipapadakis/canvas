package com.mipapadakis.canvas.ui.canvas.toolbar.bottom.editors

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.mipapadakis.canvas.CanvasPreferences
import com.mipapadakis.canvas.CanvasViewModel
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.model.layer.CvLayer
import com.mipapadakis.canvas.ui.util.DoubleTapListener
import com.mipapadakis.canvas.ui.canvas.CanvasImageView


class LayerListAdapter(
    val canvasIV: CanvasImageView,
    val canvasViewModel: CanvasViewModel,
    val resources: Resources
    ): RecyclerView.Adapter<LayerListAdapter.ItemViewHolder>() {
    private var mRecyclerView: RecyclerView? = null
    private lateinit var toast: Toast

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mRecyclerView = recyclerView
        toast = Toast(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate( R.layout.card_layer, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val layer = canvasViewModel.cvImage[position]
        if(position == itemCount - 1) {
            holder.hide()
            return
        }
        holder.show()
        holder.layerImage.setImageBitmap(layer.getBitmapWithOpacity())
        holder.layerTitleTextView.text = layer.title
        if(layer.isSelected()) holder.select() else holder.deselect()
        holder.visibilityIndicator.visibility = if(layer.isVisible()) View.GONE else View.VISIBLE
        holder.layerImage.setOnClickListener(object : DoubleTapListener(){
            override fun onSingleTap() {
                layer.selected = !layer.selected
                if(layer.isSelected()) holder.select() else holder.deselect()
            }
            override fun onDoubleTap() {
                canvasViewModel.cvImage.setTopLayer(layer)
                deselectAll()
                canvasIV.invalidateLayers()
                canvasIV.addActionToHistory(CanvasImageView.ACTION_LAYER_MOVE)
            }
        })
        holder.layerVisibilityButton.setOnClickListener {
            layer.visible = !layer.isVisible()
            if(layer.isVisible()) holder.visibilityIndicator.visibility = View.GONE
            else holder.visibilityIndicator.visibility = View.VISIBLE
            canvasIV.invalidateLayers()
        }
        addPopMenuLayerOptions(layer, holder.layerMenuButton)
    }

    override fun getItemCount() = canvasViewModel.cvImage.size

    private fun addPopMenuLayerOptions(layer: CvLayer, btn: ImageButton){
        btn.setOnClickListener {
            val paletteMenu = PopupMenu(btn.context, btn)
            paletteMenu.menuInflater.inflate(R.menu.layer_options, paletteMenu.menu)
            paletteMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.layer_option_to_front -> {
                        canvasViewModel.cvImage.setTopLayer(layer)
                        canvasIV.invalidateLayers()
                        canvasIV.addActionToHistory(CanvasImageView.ACTION_LAYER_MOVE)
                    }
                    R.id.layer_option_merge -> {
                        val selectedLayers = getSelectedLayers()
                        if(selectedLayers.size<2) showToast("Select at least two layers to merge.")
                        else{ // merge layers (according to their position in the list):
                            val bmp = Bitmap.createBitmap(selectedLayers[0].width, selectedLayers[0].height, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(bmp)
                            //Find index of first selected layer of the cvImage.
                            val firstSelectedLayerIndex = canvasViewModel.cvImage.indexOf(selectedLayers[0])
                            //Remove selected layers from the cvImage
                            canvasViewModel.cvImage.removeAll(selectedLayers)
                            //Create merged Bitmap:
                            for(i in selectedLayers.size-1 downTo 0)
                                canvas.drawBitmap(selectedLayers[i].getBitmapWithOpacity(), 0f, 0f, null)
                            //Add a new layer with the merged Bitmap to the position of the first selected layer
                            canvasViewModel.cvImage.addLayer(firstSelectedLayerIndex, bmp)
                            canvasIV.addActionToHistory(CanvasImageView.ACTION_LAYER_MERGE)
                        }
                    }
                    R.id.layer_option_duplicate -> {
                        val selectedLayers = getSelectedLayers()
                        var currentLayer: CvLayer
                        var i = 0
                        //Create duplicates of ALL selected layers
                        while(i < canvasViewModel.cvImage.lastIndex+selectedLayers.lastIndex){
                            currentLayer = canvasViewModel.cvImage[i]
                            if(selectedLayers.contains(currentLayer)){ //create a duplicate after the original
                                canvasViewModel.cvImage.add(i+1, CvLayer(currentLayer.title + " copy", currentLayer))
                                i+=2 //Avoid the duplicate
                            }
                            else i++
                        }
                        deselectAll()
                        canvasIV.addActionToHistory(CanvasImageView.ACTION_LAYER_DUPLICATE)
                    }
                    R.id.layer_option_opacity -> {
                        val selectedLayers = getSelectedLayers()
                        if(selectedLayers.size==1){
                            numberPicker("Layer Opacity", "Choose opacity:",
                                selectedLayers[0].getOpacity()){
                                if(it==selectedLayers[0].getOpacity()) return@numberPicker
                                selectedLayers[0].setOpacity(it)
                                canvasIV.invalidateLayers()
                                canvasIV.addActionToHistory(CanvasImageView.ACTION_LAYER_OPACITY)
                            }
                        }
                        else{
                            numberPicker("Layers Opacity", "Choose opacity (for ALL selected layers):", 255){
                                for(l in selectedLayers) l.setOpacity(it)
                                canvasIV.invalidateLayers()
                                canvasIV.addActionToHistory(CanvasImageView.ACTION_LAYER_OPACITY)
                            }
                        }
                        //TODO debug undo
                    }
                    R.id.layer_option_clear -> {
                        val selectedLayers = getSelectedLayers()
                        for(l in selectedLayers) l.clearCanvas()
                        canvasIV.addActionToHistory(CanvasImageView.ACTION_LAYER_CLEAR)
                    }
                    R.id.layer_option_delete -> {
                        canvasViewModel.cvImage.removeAll(getSelectedLayers())
                        if(canvasViewModel.cvImage.size<2) canvasViewModel.cvImage.newLayer()
                        canvasIV.addActionToHistory(CanvasImageView.ACTION_LAYER_DELETE)
                    }
                    else -> {}
                }
                canvasIV.invalidateLayers()
                true
            }
            paletteMenu.show()
        }
    }

    private fun numberPicker(title: String, message: String, currentValue: Int, positive: (number: Int) -> Unit){
        val numberPicker = NumberPicker(getContext())
        numberPicker.minValue = 0
        numberPicker.maxValue = 255
        numberPicker.value = currentValue

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(getContext())
        builder.setView(numberPicker)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { _, _ ->
            positive(numberPicker.value)
        }
        builder.setNegativeButton( "CANCEL") { _, _ ->}
        builder.create()
        builder.show()
    }

    private fun getViewHolder(position: Int): ItemViewHolder?{
        return mRecyclerView?.findViewHolderForAdapterPosition(position) as ItemViewHolder?
    }

    private fun getSelectedLayers(): ArrayList<CvLayer>{
        val selectedLayers = ArrayList<CvLayer>()
        for(i in 0 until itemCount){
            if(canvasViewModel.cvImage[i].isSelected())
                selectedLayers.add(canvasViewModel.cvImage[i])
        }
        return selectedLayers
    }

    private fun deselectAll(){
        for(i in canvasViewModel.cvImage.indices) {
            canvasViewModel.cvImage[i].selected = false
            getViewHolder(i)?.deselect()
        }
    }

    private fun vibrate(context: Context){
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(70)
        }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        ItemTouchHelperViewHolder {
        var outerCardView: CardView
        //var innerCardView: CardView
        var layerImage: ImageView
        var visibilityIndicator: ImageView
        var layerVisibilityButton: ImageButton
        var layerMenuButton: ImageButton
        var layerTitleTextView: TextView

        init {
            super.itemView
            outerCardView = itemView.findViewById(R.id.layer_card_outer_card)
            //innerCardView = itemView.findViewById(R.id.layer_inner_card)
            layerImage = itemView.findViewById(R.id.layer_card_image)
            visibilityIndicator = itemView.findViewById(R.id.layer_card_visibility_indicator)
            layerVisibilityButton = itemView.findViewById(R.id.layer_card_visibility)
            layerMenuButton = itemView.findViewById(R.id.layer_card_menu)
            layerTitleTextView = itemView.findViewById(R.id.layer_card_title)
            visibilityIndicator.visibility = View.GONE
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
            outerCardView.setCardBackgroundColor(Color.WHITE)
            layerMenuButton.visibility = View.VISIBLE
            layerVisibilityButton.visibility = View.VISIBLE
        }

        fun deselect(){
            outerCardView.setCardBackgroundColor(Color.TRANSPARENT)
            layerMenuButton.visibility = View.GONE
            layerVisibilityButton.visibility = View.GONE
        }

        override fun onItemSelected() {
            outerCardView.alpha = CanvasPreferences.MEDIUM_ALPHA
            vibrate(itemView.context)
        }
        override fun onItemDropped() {
            outerCardView.alpha = CanvasPreferences.FULL_ALPHA
            canvasIV.invalidateLayers()
            canvasIV.addActionToHistory(CanvasImageView.ACTION_LAYER_MOVE)
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
        canvasViewModel.cvImage.swapLayers(fromPosition, toPosition)
    }

    private fun getContext() = mRecyclerView?.context

    @Suppress("SameParameterValue")
    private fun showToast(text: String){
        if(text.length>30) toast.duration = Toast.LENGTH_LONG
        else toast.duration = Toast.LENGTH_SHORT
        toast.cancel()
        val context = mRecyclerView?.context ?: return
        toast = Toast.makeText(context, text, toast.duration)
        toast.show()
    }

    interface ItemTouchHelperViewHolder {
        fun onItemSelected()
        fun onItemDropped()
    }
}

