package com.mipapadakis.canvas.ui.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AboutViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "About"
    }
    val text: LiveData<String> = _text

    fun setText(text: String){
        _text.value = text
    }
}