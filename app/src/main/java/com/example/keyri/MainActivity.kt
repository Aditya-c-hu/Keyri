package com.example.keyri

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.keyri.ui.home.HomeScreen
import com.example.keyri.ui.theme.KeyriTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KeyriTheme(darkTheme = true, dynamicColor = false) {
                HomeScreen()
            }
        }
    }
}
