package net.bkmachine.shopapp.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import coil.compose.AsyncImage
import net.bkmachine.shopapp.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * A custom LifecycleOwner that we can manually control.
 * This is crucial for freeing the camera hardware in a single-activity app.
 */
class CameraLifecycleOwner : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry

    fun doOnCreate() { lifecycleRegistry.currentState = Lifecycle.State.CREATED }
    fun doOnStart() { lifecycleRegistry.currentState = Lifecycle.State.STARTED }
    fun doOnResume() { lifecycleRegistry.currentState = Lifecycle.State.RESUMED }
    fun doOnPause() { lifecycleRegistry.currentState = Lifecycle.State.STARTED }
    fun doOnStop() { lifecycleRegistry.currentState = Lifecycle.State.CREATED }
    fun doOnDestroy() { lifecycleRegistry.currentState = Lifecycle.State.DESTROYED }
}

@Composable
fun ImageUploaderScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    
    // Create a lifecycle owner that we can destroy manually when leaving this screen
    val cameraLifecycleOwner = remember { CameraLifecycleOwner() }
    
    // CameraX bits
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var torchEnabled by remember { mutableStateOf(false) }
    
    var hasCaptured by remember { mutableStateOf(false) }
    var capturedFile by remember { mutableStateOf<File?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    
    val previewView = remember(context) { PreviewView(context) }

    // Control the lifecycle of the camera separately from the Activity
    DisposableEffect(Unit) {
        cameraLifecycleOwner.doOnCreate()
        cameraLifecycleOwner.doOnStart()
        cameraLifecycleOwner.doOnResume()
        
        onDispose {
            Log.d("ImageUploaderScreen", "Disposing screen, destroying camera lifecycle")
            cameraLifecycleOwner.doOnPause()
            cameraLifecycleOwner.doOnStop()
            cameraLifecycleOwner.doOnDestroy()
            
            // Force an unbind of everything from the provider
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (e: Exception) {
                Log.e("ImageUploaderScreen", "Final unbind failed", e)
            }
        }
    }

    // Permission Handling
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(hasCaptured, hasCameraPermission) {
        if (!hasCaptured && hasCameraPermission) {
            try {
                val cameraProvider = context.getCameraProvider()
                
                val selector = when {
                    cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                    cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                    else -> null
                }

                if (selector != null) {
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val capture = ImageCapture.Builder()
                        .setTargetRotation(previewView.display?.rotation ?: Surface.ROTATION_0)
                        .build()

                    cameraProvider.unbindAll()
                    // Bind to our CUSTOM lifecycle owner
                    val boundCamera = cameraProvider.bindToLifecycle(cameraLifecycleOwner, selector, preview, capture)
                    camera = boundCamera
                    imageCapture = capture
                    cameraError = null
                    Log.d("ImageUploaderScreen", "Camera bound to custom lifecycle")
                } else {
                    cameraError = "No camera available"
                }
            } catch (e: Exception) {
                Log.e("ImageUploaderScreen", "Camera setup failed", e)
                cameraError = "Camera initialization failed"
            }
        } else if (hasCaptured) {
            // Immediately pause the camera lifecycle when we have a result
            // This is more effective than just unbindAll() for some hardware
            Log.d("ImageUploaderScreen", "Image captured, pausing camera lifecycle")
            cameraLifecycleOwner.doOnPause()
            cameraLifecycleOwner.doOnStop()
            
            try {
                val cameraProvider = context.getCameraProvider()
                cameraProvider.unbindAll()
                camera = null
            } catch (e: Exception) {
                Log.e("ImageUploaderScreen", "Post-capture unbind failed", e)
            }
        }
    }

    // Toggle Torch
    LaunchedEffect(torchEnabled, camera) {
        camera?.cameraControl?.enableTorch(torchEnabled)
    }

    // Launcher for gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = createFileFromUri(context, it)
            capturedFile = file
            hasCaptured = true
        }
    }

    fun takePhoto() {
        val capture = imageCapture ?: return
        previewView.display?.let { display ->
            capture.targetRotation = display.rotation
        }
        val photoFile = createImageFile(context)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e("ImageUploaderScreen", "Photo capture failed", exception)
                }
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    capturedFile = photoFile
                    hasCaptured = true
                }
            }
        )
    }

    fun resetCapture() {
        capturedFile?.let { if (it.exists()) it.delete() }
        capturedFile = null
        hasCaptured = false
        torchEnabled = false
        // Resume camera lifecycle if we go back to taking a photo
        cameraLifecycleOwner.doOnStart()
        cameraLifecycleOwner.doOnResume()
    }

    fun handleUpload() {
        val file = capturedFile ?: return
        val bytes = file.readBytes()
        viewModel.uploadImage(bytes, file.name)
    }

    // Trigger reset to camera view on successful upload
    LaunchedEffect(viewModel.imageUploadStatus) {
        if (viewModel.imageUploadStatus == "Upload successful!") {
            resetCapture()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Capture & Upload Photo", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (!hasCameraPermission) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Camera permission is required")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) }) {
                            Text("Grant Permission")
                        }
                    }
                }
            } else if (!hasCaptured) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (cameraError != null) {
                        Text(text = cameraError!!, color = MaterialTheme.colorScheme.error)
                    } else {
                        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

                        // Torch Button Overlay
                        if (camera?.cameraInfo?.hasFlashUnit() == true) {
                            IconButton(
                                onClick = { torchEnabled = !torchEnabled },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                    contentDescription = "Toggle Torch",
                                    tint = if (torchEnabled) Color.Yellow else Color.White
                                )
                            }
                        }

                        // GRID OVERLAY
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 1.dp.toPx()
                            val color = Color.White.copy(alpha = 0.3f)
                            drawLine(color, Offset(size.width / 3, 0f), Offset(size.width / 3, size.height), strokeWidth)
                            drawLine(color, Offset(size.width * 2 / 3, 0f), Offset(size.width * 2 / 3, size.height), strokeWidth)
                            drawLine(color, Offset(0f, size.height / 3), Offset(size.width, size.height / 3), strokeWidth)
                            drawLine(color, Offset(0f, size.height * 2 / 3), Offset(size.width, size.height * 2 / 3), strokeWidth)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Gallery")
                    }
                    Button(
                        onClick = { takePhoto() },
                        enabled = imageCapture != null,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Take photo")
                    }
                }
            } else {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = capturedFile,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { resetCapture() },
                        enabled = !viewModel.isUploadingImage,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Try again")
                    }
                    Button(
                        onClick = { handleUpload() },
                        enabled = !viewModel.isUploadingImage,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (viewModel.isUploadingImage) "Uploading..." else "Upload")
                    }
                }
            }
            if (viewModel.imageUploadStatus != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = viewModel.imageUploadStatus!!,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener({ 
            try {
                continuation.resume(future.get())
            } catch (e: Exception) {
                Log.e("CameraProvider", "Failed to get camera provider", e)
                continuation.resumeWithException(e)
            }
        }, ContextCompat.getMainExecutor(this))
    }
}

private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir = context.externalCacheDir ?: context.cacheDir
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}

private fun createFileFromUri(context: Context, uri: Uri): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val file = File(context.cacheDir, "GALLERY_${timeStamp}.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        file.outputStream().use { output -> input.copyTo(output) }
    }
    return file
}
