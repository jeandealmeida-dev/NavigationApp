plugins {
    id(ProjectPlugins.Library)
    id(ProjectPlugins.MyPlugin)
    id(ProjectPlugins.NavigationSafeArgs)
    id(ProjectPlugins.Kapt)
    id(ProjectPlugins.Hilt)
}

dependencies {
    implementationPackLibraries {
        addHiltDependencies(it)
    }

    implementation(libs.play.services.location)
    implementation("androidx.media3:media3-exoplayer:1.9.0")
}