package com.mipapadakis.canvas.ui.gallery

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
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
import com.mipapadakis.canvas.tools.CvFileHelper


class GalleryFragment : Fragment() {
    //private lateinit var galleryViewModel: GalleryViewModel
    private lateinit var interfaceMainActivity: InterfaceMainActivity
    private lateinit var fab: FloatingActionButton
    lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //galleryViewModel = ViewModelProvider(this).get(GalleryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_gallery, container, false)
        fab = root.findViewById(R.id.fab)
        fab.setOnClickListener {
            val navController = activity?.findNavController(R.id.nav_host_fragment)
            navController?.navigate(R.id.nav_canvas)
        }

//        val imageList = arrayListOf(
//            createCvImage("image 1", R.drawable.baseline_star_outline_black_48),
//            createCvImage("image 2", R.drawable.baseline_brush_black_48),
//            createCvImage("image 3", R.drawable.baseline_collections_black_48),
//            createCvImage("image 4", R.drawable.baseline_color_lens_black_48),
//            createCvImage("image 5", R.drawable.baseline_create_black_48))
        GalleryViewModel.setImages(CvFileHelper(requireContext()).getAllCvImages())
        val stringBuffer = StringBuffer(".\nAbsolute Path: ${context?.filesDir?.absolutePath}\nCanonical Path: ${context?.filesDir?.canonicalPath}\nFiles:\n")
        for(f in context?.fileList()?: arrayOf("")) stringBuffer.append(f+"\n")
        Log.v("CanvasDebug", stringBuffer.toString())

        recyclerView = root.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = GalleryCvImageAdapter(GalleryViewModel.images, this)
//        recyclerView.addOnItemTouchListener(
//                RecyclerViewTouchListener(context, recyclerView, object : RecyclerViewTouchListener.ClickListener {
//                    override fun onItemClick(view: View?, position: Int) {
//                        showToast("Click at position $position")
//                    }
//
//                    override fun onLongClick(view: View?, position: Int) {
//                        showToast("Long click at position $position")
//                    }
//
//                    override fun onDoubleClick(view: View?, position: Int) {
//                        showToast("Double click at position $position")
//                    }
//
//                    override fun onBackgroundClick() {
//                        showToast("Background click")
//                    }
//                })
//        )
        return root
    }

    private fun createCvImage(title: String, drawableId: Int): CvImage {
        val bmp = Bitmap.createBitmap(BitmapFactory.decodeResource(context?.resources, drawableId))
        return CvImage(resources, title, bmp)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        interfaceMainActivity = activity as InterfaceMainActivity
    }

    private fun showToast(text: String){
        interfaceMainActivity.showToast(text)
    }
}