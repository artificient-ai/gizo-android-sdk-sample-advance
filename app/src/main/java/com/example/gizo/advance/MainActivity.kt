package com.example.gizo.advance

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import de.artificient.gizo.sdk.Gizo
import com.example.gizo.advance.recording.presentation.RecordingActivity

class MainActivity : ComponentActivity() {

    private val recordingPermission: Array<String>
        get() {
            var basePermission = Gizo.app.permissionRequired
            val sdkVersion33OrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            if (sdkVersion33OrAbove) basePermission += arrayOf(
                Manifest.permission.POST_NOTIFICATIONS,
            )
            return basePermission
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current

            val launcherDrivePermission = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissionsMap ->
                val areGranted = permissionsMap.mapNotNull {
                    it.value
                }.reduce { acc, next -> acc && next }

                if (areGranted)
                    startDriveNow(context)
            }

            Screen(onDriveNow = {
                checkAndRequestLocationPermissions(
                    context,
                    recordingPermission,
                    launcherDrivePermission
                ) {
                    startDriveNow(context)
                }

            })
        }

    }

    private fun startDriveNow(context: Context) {
        startActivity(Intent(context, RecordingActivity::class.java))
        finish()
    }

    private fun checkAndRequestLocationPermissions(
        context: Context,
        permission: Array<String>,
        launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
        hasPermissionCallback: () -> Unit
    ) {

        if (
            permission.all {
                ContextCompat.checkSelfPermission(
                    context,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            hasPermissionCallback()
            // Use location because permissions are already granted
        } else {
            // Request permissions
            launcher.launch(permission)
        }
    }
}

@Preview
@Composable
fun Screen(onDriveNow: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { onDriveNow() },
                modifier = Modifier,
                enabled = true,
                shape = RoundedCornerShape(16.dp),
                content = {
                    Text(
                        text = "Drive Now",
                        style = MaterialTheme.typography.button.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        ),
                        color = Color.White,
                    )

                }
            )
        }
    }
}