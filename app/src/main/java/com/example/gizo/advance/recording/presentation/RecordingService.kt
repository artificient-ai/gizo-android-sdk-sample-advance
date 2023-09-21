package com.example.gizo.advance.recording.presentation

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.lifecycle.*
import dagger.hilt.android.AndroidEntryPoint
import com.example.gizo.advance.R
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class RecordingService : LifecycleService() {

    companion object {
        private val TAG = RecordingService::class.simpleName
        const val ACTION_START_WITH_PREVIEW: String = "ACTION_START_WITH_PREVIEW"
        const val ACTION_STOP_RECORDING: String = "ACTION_STOP_RECORDING"
        const val ACTION_START_RECORDING: String = "ACTION_START_RECORDING"
        const val BIND_USECASE: String = "bind_usecase"
        const val CHANNEL_NAME: String = "recording service"
        const val CHANNEL_ID: String = "recording_service"
        const val ONGOING_NOTIFICATION_ID: Int = 2345
    }

    private val pendingActions: HashMap<String, Runnable> = hashMapOf()

    private lateinit var dangerPlayer: MediaPlayer

    class RecordingServiceBinder(private val service: RecordingService) : Binder() {
        fun getService(): RecordingService {
            return service
        }
    }

    @Inject
    lateinit var assistedFactory: RecordingViewModel.ViewModelAssistedFactory
    private val viewModel: RecordingViewModel by lazy {
        ViewModelProvider(
            applicationContext as ViewModelStoreOwner, RecordingViewModel.Factory(
                assistedFactory
            )
        )[RecordingViewModel::class.java]
    }

    private var isItHasNotification = false

    private lateinit var recordingServiceBinder: RecordingServiceBinder

    private var startTime: Long = Date().time

    private var backgroundTime: Long? = null
    override fun onCreate() {
        super.onCreate()
        startTime = Date().time
        Log.d(TAG, "service onCreate")
        recordingServiceBinder = RecordingServiceBinder(this)
        dangerPlayer = MediaPlayer.create(this, R.raw.gizo_alert_danger)
        lifecycleScope.launch {
            viewModel.uiState
                .flowWithLifecycle(lifecycle)
                .collect {
                    updateNotification(isRecording = it.inProgress)
                }
        }
        lifecycleScope.launch {
            viewModel.ttcDangerFlow
                .flowWithLifecycle(lifecycle)
                .collect {
                    if (it) {
                        if (dangerPlayer.isPlaying.not())
                            dangerPlayer.start()
                    }
                }
        }
        lifecycleScope.launch {
            viewModel.event
                .flowWithLifecycle(lifecycle)
                .collect { event ->
                    when (event) {
                        is RecordingUiEvent.Error -> {
                            Toast.makeText(applicationContext, event.message, Toast.LENGTH_SHORT)
                                .show()
                        }

                        is RecordingUiEvent.Alert -> {
                            Toast.makeText(applicationContext, event.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "service onStartCommand action: " + intent?.action)
        when (intent?.action) {
            ACTION_START_WITH_PREVIEW -> {
                if (viewModel.cameraInitialized.not()) {
                    initializeCamera()
                }
            }

            ACTION_START_RECORDING -> {
                if (viewModel.cameraInitialized)
                    viewModel.startProgress()

            }

            ACTION_STOP_RECORDING -> {

                if (viewModel.cameraInitialized)
                    viewModel.stopProgress()
            }
        }
        return START_NOT_STICKY
    }

    private fun initializeCamera() {
        Log.d(TAG, "service initializeCamera")
        Log.d(TAG, "service createVideoCaptureUseCase")
        viewModel.start(lifecycleOwner = this) {
            Log.d(TAG, "service createVideoCaptureUseCase Done")
            val action = pendingActions[BIND_USECASE]
            action?.run()
            pendingActions.remove(BIND_USECASE)
        }
    }

    fun bindPreviewUseCase(previewView: PreviewView?) {
        Log.d(TAG, "service bindPreviewUseCase")
        if (viewModel.cameraInitialized) {
            bind(previewView)
        } else {
            pendingActions[BIND_USECASE] = Runnable {
                bind(previewView)
            }
        }
    }

    private fun stopForeground() {
        backgroundTime?.let { start ->
            Log.d(TAG, "service stopForeground duration :" + (Date().time - start))
            backgroundTime = null
        }

        Log.d(TAG, "service stopForeground")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        } else
            stopForeground(true)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(ONGOING_NOTIFICATION_ID)
        isItHasNotification = false
    }

    private fun bind(previewView: PreviewView?) {
        stopForeground()
        Log.d(TAG, "service bind")
        if (previewView != null) {
            Log.d(TAG, "service bind has preview")
            viewModel.attachPreview(previewView)
        }
    }

    private fun buildNotification(isRecording: Boolean): Notification {
        val parentStack = TaskStackBuilder.create(this)
            .addNextIntentWithParentStack(Intent(this, RecordingActivity::class.java))

        val recordingIntent = Intent(this, RecordingService::class.java).apply {
            action = if (isRecording) ACTION_STOP_RECORDING else ACTION_START_RECORDING
        }
        val recordingPendingIntent: PendingIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(
                    this,
                    0,
                    recordingIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getService(this, 0, recordingIntent, PendingIntent.FLAG_IMMUTABLE)
            }

        val actionText = if (isRecording) "Stop Recording" else "Start Recording"
        val actionIconRes = 0
        val contentTitle = "Sample Analysis"
        val contentText =
            if (isRecording) "Video recording in background" else "Video Analysis in background"

        val pendingIntent1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            parentStack.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
        } else {
            parentStack.getPendingIntent(0, 0)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.gizo_live_camera_view)
            .setContentIntent(pendingIntent1)
            .setPriority(PRIORITY_MAX)
            .setOnlyAlertOnce(true)
            .addAction(
                actionIconRes, actionText,
                recordingPendingIntent
            )
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun updateNotification(isRecording: Boolean) {
        if (isItHasNotification) {
            val notification: Notification = buildNotification(isRecording = isRecording)
            with(NotificationManagerCompat.from(this)) {
                notify(ONGOING_NOTIFICATION_ID, notification)
            }

        }
    }

    fun startRunningInForeground() {
        Log.d(TAG, "service startRunningInForeground")
        backgroundTime = Date().time

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            with(NotificationManagerCompat.from(this)) {
                createNotificationChannel(channel)
            }
        }
        val notification: Notification =
            buildNotification(isRecording = viewModel.uiState.value.inProgress)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                ONGOING_NOTIFICATION_ID,
                notification,
                FOREGROUND_SERVICE_TYPE_LOCATION or FOREGROUND_SERVICE_TYPE_CAMERA
            )
        } else
            startForeground(ONGOING_NOTIFICATION_ID, notification)
        isItHasNotification = true
    }

    override fun onDestroy() {
        Log.d(TAG, "service onDestroy")
        pendingActions.clear()
        stopForeground()
        dangerPlayer.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        Log.d(TAG, "service onBind")
        return recordingServiceBinder
    }
}