plugins {
    id(ProjectPlugins.Library)
    id(ProjectPlugins.MyPlugin)
    id(ProjectPlugins.Parcelize) // required to use parcelize
    id(ProjectPlugins.Kapt)
    id(ProjectPlugins.Hilt)
}

android {
    lint {
        abortOnError = false
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
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.fragment)

    // RxJava
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.rxkotlin)

    // Picasso
    implementation(libs.picasso)
    // Google Play Services Location
    implementation(libs.play.services.location)
}