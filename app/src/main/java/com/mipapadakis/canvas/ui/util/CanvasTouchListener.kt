package com.mipapadakis.canvas.ui.util

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.abs


private const val POINTER_1 = 0
private const val POINTER_2 = 1
private const val POINTER_3 = 2

/** This OnTouchListener class handles the user's touches and calls the corresponding methods of
 * @param touchListener (which implements a MultiTouchListener interface).
 * */
class CanvasTouchListener(private val touchListener: MultiTouchListener): View.OnTouchListener {
    companion object {
        val DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout().toLong() + 100
        val LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout().toLong() + 100
        private const val DISTANCE_TOLERANCE = 15L
    }

    //Example: touchEvent[POINTER_2][3] is the third touchEvent of pointer_2.
    //touchEvent[POINTER_2][3] will be set to 0 if POINTER_2 went UP within the last TIMEOUT ms
    private var touchEvent: Array<ArrayList<TouchEvent>?> = arrayOf(null, null, null)
    private var doubleTapFirstTouch: TouchEvent? = null
    private var longPressTimer: CountDownTimer? = null
    private var longPressTouchEvent: TouchEvent? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View?, event: MotionEvent?): Boolean {

        if(view==null || event==null) {
            touchListener.onCancelTouch(event)
            longPressTimer?.cancel()
            return false
        }

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                addEventToPointer(event, POINTER_1)
                touchListener.on1PointerDown(event)
                startLongPressTimerForPoint(event, POINTER_1)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    addEventToPointer(event, POINTER_2)
                    touchListener.on2PointerDown(event)
                    startLongPressTimerForPoint(event, POINTER_2)
                } else if (event.pointerCount == 3) {
                    addEventToPointer(event, POINTER_3)
                    touchListener.on3PointerDown(event)
                    startLongPressTimerForPoint(event, POINTER_3)
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                resetAllPointers()
                touchListener.onCancelTouch(event)
                longPressTimer?.cancel()
            }
            MotionEvent.ACTION_UP -> {
                if(doubleTapFirstTouch==null) doubleTapFirstTouch = TouchEvent(event.x, event.y, event.eventTime)
                pointerUp(event)
                touchListener.on1PointerUp(event)
                longPressTimer?.cancel()
                longPressTouchEvent = null
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (event.pointerCount == 2) touchListener.on2PointerUp(event)
                else if (event.pointerCount == 3)  touchListener.on3PointerUp(event)
                longPressTimer?.cancel()
                longPressTouchEvent = null
            }
            MotionEvent.ACTION_MOVE -> {
                editPositionOfPointer(event, POINTER_1)
                if (event.pointerCount > 1)
                    editPositionOfPointer(event, POINTER_2)
                if (event.pointerCount > 2)
                    editPositionOfPointer(event, POINTER_3)
                touchListener.onPointerMove(event)

                //If touch moves far from initial longPress position (determined by TOLERANCE), cancel the longPressTimer
                val whichPointer = event.pointerCount-1
                if(abs(event.getX(whichPointer).toDouble() - (longPressTouchEvent?.x ?: -DISTANCE_TOLERANCE).toDouble()) > DISTANCE_TOLERANCE
                        ||abs(event.getY(whichPointer).toDouble() - (longPressTouchEvent?.y ?: -DISTANCE_TOLERANCE).toDouble()) > DISTANCE_TOLERANCE ){
                    longPressTimer?.cancel()
                    longPressTouchEvent = null
                }
            }
        }
        return true
    }

    private fun pointerUp(event: MotionEvent){
        dismissOldTouches(event)
        val numberOfClicks = arrayOf(numberOfPointerClicks(POINTER_1), numberOfPointerClicks(
            POINTER_2
        ), numberOfPointerClicks(POINTER_3))
        when {
            numberOfClicks.contentEquals(arrayOf(1, 0, 0)) -> touchListener.on1PointerTap(event)
            numberOfClicks.contentEquals(arrayOf(1, 1, 0)) -> touchListener.on2PointerTap(event)
            numberOfClicks.contentEquals(arrayOf(1, 1, 1)) -> touchListener.on3PointerTap(event)
            numberOfClicks.contentEquals(arrayOf(2, 0, 0)) -> touchListener.on1PointerDoubleTap(event)
            numberOfClicks.contentEquals(arrayOf(2, 2, 0)) -> touchListener.on2PointerDoubleTap(event)
            numberOfClicks.contentEquals(arrayOf(2, 2, 2)) -> touchListener.on3PointerDoubleTap(event)
        }
    }
    private fun resetPointer(point: Int){ touchEvent[point] = null }
    private fun resetAllPointers(){ touchEvent = arrayOf(null, null, null) }
    private fun addEventToPointer(event: MotionEvent, point: Int){
        dismissOldTouches(event)
        if(touchEvent[point]==null) touchEvent[point] = ArrayList()
        touchEvent[point]?.add(TouchEvent(event.getX(point), event.getY(point), event.eventTime))
    }
    private fun dismissOldTouches(event: MotionEvent){
        for(point in 0 until 3){
            if(touchEvent[point]!=null && event.eventTime - touchEvent[point]!!.last().time > DOUBLE_TAP_TIMEOUT)
                resetPointer(point)
        }
    }
    private fun editPositionOfPointer(event: MotionEvent, point: Int){
        touchEvent[point]?.last()?.x = event.getX(point)
        touchEvent[point]?.last()?.y = event.getY(point)
    }
    private fun numberOfPointerClicks(point: Int) = touchEvent[point]?.size ?: 0
    private fun startLongPressTimerForPoint(event: MotionEvent, point: Int){
        longPressTimer?.cancel()
        longPressTouchEvent = null
        longPressTouchEvent = TouchEvent(event.getX(point), event.getY(point), event.eventTime)
        longPressTimer = object : CountDownTimer(LONG_PRESS_TIMEOUT, LONG_PRESS_TIMEOUT) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish(){
                when (point) {
                    POINTER_1 -> touchListener.on1PointerLongPress(event)
                    POINTER_2 -> touchListener.on2PointerLongPress(event)
                    else -> touchListener.on3PointerLongPress(event)
                }
            }
        }.start()
    }

    interface MultiTouchListener{
        fun on1PointerTap(event: MotionEvent){
            Log.i("CanvasTouchListener", "on1PointerTap")
        }
        fun on2PointerTap(event: MotionEvent){
            Log.i("CanvasTouchListener", "on2PointerTap")
        }
        fun on3PointerTap(event: MotionEvent){
            Log.i("CanvasTouchListener", "on3PointerTap")
        }
        fun on1PointerDoubleTap(event: MotionEvent){
            Log.i("CanvasTouchListener", "on1PointerDoubleTap")
        }
        fun on2PointerDoubleTap(event: MotionEvent){
            Log.i("CanvasTouchListener", "on2PointerDoubleTap")
        }
        fun on3PointerDoubleTap(event: MotionEvent){
            Log.i("CanvasTouchListener", "on3PointerDoubleTap")
        }
        fun on1PointerLongPress(event: MotionEvent){
            Log.i("CanvasTouchListener", "on1PointerLongPress")
        }
        fun on2PointerLongPress(event: MotionEvent){
            Log.i("CanvasTouchListener", "on2PointerLongPress")
        }
        fun on3PointerLongPress(event: MotionEvent){
            Log.i("CanvasTouchListener", "on3PointerLongPress")
        }
        fun on1PointerDown(event: MotionEvent){}
        fun on2PointerDown(event: MotionEvent){}
        fun on3PointerDown(event: MotionEvent){}
        fun on1PointerUp(event: MotionEvent){}
        fun on2PointerUp(event: MotionEvent){}
        fun on3PointerUp(event: MotionEvent){}
        fun onPointerMove(event: MotionEvent){}
        fun onCancelTouch(event: MotionEvent?){
            Log.i("CanvasTouchListener", "on2PointerDoubleTap")
        }
    }
}

private class TouchEvent(var x: Float, var y: Float, val time: Long)