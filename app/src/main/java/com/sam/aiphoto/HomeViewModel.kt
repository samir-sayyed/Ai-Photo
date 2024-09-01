package com.sam.aiphoto

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel: ViewModel() {

    private val _photos  = MutableStateFlow<List<Bitmap>>(emptyList())
    val photos = _photos.asStateFlow()


    fun onTakePhoto(bitmap: Bitmap){
        _photos.value += bitmap
    }
}