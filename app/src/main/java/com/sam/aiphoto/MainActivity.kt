package com.sam.aiphoto

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sam.aiphoto.data.TfLiteCropClassifier
import com.sam.aiphoto.domain.AiResult
import com.sam.aiphoto.presentation.CameraPreview
import com.sam.aiphoto.presentation.CropImageAnalyzer
import com.sam.aiphoto.ui.theme.AiPhotoTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasPermission()) {
            requestPermissions(CAMERA_PERMISSIONS, 0)
        }
        setContent {
            AiPhotoTheme {
                val scaffoldState = rememberBottomSheetScaffoldState()
                val coroutineScope = rememberCoroutineScope()
                var aiResults by remember {
                    mutableStateOf(emptyList<AiResult>())
                }

                val analyzer = remember {
                    CropImageAnalyzer(
                        classifier = TfLiteCropClassifier(applicationContext),
                        onResults = {
                            aiResults = it
                        }
                    )
                }


                val controller = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(
                            CameraController.IMAGE_CAPTURE or
                                    CameraController.VIDEO_CAPTURE or
                                    CameraController.IMAGE_ANALYSIS
                        )
                        setImageAnalysisAnalyzer(
                            ContextCompat.getMainExecutor(applicationContext),
                            analyzer
                        )
                    }
                }

                val viewModel = viewModel<HomeViewModel>()
                val photos by viewModel.photos.collectAsState()
                val stroke = Stroke(width = 8f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
                )
                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 0.dp,
                    sheetContent = {
                        PhotoBottomSheet(
                            photos = photos,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        CameraPreview(
                            controller = controller,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .width(350.dp)
                                .height(350.dp)
                                .drawBehind {
                                    drawRoundRect(color = Color.Blue, style = stroke)
                                }
                                .align(Alignment.Center)
                                .padding(12.dp)
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .align(Alignment.TopCenter)
                        ) {
                            aiResults.forEach {
                                Text(text = it.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(8.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                            }
                        }

//                        IconButton(onClick = {
//                            controller.cameraSelector = if (
//                                controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
//                                CameraSelector.DEFAULT_FRONT_CAMERA
//                            else
//                                CameraSelector.DEFAULT_BACK_CAMERA
//
//                        }, modifier = Modifier.offset(15.dp, 15.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Cameraswitch,
//                                contentDescription = "Switch camera",
//                                tint = Color.White
//                            )
//                        }
//
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .align(Alignment.BottomCenter)
//                                .padding(16.dp),
//                            horizontalArrangement = Arrangement.SpaceAround
//                        ) {
//                            IconButton(onClick = {
//                                coroutineScope.launch {
//                                    scaffoldState.bottomSheetState.expand()
//                                }
//                            }) {
//                                Icon(
//                                    imageVector = Icons.Default.Photo,
//                                    contentDescription = "Open Gallery",
//                                    tint = Color.White
//                                )
//
//                            }
//
//                            IconButton(onClick = {
//                                takePhoto(
//                                    controller = controller,
//                                    onImageCaptured = viewModel::onTakePhoto
//                                )
//                            }) {
//                                Icon(
//                                    imageVector = Icons.Default.PhotoCamera,
//                                    contentDescription = "Take photo",
//                                    tint = Color.White
//                                )
//
//                            }
//
//                        }
                    }
                }
            }
        }
    }

    private fun takePhoto(
        controller: LifecycleCameraController,
        onImageCaptured: (Bitmap) -> Unit,
    ) {
        controller.takePicture(
            ContextCompat.getMainExecutor(this),
            object : OnImageCapturedCallback(){
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    onImageCaptured.invoke(image.toBitmap())
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Image", "onError: Image capture failed", exception)
                }
            }
        )
    }

    private fun hasPermission(): Boolean {
        return CAMERA_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        var CAMERA_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )
    }

}

@Composable
fun DottedSquare(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Float = 2f,
    dashPattern: FloatArray = floatArrayOf(10f, 10f)
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(
                width = 2.dp,
                shape = RoundedCornerShape(4.dp),
                brush = Brush.horizontalGradient(
                    colors = listOf(color, color, Color.Transparent, Color.Transparent, color, color),
                    startX = 0f,
                    endX = strokeWidth * 2 + 100f
                ),
            )
    )
}
