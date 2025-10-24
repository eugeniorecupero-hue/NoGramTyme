package com.example.nogramtime

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme

/**
 * Full screen activity shown when Instagram is blocked. It draws over the
 * current app using the application overlay window type. The content is
 * provided by [OverlayContent] below.
 */
class OverlayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure this window is treated as an application overlay. Requires
        // SYSTEM_ALERT_WINDOW permission granted by the user.
        window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        setContent {
            OverlayContent(onExit = { finish() })
        }
    }
}

@Composable
fun OverlayContent(onExit: () -> Unit) {
    Surface(color = Color(0xCC000000), modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.overlay_default_message),
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onExit) {
                Text(text = stringResource(id = R.string.exit))
            }
        }
    }
}