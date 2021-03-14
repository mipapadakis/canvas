package com.mipapadakis.canvas.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mipapadakis.canvas.R
import com.mipapadakis.canvas.model.CvImage

class GalleryViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is gallery Fragment"
    }
    val text: LiveData<String> = _text

    fun setText(text: String){
        _text.value = text
    }

    private val _images = MutableLiveData<List<CvImage>>()
    val images: LiveData<List<CvImage>> = _images

    fun setImages(list: List<CvImage>){
        _images.value = list
    }
}