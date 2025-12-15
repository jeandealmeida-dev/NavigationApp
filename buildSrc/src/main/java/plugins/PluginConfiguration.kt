package plugins

import org.gradle.api.Project

object PluginConfiguration {
    fun applyRequiredPlugins(project: Project) {
        project.plugins.apply("kotlin-android")
        project.plugins.apply("kotlin-kapt")
    }
}