package com.mipapadakis.canvas.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mipapadakis.canvas.model.CvImage

class GalleryViewModel : ViewModel() {
    private val _images = MutableLiveData<ArrayList<CvImage>>() //List of cv files
    val images: LiveData<ArrayList<CvImage>> = _images

    fun setImages(list: ArrayList<CvImage>){
        _images.value = list
    }
}