import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

object ProjectDependencies {

    object Module {
        const val app = ":app"
        const val core = ":core"
        const val commons = ":commons"

        object Core {
            const val location = ":core:location"
        }

        object Feature {
            const val chat = ":feature:chat"
            const val map = ":feature:map"
        }
    }

    // updated 29/01/2022
    object Kotlin {
        val version = "1.7.10"
        fun GradlePlugin() = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        fun Stdlib() = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$version"
    }

    // updated 10/12/2022
    object AndroidX {
        fun ArchCoreTesting() = "androidx.arch.core:core-testing:2.1.0"

        object Navigation {
            val version = "2.5.3"
            fun SafeArgs() = "androidx.navigation:navigation-safe-args-gradle-plugin:$version"
            fun UI() = "androidx.navigation:navigation-ui-ktx:$version"
            fun Fragment() = "androidx.navigation:navigation-fragment-ktx:$version"
        }
    }

    object Mockk {
        val version = "1.13.5"
        fun core() = "io.mockk:mockk:${version}"
        fun android() = "io.mockk:mockk-android:${version}"
    }
}