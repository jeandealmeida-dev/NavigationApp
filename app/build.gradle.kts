import java.util.Properties

plugins {
    id(ProjectPlugins.Application)
    id(ProjectPlugins.MyPlugin)
    id(ProjectPlugins.NavigationSafeArgs)
    id(ProjectPlugins.Kapt)
    id(ProjectPlugins.Hilt)
    id("com.google.gms.google-services")
}

hilt {
    enableAggregatingTask = false
}

android {
    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(ProjectDependencies.Kotlin.Stdlib())

    // Project
    implementation(project(":commons"))
    implementation(project(":feature:chat"))
    implementation(project(":feature:map"))
    implementation(project(":core:location"))

    // VersionCatalog
    implementationPackLibraries {
        addHiltDependencies(it)
        addMoshiDependencies(it)
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

    //Mapbox
    implementation(libs.mapbox.maps)
    implementation(libs.mapbox.navigation.android)
    implementation(libs.mapbox.navigation.ui.components)
}