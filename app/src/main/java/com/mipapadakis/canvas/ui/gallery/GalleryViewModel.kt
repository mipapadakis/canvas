package com.mipapadakis.canvas.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mipapadakis.canvas.model.CvImage

class GalleryViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is gallery Fragment"
    }
    val text: LiveData<String> = _text

    fun setText(text: String){
        _text.value = text
    }

    private val _images = MutableLiveData<ArrayList<CvImage>>() //List of cv files
    val images: LiveData<ArrayList<CvImage>> = _images

    fun setImages(list: ArrayList<CvImage>){
        _images.value = list
    }

    fun addImage(image: CvImage){
        _images.value?.add(image)
    }
}