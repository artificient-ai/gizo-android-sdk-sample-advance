package com.example.gizo.advance.recording.presentation

import android.hardware.SensorEvent
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.*
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.artificient.gizo.sdk.Gizo
import de.artificient.gizo.sdk.GizoAnalysis
import de.artificient.gizo.sdk.model.TTCAlert
import de.artificient.gizo.sdk.model.BatteryStatus
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class RecordingViewModel @AssistedInject constructor() : ViewModel() {
    private val gizoAnalysis: GizoAnalysis = Gizo.app.gizoAnalysis
    private val _event = Channel<RecordingUiEvent>()
    val event = _event.receiveAsFlow()
    private val _uiState = MutableStateFlow(RecordingUiState())
    val uiState = _uiState.asStateFlow()
    private val gpsRequested = false
    private val ttcAlertFlow = MutableStateFlow(TTCAlert.None)

    @OptIn(FlowPreview::class)
    val ttcDangerFlow = ttcAlertFlow
        .flatMapConcat { alert ->
            flow {
                if (alert == TTCAlert.Collision) {
                    emit(true)
                    delay(2000)
                    emit(false)
                    delay(15000)
                } else
                    emit(false)
            }
        }.distinctUntilChanged()

    @OptIn(FlowPreview::class)
    val ttcWarningFlow = ttcAlertFlow
        .flatMapConcat { alert ->
            flow {
                if (alert == TTCAlert.Tailgating) {
                    emit(true)
                    delay(2000)
                } else
                    emit(false)
            }
        }.distinctUntilChanged()
    val cameraInitialized
        get() = gizoAnalysis.cameraInitialized

    init {
        gizoAnalysis.onBatteryStatusChange = { status ->
            when (status) {
                BatteryStatus.LOW_BATTERY_STOP -> {
                    _event.trySend(RecordingUiEvent.Alert("Battery is low, we will stop recording"))
                    stopProgress()
                    gizoAnalysis.lockAnalysis(true)
                }

                BatteryStatus.LOW_BATTERY_WARNING -> {
                    _event.trySend(RecordingUiEvent.Alert("Battery is low, we will stop analysis"))
                    gizoAnalysis.lockAnalysis(false)
                }

                BatteryStatus.NORMAL -> {
                    gizoAnalysis.lockAnalysis(false)
                }
            }
        }
        gizoAnalysis.ttcCalculator { frontObject, speed, ttc ->
            Log.d("ttcCalculator", "frontObject:$frontObject, speed:$speed, ttc:$ttc")
            ttc
        }
        gizoAnalysis.ttcStatusCalculator { ttc, speed, ttcStatus ->
            Log.d("ttcStatusCalculator", "ttc:$ttc, speed:$speed, ttcStatus:$ttcStatus")
            ttcStatus
        }
        gizoAnalysis.onAnalysisResult = { preview,
                                          ttc,
                                          ttcStatus,
                                          frontObject,
                                          speed,
                                          gpsTime ->
            Log.d(
                "onAnalysisResult",
                "ttc:$ttc, speed:$speed, ttcStatus:$ttcStatus, frontObject:$frontObject, gpsTime:$gpsTime"
            )
            ttcAlertFlow.tryEmit(ttcStatus)
            _uiState.update {
                it.copy(
                    preview = preview?.asImageBitmap(),
                )
            }
        }
        gizoAnalysis.onSessionStatus = { inProgress, previewAttached ->
            _uiState.update {
                it.copy(
                    inProgress = inProgress,
                    showPreview = previewAttached
                )
            }
        }
        gizoAnalysis.onLocationChange = { location, isGpsOn ->
            Log.d("onLocationChange", "location:$location, isGpsOn:$isGpsOn")
            if (isGpsOn == false && gpsRequested.not())
                _uiState.update {
                    it.copy(
                        needGps = true,
                    )
                }
        }
        gizoAnalysis.onSpeedChange = { speedLimitKph, speedKph ->
            _uiState.update {
                it.copy(
                    limitSpeed = speedLimitKph,
                    speed = speedKph,
                    speedNotSafe = speedLimitKph != null && speedLimitKph > 0 && speedKph > 0 && speedKph > speedLimitKph
                )
            }
        }
        ttcWarningFlow.onEach { warning ->
            _uiState.update {
                it.copy(
                    warning = warning,
                )
            }
        }.launchIn(viewModelScope)

        ttcDangerFlow.onEach { danger ->
            _uiState.update {
                it.copy(
                    danger = danger,
                )
            }
        }.launchIn(viewModelScope)
        gizoAnalysis.onAccelerationSensor = { sensorEvent ->
            Log.d("onAccelerationSensor", sensorEvent.toLogString())
        }
        gizoAnalysis.onLinearAccelerationSensor = { sensorEvent ->
            Log.d("LinearAcceleration", sensorEvent.toLogString())
        }
        gizoAnalysis.onAccelerationUncalibratedSensor = { sensorEvent ->
            Log.d("AccelerationUncalibrate", sensorEvent.toLogString())
        }
        gizoAnalysis.onGyroscopeSensor = { sensorEvent ->
            Log.d("onGyroscopeSensor", sensorEvent.toLogString())
        }
        gizoAnalysis.onMagneticSensor = { sensorEvent ->
            Log.d("onMagneticSensor", sensorEvent.toLogString())
        }
        gizoAnalysis.onGravitySensor = { sensorEvent ->
            Log.d("onGravitySensor", sensorEvent.toLogString())
        }
        gizoAnalysis.onGravityAlignmentChange = { isAlign ->
            Log.d("onAlignmentChange", "isAlign:$isAlign")
            if (isAlign == true)
                _uiState.update {
                    it.copy(isOrientationAlign = isAlign)
                }
        }
    }

    private fun SensorEvent?.toLogString(): String {
        return "timestamp:${this?.timestamp} name:${this?.sensor?.name} accuracy:${this?.accuracy} sensor:${this?.sensor}"
    }

    fun attachPreview(previewView: PreviewView) {
        gizoAnalysis.attachPreview(previewView)
    }

    private fun getBatteryStatus() =
        gizoAnalysis.batteryLastStatus

    fun togglePreview(previewView: PreviewView) {
        if (uiState.value.showPreview) {
            gizoAnalysis.lockPreview()
        } else {
            gizoAnalysis.unlockPreview(previewView)
        }
    }

    fun startProgress() {
        if (uiState.value.inProgress.not()) {
            if (getBatteryStatus() == BatteryStatus.LOW_BATTERY_STOP) {
                _event.trySend(RecordingUiEvent.Alert("Battery is low, you can't start recording"))
            } else
                gizoAnalysis.startSavingSession()
        } else {
            stopProgress()
        }
    }

    fun stopProgress() {
        gizoAnalysis.stopSavingSession()
    }

    fun start(
        lifecycleOwner: LifecycleOwner,
        onDone: (() -> Unit)
    ) {
        checkOrientation()
        gizoAnalysis.start(lifecycleOwner = lifecycleOwner) {
            onDone()
        }
    }

    private fun checkOrientation() {
        _uiState.update {
            it.copy(isOrientationAlign = false)
        }
    }

    fun gpsNeedConfirm() {
        _uiState.update {
            it.copy(
                needGps = false,
            )
        }
    }

    fun gpsNeedCancel() {
        _uiState.update {
            it.copy(
                needGps = false,
            )
        }
    }

    class Factory(
        private val assistedFactory: ViewModelAssistedFactory,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return assistedFactory.create() as T
        }
    }

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(): RecordingViewModel
    }
}
