package com.example.arlearner.ui.screens

import android.Manifest
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.arlearner.data.DrillRepository
import com.example.arlearner.ui.components.ARCameraView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ARScreen(drillId: Int, navController: NavController) {
    val drill = DrillRepository.drills.find { it.id == drillId } ?: return
    val context = LocalContext.current

    // Debug logging
    LaunchedEffect(Unit) {
        Log.d("ARScreen", "ARScreen started for drill: ${drill.name}")
    }

    // Camera permission
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            Log.d("ARScreen", "Requesting camera permission")
            cameraPermissionState.launchPermissionRequest()
        } else {
            Log.d("ARScreen", "Camera permission already granted")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.status.isGranted) {
            Log.d("ARScreen", "Loading AR Camera View")
            // AR Camera View
            ARCameraView(
                modifier = Modifier.fillMaxSize(),
                drillName = drill.name
            )
        } else {
            Log.d("ARScreen", "Showing permission request UI")
            // Permission request UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Camera permission is required for AR functionality",
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    Log.d("ARScreen", "Manual permission request")
                    cameraPermissionState.launchPermissionRequest()
                }) {
                    Text("Grant Permission")
                }
            }
        }

        // Top bar overlay
        TopAppBar(
            title = { Text("AR: ${drill.name}") },
            navigationIcon = {
                IconButton(onClick = {
                    Log.d("ARScreen", "Navigating back")
                    navController.popBackStack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black.copy(alpha = 0.7f),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )
    }
}