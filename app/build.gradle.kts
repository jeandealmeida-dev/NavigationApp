import java.util.Properties

plugins {
    id(ProjectPlugins.Application)
    id(ProjectPlugins.MyPlugin)
    id(ProjectPlugins.NavigationSafeArgs)
    id(ProjectPlugins.Kapt)
    id(ProjectPlugins.Hilt)
}

hilt {
    enableAggregatingTask = false
}

android {
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
    implementation(ProjectDependencies.Kotlin.Stdlib())

    // Project
    implementation(project(":feature:chat"))
    implementation(project(":feature:map"))

    // VersionCatalog
    implementationPackLibraries {
        addHiltDependencies(it)
        addRetrofitDependencies(it)
        addRxJavaDependencies(it)
    }

    // Android
    implementation(libs.multidex)

    // UI
    implementation(libs.design)
    implementation(libs.material)

    // Android X
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.preference)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso)

    // Lifecycle
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.extensions)

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Pagging
    implementation(libs.androidx.paging.rxjava)
    implementation(libs.androidx.paging.runtime)

    // Others
    implementation(libs.stetho)

    // Picasso
    implementation(libs.picasso)
}