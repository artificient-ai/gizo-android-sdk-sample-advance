package com.example.gizo.advance

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
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
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import com.example.gizo.advance.recording.presentation.camera.RecordingCameraActivity
import com.example.gizo.advance.recording.presentation.nocamera.RecordingNoCameraActivity
import com.example.gizo.advance.util.hasUsageStatsPermission
import de.artificient.gizo.sdk.Gizo

class MainActivity : ComponentActivity() {
    companion object {
        private val TAG = MainActivity::class.simpleName
    }
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

            val usageAccessSettingsIntent = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { activityResult ->
                if (activityResult.resultCode == RESULT_OK) {
                    onPermissionUsageAccessIntentGrant(areGranted = true)
                } else
                    onPermissionUsageAccessIntentGrant(areGranted = false)
            }

            val launcherDrivePermission = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissionsMap ->
                val areGranted = permissionsMap.mapNotNull {
                    it.value
                }.reduce { acc, next -> acc && next }

                if (areGranted)
                    if (recordingPermission.contains(Manifest.permission.READ_PHONE_STATE)) {
                        if (hasUsageStatsPermission()) {
                            Log.d(TAG,"hasUsageStatsPermission")
                        } else {
                            usageAccessSettingsIntent.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        }
                    } else {
                        Log.d(TAG,"hasUsageStatsPermission")
                    }
            }

            Screen(
                onDriveNow = {
                    checkAndRequestLocationPermissions(
                        context,
                        recordingPermission,
                        launcherDrivePermission
                    ) {
                        if (recordingPermission.contains(Manifest.permission.READ_PHONE_STATE)) {
                            if (hasUsageStatsPermission()) {
                                startDriveNow(context)
                            } else {
                                usageAccessSettingsIntent.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            }
                        } else {
                            startDriveNow(context)
                        }
                    }
                },
                onDriveNowWithoutCamera = {
                    checkAndRequestLocationPermissions(
                        context,
                        recordingPermission,
                        launcherDrivePermission
                    ) {
                        if (recordingPermission.contains(Manifest.permission.READ_PHONE_STATE)) {
                            if (hasUsageStatsPermission()) {
                                startDriveNowWithoutCamera(context)
                            } else {
                                usageAccessSettingsIntent.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            }
                        } else {
                            startDriveNowWithoutCamera(context)
                        }
                    }
                }
            )
        }
    }

    private fun startDriveNow(context: Context) {
        startActivity(Intent(context, RecordingCameraActivity::class.java))
        finish()
    }

    private fun startDriveNowWithoutCamera(context: Context) {
        startActivity(Intent(context, RecordingNoCameraActivity::class.java))
        finish()
    }

    fun onPermissionUsageAccessIntentGrant(areGranted: Boolean) {
        if (hasUsageStatsPermission()) {
            Log.d(TAG,"onPermissionUsageAccessIntentGrant")
        } else {
            Toast.makeText(
                this,
                "You need to grant all permissions to use drive now.",
                Toast.LENGTH_SHORT
            )
        }
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
fun Screen(onDriveNow: () -> Unit = {}, onDriveNowWithoutCamera: () -> Unit = {}) {

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
            Spacer(modifier = Modifier.size(18.dp))
            Button(
                onClick = { onDriveNowWithoutCamera() },
                modifier = Modifier,
                enabled = true,
                shape = RoundedCornerShape(16.dp),
                content = {
                    Text(
                        text = "Drive Now Without Camera",
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