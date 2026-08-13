package com.institute.calling

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.institute.calling.ui.navigation.CallingNavHost
import com.institute.calling.ui.theme.InstituteCallingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            InstituteCallingTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                    // Pad to the safe area (status bar + gesture/nav bar) once here, so
                    // no screen's content runs under the system bars on edge-to-edge devices.
                    Box(Modifier.fillMaxSize().systemBarsPadding()) {
                        // SessionViewModel here is scoped to the Activity, so the whole
                        // auth flow shares one instance.
                        CallingNavHost()
                    }
                }
            }
        }
    }
}