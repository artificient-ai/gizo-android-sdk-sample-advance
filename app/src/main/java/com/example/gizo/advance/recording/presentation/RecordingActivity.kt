package com.example.gizo.advance.recording.presentation

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.hilt.android.AndroidEntryPoint
import com.example.gizo.advance.designsystem.theme.AppTheme
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
 class RecordingActivity : ComponentActivity() {
    @Inject
    lateinit var assistedFactory: RecordingViewModel.ViewModelAssistedFactory
    private val viewModel: RecordingViewModel by lazy {
        ViewModelProvider(
            applicationContext as ViewModelStoreOwner, RecordingViewModel.Factory(
                assistedFactory
            )
        )[RecordingViewModel::class.java]
    }

    private var recordingService: RecordingService? = null

    private var previewView: PreviewView?=null

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    RecordingScreen(viewModel = viewModel,
                        onAttachPreview = { preview ->
                            previewView=preview
                            onServiceBound(recordingService)
                        },
                        onClose = { finishAction() })
                }
            }
        }
    }

    private fun finishAction() {
        stopService()
        setResult(Activity.RESULT_OK)
        super.finish()
    }

    private fun bindService() {
        val intent = Intent(this, RecordingService::class.java)
        intent.action = RecordingService.ACTION_START_WITH_PREVIEW
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun stopService() {
        val intent = Intent(this, RecordingService::class.java)
        unbindService(serviceConnection)
        stopService(intent)
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            recordingService =
                (service as RecordingService.RecordingServiceBinder).getService()
            onServiceBound(recordingService)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    private fun onServiceBound(recordingService: RecordingService?) {
        recordingService?.bindPreviewUseCase(previewView)
    }

    public override fun onStart() {
        super.onStart()
        bindService()
    }
    public override fun onStop() {
        recordingService?.startRunningInForeground()
        window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onStop()
    }

}