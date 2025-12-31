import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

private val implementation = "implementation"
private val kapt = "kapt"
private val testImplementation = "testImplementation"
private val androidTestImplementation = "androidTestImplementation"

fun DependencyHandler.addHiltDependencies(catalog: VersionCatalog) {
    add(implementation, catalog.findLibrary("hilt-android").get())
    add(kapt, catalog.findLibrary("hilt-compiler").get())
}

fun DependencyHandler.addRoomDependencies(catalog: VersionCatalog) {
    add(implementation, catalog.findLibrary("room-runtime").get())
    add(kapt, catalog.findLibrary("room-compiler").get())
    add(implementation, catalog.findLibrary("room-ktx").get())
    add(implementation, catalog.findLibrary("room-rxjava").get())
}

fun DependencyHandler.addMoshiDependencies(catalog: VersionCatalog) {
    add(implementation, catalog.findLibrary("moshi-kotlin").get())
    add(kapt, catalog.findLibrary("moshi-codegen").get())
    add(implementation, catalog.findLibrary("moshi-adapters").get())
}

fun DependencyHandler.addUnitTestDependencies(catalog: VersionCatalog) {
    add(implementation, catalog.findLibrary("jacoco").get())
    add(testImplementation, catalog.findLibrary("jacoco-agent").get())
    add(testImplementation, catalog.findLibrary("junit").get())
    add(testImplementation, catalog.findLibrary("mockk").get())
}

fun DependencyHandler.addAndroidTestDependencies(catalog: VersionCatalog) {
    add(androidTestImplementation, catalog.findLibrary("mockk-android").get())
    add(androidTestImplementation, catalog.findLibrary("androidx-junit").get())
    add(androidTestImplementation, catalog.findLibrary("androidx-espresso").get())
}

fun Project.implementationPackLibraries(function: (VersionCatalog) -> Unit) {
    function.invoke(extensions.extraProperties["version-catalog"] as VersionCatalog)
}

fun DependencyHandler.implementationModules(vararg modules: String) {
    modules.forEach { add(implementation, project(it)) }
}