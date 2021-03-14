package com.mipapadakis.canvas.ui.gallery

import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewTouchListener(val context: Context?, recycleView: RecyclerView, private val clickListener: ClickListener?): RecyclerView.OnItemTouchListener {
    private val gestureDetector: GestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            val child: View? = recycleView.findChildViewUnder(e.x, e.y)
            if (child != null && clickListener != null) {
                val position = recycleView.getChildAdapterPosition(child)
                clickListener.onLongClick(child, position)
            }
        }
    })

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val child: View? = rv.findChildViewUnder(e.x, e.y)

        if(gestureDetector.onTouchEvent(e)) {
            if (child != null) {
                val position = rv.getChildAdapterPosition(child)
                clickListener?.onItemClick(child, position)
            }
            else
                clickListener?.onBackgroundClick()
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    interface ClickListener {
        fun onItemClick(view: View?, position: Int)
        fun onLongClick(view: View?, position: Int)
        fun onBackgroundClick()
    }

}