package com.sam.aiphoto.presentation

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.sam.aiphoto.domain.AiResult
import com.sam.aiphoto.domain.CropClassifier

class CropImageAnalyzer(
    private val classifier: CropClassifier,
    private val onResults: (List<AiResult>) -> Unit
): ImageAnalysis.Analyzer {

    private var frameSkipCounter = 0

    override fun analyze(image: ImageProxy) {

        if (frameSkipCounter % 60 == 0) {
            val rotationDegrees = image.imageInfo.rotationDegrees
            val bitmap = image
                .toBitmap()
                .centerCrop(224, 224)

            val results = classifier.classify(bitmap, rotationDegrees)
            onResults(results)

        }
        frameSkipCounter++
        image.close()
    }
}