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
    implementation(project(ProjectDependencies.Module.core))

    implementationPackLibraries {
        addUnitTestDependencies(it)
        addMoshiDependencies(it)
        addHiltDependencies(it)
        addRoomDependencies(it)
    }

    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.fragment)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
}