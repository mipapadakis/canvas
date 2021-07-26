package com.mipapadakis.canvas.ui.about

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mipapadakis.canvas.InterfaceMainActivity
import com.mipapadakis.canvas.R

class AboutFragment : Fragment() {
    private lateinit var aboutViewModel: AboutViewModel
    private lateinit var interfaceMainActivity: InterfaceMainActivity

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
//        aboutViewModel = ViewModelProvider(this).get(AboutViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_about, container, false)
//        val textView: TextView = root.findViewById(R.id.text_about)
//        aboutViewModel.text.observe( viewLifecycleOwner, {
//            textView.text = it
//        })
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