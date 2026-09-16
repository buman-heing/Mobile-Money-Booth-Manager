package com.moneybooth.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.moneybooth.app.ui.navigation.NavGraph
import com.moneybooth.app.ui.theme.MoneyBoothTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as MoneyBoothApplication).container

        setContent {
            MoneyBoothTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavGraph(container = container)
                }
            }
        }
    }
}
