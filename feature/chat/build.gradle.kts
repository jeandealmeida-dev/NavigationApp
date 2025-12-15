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
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.fragment)

    // RxJava
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.rxkotlin)

    // Skeleton
    // implementation(libs.skeleton) // Commented out - library not available

    // Picasso
    implementation(libs.picasso)

    // TODO: Add Supabase dependencies when ready to integrate
    // implementation("io.github.jan-tennert.supabase:postgrest-kt:VERSION")
    // implementation("io.github.jan-tennert.supabase:realtime-kt:VERSION")
    // implementation("io.ktor:ktor-client-android:VERSION")
}