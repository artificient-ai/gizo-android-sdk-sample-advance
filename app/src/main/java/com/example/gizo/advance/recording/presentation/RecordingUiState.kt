package com.example.gizo.advance.recording.presentation

import androidx.compose.ui.graphics.ImageBitmap

 data class RecordingUiState(
    val inProgress: Boolean = false,
    val showPreview: Boolean = true,
    val isOrientationAlign: Boolean = false,
    val needGps: Boolean = false,
    val preview: ImageBitmap? = null,
    val limitSpeed: Int? = null,
    val speed: Int? = null,
    val speedNotSafe: Boolean = false,
    val warning: Boolean = false,
    val danger: Boolean = false,
    val reportAccident: Boolean = false
)

 sealed class RecordingUiEvent {
    data class Error(val message: String) : RecordingUiEvent()
    data class Alert(val message: String) : RecordingUiEvent()
}