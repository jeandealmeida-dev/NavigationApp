package plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension

class ModulePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Apply Required Plugins.
        PluginConfiguration.applyRequiredPlugins(project)
        
        // Apply config.gradle if it exists
        val configFile = project.rootProject.file("config.gradle")
        if (configFile.exists()) {
            project.rootProject.apply(mapOf("from" to "${project.rootDir}/config.gradle"))
        }

        // Configure common android build parameters.
        AndroidBuildConfiguration.configureAndroidBuild(project)

        // Config Versions catalog
        project.extensions.findByType(VersionCatalogsExtension::class.java)?.let {
            project.extensions.extraProperties["version-catalog"] = it.named("libs")
        }
    }
}

/// Ref: https://medium.com/wantedly-engineering/managing-android-multi-module-project-with-gradle-plugin-and-kotlin-4fcc126e7e49
