package com.example.gizo.advance

import android.app.Application
import android.util.Log
import androidx.camera.video.Quality
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import dagger.hilt.android.HiltAndroidApp
import de.artificient.gizo.sdk.Gizo
import de.artificient.gizo.sdk.model.AnalysisDelegateType
import de.artificient.gizo.sdk.setting.GizoAnalysisSettings
import de.artificient.gizo.sdk.setting.GizoAppOptions
import de.artificient.gizo.sdk.setting.GizoBatterySetting
import de.artificient.gizo.sdk.setting.GizoGpsSetting
import de.artificient.gizo.sdk.setting.GizoImuSetting
import de.artificient.gizo.sdk.setting.GizoOrientationSetting
import de.artificient.gizo.sdk.setting.GizoVideoSetting

@HiltAndroidApp
class Application : Application(), ViewModelStoreOwner {

    override fun onCreate() {
        super.onCreate()
        Gizo.initialize(
            this,
            GizoAppOptions.Builder(getString(R.string.gizo_access_token))
                .debug(true)
                .folderName("GizoSample")
                .analysisSetting(
                    GizoAnalysisSettings.Builder()
                        .allowAnalysis(true)
                        .modelName("arti_sense.data")
                        .loadDelegate(AnalysisDelegateType.Auto)
                        .saveMatrixFile(true)
                        .saveTtcCsvFile(true)
                        .build()
                )
                .imuSetting(
                    GizoImuSetting.Builder()
                        .allowAccelerationSensor(true)
                        .allowMagneticSensor(true)
                        .allowGyroscopeSensor(true)
                        .saveCsvFile(true)
                        .saveDataTimerPeriod(5000L)
                        .build()
                )
                .gpsSetting(
                    GizoGpsSetting.Builder()
                        .allowGps(true)
                        .mapBoxKey(getString(R.string.mapbox_access_token))
                        .saveCsvFile(true)
                        .build()
                )
                .videoSetting(
                    GizoVideoSetting.Builder()
                        .allowRecording(true)
                        .quality(Quality.LOWEST)
                        .build()
                )
                .batterySetting(
                    GizoBatterySetting.Builder()
                        .checkBattery(true)
                        .build()
                )
                .orientationSetting(
                    GizoOrientationSetting.Builder()
                        .allowGravitySensor(true)
                        .build()
                )
                .build()
        )

        Gizo.app.setLoadModelObserver { status ->
            Log.d("LoadModelStatus", "status:" + status.name)
        }

        Gizo.app.loadModel()

    }

    private val appViewModelStore: ViewModelStore by lazy {
        ViewModelStore()
    }
    override val viewModelStore: ViewModelStore
        get() = appViewModelStore

}