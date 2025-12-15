dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                username = "mapbox"
                password = providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN")
                    .orElse(providers.environmentVariable("MAPBOX_DOWNLOADS_TOKEN"))
                    .getOrElse("")
                    .ifEmpty {
                        throw GradleException("MAPBOX_DOWNLOADS_TOKEN isn't set. Set it to the project properties or to the environment variables.")
                    }
            }
        }
    }
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}
rootProject.name = "BillorTest"
include(
    ":app",
    ":commons",
    ":core",
    ":feature:chat",
    ":feature:map"
)
 