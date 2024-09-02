package com.sam.aiphoto.domain

import android.graphics.Bitmap

interface CropClassifier {
    fun classify(bitmap: Bitmap, rotation: Int): List<AiResult>
}