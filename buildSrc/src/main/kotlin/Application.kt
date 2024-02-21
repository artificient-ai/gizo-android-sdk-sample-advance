import org.gradle.api.JavaVersion

object Application {
    /*
    MAJOR version when you make incompatible API changes,
    MINOR version when you add functionality in a backwards-compatible manner, and
    PATCH version when you make backwards-compatible bug fixes.
 */
    private const val versionMajor = 0
    private const val versionMinor = 2
    private const val versionPatch = 0
    const val compileSdk = 34
    const val minSdk = 26
    const val targetSdk = 34
    const val versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
    const val versionName = "$versionMajor.$versionMinor.$versionPatch"
    const val namespace = "com.example.gizo.advance"
    const val applicationId = "de.artificient.gizo.sdk.sample"
    const val testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    const val jvmTarget = "1.8"
    val jvmVersion = JavaVersion.VERSION_1_8
    const val kotlinCompilerExtensionVersion = Compose.composeVersion
}