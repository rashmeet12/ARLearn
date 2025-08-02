package com.example.arlearner.ui.components

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.ar.core.*
import com.google.ar.core.exceptions.UnavailableException
import java.util.concurrent.ExecutionException
import kotlin.math.cos
import kotlin.math.sin

data class DrillMarker(
    val x: Float,
    val y: Float,
    val drillName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun ARCameraView(
    modifier: Modifier = Modifier,
    drillName: String
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var arAvailability by remember { mutableStateOf<String>("checking") }
    var placedMarkers by remember { mutableStateOf<List<DrillMarker>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Check AR availability
    LaunchedEffect(Unit) {
        checkARAvailability(context) { availability, error ->
            arAvailability = availability
            errorMessage = error
        }
    }

    when (arAvailability) {
        "checking" -> {
            Box(
                modifier = modifier.background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Initializing AR...",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }

        "supported" -> {
            // Show AR Camera with visual markers
            ARCameraContent(
                modifier = modifier,
                drillName = drillName,
                placedMarkers = placedMarkers,
                onMarkerPlaced = { x, y ->
                    // Replace existing marker (only one at a time)
                    placedMarkers = listOf(DrillMarker(x, y, drillName))
                }
            )
        }

        "not_supported" -> {
            // Fallback mode with regular camera and visual markers
            FallbackCameraView(
                modifier = modifier,
                drillName = drillName,
                errorMessage = errorMessage,
                placedMarkers = placedMarkers,
                onMarkerPlaced = { x, y ->
                    // Replace existing marker (only one at a time)
                    placedMarkers = listOf(DrillMarker(x, y, drillName))
                }
            )
        }

        else -> {
            // Error state
            Box(
                modifier = modifier.background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "AR Error",
                        color = Color.Red,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "Unknown error occurred",
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ARCameraContent(
    modifier: Modifier,
    drillName: String,
    placedMarkers: List<DrillMarker>,
    onMarkerPlaced: (Float, Float) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(modifier = modifier) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    setupCamera(this, ctx, lifecycleOwner) { x, y ->
                        onMarkerPlaced(x, y)
                        Log.d("ARCamera", "Placed drill marker for: $drillName at ($x, $y)")
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay for 3D markers
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            placedMarkers.forEach { marker ->
                draw3DCube(this, marker.x, marker.y, drillName)
            }
        }

        // Instructions overlay
        if (placedMarkers.isEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Blue.copy(alpha = 0.8f)
                )
            ) {
                Text(
                    text = "Tap on the ground to place drill marker",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
            }
        } else {
            // Show marker info
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Green.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = "✓ $drillName\nMarker Placed",
                    color = Color.White,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FallbackCameraView(
    modifier: Modifier,
    drillName: String,
    errorMessage: String?,
    placedMarkers: List<DrillMarker>,
    onMarkerPlaced: (Float, Float) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(modifier = modifier) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    setupCamera(this, ctx, lifecycleOwner) { x, y ->
                        onMarkerPlaced(x, y)
                        Log.d("FallbackCamera", "Placed drill marker for: $drillName at ($x, $y)")
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay for 3D markers
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            placedMarkers.forEach { marker ->
                draw3DCube(this, marker.x, marker.y, drillName)
            }
        }

        // Info overlay about fallback mode
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(
                containerColor = Color.Yellow.copy(alpha = 0.9f)
            )
        ) {
            Text(
                text = "Camera Mode (AR not available)",
                color = Color.White,
                modifier = Modifier.padding(8.dp),
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            )
        }

        // Instructions
        if (placedMarkers.isEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Blue.copy(alpha = 0.8f)
                )
            ) {
                Text(
                    text = "Tap anywhere to place drill marker",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
            }
        } else {
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Green.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = "✓ $drillName\nMarker Placed",
                    color = Color.White,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Function to draw a 3D-looking cube/cone as drill marker
private fun draw3DCube(drawScope: DrawScope, x: Float, y: Float, drillName: String) {
    with(drawScope) {
        val cubeSize = 60f
        val halfSize = cubeSize / 2

        // Draw 3D cube effect with multiple rectangles
        // Back face (darker)
        drawRect(
            color = Color.Red.copy(alpha = 0.6f),
            topLeft = Offset(x - halfSize + 10, y - halfSize + 10),
            size = androidx.compose.ui.geometry.Size(cubeSize, cubeSize)
        )

        // Front face (brighter)
        drawRect(
            color = Color.Red,
            topLeft = Offset(x - halfSize, y - halfSize),
            size = androidx.compose.ui.geometry.Size(cubeSize, cubeSize)
        )

        // Top face (medium shade)
        val topPoints = listOf(
            Offset(x - halfSize, y - halfSize),
            Offset(x + halfSize, y - halfSize),
            Offset(x + halfSize + 10, y - halfSize + 10),
            Offset(x - halfSize + 10, y - halfSize + 10)
        )

        // Draw connecting lines for 3D effect
        drawLine(
            color = Color.Black,
            start = Offset(x - halfSize, y - halfSize),
            end = Offset(x - halfSize + 10, y - halfSize + 10),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.Black,
            start = Offset(x + halfSize, y - halfSize),
            end = Offset(x + halfSize + 10, y - halfSize + 10),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.Black,
            start = Offset(x + halfSize, y + halfSize),
            end = Offset(x + halfSize + 10, y + halfSize + 10),
            strokeWidth = 2f
        )

        // Draw border around front face
        drawRect(
            color = Color.Black,
            topLeft = Offset(x - halfSize, y - halfSize),
            size = androidx.compose.ui.geometry.Size(cubeSize, cubeSize),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
        )

        // Add a small animated pulse effect
        val pulseRadius = (System.currentTimeMillis() % 2000) / 2000f * 20f + 30f
        drawCircle(
            color = Color.Yellow.copy(alpha = 0.3f),
            radius = pulseRadius,
            center = Offset(x, y)
        )

        // Add drill name label below the cube
        // Note: For text, we'd typically use a text composable overlay
        // This is a visual marker showing where text would go
        drawCircle(
            color = Color.White,
            radius = 8f,
            center = Offset(x, y + halfSize + 20)
        )
    }
}

private fun checkARAvailability(
    context: Context,
    callback: (String, String?) -> Unit
) {
    try {
        when (ArCoreApk.getInstance().checkAvailability(context)) {
            ArCoreApk.Availability.SUPPORTED_INSTALLED -> {
                // Try to create a session to verify it works
                try {
                    val session = Session(context)
                    session.close()
                    callback("supported", null)
                } catch (e: UnavailableException) {
                    callback("not_supported", "AR session failed: ${e.message}")
                }
            }
            ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD -> {
                callback("not_supported", "ARCore APK is too old")
            }
            ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED -> {
                callback("not_supported", "ARCore not installed")
            }
            ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE -> {
                callback("not_supported", "Device doesn't support AR")
            }
            else -> {
                callback("not_supported", "AR not available")
            }
        }
    } catch (e: Exception) {
        callback("not_supported", "Error checking AR: ${e.message}")
    }
}

private fun setupCamera(
    previewView: PreviewView,
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onTap: (Float, Float) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
        try {
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Unbind all use cases before rebinding
            cameraProvider.unbindAll()

            // Bind use cases to camera
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview
            )

            // Set up tap listener
            previewView.setOnTouchListener { _, event ->
                if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                    onTap(event.x, event.y)
                }
                true
            }

            Log.d("CameraSetup", "Camera initialized successfully")

        } catch (exc: ExecutionException) {
            Log.e("CameraSetup", "Camera binding failed", exc)
        } catch (exc: InterruptedException) {
            Log.e("CameraSetup", "Camera binding interrupted", exc)
        } catch (exc: Exception) {
            Log.e("CameraSetup", "Unexpected error in camera setup", exc)
        }

    }, ContextCompat.getMainExecutor(context))
}