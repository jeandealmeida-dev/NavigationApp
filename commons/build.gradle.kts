plugins {
    id(ProjectPlugins.Library)
    id(ProjectPlugins.MyPlugin)
    id(ProjectPlugins.NavigationSafeArgs)
}

dependencies {
    implementationPackLibraries {
        addDaggerDependencies(it)
        addRxJavaDependencies(it)
    }

    implementation(libs.play.services.location)
}