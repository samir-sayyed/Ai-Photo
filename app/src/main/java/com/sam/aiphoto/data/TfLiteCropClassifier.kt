package com.sam.aiphoto.data

import android.content.Context
import android.graphics.Bitmap
import com.sam.aiphoto.domain.AiResult
import com.sam.aiphoto.domain.CropClassifier
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

class TfLiteCropClassifier(
    private val context: Context,
    private val threshold: Float = 0.5f,
    private val maxResults: Int = 1
) : CropClassifier{

    private var classifier : ImageClassifier? = null

    private fun setupClassifier(){

        val baseOptions = BaseOptions.builder()
            .setNumThreads(1)
            .build()
        val options = ImageClassifier.ImageClassifierOptions.builder()
            .setBaseOptions(baseOptions)
            .setMaxResults(maxResults)
            .setScoreThreshold(threshold)
            .build()

        try {
            classifier = ImageClassifier.createFromFileAndOptions(
                context,
                "agri.tflite",
                options,
            )
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }


    override fun classify(bitmap: Bitmap, rotation: Int): List<AiResult> {
        if (classifier == null){
            setupClassifier()
        }

        val imageProcessor = ImageProcessor.Builder().build()
        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))

        val imageProcessingOptions = ImageProcessingOptions.builder()
            .setOrientation(getOrientationFromRotation(rotation))
            .build()

        val results = classifier?.classify(tensorImage, imageProcessingOptions)

        return results?.flatMap { classification ->
            classification.categories.map { category ->
                AiResult(
                    name = category.displayName,
                    score = category.score
                )
            }
        }?.distinctBy { it.name } ?: emptyList()

    }

    private fun getOrientationFromRotation(rotation: Int): ImageProcessingOptions.Orientation{
        return when(rotation){
            0 -> ImageProcessingOptions.Orientation.BOTTOM_RIGHT
            90 -> ImageProcessingOptions.Orientation.TOP_LEFT
            180 -> ImageProcessingOptions.Orientation.RIGHT_BOTTOM
            else -> ImageProcessingOptions.Orientation.RIGHT_TOP
        }
    }

}