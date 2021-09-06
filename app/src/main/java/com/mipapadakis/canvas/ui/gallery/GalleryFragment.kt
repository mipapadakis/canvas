package com.mipapadakis.canvas.ui.gallery

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mipapadakis.canvas.InterfaceMainActivity
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.model.CvImage
import com.mipapadakis.canvas.ui.util.CvFileHelper


class GalleryFragment : Fragment() {
    private lateinit var galleryViewModel: GalleryViewModel
    private lateinit var fab: FloatingActionButton
    lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        galleryViewModel = ViewModelProvider(this).get(GalleryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_gallery, container, false)
        fab = root.findViewById(R.id.fragment_gallery_fab)
        fab.setOnClickListener {
            val navController = activity?.findNavController(R.id.nav_host_fragment)
            navController?.navigate(R.id.nav_canvas)
        }

        galleryViewModel.setImages(CvFileHelper(requireContext()).getAllCvImages())
        recyclerView = root.findViewById(R.id.fragment_gallery_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = GalleryCvImageAdapter(galleryViewModel.images, this)
        return root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        // Update gallery in case the user saved an image and pressed back
        galleryViewModel.setImages(CvFileHelper(requireContext()).getAllCvImages())
    }
}