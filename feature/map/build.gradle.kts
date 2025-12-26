import java.util.Properties

plugins {
    id(ProjectPlugins.Library)
    id(ProjectPlugins.MyPlugin)
    id(ProjectPlugins.Parcelize) // required to use parcelize
    id(ProjectPlugins.Kapt)
    id(ProjectPlugins.Hilt)
}

android {
    namespace = "com.jeandealmeida_dev.billortest.map"

    lint {
        abortOnError = false
    }

    defaultConfig {
        // Read Mapbox token from local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }
        val mapboxToken = localProperties.getProperty("MAPBOX_ACCESS_TOKEN") ?: ""

        // Make token available as a string resource
        resValue("string", "mapbox_access_token", mapboxToken)
    }
}

dependencies {
    implementation(project(ProjectDependencies.Module.commons))
    implementation(project(ProjectDependencies.Module.Core.location))

    implementationPackLibraries {
        addUnitTestDependencies(it)
        addHiltDependencies(it)
        addRoomDependencies(it)
        addRetrofitDependencies(it)
        addMoshiDependencies(it)
    }

    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.lifecycle.runtime)

    // Skeleton
    // implementation(libs.skeleton) // Commented out - library not available

    // Picasso
    implementation(libs.picasso)

    // Mapbox
    implementation(libs.mapbox.maps)
    implementation(libs.mapbox.search.android)
    implementation(libs.mapbox.search.android.ui)
    implementation(libs.mapbox.search.autofill)
    implementation(libs.mapbox.search.discover)
    implementation(libs.mapbox.search.place.autocomplete)
    implementation(libs.mapbox.navigation.android)
    implementation(libs.mapbox.navigation.ui.components)
    // Kotlin Coroutines (needed for async search operations)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")


}