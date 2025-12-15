package plugins

import Config
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object AndroidBuildConfiguration {
    fun configureAndroidBuild(project: Project) {
        val androidExtension = project.extensions.findByName("android") as? BaseExtension
        androidExtension?.apply {
            compileSdkVersion(Config.compileSdkVersion)

            namespace = Config.namespace + project.path.replace(":", ".")

            buildFeatures.viewBinding = true
            buildFeatures.buildConfig = true

            buildToolsVersion(Config.buildToolsVersion)

            defaultConfig {
                minSdk = Config.minSdkVersion
                targetSdk = Config.targetSdkVersion
                versionCode = Config.versionCode
                versionName = Config.versionName
                multiDexEnabled = true
                vectorDrawables.useSupportLibrary = true
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

                val defaultProguardFilePath = "proguard-rules.pro"
                when (this) {
                    is LibraryExtension -> consumerProguardFiles(defaultProguardFilePath)
                    is AppExtension -> buildTypes {
                        getByName("debug") {
                            isDebuggable = true
                            isMinifyEnabled = false
                            enableUnitTestCoverage = true
                        }
                        getByName("release") {
                            isDebuggable = false
                            isMinifyEnabled = true
                            isShrinkResources = true
                            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), defaultProguardFilePath)
                        }
                    }
                }

                compileOptions {
                    sourceCompatibility = Config.Java.version
                    targetCompatibility = Config.Java.version
                }

                project.tasks.withType(KotlinCompile::class.java).configureEach {
                    kotlinOptions {
                        jvmTarget = Config.Java.version.majorVersion
                    }
                }
            }
        }
    }
}
