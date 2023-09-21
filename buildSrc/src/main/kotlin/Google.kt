object Google {
    private const val materialVersion = "1.9.0"
    private const val playServiceLocationVersion = "21.0.1"
    private const val secretsVersion = "2.0.1"
    private const val gsonVersion = "2.10"
    private const val gooleServiceVersion = "4.3.14"
    private const val autoUpdateVersion = "2.1.0"
    private const val coreVersion = "1.10.3"

    const val material = "com.google.android.material:material:$materialVersion"
    const val playServiceLocation = "com.google.android.gms:play-services-location:$playServiceLocationVersion"
    const val secrets = "com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:$secretsVersion"
    const val gson = "com.google.code.gson:gson:$gsonVersion"
    const val googleService = "com.google.gms:google-services:$gooleServiceVersion"

    const val core="com.google.android.play:core:$autoUpdateVersion"

    const val autoUpdate="com.google.android.play:app-update:$autoUpdateVersion"
    const val autoUpdateKtx="com.google.android.play:app-update-ktx:$autoUpdateVersion"
}