package com.example.arlearner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.arlearner.ui.screens.ARScreen
import com.example.arlearner.ui.screens.DrillSelectionScreen
import com.example.arlearner.ui.screens.DrillDetailScreen
import com.example.arlearner.ui.theme.ARLearnerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ARLearnerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "drill_selection"
                    ) {
                        composable("drill_selection") {
                            DrillSelectionScreen(navController)
                        }
                        composable("drill_detail/{drillId}") { backStackEntry ->
                            val drillId = backStackEntry.arguments?.getString("drillId")?.toIntOrNull() ?: 1
                            DrillDetailScreen(drillId, navController)
                        }
                        composable("ar_screen/{drillId}") { backStackEntry ->
                            val drillId = backStackEntry.arguments?.getString("drillId")?.toIntOrNull() ?: 1
                            ARScreen(drillId, navController)
                        }
                    }
                }
            }
        }
    }
}
