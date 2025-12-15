import org.gradle.api.JavaVersion

object Config {

    const val compileSdkVersion = 36
    const val minSdkVersion = 26
    const val targetSdkVersion = 35
    const val namespace = "com.jeandealmeida_dev.billortest"

    const val versionName = "1.0"
    const val versionCode = 2024060400 //YYYYMMDD

    const val buildToolsVersion = "35.0.0"

    object Java {
        val version = JavaVersion.VERSION_17
    }
}
