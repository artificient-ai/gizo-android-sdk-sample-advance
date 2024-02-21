object Hilt {
    const val hiltVersion = "2.50"
    private const val hiltNavigationComposeVersion = "1.1.0"
    private const val hiltWorkVersion = "1.1.0"
    const val hiltAndroid = "com.google.dagger:hilt-android:$hiltVersion"
    const val hiltAndroidCompiler = "com.google.dagger:hilt-android-compiler:$hiltVersion"
    const val hiltNavigationCompose =
        "androidx.hilt:hilt-navigation-compose:$hiltNavigationComposeVersion"
    const val hiltWork = "androidx.hilt:hilt-work:$hiltWorkVersion"
    const val hiltWorkCompiler = "androidx.hilt:hilt-compiler:$hiltWorkVersion"
}