package com.mipapadakis.canvas.ui.canvas.toolbar.bottom

import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.ui.util.ShowTipDialog
import com.mipapadakis.canvas.ui.canvas.CanvasImageView
import com.mipapadakis.canvas.ui.canvas.CanvasViews
import com.mipapadakis.canvas.ui.canvas.toolbar.bottom.editors.LayerListAdapter


class LayerProperties(canvasViews: CanvasViews) {
    private var layerAddBtn = canvasViews.layersLayout.findViewById<ImageButton>(R.id.property_layers_add_btn)
    private val context = canvasViews.layersLayout.context
    private var toast = Toast(context)

    init {
        layerAddBtn.setOnClickListener {
            if(canvasViews.canvasViewModel.cvImage.layerCount()>=15){
                showToast("Try merging some of the existing layers first to save up memory.")
                return@setOnClickListener
            }
            canvasViews.canvasViewModel.cvImage.newLayer()
            canvasViews.canvasIV.invalidateLayers()
            canvasViews.canvasIV.addActionToHistory(CanvasImageView.ACTION_LAYER_ADD)
        }
        //Create recyclerView with the list of layers.
        canvasViews.layerRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        canvasViews.layerRecyclerView.itemAnimator = DefaultItemAnimator()
        canvasViews.layerRecyclerView.adapter = LayerListAdapter(canvasViews.canvasIV, canvasViews.canvasViewModel, canvasViews.layersLayout.resources)
        getItemTouchHelper(canvasViews.layerRecyclerView).attachToRecyclerView(canvasViews.layerRecyclerView)

        ShowTipDialog(layerAddBtn, R.drawable.add_layer)
    }

    private fun getItemTouchHelper(layerRecyclerView: RecyclerView): ItemTouchHelper {
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT + ItemTouchHelper.RIGHT, 0) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                if(layerRecyclerView.adapter is LayerListAdapter)
                    (layerRecyclerView.adapter as LayerListAdapter).onItemMove( viewHolder.adapterPosition, target.adapterPosition )
                return true
            }
            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    if (viewHolder is LayerListAdapter.ItemTouchHelperViewHolder) {
                        (viewHolder as LayerListAdapter.ItemTouchHelperViewHolder).onItemSelected()
                    }
                }
                super.onSelectedChanged(viewHolder, actionState)
            }
            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder){
                super.clearView(recyclerView, viewHolder)
                if (viewHolder is LayerListAdapter.ItemTouchHelperViewHolder) {
                    (viewHolder as LayerListAdapter.ItemTouchHelperViewHolder).onItemDropped()
                }
            }
            override fun isLongPressDragEnabled(): Boolean { return true }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
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