plugins {
    id(Plugins.androidApplication)
    id(Plugins.jetbrainsKotlinAndroid)
    id(Plugins.daggerHiltAndroid)
    id(Plugins.kotlinPluginKapt)
}

android {
    namespace = Application.namespace
    compileSdk = Application.compileSdk

    defaultConfig {
        applicationId = Application.applicationId
        minSdk = Application.minSdk
        targetSdk = Application.targetSdk
        versionCode = Application.versionCode
        versionName = Application.versionName

        testInstrumentationRunner = Application.testInstrumentationRunner
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Application.kotlinCompilerExtensionVersion
    }

    compileOptions {
        sourceCompatibility =  Application.jvmVersion
        targetCompatibility =  Application.jvmVersion
    }
    kotlinOptions {
        jvmTarget = Application.jvmTarget
    }
}

dependencies {
    implementation ("com.github.artificient-ai:gizo-android-sdk-alpha:0.1.0")

    with(Android) {
        implementation(androidCore)
        implementation(appcompat)
        implementation(androidxTracingLifecycleService)
    }

    with(Google) {
        implementation(material)
    }

    with(Compose){
        implementation(composeBom)
        implementation(activity)
        implementation(ui)
        implementation(composeCoil)
        implementation(uiToolingPreview)
        implementation(uiTooling)
        implementation(material)
        implementation(foundation)
        implementation(material3)
    }

    with(Hilt){
        implementation(hiltAndroid)
        implementation(hiltNavigationCompose)
        kapt(hiltAndroidCompiler)
    }

}