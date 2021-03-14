package com.mipapadakis.canvas.ui.canvas

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mipapadakis.canvas.InterfaceMainActivity
import com.mipapadakis.canvas.R

class CanvasFragment : Fragment() {
    private lateinit var canvasViewModel: CanvasViewModel
    private lateinit var interfaceMainActivity: InterfaceMainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        interfaceMainActivity.showFab()//TODO eventually replace this with hideFab()
        interfaceMainActivity.setFabListener {
            val str = "Random Number: " + (Math.random()*100).toInt()
            showToast("Fab pressed at Canvas fragment!")
            canvasViewModel.setText(str)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        canvasViewModel = ViewModelProvider(this).get(CanvasViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_canvas, container, false)

        val textView: TextView = root.findViewById(R.id.text_canvas)
        canvasViewModel.text.observe( viewLifecycleOwner, {
            textView.text = it
        })

        return root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        interfaceMainActivity = activity as InterfaceMainActivity
    }

    private fun showToast(text: String){
        interfaceMainActivity.showToast(text)
    }
}