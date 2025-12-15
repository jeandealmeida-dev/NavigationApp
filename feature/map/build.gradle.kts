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

    buildTypes {
        debug {
//            buildConfigField(
//                "long",
//                "DEFAULT_DELAY",
//                "${project.rootProject.extra["DEFAULT_DELAY_DEBUG"]}"
//            )
//            buildConfigField(
//                "String",
//                "API_BASE_URL",
//                "\"${project.rootProject.extra["API_BASE_URL"]}\""
//            )
        }

        release {
//            buildConfigField(
//                "long",
//                "DEFAULT_DELAY",
//                "${project.rootProject.extra["DEFAULT_DELAY_RELEASE"]}"
//            )
//            buildConfigField(
//                "String",
//                "API_BASE_URL",
//                "\"${project.rootProject.extra["API_BASE_URL"]}\""
//            )
        }
    }
}

dependencies {
    implementation(project(ProjectDependencies.Module.commons))

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
    // Kotlin Coroutines (needed for async search operations)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Google Play Services Location
    implementation(libs.play.services.location)
}