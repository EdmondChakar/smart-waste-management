package com.example.smartwastemobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.smartwastemobile.core.navigation.AppNavGraph
import com.example.smartwastemobile.ui.theme.SmartWasteMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SmartWasteMobileTheme {
                AppNavGraph()
            }
        }
    }
}