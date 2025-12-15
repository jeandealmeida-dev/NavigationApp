plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("my-plugin") {
            id = "my-plugin"
            implementationClass = "plugins.ModulePlugin"
        }
    }
}

repositories{
    gradlePluginPortal()
    google()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.11.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
}