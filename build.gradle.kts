// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id(Plugins.androidApplication).version(Plugins.androidPluginVersion).apply(false)
    id(Plugins.androidLibrary).version(Plugins.androidPluginVersion).apply(false)
    id(Plugins.jetbrainsKotlinAndroid).version(Plugins.jetbrainsPluginVersion).apply(false)
    id(Plugins.kotlinPluginSerialization).version(Plugins.jetbrainsPluginVersion).apply(false)
    id(Plugins.daggerHiltAndroid).version(Hilt.hiltVersion).apply(false)
}
