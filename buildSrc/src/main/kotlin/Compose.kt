object Compose {
    const val composeVersion = "1.5.4"
    private const val composeBOMVersion = "2023.10.01"

    private const val material3Version = "1.1.2"
    private const val materialVersion = "1.5.4"
    private const val composeActivitiesVersion = "1.8.2"
    private const val composeCoilVersion = "2.5.0"
    private const val composeFoundationVersion = "1.3.1"

    const val composeBom = "androidx.compose:compose-bom:${composeBOMVersion}"
    const val runtime = "androidx.compose.runtime:runtime:${composeVersion}"
    const val ui = "androidx.compose.ui:ui:${composeVersion}"
    const val uiTooling = "androidx.compose.ui:ui-tooling:${composeVersion}"
    const val uiToolingPreview = "androidx.compose.ui:ui-tooling-preview:${composeVersion}"
    const val material = "androidx.compose.material:material:${materialVersion}"
    const val material3 = "androidx.compose.material3:material3:${material3Version}"
    const val foundation = "androidx.compose.foundation:foundation:${composeFoundationVersion}"
    const val activity = "androidx.activity:activity-compose:${composeActivitiesVersion}"
    const val composeCoil= "io.coil-kt:coil-compose:$composeCoilVersion"
}