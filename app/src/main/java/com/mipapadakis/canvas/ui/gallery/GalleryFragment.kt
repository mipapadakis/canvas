package com.mipapadakis.canvas.ui.gallery

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mipapadakis.canvas.InterfaceMainActivity
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.model.CvImage
import com.mipapadakis.canvas.model.layer.CvLayer
import com.mipapadakis.canvas.model.shape.CvShape

class GalleryFragment : Fragment() {
    private lateinit var galleryViewModel: GalleryViewModel
    private lateinit var interfaceMainActivity: InterfaceMainActivity
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        interfaceMainActivity.showFab()
        interfaceMainActivity.setFabListener {
            showToast("Fab pressed at gallery fragment!")
            //Toast.makeText(context, "Hello from Gallery fragment", Toast.LENGTH_SHORT).show()/////
        }
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        galleryViewModel = ViewModelProvider(this).get(GalleryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_gallery, container, false)

        val imageList = arrayListOf(
            createCvImage("image 1", R.drawable.baseline_star_outline_black_48),
            createCvImage("image 2", R.drawable.baseline_brush_black_48),
            createCvImage("image 3", R.drawable.baseline_collections_black_48),
            createCvImage("image 4", R.drawable.baseline_color_lens_black_48),
            createCvImage("image 5", R.drawable.baseline_create_black_48))
        galleryViewModel.setImages(imageList)


        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        recyclerView = root.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = CvImageAdapter(galleryViewModel.images, this)
        recyclerView.addOnItemTouchListener(
                RecyclerViewTouchListener(context, recyclerView, object : RecyclerViewTouchListener.ClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        showToast("Click at position $position")
                    }

                    override fun onLongClick(view: View?, position: Int) {
                        showToast("Long click at position $position")
                    }

                    override fun onBackgroundClick() {
                        showToast("Background click")
                    }
                })
        )

        //TODO: Recycler view click listener

//        val textView: TextView = root.findViewById(R.id.text_gallery)
//        galleryViewModel.text.observe( viewLifecycleOwner, {
//            textView.text = it
//        })

        return root
    }

    fun createCvImage(title:String, drawableId: Int): CvImage{
        return CvImage(Bitmap.createBitmap(BitmapFactory.decodeResource(context?.resources,drawableId)),
            title, listOf(CvLayer(null)), listOf(CvShape(null)))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        interfaceMainActivity = activity as InterfaceMainActivity
    }

    private fun showToast(text: String){
        interfaceMainActivity.showToast(text)
    }
}