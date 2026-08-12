package com.stonicai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.stonicai.app.nav.StonicNavHost
import com.stonicai.app.ui.theme.StonicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StonicTheme {
                StonicNavHost()
            }
        }
    }
}
